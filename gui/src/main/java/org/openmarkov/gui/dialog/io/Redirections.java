/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Follows the redirections of a web address to the address that finally has the file.
 *
 * @author Manuel Arias
 */
final class Redirections {

    private static final int MAXIMUM = 10;
    private static final int TIMEOUT_IN_MILLISECONDS = 15_000;

    private Redirections() {
    }

    /** What a server answers when asked for an address: where it sends the client, or null if it sends it nowhere. */
    @FunctionalInterface interface Server {
        @Nullable String redirectionOf(URL url) throws IOException;
    }

    /** @return the address that has the file after following the redirections of {@code url}; itself if it is not http or https */
    static URL finalAddressOf(URL url) throws IOException {
        return finalAddressOf(url, Redirections::askTheServer);
    }

    static URL finalAddressOf(URL url, Server server) throws IOException {
        URL current = url;
        for (int followed = 0; isWeb(current); followed++) {
            String location = server.redirectionOf(current);
            if (location == null) {
                return current;
            }
            if (followed == MAXIMUM) {
                throw new IOException("The address " + url + " redirects more than " + MAXIMUM + " times");
            }
            URL next;
            try {
                next = current.toURI().resolve(location).toURL();
            } catch (URISyntaxException | IllegalArgumentException e) {
                throw new IOException("The address " + current + " redirects to «" + location
                                      + "», which is not an address", e);
            }
            // From https only to https, and never out of the web
            if (!isWeb(next) || (current.getProtocol().equals("https") && !next.getProtocol().equals("https"))) {
                throw new IOException("The address " + current + " redirects to " + next
                                      + ", which is not followed because it is less safe");
            }
            current = next;
        }
        return current;
    }

    private static boolean isWeb(URL url) {
        return url.getProtocol().equals("http") || url.getProtocol().equals("https");
    }

    private static @Nullable String askTheServer(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(TIMEOUT_IN_MILLISECONDS);
            connection.setReadTimeout(TIMEOUT_IN_MILLISECONDS);
            int status = connection.getResponseCode();
            boolean redirects = status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
            return redirects ? connection.getHeaderField("Location") : null;
        } finally {
            connection.disconnect();
        }
    }
}
