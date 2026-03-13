CREATE DATABASE DuAnBanHang
GO
USE DuAnBanHang
GO

-----------------------
-- ROLE
-----------------------
CREATE TABLE Role(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50)
)

-----------------------
-- EMPLOYEE
-----------------------
CREATE TABLE Employee(
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50),
    password NVARCHAR(255),
    full_name NVARCHAR(100),
    phone_number NVARCHAR(20),
    role_id INT,

    created_at DATETIME DEFAULT GETDATE(),
    update_at DATETIME,
    deleted_at DATETIME,

    FOREIGN KEY (role_id) REFERENCES Role(id)
)

-----------------------
-- CUSTOMER
-----------------------
CREATE TABLE Customer(
    id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100),
    phone_number NVARCHAR(20),

    created_at DATETIME DEFAULT GETDATE(),
    update_at DATETIME,
    deleted_at DATETIME
)

-----------------------
-- MATERIAL
-----------------------
CREATE TABLE Material(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100),
    description NVARCHAR(255)
)

-----------------------
-- BRAND
-----------------------
CREATE TABLE Brand(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100)
)

-----------------------
-- CATEGORY
-----------------------
CREATE TABLE Categories(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100)
)

-----------------------
-- COLOR
-----------------------
CREATE TABLE Color(
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50),
    name NVARCHAR(50)
)

-----------------------
-- SIZE
-----------------------
CREATE TABLE Size(
    id INT IDENTITY(1,1) PRIMARY KEY,
    size INT
)

-----------------------
-- PRODUCT
-----------------------
CREATE TABLE Product(
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50),
    name NVARCHAR(100),
    description NVARCHAR(255),

    brand_id INT,
    category_id INT,
    material_id INT,

    status TINYINT,
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (brand_id) REFERENCES Brand(id),
    FOREIGN KEY (category_id) REFERENCES Categories(id),
    FOREIGN KEY (material_id) REFERENCES Material(id)
)

-----------------------
-- PRODUCT VARIANT
-----------------------
CREATE TABLE ProductVariant(
    id INT IDENTITY(1,1) PRIMARY KEY,

    product_id INT,
    color_id INT,
    size_id INT,

    sku NVARCHAR(50),
    price DECIMAL(18,2),

    stock_quantity INT,
    product_image NVARCHAR(255),

    status TINYINT,

    FOREIGN KEY (product_id) REFERENCES Product(id),
    FOREIGN KEY (color_id) REFERENCES Color(id),
    FOREIGN KEY (size_id) REFERENCES Size(id)
)

-----------------------
-- VOUCHER
-----------------------
CREATE TABLE Vouchers(
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50),
    percentage INT,
    min_order_value DECIMAL(18,2),
    quantity INT,
    start_date DATETIME,
    expiration_date DATETIME
)

-----------------------
-- INVOICE
-----------------------
CREATE TABLE Invoice(
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50),

    user_id INT,
    voucher_id INT,
    customer_id INT,

    status TINYINT,

    sub_total DECIMAL(18,2),
    total_money DECIMAL(18,2),

    payment_method NVARCHAR(50),

    created_at DATETIME DEFAULT GETDATE(),

    full_name NVARCHAR(100),
    phone_number NVARCHAR(20),

    FOREIGN KEY (user_id) REFERENCES Employee(id),
    FOREIGN KEY (voucher_id) REFERENCES Vouchers(id),
    FOREIGN KEY (customer_id) REFERENCES Customer(id)
)

-----------------------
-- INVOICE DETAILS
-----------------------
CREATE TABLE InvoiceDetails(
    id INT IDENTITY(1,1) PRIMARY KEY,

    invoice_id INT,
    product_variant_id INT,

    quantity INT,
    unit_price DECIMAL(18,2),

    FOREIGN KEY (invoice_id) REFERENCES Invoice(id),
    FOREIGN KEY (product_variant_id) REFERENCES ProductVariant(id)
)
