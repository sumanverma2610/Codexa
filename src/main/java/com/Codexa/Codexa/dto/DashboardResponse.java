package com.Codexa.Codexa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardResponse {

    private long totalSubmissions;

    private long acceptedSubmissions;

    private long wrongAnswers;

    private long compilationErrors;

    private long runtimeErrors;

    private long timeLimitExceeded;

    private double acceptanceRate;
    private long problemsSolved;

}