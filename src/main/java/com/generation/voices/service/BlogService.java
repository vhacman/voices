package com.generation.voices.service;

import com.generation.voices.dto.BlogDTO;
import com.generation.voices.mapper.BlogMapper;
import com.generation.voices.model.Blog;
import com.generation.voices.model.BlogPost;
import com.generation.voices.model.PostsByYear;
import com.generation.voices.repository.BlogPostRepository;
import com.generation.voices.repository.BlogRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogMapper blogMapper;

    public List<BlogDTO> findAll() {
        return blogMapper.toDTOs(blogRepository.findAll());
    }

    public BlogDTO findById(Integer id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + id));
        return blogMapper.toDTO(blog);
    }

    public BlogDTO save(@Valid BlogDTO blogDTO) {
        Blog blog = blogMapper.toEntity(blogDTO);
        blog = blogRepository.save(blog);
        return blogMapper.toDTO(blog);
    }

    public BlogDTO update(Integer id, @Valid BlogDTO blogDTO) {
        // Verifica che il blog esista prima di aggiornare
        blogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + id));
        Blog blog = blogMapper.toEntity(blogDTO);
        blog.setId(id);
        blog = blogRepository.save(blog);
        return blogMapper.toDTO(blog);
    }

    public void deleteById(Integer id) {
        blogRepository.deleteById(id);
    }

    /**
     * Restituisce i post PUBLISHED di un blog organizzati per anno e mese.
     * Usato dal frontend per costruire la vista "Archivio" del blog.
     * Esempio risposta: { 2024: { months: [...] }, 2025: { months: [...] } }
     */
    public Map<Integer, PostsByYear> getPostsByYearAndMonth(Integer blogId) {
        blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + blogId));

        List<BlogPost> posts = blogPostRepository.findByBlogId(blogId);

        if (posts.isEmpty()) return new LinkedHashMap<>();

        int minYear = posts.stream().filter(p -> p.getPublishedOn() != null)
                .mapToInt(p -> p.getPublishedOn().getYear()).min().orElse(0);
        int maxYear = posts.stream().filter(p -> p.getPublishedOn() != null)
                .mapToInt(p -> p.getPublishedOn().getYear()).max().orElse(0);

        Map<Integer, PostsByYear> result = new LinkedHashMap<>();
        for (int year = minYear; year <= maxYear; year++)
            result.put(year, new PostsByYear(year));

        for (BlogPost p : posts) {
            if (p.getPublishedOn() == null) continue;
            int year = p.getPublishedOn().getYear();
            int month = p.getPublishedOn().getMonthValue() - 1; // 0-based
            result.get(year).getMonths()[month].getPosts().add(p);
        }

        return result;
    }

}
