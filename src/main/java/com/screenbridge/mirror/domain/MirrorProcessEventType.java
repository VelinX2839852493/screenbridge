package com.screenbridge.mirror.domain;

/**
 * 定义 scrcpy 进程上报的事件类型。
 */
public enum MirrorProcessEventType {
    OUTPUT,
    FORCE_SHUTDOWN,
    FORCE_SHUTDOWN_FAILED,
    OUTPUT_READ_FAILED
}
