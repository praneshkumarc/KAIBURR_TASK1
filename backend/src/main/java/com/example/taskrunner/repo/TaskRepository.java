package com.example.taskrunner.repo;


import com.example.taskrunner.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface TaskRepository extends MongoRepository<Task, String> {
List<Task> findByNameContainingIgnoreCase(String name);
}