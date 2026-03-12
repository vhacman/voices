package com.generation.voices.model.enumerations;

// Ho rinominato BlogType in Visibility per allinearmi al diagramma E-R del prof.
// I valori restano PUBLIC e PRIVATE: il DB non si accorge del cambio
// perché uso @Enumerated(EnumType.STRING) e salvo la stringa "PUBLIC"/"PRIVATE",
// non il numero dell'enum. Ho anche aggiunto @Column(name = "type") in Blog.java
// così la colonna nel DB resta "type" e non devo migrare niente.
public enum Visibility {
    // Visibile a tutti gli utenti autenticati
    PUBLIC,
    // Visibile solo all'autore e agli ADMIN
    PRIVATE
}
