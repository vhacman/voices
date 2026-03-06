// Creato il 06/03/2026
package com.generation.voices.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.generation.voices.dto.BlogPostDTO;
import com.generation.voices.model.BlogPost;

@Mapper(componentModel = "spring")
public interface BlogPostMapper {

    // BlogPost -> BlogPostDTO: mappa blog.id nel campo blogId del DTO
    @Mapping(source = "blog.id", target = "blogId")
    BlogPostDTO toDTO(BlogPost blogPost);

    List<BlogPostDTO> toDTOs(List<BlogPost> blogPosts);

    // BlogPostDTO -> BlogPost: mappa blogId nel campo blog.id dell'entità
    @Mapping(source = "blogId", target = "blog.id")
    BlogPost toEntity(BlogPostDTO blogPostDTO);

    List<BlogPost> toEntities(List<BlogPostDTO> blogPostDTOs);

}
