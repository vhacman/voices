package com.generation.voices.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.generation.voices.dto.CommentDTO;
import com.generation.voices.model.Comment;

// uses = PortalUserMapper.class: author (PortalUser → PortalUserDTO) viene gestito
// automaticamente da PortalUserMapper, inclusa la traduzione username → nickname.
@Mapper(componentModel = "spring", uses = PortalUserMapper.class)
public interface CommentMapper
{

    // L'unico campo che richiede @Mapping è postId: nel DTO è un int flat,
    // nell'entità è un oggetto BlogPost. MapStruct non può inferirlo da solo.
    // author e tutti gli altri campi hanno lo stesso nome → nessun mapping extra.
    @Mapping(source = "post.id", target = "postId")
    CommentDTO toDTO(Comment comment);

    List<CommentDTO> toDTOs(List<Comment> comments);

    // Direzione inversa: postId (int) → post.id nell'entità.
    // MapStruct crea un BlogPost con solo l'id valorizzato,
    // sufficiente per la FK in JPA durante il save().
    @Mapping(source = "postId", target = "post.id")
    Comment toEntity(CommentDTO dto);

    List<Comment> toEntities(List<CommentDTO> dtos);

}
