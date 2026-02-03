package com.kerem.phinance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new LocalDateToStringConverter(),
                new StringToLocalDateConverter()
        ));
    }

    static class LocalDateToStringConverter implements Converter<LocalDate, String> {

        @Override
        public String convert(LocalDate source) {
            return source.toString(); // Returns ISO format: YYYY-MM-DD
        }
    }

    static class StringToLocalDateConverter implements Converter<String, LocalDate> {

        @Override
        public LocalDate convert(String source) {
            return LocalDate.parse(source); // Parses ISO format: YYYY-MM-DD
        }
    }
}
