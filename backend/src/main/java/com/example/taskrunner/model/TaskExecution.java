package com.example.taskrunner.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;


public class TaskExecution {
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
private Date startTime;
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
private Date endTime;
private String output;
public Date getStartTime(){return startTime;} public void setStartTime(Date d){this.startTime=d;}
public Date getEndTime(){return endTime;} public void setEndTime(Date d){this.endTime=d;}
public String getOutput(){return output;} public void setOutput(String o){this.output=o;}
}