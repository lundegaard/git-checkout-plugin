package com.github.gastaldi.git;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Lukas Zaruba, lukas.zaruba@lundegaard.eu, 2019
 */
public interface GitWrapper {

    /**
     * Initializes this wrapper with pwd. This method must be called before any other interaction
     */
    GitWrapper initialize(Path pwd);

    /**
     * Checks whether the pwd directory exists and creates if it doesn't
     * @throws IllegalStateException if there already is a .git directory inside the pwd
     */
    GitWrapper ensureDirectory();

    /**
     * Initializes new git repository in given path
     */
    GitWrapper initRepository();

    /**
     * Adds remote named origin
     */
    GitWrapper addRemote(String remote);

    /**
     * Configures repository to do sparse checkout
     * populating configuration with given checkout paths
     */
    GitWrapper configureSparseCheckout(List<String> paths);

    /**
     * Performs git pull using origin remote and given branch
     */
    GitWrapper pull(String branch);

    /**
     * Deletes .git directory in given path removing traces that this was ever a git repository
     */
    GitWrapper clean();

}
