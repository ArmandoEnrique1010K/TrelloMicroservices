package com.trello.project.entities;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(name = "workspaces")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String description;

    // ID del propietario del proyecto
    private UUID ownerId;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invitation> invitations;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "workspaces_members", joinColumns = @JoinColumn(name = "workspace_id"), inverseJoinColumns = @JoinColumn(name = "member_id"), uniqueConstraints = {
            @UniqueConstraint(columnNames = { "workspace_id", "member_id" })
    })
    private List<Member> members;
}
