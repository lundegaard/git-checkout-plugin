package com.github.gastaldi.git;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Clones only specific paths from a Git repository
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Mojo(name = "git-checkout")
public class GitCheckoutMojo extends AbstractMojo {

    private static final String HTTPS_SCHEMA = "https";

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    @Parameter(property = "repository", required = true)
    private String repository;

    @Parameter(property = "branch", defaultValue = "master")
    private String branch;

    @Parameter(property = "paths", required = true)
    private List<String> paths;

    @Parameter(property = "username")
    private String username;

    @Parameter(property = "password")
    private String password;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            outputDirectory.mkdirs();
            if (Files.exists(outputDirectory.toPath().resolve(".git"))) {
                throw new MojoExecutionException("Cannot execute mojo in a directory that already contains a .git directory");
            }
            executeCommand(outputDirectory, "git", "init");
            executeCommand(outputDirectory, "git", "remote", "add", "origin", repository);
            executeCommand(outputDirectory, "git", "config", "core.sparseCheckout", "true");
            Path sparseCheckoutFile = outputDirectory.toPath().resolve(".git/info/sparse-checkout");
            Files.write(sparseCheckoutFile, paths);
            handleCredentials();
            executeCommand(outputDirectory, "git", "pull", "origin", branch);
            executeCommand(outputDirectory, "rm", "-rf", ".git");
            getLog().info("Files were checked out in: " + outputDirectory);
        } catch (IOException e) {
            throw new MojoFailureException("Caught IOException in mojo", e);
        }
    }

    private void handleCredentials() throws IOException {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) return;
        getLog().info("credentials: both username and password present - configuring credentials helper");
        URI uri = URI.create(repository);
        if (HTTPS_SCHEMA.equals(uri.getScheme())) {
            getLog().warn("skipping credentials as it cannot be used with non-https schemas: " + uri);
            return;
        }
        getLog().info("credentials: host '" + uri.getHost()  + "'");
        executeCommand(outputDirectory,
                p -> {
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                    try {
                        w.write("schema=" + HTTPS_SCHEMA + "\n");
                        w.write("host=" + uri.getHost() + "\n");
                        w.write("username=" + username + "\n");
                        w.write("password=" + password + "\n");
                        w.write("\n");
                    } catch (IOException e) {
                        throw new RuntimeException("Error while creating credentials storage", e);
                    }
                },
                "git", "credential-store", "--file", "git.store", "store");
        executeCommand(outputDirectory, "git", "config", "credential.helper", "'store --file git.store'");
    }

    private void executeCommand(File directory, Consumer<Process> processConsumer, String... command) throws IOException {
        Process process = new ProcessBuilder()
                .directory(directory)
                .command(command)
                .inheritIO()
                .start();
        try {
            processConsumer.accept(process);
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private void executeCommand(File directory, String... command) throws IOException {
       executeCommand(directory, p -> {}, command);
    }
}
