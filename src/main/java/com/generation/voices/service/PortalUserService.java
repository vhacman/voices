package com.generation.voices.service;

import com.generation.voices.dto.PortalUserDTO;
import com.generation.voices.mapper.PortalUserMapper;
import com.generation.voices.model.PortalUser;
import com.generation.voices.repository.PortalUserRepository;
import com.generation.voices.security.JwtService;
import com.generation.voices.security.PasswordHasher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PortalUserService {

    @Autowired
    private PortalUserRepository portalUserRepository;

    @Autowired
    private PortalUserMapper portalUserMapper;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JwtService jwtService;

    public List<PortalUserDTO> findAll() {
        return portalUserMapper.toDTOs(portalUserRepository.findAll());
    }

    public PortalUserDTO findById(Integer id) {
        PortalUser user = portalUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PortalUser non trovato con id: " + id));
        return portalUserMapper.toDTO(user);
    }

    public PortalUserDTO save(@Valid PortalUserDTO userDTO) {
        userDTO.setPassword(passwordHasher.toMD5(userDTO.getPassword()));
        PortalUser user = portalUserMapper.toEntity(userDTO);
        user = portalUserRepository.save(user);
        return portalUserMapper.toDTO(user);
    }

    public PortalUserDTO update(Integer id, @Valid PortalUserDTO userDTO) {
        portalUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PortalUser non trovato con id: " + id));
        userDTO.setPassword(passwordHasher.toMD5(userDTO.getPassword()));
        PortalUser user = portalUserMapper.toEntity(userDTO);
        user.setId(id);
        user = portalUserRepository.save(user);
        return portalUserMapper.toDTO(user);
    }

    public void deleteById(Integer id) {
        portalUserRepository.deleteById(id);
    }

    /*
     * Autentica un utente verificando username e password.
     * La password ricevuta viene hashata in MD5 e confrontata
     * con l'hash salvato nel DB.
     * Se le credenziali sono valide, genera e restituisce un JWT.
     */
    public Map<String, String> login(String username, String password) {
        PortalUser user = portalUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenziali non valide"));

        if (!passwordHasher.toMD5(password).equals(user.getPassword()))
            throw new IllegalArgumentException("Credenziali non valide");

        String token = jwtService.generateToken(user);
        return Map.of("token", token);
    }
}
