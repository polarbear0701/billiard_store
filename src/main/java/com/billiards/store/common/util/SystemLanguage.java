package com.billiards.store.common.util;

import java.util.HashMap;

// @Component
public class SystemLanguage {
    public static Language language = Language.VN;

    private static HashMap<String, String> vnHashMap = new HashMap<>();
    private static HashMap<String, String> enHashMap = new HashMap<>();

    static {
        // Vietnamese messages
        vnHashMap.put("login_success", "Đăng nhập thành công");
        vnHashMap.put("login_failed", "Đăng nhập thất bại");
        vnHashMap.put("unauthorized", "Không có quyền truy cập");
        vnHashMap.put("access_denied", "Từ chối truy cập");
        vnHashMap.put("resource_not_found", "Không tìm thấy dữ liệu");
        vnHashMap.put("create_success", "Tạo mới thành công");
        vnHashMap.put("update_success", "Cập nhật thành công");
        vnHashMap.put("delete_success", "Xóa thành công");
        vnHashMap.put("table_not_found", "Không tìm thấy bàn bi-a");
        vnHashMap.put("table_in_use", "Bàn bi-a đang được sử dụng");
        vnHashMap.put("price_must_be_positive", "Giá tiền không được nhỏ hơn hoặc bằng 0");
        vnHashMap.put("session_not_found", "Không tìm thấy phiên chơi");
        vnHashMap.put("session_started", "Bắt đầu phiên chơi thành công");
        vnHashMap.put("session_ended", "Kết thúc phiên chơi thành công");
        vnHashMap.put("product_not_found", "Không tìm thấy sản phẩm");
        vnHashMap.put("out_of_stock", "Sản phẩm đã hết hàng");
        vnHashMap.put("invoice_not_found", "Không tìm thấy hóa đơn");
        vnHashMap.put("invoice_paid", "Thanh toán hóa đơn thành công");
        vnHashMap.put("success", "Thành công");
        vnHashMap.put("order_item_not_found", "Không tìm thấy mặt hàng");
        vnHashMap.put("insufficient_stock", "Không đủ hàng trong kho");
        vnHashMap.put("invalid_quantity", "Số lượng không hợp lệ");
        vnHashMap.put("session_completed", "Phiên chơi đã kết thúc");
        vnHashMap.put("session_code_empty", "Mã phiên chơi không được để trống");
        vnHashMap.put("product_code_empty", "Mã sản phẩm không được để trống");
        vnHashMap.put("start_time_future", "Thời gian bắt đầu không thể là tương lai");
        vnHashMap.put("session_closed", "Phiên chơi đã đóng hoặc bị hủy");
        vnHashMap.put("no_start_time", "Không tìm thấy thời gian bắt đầu");
        vnHashMap.put("table_not_in_session", "Bàn bi-a hiện không trong phiên chơi");
        vnHashMap.put("table_no_session", "Bàn bi-a không có phiên chơi nào");
        vnHashMap.put("invoice_already_exists", "Hóa đơn cho phiên chơi này đã được tạo");
        vnHashMap.put("malformed_json", "Yêu cầu JSON không hợp lệ");

        // English messages
        enHashMap.put("login_success", "Login successful");
        enHashMap.put("login_failed", "Login failed");
        enHashMap.put("unauthorized", "Unauthorized access");
        enHashMap.put("access_denied", "Access denied");
        enHashMap.put("resource_not_found", "Resource not found");
        enHashMap.put("create_success", "Created successfully");
        enHashMap.put("update_success", "Updated successfully");
        enHashMap.put("delete_success", "Deleted successfully");
        enHashMap.put("table_not_found", "Billiard table not found");
        enHashMap.put("table_in_use", "Billiard table is currently in use");
        enHashMap.put("price_must_be_positive", "Price cannot be less than or equal to 0");
        enHashMap.put("session_not_found", "Play session not found");
        enHashMap.put("session_started", "Play session started successfully");
        enHashMap.put("session_ended", "Play session ended successfully");
        enHashMap.put("product_not_found", "Product not found");
        enHashMap.put("out_of_stock", "Product is out of stock");
        enHashMap.put("invoice_not_found", "Invoice not found");
        enHashMap.put("invoice_paid", "Invoice paid successfully");
        enHashMap.put("success", "Success");
        enHashMap.put("order_item_not_found", "Order item not found");
        enHashMap.put("insufficient_stock", "Insufficient stock available");
        enHashMap.put("invalid_quantity", "Invalid quantity");
        enHashMap.put("session_completed", "Session has been completed");
        enHashMap.put("session_code_empty", "Session code cannot be empty");
        enHashMap.put("product_code_empty", "Product code cannot be empty");
        enHashMap.put("start_time_future", "Start time cannot be in the future");
        enHashMap.put("session_closed", "Session is already closed or cancelled");
        enHashMap.put("no_start_time", "No start time found");
        enHashMap.put("table_not_in_session", "Table is currently not in session");
        enHashMap.put("table_no_session", "This table is not having any session");
        enHashMap.put("invoice_already_exists", "This session already has invoice generated");
        enHashMap.put("malformed_json", "Malformed JSON request");
    }

    public static HashMap<String, String> getDictionary() {
        if (SystemLanguage.language == Language.VN) {
            return vnHashMap;
        } else {
            return enHashMap;
        }
    }

    public static String getMessage(String key) {
        return getDictionary().getOrDefault(key, key);
    }
}

enum Language {
    EN,
    VN
}
