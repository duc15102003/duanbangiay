package entity;

public class RevenueDTO {
    
    private String label;
    
    private float total;

    public RevenueDTO(String label, float total) {
        this.label = label;
        this.total = total;
    }

    public String getLabel() {
        return label;
    }

    public float getTotal() {
        return total;
    }
}
