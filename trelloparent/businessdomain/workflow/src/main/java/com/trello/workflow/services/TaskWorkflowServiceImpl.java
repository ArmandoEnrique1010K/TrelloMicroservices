package com.trello.workflow.services;

import com.trello.workflow.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Task;

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

}
