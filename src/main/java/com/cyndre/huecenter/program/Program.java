package com.cyndre.huecenter.program;

import com.cyndre.huecenter.hue.LightState;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Program {
    public static final String PROPERTY_INPUT = "input";
    public static final String PROPERTY_OUTPUT = "output";
    public static final String PROPERTY_CONTEXT = "context";
    public static final String PROPERTY_API = "api";

    public static class Loader {
        private final GroovyShell shell = new GroovyShell();

        public Program Load(final String script) {
            return new Program(this.shell.parse(script), new HashMap<>(), new HashMap<>());
        }

        public String LoadSource(final Path path) {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Context {
        private static final long INIT_VALUE = -1;

        private long startingSystemTimeMs = 0L;

        public long systemTimeMs = 0L;
        public long timeMs = 0L;
        public long frameNumber = 0L;
        public long loopNumber = 0L;

        public long frameDurationMs = 2000;

        private boolean running = true;
        private ArrayList<String> logs = new ArrayList<>();

        public void debug(String format, Object... parts) {
            this.logs.add(String.format(format, (Object[]) parts));
        }

        public ArrayList<String> getLogs() {
            return logs;
        }

        public long getFrameDurationMs() {
            return frameDurationMs;
        }

        public void setFrameDurationMs(long frameDurationMs) {
            this.frameDurationMs = frameDurationMs;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void StartTime(final long systemTimeMs) {
            this.startingSystemTimeMs = systemTimeMs;
            this.loopNumber = INIT_VALUE;
            this.NextLoop(systemTimeMs);
        }

        public void NextLoop(final long systemTimeMs) {
            ++this.loopNumber;
            this.frameNumber = INIT_VALUE;

        }

        public void NextFrame(final long systemTimeMs) {
            if (!this.logs.isEmpty()) {
                this.logs.clear();
            }

            ++this.frameNumber;
            this.systemTimeMs = systemTimeMs;

            this.timeMs = this.systemTimeMs - this.startingSystemTimeMs;
        }
    }


    private final Script script;
    private final Binding shellBinding;
    private final Context context;

    private final Map<String, LightState> outputBuffer;

    private Program(final Script script, Map<String, LightState> inputBuffer, Map<String, LightState> outputBuffer) {
        this.context = new Context();
        this.shellBinding = new Binding();

        this.script = script;
        this.script.setBinding(this.shellBinding);

        this.outputBuffer = outputBuffer;

        Setup(inputBuffer, System.currentTimeMillis());
    }

    public void Setup(final Map<String, LightState> inputBuffer, final long systemTimeMs) {
        this.shellBinding.setProperty(PROPERTY_INPUT, inputBuffer);

        this.shellBinding.setProperty(PROPERTY_CONTEXT, this.context);
        this.shellBinding.setProperty(PROPERTY_API, new Api());
        this.context.StartTime(systemTimeMs);
    }

    public void Step(final long systemTimeMs) {
        this.outputBuffer.clear();
        this.shellBinding.setProperty(PROPERTY_OUTPUT, this.outputBuffer);

        this.context.NextFrame(systemTimeMs);

        try {
            script.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Map<String, LightState> getOutputBuffer() {
        return (Map<String, LightState>)this.shellBinding.getProperty(PROPERTY_OUTPUT);
    }

    public Context getContext() {
        return context;
    }
}
