package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.utils.CrudStringUtils;
import com.pradeep.crudgenerator.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ControllerGeneratorService {
    public void generateController(CRUDGenerationRequest request) {
        StringBuilder controllerClass = new StringBuilder();

        controllerClass.append("package ").append(request.getPackageName()).append(".web.endpoint")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(";\n\n");

        generateImports(controllerClass, request);

        controllerClass.append("@RestController\n");
        controllerClass.append("@RequestMapping(\"/v1/").append(
                request.getEntityName().toLowerCase()).append("s\")\n");
        controllerClass.append("@Tag(name = \"").append(
                request.getEntityName()).append("Management\")\n");
        controllerClass.append("@SecurityRequirement(name = \"bearerAuth\")\n");
        controllerClass.append("@RequiredArgsConstructor\n");
        controllerClass.append("public class ").append(
                request.getEntityName()).append("Endpoint {\n\n");

        controllerClass.append("    private final ").append(request.getEntityName()).append("Service ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName())).append("Service;\n\n");
        if (request.isGenerateImportExport()) {
            controllerClass.append("    private final CsvImportExportService csvImportExportService;\n\n");
        }
        generateGetMethod(controllerClass, request);
        if (request.isGenerateImportExport()) {
            generateExtractMethod(controllerClass, request);
            generateUploadMethod(controllerClass, request);
        }
        generateGetByIdMethod(controllerClass, request);
        generateCreateMethod(controllerClass, request);
        generateUpdateMethod(controllerClass, request);
        generatePatchMethod(controllerClass, request);
        generateDeleteByIdMethod(controllerClass, request);
        
        controllerClass.append("}");

        // Write request class to file
        String filePath = request.getDirectory() + File.separator + "web" + File.separator + "endpoint"
                + (StringUtils.isNotBlank(request.getSubPackageName()) ? File.separator + request.getSubPackageName() : "")
                + File.separator + request.getEntityName() + "Endpoint.java";
        FileUtils.writeFile(controllerClass.toString(), filePath);

    }

    private void generateGetMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @GetMapping\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_READ_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<Page<").append(request.getEntityName()).append(">> get(\n");
        controllerClass.append("            @RequestParam(defaultValue = \"\") String rsql,\n");
        controllerClass.append("            @Positive @RequestParam(defaultValue = \"1\") Integer pageNo,\n");
        controllerClass.append("            @Positive @RequestParam(defaultValue = \"10\") Integer pageSize,\n");
        controllerClass.append("            @RequestParam(defaultValue = \"id\") String sortBy,\n");
        controllerClass.append("            @RequestParam(defaultValue = \"asc\") String sortDirection) {\n");
        controllerClass.append("        Page<").append(request.getEntityName()).append("> result = ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.get(\n");
        controllerClass.append("            rsql, pageNo - 1, pageSize, sortBy, sortDirection);\n");
        controllerClass.append("        return ResponseEntity.ok(result);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateExtractMethod(StringBuilder controllerClass,
                                       CRUDGenerationRequest request) {
        controllerClass.append("    @GetMapping(\"/extract\")\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_READ_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<Resource> extract(\n");
        controllerClass.append("            @RequestParam(defaultValue = \"\") String rsql,\n");
        controllerClass.append("            @Positive @RequestParam(defaultValue = \"1\") Integer pageNo,\n");
        controllerClass.append("            @Positive @RequestParam(defaultValue = \"10\") Integer pageSize,\n");
        controllerClass.append("            @RequestParam(defaultValue = \"id\") String sortBy,\n");
        controllerClass.append("            @RequestParam(defaultValue = \"asc\") String sortDirection)\n");
        controllerClass.append("            throws IOException {\n");
        controllerClass.append("        Page<").append(request.getEntityName()).append("> result = ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.get(\n");
        controllerClass.append("            rsql, pageNo - 1, pageSize, sortBy, sortDirection);\n");
        controllerClass.append("        List<String> excludeColumns = Arrays.asList(new String[] {\n");
        controllerClass.append("                \"createdAt\", \"createdBy\", \"updatedAt\", \"updatedBy\" });\n");
        controllerClass.append("        Resource resource = csvImportExportService.generateCsvFile(result.stream().toList(),\n");
        controllerClass.append("                excludeColumns);\n");
        controllerClass.append("        return ResponseEntity.ok()\n");
        controllerClass.append("                .contentType(MediaType.parseMediaType(\"application/octet-stream\"))\n");
        controllerClass.append("                .header(HttpHeaders.CONTENT_DISPOSITION, \"attachment; \" +\n");
        controllerClass.append("                        \"filename=\\\"")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("_\" + LocalDateTime.now() + \".csv\\\")\")\n");
        controllerClass.append("                .body(resource);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateUploadMethod(StringBuilder controllerClass,
                                      CRUDGenerationRequest request) {
        controllerClass.append("    @PostMapping(\"/upload\")\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public void uploadFile(@RequestParam(\"file\") MultipartFile file) {\n");
        controllerClass.append("        ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.upload(file);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateGetByIdMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @GetMapping(\"/{id}\")\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_READ_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<").append(request.getEntityName())
                .append("> getById(@PathVariable(\"id\")\n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") ")
                .append("String id) {\n");
        controllerClass.append("        ").append(request.getEntityName()).append(" response = ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.getById(id);\n");
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
                .append("','ROLE_SUPER_ADMIN')\")\n");
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
                .append("','ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<").append(request.getEntityName())
                .append("> update(@PathVariable(\"id\")\n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") ")
                .append("String id,\n");
        controllerClass.append("            @Valid @RequestBody ")
                .append(request.getEntityName()).append(" request) {\n");
        controllerClass.append("        ").append(request.getEntityName()).append(" response = ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.update(id, request);\n");
        controllerClass.append("        return new ResponseEntity<>(response, HttpStatus.OK);\n");
        controllerClass.append("    }\n\n");
    }

    private void generatePatchMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @PatchMapping(\"/{id}\")\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<").append(request.getEntityName())
                .append("> patch(@PathVariable(\"id\")\n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") ")
                .append("String id,\n");
        controllerClass.append("            @RequestBody Map<String, Object> request) {\n");
        controllerClass.append("        ").append(request.getEntityName()).append(" response = ")
                .append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.patch(id, request);\n");
        controllerClass.append("        return new ResponseEntity<>(response, HttpStatus.OK);\n");
        controllerClass.append("    }\n\n");
    }


    private void generateDeleteByIdMethod(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("    @DeleteMapping(\"/{id}\")\n");
        controllerClass.append("    @ResponseStatus(HttpStatus.NO_CONTENT)\n");
        controllerClass.append("    @PreAuthorize(\"hasAnyAuthority('ROLE_MANAGE_")
                .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()))
                .append("','ROLE_SUPER_ADMIN')\")\n");
        controllerClass.append("    public ResponseEntity<Void> deleteById(@PathVariable(\"id\") \n");
        controllerClass.append("            @NotBlank(message = \"id is mandatory\") String id) {\n");
        controllerClass.append("        ").append(CrudStringUtils.lowerCaseFirstLetter(request.getEntityName()))
                .append("Service.deleteById(id);\n");
        controllerClass.append("        return new ResponseEntity<>(HttpStatus.NO_CONTENT);\n");
        controllerClass.append("    }\n\n");
    }

    private void generateImports(StringBuilder controllerClass, CRUDGenerationRequest request) {
        controllerClass.append("import ").append(request.getPackageName()).append(".domain")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(".")
                .append(request.getEntityName()).append(";\n");
        controllerClass.append("import ").append(request.getPackageName()).append(".service")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(".")
                .append(request.getEntityName()).append("Service;\n");
        if (request.isGenerateImportExport()) {
            controllerClass.append("import ").append(request.getCommonPackageName())
                    .append(".csv.CsvImportExportService;\n");
            controllerClass.append("import org.springframework.core.io.Resource;\n");
            controllerClass.append("import org.springframework.http.MediaType;\n");
            controllerClass.append("import org.springframework.web.multipart.MultipartFile;\n");
            controllerClass.append("import java.io.IOException;\n");
            controllerClass.append("import java.time.LocalDateTime;\n");
            controllerClass.append("import java.util.Arrays;\n");
            controllerClass.append("import java.util.List;\n");
        }
        controllerClass.append("import io.swagger.v3.oas.annotations.security.SecurityRequirement;\n");
        controllerClass.append("import io.swagger.v3.oas.annotations.tags.Tag;\n");
        controllerClass.append("import jakarta.validation.Valid;\n");
        controllerClass.append("import jakarta.validation.constraints.NotBlank;\n");
        controllerClass.append("import jakarta.validation.constraints.Positive;\n");
        controllerClass.append("import lombok.RequiredArgsConstructor;\n");
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
