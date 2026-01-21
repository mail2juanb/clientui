package com.clientui.clientui.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * Configuration class for custom date formatting in the MicroDiab application.
 * Provides a bean for formatting and parsing {@link LocalDate} objects in ISO format
 * (e.g., "2023-12-26") to ensure consistency with Thymeleaf templates.
 *
 * <p>This class is annotated with {@link Configuration @Configuration} to indicate
 * that it contains Spring bean definitions.</p>
 */
@Configuration
public class LocalDateFormatterCustom {


    /**
     * Creates a {@link Formatter} bean for {@link LocalDate} objects.
     * Ensures dates are parsed and printed in ISO format (yyyy-MM-dd) to avoid
     * ambiguity and ensure compatibility with Thymeleaf.
     *
     * @return A {@link Formatter} instance for {@link LocalDate}.
     */
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
