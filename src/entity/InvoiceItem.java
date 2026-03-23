package entity;

import java.time.LocalDateTime;

public class InvoiceItem {
    
    private int id;
    
    private int invoiceId;
    
    private int productVariantId;
    
    private int quantity;
    
    private float price;
    
    private LocalDateTime createdAt;
    
    //Map data
    private String invoiceCode;
    
    private int brandId;
    
    private String brandName;
    
    private int categoryId;
    
    private String categoryName;
    
    private int sizeId;
    
    private String sizeName;
    
    private int colorId;
    
    private String colorName;
    
    private int productId;
    
    private String productCode;
    
    private String productName;
    
    private String image;
    
    private String status;
    
    private String description;

    public InvoiceItem() {
    }

    public InvoiceItem(int id, int invoiceId, int productVariantId, int quantity, float price, LocalDateTime createdAt, String invoiceCode, int brandId, String brandName, int categoryId, String categoryName, int sizeId, String sizeName, int colorId, String colorName, int productId, String productName, String image, String status, String description) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = createdAt;
        this.invoiceCode = invoiceCode;
        this.brandId = brandId;
        this.brandName = brandName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.colorId = colorId;
        this.colorName = colorName;
        this.productId = productId;
        this.productName = productName;
        this.image = image;
        this.status = status;
        this.description = description;
    }

    public InvoiceItem(int id, int invoiceId, int productVariantId, int quantity, float price, LocalDateTime createdAt, String invoiceCode, int brandId, String brandName, int categoryId, String categoryName, int sizeId, String sizeName, int colorId, String colorName, int productId, String productCode, String productName, String image, String status, String description) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = createdAt;
        this.invoiceCode = invoiceCode;
        this.brandId = brandId;
        this.brandName = brandName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.colorId = colorId;
        this.colorName = colorName;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.image = image;
        this.status = status;
        this.description = description;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(int productVariantId) {
        this.productVariantId = productVariantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}