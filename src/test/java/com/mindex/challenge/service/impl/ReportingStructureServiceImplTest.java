package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {

    private String reportingStructureUrl;
    private String employeeIdUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureUrl = "http://localhost:" + port + "/reporting-structure/{id}";
    }

    /**
     * Tests that a 404 will be returned if no employee can be found with the given id.
     */
    @Test
    public void testNonExistentEmployee_errorExpected() {
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "iaman-employeethat-doesnot-exist");
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    /**
     * Based on the default data provided, tests that the number of reports are calculated correctly.
     */
    @Test
    public void testAllUniqueDirectReports_correctNumberOfReportsCalculated() {
        ReportingStructure johnLennonReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "16a596ae-edd3-4847-99fe-c4518e82c86f").getBody();
        assertEquals(4, johnLennonReports.getNumberOfReports());

        ReportingStructure ringoStarrReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "03aa1462-ffa9-4978-901b-7c001562cf6f").getBody();
        assertEquals(2, ringoStarrReports.getNumberOfReports());
    }

    /**
     * Based on the default data provided, tests that the number of reports calculated are returning accurate results based
     * on unique employees. (Checks that no one is counted twice even if an employee reports under more than one person)
     */
    @Test
    public void testOverlappingDirectReports_correctUniqueNumberOfReportsCalculated() {
        // Paul starts out with 0 direct reports
        Employee paulMcCartney = new Employee();
        paulMcCartney.setEmployeeId("b7839309-3348-463b-a7e3-5de1c168beb3");
        ReportingStructure paulMcCartneyReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, paulMcCartney.getEmployeeId()).getBody();
        assertEquals(0, paulMcCartneyReports.getNumberOfReports());

        // Add Pete Best as a new direct report for Paul
        List<Employee> directReports = new ArrayList<>();
        Employee peteBest = new Employee();
        peteBest.setEmployeeId("62c1084e-6e34-4630-93fd-9153afb65309");
        directReports.add(peteBest);
        paulMcCartney.setDirectReports(directReports);

        // Update in the database the new direct report info
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(paulMcCartney, headers),
                        Employee.class,
                        paulMcCartney.getEmployeeId()).getBody();

        // Paul should now have a new direct report
        paulMcCartneyReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, paulMcCartney.getEmployeeId()).getBody();
        assertEquals(1, paulMcCartneyReports.getNumberOfReports());

        // John Lennon should still be 4 even though Paul McCartney has a new direct report.
        // The new direct report is an already existing employee reporting under John at some point.
        ReportingStructure johnLennonReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "16a596ae-edd3-4847-99fe-c4518e82c86f").getBody();
        assertEquals(4, johnLennonReports.getNumberOfReports());
    }

    /**
     * Based on the default data provided, tests that the number of reports calculated is accurate based on employees that
     * actually exist.
     */
    @Test
    public void testNonExistentDirectReports_onlyExistingEmployeesAreCalculated() {
        // John starts with 4 total of direct reports
        Employee johnLennon = new Employee();
        johnLennon.setEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        ReportingStructure johnLennonReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, johnLennon.getEmployeeId()).getBody();
        assertEquals(4, johnLennonReports.getNumberOfReports());
        johnLennon = johnLennonReports.getEmployee();

        // Create a non-existent employee to add as a direct report.
        Employee nonExistentEmployee = new Employee();
        nonExistentEmployee.setEmployeeId("iaman-employeethat-doesnot-exist");
        johnLennon.getDirectReports().add(nonExistentEmployee);

        // Update John with new direct report info
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(johnLennon, headers),
                        Employee.class,
                        johnLennon.getEmployeeId()).getBody();

        // The direct reports should remain 4 since the employee recently added did not exist in the database
        johnLennonReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, johnLennon.getEmployeeId()).getBody();
        assertEquals(4, johnLennonReports.getNumberOfReports());
    }

}
