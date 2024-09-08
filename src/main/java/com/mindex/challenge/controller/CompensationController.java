package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompensationController {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    @Autowired
    CompensationService compensationService;

    @GetMapping("/compensation/{id}")
    Compensation retrieveCompensation(@PathVariable String id) {
        LOG.debug("Received compensation request for employeeId [{}]", id);

        return compensationService.retrieveCompensation(id);
    }

    @PostMapping("/compensation/{id}")
    Compensation createCompensation(@PathVariable String id, @RequestBody Compensation compensation) {
        LOG.debug("Received compensation creation request for employeeId [{}]", id);

        return compensationService.createCompensation(id, compensation);
    }

}
