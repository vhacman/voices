package com.generation.voices.api;

import com.generation.voices.dto.LoginRequest;
import com.generation.voices.dto.PortalUserDTO;
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

    @GetMapping
    public ResponseEntity<List<PortalUserDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Object> insert(@Valid @RequestBody PortalUserDTO dto) {
        try {
            return ResponseEntity.status(201).body(service.save(dto));
        } catch (ConstraintViolationException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody PortalUserDTO dto) {
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (ConstraintViolationException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * Endpoint di login: pubblico (non richiede JWT).
     * Accetta { "username": "...", "password": "..." }
     * Restituisce { "token": "eyJ..." } in caso di successo,
     * oppure 401 se le credenziali sono errate.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(service.login(request.getUsername(), request.getPassword()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

}
