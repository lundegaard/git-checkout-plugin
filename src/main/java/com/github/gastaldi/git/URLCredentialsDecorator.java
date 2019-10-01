package com.github.gastaldi.git;

/**
 * @author Lukas Zaruba, lukas.zaruba@lundegaard.eu, 2019
 */
public interface URLCredentialsDecorator {

    /**
     * Decorates given url with credentials if present or returns as is when there are no credentials
     *
     * @throws IllegalArgumentException if url is not of https schema
     * @throws IllegalArgumentException if password is provided but no username
     */
    String decorate(String url, String username, String password);

}
