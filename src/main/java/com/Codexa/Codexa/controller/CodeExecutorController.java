package com.Codexa.Codexa.controller;

import com.Codexa.Codexa.executor.CodeExecutorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeExecutorController {

    private final CodeExecutorService codeExecutorService;

    public CodeExecutorController(CodeExecutorService codeExecutorService) {
        this.codeExecutorService = codeExecutorService;
    }

    @PostMapping("/execute")
    public String execute(
            @RequestParam String input,
            @RequestBody String code) {

        return codeExecutorService.execute(
                code,
                input
        );
    }
}