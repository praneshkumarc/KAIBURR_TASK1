package com.example.taskrunner.service;


import com.example.taskrunner.model.Task; import com.example.taskrunner.model.TaskExecution; import com.example.taskrunner.repo.TaskRepository;
import org.springframework.stereotype.Service; import java.util.List;


@Service
public class TaskService {
private final TaskRepository repo; private final CommandValidator validator; private final KubernetesJobRunner runner;
public TaskService(TaskRepository r, CommandValidator v, KubernetesJobRunner k){ this.repo=r; this.validator=v; this.runner=k; }
public List<Task> getAll(){ return repo.findAll(); }
public Task getByIdOrThrow(String id){ return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found: "+id)); }
public Task upsert(Task t){ validator.validateOrThrow(t.getCommand()); return repo.save(t); }
public void delete(String id){ repo.deleteById(id); }
public List<Task> searchByName(String name){ return repo.findByNameContainingIgnoreCase(name); }
public TaskExecution runExecution(String id){ Task t = getByIdOrThrow(id); validator.validateOrThrow(t.getCommand()); TaskExecution exec = runner.runAndCapture(t.getId(), t.getCommand()); t.getTaskExecutions().add(exec); repo.save(t); return exec; }
}