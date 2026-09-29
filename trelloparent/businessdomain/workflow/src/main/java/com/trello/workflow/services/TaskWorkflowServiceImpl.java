package com.trello.workflow.services;

import com.trello.workflow.repositories.TaskRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Task;
import com.trello.workflow.exception.TaskNotFoundException;

@Service
public class TaskWorkflowServiceImpl implements TaskWorkflowService {

    private final TaskRepository taskRepository;

    TaskWorkflowServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public List<Task> findAllTasksByBoardId(UUID boardId) {
        return taskRepository.findByBoardId(boardId);
    }

    @Override
    public Task findTaskById(UUID taskId) throws TaskNotFoundException {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        return task;
    }

    // Para borrar una tarea es necesario verificar que aun exista la tarea en la
    // base de datos
    @Override
    public void deleteTaskById(UUID taskId) throws TaskNotFoundException {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        taskRepository.delete(task);
    }
}
