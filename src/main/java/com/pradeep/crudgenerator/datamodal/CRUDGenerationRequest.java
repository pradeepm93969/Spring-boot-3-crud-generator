package com.pradeep.crudgenerator.datamodal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CRUDGenerationRequest {

    @NotBlank
    private String directory;

    @NotBlank
    @Pattern(regexp = "^(?:[a-zA-Z_][a-zA-Z0-9_]*\\.)*[a-zA-Z_][a-zA-Z0-9_]*$")
    private String packageName;

    @Pattern(regexp = "^[a-zA-Z_$][a-zA-Z0-9_$]*$")
    private String subPackageName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z_$][a-zA-Z0-9_$]*$")
    private String EntityName;

    @NotBlank
    private String databaseSchema;

    private String tablePrefix;

    private boolean generateLiquibase = true;
    private String liquibaseAuditorName;

    private boolean generateAuditSection = true;

    private boolean generateImportExport = true;

    @Pattern(regexp = "^(?:[a-zA-Z_][a-zA-Z0-9_]*\\.)*[a-zA-Z_][a-zA-Z0-9_]*$")
    private String commonPackageName;

    @NotEmpty
    @Valid
    private List<EntityProperties> properties;

}
