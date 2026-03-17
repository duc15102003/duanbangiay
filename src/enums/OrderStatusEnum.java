package enums;

public enum OrderStatusEnum {
    
    PENDING_PAYMENT(1, "Chờ thanh toán"),
    PAID(2, "Đã thanh toán"),
    CANCELLED(3, "Đã hủy");

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
