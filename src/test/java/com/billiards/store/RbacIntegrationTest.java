package com.billiards.store;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Role-based access: accounts come from USER_ACCOUNT (admin) and APP_ACCOUNTS (see AbstractIntegrationTest).
 */
class RbacIntegrationTest extends AbstractIntegrationTest {

    private ResultActions call(MockHttpServletRequestBuilder request, String token) throws Exception {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private String staffToken() throws Exception {
        return login("staff1", "staff-pass-1");
    }

    @Test
    void allConfiguredAccountsCanLogInAndBadPasswordsCannot() throws Exception {
        login("admin", "admin-pass");
        login("staff1", "staff-pass-1");
        login("boss2", "boss-pass-2");
        // passwords may contain ':'
        login("staff2", "staff:pass:2");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"staff1\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nobody\",\"password\":\"whatever-pass\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        call(get("/table/all"), null).andExpect(status().isUnauthorized());
        call(get("/audit-logs"), null).andExpect(status().isUnauthorized());
        call(post("/product/create").contentType(MediaType.APPLICATION_JSON).content("{}"), null)
            .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanOperateTheFloor() throws Exception {
        String staff = staffToken();

        call(get("/table/all"), staff).andExpect(status().isOk());
        call(get("/product/all"), staff).andExpect(status().isOk());
        call(get("/play-session/all"), staff).andExpect(status().isOk());
        call(get("/order-item/all"), staff).andExpect(status().isOk());
        call(get("/invoice/all"), staff).andExpect(status().isOk());

        // authorised: the request reaches business logic and fails there with 404 for unknown data, not 403
        call(post("/play-session/create").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tableCode\":\"NOPE\",\"sessionStatus\":\"ACTIVE\"}"), staff)
            .andExpect(status().isNotFound());
        call(put("/play-session/close/NOPE"), staff).andExpect(status().isNotFound());
        call(put("/invoice/complete").param("invoiceCode", "NOPE"), staff).andExpect(status().isNotFound());
        call(post("/order-item/cancel/NOPE"), staff).andExpect(status().isNotFound());
    }

    @Test
    void staffCannotManageTheStore() throws Exception {
        String staff = staffToken();

        call(post("/product/create").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\",\"quantity\":1,\"price\":1000}"), staff)
            .andExpect(status().isForbidden());
        call(put("/product/update/price/PRD1").param("newStockPrice", "5000"), staff).andExpect(status().isForbidden());
        call(put("/product/update/clear/PRD1"), staff).andExpect(status().isForbidden());
        call(delete("/product/PRD1"), staff).andExpect(status().isForbidden());
        call(post("/table/create").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tableType\":\"POOL\",\"tableStatus\":\"AVAILABLE\",\"pricePerHour\":1000}"), staff)
            .andExpect(status().isForbidden());
        call(put("/table/update-price").param("tableCode", "T").param("newPricePerHour", "1"), staff)
            .andExpect(status().isForbidden());
        call(delete("/table/T"), staff).andExpect(status().isForbidden());
        call(delete("/play-session/" + UUID.randomUUID()), staff).andExpect(status().isForbidden());
        call(delete("/order-item/" + UUID.randomUUID()), staff).andExpect(status().isForbidden());
        call(delete("/invoice/INV1"), staff).andExpect(status().isForbidden());
        call(get("/audit-logs"), staff).andExpect(status().isForbidden());
    }

    @Test
    void adminAccountsCanDoEverything() throws Exception {
        for (String token : new String[] {adminToken(), login("boss2", "boss-pass-2")}) {
            call(get("/audit-logs"), token).andExpect(status().isOk());
            call(post("/product/create").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Rbac\",\"quantity\":1,\"price\":1000}"), token)
                .andExpect(status().isOk());
            call(get("/table/all"), token).andExpect(status().isOk());
        }
    }

    @Test
    void auditEntriesRecordTheActingAccount() throws Exception {
        String boss = login("boss2", "boss-pass-2");
        call(post("/product/create").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"WhoDidIt\",\"quantity\":1,\"price\":1000}"), boss)
            .andExpect(status().isOk());

        call(get("/audit-logs").param("actor", "boss2").param("entityType", "Product"), boss)
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .jsonPath("$.data.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
