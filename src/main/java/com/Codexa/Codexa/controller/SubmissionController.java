package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.dto.CreateSubmissionRequest;
import com.Codexa.Codexa.entity.Submission;
import com.Codexa.Codexa.service.SubmissionService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Submission> createSubmission(
            @Valid @RequestBody CreateSubmissionRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Submission submission =
                submissionService.createSubmission(request, email);

        return ResponseEntity.ok(submission);
    }
    @GetMapping("/my")
    public ResponseEntity<List<Submission>> getMySubmissions(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                submissionService.getMySubmissions(email)
        );
    }
}