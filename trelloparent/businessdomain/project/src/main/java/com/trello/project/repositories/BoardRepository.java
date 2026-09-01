package com.trello.project.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Board;

public interface BoardRepository extends JpaRepository<Board, UUID> {

}
