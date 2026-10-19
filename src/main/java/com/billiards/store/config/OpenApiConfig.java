package com.billiards.store.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.billiards.store.common.base.ErrorResponse;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Billiard Store API",
        version = "v1.0",
        description = OpenApiConfig.DESCRIPTION
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    description = "Paste the `accessToken` returned by `POST /auth/login`. Swagger UI adds the `Bearer ` prefix itself.",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

    static final String DESCRIPTION = """
        Backend for running a billiard hall: tables, play sessions, food and drink orders, invoices, and admin reports.

        ## Typical flow
        1. `POST /auth/login` → keep `accessToken` and `refreshToken`.
        2. Send `Authorization: Bearer <accessToken>` on every other request.
        3. Start a session on a free table (`POST /play-session/create`), add orders (`POST /order-item/create`).
        4. Check out with `POST /invoice/create` (this also ends the session), show the QR (`GET /invoice/qr`), then mark it paid (`PUT /invoice/complete`).

        ## Roles
        Every endpoint states its access in its description.
        - **STAFF** can run the floor: view tables and products, start and end sessions, take orders, create and complete invoices.
        - **ADMIN** can do everything STAFF can, plus manage products, tables and prices, delete records, and use the Dashboard and Audit Log.
        - A request with a missing or expired token gets **401**; a valid token without the right role gets **403**.

        ## Responses
        - Success: HTTP 200 with `{ code, message, data, timestamp }`; the result is in `data` (null when there is nothing to return).
          `code` in the body can be 202 on some endpoints while the HTTP status is 200: use the HTTP status.
        - Error: HTTP 4xx/5xx with `{ status, message, timestamp }`.
        - `message` is human-readable and in the server's language (Vietnamese by default); show it, but do not parse it.
        - **404 is also used when a business rule is broken** (for example, a table is already occupied, stock is insufficient, the price is invalid), not only when something does not exist. Show `message` to the user.
        - 400 is returned for malformed JSON, an invalid enum value, or an invalid date range on dashboard calls.

        ## Conventions
        - Records have a generated **code** (`PRD000001`, `TBL000001`, `SES000001`, `ORI000001`, `INV000001`) and an internal **UUID** `id`. Almost all endpoints use the code.
        - Money is VND, sent and returned as numbers (e.g. `15000` or `135000.00`).
        - Date-times are ISO-8601 without a zone (`2026-10-19T18:30:00`) in the store's timezone (Asia/Ho_Chi_Minh by default). Dashboard dates are `yyyy-MM-dd`.
        - Paged endpoints take `page` (from 0), `size`, `sortBy`, `direction` and return `{ content, page, size, totalElements, totalPages, first, last }` inside `data`.
        - Deleted records are hidden, not erased (soft delete), and every change is written to the audit log.
        """;

    /** Order of the sections in Swagger UI. */
    private static final List<String> TAG_ORDER = List.of(
        "Auth", "Billiard Table", "Play Session", "Order Item", "Invoice", "Product", "Dashboard", "Audit Log");

    private static final String ERROR_REF = "#/components/schemas/ErrorResponse";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .addServersItem(new Server()
                .url("https://billiard.serverhub.id.vn")
                .description("Production (Cloudflare Tunnel)"))
            .addServersItem(new Server()
                .url("http://localhost:15678")
                .description("Local Development"));
    }

    /**
     * Documents the error responses every secured endpoint can return, using the real error body.
     * Login and refresh describe their own 401 and are skipped.
     */
    @Bean
    public OpenApiCustomizer errorResponsesCustomizer() {
        return openApi -> {
            mergeAndOrderTags(openApi);
            Map<String, Schema> errorSchemas = ModelConverters.getInstance().readAll(ErrorResponse.class);
            errorSchemas.forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));

            openApi.getPaths().forEach((path, item) -> {
                if (path.startsWith("/auth/")) {
                    return;
                }
                for (Operation operation : item.readOperations()) {
                    addIfAbsent(operation, "400", "Bad request: malformed JSON, an invalid enum value, a bad date or an invalid range");
                    addIfAbsent(operation, "401", "Missing, invalid or expired access token: refresh it or log in again");
                    addIfAbsent(operation, "403", "The account's role is not allowed to use this endpoint");
                    addIfAbsent(operation, "404", "The record does not exist, or a business rule was broken (see `message`)");
                }
            });
        };
    }

    /**
     * Springdoc lists a tag once per declaration; keep one entry per name (with its description) in a fixed order.
     */
    private static void mergeAndOrderTags(OpenAPI openApi) {
        Map<String, Tag> byName = new LinkedHashMap<>();
        if (openApi.getTags() != null) {
            for (Tag tag : openApi.getTags()) {
                Tag kept = byName.get(tag.getName());
                if (kept == null) {
                    byName.put(tag.getName(), tag);
                } else if (kept.getDescription() == null || kept.getDescription().isBlank()) {
                    kept.setDescription(tag.getDescription());
                }
            }
        }
        List<Tag> ordered = new ArrayList<>();
        TAG_ORDER.forEach(name -> {
            if (byName.containsKey(name)) {
                ordered.add(byName.remove(name));
            }
        });
        ordered.addAll(byName.values());
        openApi.setTags(ordered);
    }

    private static void addIfAbsent(Operation operation, String status, String description) {
        if (operation.getResponses().containsKey(status)) {
            return;
        }
        operation.getResponses().addApiResponse(status, new ApiResponse()
            .description(description)
            .content(new Content().addMediaType("application/json",
                new MediaType().schema(new Schema<>().$ref(ERROR_REF)))));
    }
}
