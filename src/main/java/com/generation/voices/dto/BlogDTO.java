package com.generation.voices.dto;

import com.generation.voices.model.enumerations.Palette;
import com.generation.voices.model.enumerations.Template;
import com.generation.voices.model.enumerations.Visibility;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogDTO
{

    private int id;

    @NotEmpty(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    // Rinominato da cover a image per allinearlo al modello del prof
    @NotEmpty(message = "Image is required")
    private String image;

    @NotEmpty(message = "Description is required")
    private String description;

    // Sostituito authorId con l'oggetto annidato PortalUserDTO
    // Il mapper gestisce la conversione PortalUser → PortalUserDTO automaticamente
    @NotNull(message = "Author is required")
    private PortalUserDTO author;

    @NotNull(message = "Template is required")
    private Template template;

    @NotNull(message = "Visibility is required")
    private Visibility visibility;

    @NotNull(message = "Palette is required")
    private Palette palette;

}
