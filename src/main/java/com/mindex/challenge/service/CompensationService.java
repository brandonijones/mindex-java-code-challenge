package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;

import java.util.List;

public interface CompensationService {

    Compensation retrieveCompensation(String employeeId);

    Compensation createCompensation(String employeeId, Compensation compensation);

}
