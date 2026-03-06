// Creato il 06/03/2026
package com.generation.voices.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.generation.voices.dto.BlogDTO;
import com.generation.voices.model.Blog;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    // Blog -> BlogDTO: mappa author.id nel campo authorId del DTO
    @Mapping(source = "author.id", target = "authorId")
    BlogDTO toDTO(Blog blog);

    List<BlogDTO> toDTOs(List<Blog> blogs);

    // BlogDTO -> Blog: mappa authorId nel campo author.id dell'entità
    @Mapping(source = "authorId", target = "author.id")
    Blog toEntity(BlogDTO blogDTO);

    List<Blog> toEntities(List<BlogDTO> blogDTOs);

}
