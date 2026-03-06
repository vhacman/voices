package com.generation.voices.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generation.voices.dto.PortalUserDTO;
import com.generation.voices.service.PortalUserService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/voices/api/users")
public class PortalUserAPI {

    @Autowired
    PortalUserService service;

    @PostMapping
    public ResponseEntity<Object> insert(@Valid @RequestBody PortalUserDTO dto){
        try {
            dto = service.save(dto);
            return ResponseEntity.status(201).body(dto);
        } catch (ConstraintViolationException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    

    
}
