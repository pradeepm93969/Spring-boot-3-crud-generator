package com.pradeep.crudgenerator.datamodal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityProperties {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z_$][a-zA-Z0-9_$]*$")
    private String name;

    @Pattern(regexp = "List|Map")
    private String ParentType;

    @NotBlank
    @Pattern(regexp = "String|Integer|Long|BigDecimal|MonetaryAmount|Boolean|Enum|OffsetDateTime|Phone")
    private String Type;

    private String enumValues;

    @Min(1)
    @Max(65535)
    private int columnLength = 255;

    private boolean id;
    private boolean required;
    private boolean unique;
    private boolean indexable;

    //Validations
    private boolean email;
    private String pattern;

    private int min;
    private int max;
    private int precision = 19;
    private int scale = 5;
    private boolean positive;
    private boolean positiveOrZero;
    private boolean negative;
    private boolean negativeOrZero;

    private boolean future;
    private boolean futureOrPresent;
    private boolean past;
    private boolean pastOrPresent;
}
