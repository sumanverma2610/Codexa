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

    private static final int MAX_OUTPUT_SIZE = 1024 * 1024; // 1 MB

    public ExecutionResult execute(String code, String input) {

        // Create a unique folder for every execution
        Path executionDirectory =
                Paths.get(
                        "execution",
                        UUID.randomUUID().toString()
                );

        try {

            // 1. Create execution directory

            Files.createDirectories(
                    executionDirectory
            );

            // 2. Create Main.java

            Path javaFile =
                    executionDirectory.resolve("Main.java");

            Files.writeString(
                    javaFile,
                    code
            );

            // 3. Compile Java code

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

            BufferedReader compileReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    compileProcess.getInputStream()
                            )
                    );

            StringBuilder compileOutput =
                    new StringBuilder();

            String line;

            while ((line = compileReader.readLine()) != null) {

                compileOutput
                        .append(line)
                        .append("\n");

                // Compilation output limit
                if (compileOutput.length() > MAX_OUTPUT_SIZE) {

                    compileProcess.destroyForcibly();

                    return new ExecutionResult(
                            SubmissionStatus.OUTPUT_LIMIT_EXCEEDED,
                            "Compilation output exceeded 1 MB"
                    );
                }
            }

            int compileExitCode =
                    compileProcess.waitFor();

            // 4. Compilation error

            if (compileExitCode != 0) {

                return new ExecutionResult(
                        SubmissionStatus.COMPILATION_ERROR,
                        compileOutput.toString()
                );
            }

            // 5. Run Java program

            Process runProcess =
                    new ProcessBuilder(
                            "java",
                            "Main"
                    )
                            .directory(
                                    executionDirectory.toFile()
                            )
                            .redirectErrorStream(true)
                            .start();

            // 6. Send input

            if (input != null) {

                OutputStream outputStream =
                        runProcess.getOutputStream();

                outputStream.write(
                        input.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                outputStream.flush();
                outputStream.close();
            }

            // 7. Maximum execution time = 5 seconds

            boolean finished =
                    runProcess.waitFor(
                            5,
                            TimeUnit.SECONDS
                    );

            // 8. Time limit exceeded

            if (!finished) {

                runProcess.destroyForcibly();

                return new ExecutionResult(
                        SubmissionStatus.TIME_LIMIT_EXCEEDED,
                        "Program exceeded 5 seconds"
                );
            }

            // 9. Read program output

            BufferedReader runReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    runProcess.getInputStream()
                            )
                    );

            StringBuilder output =
                    new StringBuilder();

            while ((line = runReader.readLine()) != null) {

                output
                        .append(line)
                        .append("\n");

                // Output limit = 1 MB

                if (output.length() > MAX_OUTPUT_SIZE) {

                    runProcess.destroyForcibly();

                    return new ExecutionResult(
                            SubmissionStatus.OUTPUT_LIMIT_EXCEEDED,
                            "Program output exceeded 1 MB"
                    );
                }
            }

            // 10. Runtime error

            int runExitCode =
                    runProcess.exitValue();

            if (runExitCode != 0) {

                return new ExecutionResult(
                        SubmissionStatus.RUNTIME_ERROR,
                        output.toString()
                );
            }

            // 11. Successful execution

            return new ExecutionResult(
                    SubmissionStatus.ACCEPTED,
                    output.toString()
            );

        } catch (Exception e) {

            return new ExecutionResult(
                    SubmissionStatus.EXECUTION_ERROR,
                    e.getMessage()
            );

        } finally {

            // 12. Delete temporary files

            deleteDirectory(
                    executionDirectory
            );
        }
    }

    // Delete execution directory

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

                            Files.deleteIfExists(
                                    path
                            );

                        } catch (Exception e) {

                            // Ignore cleanup errors
                        }
                    });

        } catch (Exception e) {

            // Ignore cleanup errors
        }
    }
}