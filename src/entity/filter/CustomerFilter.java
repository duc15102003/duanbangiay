package entity.filter;

public class CustomerFilter {
    
    private String search;

    public CustomerFilter() {
    }

    public CustomerFilter(String search) {
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
