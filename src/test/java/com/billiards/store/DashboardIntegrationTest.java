package com.billiards.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
import com.billiards.store.service.BilliardTableService;
import com.billiards.store.service.InvoiceService;
import com.billiards.store.service.OrderItemService;
import com.billiards.store.service.PlaySessionService;
import com.billiards.store.service.ProductService;

import tools.jackson.databind.JsonNode;

/**
 * Dashboard numbers are checked against fixed data in the year 2001, so they are exact and
 * independent of the other tests (which create data "now").
 *
 * <pre>
 * 2001-03-10  S1 DT1 10:00-12:00  paid 12:05  table 120000 + fnb 50000 (DP1 x2 @10000, DP2 x5 @6000; DP2 x99 cancelled)
 *             S2 DT2 20:00-21:00  paid 21:05  table  60000 + fnb 20000 (DP1 x2 @10000)
 *             S7 DT1 14:00-15:00  CANCELLED session (must not count as play time)
 * 2001-03-11  S3 DT1 09:00-10:30  paid 10:35  table  90000
 *             S4 DT1 15:00-16:00  invoice NOT paid (DP3 x5 must not count as sales)
 *             S5 DT2              invoice paid but deleted (excluded)
 *             S6 DT2 23:00 -> 2001-03-12 01:00, no invoice (play time is clipped per day)
 * </pre>
 */
class DashboardIntegrationTest extends AbstractIntegrationTest {

    private static boolean seeded = false;

    @Autowired JdbcTemplate jdbc;
    @Autowired Clock clock;
    @Autowired ProductService productService;
    @Autowired BilliardTableService tableService;
    @Autowired PlaySessionService sessionService;
    @Autowired OrderItemService orderItemService;
    @Autowired InvoiceService invoiceService;

    @BeforeEach
    void seed() {
        SecurityContextHolder.clearContext();
        if (seeded) {
            return;
        }
        seeded = true;
        product("DP1", "Cola", 3, 10000);
        product("DP2", "Beer", 50, 6000);
        product("DP3", "Chips", 0, 5000);
        table("DT1", "POOL", "AVAILABLE");
        table("DT2", "CAROM", "OCCUPIED");

        session("S1", "DT1", "2001-03-10 10:00", "2001-03-10 12:00", "COMPLETED");
        session("S2", "DT2", "2001-03-10 20:00", "2001-03-10 21:00", "COMPLETED");
        session("S7", "DT1", "2001-03-10 14:00", "2001-03-10 15:00", "CANCELLED");
        session("S3", "DT1", "2001-03-11 09:00", "2001-03-11 10:30", "COMPLETED");
        session("S4", "DT1", "2001-03-11 15:00", "2001-03-11 16:00", "COMPLETED");
        session("S5", "DT2", "2001-03-11 12:00", "2001-03-11 12:30", "COMPLETED");
        session("S6", "DT2", "2001-03-11 23:00", "2001-03-12 01:00", "COMPLETED");

        item("S1", "DP1", 2, 10000, false);
        item("S1", "DP2", 5, 6000, false);
        item("S1", "DP2", 99, 6000, true);
        item("S2", "DP1", 2, 10000, false);
        item("S4", "DP3", 5, 5000, false);

        invoice("I1", "S1", 120000, 50000, true, "2001-03-10 12:05", false);
        invoice("I2", "S2", 60000, 20000, true, "2001-03-10 21:05", false);
        invoice("I3", "S3", 90000, 0, true, "2001-03-11 10:35", false);
        invoice("I4", "S4", 60000, 25000, false, null, false);
        invoice("I5", "S5", 30000, 0, true, "2001-03-11 12:35", true);
    }

    private void product(String code, String name, int quantity, int price) {
        jdbc.update("INSERT INTO product (id, product_code, name, quantity, price, is_deleted) VALUES (gen_random_uuid(), ?, ?, ?, ?, FALSE)",
            code, name, quantity, price);
    }

    private void table(String code, String type, String status) {
        jdbc.update("INSERT INTO billiard_table (id, table_code, price_per_hour, table_type, table_status, is_deleted) VALUES (gen_random_uuid(), ?, 60000, ?, ?, FALSE)",
            code, type, status);
    }

    private void session(String code, String table, String start, String end, String status) {
        jdbc.update("INSERT INTO play_session (id, session_code, table_code, start_time, end_time, session_status, is_deleted) VALUES (gen_random_uuid(), ?, ?, ?::timestamp, ?::timestamp, ?, FALSE)",
            code, table, start, end, status);
    }

    private void item(String session, String product, int quantity, int price, boolean deleted) {
        jdbc.update("INSERT INTO order_item (id, order_item_code, session_code, product_code, quantity, price, is_deleted) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?)",
            "OI-" + session + "-" + product + "-" + quantity, session, product, quantity, price, deleted);
    }

    private void invoice(String code, String session, int tableFee, int fnbFee, boolean complete, String paidAt, boolean deleted) {
        jdbc.update("INSERT INTO invoice (id, invoice_code, session_code, table_fee, fnb_fee, total_amount, complete, paid_at, is_deleted) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?, ?::timestamp, ?)",
            code, session, tableFee, fnbFee, tableFee + fnbFee, complete, paidAt, deleted);
    }

    private JsonNode data(String url, String... params) throws Exception {
        return dataAs(adminToken(), url, params);
    }

    private JsonNode dataAs(String token, String url, String... params) throws Exception {
        MockHttpServletRequestBuilder request = get(url).header("Authorization", "Bearer " + token);
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return json(mockMvc.perform(request).andExpect(status().isOk()).andReturn()).at("/data");
    }

    private void expectStatus(String url, int expected, String... params) throws Exception {
        MockHttpServletRequestBuilder request = get(url).header("Authorization", "Bearer " + adminToken());
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        mockMvc.perform(request).andExpect(status().is(expected));
    }

    private static void assertAmount(JsonNode node, String expected) {
        assertThat(node.decimalValue()).isEqualByComparingTo(new BigDecimal(expected));
    }

    private JsonNode tableRow(JsonNode overview, String code) {
        for (JsonNode row : overview.get("tables")) {
            if (code.equals(row.get("tableCode").asString())) {
                return row;
            }
        }
        throw new AssertionError("table not in overview: " + code);
    }

    // ---------------------------------------------------------------- summary

    @Test
    void summaryCountsOnlyPaidInvoicesOnTheirPaymentDay() throws Exception {
        JsonNode day = data("/dashboard/summary", "from", "2001-03-10", "to", "2001-03-10");
        assertAmount(day.get("revenue"), "250000");
        assertAmount(day.get("tableRevenue"), "180000");
        assertAmount(day.get("fnbRevenue"), "70000");
        assertThat(day.get("invoiceCount").asInt()).isEqualTo(2);
        assertAmount(day.get("averageInvoice"), "125000");
        assertThat(day.at("/previous/from").asString()).isEqualTo("2001-03-09");
        assertAmount(day.at("/previous/revenue"), "0");
        assertThat(day.get("revenueChangePercent").isNull()).isTrue();
    }

    @Test
    void summaryExcludesUnpaidAndDeletedInvoicesAndComparesWithPreviousPeriod() throws Exception {
        // I4 (unpaid) and I5 (deleted) are on this day but must not count
        JsonNode day = data("/dashboard/summary", "from", "2001-03-11", "to", "2001-03-11");
        assertAmount(day.get("revenue"), "90000");
        assertThat(day.get("invoiceCount").asInt()).isEqualTo(1);
        assertAmount(day.at("/previous/revenue"), "250000");
        assertAmount(day.get("revenueChangePercent"), "-64.0");
    }

    @Test
    void summaryOverSeveralDaysUsesAnEqualLengthPreviousPeriod() throws Exception {
        JsonNode both = data("/dashboard/summary", "from", "2001-03-10", "to", "2001-03-11");
        assertAmount(both.get("revenue"), "340000");
        assertThat(both.get("invoiceCount").asInt()).isEqualTo(3);
        assertThat(both.at("/previous/from").asString()).isEqualTo("2001-03-08");
        assertThat(both.at("/previous/to").asString()).isEqualTo("2001-03-09");
    }

    @Test
    void emptyPeriodGivesZerosNotErrors() throws Exception {
        JsonNode empty = data("/dashboard/summary", "from", "2001-01-01", "to", "2001-01-02");
        assertAmount(empty.get("revenue"), "0");
        assertAmount(empty.get("averageInvoice"), "0");
        assertThat(empty.get("invoiceCount").asInt()).isZero();
    }

    // ---------------------------------------------------------------- series

    @Test
    void dailySeriesIsZeroFilled() throws Exception {
        JsonNode series = data("/dashboard/revenue", "from", "2001-03-09", "to", "2001-03-11", "granularity", "DAY");
        assertThat(series.get("granularity").asString()).isEqualTo("DAY");
        JsonNode points = series.get("points");
        assertThat(points.size()).isEqualTo(3);
        assertThat(points.get(0).get("bucket").asString()).startsWith("2001-03-09T00:00");
        assertAmount(points.get(0).get("revenue"), "0");
        assertAmount(points.get(1).get("revenue"), "250000");
        assertThat(points.get(1).get("invoiceCount").asInt()).isEqualTo(2);
        assertAmount(points.get(2).get("revenue"), "90000");
    }

    @Test
    void hourlySeriesPlacesSalesInThePaymentHour() throws Exception {
        JsonNode points = data("/dashboard/revenue", "from", "2001-03-10", "to", "2001-03-10", "granularity", "HOUR").get("points");
        assertThat(points.size()).isEqualTo(24);
        assertAmount(points.get(12).get("revenue"), "170000");
        assertAmount(points.get(21).get("revenue"), "80000");
        assertAmount(points.get(13).get("revenue"), "0");
    }

    @Test
    void monthlySeriesUsesCalendarMonths() throws Exception {
        JsonNode points = data("/dashboard/revenue", "from", "2001-02-15", "to", "2001-04-20", "granularity", "MONTH").get("points");
        assertThat(points.size()).isEqualTo(3);
        assertThat(points.get(1).get("bucket").asString()).startsWith("2001-03-01T00:00");
        assertAmount(points.get(0).get("revenue"), "0");
        assertAmount(points.get(1).get("revenue"), "340000");
        assertAmount(points.get(2).get("revenue"), "0");
    }

    @Test
    void invalidRangesAreRejected() throws Exception {
        expectStatus("/dashboard/summary", 400, "from", "2001-03-11", "to", "2001-03-10");
        expectStatus("/dashboard/summary", 400, "from", "2000-01-01", "to", "2001-12-31");
        expectStatus("/dashboard/revenue", 400, "from", "2001-01-01", "to", "2001-03-01", "granularity", "HOUR");
        expectStatus("/dashboard/revenue", 400, "granularity", "WEEK");
        expectStatus("/dashboard/summary", 400, "from", "not-a-date");
    }

    // ---------------------------------------------------------------- products

    @Test
    void topProductsAreRankedByRevenueOrQuantityFromPaidInvoicesOnly() throws Exception {
        JsonNode byRevenue = data("/dashboard/products/top", "from", "2001-03-10", "to", "2001-03-11");
        assertThat(byRevenue.size()).isEqualTo(2); // DP3 sold only on an unpaid invoice; cancelled DP2 x99 ignored
        assertThat(byRevenue.get(0).get("productCode").asString()).isEqualTo("DP1");
        assertThat(byRevenue.get(0).get("name").asString()).isEqualTo("Cola");
        assertThat(byRevenue.get(0).get("quantitySold").asInt()).isEqualTo(4);
        assertAmount(byRevenue.get(0).get("revenue"), "40000");
        assertThat(byRevenue.get(1).get("productCode").asString()).isEqualTo("DP2");
        assertThat(byRevenue.get(1).get("quantitySold").asInt()).isEqualTo(5);
        assertAmount(byRevenue.get(1).get("revenue"), "30000");

        JsonNode byQuantity = data("/dashboard/products/top", "from", "2001-03-10", "to", "2001-03-11", "by", "QUANTITY", "limit", "1");
        assertThat(byQuantity.size()).isEqualTo(1);
        assertThat(byQuantity.get(0).get("productCode").asString()).isEqualTo("DP2");
    }

    @Test
    void topProductsValidatesLimit() throws Exception {
        expectStatus("/dashboard/products/top", 400, "limit", "0");
        expectStatus("/dashboard/products/top", 400, "limit", "51");
    }

    @Test
    void lowStockListsProductsAtOrBelowThreshold() throws Exception {
        List<String> codes = data("/dashboard/products/low-stock", "threshold", "3").findValuesAsString("productCode");
        assertThat(codes).contains("DP1", "DP3").doesNotContain("DP2");
        assertThat(data("/dashboard/products/low-stock", "threshold", "0").findValuesAsString("productCode"))
            .contains("DP3").doesNotContain("DP1");
        expectStatus("/dashboard/products/low-stock", 400, "threshold", "-1");
    }

    // ---------------------------------------------------------------- tables

    @Test
    void tableOverviewReportsPlayTimeUtilizationAndRevenue() throws Exception {
        JsonNode overview = data("/dashboard/tables", "from", "2001-03-10", "to", "2001-03-10");

        JsonNode dt1 = tableRow(overview, "DT1");
        assertThat(dt1.get("sessionCount").asInt()).isEqualTo(1); // cancelled S7 ignored
        assertThat(dt1.get("playedMinutes").asInt()).isEqualTo(120);
        assertAmount(dt1.get("utilizationPercent"), "8.3");
        assertAmount(dt1.get("tableRevenue"), "120000");
        assertThat(dt1.get("currentStatus").asString()).isEqualTo("AVAILABLE");
        assertThat(dt1.get("tableType").asString()).isEqualTo("POOL");

        JsonNode dt2 = tableRow(overview, "DT2");
        assertThat(dt2.get("playedMinutes").asInt()).isEqualTo(60);
        assertAmount(dt2.get("utilizationPercent"), "4.2");
        assertAmount(dt2.get("tableRevenue"), "60000");
        assertThat(dt2.get("currentStatus").asString()).isEqualTo("OCCUPIED");

        assertThat(overview.at("/statusCounts/OCCUPIED").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(overview.at("/statusCounts/MAINTENANCE").isMissingNode()).isFalse();
    }

    @Test
    void playTimeIsClippedToTheRequestedDays() throws Exception {
        // S6 runs 23:00 on the 11th until 01:00 on the 12th
        JsonNode eleventh = data("/dashboard/tables", "from", "2001-03-11", "to", "2001-03-11");
        assertThat(tableRow(eleventh, "DT2").get("playedMinutes").asInt()).isEqualTo(60 + 30); // S6 + deleted-invoice session S5 still played
        assertThat(tableRow(eleventh, "DT1").get("playedMinutes").asInt()).isEqualTo(150);
        assertAmount(tableRow(eleventh, "DT1").get("tableRevenue"), "90000");

        JsonNode twelfth = data("/dashboard/tables", "from", "2001-03-12", "to", "2001-03-12");
        assertThat(tableRow(twelfth, "DT2").get("playedMinutes").asInt()).isEqualTo(60);
        assertThat(tableRow(twelfth, "DT2").get("sessionCount").asInt()).isEqualTo(1);
        assertThat(tableRow(twelfth, "DT1").get("playedMinutes").asInt()).isZero();
        assertAmount(tableRow(twelfth, "DT2").get("tableRevenue"), "0");
    }

    @Test
    void runningSessionsCountUpToNow() throws Exception {
        LocalDateTime started = LocalDateTime.now(clock).minusMinutes(10);
        table("DT3", "LIBRE", "OCCUPIED");
        jdbc.update("INSERT INTO play_session (id, session_code, table_code, start_time, end_time, session_status, is_deleted) VALUES (gen_random_uuid(), 'S8', 'DT3', ?, NULL, 'ACTIVE', FALSE)",
            java.sql.Timestamp.valueOf(started));

        LocalDate today = LocalDate.now(clock);
        JsonNode overview = data("/dashboard/tables", "from", today.minusDays(1).toString(), "to", today.toString());
        long minutes = tableRow(overview, "DT3").get("playedMinutes").asLong();
        assertThat(minutes).isBetween(9L, 12L);
    }

    // ---------------------------------------------------------------- paid_at and access

    @Test
    void completingAnInvoiceRecordsThePaymentTimeOnceAndCountsAsRevenueToday() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        ProductResponseDto product = productService.create(new ProductRequestDto("PaidAt", 10, BigDecimal.valueOf(20000)));
        BilliardTableResponseDto table = tableService.create(
            new BilliardTableRequestDto(TableType.POOL, TableStatus.AVAILABLE, BigDecimal.valueOf(60000)));
        PlaySessionResponseDto session = sessionService.create(new PlaySessionRequestDto(table.getTableCode(), SessionStatus.ACTIVE));
        orderItemService.create(new OrderItemCreateRequestDto(session.getSessionCode(),
            List.of(new OrderItemLineDto(product.getProductCode(), 2))));
        InvoiceResponseDto invoice = invoiceService.create(new InvoiceRequestDto(session.getSessionCode()));
        SecurityContextHolder.clearContext();

        assertThat(invoice.getPaidAt()).isNull();
        BigDecimal before = data("/dashboard/summary").get("revenue").decimalValue();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        invoiceService.completeInvoice(invoice.getInvoiceCode());
        LocalDateTime firstPaidAt = jdbc.queryForObject(
            "SELECT paid_at FROM invoice WHERE invoice_code = ?", LocalDateTime.class, invoice.getInvoiceCode());
        invoiceService.completeInvoice(invoice.getInvoiceCode());
        LocalDateTime secondPaidAt = jdbc.queryForObject(
            "SELECT paid_at FROM invoice WHERE invoice_code = ?", LocalDateTime.class, invoice.getInvoiceCode());
        SecurityContextHolder.clearContext();

        assertThat(firstPaidAt).isNotNull().isBetween(LocalDateTime.now(clock).minusMinutes(1), LocalDateTime.now(clock).plusMinutes(1));
        assertThat(secondPaidAt).isEqualTo(firstPaidAt);

        BigDecimal after = data("/dashboard/summary").get("revenue").decimalValue();
        assertThat(after.subtract(before)).isEqualByComparingTo(invoice.getTotalAmount());
    }

    @Test
    void dashboardIsAdminOnly() throws Exception {
        String staff = login("staff1", "staff-pass-1");
        for (String url : List.of("/dashboard/summary", "/dashboard/revenue", "/dashboard/products/top",
                "/dashboard/products/low-stock", "/dashboard/tables")) {
            mockMvc.perform(get(url).header("Authorization", "Bearer " + staff)).andExpect(status().isForbidden());
            mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
        }
        dataAs(login("boss2", "boss-pass-2"), "/dashboard/summary");
    }
}
