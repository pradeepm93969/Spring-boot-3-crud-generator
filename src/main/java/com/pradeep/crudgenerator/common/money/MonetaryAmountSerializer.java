package com.pradeep.crudgenerator.common.money;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.money.MonetaryAmount;
import java.io.IOException;
import java.math.BigDecimal;

public class MonetaryAmountSerializer extends JsonSerializer<MonetaryAmount> {
    @Override
    public void serialize(MonetaryAmount value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("currency", value.getCurrency().getCurrencyCode());
        gen.writeStringField("amount", value.getNumber().numberValue(BigDecimal.class).toPlainString());
        gen.writeEndObject();
    }
}

