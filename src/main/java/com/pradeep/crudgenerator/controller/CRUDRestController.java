package com.pradeep.crudgenerator.controller;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.datamodal.CrudGenerationResponse;
import com.pradeep.crudgenerator.service.CRUDGeneratorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crudGenerator")
public class CRUDRestController {

    @Autowired
    private CRUDGeneratorService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrudGenerationResponse create(@Valid @RequestBody CRUDGenerationRequest request) {
        return service.createClasses(request);
    }
}
