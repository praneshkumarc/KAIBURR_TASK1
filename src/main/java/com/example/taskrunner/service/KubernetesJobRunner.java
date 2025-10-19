package com.example.taskrunner.service;

import com.example.taskrunner.model.TaskExecution;
import com.example.taskrunner.util.NameUtil;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@ConditionalOnProperty(prefix = "runner", name = "mode", havingValue = "k8s", matchIfMissing = true)
public class KubernetesJobRunner implements JobRunner {

    private final KubernetesClient client;
    private final String namespace;
    private final int timeoutSeconds;
    private final String image;

    public KubernetesJobRunner(
            KubernetesClient client,
            @Value("${runner.namespace}") String namespace,
            @Value("${runner.jobTimeoutSeconds}") int timeoutSeconds,
            @Value("${runner.image}") String image
    ) {
        this.client = client;
        this.namespace = namespace;
        this.timeoutSeconds = timeoutSeconds;
        this.image = image;
    }

    @Override
    public TaskExecution runAndCapture(String taskId, String command) {
        String baseName = "task-" + NameUtil.toK8sName(taskId);
        String podName = baseName + "-" + System.currentTimeMillis();
        if (podName.length() > 63) {
            podName = podName.substring(0, 63);
        }

        Date start = new Date();

        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName(podName)
                .addToLabels("app", "task-runner")
                .addToLabels("taskId", NameUtil.toK8sName(taskId))
                .endMetadata()
                .withNewSpec()
                .withRestartPolicy("Never")
                .addNewContainer()
                .withName("runner")
                .withImage(image)
                .withCommand("sh", "-c", command)
                .endContainer()
                .endSpec()
                .build();

        client.pods().inNamespace(namespace).resource(pod).create();

        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        String phase = null;
        do {
            Pod current = client.pods().inNamespace(namespace).withName(podName).get();
            if (current != null && current.getStatus() != null) {
                phase = current.getStatus().getPhase();
                if ("Succeeded".equalsIgnoreCase(phase) || "Failed".equalsIgnoreCase(phase)) {
                    break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        } while (System.currentTimeMillis() < deadline);

        String logs = client.pods().inNamespace(namespace).withName(podName).getLog();

        // Best-effort cleanup; the pod might already be gone
        try {
            client.pods().inNamespace(namespace).withName(podName).delete();
        } catch (Exception ignored) {
            // no-op
        }

        Date end = new Date();

        TaskExecution execution = new TaskExecution();
        execution.setStartTime(start);
        execution.setEndTime(end);
        execution.setOutput(logs == null ? "" : logs.trim());
        return execution;
    }
}
