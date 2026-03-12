package com.generation.voices.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Comment è la nuova entità che mancava nel backend.
// L'ho creata seguendo lo stesso schema di BlogPost: @Entity + @Data + foreign key con @ManyToOne.
// Nel diagramma E-R un Comment appartiene a un solo BlogPost e viene scritto da un solo PortalUser.
@Entity
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Uso TEXT come in BlogPost.content: un commento può essere lungo,
    // VARCHAR(255) di default sarebbe troppo corto.
    @NotEmpty(message = "Content is required")
    @Column(columnDefinition = "TEXT")
    private String content;

    // createdAt non ha @NotNull perché non lo manda il client:
    // viene impostato nel CommentService.save() con LocalDateTime.now().
    // In questo modo la data è sempre quella del server, non manipolabile dall'utente.
    private LocalDateTime createdAt;

    // Relazione verso BlogPost: un commento appartiene a un solo post.
    // @JoinColumn(name = "post_id") crea la colonna FK nella tabella comment.
    // Stesso pattern di BlogPost → Blog.
    @NotNull(message = "Post is required")
    @ManyToOne
    @JoinColumn(name = "post_id")
    private BlogPost post;

    // Relazione verso PortalUser: un commento ha un solo autore.
    // Stessa FK che usa Blog per l'autore del blog (author_id).
    @NotNull(message = "Author is required")
    @ManyToOne
    @JoinColumn(name = "author_id")
    private PortalUser author;


    // toString() scritto a mano per lo stesso motivo di BlogPost:
    // Comment → post (BlogPost) → blog (Blog) → posts (List<BlogPost>) → loop infinito.
    // Includo post e author perché mi servono per il debug, ma non chiamo i loro toString()
    // direttamente: Lombok di BlogPost e PortalUser gestisce la loro rappresentazione
    // senza ricadere in catene cicliche (PortalUser non ha riferimenti inversi a Comment).
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", post=" + post +
                ", author=" + author +
                '}';
    }
}
