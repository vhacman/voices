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

// Questo filtro gira a ogni request e ha un solo compito:
// leggere il JWT dall'header Authorization, verificarlo,
// e se è valido dire a Spring chi sta facendo la chiamata.
// OncePerRequestFilter garantisce che non venga eseguito due volte
// per la stessa request (può succedere con forward/include interni).
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{

    @Autowired
    private JwtService jwtService;

    // Ricarico l'utente dal DB a ogni request invece di fidarmi solo del token:
    // se nel frattempo è stato disabilitato o il ruolo è cambiato, lo voglio sapere subito.
    @Autowired
    private UserDetailsService userDetailsService;

    // Tre scenari possibili:
    // 1. Nessun header Authorization → non è una request autenticata via JWT,
    //    passo avanti e lascia decidere a SecurityConfig.
    // 2. Token valido → popolo il SecurityContext, la request arriva al controller come autenticata.
    // 3. Token scaduto/manomesso/invalido → non popolo niente, SecurityConfig risponde 401.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Niente header o formato sbagliato → non è una request JWT, vado avanti.
        // Sarà SecurityConfig a decidere se la route è pubblica o richiede login.
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            filterChain.doFilter(request, response);
            return;
        }

        // Tolgo "Bearer " (7 caratteri) e tengo solo il token grezzo.
        jwt = authHeader.substring(7);

        // Leggo lo username dal payload del token — ancora senza verificare la firma.
        username = jwtService.extractUsername(jwt);

        // Entro qui solo se ho uno username e il SecurityContext è ancora vuoto.
        // Il secondo controllo evita di rielaborare un'autenticazione già impostata
        // da un filtro precedente nella chain.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            try
            {
                // Verifica completa: firma, scadenza, username, età massima del token.
                // Se anche una sola condizione fallisce, il token viene rifiutato.
                if (jwtService.isTokenValid(jwt, userDetails))
                {
                    // Creo il token interno di Spring con le authorities dell'utente.
                    // Le credenziali (secondo parametro) sono null: non servono più,
                    // l'identità è già stata verificata dal JWT.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    // Aggiungo IP e session ID alla request: utile per audit e logging.
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Da questo momento, per questa request, l'utente è autenticato.
                    // Spring troverà il SecurityContext popolato e lascerà passare la request.
                    // Il context viene azzerato automaticamente a fine request: ogni chiamata
                    // riparte da zero, nessuno stato lato server — è questo che rende il sistema stateless.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                // isTokenValid false → SecurityContext rimane vuoto → 401
            }
            catch (Exception e)
            {
                // Token malformato o claim corrotti: non autentico niente, vado avanti.
                // SecurityConfig penserà a rispondere con 401.
                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
