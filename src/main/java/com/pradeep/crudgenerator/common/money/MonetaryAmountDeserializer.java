package com.pradeep.crudgenerator.common.money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.math.BigDecimal;

public class MonetaryAmountDeserializer extends JsonDeserializer<MonetaryAmount> {
    @Override
    public MonetaryAmount deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        String currencyCode = node.get("currency").asText();
        BigDecimal amount = new BigDecimal(node.get("amount").asText());

        CurrencyUnit currency = Monetary.getCurrency(currencyCode);
        return Money.of(amount, currency);
    }
}


