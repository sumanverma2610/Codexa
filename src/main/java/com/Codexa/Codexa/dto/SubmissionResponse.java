package com.Codexa.Codexa.dto;

import com.Codexa.Codexa.entity.SubmissionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionResponse {

    private Long id;
    private Long problemId;
    private String language;
    private SubmissionStatus status;
    private String result;
    private String code;
    private long executionTime;
    private long memoryUsed;
}