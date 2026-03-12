// Creato il 06/03/2026
package com.generation.voices.api;

import java.util.Map;
import com.generation.voices.dto.BlogDTO;
import com.generation.voices.model.PostsByYear;
import com.generation.voices.service.BlogService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voices/api/blogs")
public class BlogAPI
{

    @Autowired
    BlogService service;

    // Authentication è null se la request non ha JWT (visitatore non loggato).
    // In quel caso mostro solo i blog PUBLIC; se è loggato vede tutto.
    @GetMapping
    public ResponseEntity<List<BlogDTO>> findAll(Authentication authentication)
    {
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(isLoggedIn ? service.findAll() : service.findAllPublic());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable Integer id) {
        try
        {
            return ResponseEntity.ok(service.findById(id));
        } catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Object> insert(@Valid @RequestBody BlogDTO dto) {
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
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody BlogDTO dto) {
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

    // Archivio del blog: post raggruppati per anno → mese
    // GET /voices/api/blogs/{id}/archive
    // il prof ha suggerito di chiamare questo tipo di API "loadPostsPeriodized"
    @GetMapping("/{id}/archive")
    public ResponseEntity<Object> getArchive(@PathVariable Integer id) {
        try
        {
            Map<Integer, PostsByYear> archive = service.getPostsByYearAndMonth(id);
            return ResponseEntity.ok(archive);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
