package com.generation.voices.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {

    @NotEmpty(message = "Username obbligatorio")
    private String username;

    @NotEmpty(message = "Password obbligatoria")
    private String password;
}
