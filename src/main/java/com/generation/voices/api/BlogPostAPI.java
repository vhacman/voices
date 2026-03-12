// Creato il 06/03/2026
package com.generation.voices.api;

import com.generation.voices.dto.BlogPostDTO;
import com.generation.voices.service.BlogPostService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/voices/api/posts")
public class BlogPostAPI
{

    @Autowired
    BlogPostService service;

    @GetMapping
    public ResponseEntity<List<BlogPostDTO>> findAll()
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

    // Ritorna tutti i post di un blog specifico
    @GetMapping("/blog/{blogId}")
    public ResponseEntity<List<BlogPostDTO>> findByBlogId(@PathVariable int blogId)
    {
        return ResponseEntity.ok(service.findByBlogId(blogId));
    }

    @PostMapping
    public ResponseEntity<Object> insert(@Valid @RequestBody BlogPostDTO dto)
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
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody BlogPostDTO dto) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id)
    {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Incrementa il contatore di visualizzazioni — chiamato dal frontend al caricamento del post
    @PostMapping("/{id}/view")
    public ResponseEntity<Object> incrementViewCount(@PathVariable Integer id)
    {
        try
        {
            return ResponseEntity.ok(service.incrementViewCount(id));
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
