package com.dentflow.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punkt wejścia modułu tożsamości systemu DentFlow.
 *
 * <p>Odpowiada za uwierzytelnianie, zarządzanie użytkownikami
 * i przypisywanie ról w obrębie gabinetów.</p>
 */
@SpringBootApplication
public class IdentityServiceApplication {

    /**
     * Uruchamia moduł tożsamości jako aplikację Spring Boot.
     *
     * @param args argumenty wiersza poleceń przekazywane do kontekstu Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

}
