package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.pet.Pet;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Users.
 *
 * Includes requests for both customers and employees. Splitting this into separate user and customer controllers
 * would be fine too, though that is not part of the required scope for this class.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public UserController(CustomerRepository customerRepository,
                          EmployeeRepository employeeRepository) {
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/customer")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customerDTO){
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setNotes(customerDTO.getNotes());
        return toCustomerDTO(customerRepository.save(customer));
    }

    @GetMapping("/customer")
    public List<CustomerDTO> getAllCustomers(){
        return customerRepository.findAllByOrderByIdAsc().stream()
                .map(this::toCustomerDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/customer/pet/{petId}")
    public CustomerDTO getOwnerByPet(@PathVariable long petId){
        return customerRepository.findByPetsId(petId)
                .map(this::toCustomerDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @PostMapping("/employee")
    public EmployeeDTO saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        employee.setSkills(employeeDTO.getSkills() == null ? new HashSet<>() : new HashSet<>(employeeDTO.getSkills()));
        if (employeeDTO.getDaysAvailable() != null) {
            employee.setDaysAvailable(new HashSet<>(employeeDTO.getDaysAvailable()));
        }
        return toEmployeeDTO(employeeRepository.save(employee));
    }

    @PostMapping("/employee/{employeeId}")
    public EmployeeDTO getEmployee(@PathVariable long employeeId) {
        return toEmployeeDTO(findEmployee(employeeId));
    }

    @PutMapping("/employee/{employeeId}")
    public void setAvailability(@RequestBody Set<DayOfWeek> daysAvailable, @PathVariable long employeeId) {
        Employee employee = findEmployee(employeeId);
        employee.setDaysAvailable(daysAvailable == null ? new HashSet<>() : new HashSet<>(daysAvailable));
        employeeRepository.save(employee);
    }

    @GetMapping("/employee/availability")
    public List<EmployeeDTO> findEmployeesForService(@RequestBody EmployeeRequestDTO employeeDTO) {
        DayOfWeek requestedDay = employeeDTO.getDate().getDayOfWeek();
        Set<EmployeeSkill> requestedSkills = employeeDTO.getSkills() == null ? Set.of() : employeeDTO.getSkills();
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getDaysAvailable() != null && employee.getDaysAvailable().contains(requestedDay))
                .filter(employee -> employee.getSkills() != null && employee.getSkills().containsAll(requestedSkills))
                .map(this::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    private Employee findEmployee(long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private CustomerDTO toCustomerDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setNotes(customer.getNotes());
        List<Long> petIds = customer.getPets() == null ? new ArrayList<>() : customer.getPets().stream()
                .map(Pet::getId)
                .collect(Collectors.toList());
        dto.setPetIds(petIds);
        return dto;
    }

    private EmployeeDTO toEmployeeDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setSkills(employee.getSkills() == null ? new HashSet<>() : new HashSet<>(employee.getSkills()));
        if (employee.getDaysAvailable() != null && !employee.getDaysAvailable().isEmpty()) {
            dto.setDaysAvailable(new HashSet<>(employee.getDaysAvailable()));
        }
        return dto;
    }
}
