package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.dto.CreateSubmissionRequest;
import com.Codexa.Codexa.dto.SubmissionResponse;
import com.Codexa.Codexa.dto.SubmissionStatsResponse;
import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.entity.SubmissionStatus;
import com.Codexa.Codexa.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(
            @Valid @RequestBody CreateSubmissionRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Submission submission =
                submissionService.createSubmission(
                        request,
                        email
                );

        SubmissionResponse response =
                new SubmissionResponse();

        response.setId(
                submission.getId()
        );

        response.setProblemId(
                submission.getProblem().getId()
        );

        response.setLanguage(
                submission.getLanguage()
        );

        response.setStatus(
                submission.getStatus()
        );

        response.setResult(
                submission.getResult()
        );

        response.setCode(
                submission.getCode()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<SubmissionResponse>> getMySubmissions(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            SubmissionStatus status,

            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                submissionService.getMySubmissions(
                        email,
                        page,
                        size,
                        status
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmissionById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                submissionService.getSubmissionById(
                        id,
                        email
                )
        );
    }
    @GetMapping("/stats")
    public ResponseEntity<SubmissionStatsResponse> getSubmissionStats(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                submissionService.getSubmissionStats(email)
        );
    }
}