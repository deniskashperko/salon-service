package com.example.salon_service.service;

import com.example.salon_service.entity.Procedure;
import com.example.salon_service.repository.ProcedureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcedureService {

    @Autowired
    private ProcedureRepository procedureRepository;

    public List<Procedure> getAllProcedures() {
        return procedureRepository.findAll();
    }

    public Procedure getProcedureById(Long id) {
        return procedureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Процедура не найдена"));
    }
}
