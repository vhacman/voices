package com.generation.voices.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.generation.voices.model.enumerations.BlogPostStatus;
import lombok.Data;

@Entity
@Data
public class BlogPost
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Aggiunto @ManyToOne e @JoinColumn per creare la foreign key blog_id verso la tabella Blog
    @NotNull(message = "Blog is required")
    @ManyToOne
    @JoinColumn(name = "blog_id")
    private Blog blog;

    private LocalDateTime publishedOn;

    // Aggiunto @Enumerated(EnumType.STRING) per salvare il valore come stringa nel DB (es. "PUBLISHED")
    // invece di un intero (indice dell'enum), rendendo i dati leggibili direttamente nel DB
    @Enumerated(EnumType.STRING)
    private BlogPostStatus status;

    private String tags;

    // Aggiunto viewCount per tracciare quante volte un post è stato aperto.
    // Parte da 0: ogni chiamata a POST /{id}/view lo incrementa di 1 lato server.
    // Non lo metto @NotNull perché il default 0 va benissimo alla creazione.
    private int viewCount = 0;

    @NotEmpty(message = "Title is required")
    private String title;

    // Aggiunto columnDefinition = "TEXT" per evitare il limite di 255 caratteri di VARCHAR
    // necessario per contenuti lunghi come un post di un blog
    @NotEmpty(message = "Content is required")
    @Column(columnDefinition = "TEXT")
    private String content;

    // Ho dovuto scrivere toString() a mano invece di affidarmi a @Data di Lombok.
    // Il problema: BlogPost ha un campo "blog" (Blog) e Blog ha una lista "posts" (List<BlogPost>).
    // Se Lombok genera entrambi i toString(), ognuno chiama l'altro → StackOverflowError.
    // Scrivendo questo toString() manualmente controllo esattamente cosa viene stampato
    // e interrompo la catena ricorsiva. blog è incluso perché mi serve vedere a quale blog appartiene.
    @Override
    public String toString() {
        return "BlogPost{" +
                "id=" + id +
                ", blog=" + blog +
                ", publishedOn=" + publishedOn +
                ", status=" + status +
                ", tags='" + tags + '\'' +
                ", viewCount=" + viewCount +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
