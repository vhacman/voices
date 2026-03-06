package com.generation.voices.service;

import com.generation.voices.model.Blog;
import com.generation.voices.repository.BlogRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    /**
     * Ritorna la lista di tutti i blog.
     */
    public List<Blog> findAll() {
        return blogRepository.findAll();
    }

    /**
     * Cerca un blog tramite ID. Lancia un'eccezione se non trovato.
     */
    public Blog findById(Integer id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + id));
    }

    /**
     * Salva un nuovo blog o aggiorna uno esistente.
     */
    public Blog save(@Valid Blog blog) {
        return blogRepository.save(blog);
    }

    /**
     * Elimina un blog tramite ID.
     */
    public void deleteById(Integer id) {
        blogRepository.deleteById(id);
    }

}
