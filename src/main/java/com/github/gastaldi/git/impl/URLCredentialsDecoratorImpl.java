package com.github.gastaldi.git.impl;

import com.github.gastaldi.git.URLCredentialsDecorator;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Lukas Zaruba, lukas.zaruba@lundegaard.eu, 2019
 */
public class URLCredentialsDecoratorImpl implements URLCredentialsDecorator {

    private static final String HTTPS_SCHEME = "https";

    @Override
    public String decorate(String url, String username, String password) {
        if (url == null) {
            throw new IllegalArgumentException("url must be filled");
        }
        URI uri = URI.create(url);
        if (!HTTPS_SCHEME.equals(uri.getScheme())) {
            throw new IllegalArgumentException("Cannot decorate other schemes than https, was " + uri.getScheme());
        }
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return url;
        }
        if (StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be filled without filling a username");
        }
        return uri.getScheme() + "://" + getAuthority(username, password) + "@" + uri.getHost() + uri.getPath();
    }

    private String getAuthority(String username, String password) {
        String authority = encode(username) + ":";
        if (StringUtils.isEmpty(password)) return authority;
        return authority + encode(password);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown encoding", e);
        }
    }

}
