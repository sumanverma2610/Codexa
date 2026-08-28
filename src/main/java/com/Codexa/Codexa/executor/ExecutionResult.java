package com.Codexa.Codexa.executor;

import com.Codexa.Codexa.entity.SubmissionStatus;
import lombok.Getter;

@Getter
public class ExecutionResult {

    private final SubmissionStatus status;
    private final String output;

    public ExecutionResult(
            SubmissionStatus status,
            String output) {

        this.status = status;
        this.output = output;
    }
}