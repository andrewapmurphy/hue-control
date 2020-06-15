package com.cyndre.huecenter;

import com.cyndre.huecenter.hue.*;
import com.cyndre.huecenter.hue.Error;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

//Special thanks to https://www.burgestrand.se/hue-api/
public class HueClient {
    @FunctionalInterface
    public interface ResultHandler <T> {
        void accept(Optional<T> result, Collection<Error> orErrors);
    }

    private static final String DEFAULT_HOST_AND_PORT = null;
    private static final String DEFAULT_AUTHORIZED_USER = null;
    private static final Duration TIMEOUT = Duration.ofMinutes(2);

    private static final TypeReference<HashMap<String, LightGroup>> LIGHT_GROUP_MAP_TYPE = new TypeReference<>(){};

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

    public void register(final String username, final String devicetype, final ResultHandler<String> onComplete) {
        assert this.hueHubHostAndPort != null && !this.hueHubHostAndPort.isEmpty();

        this.authorizedUser = Optional.empty();

        this.requestPost(
                "/api",
                new RegisterUsernameRequest(devicetype),
                (response) -> onRegisterResponse(response, onComplete)
        );
    }

    public void listGroups(final ResultHandler<Map<String, LightGroup>> onComplete) {
        this.requestGet(
                "/api/" + this.authorizedUser.get() + "/groups",
                (response) -> onListGroupResponse(response, onComplete)
        );
    }

    public void setLight(final String lightId, final LightState newState) {
        this.requestPut(
                "/api/" + this.authorizedUser.get() + "/lights/" + lightId + "/state",
                newState,
                (response) -> { }
        );
    }

    public void getLight(final String lightId, final ResultHandler<GetLightResponse> onComplete) {
        this.requestGet(
                "/api/" + this.authorizedUser.get() + "/lights/" + lightId,
                (response) -> onGetLightResponse(response, onComplete)
        );
    }

    private void onGetLightResponse(final String responseBody, final ResultHandler<GetLightResponse> onComplete) {
        Optional<GetLightResponse> response = Optional.empty();
        List<Error> errors = null;

        try {
            response = Optional.ofNullable(mapper.readValue(responseBody, GetLightResponse.class));
        } catch (JsonProcessingException e) {
            errors.add(new Error(0, "onListGroupResponse", e.getMessage()));
        }

        onComplete.accept(response, errors);
    }

    private void onListGroupResponse(final String responseBody, final ResultHandler<Map<String, LightGroup>> onComplete) {
        Optional<Map<String, LightGroup>> groups = Optional.empty();
        List<Error> errors = null;

        try {
            groups = Optional.ofNullable(mapper.readValue(responseBody, LIGHT_GROUP_MAP_TYPE));
        } catch (JsonProcessingException e) {
            errors.add(new Error(0, "onListGroupResponse", e.getMessage()));
        }

        onComplete.accept(groups, errors);
    }

    private void onRegisterResponse(final InputStream responseBody, final ResultHandler<String> onComplete) {
        List<Error> errors = null;
        Error currentError = null;

        Optional<String> username = Optional.empty();

        try {
            try (final JsonParser jp = this.mapper.getFactory().createParser(responseBody)) {
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    String fieldName = jp.getCurrentName();

                    if (fieldName == null) {
                        continue;
                    }

                    // Let's move to value
                    jp.nextToken();

                    switch (fieldName) {
                        case "error":
                            if (errors == null) {
                                errors = new ArrayList<>();
                            }

                            errors.add(new Error());
                            currentError = errors.get(errors.size() - 1);
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
                            username = Optional.of(jp.getValueAsString());
                            this.authorizedUser = username;
                            break;
                    }
                }
            } catch (Exception e) {
                if (errors == null) {
                    errors = new ArrayList<>();
                }

                errors.add(new Error(0, "While parsing response", e.getMessage()));
            }
        } catch (Exception e) {
            if (errors == null) {
                errors = new ArrayList<>();
            }

            errors.add(new Error(0, "While parsing response", e.getMessage()));
        }

        onComplete.accept(username, errors);
    }

    private HttpRequest.Builder newBuilder(final String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://" + this.hueHubHostAndPort + uri))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json");
    }

    private void requestPut(final String uri, final Object requestBody, Consumer<java.io.InputStream> handler) {
        try {
            final String strRequestBody = this.mapper.writeValueAsString(requestBody);

            final HttpRequest request = this.newBuilder(uri)
                    .PUT(HttpRequest.BodyPublishers.ofString(strRequestBody))
                    .build();

            this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenApply(HttpResponse::body)
                    .thenAccept(handler);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void requestPost(final String uri, final Object requestBody, Consumer<java.io.InputStream> handler) {
        try {
            final String strRequestBody = this.mapper.writeValueAsString(requestBody);

            final HttpRequest request = this.newBuilder(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(strRequestBody))
                    .build();

            this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenApply(HttpResponse::body)
                    .thenAccept(handler);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void requestGet(final String uri, Consumer<String> handler) {
        try {
            final HttpRequest request = this.newBuilder(uri)
                    .build();

            this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(handler);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Consumer<java.io.InputStream> ensureInputStreamClose(Consumer<java.io.InputStream> handler) {
        return (input) -> {
            try (final java.io.InputStream is = input) {
                handler.accept(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
