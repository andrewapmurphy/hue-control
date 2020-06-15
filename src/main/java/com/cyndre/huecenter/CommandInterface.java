package com.cyndre.huecenter;

import com.cyndre.huecenter.hue.LightGroup;
import com.cyndre.huecenter.hue.LightState;
import com.cyndre.huecenter.program.Program;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface CommandInterface {
    class ExecutionContext {
        public String scriptText;
        public Map<String, LightState> lights;
        public long timeMs = 0L;
        public long durationMs = TimeUnit.SECONDS.toMillis(10);
        public boolean loop = false;
    }

    void RegisterUsername(final String username, final HueClient.ResultHandler<String> onComplete);

    Program.Context Execute(final ExecutionContext context);

    void Quit();

    void Log(String message, final Object... extra);

    void ListGroups(HueClient.ResultHandler<Map<String, LightGroup>> onComplete);

    void GetLights(final Collection<String> lights, HueClient.ResultHandler<Map<String, LightState>> onComplete);
}
