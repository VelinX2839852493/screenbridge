package com.screenbridge.mirror.domain;

/**
 * 表示 scrcpy 运行过程中产生的输出或生命周期事件。
 */
public record MirrorProcessEvent(MirrorProcessEventType type, String detail) {
    public static MirrorProcessEvent output(String detail) {
        return new MirrorProcessEvent(MirrorProcessEventType.OUTPUT, detail);
    }

    public static MirrorProcessEvent forceShutdown() {
        return new MirrorProcessEvent(MirrorProcessEventType.FORCE_SHUTDOWN, null);
    }

    public static MirrorProcessEvent forceShutdownFailed() {
        return new MirrorProcessEvent(MirrorProcessEventType.FORCE_SHUTDOWN_FAILED, null);
    }

    public static MirrorProcessEvent outputReadFailed(String detail) {
        return new MirrorProcessEvent(MirrorProcessEventType.OUTPUT_READ_FAILED, detail);
    }
}
