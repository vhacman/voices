package com.generation.voices.security;

import com.generation.voices.model.PortalUser;
import com.generation.voices.repository.PortalUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 *  PERCHÉ ESISTE QUESTA CLASSE? 
 *
 * Spring Security ha bisogno di un modo per caricare i dati
 * di un utente dato il suo username. Lo fa tramite l'interfaccia
 * UserDetailsService, che definisce un solo metodo: loadUserByUsername().
 *
 * Spring Security chiama questo metodo automaticamente durante
 * il processo di autenticazione per recuperare i dati aggiornati
 * dell'utente dal database — e confrontarli con quanto dichiarato nel JWT.
 *
 * Noi dobbiamo fornire l'implementazione concreta ("custom") perché
 * Spring non sa dove sono i nostri utenti: potrebbero essere in memoria,
 * in un LDAP, in un database NoSQL, ecc.
 * Noi li teniamo in MySQL tramite JPA → quindi usiamo PortalUserRepository.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PortalUserRepository portalUserRepository;

    /*
     * Carica un PortalUser dal database dato il suo username,
     * e lo converte in un oggetto UserDetails che Spring Security capisce.
     *
     * UserDetails è l'astrazione di Spring per "un utente autenticabile":
     * contiene username, password hashata, e authorities (ruoli/permessi).
     * Spring non sa niente del nostro PortalUser — gli serve UserDetails.
     *
     * Perché ricaricare l'utente dal DB ogni request invece di usare i dati nel token?
     * Il token potrebbe essere stato emesso ore fa. Nel frattempo l'utente
     * potrebbe essere stato disabilitato, o il suo ruolo potrebbe essere cambiato.
     * Ricaricare dal DB garantisce che lavoriamo sempre con dati aggiornati.
     *
     * Se l'username non esiste nel DB lanciamo UsernameNotFoundException:
     * Spring Security la intercetta e tratta la request come non autenticata.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PortalUser user = portalUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));

        /*
         * Costruiamo uno UserDetails "standard" di Spring Security con il builder.
         *
         * .username()  → lo username (identifica l'utente)
         * .password()  → la password hashata con BCrypt dal DB
         *               Spring la userà per confrontarla in fase di login
         * .roles()     → il ruolo del nostro PortalUser (es. BLOGGER, ADMIN)
         *               Spring aggiunge automaticamente il prefisso "ROLE_":
         *               BLOGGER → ROLE_BLOGGER
         *               ADMIN   → ROLE_ADMIN
         *               Questo prefisso è una convenzione di Spring Security.
         */
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().toString())
                .build();
    }
}
