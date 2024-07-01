package com.pradeep.crudgenerator.common.money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.io.IOException;

public class CurrencyUnitDeserializer extends JsonDeserializer<CurrencyUnit> {
    @Override
    public CurrencyUnit deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String currencyCode = p.getText();
        return Monetary.getCurrency(currencyCode);
    }
}

