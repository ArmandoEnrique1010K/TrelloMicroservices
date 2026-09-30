package com.trello.workflow.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.Label;

public interface LabelRepository extends JpaRepository<Label, UUID> {

    List<Label> findByBoardId(UUID boardId);

    boolean existsByBoardIdAndContent(UUID boardId, String content);

    boolean existsByBoardIdAndContentAndIdNot(UUID boardId, String content, UUID labelId);
}
