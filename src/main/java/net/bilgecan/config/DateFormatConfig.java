package net.bilgecan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class DateFormatConfig {

    @Value("${bilgecan.runDateFormat:yyyy-MM-dd HH:mm:ss}")
    private String runDateFormat;

    @Bean
    public DateTimeFormatter runDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(runDateFormat);
    }
}
