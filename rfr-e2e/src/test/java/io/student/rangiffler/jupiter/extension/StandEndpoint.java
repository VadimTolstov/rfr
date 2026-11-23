package io.student.rangiffler.jupiter.extension;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.util.Timeout;

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
            try (CloseableHttpClient client = client(timeout)) {
                ClassicRequestBuilder request = payload.isEmpty() ? ClassicRequestBuilder.get(address)
                        : ClassicRequestBuilder.post(address).setEntity(payload, ContentType.APPLICATION_JSON);
                int status = client.execute(request.build(), HttpResponse::getCode);
                return payload.isEmpty() ? status < 400 : status > 0;
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

    private CloseableHttpClient client(Duration timeout) {
        Timeout limit = Timeout.ofMilliseconds(Math.max(1, timeout.toMillis()));
        ConnectionConfig connection = ConnectionConfig.custom().setConnectTimeout(limit).setSocketTimeout(limit).build();
        // Vite handles protocol upgrades separately from ordinary HTTP readiness requests.
        return HttpClients.custom().useSystemProperties().disableAutomaticRetries().disableRedirectHandling()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create().useSystemProperties()
                        .setDefaultConnectionConfig(connection).build())
                .setDefaultRequestConfig(RequestConfig.custom().setProtocolUpgradeEnabled(false).build())
                .build();
    }
}
