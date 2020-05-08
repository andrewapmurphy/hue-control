package com.cyndre.huecenter;

import com.cyndre.huecenter.hue.Error;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final int SUCCESS_CODE = 0;
    private static final int ERROR_CODE = 1;
    private static final int CONTUNUE = -1;

    private final HueClient hueClient;

    private Integer exitNextFrameCode = null;
    private String username;

    private Application(HueClient hueClient, final String username) {
        this.hueClient = hueClient;
        this.username = username;
    }

    private int run() {
        int result;
        try {
            this.hueClient.register(this.username, "unknown", );

            do {
                result = loop();
                Thread.sleep(TimeUnit.MILLISECONDS.toMillis(250));
            } while (result != CONTUNUE);
        } catch (Exception e) {
            result = ERROR_CODE;
        }

        return result;
    }

    private void onRegisterComplete(boolean success, Collection<Error> errors) {
        if (success) {
            this.exitNextFrame(SUCCESS_CODE);
        } else {

        }
    }

    private int loop() {
        if (this.exitNextFrameCode != null) {
            return this.exitNextFrameCode;
        }

        return CONTUNUE;
    }

    private void exitNextFrame(int code) {
        assert this.exitNextFrameCode == null;

        this.exitNextFrameCode = code;
    }

    public static void main(String[] args) {
        final HttpClient httpClient = HttpClient.newBuilder()
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final HueClient hueClient = new HueClient(httpClient, mapper);
        final Application application = new Application(hueClient, "joe");

        final int result = application.run();

        System.exit(result);
    }
}
