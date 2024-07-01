package com.pradeep.crudgenerator.common.money;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.javamoney.moneta.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

@Configuration
public class JacksonConfig {
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
        return mapper;
    }
}
