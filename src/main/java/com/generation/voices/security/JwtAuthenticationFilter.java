package com.generation.voices.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/*
 *  QUESTO FILTRO: IL GUARDIANO DI OGNI REQUEST
 * In Spring Security l'autenticazione avviene attraverso una catena
 * di filtri (FilterChain). Ogni filtro può fare il suo controllo
 * e poi passare al prossimo, oppure bloccare la request.
 *
 * Questo filtro ha un compito preciso: leggere il JWT dall'header
 * Authorization, verificarlo, e — se valido — registrare l'utente
 * nel SecurityContext così che Spring sappia chi sta facendo la request.
 *
 * OncePerRequestFilter garantisce che il filtro venga eseguito
 * esattamente una volta per ogni request HTTP (non due volte
 * in caso di forward/include interni).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /*
     * JwtService sa come leggere, verificare e validare i token JWT.
     * Lo usiamo per estrarre lo username e per controllare se il token è valido.
     */
    @Autowired
    private JwtService jwtService;

    /*
     * UserDetailsService sa come caricare un utente dal database dato il suo username.
     * Spring userà questo servizio per confrontare l'utente nel token con quello nel DB.
     *
     * Perché ricaricare l'utente dal DB se abbiamo già lo username nel token?
     * Perché il token potrebbe essere stato emesso prima che l'utente venisse disabilitato
     * o che il suo ruolo cambiasse. Ricaricare dal DB garantisce dati aggiornati.
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /* cosa succede a ogni request
     * Scenario 1 — request senza token (es. POST /login):
     *   Authorization: (assente)
     *   → non mi interessa, passo avanti nella chain
     *   → SecurityConfig deciderà se la route è pubblica o meno
     *
     * Scenario 2 — request con token valido:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     *   → estraggo username dal token
     *   → carico utente dal DB
     *   → verifico firma, scadenza, età del token
     *   → popolo il SecurityContext con l'identità dell'utente
     *   → la request raggiunge il controller come "autenticata"
     *
     * Scenario 3 — request con token invalido/scaduto/manomesso:
     *   Authorization: Bearer tokenFalsificato
     *   → la verifica fallisce → non popolo il SecurityContext
     *   → la request arriva a SecurityConfig senza autenticazione → 401
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        /*
         * Leggo l'header Authorization dalla request HTTP.
         * Una request tipica autenticata ha questo aspetto:
         *
         *   GET /voices/api/blogs HTTP/1.1
         *   Host: localhost:8080
         *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwiZm...
         *
         * Il formato standard per JWT è "Bearer <token>".
         * "Bearer" indica che chi presenta il token "possiede" l'identità.
         */
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        /*
         * Se non c'è l'header Authorization, o non inizia con "Bearer ",
         * questa request non sta cercando di autenticarsi via JWT.
         * Può essere il login, o una request malformata.
         * In ogni caso: non tocco niente, passo al filtro successivo.
         * Sarà SecurityConfig a decidere se la route richiede autenticazione o no.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Rimuovo i primi 7 caratteri ("Bearer ") per ottenere solo il token grezzo.
         * Es: "Bearer eyJhbGci..." → "eyJhbGci..."
         */
        jwt = authHeader.substring(7);

        /*
         * Estraggo lo username dal claim "sub" del payload JWT.
         * Questo non verifica ancora la firma — legge solo il contenuto.
         * Se il token è malformato, JwtService lancerà un'eccezione.
         */
        username = jwtService.extractUsername(jwt);

        /*
         * Entro in questo blocco solo se:
         * 1. Ho trovato uno username nel token (token parzialmente leggibile)
         * 2. Il SecurityContext è ancora vuoto — nessun utente registrato per questa request
         *
         * Il controllo sul SecurityContext evita di rielaborare l'autenticazione
         * se un filtro precedente l'aveva già impostata (scenario raro ma possibile).
         *
         * COS'È IL SecurityContext?
         * È un oggetto ThreadLocal (uno per thread, quindi uno per request)
         * dove Spring memorizza l'utente autenticato durante l'elaborazione
         * di una singola request HTTP. NON è il context della Dependency Injection.
         * Viene azzerato automaticamente alla fine della request.
         */
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            /*
             * Carico i dettagli dell'utente dal database.
             * Questo mi dà la password hashata e le authorities (ruoli).
             * Serve per confrontare l'identità nel token con quella nel DB.
             */
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            try {
                /*
                 * Qui avviene la verifica COMPLETA del token:
                 * - firma crittografica corretta? (il token non è stato manomesso)
                 * - lo username nel token coincide con quello nel DB?
                 * - il token non è scaduto? ("exp")
                 * - il token è già attivo? ("nbf")
                 * - il claim "iat" non è nel futuro? (difesa da manomissioni)
                 * - il token non è più vecchio di 24 ore? (forza re-login)
                 *
                 * Se UNA SOLA di queste condizioni fallisce → token rifiutato.
                 */
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    /*
                     * Creo il token di autenticazione che Spring usa internamente.
                     * Parametri:
                     * - userDetails: chi è l'utente (username, ruoli)
                     * - null: credenziali (non servono, già verificate)
                     * - getAuthorities(): i permessi dell'utente (es. ROLE_BLOGGER)
                     */
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    /*
                     * Aggiungo dettagli extra sull'autenticazione:
                     * indirizzo IP del client, session ID, ecc.
                     * Utile per logging e audit trail.
                     */
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    /*
                     * REGISTRO L'AUTENTICAZIONE NEL SECURITY CONTEXT.
                     *
                     * Questo è il passo cruciale: da questo momento,
                     * per QUESTA request, l'utente è considerato loggato.
                     * Tutti i controlli successivi di Spring Security
                     * troveranno il SecurityContext popolato e lasceranno
                     * passare la request verso il controller.
                     *
                     * Il SecurityContext viene pulito automaticamente
                     * a fine request — ogni request riparte da zero.
                     * Questo è il cuore del concetto STATELESS.
                     */
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                // se isTokenValid restituisce false, non popolo il SecurityContext →
                // la request arriverà a SecurityConfig come non autenticata → 401

            } catch (Exception e) {
                /*
                 * Qualcosa è andato storto durante la validazione:
                 * token malformato, firma non valida, claims corrotti...
                 * Non registro nessuna autenticazione e passo avanti.
                 * SecurityConfig rifiuterà la request con 401.
                 */
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Passo al filtro successivo nella chain (e infine al controller)
        filterChain.doFilter(request, response);
    }
}
