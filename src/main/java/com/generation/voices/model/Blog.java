package com.generation.voices.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.generation.voices.model.enumerations.Palette;
import com.generation.voices.model.enumerations.Template;
import com.generation.voices.model.enumerations.Visibility;
import lombok.Data;
import lombok.ToString;

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
    
    @NotNull(message = "Template is required")
    @Enumerated(EnumType.STRING)
    private Template template;

    @NotNull(message = "Visibility is required")
    @Enumerated(EnumType.STRING)
    // Il nome colonna resta "type" per compatibilità col DB esistente
    @jakarta.persistence.Column(name = "type")
    private Visibility visibility;

    @NotNull(message = "Palette is required")
    @Enumerated(EnumType.STRING)
    private Palette palette;

    // mappedBy = "blog" dice a JPA che questa relazione è il lato inverso:
    // la FK (blog_id) sta nella tabella blog_post, non qui in blog.
    // Se non mettessi mappedBy, JPA creerebbe una tabella di join intermedia inutile.
    // LAZY: i post NON vengono caricati dal DB ogni volta che carico un Blog.
    // Vengono letti solo quando chiamo getPosts() o itero su posts.
    // Utile perché spesso voglio solo il titolo/descrizione del blog, non tutti i suoi post.
    // @ToString.Exclude: senza questo, @Data genera Blog.toString() che chiama posts,
    // che chiama BlogPost.toString() che chiama blog, che chiama Blog.toString() → loop infinito.
    @OneToMany(mappedBy = "blog", fetch = FetchType.LAZY)
    @ToString.Exclude
    List<BlogPost> posts = new ArrayList<BlogPost>();


    // Metodo richiesto dal prof: organizza i post del blog per anno e mese.
    // Serve per la vista "archivio" laterale (tipo sidebar con "Marzo 2024 (3 post)").
    // Restituisce una Map<anno, PostsByYear> dove ogni PostsByYear contiene 12 PostsByMonth.
    public Map<Integer, PostsByYear> getPostsByYearAndMonth()
    {
        // Cerco il range di anni tra tutti i post: parto da valori sentinella estremi.
        // minYear = 4500 e maxYear = 0: qualsiasi anno reale li sostituirà al primo ciclo.
        int minYear = 4500;
        int maxYear = 0;

        for(BlogPost p:posts)
        {
            if(p.getPublishedOn().getYear()<minYear)
                minYear = p.getPublishedOn().getYear();
            if(p.getPublishedOn().getYear()>maxYear)
                maxYear = p.getPublishedOn().getYear();
        }

        // Creo una entry PostsByYear per ogni anno nel range.
        // LinkedHashMap mantiene l'ordine di inserimento → gli anni escono in ordine cronologico.
        Map<Integer, PostsByYear> res = new LinkedHashMap<Integer,PostsByYear>();
        for(int year=minYear;year<=maxYear;year++)
            res.put(year, new PostsByYear(year));

        for(BlogPost p:posts)
        {
            // getMonthValue() restituisce 1-12, ma months[] è 0-indexed → devo sottrarre 1.
            // Senza il -1, gennaio (1) finirebbe in months[1] che è febbraio: bug silenzioso.
            int year = p.getPublishedOn().getYear();
            int month = p.getPublishedOn().getMonthValue()-1;
            res.get(year).months[month].posts.add(p);
        }

        return res;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                ", description='" + description + '\'' +
                ", author=" + author +
                ", template=" + template +
                ", visibility=" + visibility +
                ", palette=" + palette +
                ", posts=" + posts +
                '}';
    }

}
