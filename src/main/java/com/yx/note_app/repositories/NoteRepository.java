package com.yx.note_app.repositories;

import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Integer>{
    // Find all notes by author (JOIN FETCH author to avoid N+1)
    @Query("SELECT n FROM Note n JOIN FETCH n.author WHERE n.author = :author")
    List<Note> findByAuthor(@Param("author") User author);

    // Find all notes by author with pagination (JOIN FETCH author to avoid N+1)
    @Query(value = "SELECT n FROM Note n JOIN FETCH n.author WHERE n.author = :author",
           countQuery = "SELECT COUNT(n) FROM Note n WHERE n.author = :author")
    Page<Note> findByAuthor(@Param("author") User author, Pageable pageable);

    // Find note by id (JOIN FETCH author to avoid N+1)
    @Query("SELECT n FROM Note n JOIN FETCH n.author WHERE n.id = :id")
    Note findById(@Param("id") int id);

    // Find note by id with shared notes and their users (for GetSharedToUsersService)
    @Query("SELECT n FROM Note n JOIN FETCH n.author LEFT JOIN FETCH n.sharedNotes sn LEFT JOIN FETCH sn.sharedToUser WHERE n.id = :id")
    Note findByIdWithSharedUsers(@Param("id") int id);

    //Delete notes by id
    void deleteById(int id);

    // Update note by id
    @Modifying
    @Transactional
    @Query("UPDATE Note n SET n.title = :title, n.content = :content WHERE n.id = :id")
    void updateNote(
            @Param("id") int id,
            @Param("title") String title,
            @Param("content") String content
    );
}