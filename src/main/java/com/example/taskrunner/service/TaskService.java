package com.example.taskrunner.service;

import com.example.taskrunner.model.Task;
import com.example.taskrunner.model.TaskExecution;
import com.example.taskrunner.repo.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repo;
    private final CommandValidator validator;
    private final JobRunner runner;

    public TaskService(TaskRepository repo, CommandValidator validator, JobRunner runner) {
        this.repo = repo;
        this.validator = validator;
        this.runner = runner;
    }

    public List<Task> getAll() {
        return repo.findAll();
    }

    public Task getByIdOrThrow(String id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    public Task upsert(Task t) {
        validator.validateOrThrow(t.getCommand());
        return repo.save(t);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    public List<Task> searchByName(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }

    public TaskExecution runExecution(String id) {
        Task task = getByIdOrThrow(id);
        validator.validateOrThrow(task.getCommand());
        TaskExecution execution = runner.runAndCapture(task.getId(), task.getCommand());
        task.getTaskExecutions().add(execution);
        repo.save(task);
        return execution;
    }
}
