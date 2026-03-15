package entity;

import java.time.LocalDateTime;

public class Category extends BaseEntity {
    
    private String code;
    
    private String name;

    public Category() {
    }

    public Category(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Category(String code, String name, int id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(id, createdAt, updatedAt, deletedAt);
        this.code = code;
        this.name = name;
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
    
    @Override
    public String toString() {
        return name;
    }
}
