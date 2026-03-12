package com.generation.voices.security;

import com.generation.voices.model.PortalUser;
import com.generation.voices.repository.PortalUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security non sa dove tengo i miei utenti — potrebbero essere ovunque.
// Devo dirgli io come recuperarli implementando UserDetailsService,
// che ha un solo metodo: loadUserByUsername().
// Spring lo chiama da solo durante l'autenticazione.
@Service
public class CustomUserDetailsService implements UserDetailsService
{

    @Autowired
    private PortalUserRepository portalUserRepository;

    // Spring mi chiede un utente per username e si aspetta un UserDetails,
    // non il mio PortalUser — quindi devo convertirlo.
    // Ricarico dal DB a ogni request invece di fidarmi del token:
    // se nel frattempo l'utente è stato disabilitato o il ruolo è cambiato,
    // voglio che si veda subito, non alla scadenza del token.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        PortalUser user = portalUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));
        // .roles() aggiunge automaticamente il prefisso "ROLE_" che Spring si aspetta:
        // ADMIN → ROLE_ADMIN, BLOGGER → ROLE_BLOGGER.
        // È questo che poi fa funzionare hasRole("ADMIN") nella SecurityConfig.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().toString())
                .build();
    }
}
