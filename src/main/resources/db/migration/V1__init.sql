-- ==========================================
-- 1. CREATE SEQUENCES FOR CUSTOM CODES
-- ==========================================
CREATE SEQUENCE product_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE session_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE order_item_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE invoice_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE billiard_table_code_seq START WITH 1 INCREMENT BY 1;

-- ==========================================
-- 2. CREATE TABLES
-- ==========================================

-- BILLIARD TABLE
CREATE TABLE billiard_table (
	id UUID PRIMARY KEY,
	created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    
    table_code VARCHAR(50) NOT NULL UNIQUE,
    price_per_hour DECIMAL(10, 2) NOT NULL,
    table_type VARCHAR(10),
    table_status VARCHAR(10)
);

-- PRODUCTS
CREATE TABLE product (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    
    product_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- SESSIONS
CREATE TABLE play_session (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    
    session_code VARCHAR(50) NOT NULL UNIQUE,
    table_code VARCHAR(50) NOT NULL,
    start_time TIMESTAMP, 
    end_time TIMESTAMP,
    session_status VARCHAR(50)
);

-- ORDER ITEMS
CREATE TABLE order_item (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    
    order_item_code VARCHAR(50) NOT NULL UNIQUE,
    session_code VARCHAR(50) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- INVOICES
CREATE TABLE invoice (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    
    invoice_code VARCHAR(50) NOT NULL UNIQUE,
    session_code VARCHAR(50) NOT NULL,
    table_fee DECIMAL(10, 2) NOT NULL,
    fnb_fee DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL
);