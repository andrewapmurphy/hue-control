package com.cyndre.huecenter;

import com.cyndre.huecenter.hue.Error;
import com.cyndre.huecenter.hue.RegisterUsernameRequest;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class HueClient {
    @FunctionalInterface
    public interface SimpleResultConsumer {
        void accept(boolean success, Collection<Error> orErrors);
    }

    @FunctionalInterface
    public interface DetailedResultConsumer <T> {
        void accept(boolean success, T result, Collection<Error> orErrors);
    }

    private static final String DEFAULT_HOST_AND_PORT = null;
    private static final String DEFAULT_AUTHORIZED_USER = null;
    private static final Duration TIMEOUT = Duration.ofMinutes(2);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private Optional<String> authorizedUser;
    private String hueHubHostAndPort;

    public HueClient(final HttpClient httpClient, final ObjectMapper mapper) {
        this(httpClient, mapper, DEFAULT_HOST_AND_PORT);
    }

    public HueClient(final HttpClient httpClient, final ObjectMapper mapper, final String hueHubHostAndPort) {
        this(httpClient, mapper, hueHubHostAndPort, DEFAULT_AUTHORIZED_USER);
    }

    public HueClient(final HttpClient httpClient, final ObjectMapper mapper, final String hueHubHostAndPort, final String authorizedUser) {
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.hueHubHostAndPort = hueHubHostAndPort;
        this.authorizedUser = Optional.ofNullable(authorizedUser);
    }

    public String getHueHubHostAndPort() {
        return hueHubHostAndPort;
    }

    public void setHueHubHostAndPort(String hueHubHostAndPort) {
        this.hueHubHostAndPort = hueHubHostAndPort;
    }

    public boolean isLoggedIn() {
        return this.authorizedUser.isPresent();
    }

    public void register(final String username, final String devicetype, final SimpleResultConsumer onComplete) {
        this.authorizedUser = Optional.empty();

        this.request(
                "/api",
                new RegisterUsernameRequest(username, devicetype),
                (response) -> onRegisterResponse(response, onComplete)
        );
    }

    private void onRegisterResponse(final String response, final SimpleResultConsumer onComplete) {
        System.out.println(response);

        List<Error> errors = null;
        Error currentError = null;
        boolean success = false;

        try (final JsonParser jp = this.mapper.getFactory().createParser(response)) {
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                String fieldName = jp.getCurrentName();
                // Let's move to value
                jp.nextToken();

                switch (fieldName) {
                    case "error":
                        if (errors == null) {
                            errors = new ArrayList<>();
                        }

                        errors.add(new Error());
                        currentError = errors.get(errors.size()-1);
                        break;
                    case "type":
                        assert currentError != null;
                        currentError.setType(jp.getIntValue());
                        break;
                    case "address":
                        assert currentError != null;
                        currentError.setAddress(jp.getValueAsString());
                        break;
                    case "description":
                        assert currentError != null;
                        currentError.setDescription(jp.getValueAsString());
                        break;
                    case "username":
                        success = true;
                        this.authorizedUser = Optional.of(jp.getValueAsString());
                        break;
                }
            }
        } catch (Exception e) {
            success = false;

            if (errors == null) {
                errors = new ArrayList<>();
            }

            errors.add(new Error(0, "While parsing response", e.getMessage()));
        }

        onComplete.accept(success, errors);
    }

    private void request(final String uri, final Object requestBody, Consumer<String> handler) {
        try {
            final String strRequestBody = this.mapper.writeValueAsString(requestBody);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.hueHubHostAndPort + uri))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(strRequestBody))
                    .build();
            this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
