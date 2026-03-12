package com.generation.voices.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.generation.voices.model.enumerations.Role;
import lombok.Data;

@Entity
@Data
public class PortalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    @NotNull(message="Role is required")
    private Role role;

    // Tengo traccia di quando l'utente ha cambiato la password l'ultima volta.
    // Lo imposto al momento della registrazione e lo aggiorno ad ogni cambio password.
    // Serve per forzare il cambio periodico: 2 settimane per ADMIN, 3 mesi per BLOGGER.
    private LocalDate lastPasswordChange;

    @Override
    public String toString() {
        return "PortalUser{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", dob=" + dob +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", lastPasswordChange=" + lastPasswordChange +
                '}';
    }
}
