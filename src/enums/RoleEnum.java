package enums;

public enum RoleEnum {
    
    STAFF(1, "Nhân viên bán hàng"),
    MANAGER(2, "Quản lý");

    private final int value;
    private final String label;

    RoleEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static RoleEnum fromValue(int value) {
        for (RoleEnum status : values()) {
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
