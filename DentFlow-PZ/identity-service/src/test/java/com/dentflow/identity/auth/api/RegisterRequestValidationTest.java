package com.dentflow.identity.auth.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldHaveViolationsWhenEmailIsFake() {
        // Given
        RegisterRequest request = new RegisterRequest("fake-email", "password123");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have validation errors for fake email");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldNotHaveViolationsWhenEmailIsValid() {
        // Given
        RegisterRequest request = new RegisterRequest("valid@example.com", "password123");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty(), "Should not have validation errors for valid email");
    }
}
