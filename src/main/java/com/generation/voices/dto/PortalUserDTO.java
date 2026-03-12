package com.generation.voices.dto;

import com.generation.voices.model.enumerations.Role;
import lombok.Data;

// DTO di sola lettura: viene restituito nelle risposte API.
// Non include password per sicurezza.
// Non include blogs per evitare riferimenti circolari (Blog → PortalUser → Blog...).
// nickname è mappato dal campo username dell'entità tramite il mapper.
@Data
public class PortalUserDTO
{

    private int id;

    // Mappato da username nel PortalUserMapper
    private String nickname;

    private String email;

    private Role role;

}
