package com.generation.voices.security;

import com.generation.voices.model.PortalUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Un JWT è tre stringhe Base64Url separate da punti: header.payload.firma.
// Header e payload sono leggibili da chiunque — non sono cifrati.
// La firma è l'unica cosa che garantisce che il token non sia stato manomesso:
// senza il SECRET non si può ricalcolare, quindi non si può falsificare.
// Conseguenza: nel payload non vanno mai dati sensibili (password, PIN, ecc.).
//
// Ho scelto JWT invece delle sessioni perché il server non deve ricordare niente:
// ogni request porta con sé tutto il necessario per verificare chi è l'utente.
@Service
public class JwtService
{
    // Il SECRET viene iniettato da application.properties, non è hardcodato nel sorgente:
    // così il codice può stare su GitHub senza esporre la chiave.
    // Se il SECRET viene compromesso, tutti i token esistenti vanno considerati falsi.
    @Value("${jwt.secret}")
    private String SECRET;

    // "exp" da solo non basta: un token rubato è valido fino alla sua scadenza naturale.
    // Aggiungo un limite di età assoluto: dopo 24 ore il token viene rifiutato
    // indipendentemente da "exp", forzando il re-login.
    private static final int MAX_TOKEN_AGE_HOURS = 24;

    // Costruisce il JWT per l'utente: metto nel payload i dati che il frontend
    // usa spesso (id, ruolo, nome) così non deve fare chiamate extra solo per sapere
    // chi è loggato. La scadenza tecnica è 10 ore, ma isTokenTooOld() la taglia a 24.
    public String generateToken(PortalUser user)
    {
        if (user == null)
            throw new IllegalArgumentException("User non può essere null");
        if (user.getUsername() == null || user.getUsername().isBlank())
            throw new IllegalArgumentException("Username obbligatorio per generare il token");
        if (user.getRole() == null)
            throw new IllegalArgumentException("Il ruolo dell'utente è obbligatorio");

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(now))                         // "iat": quando è stato emesso
                .setNotBefore(new Date(now))                        // "nbf": valido da subito
                .setExpiration(new Date(now + 1000L * 60 * 60 * 10)) // "exp": scade tra 10 ore
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Estrae lo username dal claim "sub" — non verifica ancora la firma,
    // quella parte avviene in extractAllClaims().
    public String extractUsername(String token)
    {
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Token non può essere nullo o vuoto");
        return extractClaim(token, Claims::getSubject);
    }

    // Metodo generico per leggere qualsiasi claim senza duplicare la logica di parsing.
    // Esempio: extractClaim(token, Claims::getExpiration) → data di scadenza.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Verifica che il token sia valido sotto tutti gli aspetti:
    // 1. username nel token == username nel DB (difesa da token emessi per altri utenti)
    // 2. "exp" non scaduto
    // 3. "nbf" già superato (il token è già attivo)
    // 4. "iat" non nel futuro (difesa da payload manomessi)
    // 5. età del token entro MAX_TOKEN_AGE_HOURS
    // Basta che una sola condizione fallisca per rifiutare il token.
    public boolean isTokenValid(String token, UserDetails userDetails)
    {
        if (token == null || userDetails == null)
            return false;

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !isTokenNotYetValid(token)
                && !isIssuedAtInFuture(token)
                && !isTokenTooOld(token);
    }

    // Se il parsing fallisce (token malformato, firma non valida) catturo l'eccezione
    // e restituisco true = scaduto: in caso di dubbio nego l'accesso.
    private boolean isTokenExpired(String token)
    {
        try
        {
            return extractExpiration(token).before(new Date());
        }
        catch (Exception e)
        {
            return true;
        }
    }

    // "nbf" (Not Before): il token non è valido prima di questa data.
    // Per i nostri token di login nbf == iat quindi passa sempre,
    // ma lo tengo perché è parte del contratto standard JWT.
    private boolean isTokenNotYetValid(String token)
    {
        Date notBefore = extractClaim(token, Claims::getNotBefore);
        if (notBefore == null)
            return false;
        return notBefore.getTime() > System.currentTimeMillis();
    }

    // "iat" nel futuro è anomalo: qualcuno ha modificato il payload per far sembrare
    // il token più recente di quanto è, probabilmente per aggirare isTokenTooOld().
    private boolean isIssuedAtInFuture(String token)
    {
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        if (issuedAt == null) return false;
        return issuedAt.getTime() > System.currentTimeMillis();
    }

    // Un token senza "iat" non posso controllarne l'età: lo rifiuto per sicurezza.
    // Altrimenti confronto l'età effettiva con MAX_TOKEN_AGE_HOURS.
    private boolean isTokenTooOld(String token)
    {
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        if (issuedAt == null) return true;
        long maxAgeMillis = (long) MAX_TOKEN_AGE_HOURS * 60 * 60 * 1000;
        return (System.currentTimeMillis() - issuedAt.getTime()) > maxAgeMillis;
    }

    private Date extractExpiration(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }

    // parseClaimsJws() (con la "s" = signed) decodifica il token e verifica la firma:
    // ricalcola l'HMAC-SHA256 e lo confronta con quello nel token.
    // Se non coincidono lancia SignatureException — il token è stato manomesso.
    // Non uso parseClaimsJwt() (senza "s"): accetterebbe token non firmati (alg:none),
    // che è una falla di sicurezza classica.
    private Claims extractAllClaims(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Il SECRET in application.properties è codificato in Base64 per poter rappresentare
    // 32 byte binari in un file di testo. Lo decodifico e costruisco la chiave HMAC.
    // Non uso la stringa direttamente: in UTF-8 potrebbe avere meno entropia dei byte grezzi.
    private Key getSignKey()
    {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
