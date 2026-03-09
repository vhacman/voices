package com.generation.voices.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/*
 * ============================================================
 *  PERCHÉ ESISTE QUESTA CLASSE?
 * ============================================================
 * Spring Security, appena aggiunto al progetto, blocca TUTTO
 * per default con una pagina di login HTML generata in automatico.
 * Questo ha senso per le web app con sessioni, ma per una REST API
 * con JWT dobbiamo ridefinire completamente le regole del gioco.
 *
 * Questa classe è il "regolamento" della sicurezza dell'applicazione:
 * decide chi può fare cosa, in che modo ci si autentica,
 * e quali meccanismi vengono attivati.
 * ============================================================
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * Inietto il filtro JWT che ho costruito.
     * SecurityConfig sa che esiste questo filtro e deve inserirlo
     * nella catena di elaborazione delle request HTTP.
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    /*
     * ============================================================
     *  LA FILTER CHAIN: come viene elaborata ogni request
     * ============================================================
     *
     * Ogni request HTTP che arriva al backend attraversa una catena
     * di filtri (SecurityFilterChain) prima di raggiungere il controller.
     * Questi filtri possono bloccare la request, modificarla, o lasciarla passare.
     *
     * La catena che configuriamo qui funziona così:
     *
     *   [Request in arrivo]
     *        │
     *        ▼
     *   CorsFilter              ← gestisce i preflight OPTIONS di Angular
     *        │
     *        ▼
     *   JwtAuthenticationFilter ← legge Authorization, valida il token,
     *        │                     popola il SecurityContext se valido
     *        ▼
     *   Regole di autorizzazione ← definite in authorizeHttpRequests
     *        │  login e registrazione → sempre accessibili
     *        │  gestione utenti      → solo ADMIN
     *        │  tutto il resto       → qualsiasi utente autenticato
     *        ▼
     *   Controller (API)
     *
     * Se la request non supera le regole, Spring risponde 401 (non autenticato)
     * o 403 (autenticato ma non autorizzato) senza mai toccare il controller.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            /*
             * CSRF (Cross-Site Request Forgery) disabilitato.
             *
             * Il CSRF è un attacco in cui un sito malevolo induce il browser
             * dell'utente a fare richieste a nostra insaputa, sfruttando il cookie
             * di sessione che il browser manda in automatico.
             *
             * Con JWT stateless questo problema non esiste: il token va messo
             * esplicitamente nell'header Authorization a ogni request —
             * il browser non lo manda mai da solo. Quindi CSRF non serve.
             */
            .csrf(csrf -> csrf.disable())

            /*
             * CORS abilitato con la configurazione definita nel bean corsConfigurationSource().
             * Deve essere abilitato PRIMA delle regole di autorizzazione, altrimenti
             * le request preflight OPTIONS di Angular vengono bloccate con 403
             * ancora prima di arrivare alla logica CORS.
             */
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(auth -> auth

                /*
                 * ROTTE PUBBLICHE — non richiedono JWT.
                 *
                 * Il login deve essere pubblico per forza:
                 * è l'unica route che produce il token.
                 * Se richiedesse autenticazione, nessuno potrebbe mai autenticarsi.
                 *
                 * La registrazione (POST /users) è pubblica perché un nuovo utente
                 * non ha ancora un token — non può registrarsi se deve già essere loggato.
                 * Specifichiamo HttpMethod.POST per non aprire tutti i metodi su /users.
                 */
                .requestMatchers("/voices/api/users/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/voices/api/users").permitAll()

                /*
                 * ROTTE SOLO ADMIN — ruolo ADMIN richiesto.
                 *
                 * La gestione degli account utente (lista, modifica, eliminazione)
                 * è riservata agli amministratori del portale.
                 * Un BLOGGER non deve poter vedere tutti gli utenti,
                 * modificare account altrui, o eliminare account.
                 *
                 * hasRole("ADMIN") controlla che il SecurityContext contenga
                 * l'authority "ROLE_ADMIN" — il prefisso "ROLE_" viene aggiunto
                 * automaticamente da CustomUserDetailsService tramite .roles().
                 *
                 * ATTENZIONE: l'ordine conta. Questa regola viene prima di anyRequest()
                 * altrimenti verrebbe ignorata (Spring applica la prima regola che matcha).
                 */
                .requestMatchers("/voices/api/users/**").hasRole("ADMIN")

                /*
                 * TUTTO IL RESTO — richiede autenticazione (qualsiasi ruolo).
                 *
                 * Blog e post sono accessibili a qualsiasi utente loggato,
                 * sia BLOGGER che ADMIN.
                 * "Autenticato" significa: il SecurityContext è stato popolato
                 * dal JwtAuthenticationFilter con un token valido.
                 * Se il token manca, è scaduto, o è manomesso → 401.
                 */
                .anyRequest().authenticated()
            )

            /*
             * Sessioni STATELESS: Spring NON crea né usa HttpSession.
             *
             * Nelle app tradizionali, dopo il login il server memorizza
             * l'identità dell'utente in una sessione (lato server) e
             * manda un cookie al browser. Ad ogni request successiva
             * il browser manda il cookie e il server recupera la sessione.
             *
             * Con JWT è l'opposto: il server non ricorda niente.
             * L'identità dell'utente è dentro il token stesso.
             * A ogni request il filtro ricostruisce l'autenticazione
             * leggendo e verificando il token. Zero stato lato server.
             *
             * Vantaggi:
             * - scalabilità orizzontale (più istanze del server, nessun problema)
             * - nessun problema di sessioni scadute sul server
             * - il token può essere usato da qualsiasi client (browser, mobile, CLI)
             */
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            /*
             * Inserisco il mio filtro JWT PRIMA del filtro standard di Spring
             * (UsernamePasswordAuthenticationFilter). In questo modo, quando
             * Spring controlla se l'utente è autenticato, lo trova già nel
             * SecurityContext perché il mio filtro l'ha già messo lì.
             */
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * ============================================================
     *  CORS — Cross-Origin Resource Sharing
     * ============================================================
     *
     * Il browser, per sicurezza, blocca le chiamate HTTP verso un dominio
     * diverso da quello della pagina corrente (same-origin policy).
     * Angular gira su localhost:4200, il backend su localhost:8080 —
     * origini diverse → il browser blocca tutto per default.
     *
     * CORS è il meccanismo che permette al backend di dire al browser:
     * "va bene, accetto request da questa origine specifica".
     *
     * Come funziona il preflight:
     * Prima di ogni request "non semplice" (POST, PUT, DELETE con JSON),
     * il browser manda automaticamente una request OPTIONS (preflight)
     * per chiedere al server quali origini/metodi/header sono permessi.
     * Il server risponde con gli header Access-Control-Allow-*.
     * Solo se il server approva, il browser manda la request reale.
     *
     * Configurazione:
     * - allowedOrigins: solo Angular in locale (non "*" per sicurezza)
     * - allowedMethods: tutti i metodi HTTP usati dalle nostre API
     * - allowedHeaders: Authorization (per il JWT) + Content-Type (per il JSON)
     * - maxAge: il browser mette in cache il risultato del preflight per 1 ora,
     *   evitando di ripetere la request OPTIONS a ogni chiamata
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origine permessa: Angular in locale
        // In produzione, sostituire con il dominio reale (es. "https://voices.com")
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Metodi HTTP permessi — tutti quelli usati dalle nostre API REST
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Header permessi nelle request:
        // - Authorization: trasporta il JWT ("Bearer eyJ...")
        // - Content-Type: specifica che il body è JSON ("application/json")
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Cache del preflight nel browser: 3600 secondi = 1 ora
        // Riduce il numero di request OPTIONS inutili
        config.setMaxAge(3600L);

        // Applica questa configurazione CORS a tutti gli endpoint del backend
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
