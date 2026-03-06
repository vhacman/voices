package com.generation.voices.model;

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
import jakarta.validation.constraints.Size;
import com.generation.voices.model.enumerations.BlogType;
import com.generation.voices.model.enumerations.Palette;
import lombok.Data;

// scadenza 6 marzo ore 13
// una pagina web con un BLOG DI PROVA
// inserito a mano dall'admin
// con dati di prova
// ma che dia un'idea di come funzionerà il tutto
// solo lettura: API GET /api/voices/blogs/1
// voices/blogs/la_strada_e_i_canti (percorso verso il componente BlogPage di Angular)

@Entity
@Data
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotEmpty(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    @Column(unique = true)
    private String title;

    // sarà scelta da una lista di immagini disponibili
    @NotEmpty(message = "Cover is required")
    private String cover;

    @NotEmpty(message = "Description is required")
    private String description;
    
    @NotNull(message = "Author is required")
    @ManyToOne
    @JoinColumn(name = "author_id")
    private PortalUser author;
    
    @NotNull(message = "Blog type is required")
    @Enumerated(EnumType.STRING)
    private BlogType type;
    
    @NotNull(message = "Palette is required")
    @Enumerated(EnumType.STRING)
    private Palette palette;

}
