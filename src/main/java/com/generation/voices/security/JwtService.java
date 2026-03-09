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

/*
 * ============================================================
 *  COS'È UN JWT (JSON Web Token)?
 * ============================================================
 * Un JWT è una stringa composta da tre parti separate da punti:
 *
 *   eyJhbGciOiJIUzI1NiJ9         ← HEADER  (Base64Url)
 *   .eyJpZCI6MSwidXNlcm5hbWUiOiJ  ← PAYLOAD (Base64Url)
 *   .7U3vux8QxnB3CuG23q7psYMJQ31  ← FIRMA   (HMAC-SHA256)
 *
 * HEADER: algoritmo usato per firmare (HS256) e tipo di token
 * PAYLOAD: i "claims", cioè i dati che vogliamo trasportare
 *           (username, ruolo, scadenza, ecc.)
 * FIRMA: hash crittografico di header+payload, firmato con il SECRET
 *
 * IMPORTANTE: header e payload sono solo codificati in Base64Url,
 * NON cifrati. Chiunque può leggerli su jwt.io.
 * La firma NON rende il token segreto — garantisce solo che
 * nessuno l'abbia modificato senza conoscere il SECRET.
 *
 * Quindi: non mettere mai dati sensibili nel payload (PIN, CVV, ecc.)
 * ============================================================
 *
 * PERCHÉ JWT INVECE DELLE SESSIONI?
 * - Il server non deve ricordare niente → stateless, scalabile
 * - Il token viaggia nel client (header Authorization)
 * - Funziona con qualsiasi client: browser, mobile, CLI, ecc.
 * - Può contenere dati utili (ruolo, nome) → meno query al DB
 * ============================================================
 */
@Service
public class JwtService {

    /*
     * La chiave segreta per firmare i token.
     * Viene iniettata da application.properties tramite @Value:
     *   jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
     *
     * Non è hardcodata nel codice sorgente per due motivi:
     * 1. Sicurezza: il codice può essere pubblicato su GitHub senza esporre la chiave
     * 2. Flessibilità: ogni ambiente (dev/staging/prod) può avere la sua chiave
     *
     * Se la chiave viene compromessa → tutti i token esistenti diventano falsi.
     * È l'equivalente digitale del sigillo reale: chi ha il sigillo può firmare.
     */
    @Value("${jwt.secret}")
    private String SECRET;

    /*
     * Limite di età massima del token in ore.
     * Anche se "exp" (expiration) non è scaduto, un token più vecchio
     * di 24 ore viene rifiutato — forza un re-login periodico.
     *
     * Perché questa doppia protezione?
     * "exp" controlla la scadenza tecnica del token.
     * MAX_TOKEN_AGE_HOURS è una politica di sicurezza aziendale:
     * anche se un token rubato non è ancora scaduto, dopo 24 ore
     * smette di funzionare comunque.
     */
    private static final int MAX_TOKEN_AGE_HOURS = 24;

    /*
     * ============================================================
     *  GENERAZIONE DEL TOKEN
     * ============================================================
     * Costruisce un JWT per l'utente dato, con tutti i suoi dati
     * nei claims e la firma crittografica con il SECRET.
     *
     * Il token generato conterrà nel payload (visibile a tutti):
     * - id, firstName, lastName, username, email, role
     * - sub (subject): username — identificativo principale
     * - iat (issued at): quando è stato emesso
     * - nbf (not before): da quando è valido (subito)
     * - exp (expiration): quando scade (tra 10 ore)
     */
    public String generateToken(PortalUser user) {
        // Validazione difensiva: meglio fallire subito con un messaggio chiaro
        // che produrre un token malformato che causa errori misteriosi dopo
        if (user == null)
            throw new IllegalArgumentException("User non può essere null");
        if (user.getUsername() == null || user.getUsername().isBlank())
            throw new IllegalArgumentException("Username obbligatorio per generare il token");
        if (user.getRole() == null)
            throw new IllegalArgumentException("Il ruolo dell'utente è obbligatorio");

        /*
         * Claims custom: dati aggiuntivi che mettiamo nel payload.
         * Il frontend potrà leggerli direttamente dal token
         * senza fare una chiamata extra al backend per sapere
         * chi è l'utente loggato o qual è il suo ruolo.
         *
         * Attenzione: questi dati sono visibili a chiunque decodifichi il token.
         * Non mettere qui password, PIN, o altri dati confidenziali.
         */
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());       // visibile nel token — non è un problema
        claims.put("role", user.getRole());          // usato dal frontend per mostrare/nascondere UI

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())              // "sub": identificativo standard del token
                .setIssuedAt(new Date(now))                  // "iat": momento di emissione
                .setNotBefore(new Date(now))                 // "nbf": valido da subito
                .setExpiration(new Date(now + 1000L * 60 * 60 * 10)) // "exp": scade tra 10 ore
                .signWith(getSignKey(), SignatureAlgorithm.HS256)     // firma HMAC-SHA256
                .compact();                                  // serializza in stringa "xxx.yyy.zzz"
    }

    /*
     * Estrae lo username dal claim "sub" (subject) del token.
     * Usato dal filtro JWT per capire chi sta facendo la request.
     * Non verifica ancora la firma — quella parte arriva dopo.
     */
    public String extractUsername(String token) {
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Token non può essere nullo o vuoto");
        return extractClaim(token, Claims::getSubject);
    }

    /*
     * Metodo generico per estrarre qualsiasi claim dal payload.
     * Usa una funzione come parametro (pattern Strategy):
     *
     *   extractClaim(token, Claims::getSubject)      → username
     *   extractClaim(token, Claims::getExpiration)   → data di scadenza
     *   extractClaim(token, Claims::getIssuedAt)     → data di emissione
     *
     * Questo evita di duplicare la logica di parsing per ogni singolo claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // decodifica + verifica firma
        return claimsResolver.apply(claims);            // estrae il campo richiesto
    }

    /*
     * ============================================================
     *  VALIDAZIONE COMPLETA DEL TOKEN
     * ============================================================
     * Questo metodo è il vero "guardiano" della sicurezza.
     * Controlla tutte le condizioni necessarie perché un token
     * sia considerato valido e l'utente possa essere autenticato.
     *
     * Ritorna true SOLO SE TUTTE le condizioni sono soddisfatte:
     *
     * 1. username nel token == username dell'utente nel DB
     *    → difesa da token emessi per utenti diversi
     *
     * 2. token non scaduto secondo "exp"
     *    → il token ha una vita limitata
     *
     * 3. token già attivo secondo "nbf"
     *    → difesa da token "schedulati" usati prima del previsto
     *
     * 4. "iat" non nel futuro
     *    → difesa da payload manomessi con date future
     *
     * 5. token non più vecchio di MAX_TOKEN_AGE_HOURS
     *    → politica di re-login periodico indipendente da "exp"
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || userDetails == null)
            return false;

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !isTokenNotYetValid(token)
                && !isIssuedAtInFuture(token)
                && !isTokenTooOld(token);
    }

    /*
     * Controlla se il token è scaduto confrontando "exp" con l'ora attuale.
     *
     * Il try/catch è intenzionale: se il token è malformato o la firma
     * non è valida, la libreria JJWT lancia un'eccezione durante il parsing.
     * In quel caso consideriamo il token scaduto/invalido → true = rifiutato.
     * Principio: in caso di dubbio, nega l'accesso.
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true; // token non parsabile = token non valido = accesso negato
        }
    }

    /*
     * Controlla il claim "nbf" (Not Before).
     * Il token non è valido prima di questa data.
     *
     * Caso d'uso: token emessi per attivazione via email con validità futura.
     * Per i nostri token di login, nbf == iat (valido subito),
     * quindi questo controllo passa sempre. Ma è buona pratica tenerlo.
     */
    private boolean isTokenNotYetValid(String token) {
        Date notBefore = extractClaim(token, Claims::getNotBefore);
        if (notBefore == null) return false; // claim assente → non applichiamo il controllo
        return notBefore.getTime() > System.currentTimeMillis();
    }

    /*
     * Controlla se "iat" (issued at) è nel futuro.
     *
     * Un token con "iat" nel futuro è anomalo: significa che qualcuno
     * ha modificato il payload per far sembrare il token più nuovo di quanto è.
     * Questo potrebbe essere un tentativo di aggirare il controllo MAX_TOKEN_AGE_HOURS.
     * Principio: non fidarsi di nessun dato che non ha senso logico.
     */
    private boolean isIssuedAtInFuture(String token) {
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        if (issuedAt == null) return false;
        return issuedAt.getTime() > System.currentTimeMillis();
    }

    /*
     * Controlla se il token è "troppo vecchio" secondo la nostra politica.
     *
     * Scenario: un token ha "exp" tra 10 ore (come configurato in generateToken),
     * ma se è stato emesso 23 ore fa, è "vecchio" anche se tecnicamente non ancora scaduto.
     * Questo controllo aggiuntivo forza il re-login ogni 24 ore al massimo.
     *
     * Se "iat" non è presente nel token, lo rifiutiamo per sicurezza:
     * un token senza "iat" non può essere controllato sull'età → meglio non fidarsi.
     */
    private boolean isTokenTooOld(String token) {
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        if (issuedAt == null) return true; // nessun "iat" → token sospetto → rifiutato
        long maxAgeMillis = (long) MAX_TOKEN_AGE_HOURS * 60 * 60 * 1000;
        return (System.currentTimeMillis() - issuedAt.getTime()) > maxAgeMillis;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /*
     * Decodifica il token e verifica la firma crittografica.
     *
     * parseClaimsJws() (con la "s" finale, sta per "signed"):
     * - decodifica header e payload da Base64Url
     * - ricalcola l'HMAC-SHA256 di header+payload con il nostro SECRET
     * - confronta il risultato con la firma nel token
     * - se non combaciano → SignatureException → token rifiutato
     *
     * NON usare parseClaimsJwt() (senza "s"): accetterebbe token
     * senza firma (con alg:none), aprendo una grave falla di sicurezza.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey()) // chiave per verificare la firma
                .build()
                .parseClaimsJws(token)       // decodifica + verifica firma → eccezione se invalido
                .getBody();                  // restituisce solo il payload (Claims)
    }

    /*
     * Converte il SECRET (stringa esadecimale codificata in Base64) in un oggetto Key
     * pronto per HMAC-SHA256.
     *
     * Il SECRET in application.properties è una stringa Base64 che rappresenta
     * 32 byte (256 bit) — il minimo richiesto dall'algoritmo HS256.
     * Decoders.BASE64.decode() la converte in un array di byte,
     * e Keys.hmacShaKeyFor() costruisce l'oggetto Key appropriato.
     *
     * Perché non usare direttamente la stringa come chiave?
     * Una stringa in UTF-8 potrebbe avere meno entropia dei byte grezzi.
     * La codifica Base64 è solo un modo sicuro per trasportare dati binari
     * in un file di configurazione testuale.
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
