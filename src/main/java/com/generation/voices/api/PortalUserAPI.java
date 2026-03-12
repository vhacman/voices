package com.generation.voices.api;

import com.generation.voices.dto.ChangePasswordDTO;
import com.generation.voices.dto.LoginRequestDTO;
import com.generation.voices.dto.PortalUserDTO;
import com.generation.voices.dto.RegisterDTO;
import com.generation.voices.service.PortalUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voices/api/users")
public class PortalUserAPI {

    @Autowired
    PortalUserService service;

    // Solo ADMIN: lista completa degli utenti
    @GetMapping
    public ResponseEntity<List<PortalUserDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Solo ADMIN: dettaglio singolo utente
    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Solo ADMIN: crea utente con ruolo a scelta
    @PostMapping
    public ResponseEntity<Object> insert(@Valid @RequestBody RegisterDTO dto) {
        try {
            return ResponseEntity.status(201).body(service.save(dto));
        } catch (ConstraintViolationException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Solo ADMIN: modifica utente
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody RegisterDTO dto) {
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (ConstraintViolationException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Solo ADMIN: elimina utente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Pubblico: chiunque può registrarsi, viene creato come BLOGGER.
    // Separato da POST /users (ADMIN) perché la logica è diversa:
    // qui il ruolo è forzato a BLOGGER, lì l'ADMIN può sceglierlo.
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            return ResponseEntity.status(201).body(service.register(dto));
        } catch (ConstraintViolationException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Pubblico: login con username e password.
    // La risposta include mustChangePassword: se true il frontend
    // deve mandare l'utente sulla schermata di cambio password obbligato.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            return ResponseEntity.ok(service.login(request.getUsername(), request.getPassword()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

    // Cambio password: richiede JWT (utente loggato).
    // Usato sia per il cambio volontario che per quello forzato post-login.
    @PutMapping("/{id}/password")
    public ResponseEntity<Object> changePassword(@PathVariable Integer id, @Valid @RequestBody ChangePasswordDTO dto) {
        try {
            service.changePassword(id, dto);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
