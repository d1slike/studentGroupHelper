package ru.disdev.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.StringTokenizer;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        StringTokenizer tokenizer = new StringTokenizer(p.readValueAs(String.class), ".");
        int day = Integer.parseInt(tokenizer.nextToken());
        int mouth = Integer.parseInt(tokenizer.nextToken());
        return LocalDate.of(2016, mouth, day);
    }
}
