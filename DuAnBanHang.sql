CREATE DATABASE DuAnBanHang
GO
USE DuAnBanHang
GO

-----------------------
-- ROLE
-----------------------
CREATE TABLE Role(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
)

-----------------------
-- USER (TÀI KHOẢN LOGIN)
-----------------------
CREATE TABLE [User](
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    createdAt DATETIME DEFAULT GETDATE(),
    status TINYINT DEFAULT 1
)

-----------------------
-- USER ROLE
-----------------------
CREATE TABLE UserRole(
    id INT IDENTITY(1,1) PRIMARY KEY,
    userId INT NOT NULL,
    roleId INT NOT NULL,

    CONSTRAINT UQ_UserRole UNIQUE(userId,roleId),

    FOREIGN KEY (userId) REFERENCES [User](id),
    FOREIGN KEY (roleId) REFERENCES Role(id)
)

-----------------------
-- EMPLOYEE (NHÂN VIÊN)
-----------------------
CREATE TABLE Employee(
    id INT IDENTITY(1,1) PRIMARY KEY,
    fullName NVARCHAR(100) NOT NULL,
    phoneNumber NVARCHAR(20),
    email NVARCHAR(100),
    address NVARCHAR(200),
    idUser INT UNIQUE,

    createdAt DATETIME DEFAULT GETDATE(),
    updatedAt DATETIME,
    deletedAt DATETIME,

    FOREIGN KEY (idUser) REFERENCES [User](id)
)

-----------------------
-- CUSTOMER (KHÁCH HÀNG)
-----------------------
CREATE TABLE Customer(
    id INT IDENTITY(1,1) PRIMARY KEY,
    fullName NVARCHAR(100) NOT NULL,
    phoneNumber NVARCHAR(20),
    email NVARCHAR(100),
    address NVARCHAR(200),

    createdAt DATETIME DEFAULT GETDATE()
)

-----------------------
-- CATEGORY
-----------------------
CREATE TABLE Category(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
)

-----------------------
-- BRAND
-----------------------
CREATE TABLE Brand(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
)

-----------------------
-- MATERIAL
-----------------------
CREATE TABLE Material(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
)

-----------------------
-- PRODUCT
-----------------------
CREATE TABLE Product(
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),

    idCategory INT NOT NULL,
    idBrand INT NOT NULL,
    idMaterial INT,

    status TINYINT DEFAULT 1
    CHECK (status IN (0,1,2)),

    createdAt DATETIME DEFAULT GETDATE(),
    updatedAt DATETIME,
    deletedAt DATETIME,

    FOREIGN KEY (idCategory) REFERENCES Category(id),
    FOREIGN KEY (idBrand) REFERENCES Brand(id),
    FOREIGN KEY (idMaterial) REFERENCES Material(id)
)

-----------------------
-- COLOR
-----------------------
CREATE TABLE Color(
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(50) NOT NULL
)

-----------------------
-- SIZE
-----------------------
CREATE TABLE Size(
    id INT IDENTITY(1,1) PRIMARY KEY,
    size INT NOT NULL UNIQUE
)

-----------------------
-- PRODUCT VARIANT
-----------------------
CREATE TABLE ProductVariant(
    id INT IDENTITY(1,1) PRIMARY KEY,

    idProduct INT NOT NULL,
    idColor INT NOT NULL,
    idSize INT NOT NULL,

    price DECIMAL(18,2) NOT NULL
    CHECK (price >= 0),

    quantity INT DEFAULT 0
    CHECK (quantity >= 0),

    image NVARCHAR(500),

    CONSTRAINT UQ_Variant UNIQUE(idProduct,idColor,idSize),

    FOREIGN KEY (idProduct) REFERENCES Product(id),
    FOREIGN KEY (idColor) REFERENCES Color(id),
    FOREIGN KEY (idSize) REFERENCES Size(id)
)

-----------------------
-- PAYMENT METHOD
-----------------------
CREATE TABLE PaymentMethod(
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
)

-----------------------
-- VOUCHER
-----------------------
CREATE TABLE Voucher(
    id INT IDENTITY(1,1) PRIMARY KEY,
    maVoucher NVARCHAR(50) NOT NULL UNIQUE,
    discountPercent INT,
    discountAmount DECIMAL(18,2),
    startDate DATETIME NOT NULL,
    endDate DATETIME NOT NULL,
    quantity INT DEFAULT 0,
    status TINYINT DEFAULT 1
)

-----------------------
-- INVOICE
-----------------------
CREATE TABLE Invoice(
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    date DATETIME DEFAULT GETDATE(),

    idEmployee INT NOT NULL,
    idCustomer INT NULL,
    idPaymentMethod INT NOT NULL,
    idVoucher INT NULL,

    totalMoney DECIMAL(18,2) DEFAULT 0
    CHECK (totalMoney >= 0),

    FOREIGN KEY (idEmployee) REFERENCES Employee(id),
    FOREIGN KEY (idCustomer) REFERENCES Customer(id),
    FOREIGN KEY (idPaymentMethod) REFERENCES PaymentMethod(id),
    FOREIGN KEY (idVoucher) REFERENCES Voucher(id)
)

-----------------------
-- INVOICE DETAILS
-----------------------
CREATE TABLE InvoiceDetails(
    id INT IDENTITY(1,1) PRIMARY KEY,

    idInvoice INT NOT NULL,
    idProductVariant INT NOT NULL,

    quantity INT NOT NULL
    CHECK (quantity > 0),

    price DECIMAL(18,2) NOT NULL
    CHECK (price >= 0),

    FOREIGN KEY (idInvoice) REFERENC
