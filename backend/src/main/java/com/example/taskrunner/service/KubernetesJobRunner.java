package com.example.taskrunner.service;


import com.example.taskrunner.model.TaskExecution;
import com.example.taskrunner.util.NameUtil;
import io.fabric8.kubernetes.api.model.Pod; import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job; import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient; import io.fabric8.kubernetes.client.dsl.Resource;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import java.time.Instant; import java.util.Date;


@Service
public class KubernetesJobRunner {
private final KubernetesClient client; private final String namespace; private final int timeoutSeconds; private final String image;
public KubernetesJobRunner(KubernetesClient c, @Value("${runner.namespace}") String ns,
@Value("${runner.jobTimeoutSeconds}") int to, @Value("${runner.image}") String img){
this.client=c; this.namespace=ns; this.timeoutSeconds=to; this.image=img; }


public TaskExecution runAndCapture(String taskId, String command){
String base = "task-"+ NameUtil.toK8sName(taskId) +"-"+ Instant.now().toEpochMilli();
String jobName = base.substring(0, Math.min(63, base.length()));
Date start = new Date();


Job job = new JobBuilder()
.withNewMetadata().withName(jobName).addToLabels("app","task-runner").endMetadata()
.withNewSpec().withBackoffLimit(0)
.withNewTemplate().withNewSpec().withRestartPolicy("Never")
.addNewContainer().withName("runner").withImage(image).withCommand("sh","-lc",command).endContainer()
.endSpec().endTemplate()
.endSpec().build();


client.batch().v1().jobs().inNamespace(namespace).resource(job).create();


long startWait = System.currentTimeMillis(); boolean completed=false; boolean failed=false; String podName=null;
while (System.currentTimeMillis()-startWait < timeoutSeconds*1000L){
Job cur = client.batch().v1().jobs().inNamespace(namespace).withName(jobName).get();
if (cur!=null && cur.getStatus()!=null){
Integer s = cur.getStatus().getSucceeded(); Integer f = cur.getStatus().getFailed();
if (s!=null && s>0){ completed=true; break; }
if (f!=null && f>0){ failed=true; break; }
}
try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
}


PodList pods = client.pods().inNamespace(namespace).withLabel("job-name", jobName).list();
if (pods!=null && !pods.getItems().isEmpty()) podName = pods.getItems().get(0).getMetadata().getName();
String logs = podName==null?"": client.pods().inNamespace(namespace).withName(podName).getLog();


Resource<Job> res = client.batch().v1().jobs().inNamespace(namespace).withName(jobName);
try { res.delete(); } catch (Exception ignored) {}


Date end = new Date();
if (!completed) throw new RuntimeException("Job "+jobName+" did not complete: "+ (failed?"FAILED":"TIMEOUT")+". Logs: "+logs);


TaskExecution exec = new TaskExecution(); exec.setStartTime(start); exec.setEndTime(end); exec.setOutput(logs==null?"":logs.trim());
return exec;
}
}