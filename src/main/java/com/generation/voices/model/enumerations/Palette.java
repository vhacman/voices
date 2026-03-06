package com.generation.voices.model.enumerations;

import lombok.Getter;

/**
 * Combinazione di colori
 */
@Getter
public enum Palette {

    OCEAN("Ocean", "Deep sea vibes", "#FFFFFF", "#E0F2F1", "#006064", "#004D40", "#4DD0E1", "#00BCD4"),
    FOREST("Forest", "Nature and greenery", "#F1F8E9", "#DCEDC8", "#2E7D32", "#1B5E20", "#81C784", "#4CAF50"),
    SUNSET("Sunset", "Warm evening colors", "#FFFFFF", "#FFF3E0", "#BF360C", "#3E2723", "#FF7043", "#FF5722"),
    NIGHT("Night", "Dark mode elegance", "#E0E0E0", "#BDBDBD", "#212121", "#121212", "#424242", "#BB86FC"),
    SAND("Sand", "Warm desert tones", "#3E2723", "#4E342E", "#FFF8E1", "#FFECB3", "#D7CCC8", "#FFC107");

    String title;
    String description;
    String titleColor;
    String textColor;
    String titleBackground;
    String textBackground;
    String borderColor;
    String accent;

    private Palette(String title, String description, String titleColor, String textColor, String titleBackground, 
        String textBackground, String borderColor, String accent
    )
    {
        this.titleColor = titleColor;
        this.textColor = textColor;
        this.titleBackground = titleBackground;
        this.textBackground = textBackground;
        this.borderColor = borderColor;
        this.accent = accent;
        this.title = title;
        this.description = description;
    }

}
