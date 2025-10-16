package com.example.taskrunner.controller;


import com.example.taskrunner.model.Task; import com.example.taskrunner.model.TaskExecution; import com.example.taskrunner.service.TaskService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.List;


@RestController
@RequestMapping("/api")
public class TaskController {
private final TaskService service; public TaskController(TaskService s){ this.service=s; }


@GetMapping("/tasks")
public ResponseEntity<?> getTasks(@RequestParam(value="id", required=false) String id){
if (id==null) return ResponseEntity.ok(service.getAll());
return ResponseEntity.ok(service.getByIdOrThrow(id));
}


@PutMapping("/tasks")
public ResponseEntity<Task> putTask(@Valid @RequestBody Task task){ return new ResponseEntity<>(service.upsert(task), HttpStatus.CREATED); }


@DeleteMapping("/tasks/{id}")
public ResponseEntity<Void> deleteTask(@PathVariable String id){ service.delete(id); return ResponseEntity.noContent().build(); }


@GetMapping("/tasks/search")
public ResponseEntity<List<Task>> search(@RequestParam("name") String name){ List<Task> found = service.searchByName(name); return found.isEmpty()? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(found); }


@PutMapping("/tasks/{id}/executions")
public ResponseEntity<TaskExecution> runExecution(@PathVariable String id){ return new ResponseEntity<>(service.runExecution(id), HttpStatus.CREATED); }
}