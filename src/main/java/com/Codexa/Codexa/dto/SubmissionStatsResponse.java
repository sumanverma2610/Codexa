package com.Codexa.Codexa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionStatsResponse {

    private long total;

    private long accepted;

    private long wrongAnswer;

    private long compilationError;

    private long runtimeError;

    private long timeLimitExceeded;

    private long outputLimitExceeded;
}