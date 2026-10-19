package com.billiards.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;

import com.billiards.store.common.enumerated.SessionStatus;
import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.enumerated.TableType;
import com.billiards.store.dto.billiard_table.BilliardTableRequestDto;
import com.billiards.store.dto.billiard_table.BilliardTableResponseDto;
import com.billiards.store.dto.invoice.InvoiceRequestDto;
import com.billiards.store.dto.invoice.InvoiceResponseDto;
import com.billiards.store.dto.order_item.OrderItemCreateRequestDto;
import com.billiards.store.dto.order_item.OrderItemLineDto;
import com.billiards.store.dto.play_session.PlaySessionRequestDto;
import com.billiards.store.dto.play_session.PlaySessionResponseDto;
import com.billiards.store.dto.product.ProductRequestDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.dto.product.ProductUpdateStockDto;
import com.billiards.store.model.AuditLog;
import com.billiards.store.repository.AuditLogRepository;
import com.billiards.store.service.BilliardTableService;
import com.billiards.store.service.InvoiceService;
import com.billiards.store.service.OrderItemService;
import com.billiards.store.service.PlaySessionService;
import com.billiards.store.service.ProductService;


/**
 * End-to-end check of the audit feature (see {@link AbstractIntegrationTest}).
 */
class AuditLogIntegrationTest extends AbstractIntegrationTest {

    @Autowired AuditLogRepository auditLogRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired ProductService productService;
    @Autowired BilliardTableService tableService;
    @Autowired PlaySessionService sessionService;
    @Autowired OrderItemService orderItemService;
    @Autowired InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    private void actAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private List<AuditLog> logsFor(String entityType, String entityCode) {
        return auditLogRepository.findAll().stream()
            .filter(l -> l.getEntityType().equals(entityType) && entityCode.equals(l.getEntityCode()))
            .sorted((a, b) -> a.getOccurredAt().compareTo(b.getOccurredAt()))
            .toList();
    }

    @Test
    void flywayAppliedAuditTable() {
        Integer applied = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE", Integer.class);
        assertThat(applied).isGreaterThanOrEqualTo(3);
    }

    @Test
    void auditEndpointRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/audit-logs")).andExpect(status().isUnauthorized());
    }

    @Test
    void productLifecycleIsAuditedThroughTheApi() throws Exception {
        String token = adminToken();

        MvcResult created = mockMvc.perform(post("/product/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cola\",\"quantity\":10,\"price\":15000}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.createdBy").value("admin"))
            .andReturn();
        String code = json(created).at("/data/productCode").asString();

        mockMvc.perform(delete("/product/" + code).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        mockMvc.perform(get("/audit-logs")
                .param("entityType", "Product")
                .param("entityCode", code)
                .param("direction", "asc")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content[0].action").value("CREATE"))
            .andExpect(jsonPath("$.data.content[0].actor").value("admin"))
            .andExpect(jsonPath("$.data.content[0].oldValue").doesNotExist())
            .andExpect(jsonPath("$.data.content[0].newValue.name").value("Cola"))
            .andExpect(jsonPath("$.data.content[1].action").value("DELETE"))
            .andExpect(jsonPath("$.data.content[1].oldValue.name").value("Cola"))
            .andExpect(jsonPath("$.data.content[1].newValue").doesNotExist());
    }

    @Test
    void searchFiltersAndPageSizeCapAreApplied() throws Exception {
        actAsAdmin();
        ProductResponseDto product = productService.create(new ProductRequestDto("Tea", 5, BigDecimal.valueOf(8000)));
        String token = adminToken();
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/audit-logs")
                .param("actor", "admin").param("action", "CREATE")
                .param("entityCode", product.getProductCode())
                .header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/audit-logs")
                .param("entityCode", product.getProductCode())
                .param("actor", "somebody-else")
                .header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(get("/audit-logs")
                .param("entityCode", product.getProductCode())
                .param("from", LocalDateTime.now().plusDays(1).toString())
                .header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(get("/audit-logs").param("size", "100000")
                .header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.data.size").value(100));
    }

    @Test
    void failedOperationLeavesNoAuditEntry() {
        actAsAdmin();
        ProductResponseDto product = productService.create(new ProductRequestDto("Water", 2, BigDecimal.valueOf(5000)));
        int before = logsFor("Product", product.getProductCode()).size();

        assertThatThrownBy(() -> productService.updateStock(
                product.getProductCode(), new ProductUpdateStockDto(true, 99)))
            .isInstanceOf(RuntimeException.class);

        assertThat(logsFor("Product", product.getProductCode())).hasSize(before);
    }

    @Test
    void entriesWithoutAuthenticatedUserAreAttributedToSystem() {
        ProductResponseDto product = productService.create(new ProductRequestDto("Chalk", 1, BigDecimal.valueOf(3000)));
        assertThat(logsFor("Product", product.getProductCode()))
            .singleElement().extracting(AuditLog::getActor).isEqualTo("SYSTEM");
    }

    @Test
    void fullSessionFlowIsAudited() {
        actAsAdmin();
        ProductResponseDto product = productService.create(new ProductRequestDto("Beer", 20, BigDecimal.valueOf(20000)));
        BilliardTableResponseDto table = tableService.create(
            new BilliardTableRequestDto(TableType.POOL, TableStatus.AVAILABLE, BigDecimal.valueOf(60000)));

        PlaySessionResponseDto session = sessionService.create(
            new PlaySessionRequestDto(table.getTableCode(), SessionStatus.ACTIVE));
        orderItemService.create(new OrderItemCreateRequestDto(
            session.getSessionCode(), List.of(new OrderItemLineDto(product.getProductCode(), 3))));
        InvoiceResponseDto invoice = invoiceService.create(new InvoiceRequestDto(session.getSessionCode()));
        invoiceService.completeInvoice(invoice.getInvoiceCode());

        assertThat(logsFor("PlaySession", session.getSessionCode()))
            .extracting(AuditLog::getAction).containsExactly("CREATE", "END");
        assertThat(logsFor("Invoice", invoice.getInvoiceCode()))
            .extracting(AuditLog::getAction).containsExactly("CREATE", "COMPLETE");
        assertThat(logsFor("BilliardTable", table.getTableCode()))
            .extracting(AuditLog::getAction)
            .containsExactly("CREATE", "UPDATE_STATUS", "UPDATE_STATUS");
        assertThat(logsFor("Product", product.getProductCode()))
            .extracting(AuditLog::getAction).containsExactly("CREATE", "UPDATE_STOCK");

        AuditLog complete = logsFor("Invoice", invoice.getInvoiceCode()).get(1);
        assertThat(complete.getOldValue()).containsEntry("complete", false);
        assertThat(complete.getNewValue()).containsEntry("complete", true);
        assertThat(complete.getActor()).isEqualTo("admin");
    }
}
