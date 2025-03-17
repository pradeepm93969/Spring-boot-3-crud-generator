package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.datamodal.CrudGenerationResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
@AllArgsConstructor
public class CRUDGeneratorService {
    private EntityGeneratorService entityGeneratorService;
    private DomainGeneratorService domainGeneratorService;
    private ControllerGeneratorService controllerGeneratorService;
    private ServiceGeneratorService serviceGeneratorService;
    private RepositoryGeneratorService repositoryGeneratorService;
    private LiquibaseGeneratorService liquibaseGeneratorService;

    public CrudGenerationResponse createClasses(CRUDGenerationRequest request) {

        if (StringUtils.isBlank(request.getCommonPackageName())) {
            request.setCommonPackageName(request.getPackageName());
        }

        request.getProperties().stream().forEach(p -> {
            if (StringUtils.isNotBlank(p.getPattern())) {
                try {
                    Pattern.compile(p.getPattern());
                } catch (PatternSyntaxException e) {
                    throw new RestClientException("Invalid Regex Pattern for Property" + p.getName());
                }
            }
        });

        //Generate Entity
        entityGeneratorService.generateEntity(request);

        //Generate Request & Response
        domainGeneratorService.generateDomain(request);

        //Generate Controller
        controllerGeneratorService.generateController(request);

        //Generate Service
        serviceGeneratorService.generateService(request);

        //Generate Repository
        repositoryGeneratorService.generateRepository(request);

        //Generate Liquibase File
        liquibaseGeneratorService.generateLiquibase(request);

        return new CrudGenerationResponse("SUCCESS");
    }
}
