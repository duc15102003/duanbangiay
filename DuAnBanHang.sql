CREATE DATABASE DuAnBanHang
USE DuAnBanHang
GO

-----------------------
-- ROLE
-----------------------
CREATE TABLE Role(
    id   INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
)

-----------------------
-- USER
-----------------------
CREATE TABLE [User](
    id          INT IDENTITY(1,1) PRIMARY KEY,
    userName    NVARCHAR(50)  NOT NULL UNIQUE,
    password    NVARCHAR(255) NOT NULL,          -- tăng lên 255 để chứa hash
    fullName    NVARCHAR(100) NOT NULL,
    phoneNumber NVARCHAR(20),
    createdAt   DATETIME      NOT NULL DEFAULT GETDATE(),
    updatedAt   DATETIME,
    deletedAt   DATETIME                          -- NULL = chưa xóa (soft delete)
)

-----------------------
-- USER ROLE
-----------------------
CREATE TABLE UserRole(
    id     INT IDENTITY(1,1) PRIMARY KEY,
    userId INT NOT NULL,
    roleId INT NOT NULL,
    CONSTRAINT UQ_UserRole UNIQUE (userId, roleId),   -- chống trùng lặp
    FOREIGN KEY (userId) REFERENCES [User](id),
    FOREIGN KEY (roleId) REFERENCES Role(id)
)

-----------------------
-- CATEGORY
-----------------------
CREATE TABLE Category(
    id   INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
)

-----------------------
-- BRAND
-----------------------
CREATE TABLE Brand(
    id   INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
)

-----------------------
-- PRODUCT
-----------------------
CREATE TABLE Product(
    id          INT IDENTITY(1,1) PRIMARY KEY,
    code        NVARCHAR(50)  NOT NULL UNIQUE,
    name        NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),                         -- tăng độ dài mô tả
    idCategory  INT NOT NULL,
    idBrand     INT NOT NULL,
    status      TINYINT NOT NULL DEFAULT 1             -- 1=active, 0=inactive, 2=ngừng KD
        CONSTRAINT CHK_Product_Status CHECK (status IN (0, 1, 2)),
    createdAt   DATETIME NOT NULL DEFAULT GETDATE(),   -- đồng nhất tên với User
    updatedAt   DATETIME,
    deletedAt   DATETIME,
    FOREIGN KEY (idCategory) REFERENCES Category(id),
    FOREIGN KEY (idBrand)    REFERENCES Brand(id)
)

-----------------------
-- COLOR
-----------------------
CREATE TABLE Color(
    id   INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50)  NOT NULL UNIQUE,
    name NVARCHAR(50)  NOT NULL
)

-----------------------
-- SIZE
-----------------------
CREATE TABLE Size(
    id   INT IDENTITY(1,1) PRIMARY KEY,
    size INT NOT NULL UNIQUE                           -- kích cỡ không được trùng
)

-----------------------
-- PRODUCT VARIANT
-----------------------
CREATE TABLE ProductVariant(
    id        INT IDENTITY(1,1) PRIMARY KEY,
    idProduct INT            NOT NULL,
    idColor   INT            NOT NULL,
    idSize    INT            NOT NULL,
    price     DECIMAL(18,2)  NOT NULL
        CONSTRAINT CHK_ProductVariant_Price CHECK (price >= 0),
    quantity  INT            NOT NULL DEFAULT 0
        CONSTRAINT CHK_ProductVariant_Qty CHECK (quantity >= 0),
    image     NVARCHAR(500),
    CONSTRAINT UQ_Variant UNIQUE (idProduct, idColor, idSize),  -- chống trùng biến thể
    FOREIGN KEY (idProduct) REFERENCES Product(id),
    FOREIGN KEY (idColor)   REFERENCES Color(id),
    FOREIGN KEY (idSize)    REFERENCES Size(id)
)

-----------------------
-- PAYMENT METHOD
-----------------------
CREATE TABLE PaymentMethod(
    id   INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
)

-----------------------
-- INVOICE
-----------------------
CREATE TABLE Invoice(
    id              INT IDENTITY(1,1) PRIMARY KEY,
    code            NVARCHAR(50)  NOT NULL UNIQUE,
    date            DATETIME      NOT NULL DEFAULT GETDATE(),
    idUser          INT           NOT NULL,
    idPaymentMethod INT           NOT NULL,
    totalMoney      DECIMAL(18,2) NOT NULL DEFAULT 0
        CONSTRAINT CHK_Invoice_Total CHECK (totalMoney >= 0),
    FOREIGN KEY (idUser)          REFERENCES [User](id),
    FOREIGN KEY (idPaymentMethod) REFERENCES PaymentMethod(id)
)

-----------------------
-- INVOICE DETAILS
-----------------------
CREATE TABLE InvoiceDetails(
    id               INT IDENTITY(1,1) PRIMARY KEY,
    idInvoice        INT           NOT NULL,
    idProductVariant INT           NOT NULL,
    quantity         INT           NOT NULL
        CONSTRAINT CHK_InvDetail_Qty CHECK (quantity > 0),  -- phải >= 1
    price            DECIMAL(18,2) NOT NULL
        CONSTRAINT CHK_InvDetail_Price CHECK (price >= 0),
    FOREIGN KEY (idInvoice)        REFERENCES Invoice(id),
    FOREIGN KEY (idProductVariant) REFERENCES ProductVariant(id)
)

-----------------------
-- DATA MẪU
-----------------------
INSERT INTO Role (name) VALUES ('Admin'), ('NhanVien')

INSERT INTO PaymentMethod (name) VALUES
(N'Tiền mặt'),
(N'Chuyển khoản'),
(N'Momo')

INSERT INTO Category (name) VALUES
(N'Giày thể thao'),
(N'Giày chạy bộ')

INSERT INTO Brand (name) VALUES
(N'Nike'),
(N'Adidas')

INSERT INTO Color (code, name) VALUES
('DEN',   N'Đen'),
('TRANG', N'Trắng')

INSERT INTO Size (size) VALUES
(38),(39),(40),(41),(42)
