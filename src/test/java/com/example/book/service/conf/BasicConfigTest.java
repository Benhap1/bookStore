package com.example.book.service.conf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicConfigTest {
    @Test
    @DisplayName("Annotation [Configuration] exist for BaseConfig class")
    public void testsConfigAnnotation() throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.example.book.service.conf.BaseConfig");

        assertTrue(aClass.isAnnotationPresent(Configuration.class),
                String.format("Class [%s]. [@Configuration] is missed.", aClass.getSimpleName()));
    }
}
