package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.datamodal.EntityProperties;
import com.pradeep.crudgenerator.utils.CrudStringUtils;
import com.pradeep.crudgenerator.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntityGeneratorService {

    public void generateEntity(CRUDGenerationRequest request) {
        StringBuilder entityClass = new StringBuilder();

        // Generate entity class declaration
        entityClass.append("@Entity\n");
        entityClass.append("@Data\n");
        if (request.isGenerateAuditSection()) {
            entityClass.append("@EntityListeners(AuditingEntityListener.class)\n");
        }
        generateTableAnnotation(request, entityClass);
        entityClass.append("public class Jpa").append(
                request.getEntityName()).append(" implements Serializable {\n\n");

        entityClass.append("    private static final long serialVersionUID = 1L;\n\n");

        // Generate fields
        for (EntityProperties property : request.getProperties()) {
            entityClass.append(generateField(property, request));
        }

        //Add Currency Field
        if (request.getProperties().stream().anyMatch(p -> p.getType().equalsIgnoreCase("MonetaryAmount"))) {
            entityClass.append("    @Column(name = \"CURRENCY\", length = 3)\n");
            entityClass.append("    private String currency;\n\n");
        }

        if (request.isGenerateAuditSection()) {
            entityClass.append("    @CreatedDate\n");
            entityClass.append("    @Column(name = \"CREATED_AT\", columnDefinition = \"TIMESTAMP\", nullable = false, updatable = false)\n");
            entityClass.append("    private Instant createdAt;\n\n");

            entityClass.append("    @LastModifiedDate\n");
            entityClass.append("    @Column(name = \"UPDATED_AT\", columnDefinition = \"TIMESTAMP\")\n");
            entityClass.append("    private Instant updatedAt;\n\n");

            entityClass.append("    @CreatedBy\n");
            entityClass.append("    @Column(name = \"CREATED_BY\", nullable = false, updatable = false)\n");
            entityClass.append("    private String createdBy;\n\n");

            entityClass.append("    @LastModifiedBy\n");
            entityClass.append("    @Column(name = \"UPDATED_BY\", nullable = false)\n");
            entityClass.append("    private String updatedBy;\n\n");
        }

        generateFromMeMethod(entityClass, request);
        generateToMeMethod(entityClass, request);

        // Close class
        entityClass.append("\n}");

        StringBuilder entityFile = new StringBuilder();
        // Generate package declaration
        entityFile.append("package ").append(request.getPackageName()).append(".jpa.domain")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(";\n\n");
        // Import necessary packages
        generateImportStatements(entityClass.toString(), entityFile, request);

        entityFile.append(entityClass.toString());

        // Write entity class to file
        String filePath = request.getDirectory() + "\\jpa\\domain"
                + (StringUtils.isNotBlank(request.getSubPackageName()) ? "\\"
                + request.getSubPackageName() : "")
                + "\\Jpa" + request.getEntityName() + ".java";
        FileUtils.writeFile(entityFile.toString(), filePath);
    }

    private void generateToMeMethod(StringBuilder entityClass, CRUDGenerationRequest request) {
        String entityName = request.getEntityName();
        entityClass.append("    public void toMe(")
                .append(entityName)
                .append(" domain) {\n");

        entityClass.append("        if (domain == null) {\n");
        entityClass.append("            return;\n");
        entityClass.append("        }\n");

        if (request.getProperties().stream().anyMatch(p -> p.getType().equalsIgnoreCase("MonetaryAmount"))) {
            entityClass.append("        this.setCurrency(domain.getCurrency().getCurrencyCode());\n");
        }

        // Assuming there are getter methods for request fields and setter methods for entity fields
        request.getProperties().forEach(property -> {
            String propertyName = CrudStringUtils.capitalizeFirstLetter(property.getName());
            if (property.getType().equalsIgnoreCase("MonetaryAmount")) {
                entityClass.append("        if (domain.get").append(propertyName).append("() != null) {\n");
                entityClass.append("            this.set").append(propertyName)
                        .append("(domain.get").append(propertyName).append("().getNumber().numberValue(BigDecimal.class));\n");
                entityClass.append("        }\n");
            } else if (StringUtils.isNotBlank(property.getParentType())
                    && property.getType().equalsIgnoreCase("Enum")) {

                String enumClassName = CrudStringUtils.capitalizeFirstLetter(property.getName()) + "Enum";

                if (property.getParentType().equals("Set")) {
                    entityClass.append("        if (domain.get").append(propertyName).append("() != null && !domain.get")
                            .append(propertyName).append("().isEmpty()) {\n");
                    entityClass.append("            this.set").append(propertyName)
                            .append("(domain.get").append(propertyName).append("().stream()")
                            .append(".map(\n                ").append(enumClassName).append("::name)")
                            .append(".collect(Collectors.toSet()));\n");
                    entityClass.append("        }\n");
                } else if (property.getParentType().equals("List")) {
                    entityClass.append("        if (domain.get").append(propertyName).append("() != null && !domain.get")
                            .append(propertyName).append("().isEmpty()) {\n");
                    entityClass.append("            this.set").append(propertyName)
                            .append("(domain.get").append(propertyName).append("().stream()")
                            .append(".map(\n                ").append(enumClassName).append("::name)")
                            .append(".collect(Collectors.toList()));\n");
                    entityClass.append("        }\n");
                } else if (property.getParentType().equals("Map")) {
                    entityClass.append("        if (domain.get").append(propertyName).append("() != null && !domain.get")
                            .append(propertyName).append("().isEmpty()) {\n");
                    entityClass.append("            this.set").append(propertyName)
                            .append("(domain.get").append(propertyName).append("().entrySet().stream()")
                            .append(".collect(\n                Collectors.toMap(Map.Entry::getKey, e -> e.getValue().name())));\n");
                    entityClass.append("        }\n");
                }
            } else {
                entityClass.append("        this.set").append(propertyName).append("(domain.get")
                        .append(propertyName).append("());\n");
            }
        });
        entityClass.append("    }\n\n");
    }

    private void generateFromMeMethod(StringBuilder entityClass, CRUDGenerationRequest request) {
        String entityName = request.getEntityName();
        entityClass.append("    public ").append(entityName).append(" fromMe() {\n");
        entityClass.append("        ").append(entityName).append(" domain = new ")
                .append(entityName).append("();\n");

        if (request.getProperties().stream().anyMatch(p -> p.getType().equalsIgnoreCase("MonetaryAmount"))) {
            entityClass.append("        domain.setCurrency(Monetary.getCurrency(this.getCurrency()));\n");
        }

        // Assuming there are getter methods for entity fields and setter methods for response fields
        request.getProperties().forEach(property -> {
            String propertyName = CrudStringUtils.capitalizeFirstLetter(property.getName());
            if (property.getType().equalsIgnoreCase("MonetaryAmount")) {
                entityClass.append("        if (this.get").append(propertyName).append("() != null) {\n");
                entityClass.append("            domain.set").append(propertyName)
                        .append("(Money.of(this.get").append(propertyName).append(
                                "(), domain.getCurrency()));\n");
                entityClass.append("        }\n");
            } else if (StringUtils.isNotBlank(property.getParentType())
                    && property.getType().equalsIgnoreCase("Enum")) {

                String enumClassName = CrudStringUtils.capitalizeFirstLetter(property.getName()) + "Enum";

                if (property.getParentType().equals("Set")) {
                    entityClass.append("        if (this.get").append(propertyName).append("() != null && !this.get")
                            .append(propertyName).append("().isEmpty()) {\n");
                    entityClass.append("            domain.set").append(propertyName)
                            .append("(this.get").append(propertyName).append("().stream()")
                            .append(".map(\n                s -> ").append(enumClassName).append(".valueOf(s))")
                            .append(".collect(Collectors.toSet()));\n");
                    entityClass.append("        }\n");
                } else if (property.getParentType().equals("List")) {
                    entityClass.append("        if (this.get").append(propertyName).append("() != null && !this.get")
                            .append(propertyName).append("().isEmpty()) {\n");
                    entityClass.append("            domain.set").append(propertyName)
                            .append("(this.get").append(propertyName).append("().stream()")
                            .append(".map(\n                s -> ").append(enumClassName).append(".valueOf(s))")
                            .append(".collect(Collectors.toList()));\n");
                    entityClass.append("        }\n");
                } else if (property.getParentType().equals("Map")) {
                    entityClass.append("        if (this.get").append(propertyName).append("() != null && !this.get")
                            .append(propertyName).append("().isEmpty()) {\n");
                    entityClass.append("            domain.set").append(propertyName)
                            .append("(this.get").append(propertyName).append("().entrySet().stream()")
                            .append(".collect(\n                Collectors.toMap(Map.Entry::getKey, e -> ").append(enumClassName)
                            .append(".valueOf(e.getValue()))));\n");
                    entityClass.append("        }\n");
                }
            } else {
                entityClass.append("        domain.set").append(propertyName).append("(this.get")
                        .append(propertyName).append("());\n");
            }
        });
        if (request.isGenerateAuditSection()) {
            entityClass.append("        domain.setCreatedAt(this.getCreatedAt());\n");
            entityClass.append("        domain.setCreatedBy(this.getCreatedBy());\n");
            entityClass.append("        domain.setUpdatedAt(this.getUpdatedAt());\n");
            entityClass.append("        domain.setUpdatedBy(this.getUpdatedBy());\n");
        }
        entityClass.append("        return domain;\n");
        entityClass.append("    }\n\n");
    }

    private void generateTableAnnotation(CRUDGenerationRequest request, StringBuilder entityClass) {

        List<EntityProperties> uniqueProperties = request.getProperties().stream().filter(p -> p.isUnique())
                .collect(Collectors.toList());
        List<EntityProperties> indexableProperties = request.getProperties().stream().filter(p -> p.isIndexable())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(uniqueProperties) && CollectionUtils.isEmpty(indexableProperties)) {
            entityClass.append("@Table(name = \"").append(CrudStringUtils.convertCamelToSnake(
                    request.getTablePrefix() + request.getEntityName())).append("\")\n");
        } else {
            entityClass.append("@Table(name = \"").append(CrudStringUtils.convertCamelToSnake(
                    request.getTablePrefix() + request.getEntityName())).append("\"");
            if (!CollectionUtils.isEmpty(uniqueProperties)) {
                entityClass.append(",\n");
                entityClass.append("    uniqueConstraints = {\n");
                final int[] index = {0};
                uniqueProperties.stream().forEach(p -> {
                    entityClass.append("         @UniqueConstraint(name = \"UK_")
                            .append(CrudStringUtils.convertCamelToSnake(request.getEntityName()
                                    + CrudStringUtils.capitalizeFirstLetter(p.getName())))
                            .append("\", columnNames = {\"")
                            .append(CrudStringUtils.convertCamelToSnake(p.getName()) + "\"})");
                    if (++index[0] < uniqueProperties.size()) { // Check if not the last element
                        entityClass.append(","); // Append comma if not the last element
                    }
                    entityClass.append("\n");
                });
                entityClass.append("    }");
            }
            if (!CollectionUtils.isEmpty(indexableProperties)) {
                entityClass.append(",\n");
                entityClass.append("    indexes = {\n");
                final int[] index = {0};
                indexableProperties.stream().forEach(p -> {
                    entityClass.append("        @Index(columnList = \"" +
                            CrudStringUtils.convertCamelToSnake(p.getName()) + "\")");
                    if (++index[0] < indexableProperties.size()) { // Check if not the last element
                        entityClass.append(","); // Append comma if not the last element
                    }
                    entityClass.append("\n");
                });
                entityClass.append("    }");
            }
            entityClass.append("\n)\n");
        }
    }

    private void generateImportStatements(String content, StringBuilder entityFile, CRUDGenerationRequest request) {

        entityFile.append("import ").append(request.getJpaPackageName()).append(".id.UlidGenerator;\n");
        entityFile.append("import lombok.Data;\n");
        entityFile.append("import jakarta.persistence.Column;\n");
        entityFile.append("import ").append(request.getPackageName()).append(".domain.").append(
                request.getEntityName()).append(";\n");
        if (content.contains("@Convert"))
            entityFile.append("import jakarta.persistence.Convert;\n");
        entityFile.append("import jakarta.persistence.Entity;\n");
        if (content.contains("@EntityListeners"))
            entityFile.append("import jakarta.persistence.EntityListeners;\n");
        entityFile.append("import jakarta.persistence.Id;\n");
        entityFile.append("import jakarta.persistence.Table;\n");
        if (content.contains("@UniqueConstraint"))
            entityFile.append("import jakarta.persistence.UniqueConstraint;\n");
        if (content.contains("@Index"))
            entityFile.append("import jakarta.persistence.Index;\n");
        if (content.contains("@GeneratedValue"))
            entityFile.append("import jakarta.persistence.GeneratedValue;\n");
        if (content.contains("@JdbcTypeCode"))
            entityFile.append("import org.hibernate.annotations.JdbcTypeCode;\n");
        if (content.contains("SqlTypes"))
            entityFile.append("import org.hibernate.type.SqlTypes;\n");

        if (request.isGenerateAuditSection()) {
            entityFile.append("import org.springframework.data.jpa.domain.support.AuditingEntityListener;\n");
            entityFile.append("import org.springframework.data.annotation.CreatedBy;\n");
            entityFile.append("import org.springframework.data.annotation.CreatedDate;\n");
            entityFile.append("import org.springframework.data.annotation.LastModifiedBy;\n");
            entityFile.append("import org.springframework.data.annotation.LastModifiedDate;\n");
        }

        if (content.contains(" MonetaryAmount "))
            entityFile.append("import javax.money.MonetaryAmount;\n");
        entityFile.append("import java.io.Serializable;\n");
        if (content.contains(" Set<"))
            entityFile.append("import java.util.Set;\n");
        if (content.contains(" List<"))
            entityFile.append("import java.util.List;\n");
        if (content.contains(" Map<"))
            entityFile.append("import java.util.Map;\n");
        if (content.contains(" Instant"))
            entityFile.append("import java.time.Instant;\n");
        if (content.contains(" BigDecimal"))
            entityFile.append("import java.math.BigDecimal;\n");
        if (content.contains(" CurrencyUnit"))
            entityFile.append("import javax.money.CurrencyUnit;\n");
        if (content.contains("Monetary."))
            entityFile.append("import javax.money.Monetary;\n");
        if (content.contains("Money."))
            entityFile.append("import org.javamoney.moneta.Money;\n");

        if (content.contains("Phone"))
            entityFile.append("import ").append(request.getJpaPackageName()).append(".domain.Phone;\n");

        request.getProperties().stream().filter(p -> p.getType().equalsIgnoreCase("Enum")).forEach(
                fp -> {
                    entityFile.append("import ").append(request.getPackageName()).append(".domain")
                            .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                                    + request.getSubPackageName() : "")
                            .append(".support.")
                            .append(CrudStringUtils.capitalizeFirstLetter(fp.getName())).append("Enum;\n");
                }
        );
        entityFile.append("\n");
    }

    private String generateField(EntityProperties property, CRUDGenerationRequest request) {
        StringBuilder field = new StringBuilder();

        // Add field declaration
        if (property.isId()) {
            field.append("    @Id\n");
            field.append("    @UlidGenerator\n");
            field.append("    @GeneratedValue\n");
        }
        field.append("    @Column(name = \"").append(CrudStringUtils.convertCamelToSnake(property.getName()));
        if (property.getType().equalsIgnoreCase("Instant")) {
            field.append("\", columnDefinition = \"TIMESTAMP\"");
        } else if (property.getType().equalsIgnoreCase("BigDecimal") ||
                property.getType().equalsIgnoreCase("MonetaryAmount")) {
            field.append("\", precision = ").append(property.getPrecision());
            field.append(", scale = ").append(property.getScale());
        } else if (property.getType().equalsIgnoreCase("Integer") ||
                property.getType().equalsIgnoreCase("Long")) {
            field.append("\"");
        } else if (property.getType().equalsIgnoreCase("Boolean")) {
            field.append("\", length = 1");
        } else if (property.getType().equalsIgnoreCase("Phone")) {
            field.append("\", length = 100");
        } else if (property.isId()) {
            field.append("\", length = 60");
        } else {
            field.append("\", length = ").append(property.getColumnLength());
        }

        if (property.isRequired() || property.getType().equalsIgnoreCase("MonetaryAmount")) {
            field.append(", nullable = false");
        }
        if (property.isUnique()) {
            field.append(", unique = true");
        }
        field.append(")\n");

        //Add Enum Constant
        if (property.getType().equalsIgnoreCase("Enum")) {
            generateEnumClass(property, request);
        }

        //JSON Support
        if (property.getType().equalsIgnoreCase("Phone")
                || StringUtils.isNotBlank(property.getParentType())) {
            field.append("    @JdbcTypeCode(SqlTypes.JSON)\n");
        }

        field.append("    private ");
        if (StringUtils.isBlank(property.getParentType())) {
            if (property.getType().equalsIgnoreCase("Enum")) {
                field.append(CrudStringUtils.capitalizeFirstLetter(property.getName()) + "Enum").append(" ");
            } else if (property.getType().equalsIgnoreCase("MonetaryAmount")) {
                field.append("BigDecimal ");
            } else {
                field.append(property.getType()).append(" ");
            }
            field.append(property.getName()).append(";\n\n");
        } else {
            field.append(property.getParentType()).append("<");
            if (StringUtils.equals(property.getParentType(), "Map")) {
                field.append("String").append(", ");
            }
            if (property.getType().equalsIgnoreCase("Enum")) {
                field.append("String").append("> ");
            } else {
                field.append(property.getType()).append("> ");
            }
            field.append(property.getName()).append(";\n\n");
        }
        return field.toString();
    }

    private void generateEnumClass(EntityProperties property, CRUDGenerationRequest request) {
        String filePath = request.getDirectory() + "\\domain"
                + (StringUtils.isNotBlank(request.getSubPackageName()) ? "\\"
                + request.getSubPackageName() : "")
                + "\\support\\" + CrudStringUtils.capitalizeFirstLetter(property.getName()) + "Enum.java";
        StringBuilder enumClassBuilder = new StringBuilder();
        enumClassBuilder.append("package ").append(request.getPackageName()).append(".domain")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(".support;\n\n");
        enumClassBuilder.append("public enum ").append(CrudStringUtils.capitalizeFirstLetter(property.getName()))
                .append("Enum").append(" {\n\n");

        String[] enumValues = property.getEnumValues().split(",");
        for (int i = 0; i < enumValues.length; i++) {
            enumClassBuilder.append("    ").append(enumValues[i].trim().toUpperCase());
            if (i < enumValues.length - 1) {
                enumClassBuilder.append(",");
            }
            enumClassBuilder.append("\n");
        }

        enumClassBuilder.append("}");
        FileUtils.writeFile(enumClassBuilder.toString(), filePath);
    }

}
