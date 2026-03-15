package enums;

public enum ProductStatusEnum {

    ACTIVE(1, "Hoạt động"),
    INACTIVE(2, "Ngừng bán"),
    OUT_OF_STOCK(3, "Hết hàng");

    private final int value;
    private final String label;

    ProductStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static ProductStatusEnum fromValue(int value) {
        for (ProductStatusEnum status : values()) {
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