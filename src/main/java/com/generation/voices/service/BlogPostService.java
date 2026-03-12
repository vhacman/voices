// Creato il 06/03/2026
package com.generation.voices.service;

import com.generation.voices.dto.BlogPostDTO;
import com.generation.voices.mapper.BlogPostMapper;
import com.generation.voices.model.BlogPost;
import com.generation.voices.repository.BlogPostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class BlogPostService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogPostMapper blogPostMapper;

    public List<BlogPostDTO> findAll() {
        return blogPostMapper.toDTOs(blogPostRepository.findAll());
    }

    public BlogPostDTO findById(Integer id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost not found with id: " + id));
        return blogPostMapper.toDTO(blogPost);
    }

    // Ritorna tutti i post di un blog specifico
    public List<BlogPostDTO> findByBlogId(int blogId) {
        return blogPostMapper.toDTOs(blogPostRepository.findByBlogId(blogId));
    }

    public BlogPostDTO save(@Valid BlogPostDTO blogPostDTO) {
        BlogPost blogPost = blogPostMapper.toEntity(blogPostDTO);
        blogPost = blogPostRepository.save(blogPost);
        return blogPostMapper.toDTO(blogPost);
    }

    public BlogPostDTO update(Integer id, @Valid BlogPostDTO blogPostDTO) {
        // Verifica che il post esista prima di aggiornare
        blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost not found with id: " + id));
        BlogPost blogPost = blogPostMapper.toEntity(blogPostDTO);
        blogPost.setId(id);
        blogPost = blogPostRepository.save(blogPost);
        return blogPostMapper.toDTO(blogPost);
    }

    public void deleteById(Integer id) {
        blogPostRepository.deleteById(id);
    }

    // Incrementa il contatore di visualizzazioni di 1 e restituisce il post aggiornato
    public BlogPostDTO incrementViewCount(Integer id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost not found with id: " + id));
        blogPost.setViewCount(blogPost.getViewCount() + 1);
        blogPost = blogPostRepository.save(blogPost);
        return blogPostMapper.toDTO(blogPost);
    }

}
