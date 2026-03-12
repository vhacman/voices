package com.generation.voices.model;

import java.util.ArrayList;
import java.util.List;
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

@Entity
@Data
public class Blog
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    @Column(unique = true)
    private String title;

    // sarà scelta da una lista di immagini disponibili
    @NotEmpty(message = "Image is required")
    private String image;

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

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", author=" + author +
                ", template=" + template +
                ", visibility=" + visibility +
                ", palette=" + palette +
                ", posts=" + posts +
                '}';
    }

}
