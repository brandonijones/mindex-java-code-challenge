package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testReadNonExistentEmployee_notFoundStatus() {
        ResponseEntity<Compensation> responseEntity = restTemplate.getForEntity(compensationUrl, Compensation.class, "iaman-employeethat-doesnot-exist");
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testCreateNonExistentEmployee_notFoundStatus() {
        Compensation testComp = new Compensation();
        testComp.setSalary("200000");
        testComp.setEffectiveDate("2024-09-08");
        ResponseEntity<Compensation> responseEntity = restTemplate.postForEntity(compensationUrl, testComp, Compensation.class, "iaman-employeethat-doesnot-exist");
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testCreateInvalidSalaryFormat_badRequestStatus() {
        Compensation testComp = new Compensation();
        testComp.setSalary("200000.5.5");
        testComp.setEffectiveDate("2024-09-08");
        ResponseEntity<Compensation> responseEntity = restTemplate.postForEntity(compensationUrl, testComp, Compensation.class, "16a596ae-edd3-4847-99fe-c4518e82c86f");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testCreateInvalidEffectiveDateFormat_badRequestStatus() {
        Compensation testComp = new Compensation();
        testComp.setSalary("200000.5");
        testComp.setEffectiveDate("2024-09-088");
        ResponseEntity<Compensation> responseEntity = restTemplate.postForEntity(compensationUrl, testComp, Compensation.class, "16a596ae-edd3-4847-99fe-c4518e82c86f");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testReadNoCompensationInformationForValidEmployee_notFoundStatus() {
        ResponseEntity<Compensation> responseEntity = restTemplate.getForEntity(compensationUrl, Compensation.class, "16a596ae-edd3-4847-99fe-c4518e82c86f");
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testCreateReadCompensationInfo_successfulResponse() {
        Compensation testComp = new Compensation();
        testComp.setSalary("200000");
        testComp.setEffectiveDate("2024-09-08");
        ResponseEntity<Compensation> responseEntity = restTemplate.postForEntity(compensationUrl, testComp, Compensation.class, "62c1084e-6e34-4630-93fd-9153afb65309");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        responseEntity = restTemplate.getForEntity(compensationUrl, Compensation.class, "62c1084e-6e34-4630-93fd-9153afb65309");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Compensation compResponseBody = responseEntity.getBody();
        assertNotNull(compResponseBody);
        assertEquals(testComp.getSalary(), compResponseBody.getSalary());
        assertEquals(testComp.getEffectiveDate(), compResponseBody.getEffectiveDate());
    }

}
