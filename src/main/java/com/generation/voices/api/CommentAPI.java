package com.generation.voices.api;

import java.util.List;
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
import com.generation.voices.dto.CommentDTO;
import com.generation.voices.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/voices/api/comments")
public class CommentAPI
{
    @Autowired
    CommentService service;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> findAll()
    {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable Integer id)
    {
        try
        {
            return ResponseEntity.ok(service.findById(id));
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Tutti i commenti di un post — usato dal frontend per caricare la sezione commenti
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> findByPostId(@PathVariable int postId)
    {
        return ResponseEntity.ok(service.findByPostId(postId));
    }

    // Tutti i commenti scritti da un utente specifico
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<CommentDTO>> findByAuthorId(@PathVariable int authorId)
    {
        return ResponseEntity.ok(service.findByAuthorId(authorId));
    }

    @PostMapping
    public ResponseEntity<Object> insert(@Valid @RequestBody CommentDTO dto)
    {
        try
        {
            return ResponseEntity.status(201).body(service.save(dto));
        }
        catch (ConstraintViolationException e)
        {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody CommentDTO dto)
    {
        try
        {
            return ResponseEntity.ok(service.update(id, dto));
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(404).body(e.getMessage());
        }
        catch (ConstraintViolationException e)
        {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Solo ADMIN o l'autore del commento possono eliminarlo (gestito lato service/security)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id)
    {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
