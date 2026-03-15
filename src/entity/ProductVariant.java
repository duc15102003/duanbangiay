package entity;

import enums.ProductStatusEnum;
import java.time.LocalDateTime;

public class ProductVariant extends BaseEntity {
    
    private int productId;
    
    private int colorId;  
    
    private int sizeId;
        
    private float price;
    
    private int quantity;
    
    private String image;
    
    // Map data
    private String productCode;
    
    private String productName;
    
    private String colorName;
    
    private String sizeName;
    
    private String brandName;
    
    private String categoryName;
    
    private String description;
    
    private ProductStatusEnum status;
    
    public ProductVariant() {
    }

    public ProductVariant(int productId, int colorId, int sizeId, float price, int quantity, String image) {
        this.productId = productId;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
    }
    
    public ProductVariant(int productId, int colorId, int sizeId, float price, int quantity, String image, String productCode, String productName, String colorName, String sizeName, String brandName, String categoryName, String description, ProductStatusEnum status) {
        this.productId = productId;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
        this.productCode = productCode;
        this.productName = productName;
        this.colorName = colorName;
        this.sizeName = sizeName;
        this.brandName = brandName;
        this.categoryName = categoryName;
        this.description = description;
        this.status = status;
    }

    public ProductVariant(int productId, int colorId, int sizeId, float price, int quantity, String image, String productCode, String productName, String colorName, String sizeName, String brandName, String categoryName, String description, ProductStatusEnum status, int id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(id, createdAt, updatedAt, deletedAt);
        this.productId = productId;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
        this.productCode = productCode;
        this.productName = productName;
        this.colorName = colorName;
        this.sizeName = sizeName;
        this.brandName = brandName;
        this.categoryName = categoryName;
        this.description = description;
        this.status = status;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ProductStatusEnum status) {
        this.status = status;
    }
}
