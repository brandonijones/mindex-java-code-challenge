package com.mindex.challenge.dao;

import com.mindex.challenge.data.CompensationDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompensationRepository extends MongoRepository<CompensationDTO, String> {

    CompensationDTO findByEmployeeId(String employeeId);

}
