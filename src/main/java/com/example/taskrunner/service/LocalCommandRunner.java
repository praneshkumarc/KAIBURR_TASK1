package com.example.taskrunner.service;

import com.example.taskrunner.model.TaskExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "runner", name = "mode", havingValue = "local")
public class LocalCommandRunner implements JobRunner {

    private final int timeoutSeconds;
    private final boolean windows;

    public LocalCommandRunner(@Value("${runner.jobTimeoutSeconds}") int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.windows = System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public TaskExecution runAndCapture(String taskId, String command) {
        Date start = new Date();
        ProcessBuilder builder = new ProcessBuilder(buildShellCommand(command));
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Command timed out after " + timeoutSeconds + " seconds");
            }

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.exitValue();
            Date end = new Date();

            TaskExecution execution = new TaskExecution();
            execution.setStartTime(start);
            execution.setEndTime(end);
            String normalized = output == null ? "" : output.trim();
            if (exitCode != 0) {
                String exitMessage = "Process exited with code " + exitCode;
                normalized = normalized.isEmpty() ? exitMessage : normalized + System.lineSeparator() + exitMessage;
            }
            execution.setOutput(normalized);
            return execution;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command execution interrupted", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute command", ex);
        }
    }

    private String[] buildShellCommand(String command) {
        if (windows) {
            return new String[]{"cmd.exe", "/c", command};
        }
        return new String[]{"sh", "-c", command};
    }
}
