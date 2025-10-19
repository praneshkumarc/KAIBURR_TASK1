package com.example.taskrunner.service;

import com.example.taskrunner.model.TaskExecution;

/**
 * Abstraction for executing a task command and returning its captured output.
 */
public interface JobRunner {
    TaskExecution runAndCapture(String taskId, String command);
}
