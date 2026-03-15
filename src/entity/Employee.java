package entity;

import enums.RoleEnum;
import java.time.LocalDateTime;
import java.sql.Date;

public class Employee extends BaseEntity {
    
    private String code;
    
    private String name;
    
    private String username;
    
    private String password;
    
    private String phone;
    
    private String email;
    
    private String address;
    
    private RoleEnum role;
    
    private Date dateOfBirth;
    
    private Boolean gender;

    public Employee() {
    }

    public Employee(String code, String name, String username, String password, String phone, String email, String address, RoleEnum role, Date dateOfBirth, Boolean gender) {
        this.code = code;
        this.name = name;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public Employee(String code, String name, String username, String password, String phone, String email, String address, RoleEnum role, Date dateOfBirth, Boolean gender, Integer id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(id, createdAt, updatedAt, deletedAt);
        this.code = code;
        this.name = name;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }
    
}
