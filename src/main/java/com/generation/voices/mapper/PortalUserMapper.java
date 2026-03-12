package com.generation.voices.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.generation.voices.dto.PortalUserDTO;
import com.generation.voices.dto.RegisterDTO;
import com.generation.voices.model.PortalUser;

// PortalUser ha il campo "username" ma il frontend (e il prof) si aspetta "nickname".
// Invece di rinominare il campo nel DB o nell'entità, uso @Mapping per fare la traduzione
// solo a livello di DTO: il DB rimane invariato, il frontend riceve quello che si aspetta.
@Mapper(componentModel = "spring")
public interface PortalUserMapper
{

    // username nell'entità → nickname nel DTO (il prof chiama così il campo nel frontend)
    @Mapping(source = "username", target = "nickname")
    PortalUserDTO toDTO(PortalUser portalUser);

    List<PortalUserDTO> toDTOs(List<PortalUser> portalUsers);

    // Direzione inversa: nickname nel DTO → username nell'entità.
    // PortalUserDTO non ha firstName, lastName, dob, password → li ignoro esplicitamente
    // per evitare warning di MapStruct. Questo metodo serve solo per oggetti annidati
    // (es. Blog.author) dove quegli extra campi non servono.
    @Mapping(source = "nickname", target = "username")
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "dob", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastPasswordChange", ignore = true)
    PortalUser toEntity(PortalUserDTO portalUserDTO);

    List<PortalUser> toEntities(List<PortalUserDTO> portalUserDTOs);

    // Converte il DTO di registrazione (con password) in entità.
    // Usato solo in save() e update() nel service.
    // La password viene hashata nel service PRIMA di chiamare questo metodo.
    @Mapping(source = "nickname", target = "username")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastPasswordChange", ignore = true)
    PortalUser toEntity(RegisterDTO registerDTO);

}
