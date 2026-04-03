package entity;

public class RevenueDTO {
    
    private String label;
    
    private float total;

    private int count;
    
    public RevenueDTO(String label, float total) {
        this.label = label;
        this.total = total;
    }
    
    public RevenueDTO(String label, float total, int count) {
        this.label = label;
        this.total = total;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    public String getLabel() {
        return label;
    }

    public float getTotal() {
        return total;
    }
}
