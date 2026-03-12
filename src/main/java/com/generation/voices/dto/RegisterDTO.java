package com.generation.voices.dto;

import java.time.LocalDate;
import com.generation.voices.model.enumerations.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO usato solo in ingresso per POST /users e PUT /users/{id}.
// Contiene tutti i campi che il client manda alla creazione/modifica,
// inclusa la password in chiaro (che viene hashata nel service prima del salvataggio).
// Separato da PortalUserDTO per non esporre mai la password nelle risposte.
@Data
public class RegisterDTO {

    // Il frontend manda "nickname", ma nell'entità il campo si chiama "username".
    // La traduzione avviene nel mapper con @Mapping(source="nickname", target="username").
    @NotEmpty(message = "Nickname is required")
    private String nickname;

    @NotEmpty(message = "Email is required")
    private String email;

    @NotEmpty(message = "Password is required")
    private String password;

    @NotEmpty(message = "First name is required")
    private String firstName;

    @NotEmpty(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @NotNull(message = "Role is required")
    private Role role;

}
