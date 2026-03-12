package com.generation.voices.service;

import com.generation.voices.dto.ChangePasswordDTO;
import com.generation.voices.dto.PortalUserDTO;
import com.generation.voices.dto.RegisterDTO;
import com.generation.voices.mapper.PortalUserMapper;
import com.generation.voices.model.PortalUser;
import com.generation.voices.model.enumerations.Role;
import com.generation.voices.repository.PortalUserRepository;
import com.generation.voices.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PortalUserService
{

    // Scadenza password: 2 settimane per ADMIN (più sensibili), 3 mesi per BLOGGER.
    private static final int PASSWORD_EXPIRY_ADMIN_DAYS = 14;
    private static final int PASSWORD_EXPIRY_BLOGGER_DAYS = 90;

    @Autowired
    private PortalUserRepository portalUserRepository;

    @Autowired
    private PortalUserMapper portalUserMapper;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JwtService jwtService;

    public List<PortalUserDTO> findAll()
    {
        return portalUserMapper.toDTOs(portalUserRepository.findAll());
    }

    public PortalUserDTO findById(Integer id)
    {
        PortalUser user = portalUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PortalUser non trovato con id: " + id));
        return portalUserMapper.toDTO(user);
    }

    // Registrazione pubblica: chiunque può crearsi un account.
    // Imposto lastPasswordChange a oggi così al primo login il timer parte subito.
    // Il ruolo di default è BLOGGER: solo un ADMIN può promuovere qualcuno ad ADMIN.
    public PortalUserDTO register(@Valid RegisterDTO registerDTO)
    {
        registerDTO.setPassword(passwordHasher.toMD5(registerDTO.getPassword()));
        PortalUser user = portalUserMapper.toEntity(registerDTO);
        user.setRole(Role.BLOGGER);
        user.setLastPasswordChange(LocalDate.now());
        user = portalUserRepository.save(user);
        return portalUserMapper.toDTO(user);
    }

    // save() è riservato agli ADMIN: possono scegliere il ruolo e non
    // viene forzato BLOGGER come nella registrazione pubblica.
    public PortalUserDTO save(@Valid RegisterDTO registerDTO)
    {
        registerDTO.setPassword(passwordHasher.toMD5(registerDTO.getPassword()));
        PortalUser user = portalUserMapper.toEntity(registerDTO);
        user.setLastPasswordChange(LocalDate.now());
        user = portalUserRepository.save(user);
        return portalUserMapper.toDTO(user);
    }

    public PortalUserDTO update(Integer id, @Valid RegisterDTO registerDTO)
    {
        portalUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PortalUser non trovato con id: " + id));
        registerDTO.setPassword(passwordHasher.toMD5(registerDTO.getPassword()));
        PortalUser user = portalUserMapper.toEntity(registerDTO);
        user.setId(id);
        user = portalUserRepository.save(user);
        return portalUserMapper.toDTO(user);
    }

    public void deleteById(Integer id)
    {
        portalUserRepository.deleteById(id);
    }

    // Cambio password: aggiorno l'hash e resetto lastPasswordChange a oggi.
    // L'id arriva dall'URL, non dal token — il controller deve assicurarsi
    // che l'utente possa modificare solo se stesso (o essere ADMIN).
    public void changePassword(Integer id, @Valid ChangePasswordDTO dto)
    {
        PortalUser user = portalUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PortalUser non trovato con id: " + id));
        user.setPassword(passwordHasher.toMD5(dto.getNewPassword()));
        // Resetto il timer: dalla data di oggi ripartono i 14 o 90 giorni.
        user.setLastPasswordChange(LocalDate.now());
        portalUserRepository.save(user);
    }

    // Controlla se la password dell'utente è scaduta in base al suo ruolo.
    // Restituisce true se deve cambiare password, false se è ancora valida.
    private boolean isPasswordExpired(PortalUser user)
    {
        if (user.getLastPasswordChange() == null)
            return true; // se non c'è data → tratto come scaduta per sicurezza

        int expiryDays = user.getRole() == Role.ADMIN
                ? PASSWORD_EXPIRY_ADMIN_DAYS
                : PASSWORD_EXPIRY_BLOGGER_DAYS;

        return user.getLastPasswordChange()
                .plusDays(expiryDays)
                .isBefore(LocalDate.now());
    }

    // Login: verifica credenziali, genera JWT e controlla scadenza password.
    // mustChangePassword = true forza il frontend a mandare l'utente sulla
    // schermata di cambio password prima di qualsiasi altra azione.
    public Map<String, String> login(String username, String password)
    {
        PortalUser user = portalUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenziali non valide"));

        if (!passwordHasher.toMD5(password).equals(user.getPassword()))
            throw new IllegalArgumentException("Credenziali non valide");

        String token = jwtService.generateToken(user);

        // Uso HashMap invece di Map.of() perché Map.of() non accetta valori boolean —
        // accetta solo Object, ma il tipo inferito qui è Map<String,String>.
        // Converto mustChangePassword in stringa: il frontend farà il parse.
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", String.valueOf(user.getId()));
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("mustChangePassword", String.valueOf(isPasswordExpired(user)));
        return response;
    }
}
