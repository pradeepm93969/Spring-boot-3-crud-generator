package com.pradeep.crudgenerator.common.jpa.domain;

import jakarta.validation.Payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Phone implements Payload, Serializable {

    private static final long serialVersionUID = 1L;

    private String countryCode;

    private String phoneNumber;

    private String extension;
}
