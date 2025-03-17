package com.pradeep.crudgenerator.service;

import com.pradeep.crudgenerator.datamodal.CRUDGenerationRequest;
import com.pradeep.crudgenerator.datamodal.EntityProperties;
import com.pradeep.crudgenerator.utils.CrudStringUtils;
import com.pradeep.crudgenerator.utils.FileUtils;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class DomainGeneratorService {
    public void generateDomain(@Valid CRUDGenerationRequest request) {
        StringBuilder domainClass = new StringBuilder();

        domainClass.append("@Data\n");
        domainClass.append("@NoArgsConstructor\n");
        domainClass.append("@AllArgsConstructor\n");
        domainClass.append("@Builder\n");
        domainClass.append("public class ").append(request.getEntityName()).append(" implements Serializable {\n\n");
        domainClass.append("    private static final long serialVersionUID = 1L;\n\n");

        List<EntityProperties> properties = request.getProperties();

        for (EntityProperties property : properties) {
            domainClass.append(generateField(property, true));
        }
        if (request.getProperties().stream().anyMatch(p -> p.getType().equalsIgnoreCase("MonetaryAmount"))) {
            domainClass.append("    private CurrencyUnit currency = Monetary.getCurrency(\"USD\");\n\n");
        }
        if (request.isGenerateAuditSection()) {
            domainClass.append("    private Instant createdAt;\n");
            domainClass.append("    private Instant updatedAt;\n");
            domainClass.append("    private String createdBy;\n");
            domainClass.append("    private String updatedBy;\n");
        }
        domainClass.append("\n}");

        StringBuilder domainFile = new StringBuilder();
        // Generate package declaration
        domainFile.append("package ").append(request.getPackageName())
                .append(".domain")
                .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                        + request.getSubPackageName() : "")
                .append(";\n\n");
        // Import necessary packages
        generateImportStatements(domainClass.toString(), domainFile, request);

        domainFile.append(domainClass.toString());

        // Write request class to file
        String filePath = request.getDirectory() + File.separator + "domain"
                + (StringUtils.isNotBlank(request.getSubPackageName()) ? File.separator + request.getSubPackageName() : "")
                + File.separator + request.getEntityName() + ".java";
        FileUtils.writeFile(domainFile.toString(), filePath);
    }

    private void generateImportStatements(String content, StringBuilder file, CRUDGenerationRequest request) {
        file.append("import lombok.Data;\n");
        file.append("import lombok.NoArgsConstructor;\n");
        file.append("import lombok.AllArgsConstructor;\n");
        file.append("import lombok.Builder;\n");

        if (content.contains("@NotNull"))
            file.append("import jakarta.validation.constraints.NotNull;\n");
        if (content.contains("@Size"))
            file.append("import jakarta.validation.constraints.Size;\n");
        if (content.contains("@Email"))
            file.append("import jakarta.validation.constraints.Email;\n");
        if (content.contains("@Pattern"))
            file.append("import jakarta.validation.constraints.Pattern;\n");
        if (content.contains("@Min"))
            file.append("import jakarta.validation.constraints.Min;\n");
        if (content.contains("@Max"))
            file.append("import jakarta.validation.constraints.Max;\n");
        if (content.contains("@Digits"))
            file.append("import jakarta.validation.constraints.Digits;\n");
        if (content.contains("@Positive"))
            file.append("import jakarta.validation.constraints.Positive;\n");
        if (content.contains("@PositiveOrZero"))
            file.append("import jakarta.validation.constraints.PositiveOrZero;\n");
        if (content.contains("@Negative"))
            file.append("import jakarta.validation.constraints.Negative;\n");
        if (content.contains("@NegativeOrZero"))
            file.append("import jakarta.validation.constraints.NegativeOrZero;\n");
        if (content.contains("@Future"))
            file.append("import jakarta.validation.constraints.Future;\n");
        if (content.contains("@FutureOrPresent"))
            file.append("import jakarta.validation.constraints.FutureOrPresent;\n");
        if (content.contains("@Past"))
            file.append("import jakarta.validation.constraints.Past;\n");
        if (content.contains("@PastOrPresent"))
            file.append("import jakarta.validation.constraints.PastOrPresent;\n");

        if (content.contains(" MonetaryAmount "))
            file.append("import javax.money.MonetaryAmount;\n");
        file.append("import java.io.Serializable;\n");
        if (content.contains(" Set<"))
            file.append("import java.util.Set;\n");
        if (content.contains(" List<"))
            file.append("import java.util.List;\n");
        if (content.contains(" BigDecimal"))
            file.append("import java.math.BigDecimal;\n");
        if (content.contains(" Map<"))
            file.append("import java.util.Map;\n");
        if (content.contains(" Instant"))
            file.append("import java.time.Instant;\n");
        if (content.contains("@JsonProperty"))
            file.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        if (content.contains("Phone"))
            file.append("import ").append(request.getCommonPackageName()).append(".jpa.domain.Phone;\n");
        if (content.contains(" CurrencyUnit"))
            file.append("import javax.money.CurrencyUnit;\n");
        if (content.contains(" Monetary"))
            file.append("import javax.money.Monetary;\n");

        request.getProperties().stream().filter(p -> p.getType().equalsIgnoreCase("Enum")).forEach(
                fp -> {
                    file.append("import ").append(request.getPackageName()).append(".domain")
                            .append(StringUtils.isNotBlank(request.getSubPackageName()) ? "."
                                    + request.getSubPackageName() : "")
                            .append(".support.")
                            .append(CrudStringUtils.capitalizeFirstLetter(fp.getName())).append("Enum;\n");
                }
        );
        file.append("\n");
    }

    private String generateField(EntityProperties property, boolean addValidation) {
        StringBuilder field = new StringBuilder();

        if (addValidation)
            addValidationAnnotations(property, field);

        if (property.getType().equalsIgnoreCase("Boolean"))
            field.append("    @JsonProperty(\"is")
                    .append(CrudStringUtils.capitalizeFirstLetter(property.getName()))
                    .append("\")\n");

        field.append("    private ");
        if (StringUtils.isBlank(property.getParentType())) {
            if (property.getType().equalsIgnoreCase("Enum")) {
                field.append(CrudStringUtils.capitalizeFirstLetter(property.getName()) + "Enum").append(" ");
            } else {
                field.append(property.getType()).append(" ");
            }
        } else {
            field.append(property.getParentType()).append("<");
            if (StringUtils.equals(property.getParentType(), "Map")) {
                field.append("String").append(", ");
            }
            if (property.getType().equalsIgnoreCase("Enum")) {
                field.append(CrudStringUtils.capitalizeFirstLetter(property.getName()) + "Enum").append("> ");
            } else {
                field.append(property.getType()).append("> ");
            }
        }
        field.append(property.getName()).append(";\n\n");

        return field.toString();
    }

    private void addValidationAnnotations(EntityProperties property, StringBuilder field) {
        if (property.isRequired()) {
            field.append("    @NotNull\n");
        }
        if (property.getType().equalsIgnoreCase("String")
                || StringUtils.isNotBlank(property.getParentType())) {
            if (property.getMin() > 0 || property.getMax() > 0) {
                field.append("    @Size(");
                if (property.getMin() > 0) {
                    field.append("min = ").append(property.getMin());
                    if (property.getMax() > 0) {
                        field.append(", ");
                    }
                }
                if (property.getMax() > 0) {
                    field.append("max = ").append(property.getMax());
                }
                field.append(")\n");
            }
            if (property.isEmail()) {
                field.append("    @Email\n");
            }
            if (StringUtils.isNotBlank(property.getPattern())) {
                field.append("    @Pattern(regexp = \"").append(property.getPattern()).append("\")\n");
            }
        } else if (property.getType().equalsIgnoreCase("Integer")
                || property.getType().equalsIgnoreCase("BigDecimal")) {

            if (property.getMin() > 0) {
                field.append("    @Min(").append(property.getMin()).append(")\n");
            }
            if (property.getMax() > 0) {
                field.append("    @Max(").append(property.getMax()).append(")\n");
            }
            if (property.getPrecision() > 0) {
                field.append("    @Digits(integer = ").append(property.getPrecision());
                if (property.getScale() > 0) {
                    field.append(", fraction = ").append(property.getScale());
                }
                field.append(")\n");
            }
            if (property.isPositive()) {
                field.append("    @Positive\n");
            }
            if (property.isPositiveOrZero()) {
                field.append("    @PositiveOrZero\n");
            }
            if (property.isNegative()) {
                field.append("    @Negative\n");
            }
            if (property.isNegativeOrZero()) {
                field.append("    @NegativeOrZero\n");
            }

        }  else if (property.getType().equalsIgnoreCase("Instant")) {
            if (property.isFuture()) {
                field.append("    @Future\n");
            }
            if (property.isFutureOrPresent()) {
                field.append("    @FutureOrPresent\n");
            }
            if (property.isPast()) {
                field.append("    @Past\n");
            }
            if (property.isPastOrPresent()) {
                field.append("    @PastOrPresent\n");
            }
        }
    }
}
