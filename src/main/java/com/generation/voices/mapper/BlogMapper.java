// Creato il 06/03/2026
package com.generation.voices.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.generation.voices.dto.BlogDTO;
import com.generation.voices.model.Blog;

// uses = PortalUserMapper.class: quando MapStruct deve convertire PortalUser → PortalUserDTO
// (il campo author), riutilizza il mapper già esistente invece di generarne uno nuovo.
// Senza "uses", MapStruct non saprebbe come mappare l'oggetto annidato e darebbe errore a compile time.
@Mapper(componentModel = "spring", uses = PortalUserMapper.class)
public interface BlogMapper {

    // Tutti i campi hanno lo stesso nome tra entità e DTO → nessun @Mapping necessario.
    // author (PortalUser) → author (PortalUserDTO): gestito automaticamente da PortalUserMapper.
    BlogDTO toDTO(Blog blog);

    List<BlogDTO> toDTOs(List<Blog> blogs);

    Blog toEntity(BlogDTO blogDTO);

    List<Blog> toEntities(List<BlogDTO> blogDTOs);

}
