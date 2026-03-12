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

// Questa classe serve perché Spring Security di default blocca tutto
// e genera una sua pagina di login — che non voglio.
// Devo dirgli esplicitamente come gestire autenticazione e autorizzazione
// per una REST API con JWT.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Mi serve il filtro JWT che ho scritto: lo inietto qui
    // per poterlo inserire nella catena di filtri più avanti.
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    // Qui definisco come Spring deve trattare ogni request in arrivo.
    // L'ordine dei filtri e delle regole conta: Spring applica
    // la prima regola che matcha e si ferma.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disabilito CSRF perché con JWT stateless non serve:
            // il token va messo a mano nell'header Authorization,
            // il browser non lo manda mai da solo come fa con i cookie.
            .csrf(csrf -> csrf.disable())

            // CORS deve stare prima delle regole di autorizzazione,
            // altrimenti le preflight OPTIONS di Angular vengono bloccate
            // con 403 prima ancora di arrivare alla logica CORS.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(auth -> auth

                // Il login deve essere pubblico: è l'unico endpoint
                // che non ha ancora un token perché lo sta producendo.
                // La registrazione è pubblica: chiunque può crearsi un account.
                .requestMatchers("/voices/api/users/login", "/voices/api/users/register").permitAll()

                // Un visitatore non loggato può leggere blog, post e archivio.
                // Non ha senso richiedere il login solo per sfogliare i contenuti.
                // Limitato ai soli GET: POST/PUT/DELETE restano protetti.
                .requestMatchers(HttpMethod.GET, "/voices/api/blogs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/voices/api/posts/**").permitAll()

                // La gestione degli utenti è solo per ADMIN.
                // Un BLOGGER non deve poter vedere, modificare o cancellare account.
                // Nota: hasRole("ADMIN") cerca "ROLE_ADMIN" nel SecurityContext —
                // il prefisso ROLE_ lo aggiunge CustomUserDetailsService.
                .requestMatchers("/voices/api/users/**").hasRole("ADMIN")

                // Tutto il resto richiede solo di essere loggati,
                // sia BLOGGER che ADMIN. Se il token manca o è scaduto → 401.
                .anyRequest().authenticated()
            )

            // STATELESS: non voglio sessioni lato server.
            // L'identità dell'utente è tutta dentro il JWT.
            // Ad ogni request il filtro lo rilegge e ricostruisce il contesto.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Metto il mio filtro JWT prima di quello standard di Spring.
            // In questo modo quando Spring controlla l'autenticazione,
            // il SecurityContext è già stato popolato dal mio filtro.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Angular gira su localhost:4200, il backend su localhost:8080.
    // Il browser blocca le chiamate cross-origin per default (same-origin policy).
    // Qui dico al browser che localhost:4200 è un'origine fidata.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Solo Angular in locale — in produzione va cambiato col dominio reale.
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Tutti i metodi che usano le mie API.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Authorization serve per il JWT, Content-Type per mandare JSON nel body.
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Il browser mette in cache la risposta preflight per un'ora:
        // evita di fare una request OPTIONS prima di ogni chiamata.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
