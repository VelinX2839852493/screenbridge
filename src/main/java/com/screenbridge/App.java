package com.screenbridge;

import com.screenbridge.mirror.application.MirrorController;
import com.screenbridge.mirror.application.MirrorRequestFactory;
import com.screenbridge.mirror.i18n.LocaleManager;
import com.screenbridge.mirror.i18n.Messages;
import com.screenbridge.mirror.infrastructure.AndroidMirrorService;
import com.screenbridge.mirror.infrastructure.DefaultExecutableFinder;
import com.screenbridge.mirror.settings.PreferencesSettingsStore;
import com.screenbridge.mirror.ui.MirrorLauncherFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        LocaleManager localeManager = new LocaleManager(new PreferencesSettingsStore(App.class), Locale.getDefault());
        Messages messages = new Messages(localeManager);
        ExecutorService backgroundExecutor = Executors.newCachedThreadPool(new MirrorThreadFactory());

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            MirrorLauncherFrame frame = new MirrorLauncherFrame(messages, localeManager);
            MirrorController controller = new MirrorController(
                    frame,
                    new AndroidMirrorService(),
                    new MirrorRequestFactory(messages),
                    messages,
                    localeManager,
                    backgroundExecutor,
                    new DefaultExecutableFinder());
            frame.bindController(controller);
            Runtime.getRuntime().addShutdownHook(new Thread(controller::onExit, "connect-exit-cleanup"));
            frame.setVisible(true);
            SwingUtilities.invokeLater(controller::initialize);
        });
    }

    private static final class MirrorThreadFactory implements ThreadFactory {
        private final AtomicInteger threadId = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "connect-worker-" + threadId.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
