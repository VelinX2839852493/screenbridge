package com.screenbridge.mirror.application;

import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.InputMode;
import com.screenbridge.mirror.domain.MirrorFormState;
import com.screenbridge.mirror.domain.MirrorLaunchRequest;
import com.screenbridge.mirror.i18n.LocaleManager;
import com.screenbridge.mirror.i18n.Messages;
import com.screenbridge.mirror.settings.SettingsStore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MirrorRequestFactoryTest {
    @Test
    public void createLaunchRequestAllowsEmptyManualWindowSize() throws Exception {
        MirrorRequestFactory factory = new MirrorRequestFactory(messages());
        Path scrcpy = createTempExecutable("scrcpy");
        Path adb = createTempExecutable("adb");

        MirrorFormState formState = new MirrorFormState(
                scrcpy.toString(),
                adb.toString(),
                new DeviceInfo("R52W123ABC", "device", "Galaxy Tab"),
                "192.168.1.10:5555",
                "1600",
                "60",
                "6M",
                "",
                "",
                false,
                "4:3",
                false,
                false,
                InputMode.DEFAULT,
                InputMode.DEFAULT,
                "/sdcard/Download/",
                false,
                false,
                true,
                1920,
                1080);

        MirrorLaunchRequest request = factory.createLaunchRequest(formState, "Android Mirroring");

        assertNull(request.config().windowWidth());
        assertNull(request.config().windowHeight());
        assertEquals(Integer.valueOf(1600), request.config().maxSize());
        assertEquals(Integer.valueOf(60), request.config().maxFps());
    }

    private Messages messages() {
        SettingsStore settingsStore = new SettingsStore() {
            @Override
            public Optional<String> get(String key) {
                return Optional.empty();
            }

            @Override
            public void put(String key, String value) {
            }
        };
        return new Messages(new LocaleManager(settingsStore, Locale.ENGLISH));
    }

    private Path createTempExecutable(String prefix) throws IOException {
        Path file = Files.createTempFile(prefix, ".exe");
        file.toFile().deleteOnExit();
        return file;
    }
}
