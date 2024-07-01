package com.pradeep.crudgenerator.common.money;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.money.CurrencyUnit;
import java.io.IOException;

public class CurrencyUnitSerializer extends JsonSerializer<CurrencyUnit> {
    @Override
    public void serialize(CurrencyUnit value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getCurrencyCode());
    }
}