package com.github.gastaldi.git.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @author Lukas Zaruba, lukas.zaruba@lundegaard.eu, 2019
 */
public class ExecutionHelper {

    private ExecutionHelper() {
        // to prevent instantiation
    }

    /**
     * Executes given command in pwd and waits for result
     * IO is inherited from this process
     *
     * @throws IOException when interrupted
     * @throws RuntimeException when process finishes with status other than 0
     */
    public static void executeCommand(Path pwd, String... command) {
        try {
            Process process = new ProcessBuilder()
                    .directory(pwd.toFile())
                    .command(command)
                    .inheritIO()
                    .start();
            int result = process.waitFor();
            if (result != 0) {
                throw new ExecutionException(result, "Command " + Arrays.toString(command) + " exited with error code " + result);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new ExecutionException("Exception while running command " + Arrays.toString(command), e);
        }
    }

}
