package com.Codexa.Codexa.executor;

import com.Codexa.Codexa.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExecutionResult {

    private SubmissionStatus status;

    private String output;
}