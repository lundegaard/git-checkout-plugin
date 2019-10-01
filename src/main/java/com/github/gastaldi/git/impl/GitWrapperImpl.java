package com.github.gastaldi.git.impl;

import com.github.gastaldi.git.GitWrapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.gastaldi.git.impl.ExecutionHelper.executeCommand;

/**
 * @author Lukas Zaruba, lukas.zaruba@lundegaard.eu, 2019
 */
public class GitWrapperImpl implements GitWrapper {

    private Path pwd;

    @Override
    public GitWrapper initialize(Path pwd) {
        if (pwd == null) {
            throw new IllegalArgumentException("Pwd must be filled");
        }
        this.pwd = pwd;
        return this;
    }

    @Override
    public GitWrapper ensureDirectory() throws IOException {
        assertInitialized();
        Files.createDirectories(pwd);
        if (Files.exists(getGitDirectory())) {
            throw new IllegalStateException("Cannot execute mojo in a directory that already contains a .git directory");
        }
        return this;
    }

    private void assertInitialized() {
        if (pwd == null) {
            throw new IllegalStateException("Wrapper is not initialized, call initialize(pwd) before any other interaction");
        }
    }

    private Path getGitDirectory() {
        return pwd.resolve(".git");
    }

    @Override
    public GitWrapper initRepository() {
        assertInitialized();
        executeCommand(pwd, "git", "init");
        return this;
    }

    @Override
    public GitWrapper addRemote(String remote) {
        assertInitialized();
        if (StringUtils.isEmpty(remote)) {
            throw new IllegalArgumentException("Remote must be filled!");
        }
        executeCommand(pwd, "git", "remote", "add", "origin", remote);
        return this;
    }

    @Override
    public GitWrapper configureSparseCheckout(List<String> paths) throws IOException {
        assertInitialized();
        if (paths == null) {
            throw new IllegalArgumentException("Paths must not be null");
        }
        if (paths.isEmpty()) {
            return this;
        }
        executeCommand(pwd, "git", "config", "core.sparseCheckout", "true");
        Path sparseCheckoutFile = pwd.resolve(".git/info/sparse-checkout");
        Files.write(sparseCheckoutFile, paths);
        return this;
    }

    @Override
    public GitWrapper pull(String branch) {
        assertInitialized();
        if (StringUtils.isEmpty(branch)) {
            throw new IllegalArgumentException("Branch must be filled");
        }
        executeCommand(pwd, "git", "pull", "origin", branch);
        return this;
    }

    @Override
    public GitWrapper clean() {
        assertInitialized();
        if (!Files.exists(getGitDirectory())) return this;
        executeCommand(pwd, "rm", "-rf", ".git");
        return this;
    }

}
