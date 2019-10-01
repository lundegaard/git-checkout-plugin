package com.github.gastaldi.git.impl;

/**
 * @author Lukas Zaruba, lukas.zaruba@lundegaard.eu, 2019
 */
public class ExecutionException extends RuntimeException {

    private final int exitCode;

    public ExecutionException(int exitCode, String message, Throwable t) {
        super(message, t);
        this.exitCode = exitCode;
    }

    public ExecutionException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public ExecutionException(String message, Throwable t) {
        this(-1, message, t);
    }

    public int getExitCode() {
        return exitCode;
    }

}
