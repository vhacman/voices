package com.generation.voices.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Stesso pattern usato in BlogPostDTO e BlogDTO:
// invece di mandare al client l'intero oggetto BlogPost annidato dentro Comment
// (che porterebbe anche Blog, che porta altri BlogPost... esplosione di JSON),
// mando solo gli id. Il frontend sa già quale post sta guardando, non ha bisogno
// di ricevere tutti i dati annidati ogni volta.
@Data
public class CommentDTO {

    private int id;

    @NotEmpty(message = "Content is required")
    private String content;

    // Non valido createdAt con @NotNull: il client non lo deve mandare.
    // È il service a impostarlo con LocalDateTime.now() al momento del salvataggio.
    private LocalDateTime createdAt;

    // Passo solo l'id del post invece dell'oggetto BlogPost completo.
    // Il mapper si occupa di convertire postId → comment.post.id (e viceversa).
    @NotNull(message = "Post id is required")
    private int postId;

    // Passo solo l'id dell'autore invece dell'oggetto PortalUser completo.
    // Stessa logica di authorId in BlogDTO.
    @NotNull(message = "Author id is required")
    private int authorId;

}
