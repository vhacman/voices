package com.generation.voices.dto;

import java.time.LocalDate;

import com.generation.voices.model.enumerations.Role;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalUserDTO {

    private int id;
    
    @NotEmpty(message = "Firstname is required")
    private String firstName;
    
    @NotEmpty(message = "Lastname is required")
    private String lastName;
    
    @NotEmpty(message = "Username is required")
    @Column(unique = true)
    private String username;
    
    @NotNull(message = "Date of birth is required")
    private LocalDate dob;
    
    @NotEmpty(message="Email is required")
    @Column(unique = true)
    private String email;
    
    @NotEmpty(message="Password is required")
    private String password;

    @NotNull(message="Role is required")
    private Role role;

}
