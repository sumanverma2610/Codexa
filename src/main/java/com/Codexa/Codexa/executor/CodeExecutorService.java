package com.Codexa.Codexa.executor;

import com.Codexa.Codexa.entity.SubmissionStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutorService {

    private static final int MAX_OUTPUT_SIZE = 1024 * 1024;
    private static final long TIME_LIMIT = 5;

    public ExecutionResult execute(String code, String input) {

        Path executionDirectory =
                Paths.get(
                        "execution",
                        UUID.randomUUID().toString()
                );

        try {
            Files.createDirectories(executionDirectory);

            Path javaFile =
                    executionDirectory.resolve("Main.java");

            Files.writeString(
                    javaFile,
                    code
            );
            

            // Compile
            Process compileProcess =
                    new ProcessBuilder(
                            "javac",
                            "Main.java"
                    )
                            .directory(
                                    executionDirectory.toFile()
                            )
                            .redirectErrorStream(true)
                            .start();

            StringBuilder compileOutput =
                    new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         compileProcess.getInputStream()
                                 )
                         )) {

                String line;

                while ((line = reader.readLine()) != null) {

                    compileOutput
                            .append(line)
                            .append("\n");

                    if (compileOutput.length()
                            > MAX_OUTPUT_SIZE) {

                        compileProcess.destroyForcibly();

                        return new ExecutionResult(
                                SubmissionStatus.OUTPUT_LIMIT_EXCEEDED,
                                "Compilation output exceeded 1 MB",
                                0,
                                0
                        );
                    }
                }
            }

            int compileExitCode =
                    compileProcess.waitFor();

            if (compileExitCode != 0) {

                return new ExecutionResult(
                        SubmissionStatus.COMPILATION_ERROR,
                        compileOutput.toString(),
                        0,
                        0
                );
            }

            // Run
            long startTime =
                    System.nanoTime();

            Process runProcess =
                    new ProcessBuilder(
                            "java",
                            "-Xmx128m",
                            "-Xss1m",
                            "-Djava.security.manager=disallow",
                            "Main"
                    )
                            .directory(
                                    executionDirectory.toFile()
                            )
                            .redirectErrorStream(true)
                            .start();

            if (input != null) {

                try (OutputStream outputStream =
                             runProcess.getOutputStream()) {

                    outputStream.write(
                            input.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

                    outputStream.flush();
                }
            }

            StringBuilder output =
                    new StringBuilder();

            final boolean[] outputLimitExceeded =
                    {false};

            // Read output
            Thread outputReader =
                    new Thread(() -> {

                        try {

                            BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    runProcess
                                                            .getInputStream()
                                            )
                                    );

                            String line;

                            while ((line =
                                    reader.readLine()) != null) {

                                synchronized (output) {

                                    output.append(line)
                                            .append("\n");

                                    if (output.length()
                                            > MAX_OUTPUT_SIZE) {

                                        outputLimitExceeded[0] =
                                                true;

                                        runProcess
                                                .destroyForcibly();

                                        break;
                                    }
                                }
                            }

                        } catch (Exception ignored) {
                        }
                    });

            outputReader.start();

            // Monitor memory
            final long[] peakMemory =
                    {0};

            Thread memoryMonitor =
                    new Thread(() -> {

                        while (runProcess.isAlive()) {

                            long memory =
                                    getProcessMemory(
                                            runProcess.pid()
                                    );

                            if (memory >
                                    peakMemory[0]) {

                                peakMemory[0] =
                                        memory;
                            }

                            try {

                                Thread.sleep(20);

                            } catch (InterruptedException e) {

                                Thread.currentThread()
                                        .interrupt();

                                break;
                            }
                        }
                    });

            memoryMonitor.start();

            boolean finished =
                    runProcess.waitFor(
                            TIME_LIMIT,
                            TimeUnit.SECONDS
                    );

            long endTime =
                    System.nanoTime();

            long executionTime =
                    (endTime - startTime)
                            / 1_000_000;

            if (!finished) {

                runProcess.destroyForcibly();

                outputReader.join(1000);

                memoryMonitor.interrupt();
                memoryMonitor.join(1000);

                return new ExecutionResult(
                        SubmissionStatus.TIME_LIMIT_EXCEEDED,
                        "Program exceeded 5 seconds",
                        executionTime,
                        peakMemory[0]
                );
            }

            outputReader.join(1000);

            memoryMonitor.interrupt();
            memoryMonitor.join(1000);

            long memoryUsed =
                    peakMemory[0];

            // Output limit
            if (outputLimitExceeded[0]) {

                return new ExecutionResult(
                        SubmissionStatus.OUTPUT_LIMIT_EXCEEDED,
                        "Program output exceeded 1 MB",
                        executionTime,
                        memoryUsed
                );
            }

            // Runtime error
            int runExitCode =
                    runProcess.exitValue();

            if (runExitCode != 0) {

                return new ExecutionResult(
                        SubmissionStatus.RUNTIME_ERROR,
                        output.toString(),
                        executionTime,
                        memoryUsed
                );
            }

            // Success
            return new ExecutionResult(
                    SubmissionStatus.ACCEPTED,
                    output.toString(),
                    executionTime,
                    memoryUsed
            );

        } catch (Exception e) {

            return new ExecutionResult(
                    SubmissionStatus.EXECUTION_ERROR,
                    e.getMessage(),
                    0,
                    0
            );

        } finally {

            deleteDirectory(
                    executionDirectory
            );
        }
    }

    private long getProcessMemory(long pid) {

        try {

            Process process =
                    new ProcessBuilder(
                            "powershell",
                            "-Command",
                            "(Get-Process -Id " + pid +
                                    ").WorkingSet64"
                    )
                            .redirectErrorStream(true)
                            .start();

            String result;

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream()
                                 )
                         )) {

                result = reader.readLine();
            }

            process.waitFor(
                    1,
                    TimeUnit.SECONDS
            );

            if (result != null) {

                return Long.parseLong(
                        result.trim()
                );
            }

        } catch (Exception ignored) {
        }

        return 0;
    }

    private void deleteDirectory(
            Path directory) {

        try {

            if (!Files.exists(directory)) {
                return;
            }

            Files.walk(directory)
                    .sorted(
                            Comparator.reverseOrder()
                    )
                    .forEach(path -> {

                        try {

                            Files.deleteIfExists(path);

                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception ignored) {
        }
    }
}