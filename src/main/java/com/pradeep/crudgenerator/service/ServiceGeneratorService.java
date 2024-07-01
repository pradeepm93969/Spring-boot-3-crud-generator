package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.utils.CrudStringUtils;
import com.pradeep.crudgenerator.utils.FileUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ServiceGeneratorService {

    public void generateService(CRUDGenerationRequest request) {
        StringBuilder serviceClass = new StringBuilder();

        serviceClass.append("package ").append(request.getPackageName()).append(".service;\n\n");

        generateImports(serviceClass, request);

        serviceClass.append("@Service\n");
        serviceClass.append("@AllArgsConstructor\n");
        serviceClass.append("public class ").append(
                request.getEntityName()).append("Service {\n\n");

        serviceClass.append("    @Getter\n");
        serviceClass.append("    private final ").append(request.getEntityName()).append("Repository ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName())).append("Repository;\n\n");

        generateGetMethod(serviceClass, request);
        generateGetByIdMethod(serviceClass, request);
        generateCreateMethod(serviceClass, request);
        generateUpdateMethod(serviceClass, request);
        generateDeleteByIdMethod(serviceClass, request);
        generateFindByIdMethod(serviceClass, request);
        
        serviceClass.append("}");

        // Write request class to file
        String filePath = request.getDirectory() + "\\service\\" + request.getEntityName() + "Service.java";
        FileUtils.writeFile(serviceClass.toString(), filePath);

    }

    private void generateGetMethod(StringBuilder serviceClass, CRUDGenerationRequest request) {
        serviceClass.append("    public Page<").append(request.getEntityName()).append("> get(");
        serviceClass.append("String rsql, Integer pageNo, Integer pageSize, \n");
        serviceClass.append("            String sortBy, String sortDirection) {\n");

        serviceClass.append("        Pageable pageable = PageRequest.of(pageNo, pageSize,\n");
        serviceClass.append("            Sort.by((StringUtils.isBlank(sortDirection) || sortDirection.equalsIgnoreCase(\"asc\"))\n");
        serviceClass.append("            ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));\n");

        serviceClass.append("        Specification<Jpa").append(request.getEntityName())
                .append("> spec = RSQLSpecificationFactory.createSpecification(rsql);\n");

        serviceClass.append("        Page<Jpa").append(request.getEntityName())
                .append("> page = get").append(request.getEntityName())
                .append("Repository().findAll(spec, pageable);\n");

        serviceClass.append("        return page.map(entity -> entity.fromMe());\n");

        serviceClass.append("    }\n\n");
    }

    private void generateGetByIdMethod(StringBuilder serviceClass, CRUDGenerationRequest request) {
        serviceClass.append("    @Cacheable(value = \"").append(request.getEntityName()).append("\", key = \"#id\")\n");
        serviceClass.append("    public ").append(request.getEntityName()).append(" getById(String id) {\n");
        serviceClass.append("        return findById(id).fromMe();\n");
        serviceClass.append("    }\n\n");
    }

    private void generateCreateMethod(StringBuilder serviceClass, CRUDGenerationRequest request) {
        serviceClass.append("    public ").append(request.getEntityName())
                .append(" create(")
                .append(request.getEntityName()).append(" request) {\n");
        serviceClass.append("        Jpa").append(request.getEntityName()).append(" entity = new Jpa")
                .append(request.getEntityName()).append("();\n");
        serviceClass.append("        entity.toMe(request);\n");
        serviceClass.append("        entity = get").append(request.getEntityName()).append("Repository().save(entity);\n");
        serviceClass.append("        return entity.fromMe();\n");
        serviceClass.append("    }\n\n");
    }

    private void generateUpdateMethod(StringBuilder serviceClass, CRUDGenerationRequest request) {
        serviceClass.append("    @CachePut(value = \"").append(request.getEntityName()).append("\", key = \"#id\")\n");
        serviceClass.append("    public ").append(request.getEntityName())
                .append(" update(String id, ")
                .append(request.getEntityName()).append(" request) {\n");
        serviceClass.append("        Jpa").append(request.getEntityName()).append(" entity = findById(id);\n");
        serviceClass.append("        entity.toMe(request);\n");
        serviceClass.append("        entity = get").append(request.getEntityName()).append("Repository().save(entity);\n");
        serviceClass.append("        return entity.fromMe();\n");
        serviceClass.append("    }\n\n");
    }

    private void generateDeleteByIdMethod(StringBuilder serviceClass, CRUDGenerationRequest request) {
        serviceClass.append("    @CacheEvict(value = \"").append(request.getEntityName()).append("\", key = \"#id\")\n");
        serviceClass.append("    public void deleteById(String id) {\n");
        serviceClass.append("        get").append(request.getEntityName())
                .append("Repository().deleteById(id);\n");
        serviceClass.append("    }\n\n");
    }

    private void generateFindByIdMethod(StringBuilder serviceClass, CRUDGenerationRequest request) {
        serviceClass.append("    private Jpa").append(request.getEntityName()).append(" findById(String id) {\n");
        serviceClass.append("        return get").append(request.getEntityName())
                .append("Repository().findById(id)\n");
        serviceClass.append("            .orElseThrow(() -> new EntityNotFoundException(\"Entity not found with id: \" + id));\n");
        serviceClass.append("    }\n\n");
    }

    private void generateImports(StringBuilder serviceClass, CRUDGenerationRequest request) {
        serviceClass.append("import ").append(request.getPackageName()).append(".domain.").append(
                request.getEntityName()).append(";\n");
        serviceClass.append("import ").append(request.getPackageName()).append(".jpa.domain.Jpa").append(
                request.getEntityName()).append(";\n");
        serviceClass.append("import ").append(request.getPackageName()).append(".jpa.repository.").append(
                request.getEntityName()).append("Repository;\n");

        serviceClass.append("import ").append(request.getPackageName()).append(".common.jpa.RSQLSpecificationFactory;\n");
        serviceClass.append("import ").append(request.getPackageName()).append(".jpa.domain.Jpa").append(
                request.getEntityName()).append(";\n");

        if (request.getProperties().stream().anyMatch(p -> p.getType().equalsIgnoreCase("MonetaryAmount"))) {
            serviceClass.append("import javax.money.Monetary;\n");
            serviceClass.append("import java.math.BigDecimal;\n");
        }

        serviceClass.append("import jakarta.persistence.EntityNotFoundException;\n");
        serviceClass.append("import lombok.AllArgsConstructor;\n");
        serviceClass.append("import lombok.Getter;\n");
        serviceClass.append("import org.apache.commons.lang3.StringUtils;\n");
        serviceClass.append("import org.springframework.data.domain.Page;\n");
        serviceClass.append("import org.springframework.data.domain.PageRequest;\n");
        serviceClass.append("import org.springframework.data.domain.Pageable;\n");
        serviceClass.append("import org.springframework.data.domain.Sort;\n");
        serviceClass.append("import org.springframework.data.jpa.domain.Specification;\n");
        serviceClass.append("import org.springframework.stereotype.Service;\n");
        serviceClass.append("import org.springframework.cache.annotation.CacheEvict;\n");
        serviceClass.append("import org.springframework.cache.annotation.CachePut;\n");
        serviceClass.append("import org.springframework.cache.annotation.Cacheable;\n");
        serviceClass.append("\n");

    }
}
