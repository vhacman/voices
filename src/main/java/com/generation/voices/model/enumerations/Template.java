package com.generation.voices.model.enumerations;

import lombok.Getter;

// Template era presente nel diagramma E-R ma mancava completamente nel codice.
// Nel diagramma E-R ha una relazione 1:M con Blog (un template → molti blog),
// quindi poteva essere un'entità separata. Ho scelto un enum perché i template
// sono un insieme fisso noto a priori: non ha senso una tabella DB con righe che
// non cambieranno mai. Stesso ragionamento usato per Palette e Role.
//
// Ho aggiunto title e description come campi dell'enum seguendo il pattern di Palette:
// in questo modo il frontend può mostrare "Minimal - Testo puro, spazio bianco..."
// senza dover fare un secondo endpoint solo per leggere i metadati dei template.
// @Getter di Lombok genera getTitle() e getDescription() automaticamente.
@Getter
public enum Template {

    MINIMAL("Minimal", "Testo puro, spazio bianco, nessuna distrazione"),
    MAGAZINE("Magazine", "Multi-colonna stile rivista, griglia di anteprime"),
    PORTFOLIO("Portfolio", "Focus su immagini e gallery, testo in secondo piano"),
    JOURNAL("Journal", "Diario personale, una colonna, data in evidenza"),
    CLASSIC("Classic", "Layout blog tradizionale con sidebar e header grande");

    private final String title;
    private final String description;

    Template(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
