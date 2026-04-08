package com.screenbridge.mirror.ui;

import com.screenbridge.mirror.application.MirrorController;
import com.screenbridge.mirror.application.MirrorRequestFactory;
import com.screenbridge.mirror.application.MirrorView;
import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.InputMode;
import com.screenbridge.mirror.domain.MirrorFormState;
import com.screenbridge.mirror.i18n.Language;
import com.screenbridge.mirror.i18n.LocaleManager;
import com.screenbridge.mirror.i18n.Messages;
import com.screenbridge.mirror.infrastructure.ExecutableLocator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Swing launcher window for configuring and controlling scrcpy sessions.
 */
public final class MirrorLauncherFrame extends JFrame implements MirrorView {
    private static final String DEFAULT_FIT_ASPECT_RATIO = MirrorRequestFactory.DEFAULT_FIT_ASPECT_RATIO;
    private static final String DEFAULT_PUSH_TARGET = MirrorRequestFactory.DEFAULT_PUSH_TARGET;

    private final Messages messages;
    private final LocaleManager localeManager;

    private final JLabel languageLabel = new JLabel();
    private final JComboBox<Language> languageComboBox = new JComboBox<>(Language.values());
    private final JLabel scrcpyPathLabel = new JLabel();
    private final JLabel adbPathLabel = new JLabel();
    private final JLabel deviceLabel = new JLabel();
    private final JLabel wifiAddressLabel = new JLabel();
    private final JLabel maxSizeLabel = new JLabel();
    private final JLabel maxFpsLabel = new JLabel();
    private final JLabel videoBitRateLabel = new JLabel();
    private final JLabel windowWidthLabel = new JLabel();
    private final JLabel windowHeightLabel = new JLabel();
    private final JLabel fitAspectRatioLabel = new JLabel();
    private final JLabel keyboardModeLabel = new JLabel();
    private final JLabel mouseModeLabel = new JLabel();
    private final JLabel pushTargetLabel = new JLabel();
    private final JTextField scrcpyPathField = new JTextField();
    private final JTextField adbPathField = new JTextField();
    private final JComboBox<DeviceInfo> deviceComboBox = new JComboBox<>();
    private final JTextField wifiAddressField = new JTextField("192.168.1.10:5555");
    private final JTextField maxSizeField = new JTextField("3000");
    private final JTextField maxFpsField = new JTextField("60");
    private final JTextField videoBitRateField = new JTextField("8M");
    private final JTextField windowWidthField = new JTextField();
    private final JTextField windowHeightField = new JTextField();
    private final JTextField fitAspectRatioField = new JTextField(DEFAULT_FIT_ASPECT_RATIO);
    private final JComboBox<InputMode> keyboardModeComboBox = new JComboBox<>(InputMode.values());
    private final JComboBox<InputMode> mouseModeComboBox = new JComboBox<>(InputMode.values());
    private final JTextField pushTargetField = new JTextField(DEFAULT_PUSH_TARGET);
    private final JCheckBox fitWindowToScreenCheckBox = new JCheckBox();
    private final JCheckBox fullscreenCheckBox = new JCheckBox();
    private final JCheckBox alwaysOnTopCheckBox = new JCheckBox();
    private final JCheckBox noAudioCheckBox = new JCheckBox();
    private final JCheckBox turnScreenOffCheckBox = new JCheckBox();
    private final JCheckBox stayAwakeCheckBox = new JCheckBox();
    private final JButton refreshDevicesButton = new JButton();
    private final JButton connectWifiButton = new JButton();
    private final JButton startMirrorButton = new JButton();
    private final JButton stopMirrorButton = new JButton();
    private final JButton scrcpyBrowseButton = new JButton();
    private final JButton adbBrowseButton = new JButton();
    private final JButton sendFilesButton = new JButton();
    private final JTextArea logArea = new JTextArea();
    private final JScrollPane logScrollPane = new JScrollPane(logArea);
    private final JPanel dropPanel = new JPanel(new BorderLayout());
    private final JLabel dropHintLabel = new JLabel("", SwingConstants.CENTER);
    private final TitledBorder executableBorder = BorderFactory.createTitledBorder("");
    private final TitledBorder deviceBorder = BorderFactory.createTitledBorder("");
    private final TitledBorder optionsBorder = BorderFactory.createTitledBorder("");
    private final TitledBorder transferBorder = BorderFactory.createTitledBorder("");
    private final TitledBorder logBorder = BorderFactory.createTitledBorder("");

    private MirrorController controller;
    private boolean updatingLanguageSelection;

    public MirrorLauncherFrame(Messages messages, LocaleManager localeManager) {
        super(messages.get("app.title"));
        this.messages = messages;
        this.localeManager = localeManager;

        stayAwakeCheckBox.setSelected(true);
        keyboardModeComboBox.setSelectedItem(InputMode.DEFAULT);
        mouseModeComboBox.setSelectedItem(InputMode.DEFAULT);
        deviceComboBox.setRenderer(new DeviceCellRenderer());
        keyboardModeComboBox.setRenderer(new InputModeCellRenderer());
        mouseModeComboBox.setRenderer(new InputModeCellRenderer());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 820));
        setSize(1120, 820);
        setLocationRelativeTo(null);

        buildUi();
        updateWindowSizingState();
        applyTexts(localeManager.currentLanguage());
        localeManager.addListener(this::applyTexts);
    }

    public void bindController(MirrorController controller) {
        this.controller = controller;

        refreshDevicesButton.addActionListener(event -> controller.onRefreshDevices());
        connectWifiButton.addActionListener(event -> controller.onConnectWifi());
        startMirrorButton.addActionListener(event -> controller.onStartMirror());
        stopMirrorButton.addActionListener(event -> controller.onStopMirror());
        sendFilesButton.addActionListener(event -> chooseFilesToSend());
        scrcpyBrowseButton.addActionListener(event -> chooseExecutable(scrcpyPathField, messages.get("dialog.choose.scrcpy")));
        adbBrowseButton.addActionListener(event -> chooseExecutable(adbPathField, messages.get("dialog.choose.adb")));
        languageComboBox.addActionListener(event -> onLanguageSelected());
        fitWindowToScreenCheckBox.addActionListener(event -> updateWindowSizingState());
        installFileTransferSupport();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                controller.onExit();
            }
        });
    }

    @Override
    public MirrorFormState readFormState() {
        Rectangle availableBounds = currentAvailableScreenBounds();
        return new MirrorFormState(
                scrcpyPathField.getText(),
                adbPathField.getText(),
                (DeviceInfo) deviceComboBox.getSelectedItem(),
                wifiAddressField.getText(),
                maxSizeField.getText(),
                maxFpsField.getText(),
                videoBitRateField.getText(),
                windowWidthField.getText(),
                windowHeightField.getText(),
                fitWindowToScreenCheckBox.isSelected(),
                fitAspectRatioField.getText(),
                fullscreenCheckBox.isSelected(),
                alwaysOnTopCheckBox.isSelected(),
                (InputMode) keyboardModeComboBox.getSelectedItem(),
                (InputMode) mouseModeComboBox.getSelectedItem(),
                pushTargetField.getText(),
                noAudioCheckBox.isSelected(),
                turnScreenOffCheckBox.isSelected(),
                stayAwakeCheckBox.isSelected(),
                availableBounds.width,
                availableBounds.height);
    }

    @Override
    public void setScrcpyPath(String path) {
        runOnUiThread(() -> scrcpyPathField.setText(path == null ? "" : path));
    }

    @Override
    public void setAdbPath(String path) {
        runOnUiThread(() -> adbPathField.setText(path == null ? "" : path));
    }

    @Override
    public void setDevices(List<DeviceInfo> devices) {
        runOnUiThread(() -> {
            deviceComboBox.removeAllItems();
            for (DeviceInfo device : devices) {
                deviceComboBox.addItem(device);
            }
            deviceComboBox.repaint();
        });
    }

    @Override
    public void setRefreshDevicesEnabled(boolean enabled) {
        runOnUiThread(() -> refreshDevicesButton.setEnabled(enabled));
    }

    @Override
    public void setConnectWifiEnabled(boolean enabled) {
        runOnUiThread(() -> connectWifiButton.setEnabled(enabled));
    }

    @Override
    public void setMirrorButtons(boolean startEnabled, boolean stopEnabled) {
        runOnUiThread(() -> {
            startMirrorButton.setEnabled(startEnabled);
            stopMirrorButton.setEnabled(stopEnabled);
        });
    }

    @Override
    public void setFileTransferEnabled(boolean enabled) {
        runOnUiThread(() -> {
            sendFilesButton.setEnabled(enabled);
            dropHintLabel.setEnabled(enabled);
            pushTargetField.setEnabled(enabled);
        });
    }

    @Override
    public void appendLog(String message) {
        runOnUiThread(() -> {
            logArea.append(message + System.lineSeparator());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void showError(String title, String message) {
        runOnUiThread(() -> JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE));
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(buildHeaderPanel(), BorderLayout.NORTH);
        content.add(buildLogPanel(), BorderLayout.CENTER);
        content.add(buildControlPanel(), BorderLayout.SOUTH);
        setContentPane(content);
    }

    private JPanel buildHeaderPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(12, 12));
        wrapper.add(buildLanguagePanel(), BorderLayout.NORTH);
        wrapper.add(buildTopPanel(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildLanguagePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.add(languageLabel);
        panel.add(languageComboBox);
        return panel;
    }

    private JPanel buildTopPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(buildExecutablePanel());
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(buildDevicePanel());
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(buildOptionsPanel());
        wrapper.add(Box.createVerticalStrut(12));
        wrapper.add(buildTransferPanel());
        return wrapper;
    }

    private JPanel buildExecutablePanel() {
        JPanel panel = createSectionPanel(executableBorder);
        GridBagConstraints gbc = baseConstraints();

        addLabel(panel, scrcpyPathLabel, gbc, 0);
        addField(panel, scrcpyPathField, gbc, 1);
        addButton(panel, scrcpyBrowseButton, gbc, 2);

        addLabel(panel, adbPathLabel, gbc, 3);
        addField(panel, adbPathField, gbc, 4);
        addButton(panel, adbBrowseButton, gbc, 5);

        return panel;
    }

    private JPanel buildDevicePanel() {
        JPanel panel = createSectionPanel(deviceBorder);
        GridBagConstraints gbc = baseConstraints();

        addLabel(panel, deviceLabel, gbc, 0);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        deviceComboBox.setPrototypeDisplayValue(new DeviceInfo("R52W123ABC", "unauthorized", "Galaxy Tab S8 Ultra"));
        panel.add(deviceComboBox, gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        addButton(panel, refreshDevicesButton, gbc, 2);

        addLabel(panel, wifiAddressLabel, gbc, 3);
        addField(panel, wifiAddressField, gbc, 4);
        addButton(panel, connectWifiButton, gbc, 5);

        return panel;
    }

    private JPanel buildOptionsPanel() {
        JPanel panel = createSectionPanel(optionsBorder);
        GridBagConstraints gbc = baseConstraints();

        addLabel(panel, maxSizeLabel, gbc, 0);
        addField(panel, maxSizeField, gbc, 1);
        addLabel(panel, maxFpsLabel, gbc, 2);
        addField(panel, maxFpsField, gbc, 3);
        addLabel(panel, videoBitRateLabel, gbc, 4);
        addField(panel, videoBitRateField, gbc, 5);

        gbc.gridy = 1;
        addLabel(panel, windowWidthLabel, gbc, 0);
        addField(panel, windowWidthField, gbc, 1);
        addLabel(panel, windowHeightLabel, gbc, 2);
        addField(panel, windowHeightField, gbc, 3);
        addLabel(panel, fitAspectRatioLabel, gbc, 4);
        addField(panel, fitAspectRatioField, gbc, 5);

        gbc.gridy = 2;
        addLabel(panel, keyboardModeLabel, gbc, 0);
        addComboBox(panel, keyboardModeComboBox, gbc, 1);
        addLabel(panel, mouseModeLabel, gbc, 2);
        addComboBox(panel, mouseModeComboBox, gbc, 3);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(fitWindowToScreenCheckBox, gbc);
        gbc.gridx = 2;
        panel.add(fullscreenCheckBox, gbc);
        gbc.gridx = 4;
        panel.add(alwaysOnTopCheckBox, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(noAudioCheckBox, gbc);
        gbc.gridx = 2;
        panel.add(turnScreenOffCheckBox, gbc);
        gbc.gridx = 4;
        panel.add(stayAwakeCheckBox, gbc);
        gbc.gridwidth = 1;

        return panel;
    }

    private JPanel buildTransferPanel() {
        JPanel panel = createSectionPanel(transferBorder);
        GridBagConstraints gbc = baseConstraints();

        addLabel(panel, pushTargetLabel, gbc, 0);
        gbc.gridx = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pushTargetField, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        addButton(panel, sendFilesButton, gbc, 5);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 6;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dropPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(getForeground()),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        dropPanel.add(dropHintLabel, BorderLayout.CENTER);
        dropPanel.setPreferredSize(new Dimension(0, 72));
        panel.add(dropPanel, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        return panel;
    }

    private JScrollPane buildLogPanel() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logScrollPane.setBorder(logBorder);
        return logScrollPane;
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(startMirrorButton);
        panel.add(stopMirrorButton);
        stopMirrorButton.setEnabled(false);
        return panel;
    }

    private JPanel createSectionPanel(TitledBorder border) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(border);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addLabel(JPanel panel, JLabel label, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        panel.add(label, gbc);
    }

    private void addField(JPanel panel, JTextField field, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        panel.add(field, gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
    }

    private void addComboBox(JPanel panel, JComboBox<?> comboBox, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        panel.add(comboBox, gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
    }

    private void addButton(JPanel panel, JButton button, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.gridwidth = 1;
        panel.add(button, gbc);
    }

    private void chooseExecutable(JTextField targetField, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileNameExtensionFilter(
                messages.get("fileFilter.windowsExecutables"), "exe", "cmd", "bat"));
        if (!targetField.getText().isBlank()) {
            chooser.setSelectedFile(Path.of(targetField.getText()).toFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path selectedPath = chooser.getSelectedFile().toPath().toAbsolutePath();
            targetField.setText(selectedPath.toString());
            if (targetField == scrcpyPathField && adbPathField.getText().isBlank()) {
                ExecutableLocator.findAdb(selectedPath).ifPresent(path -> adbPathField.setText(path.toString()));
            }
        }
    }

    private void chooseFilesToSend() {
        if (controller == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(messages.get("dialog.choose.files"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            List<Path> selectedFiles = new ArrayList<>();
            for (File file : chooser.getSelectedFiles()) {
                selectedFiles.add(file.toPath().toAbsolutePath());
            }
            controller.onPushFiles(selectedFiles);
        }
    }

    private void onLanguageSelected() {
        if (controller == null || updatingLanguageSelection) {
            return;
        }

        Language selectedLanguage = (Language) languageComboBox.getSelectedItem();
        if (selectedLanguage != null) {
            controller.onLanguageSelected(selectedLanguage);
        }
    }

    private void applyTexts(Language language) {
        runOnUiThread(() -> {
            updatingLanguageSelection = true;
            try {
                languageComboBox.setSelectedItem(language);
            } finally {
                updatingLanguageSelection = false;
            }

            setTitle(messages.get("app.title"));
            languageLabel.setText(messages.get("label.language"));
            executableBorder.setTitle(messages.get("section.executables"));
            deviceBorder.setTitle(messages.get("section.deviceConnection"));
            optionsBorder.setTitle(messages.get("section.scrcpyOptions"));
            transferBorder.setTitle(messages.get("section.fileTransfer"));
            logBorder.setTitle(messages.get("section.log"));
            scrcpyPathLabel.setText(messages.get("label.scrcpy"));
            adbPathLabel.setText(messages.get("label.adb"));
            deviceLabel.setText(messages.get("label.discoveredDevices"));
            wifiAddressLabel.setText(messages.get("label.wifiAddress"));
            maxSizeLabel.setText(messages.get("label.maxSize"));
            maxFpsLabel.setText(messages.get("label.maxFps"));
            videoBitRateLabel.setText(messages.get("label.videoBitRate"));
            windowWidthLabel.setText(messages.get("label.windowWidth"));
            windowHeightLabel.setText(messages.get("label.windowHeight"));
            fitAspectRatioLabel.setText(messages.get("label.fitAspectRatio"));
            keyboardModeLabel.setText(messages.get("label.keyboardMode"));
            mouseModeLabel.setText(messages.get("label.mouseMode"));
            pushTargetLabel.setText(messages.get("label.pushTarget"));
            scrcpyBrowseButton.setText(messages.get("button.browse"));
            adbBrowseButton.setText(messages.get("button.browse"));
            refreshDevicesButton.setText(messages.get("button.refreshDevices"));
            connectWifiButton.setText(messages.get("button.connectWifi"));
            sendFilesButton.setText(messages.get("button.sendFiles"));
            startMirrorButton.setText(messages.get("button.startMirror"));
            stopMirrorButton.setText(messages.get("button.stopMirror"));
            fitWindowToScreenCheckBox.setText(messages.get("checkbox.fitWindowToScreen"));
            fullscreenCheckBox.setText(messages.get("checkbox.fullscreen"));
            alwaysOnTopCheckBox.setText(messages.get("checkbox.alwaysOnTop"));
            noAudioCheckBox.setText(messages.get("checkbox.noAudio"));
            turnScreenOffCheckBox.setText(messages.get("checkbox.turnScreenOff"));
            stayAwakeCheckBox.setText(messages.get("checkbox.stayAwake"));
            dropHintLabel.setText(messages.get("label.fileDropHint"));
            deviceComboBox.repaint();
            keyboardModeComboBox.repaint();
            mouseModeComboBox.repaint();
            revalidate();
            repaint();
        });
    }

    private String formatDevice(DeviceInfo device) {
        if (device == null) {
            return "";
        }
        if (device.isReady()) {
            return messages.get("device.format.ready", device.displayName(), device.serial());
        }
        return messages.get(
                "device.format.withState",
                device.displayName(),
                device.serial(),
                localizeDeviceState(device.state()));
    }

    private String localizeDeviceState(String rawState) {
        if (rawState == null || rawState.isBlank()) {
            return "";
        }

        return switch (rawState.toLowerCase(Locale.ROOT)) {
            case "device" -> messages.get("device.state.device");
            case "offline" -> messages.get("device.state.offline");
            case "unauthorized" -> messages.get("device.state.unauthorized");
            default -> rawState;
        };
    }

    private void updateWindowSizingState() {
        boolean fitToScreen = fitWindowToScreenCheckBox.isSelected();
        windowWidthField.setEnabled(!fitToScreen);
        windowHeightField.setEnabled(!fitToScreen);
        fitAspectRatioField.setEnabled(fitToScreen);
    }

    private Rectangle currentAvailableScreenBounds() {
        GraphicsConfiguration configuration = getGraphicsConfiguration();
        if (configuration == null) {
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        }

        Rectangle bounds = configuration.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        return new Rectangle(
                bounds.x + insets.left,
                bounds.y + insets.top,
                Math.max(1, bounds.width - insets.left - insets.right),
                Math.max(1, bounds.height - insets.top - insets.bottom));
    }

    private void installFileTransferSupport() {
        TransferHandler handler = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return controller != null
                        && sendFilesButton.isEnabled()
                        && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    Object transferData = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!(transferData instanceof List<?> rawFiles)) {
                        return false;
                    }

                    List<Path> paths = new ArrayList<>();
                    for (Object rawFile : rawFiles) {
                        if (rawFile instanceof File file) {
                            paths.add(file.toPath().toAbsolutePath());
                        }
                    }
                    if (paths.isEmpty()) {
                        return false;
                    }

                    controller.onPushFiles(paths);
                    return true;
                } catch (UnsupportedFlavorException | IOException exception) {
                    appendLog(messages.get("log.fileTransfer.importFailed", rootMessage(exception)));
                    return false;
                }
            }
        };

        dropPanel.setTransferHandler(handler);
        dropHintLabel.setTransferHandler(handler);
    }

    private String rootMessage(Exception exception) {
        return exception.getMessage() == null ? exception.toString() : exception.getMessage();
    }

    private void runOnUiThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
            return;
        }
        SwingUtilities.invokeLater(runnable);
    }

    private final class DeviceCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof DeviceInfo device) {
                setText(formatDevice(device));
            } else if (value == null) {
                setText("");
            }
            return this;
        }
    }

    private final class InputModeCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof InputMode inputMode) {
                setText(messages.get(inputMode.messageKey()));
            } else if (value == null) {
                setText("");
            }
            return this;
        }
    }
}
