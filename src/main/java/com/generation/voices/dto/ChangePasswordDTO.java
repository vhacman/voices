package com.generation.voices.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

// DTO usato per il cambio password: il client manda solo la nuova password.
// Non serve la vecchia perché l'endpoint è protetto da JWT —
// se hai un token valido sei già identificato.
@Data
public class ChangePasswordDTO {

    @NotEmpty(message = "La nuova password è obbligatoria")
    private String newPassword;

}
