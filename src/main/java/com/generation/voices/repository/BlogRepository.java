package com.generation.voices.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.generation.voices.model.Blog;
import com.generation.voices.model.enumerations.Visibility;

public interface BlogRepository extends JpaRepository<Blog, Integer> {

    // Usato per i visitatori non loggati: vedono solo i blog PUBLIC.
    List<Blog> findByVisibility(Visibility visibility);

}
