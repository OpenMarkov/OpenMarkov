/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A network at an address that redirects to another is opened from the address that has the file, with its name
 * and the reader of its extension (error 370).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANetworkBehindARedirectionIsOpenedTest {

    private static URL url(String address) throws Exception {
        return URI.create(address).toURL();
    }

    /** A server that redirects each address of the map to its value. */
    private static Redirections.Server redirecting(Map<String, String> redirections) {
        return url -> redirections.get(url.toString());
    }

    @Test void theRedirectionsAreFollowedToTheAddressThatHasTheFile() throws Exception {
        Redirections.Server server = redirecting(Map.of("http://one.invalid/short", "/networks/asia.pgmx",
                                                        "http://one.invalid/networks/asia.pgmx",
                                                        "https://one.invalid/networks/asia.pgmx"));

        assertEquals(url("https://one.invalid/networks/asia.pgmx"),
                     Redirections.finalAddressOf(url("http://one.invalid/short"), server));
    }

    @Test void anAddressThatDoesNotRedirectIsItself() throws Exception {
        assertEquals(url("https://one.invalid/asia.pgmx"),
                     Redirections.finalAddressOf(url("https://one.invalid/asia.pgmx"), redirecting(Map.of())));
    }

    @Test void aFileIsNotAskedToAnyServer() throws Exception {
        URL file = Path.of("asia.pgmx").toUri().toURL();

        assertEquals(file, Redirections.finalAddressOf(file, url -> {
            throw new AssertionError("A file was asked to a server");
        }));
    }

    @Test void fromHttpsItIsNotFollowedToHttp() {
        Redirections.Server server = redirecting(Map.of("https://one.invalid/a.pgmx", "http://one.invalid/a.pgmx"));

        IOException refused = assertThrows(IOException.class,
                () -> Redirections.finalAddressOf(url("https://one.invalid/a.pgmx"), server));
        assertTrue(refused.getMessage().contains("less safe"), refused.getMessage());
    }

    @Test void itIsNotFollowedOutOfTheWeb() {
        Redirections.Server server = redirecting(Map.of("http://one.invalid/a.pgmx", "file:///etc/passwd"));

        assertThrows(IOException.class, () -> Redirections.finalAddressOf(url("http://one.invalid/a.pgmx"), server));
    }

    @Test void anAddressThatRedirectsToItselfIsRefused() {
        Redirections.Server server = redirecting(Map.of("http://one.invalid/a.pgmx", "/a.pgmx"));

        IOException refused = assertThrows(IOException.class,
                () -> Redirections.finalAddressOf(url("http://one.invalid/a.pgmx"), server));
        assertTrue(refused.getMessage().contains("more than 10 times"), refused.getMessage());
    }

    /** With a real server: an address without extension that answers 302 towards a .pgmx. */
    @Test void aNetworkBehindARedirectionIsOpenedWithItsName() throws Exception {
        byte[] network = Files.readAllBytes(
                Path.of(getClass().getClassLoader().getResource("ID-decide-test.pgmx").toURI()));
        try (ServerSocket server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress())) {
            Thread answering = new Thread(() -> answer(server, network));
            answering.setDaemon(true);
            answering.start();
            URL written = url("http://127.0.0.1:" + server.getLocalPort() + "/short");

            ProbNet opened = NetsIO.openNetworkURL(written).probNet();

            assertEquals("ID-decide-test.pgmx", opened.getName());
            assertTrue(opened.getNumNodes() > 0);
        }
    }

    /** Answers each request until the server is closed: 302 for /short, and the network for any other address. */
    private static void answer(ServerSocket server, byte[] network) {
        while (!server.isClosed()) {
            try (Socket client = server.accept()) {
                BufferedReader request = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.ISO_8859_1));
                String firstLine = request.readLine();
                for (String line = firstLine; line != null && !line.isEmpty(); line = request.readLine()) {
                    // the headers of the request are not needed
                }
                OutputStream response = client.getOutputStream();
                if (firstLine != null && firstLine.contains(" /short ")) {
                    response.write(("HTTP/1.1 302 Found\r\nLocation: /networks/ID-decide-test.pgmx\r\n"
                                    + "Content-Length: 0\r\nConnection: close\r\n\r\n")
                                           .getBytes(StandardCharsets.ISO_8859_1));
                } else {
                    response.write(("HTTP/1.1 200 OK\r\nContent-Length: " + network.length
                                    + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
                    response.write(network);
                }
                response.flush();
            } catch (IOException closed) {
                // the server was closed, or a client left: nothing to answer
            }
        }
    }
}
