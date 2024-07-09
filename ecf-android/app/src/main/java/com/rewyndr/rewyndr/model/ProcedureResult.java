package com.rewyndr.rewyndr.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcedureResult {
    public int procedureId;
    private String procedureName;
    private List<Step> steps;
    private List<ExecutionResult> results;
    private String operatorUserName;
    private Instant timeCompleted;

    public ProcedureResult(Procedure procedure){
        this.steps = procedure.getSteps();
        this.procedureId = procedure.getId();
        this.procedureName = procedure.getName();
        this.results = new ArrayList<ExecutionResult>(steps.size());
        this.timeCompleted = procedure.getTimeCreated();

        for(int i = 0; i < steps.size(); i++){
            results.add(new ExecutionResult(steps.get(i)));
        }
    }

    public String serialize(){
        Gson gson = new Gson();

        JsonElement json = gson.toJsonTree(this);
        return gson.toJson(json);
    }

    public static ArrayList<ProcedureResult> fromProcedureArrayList(ArrayList<Procedure> procedures){
        ArrayList<ProcedureResult> procedureResults = new ArrayList<>();
        for(Procedure p : procedures){
            procedureResults.add(new ProcedureResult(p));
        }

        return procedureResults;
    }

    public List<Step> getSteps () { return steps; }

    public String  getName () { return procedureName; }

    public String getOperatorUserName(){
        return operatorUserName;
    }

    public void setOperatorUserName(String userName){ operatorUserName = userName; }

    public Instant getTimeCompleted(){
        return timeCompleted;
    }

    public void setTimeCompleted(Instant time){
        timeCompleted = time;
    }

    public void setExecutionIdForStep(int stepNumber, int id){
        results.get(stepNumber).executionId = id;
    }

    public int getExecutionIdForStep(int stepNumber){
        return results.get(stepNumber).executionId;
    }

    public void setStepIdForStep(int stepNumber, int id){
        results.get(stepNumber).executionId = id;
    }

    public String getTextForStep(int stepNumber){
        return results.get(stepNumber).comment;
    }

    public void setTextForStep(int stepNumber, String val){
        results.get(stepNumber).comment = val;
    }

    public File getImageForStep(int stepNumber){
        return results.get(stepNumber).image;
    }

    public void setImageForStep(int stepNumber, File file){
        results.get(stepNumber).image = file;
    }

    public void setStatusForStep(int stepNumber, String status) { results.get(stepNumber).status = status; }

    public List<ExecutionResult> getStepResults(){
        return results;
    }

    public ExecutionResult getStepResult(int i){
        return results.get(i);
    }

    public boolean isComplete(){
        for(ExecutionResult s: results){
            if (s.status == null || s.status.equals("null")){
                return false;
            }
        }
        return true;
    }

    public boolean containsFailure(){
        for(ExecutionResult s : results){
            if(s.status.equals("fail")){
                return true;
            }
        }

        return false;
    }
}
