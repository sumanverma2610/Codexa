package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.dto.CreateProblemRequest;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    public ResponseEntity<Problem> createProblem(
            @Valid @RequestBody CreateProblemRequest request) {

        return ResponseEntity.ok(
                problemService.createProblem(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<Problem>> getAllProblems() {

        return ResponseEntity.ok(
                problemService.getAllProblems()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Problem> getProblemById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                problemService.getProblemById(id)
        );
    }
}
