package com.generation.voices.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.generation.voices.model.Blog;

public interface BlogRepository extends JpaRepository<Blog, Integer> {

}
