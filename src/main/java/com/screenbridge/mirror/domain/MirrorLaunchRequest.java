package com.screenbridge.mirror.domain;

import java.nio.file.Path;

public record MirrorLaunchRequest(MirrorConfig config, Path adbPath) {
}
