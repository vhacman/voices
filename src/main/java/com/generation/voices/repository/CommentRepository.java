package com.generation.voices.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.generation.voices.model.Comment;

// Stendo JpaRepository<Comment, Integer> come ho fatto per Blog e BlogPost.
// Il secondo tipo generico è il tipo dell'@Id: uso Integer e non int
// perché i metodi di JpaRepository (es. findById) lavorano con oggetti, non primitivi.
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Spring Data JPA genera la query SQL in automatico leggendo il nome del metodo.
    // findBy + PostId → WHERE post_id = ?
    // Non devo scrivere niente altro: funziona per convenzione di naming.
    List<Comment> findByPostId(int postId);

    // findBy + AuthorId → WHERE author_id = ?
    // Mi serve per la pagina profilo dell'utente che mostra i suoi commenti.
    List<Comment> findByAuthorId(int authorId);

}
