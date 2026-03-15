package entity;

import enums.CustomerStatusEnum;
import java.sql.Date;
import java.time.LocalDateTime;

public class Customer extends BaseEntity {
    
    private String code; 
    
    private String name;
    
    private String phone;
    
    private String email; 
    
    private String address;

    private Date dateOfBirth;
    
    private Boolean gender;

    private CustomerStatusEnum status;

    public Customer() {
    }

    public Customer(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Customer(String code, String name, String phone, String email, String address, Date dateOfBirth, Boolean gender, CustomerStatusEnum status) {
        this.code = code;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.status = status;
    }

    public Customer(String code, String name, String phone, String email, String address, Date dateOfBirth, Boolean gender, CustomerStatusEnum status, Integer id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        super(id, createdAt, updatedAt, deletedAt);
        this.code = code;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
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

    public CustomerStatusEnum getStatus() {
        return status;
    }

    public void setStatus(CustomerStatusEnum status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Customer{" + "code=" + code + ", name=" + name + ", phone=" + phone + ", email=" + email + ", address=" + address + ", dateOfBirth=" + dateOfBirth + ", gender=" + gender + ", status=" + status + '}';
    }
}
