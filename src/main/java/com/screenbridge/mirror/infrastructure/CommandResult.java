package com.screenbridge.mirror.infrastructure;

/**
 * 保存外部命令执行后的退出码和输出内容。
 */
public record CommandResult(int exitCode, String output) {
}
