package com.generation.voices.service;

import com.generation.voices.dto.PortalUserDTO;
import com.generation.voices.dto.RegisterDTO;
import com.generation.voices.mapper.PortalUserMapper;
import com.generation.voices.model.PortalUser;
import com.generation.voices.repository.PortalUserRepository;
import com.generation.voices.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PortalUserService
{

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

    /*
     * Ho separato il DTO di input (RegisterDTO) da quello di output (PortalUserDTO).
     * Prima usavo un solo PortalUserDTO per tutto, ma questo mi obbligava a mettere
     * la password nel DTO di risposta — un problema di sicurezza.
     * Ora il client manda un RegisterDTO (con password in chiaro) e riceve indietro
     * un PortalUserDTO (senza password), generato dal mapper partendo dall'entità salvata.
     * La password viene hashata qui nel service, prima della conversione in entità,
     * così nel DB arriva sempre l'hash MD5 e mai la password in chiaro.
     */
    public PortalUserDTO save(@Valid RegisterDTO registerDTO)
    {
        registerDTO.setPassword(passwordHasher.toMD5(registerDTO.getPassword()));
        PortalUser user = portalUserMapper.toEntity(registerDTO);
        user = portalUserRepository.save(user);
        return portalUserMapper.toDTO(user);
    }

    /*
     * Stesso ragionamento di save(): ricevo RegisterDTO in ingresso (con password),
     * forzo l'id sull'entità per fare un UPDATE invece di un INSERT,
     * e restituisco PortalUserDTO senza password.
     */
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

    /*
     * Autentica un utente verificando username e password.
     * La password ricevuta viene hashata in MD5 e confrontata
     * con l'hash salvato nel DB.
     * Se le credenziali sono valide, genera e restituisce un JWT.
     */
    public Map<String, String> login(String username, String password)
    {
        PortalUser user = portalUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenziali non valide"));

        if (!passwordHasher.toMD5(password).equals(user.getPassword()))
            throw new IllegalArgumentException("Credenziali non valide");

        String token = jwtService.generateToken(user);
        return Map.of("token", token);
    }
}
