package com.screenbridge;

import com.screenbridge.mirror.application.MirrorController;
import com.screenbridge.mirror.application.MirrorRequestFactory;
import com.screenbridge.mirror.i18n.LocaleManager;
import com.screenbridge.mirror.i18n.Messages;
import com.screenbridge.mirror.infrastructure.AndroidMirrorService;
import com.screenbridge.mirror.infrastructure.DefaultExecutableFinder;
import com.screenbridge.mirror.settings.PreferencesSettingsStore;
import com.screenbridge.mirror.ui.MirrorLauncherFrame;

import javax.swing.*;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 程序启动入口，负责组装界面 (UI)、逻辑服务 (Service) 和后台线程池。
 * 采用了简单的依赖注入方式手动初始化各个组件。
 */
public final class App {

    // 私有构造函数，防止该工具类被实例化
    private App() {
    }

    /**
     * 程序的主入口方法
     */
    public static void main(String[] args) {
        // 1. 初始化国际化和设置管理
        // PreferencesSettingsStore 用于持久化存储用户设置（如语言、路径等）
        LocaleManager localeManager = new LocaleManager(new PreferencesSettingsStore(App.class), Locale.getDefault());
        // Messages 负责根据当前语言环境获取文本资源
        Messages messages = new Messages(localeManager);

        // 2. 初始化后台执行器
        // 使用缓存线程池处理耗时操作（如执行 ADB 命令），避免阻塞 UI 线程
        ExecutorService backgroundExecutor = Executors.newCachedThreadPool(new MirrorThreadFactory());

        // 3. 在 Swing 事件调度线程 (EDT) 中启动 UI
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置外观为当前操作系统的原生风格 (Windows, macOS, Linux 等)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // 如果设置失败，则使用 Swing 默认外观
            }

            // 4. 创建主界面窗口
            MirrorLauncherFrame frame = new MirrorLauncherFrame(messages, localeManager);

            // 5. 核心控制器组装 (Dependency Injection)
            // 将 UI、底层服务、请求工厂、线程池等注入到控制器中
            MirrorController controller = new MirrorController(
                    frame,                                  // UI 视图
                    new AndroidMirrorService(),             // 安卓镜像服务底层实现
                    new MirrorRequestFactory(messages),     // 请求参数构造工厂
                    messages,                               // 国际化文本
                    localeManager,                          // 语言管理器
                    backgroundExecutor,                     // 异步任务执行器
                    new DefaultExecutableFinder());         // 可执行文件（如 adb/scrcpy）查找器

            // 6. 绑定控制器到界面，并注册退出钩子
            frame.bindController(controller);

            // 注册程序关闭时的钩子，确保程序退出时能清理进程（如关闭正在运行的 scrcpy）
            Runtime.getRuntime().addShutdownHook(new Thread(controller::onExit, "connect-exit-cleanup"));

            // 显示界面
            frame.setVisible(true);

            // 界面显示后，再次通过 Swing 线程执行初始化逻辑（如检测环境等）
            SwingUtilities.invokeLater(controller::initialize);
        });
    }

    /**
     * 自定义线程工厂，为后台设备操作和进程任务创建带名称的守护线程。
     * 作用：方便在调试或查看堆栈轨迹时识别是哪个模块创建的线程。
     */
    private static final class MirrorThreadFactory implements ThreadFactory {
        private final AtomicInteger threadId = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            // 设置线程名称，例如：connect-worker-1
            Thread thread = new Thread(runnable, "connect-worker-" + threadId.incrementAndGet());
            // 设置为守护线程，这样当主程序关闭时，这些线程会自动停止
            thread.setDaemon(true);
            return thread;
        }
    }
}