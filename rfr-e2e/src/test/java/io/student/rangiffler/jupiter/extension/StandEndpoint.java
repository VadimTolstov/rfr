package io.student.rangiffler.jupiter.extension;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Probes one service using the same readiness signal as the stand script. */
final class StandEndpoint {
    private final String name;
    private final URI address;
    private final String command;
    private final String payload;

    StandEndpoint(String name, URI address, String command, String payload) {
        this.name = name;
        this.address = address;
        this.command = command;
        this.payload = payload;
    }

    boolean readiness(Duration timeout) throws InterruptedException {
        try {
            if (address.getScheme().equals("mysql")) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(address.getHost(),
                            address.getPort() < 0 ? 3306 : address.getPort()),
                            (int) Math.max(1, timeout.toMillis()));
                    return true;
                }
            }
            // Vite routes protocol upgrades separately from ordinary HTTP readiness requests.
            try (HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(timeout).build()) {
                try {
                    return payload.isEmpty()
                            ? client.send(HttpRequest.newBuilder(address).timeout(timeout).GET().build(),
                                    HttpResponse.BodyHandlers.discarding()).statusCode() < 400
                            : client.send(HttpRequest.newBuilder(address).timeout(timeout)
                                    .header("Content-Type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(payload)).build(),
                                    HttpResponse.BodyHandlers.discarding()).statusCode() > 0;
                } finally {
                    client.shutdownNow();
                }
            }
        } catch (IOException failure) {
            System.getLogger("io.student.rangiffler.stand").log(System.Logger.Level.DEBUG,
                    "Stand probe failed for {0}: {1}", name, failure.getMessage());
            return false;
        }
    }

    String instruction() {
        return name + " (start with '" + command + "')";
    }
}
