package com.app.common.money;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pradeep.crudgenerator.common.jackson.CurrencyUnitDeserializer;
import com.pradeep.crudgenerator.common.jackson.CurrencyUnitSerializer;
import com.pradeep.crudgenerator.common.jackson.MonetaryAmountDeserializer;
import com.pradeep.crudgenerator.common.jackson.MonetaryAmountSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule javaTimeModule() {
        return new JavaTimeModule();
    }

    @Bean
    public SimpleModule currencyUnitModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(CurrencyUnit.class, new CurrencyUnitSerializer());
        module.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer());
        return module;
    }

    @Bean
    public SimpleModule monetaryModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(MonetaryAmount.class, new MonetaryAmountSerializer());
        module.addDeserializer(MonetaryAmount.class, new MonetaryAmountDeserializer());
        return module;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(monetaryModule());
        mapper.registerModule(currencyUnitModule());
        mapper.registerModule(javaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
