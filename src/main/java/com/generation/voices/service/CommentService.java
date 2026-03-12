package com.generation.voices.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.generation.voices.dto.CommentDTO;
import com.generation.voices.mapper.CommentMapper;
import com.generation.voices.model.Comment;
import com.generation.voices.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

// Struttura identica a BlogPostService e BlogService:
// @Service + @Validated + @Autowired repository e mapper.
// Il service è l'unico posto dove metto logica di business;
// il controller si limita a chiamare questi metodi e a gestire le risposte HTTP.
@Service
@Validated
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    public List<CommentDTO> findAll() {
        return commentMapper.toDTOs(commentRepository.findAll());
    }

    // orElseThrow con EntityNotFoundException: se l'id non esiste nel DB
    // lancio un'eccezione che il controller cattura e trasforma in una risposta 404.
    // Meglio che restituire null e far crashare tutto altrove.
    public CommentDTO findById(Integer id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));
        return commentMapper.toDTO(comment);
    }

    // Metodo chiave per il frontend: quando apro un post devo caricare tutti i suoi commenti.
    // Delego la query a findByPostId del repository (Spring Data JPA la genera da solo).
    public List<CommentDTO> findByPostId(int postId) {
        return commentMapper.toDTOs(commentRepository.findByPostId(postId));
    }

    // Utile per la pagina profilo utente: "commenti che ho scritto".
    public List<CommentDTO> findByAuthorId(int authorId) {
        return commentMapper.toDTOs(commentRepository.findByAuthorId(authorId));
    }

    // @Valid attiva le annotazioni di validazione sul DTO prima di eseguire il metodo.
    // Se il DTO non è valido (es. content vuoto), Spring lancia ConstraintViolationException
    // ancora prima di toccare il DB → il controller risponde 400.
    public CommentDTO save(@Valid CommentDTO dto) {
        Comment comment = commentMapper.toEntity(dto);
        // Il client non manda createdAt: lo imposto qui con l'ora esatta del server.
        // Se lasciassi che il client lo mandasse, potrebbe falsificare la data.
        comment.setCreatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);
        return commentMapper.toDTO(comment);
    }

    public CommentDTO update(Integer id, @Valid CommentDTO dto) {
        // Prima controllo che il commento esista: se non c'è lancio 404.
        commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));
        Comment comment = commentMapper.toEntity(dto);
        comment.setId(id);
        // Il mapper ricrea un oggetto Comment pulito dal DTO, quindi createdAt sarebbe null.
        // Devo rileggere il commento originale dal DB per recuperare la data di creazione
        // e riapplicarla: non voglio che un update azzeri la data.
        Comment original = commentRepository.findById(id).get();
        comment.setCreatedAt(original.getCreatedAt());
        comment = commentRepository.save(comment);
        return commentMapper.toDTO(comment);
    }

    public void deleteById(Integer id) {
        commentRepository.deleteById(id);
    }

}
