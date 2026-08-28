package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.executor.CodeExecutorService;
import com.Codexa.Codexa.executor.ExecutionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeExecutorController {

    private final CodeExecutorService codeExecutorService;

    public CodeExecutorController(
            CodeExecutorService codeExecutorService) {

        this.codeExecutorService = codeExecutorService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResult> execute(
            @RequestParam String input,
            @RequestBody String code) {

        ExecutionResult result =
                codeExecutorService.execute(
                        code,
                        input
                );

        return ResponseEntity.ok(result);
    }
}