package com.dst.restaurantmanagement.services;

import com.dst.restaurantmanagement.enums.RoleType;
import com.dst.restaurantmanagement.models.dto.AddEmployeeDTO;
import com.dst.restaurantmanagement.models.dto.EditEmployeeDTO;
import com.dst.restaurantmanagement.models.dto.EmployeeInfoDTO;
import com.dst.restaurantmanagement.models.entities.Employee;
import com.dst.restaurantmanagement.models.entities.Role;
import com.dst.restaurantmanagement.repositories.EmployeeRepository;
import com.dst.restaurantmanagement.repositories.RoleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.password}")
    private String adminPassword;
    @Value("${app.admin.firstName}")
    private String adminFirstName;
    @Value("${app.admin.lastName}")
    private String adminLastName;
    @Value("${app.admin.phoneNumber}")
    private String adminPhoneNumber;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper mapper;

    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository, ModelMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    public Boolean saveEmployee(AddEmployeeDTO employeeDTO) {

        Optional<Employee> employeeByUsername = this.employeeRepository.getByUsername(employeeDTO.getUsername());
        Optional<Employee> employeeByPhoneNumber = this.employeeRepository.getByPhoneNumber(employeeDTO.getPhoneNumber());

        if (employeeByUsername.isPresent() || employeeByPhoneNumber.isPresent()) {
            return false;
        }

        Role employeeRole = roleRepository.findByRoleType(RoleType.valueOf(employeeDTO.getRole()));

        Employee employee = mapper.map(employeeDTO, Employee.class);

        employee.setRole(employeeRole);

        //TODO: encrypted password
        employee.setPassword(employeeDTO.getPassword());

        this.employeeRepository.save(employee);

        return true;
    }

    public void initAdministrator() {
        Role role = this.roleRepository.findByRoleType(RoleType.ADMIN);

        Employee employee = new Employee(
                adminFirstName,
                adminLastName,
                adminUsername,
                adminPassword,
                adminPhoneNumber,
                LocalDate.now(),
                role
        );

        this.employeeRepository.save(employee);
    }

    public Boolean isAdministratorInitialized() {
        Optional<Employee> admin = this.employeeRepository.findByUsernameAndPassword(this.adminUsername, this.adminPassword);

        return admin.isPresent();
    }

    public List<EmployeeInfoDTO> getAllEmployees() {
        List<EmployeeInfoDTO> employeeInfoDTOS = this.employeeRepository.findAll().stream().map(this::mapToEmployeeInfoDTO).toList();

        return employeeInfoDTOS;
    }

    private EmployeeInfoDTO mapToEmployeeInfoDTO(Employee employee) {
        return new EmployeeInfoDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getUsername(),
                employee.getPhoneNumber(),
                employee.getHireDate(),
                employee.getRole().getRoleType().name()
        );
    }

    public void delete(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
    }

    public EditEmployeeDTO getEmployee(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);

        return mapper.map(employee.get(), EditEmployeeDTO.class);
    }
}
