// Creato il 06/03/2026
package com.generation.voices.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.generation.voices.dto.BlogPostDTO;
import com.generation.voices.model.BlogPost;

// uses = {BlogMapper.class, CommentMapper.class}: BlogPostDTO ha due oggetti annidati:
// - blog (Blog → BlogDTO): delegato a BlogMapper
// - comments (List<Comment> → List<CommentDTO>): delegato a CommentMapper
// MapStruct non genera mapper per oggetti annidati da solo: devo dirgli esplicitamente
// quali mapper usare, altrimenti errore di compilazione.
@Mapper(componentModel = "spring", uses = {BlogMapper.class, CommentMapper.class})
public interface BlogPostMapper
{

    // Tutti i campi hanno lo stesso nome tra entità e DTO → nessun @Mapping necessario.
    // blog (Blog) → blog (BlogDTO): gestito da BlogMapper.
    // comments (List<Comment>) → comments (List<CommentDTO>): gestito da CommentMapper.
    BlogPostDTO toDTO(BlogPost blogPost);

    List<BlogPostDTO> toDTOs(List<BlogPost> blogPosts);

    BlogPost toEntity(BlogPostDTO blogPostDTO);

    List<BlogPost> toEntities(List<BlogPostDTO> blogPostDTOs);

}
