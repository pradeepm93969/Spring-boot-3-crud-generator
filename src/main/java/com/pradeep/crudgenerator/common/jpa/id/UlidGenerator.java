package com.pradeep.crudgenerator.common.jpa.id;

import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.annotations.ValueGenerationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IdGeneratorType(UlidGeneratorImpl.class)
@ValueGenerationType(generatedBy = UlidGeneratorImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface UlidGenerator {
}
