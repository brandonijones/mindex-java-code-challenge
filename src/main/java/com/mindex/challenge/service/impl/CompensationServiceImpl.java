package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.CompensationDTO;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    CompensationRepository compensationRepository;

    @Override
    public Compensation retrieveCompensation(String employeeId) {
        LOG.debug("Retrieving compensation info for employee with id [{}]", employeeId);
        Employee employee = employeeRepository.findByEmployeeId(employeeId);

        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid employee id " + employeeId);
        }

        Compensation compensation = new Compensation();
        compensation.setEmployee(employee);

        CompensationDTO compensationDTO = compensationRepository.findByEmployeeId(compensation.getEmployee().getEmployeeId());
        if (compensationDTO == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No compensation info found for employee id " + employeeId);
        }
        compensation.setSalary(compensationDTO.getSalary().toString());
        compensation.setEffectiveDate(compensationDTO.getEffectiveDate().format(DateTimeFormatter.ISO_DATE));

        return compensation;
    }

    @Override
    public Compensation createCompensation(String employeeId, Compensation compensation) {
        LOG.debug("Creating compensation info for employee with id [{}]", employeeId);

        Employee employee = employeeRepository.findByEmployeeId(employeeId);

        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid employee id " + employeeId);
        }
        compensation.setEmployee(employee);

        CompensationDTO compensationDTO = new CompensationDTO();
        compensationDTO.setEmployeeId(employee.getEmployeeId());
        try {
            // Validate the salary and effective date formats before saving into the database.
            compensationDTO.setSalary(new BigDecimal(compensation.getSalary()));
            compensationDTO.setEffectiveDate(LocalDate.parse(compensation.getEffectiveDate()));
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid salary format: " + compensation.getSalary());
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid effective date format (YYYY-MM-DD): " + compensation.getEffectiveDate());
        }

        compensationRepository.save(compensationDTO);

        return compensation;
    }

}
