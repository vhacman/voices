package com.generation.voices.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.generation.voices.dto.CommentDTO;
import com.generation.voices.model.Comment;

// MapStruct genera automaticamente il codice di conversione a compile time.
// componentModel = "spring" fa sì che MapStruct produca un @Component
// che Spring può iniettare con @Autowired nei service.
@Mapper(componentModel = "spring")
public interface CommentMapper {

    // Quando converto da Comment a CommentDTO ho bisogno di "appiattire" la struttura:
    // comment.post è un oggetto BlogPost, ma nel DTO voglio solo comment.post.id → postId.
    // La sintassi "post.id" dice a MapStruct di navigare dentro l'oggetto annidato.
    // Stesso ragionamento per author.id → authorId.
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "author.id", target = "authorId")
    CommentDTO toDTO(Comment comment);

    // toDTOs non ha bisogno di @Mapping: MapStruct riutilizza in automatico
    // il metodo toDTO() già definito sopra per ogni elemento della lista.
    List<CommentDTO> toDTOs(List<Comment> comments);

    // Direzione inversa: dal DTO ricostruisco l'entità.
    // postId (int) → comment.post.id: MapStruct crea un oggetto BlogPost vuoto
    // con solo l'id valorizzato. È sufficiente perché JPA usa solo l'id
    // per stabilire la foreign key in fase di save().
    // Stesso meccanismo per authorId → comment.author.id.
    @Mapping(source = "postId", target = "post.id")
    @Mapping(source = "authorId", target = "author.id")
    Comment toEntity(CommentDTO dto);

}
