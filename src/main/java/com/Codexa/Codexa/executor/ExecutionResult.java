package com.Codexa.Codexa.executor;

import com.Codexa.Codexa.entity.SubmissionStatus;
import lombok.Getter;

@Getter
public class ExecutionResult {

    private final SubmissionStatus status;
    private final String output;
    private final long executionTime;
    private final long memoryUsed;

    public ExecutionResult(
            SubmissionStatus status,
            String output,
            long executionTime,
            long memoryUsed) {

        this.status = status;
        this.output = output;
        this.executionTime = executionTime;
        this.memoryUsed = memoryUsed;
    }
}