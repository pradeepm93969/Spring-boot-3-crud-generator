package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.utils.CrudStringUtils;
import com.pradeep.crudgenerator.utils.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class ControllerGeneratorService {
    public void generateController(CRUDGenerationRequest request) {
        StringBuilder controllerClass = new StringBuilder();

        controllerClass.append("package ").append(request.getPackageName()).append(".web.endpoint;\n\n");

        generateImports(controllerClass, request);

        controllerClass.append("@RestController\n");
        controllerClass.append("@RequestMapping(\"/v1/").append(
                request.getEntityName().toLowerCase()).append("s\")\n");
        controllerClass.append("@Tag(name = \"").append(
                request.getEntityName()).append("Management\")\n");
        controllerClass.append("@SecurityRequirement(name = \"bearerAuth\")\n");
        controllerClass.append("@AllArgsConstructor\n");
        controllerClass.append("public class ").append(
                request.getEntityName()).append("Endpoint {\n\n");

        controllerClass.append("    @Getter\n");
        controllerClass.append("    private final ").append(request.getEntityName()).append("Service ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName())).append("Service;\n\n");

        generateGetMethod(controllerClass, request);
        generateGetByIdMethod(controllerClass, request);
        generateCreateMethod(controllerClass, request);
        generateUpdateMethod(controllerClass, request);
        generatePatchMethod(controllerClass, request);
        generateDeleteByIdMethod(controllerClass, request);
        
        controllerClass.append("}");

        // Write request class to file
        String filePath = request.getDirectory() + "\\web\\endpoint\\" + request.getEntityName() + "Endpoint.java";
        FileUtils.writeFile(controllerClass.toString(), filePath);

    }

    private void generateGetMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @GetMapping\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_READ_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<Page<").append(request.getEntityName()).append(">> get(\n");
        controllerClass.append("            @RequestParam(defaultValue = \"\") String rsql,\n");
        controllerClass.append("            @Positive @RequestParam(defaultValue = \"1\") Integer pageNo,\n");
        controllerClass.append("            @Positive @RequestParam(defaultValue = \"10\") Integer pageSize,\n");
        controllerClass.append("            @RequestParam(defaultValue = \"id\") String sortBy,\n");
        controllerClass.append("            @RequestParam(defaultValue = \"asc\") String sortDirection) {\n");
        controllerClass.append("        Page<").append(request.getEntityName()).append("> result = get")
                .append(request.getEntityName())
                .append("Service().get(\n");
        controllerClass.append("            rsql, pageNo - 1, pageSize, sortBy, sortDirection);\n");
        controllerClass.append("        return ResponseEntity.ok(result);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateGetByIdMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @GetMapping(\"/{id}\")\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_READ_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<").append(request.getEntityName())
                .append("> getById(@PathVariable(\"id\")\n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") ")
                .append("String id) {\n");
        controllerClass.append("        ").append(request.getEntityName()).append(" response = get")
                .append(request.getEntityName()).append("Service().getById(id);\n");
        controllerClass.append("        if (response == null) {\n");
        controllerClass.append("            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);\n");
        controllerClass.append("        }\n");
        controllerClass.append("        return ResponseEntity.ok(response);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateCreateMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @PostMapping\n");
        controllerClass.append("    @ResponseStatus(HttpStatus.CREATED)\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<").append(request.getEntityName())
                .append("> create(@Valid @RequestBody ").append(request.getEntityName())
                .append(" request) {\n");
        controllerClass.append("        ").append(request.getEntityName()).append(" response = ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.create(request);\n");
        controllerClass.append("        return ResponseEntity.status(HttpStatus.CREATED).body(response);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateUpdateMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @PutMapping(\"/{id}\")\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<").append(request.getEntityName())
                .append("> update(@PathVariable(\"id\")\n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") ")
                .append("String id,\n");
        controllerClass.append("            @Valid @RequestBody ")
                .append(request.getEntityName()).append(" request) {\n");
        controllerClass.append("        ").append(request.getEntityName()).append(" response = get")
                .append(request.getEntityName())
                .append("Service().update(id, request);\n");
        controllerClass.append("        return new ResponseEntity<>(response, HttpStatus.OK);\n");
        controllerClass.append("    }\n\n");
    }

    private void generatePatchMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @PatchMapping(\"/{id}\")\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<").append(request.getEntityName())
                .append("> patch(@PathVariable(\"id\")\n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") ")
                .append("String id,\n");
        controllerClass.append("            @RequestBody Map<String, Object> request) {\n");
        controllerClass.append("        ").append(request.getEntityName()).append(" response = get")
                .append(request.getEntityName())
                .append("Service().patch(id, request);\n");
        controllerClass.append("        return new ResponseEntity<>(response, HttpStatus.OK);\n");
        controllerClass.append("    }\n\n");
    }


    private void generateDeleteByIdMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @DeleteMapping(\"/{id}\")\n");
        controllerClass.append("    @ResponseStatus(HttpStatus.NO_CONTENT)\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<Void> deleteById(@PathVariable(\"id\") \n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") String id) {\n");
        controllerClass.append("        get").append(request.getEntityName())
                .append("Service().deleteById(id);\n");
        controllerClass.append("        return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateImports(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("import ").append(request.getPackageName()).append(".domain.").append(
                request.getEntityName()).append(";\n");
        controllerClass.append("import ").append(request.getPackageName()).append(".service.").append(
                request.getEntityName()).append("Service;\n");
        controllerClass.append("import io.swagger.v3.oas.annotations.security.SecurityRequirement;\n");
        controllerClass.append("import io.swagger.v3.oas.annotations.tags.Tag;\n");
        controllerClass.append("import jakarta.validation.Valid;\n");
        controllerClass.append("import jakarta.validation.constraints.NotBlank;\n");
        controllerClass.append("import jakarta.validation.constraints.Positive;\n");
        controllerClass.append("import lombok.AllArgsConstructor;\n");
        controllerClass.append("import lombok.Getter;\n");
        controllerClass.append("import org.springframework.data.domain.Page;\n");
        controllerClass.append("import org.springframework.http.HttpStatus;\n");
        controllerClass.append("import org.springframework.http.ResponseEntity;\n");
        controllerClass.append("import org.springframework.security.access.prepost.PreAuthorize;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.DeleteMapping;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.GetMapping;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.PathVariable;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.PostMapping;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.PutMapping;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.PatchMapping;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.RequestBody;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.RequestParam;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.ResponseStatus;\n");
        controllerClass.append("import org.springframework.web.bind.annotation.RestController;\n\n");

        controllerClass.append("import java.util.Map;\n\n");
    }
}
