// Creato il 06/03/2026
package com.generation.voices.dto;

import java.time.LocalDateTime;

import com.generation.voices.model.enumerations.BlogPostStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogPostDTO {

    private int id;

    // Passiamo solo l'id del blog invece dell'oggetto Blog completo
    @NotNull(message = "Blog id is required")
    private int blogId;

    private LocalDateTime publishedOn;

    private BlogPostStatus status;

    private String tags;

    private int viewCount;

    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Content is required")
    private String content;

}
