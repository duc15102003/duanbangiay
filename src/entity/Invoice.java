package entity;

import enums.OrderStatusEnum;
import java.time.LocalDateTime;

public class Invoice {

    private int id;

    private String code;

    private int employeeId;

    private int customerId;

    private float totalAmount;

    private OrderStatusEnum status;

    private LocalDateTime createdAt;

    // customer info (lưu trực tiếp trong invoice)
    private String customerPhone;

    private String customerAddress;

    private String customerName;

    // map data
    private String employeeName;

    public Invoice() {
    }

    public Invoice(int id, String code, int employeeId, int customerId, float totalAmount,
                   OrderStatusEnum status, LocalDateTime createdAt,
                   String customerPhone, String customerAddress,
                   String customerName, String employeeName) {
        this.id = id;
        this.code = code;
        this.employeeId = employeeId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.customerName = customerName;
        this.employeeName = employeeName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public float getTotalAmount() { return totalAmount; }
    public void setTotalAmount(float totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatusEnum getStatus() { return status; }
    public void setStatus(OrderStatusEnum status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    @Override
    public String toString() {
        return code;
    }
}