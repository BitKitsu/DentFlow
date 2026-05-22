package com.dentflow.identity.auth.api;

import com.dentflow.identity.auth.application.AuthService;
import com.dentflow.identity.security.JwtService;
import com.dentflow.identity.user.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    /**
     * UserRepository jest wymagane przez JwtAuthenticationFilter, który jest częścią
     * kontekstu @WebMvcTest. Bez tego mocka Spring nie może zainicjalizować filtra.
     */
    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest("fake-email", "password123");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenEmailIsEmpty() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest("", "password123");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
