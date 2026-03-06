// Creato il 06/03/2026
package com.generation.voices.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.generation.voices.model.BlogPost;

public interface BlogPostRepository extends JpaRepository<BlogPost, Integer> {

    // Ritorna tutti i post di un blog specifico tramite l'id del blog
    List<BlogPost> findByBlogId(int blogId);

}
