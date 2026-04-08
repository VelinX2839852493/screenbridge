package com.screenbridge.mirror.infrastructure;

import com.screenbridge.mirror.domain.InputMode;
import com.screenbridge.mirror.domain.MirrorConfig;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScrcpyCommandBuilderTest {
    @Test
    public void buildIncludesExtendedWindowInputAndTransferOptions() {
        MirrorConfig config = new MirrorConfig(
                Path.of("C:/tools/scrcpy.exe"),
                Path.of("C:/tools/adb.exe"),
                "R52W123ABC",
                "Android Mirroring - R52W123ABC",
                2048,
                60,
                "12M",
                1024,
                768,
                true,
                true,
                InputMode.UHID,
                InputMode.SDK,
                "/sdcard/Download/",
                false,
                true,
                true);

        List<String> command = ScrcpyCommandBuilder.build(config);

        assertTrue(command.contains("--window-width=1024"));
        assertTrue(command.contains("--window-height=768"));
        assertTrue(command.contains("--fullscreen"));
        assertTrue(command.contains("--always-on-top"));
        assertTrue(command.contains("--keyboard=uhid"));
        assertTrue(command.contains("--mouse=sdk"));
        assertTrue(command.contains("--push-target=/sdcard/Download/"));
        assertTrue(command.contains("--turn-screen-off"));
        assertTrue(command.contains("--stay-awake"));
    }

    @Test
    public void buildSkipsDefaultInputModesAndBlankPushTarget() {
        MirrorConfig config = new MirrorConfig(
                Path.of("C:/tools/scrcpy.exe"),
                Path.of("C:/tools/adb.exe"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                InputMode.DEFAULT,
                InputMode.DEFAULT,
                "   ",
                false,
                false,
                false);

        List<String> command = ScrcpyCommandBuilder.build(config);

        assertFalse(command.stream().anyMatch(argument -> argument.startsWith("--keyboard=")));
        assertFalse(command.stream().anyMatch(argument -> argument.startsWith("--mouse=")));
        assertFalse(command.stream().anyMatch(argument -> argument.startsWith("--push-target=")));
    }
}
