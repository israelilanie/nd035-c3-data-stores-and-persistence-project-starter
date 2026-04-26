package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.pet.PetRepository;
import com.udacity.jdnd.course3.critter.user.Employee;
import com.udacity.jdnd.course3.critter.user.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Schedules.
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final PetRepository petRepository;
    private final EmployeeRepository employeeRepository;

    public ScheduleController(ScheduleRepository scheduleRepository,
                              PetRepository petRepository,
                              EmployeeRepository employeeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.petRepository = petRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    public ScheduleDTO createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        Schedule schedule = new Schedule();
        schedule.setDate(scheduleDTO.getDate());
        schedule.setActivities(scheduleDTO.getActivities() == null ? new java.util.HashSet<>() : new java.util.HashSet<>(scheduleDTO.getActivities()));
        schedule.setPets(loadPets(scheduleDTO.getPetIds()));
        schedule.setEmployees(loadEmployees(scheduleDTO.getEmployeeIds()));
        return toScheduleDTO(scheduleRepository.save(schedule));
    }

    @GetMapping
    public List<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAllByOrderByIdAsc().stream()
                .map(this::toScheduleDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/pet/{petId}")
    public List<ScheduleDTO> getScheduleForPet(@PathVariable long petId) {
        return scheduleRepository.findByPetId(petId).stream()
                .map(this::toScheduleDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/employee/{employeeId}")
    public List<ScheduleDTO> getScheduleForEmployee(@PathVariable long employeeId) {
        return scheduleRepository.findByEmployeeId(employeeId).stream()
                .map(this::toScheduleDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/customer/{customerId}")
    public List<ScheduleDTO> getScheduleForCustomer(@PathVariable long customerId) {
        return scheduleRepository.findByCustomerId(customerId).stream()
                .map(this::toScheduleDTO)
                .collect(Collectors.toList());
    }

    private List<Pet> loadPets(List<Long> petIds) {
        List<Pet> pets = new ArrayList<>();
        if (petIds == null) {
            return pets;
        }
        for (Long petId : petIds) {
            Pet pet = petRepository.findById(petId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
            pets.add(pet);
        }
        return pets;
    }

    private List<Employee> loadEmployees(List<Long> employeeIds) {
        List<Employee> employees = new ArrayList<>();
        if (employeeIds == null) {
            return employees;
        }
        for (Long employeeId : employeeIds) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
            employees.add(employee);
        }
        return employees;
    }

    private ScheduleDTO toScheduleDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDate(schedule.getDate());
        dto.setActivities(schedule.getActivities());
        dto.setEmployeeIds(schedule.getEmployees().stream()
                .map(Employee::getId)
                .collect(Collectors.toList()));
        dto.setPetIds(schedule.getPets().stream()
                .map(Pet::getId)
                .collect(Collectors.toList()));
        return dto;
    }
}
