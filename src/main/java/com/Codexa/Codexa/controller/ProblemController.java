package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.dto.CreateProblemRequest;
import com.Codexa.Codexa.entity.Problem;
import com.Codexa.Codexa.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    // Create problem
    @PostMapping
    public ResponseEntity<Problem> createProblem(
            @Valid @RequestBody CreateProblemRequest request) {

        return ResponseEntity.ok(
                problemService.createProblem(request)
        );
    }

    // Get problems with pagination, search, filter and sorting
    @GetMapping
    public ResponseEntity<Page<Problem>> getAllProblems(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String difficulty,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return ResponseEntity.ok(
                problemService.searchProblems(
                        search,
                        difficulty,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    // Get problem by ID
    @GetMapping("/{id}")
    public ResponseEntity<Problem> getProblemById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                problemService.getProblemById(id)
        );
    }
}