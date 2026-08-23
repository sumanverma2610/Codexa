package com.Codexa.Codexa.executor;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutorService {

    public String execute(String code, String input) {

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
                    executionDirectory.resolve(
                            "Main.java"
                    );

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
            }

            int compileExitCode =
                    compileProcess.waitFor();

            // 4. Compilation error

            if (compileExitCode != 0) {

                return "Compilation Error:\n"
                        + compileOutput;
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
                        input.getBytes()
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

                return "Time Limit Exceeded";
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
            }

            // 10. Runtime error

            int runExitCode =
                    runProcess.exitValue();

            if (runExitCode != 0) {

                return "Runtime Error:\n"
                        + output;
            }

            // 11. Successful execution

            return output.toString();

        } catch (Exception e) {

            return "Execution Error: "
                    + e.getMessage();

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