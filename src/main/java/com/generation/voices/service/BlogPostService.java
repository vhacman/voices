// Creato il 06/03/2026
package com.generation.voices.service;

import com.generation.voices.dto.BlogPostDTO;
import com.generation.voices.mapper.BlogPostMapper;
import com.generation.voices.model.BlogPost;
import com.generation.voices.repository.BlogPostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

// @Validated a livello di classe abilita la validazione dei parametri annotati con @Valid.
// Senza @Validated, le annotazioni @Valid sui parametri dei metodi vengono ignorate.
@Service
@Validated
public class BlogPostService
{
    // Spring inietta automaticamente le dipendenze grazie a @Autowired.
    // Non istanzio mai repository e mapper con "new": ci pensa Spring a creare
    // e gestire questi oggetti nel suo contesto (IoC - Inversion of Control).
    @Autowired
    private BlogPostRepository blogPostRepository;
    @Autowired
    private BlogPostMapper blogPostMapper;

    // findAll() non ha bisogno di gestire eccezioni: se non ci sono post restituisce lista vuota,
    // non lancia errori. Il mapper converte la lista di entità in lista di DTO in un colpo solo.
    public List<BlogPostDTO> findAll()
    {
        return blogPostMapper.toDTOs(blogPostRepository.findAll());
    }

    // findById usa orElseThrow: se l'id non esiste nel DB lancia EntityNotFoundException
    // invece di restituire null. Il controller la cattura e risponde con 404.
    // È più esplicito e sicuro che fare un controllo manuale su null.
    public BlogPostDTO findById(Integer id)
    {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost not found with id: " + id));
        return blogPostMapper.toDTO(blogPost);
    }

    // Ritorna tutti i post di un blog specifico.
    // Usato dall'API GET /posts/blog/{blogId} per caricare i post di un singolo blog.
    // La query è generata da Spring Data JPA tramite il nome del metodo nel repository.
    public List<BlogPostDTO> findByBlogId(int blogId)
    {
        return blogPostMapper.toDTOs(blogPostRepository.findByBlogId(blogId));
    }

    // @Valid attiva le annotazioni di validazione definite nel DTO (es. @NotEmpty, @NotNull).
    // Se il DTO non supera la validazione, Spring lancia ConstraintViolationException
    // prima ancora di entrare nel corpo del metodo → il controller risponde 400.
    public BlogPostDTO save(@Valid BlogPostDTO blogPostDTO)
    {
        BlogPost blogPost = blogPostMapper.toEntity(blogPostDTO);
        blogPost = blogPostRepository.save(blogPost);
        return blogPostMapper.toDTO(blogPost);
    }

    public BlogPostDTO update(Integer id, @Valid BlogPostDTO blogPostDTO)
    {
        // Verifico che il post esista prima di aggiornare: se non c'è lancio 404.
        // Non uso direttamente il risultato della findById perché il mapper
        // ricostruisce l'entità dal DTO da zero, non parte dall'entità esistente.
        blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost not found with id: " + id));
        BlogPost blogPost = blogPostMapper.toEntity(blogPostDTO);
        // Devo impostare l'id manualmente: il DTO non lo porta (o non ci fidiamo di quello che manda il client).
        // Con l'id corretto, JPA capisce che deve fare un UPDATE e non un INSERT.
        blogPost.setId(id);
        blogPost = blogPostRepository.save(blogPost);
        return blogPostMapper.toDTO(blogPost);
    }

    public void deleteById(Integer id)
    {
        blogPostRepository.deleteById(id);
    }

    // Incrementa il contatore di visualizzazioni di 1 e restituisce il post aggiornato.
    // L'ho separato da findById perché è un'operazione di scrittura (modifica il DB),
    // non voglio che un semplice GET incrementi il contatore: serve una chiamata esplicita
    // da parte del frontend (POST /{id}/view) quando l'utente apre davvero il post.
    public BlogPostDTO incrementViewCount(Integer id)
    {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost not found with id: " + id));
        // getView() + 1: leggo il valore attuale dal DB e aggiungo 1.
        // Non uso una query SQL di incremento diretta per restare coerente
        // con il pattern usato nel resto del service (leggi → modifica → salva).
        blogPost.setView(blogPost.getView() + 1);
        blogPost = blogPostRepository.save(blogPost);
        return blogPostMapper.toDTO(blogPost);
    }
}
