package enums;

public enum OrderStatusEnum {
    
    DRAFT(1, "Nháp"),
    PENDING_PAYMENT(2, "Chờ thanh toán"),
    PAID(3, "Đã thanh toán"),
    CANCELLED(4, "Đã hủy"),
    COMPLETED(5, "Hoàn thành");

    private final int value;
    private final String label;

    OrderStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static OrderStatusEnum fromValue(int value) {
        for (OrderStatusEnum status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
