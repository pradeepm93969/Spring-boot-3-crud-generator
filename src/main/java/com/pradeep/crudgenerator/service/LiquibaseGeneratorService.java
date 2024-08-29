package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.datamodal.EntityProperties;
import com.pradeep.crudgenerator.utils.CrudStringUtils;
import com.pradeep.crudgenerator.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LiquibaseGeneratorService {

    public void generateLiquibase(CRUDGenerationRequest request) {
        StringBuilder sqlFile = new StringBuilder();

        sqlFile.append("-- liquibase formatted sql\n");
        sqlFile.append("-- changeset ").append(request.getLiquibaseAuditorName())
                .append(":").append("create-").append(request.getEntityName().toLowerCase()).append("-table")
                .append("\n\n");

        sqlFile.append("CREATE TABLE ")
                .append(request.getDatabaseSchema()).append(".")
                .append(CrudStringUtils.convertCamelToSnake(request.getTablePrefix()
                        + request.getEntityName()))
                .append(" (\n");

        for (EntityProperties property : request.getProperties()) {
            sqlFile.append(generateField(property, request));
        }
        if (request.getProperties().stream().anyMatch(p -> p.getType().equalsIgnoreCase("MonetaryAmount"))) {
            sqlFile.append("    CURRENCY VARCHAR(3) NOT NULL,\n");
        }
        if (request.isGenerateAuditSection()) {
            sqlFile.append("    CREATED_AT TIMESTAMP NULL,\n");
            sqlFile.append("    CREATED_BY VARCHAR(60) NULL,\n");
            sqlFile.append("    UPDATED_AT TIMESTAMP NULL,\n");
            sqlFile.append("    UPDATED_BY VARCHAR(60) NULL,\n");
        }

        request.getProperties().stream()
                .filter(EntityProperties::isId)
                .findFirst()
                .ifPresent(e -> sqlFile
                        .append("    CONSTRAINT ").append(CrudStringUtils.convertCamelToSnake(
                                request.getTablePrefix()
                                        + request.getEntityName()))
                        .append("_pkey PRIMARY KEY (")
                        .append(CrudStringUtils.convertCamelToSnake(e.getName()))
                        .append(")\n"));

        sqlFile.append(");\n\n");

        // Generate index creation SQL
        generateIndexes(sqlFile, request);

        // Write request class to file
        String filePath = request.getDirectory() + "\\jpa"
                + (StringUtils.isNotBlank(request.getSubPackageName()) ? "\\"
                + request.getSubPackageName() : "")
                + "\\create-" + request.getEntityName().toLowerCase() + "-table.sql";
        FileUtils.writeFile(sqlFile.toString(), filePath);

    }

    private void generateIndexes(StringBuilder sqlFile, CRUDGenerationRequest request) {
        for (EntityProperties property : request.getProperties()) {
            if (property.isUnique()) {
                sqlFile.append("CREATE UNIQUE INDEX idx_")
                        .append(CrudStringUtils.convertCamelToSnake(request.getTablePrefix()
                                + request.getEntityName()))
                        .append("_").append(CrudStringUtils.convertCamelToSnake(property.getName()))
                        .append(" ON ")
                        .append(request.getDatabaseSchema())
                        .append(".")
                        .append(CrudStringUtils.convertCamelToSnake(request.getTablePrefix()
                                + request.getEntityName()))
                        .append(" (")
                        .append(CrudStringUtils.convertCamelToSnake(property.getName()))
                        .append(");\n");
            } else if (property.isIndexable()) {
                sqlFile.append("CREATE INDEX idx_")
                        .append(CrudStringUtils.convertCamelToSnake(request.getTablePrefix()
                                + request.getEntityName()))
                        .append("_").append(CrudStringUtils.convertCamelToSnake(property.getName()))
                        .append(" ON ")
                        .append(request.getDatabaseSchema())
                        .append(".")
                        .append(CrudStringUtils.convertCamelToSnake(request.getTablePrefix()
                                + request.getEntityName()))
                        .append(" (")
                        .append(CrudStringUtils.convertCamelToSnake(property.getName()))
                        .append(");\n");
            }
        }
    }

    private String generateField(EntityProperties property, CRUDGenerationRequest request) {
        StringBuilder field = new StringBuilder();

        field.append("    ").append(CrudStringUtils.convertCamelToSnake(property.getName())).append(" ");

        switch (property.getType().toLowerCase()) {
            case "string":
            case "enum":
            case "object":
                field.append("VARCHAR(").append(property.getColumnLength()).append(")");
                break;
            case "long":
                field.append("BIGINT");
                break;
            case "integer":
                field.append("INT");
                break;
            case "boolean":
                field.append("BOOLEAN");
                break;
            case "instant":
                field.append("TIMESTAMP");
                break;
            case "bigdecimal":
                field.append("NUMERIC(").append(property.getPrecision()).append(",")
                        .append(property.getScale()).append(")");
                break;
            case "monetaryamount":
                field.append("NUMERIC(19,5)");
                break;
            case "phone":
                field.append("VARCHAR(100)");
                break;
            default:
                field.append("TEXT"); // Default to TEXT for other types
                break;
        }

        if (property.isRequired()) {
            field.append(" NOT NULL");
        }

        if (property.isUnique()) {
            field.append(" UNIQUE");
        }

        return field.append(",\n").toString();
    }

}
