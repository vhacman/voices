package com.generation.voices.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.generation.voices.model.enumerations.BlogPostStatus;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogPostDTO
{

    private int id;

    // Sostituito blogId con l'oggetto BlogDTO annidato
    @NotNull(message = "Blog is required")
    private BlogDTO blog;

    private LocalDateTime publishedOn;

    @Enumerated
    private BlogPostStatus status;

    private String tags;

    // Rinominato da viewCount a view per allinearlo al modello del prof
    private int view;

    // Immagine di copertina del post — richiesta dal modello del prof
    private String image;

    @NotEmpty(message = "Title is required")
    private String title;

    // Rinominato da content a text per allinearlo al modello del prof
    @NotEmpty(message = "Text is required")
    private String text;

    // Lista commenti del post — popolata dal BlogPostMapper usando la relazione @OneToMany
    private List<CommentDTO> comments;

}
