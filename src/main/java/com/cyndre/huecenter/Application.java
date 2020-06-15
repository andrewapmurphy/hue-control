package com.cyndre.huecenter;

import com.cyndre.huecenter.hue.GetLightResponse;
import com.cyndre.huecenter.hue.LightGroup;
import com.cyndre.huecenter.hue.LightState;
import com.cyndre.huecenter.program.Program;
import com.cyndre.huecenter.time.TimeData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Application implements CommandInterface {
    private static final int SUCCESS_CODE = 0;
    private static final int ERROR_CODE = 1;
    private static final int CONTUNUE = -1;

    private final HueClient hueClient;
    private View view;

    private Integer exitNextFrameCode = null;

    private Map<String, LightState> lights = new HashMap<>();

    private TimeData time = null;

    private final Program.Loader programLoader;

    private Executor executor = Executors.newSingleThreadExecutor();

    private Application(final HueClient hueClient, final Program.Loader programLoader) {
        this.hueClient = hueClient;
        this.programLoader = programLoader;
    }

    @Override
    public void RegisterUsername(final String username, final HueClient.ResultHandler<String> onComplete) {
        this.hueClient.register(username, username, (result, errors) -> {
            ScheduleOnUiThread(() -> onComplete.accept(result, errors));
        });
    }

    @Override
    public Program.Context Execute(final ExecutionContext exeContext) {
        exeContext.lights = this.lights;

        this.Log("Executed %s %s", exeContext.scriptText, exeContext.lights.keySet().stream().collect(Collectors.joining(",")));

        try {
            final Program program = this.programLoader.Load(exeContext.scriptText);
            program.Setup(exeContext.lights, System.currentTimeMillis());

            this.executor.execute(() -> {
                final Program.Context context = program.getContext();
                final long frameDurationMs = context.getFrameDurationMs();

                while (context.isRunning()) {
                    final long frameStartTimeMs = System.currentTimeMillis();

                    program.Step(frameStartTimeMs);

                    final Map<String, LightState> stepUpdate = program.getOutputBuffer();

                    if (stepUpdate == null || stepUpdate.isEmpty()) {
                        return;
                    }

                    stepUpdate.entrySet().stream().forEach((kv) -> {
                        this.lights.put(kv.getKey(), kv.getValue());

                        //Start of view
                        this.SetLight(kv.getKey(), kv.getValue());
                    });

                    final long frameEndTime = System.currentTimeMillis();
                    final long thisFrameDuration = frameEndTime - frameStartTimeMs;

                    //View
                    context.getLogs().forEach(this::Log);
                    this.view.onLights(Optional.of(program.getOutputBuffer()), Collections.EMPTY_LIST);

                    //Pause
                    final long sleepDurationMs = frameDurationMs - thisFrameDuration;

                    if (sleepDurationMs > 0L) {
                        try {
                            Thread.sleep(sleepDurationMs);
                        } catch (InterruptedException ie) { }
                    }
                }
            });

            return program.getContext();
        } catch (Exception e) {
            this.Log("Error setting up program to execute: %s", e);

            return null;
        }
    }

    @Override
    public void ListGroups(HueClient.ResultHandler<Map<String, LightGroup>> onComplete) {
        this.hueClient.listGroups((result, errors) -> {
            ScheduleOnUiThread(() -> onComplete.accept(result, errors));
        });
    }

    @Override
    public void GetLights(final Collection<String> lights, HueClient.ResultHandler<Map<String, LightState>> onComplete) {
        final AtomicInteger lightCounter = new AtomicInteger(lights.size());
        final ConcurrentMap<String, LightState> lightData = new ConcurrentHashMap<>();

        lights.stream().forEach((lightId) -> {
                this.hueClient.getLight(lightId, (result, errors) -> {
                    final GetLightResponse lightResponse = result.orElseGet(() -> new GetLightResponse());
                    final LightState lightState = lightResponse.getState() != null ? lightResponse.getState() : new LightState();

                    lightData.put(lightId, lightState);

                    final int remaining = lightCounter.decrementAndGet();

                    if (remaining != 0) {
                        return;
                    }

                    this.lights = lightData;

                    ScheduleOnUiThread(() -> onComplete.accept(Optional.of(lightData), errors));
                });
            }
        );
    }

    @Override
    public void Quit() {
        Quit(SUCCESS_CODE);
    }

    @Override
    public void Log(String message, Object... extra) {
        ScheduleOnUiThread(() -> this.view.log(message, extra));
    }

    private void SetLight(final String lightId, final LightState newState) {
        this.hueClient.setLight(lightId, newState);
    }

    public void setView(View view) {
        this.view = view;
    }

    private void Quit(int status) {
        System.exit(status);
    }

    private void ScheduleOnUiThread(final Runnable runnable) {
        javax.swing.SwingUtilities.invokeLater(runnable);
    }

    private static Optional<String> getHueHubIpFromArguments(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equalsIgnoreCase("-p") && ((i+1) < args.length)) {
                return Optional.of(args[i+1]);
            }
        }

        return Optional.empty();
    }

    public static void main(String[] args) {
        final HttpClient httpClient = HttpClient.newBuilder().build();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final HueClient hueClient = new HueClient(httpClient, mapper);
        hueClient.setHueHubHostAndPort(getHueHubIpFromArguments(args).get());

        final Program.Loader programLoader = new Program.Loader();

        final Application application = new Application(hueClient, programLoader);

        final View view = new View(application);

        application.setView(view);

        application.ScheduleOnUiThread(view::run);


    }
}
