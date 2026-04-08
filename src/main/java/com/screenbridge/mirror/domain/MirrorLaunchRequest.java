package com.screenbridge.mirror.domain;

import java.nio.file.Path;

/**
 * 封装一次投屏启动所需的已校验参数和 adb 路径。
 */
public record MirrorLaunchRequest(MirrorConfig config, Path adbPath) {
}
