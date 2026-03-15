package enums;

public enum CustomerStatusEnum {

    ACTIVE(1, "Hoạt động"),
    INACTIVE(2, "Ngừng hoạt động");

    private final int value;
    private final String label;

    CustomerStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static CustomerStatusEnum fromValue(int value) {
        for (CustomerStatusEnum status : values()) {
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