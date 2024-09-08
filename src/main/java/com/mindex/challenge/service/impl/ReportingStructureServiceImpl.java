package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure retrieveReportingStructure(String id) {
        LOG.debug("Retrieving reporting structure for employee with id [{}]", id);

        ReportingStructure reportingStructure = new ReportingStructure();

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid employeeId: " + id);
        }

        // This set will contain the all unique employee ids of direct reports.
        Set<String> allDirectReports = new HashSet<>();
        calculateNumberOfReports(employee, allDirectReports);

        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(allDirectReports.size());

        return reportingStructure;
    }

    /**
     * A recursive method that will collect all the unique employeeIds found in the directReports field of the Employees and
     * its descendants. Adding the employeeIds to a set ensures that an employee is not counted twice in case there
     * are any overlaps in direct reports.
     *
     * @param parentEmployee The current Employee to calculate the direct reports.
     * @param allDirectReports The current set of employeeIds found in direct reports.
     */
    private void calculateNumberOfReports(Employee parentEmployee, Set<String> allDirectReports) {
        List<Employee> detailedDirectReports = new ArrayList<>();
        for (Employee directReport : parentEmployee.getDirectReports()) {
            // By default, the directReports only have the employeeId, so we need to retrieve the rest of the employee
            // information from the database such as the directReports list in order to continue calculating the full
            // number of reports.
            String directReportEmployeeId = directReport.getEmployeeId();
            directReport = employeeRepository.findByEmployeeId(directReportEmployeeId);
            if (directReport != null) {
                detailedDirectReports.add(directReport);
                allDirectReports.add(directReport.getEmployeeId());
                if (directReport.getDirectReports() != null && !directReport.getDirectReports().isEmpty()) {
                    calculateNumberOfReports(directReport, allDirectReports);
                }
            } else {
                LOG.warn("The direct report under employee did not exist in the database. Skipping employee. "
                        + "parentEmployeeId=[{}], directReportEmployeeId=[{}]", parentEmployee.getEmployeeId(), directReportEmployeeId);
            }

        }

        // Resets the direct reports to contain the additional information other than the employeeId,
        // especially for the directReports field.
        parentEmployee.setDirectReports(detailedDirectReports);
    }
}
