package entity.filter;

public class EmployeeFilter {
    
    private String search;

    public EmployeeFilter() {
    }

    public EmployeeFilter(String search) {
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
