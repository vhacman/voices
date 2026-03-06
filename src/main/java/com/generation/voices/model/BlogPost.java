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
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    // 1 cosa fatta: ore 12.50
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

    @NotEmpty(message = "Title is required")
    private String title;

    // Aggiunto columnDefinition = "TEXT" per evitare il limite di 255 caratteri di VARCHAR
    // necessario per contenuti lunghi come un post di un blog
    @NotEmpty(message = "Content is required")
    @Column(columnDefinition = "TEXT")
    private String content;

    
}
