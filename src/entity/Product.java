package entity;

import enums.ProductStatusEnum;
import java.time.LocalDateTime;

public class Product extends BaseEntity {
    
    private String code;
    
    private String name;
    
    private String description;
    
    private int categoryId;
        
    private int brandId;
    
    private ProductStatusEnum status;
    
    //Map data
    private String brandName;
    
    private String categoryName;

    public Product() {
    }

    public Product(String code, String name, String description, int categoryId, String categoryName, int brandId, String brandName, ProductStatusEnum status) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brandId = brandId;
        this.brandName = brandName;
        this.status = status;
    }

    public Product(String code, String name, String description, int categoryId, String categoryName, int brandId, String brandName, ProductStatusEnum status, int id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(id, createdAt, updatedAt, deletedAt);
        this.code = code;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brandId = brandId;
        this.brandName = brandName;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public ProductStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ProductStatusEnum status) {
        this.status = status;
    }
    
    @Override
    public String toString(){
        return this.name;
    }
}
