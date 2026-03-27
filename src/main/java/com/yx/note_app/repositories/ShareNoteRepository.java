package com.yx.note_app.repositories;

import com.yx.note_app.enums.Permission;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.models.User;
import com.yx.note_app.models.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ShareNoteRepository extends JpaRepository<SharedNote, Integer>{
    // Find all notes shared to a specific user (JOIN FETCH to avoid N+1)
    @Query("SELECT sn FROM SharedNote sn JOIN FETCH sn.note n JOIN FETCH n.author JOIN FETCH sn.sharedToUser WHERE sn.sharedToUser = :user")
    List<SharedNote> findBySharedToUser(@Param("user") User user);

    // Find all notes shared to a specific user with pagination (JOIN FETCH to avoid N+1)
    @Query(value = "SELECT sn FROM SharedNote sn JOIN FETCH sn.note n JOIN FETCH n.author JOIN FETCH sn.sharedToUser WHERE sn.sharedToUser = :user",
           countQuery = "SELECT COUNT(sn) FROM SharedNote sn WHERE sn.sharedToUser = :user")
    Page<SharedNote> findBySharedToUser(@Param("user") User user, Pageable pageable);

    // Find a specific shared note (JOIN FETCH to avoid N+1)
    @Query("SELECT sn FROM SharedNote sn JOIN FETCH sn.note n JOIN FETCH n.author JOIN FETCH sn.sharedToUser WHERE sn.id = :id")
    SharedNote findById(@Param("id") int id);

    // Find a specific shared note base on user & note
    SharedNote findByNoteIdAndSharedToUserId(int noteId, int sharedToUserId);

    // Delete by Id
    void deleteById(int id);

    // Check if duplicate exist
    boolean existsByNoteIdAndSharedToUserId(int noteId, int sharedToUserId);

    // Update permission of shared note
    @Modifying
    @Transactional
    @Query("UPDATE SharedNote n SET n.permission = :permission WHERE n.id = :id")
    void updateSharedNotePermission(
            @Param("id") int id,
            @Param("permission") Permission permission
    );
}