package com.generation.voices.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDTO
{

    private int id;

    // Rinominato da content a text per allinearlo al modello del prof
    @NotEmpty(message = "Text is required")
    private String text;

    // Rinominato da createdAt a publishedOn per allinearlo al modello del prof.
    // Non validato: il service lo imposta con LocalDateTime.now() al momento del salvataggio.
    private LocalDateTime publishedOn;

    // Autore del commento come oggetto annidato invece del solo authorId.
    // Il mapper converte PortalUser → PortalUserDTO automaticamente.
    @NotNull(message = "Author is required")
    private PortalUserDTO author;

    // postId mantenuto per le operazioni di scrittura (POST /comments):
    // il client manda solo l'id del post, non l'oggetto annidato completo.
    // Non includiamo BlogPostDTO per evitare riferimenti circolari
    // (BlogPost → Comment → BlogPost → ...).
    @NotNull(message = "Post id is required")
    private int postId;

}
