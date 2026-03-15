package entity;

import enums.RoleEnum;

public class AuthResult {

    private Integer id;
    
    private String name;
    
    private RoleEnum role;

    public AuthResult() {
    }

    public AuthResult(Integer id, RoleEnum role) {
        this.id = id;
        this.role = role;
    }

    public AuthResult(Integer id, String name, RoleEnum role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }
}
