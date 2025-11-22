package com.clientui.clientui.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
public class LocalDateFormatterCustom {
    // Class de configuration permettant de garantir le bon format de date pour interpretation par thymeleaf.
    // Concretement eviter d'avoir 26/12/2023 mais plutot 2023-12-26

    @Bean
    public Formatter<LocalDate> localDateFormatter() {
        return new Formatter<LocalDate>() {
            @Override
            public LocalDate parse(String text, Locale locale) throws ParseException {
                return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            }

            @Override
            public String print(LocalDate object, Locale locale) {
                return object.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        };
    }
}
