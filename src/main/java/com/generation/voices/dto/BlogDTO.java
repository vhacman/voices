// Creato il 06/03/2026
package com.generation.voices.dto;

import com.generation.voices.model.enumerations.BlogType;
import com.generation.voices.model.enumerations.Palette;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogDTO {

    private int id;

    @NotEmpty(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @NotEmpty(message = "Cover is required")
    private String cover;

    @NotEmpty(message = "Description is required")
    private String description;

    // Passiamo solo l'id dell'autore invece dell'oggetto PortalUser completo
    @NotNull(message = "Author id is required")
    private int authorId;

    @NotNull(message = "Blog type is required")
    private BlogType type;

    @NotNull(message = "Palette is required")
    private Palette palette;

}
