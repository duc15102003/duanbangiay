package enums;

public enum DiscountStatusEnum {

    UPCOMING(1, "Chưa tới ngày"),
    ACTIVE(2, "Đang hoạt động"),
    EXPIRED(3, "Quá ngày");

    private final int value;
    private final String label;

    DiscountStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static DiscountStatusEnum fromValue(int value) {
        for (DiscountStatusEnum status : values()) {
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