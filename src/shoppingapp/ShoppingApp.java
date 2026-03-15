package shoppingapp;

import entity.Customer;
import entity.Employee;
import enums.RoleEnum;
import java.util.List;
import service.CustomerService;
import service.EmployeeService;
import ui.auth.LoginUI;

public class ShoppingApp {
    
    private CustomerService customerService = new CustomerService();
    private EmployeeService employeeService = new EmployeeService();

    public static void main(String[] args) {
        ShoppingApp app = new ShoppingApp();

        app.initDataCustomer();
        app.initDataEmployee();
        
        new LoginUI().setVisible(true);
    }
    
    public void initDataCustomer(){

        if(!customerService.findAll(null).isEmpty()){
            return;
        }

        List<Customer> customers = List.of(
            new Customer("KL", "Khách lẻ")
        );
        
        for(Customer c : customers){
            customerService.insert(c);
        }
    }
    
    public void initDataEmployee(){

        if(!employeeService.findAll(null).isEmpty()){
            return;
        }

        Employee manager = new Employee();
        manager.setUsername("manager");
        manager.setPassword("123");
        manager.setGender(true); 
        manager.setRole(RoleEnum.MANAGER);

        Employee staff = new Employee();
        staff.setUsername("staff");
        staff.setPassword("123");
        manager.setGender(true); 
        staff.setRole(RoleEnum.STAFF);

        List<Employee> employees = List.of(manager, staff);

        for(Employee e : employees){
            employeeService.insert(e);
        }
    }
}
