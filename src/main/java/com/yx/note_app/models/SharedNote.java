package com.yx.note_app.models;

import jakarta.persistence.*;
import com.yx.note_app.enums.Permission;

@Entity
@Table(
        name = "sharednotes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"note_id", "shared_to_user_id"})
)
public class SharedNote extends Model{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_to_user_id")
    private User sharedToUser;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Permission permission;

    public SharedNote() {
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public User getSharedToUser() {
        return sharedToUser;
    }

    public void setSharedToUser(User sharedToUser) {
        this.sharedToUser = sharedToUser;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
