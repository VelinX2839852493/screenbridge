package com.screenbridge.mirror.ui;

import com.screenbridge.mirror.application.MirrorController;
import com.screenbridge.mirror.application.MirrorView;
import com.screenbridge.mirror.domain.DeviceInfo;
import com.screenbridge.mirror.domain.MirrorFormState;
import com.screenbridge.mirror.i18n.Language;
import com.screenbridge.mirror.i18n.LocaleManager;
import com.screenbridge.mirror.i18n.Messages;
import com.screenbridge.mirror.infrastructure.ExecutableLocator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MirrorLauncherFrame extends JFrame implements MirrorView {
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
    private final JTextField scrcpyPathField = new JTextField();
    private final JTextField adbPathField = new JTextField();
    private final JComboBox<DeviceInfo> deviceComboBox = new JComboBox<>();
    private final JTextField wifiAddressField = new JTextField("192.168.1.10:5555");
    private final JTextField maxSizeField = new JTextField("3000");
    private final JTextField maxFpsField = new JTextField("90");
    private final JTextField videoBitRateField = new JTextField("8M");
    private final JCheckBox noAudioCheckBox = new JCheckBox();
    private final JCheckBox turnScreenOffCheckBox = new JCheckBox();
    private final JCheckBox stayAwakeCheckBox = new JCheckBox();
    private final JButton refreshDevicesButton = new JButton();
    private final JButton connectWifiButton = new JButton();
    private final JButton startMirrorButton = new JButton();
    private final JButton stopMirrorButton = new JButton();
    private final JButton scrcpyBrowseButton = new JButton();
    private final JButton adbBrowseButton = new JButton();
    private final JTextArea logArea = new JTextArea();
    private final JScrollPane logScrollPane = new JScrollPane(logArea);
    private final TitledBorder executableBorder = BorderFactory.createTitledBorder("");
    private final TitledBorder deviceBorder = BorderFactory.createTitledBorder("");
    private final TitledBorder optionsBorder = BorderFactory.createTitledBorder("");
    private final TitledBorder logBorder = BorderFactory.createTitledBorder("");

    private MirrorController controller;
    private boolean updatingLanguageSelection;

    public MirrorLauncherFrame(Messages messages, LocaleManager localeManager) {
        super(messages.get("app.title"));
        this.messages = messages;
        this.localeManager = localeManager;

        stayAwakeCheckBox.setSelected(true);
        deviceComboBox.setRenderer(new DeviceCellRenderer());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 700));
        setSize(980, 700);
        setLocationRelativeTo(null);

        buildUi();
        applyTexts(localeManager.currentLanguage());
        localeManager.addListener(this::applyTexts);
    }

    public void bindController(MirrorController controller) {
        this.controller = controller;

        refreshDevicesButton.addActionListener(event -> controller.onRefreshDevices());
        connectWifiButton.addActionListener(event -> controller.onConnectWifi());
        startMirrorButton.addActionListener(event -> controller.onStartMirror());
        stopMirrorButton.addActionListener(event -> controller.onStopMirror());
        scrcpyBrowseButton.addActionListener(event -> chooseExecutable(scrcpyPathField, messages.get("dialog.choose.scrcpy")));
        adbBrowseButton.addActionListener(event -> chooseExecutable(adbPathField, messages.get("dialog.choose.adb")));
        languageComboBox.addActionListener(event -> onLanguageSelected());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                controller.onExit();
            }
        });
    }

    @Override
    public MirrorFormState readFormState() {
        return new MirrorFormState(
                scrcpyPathField.getText(),
                adbPathField.getText(),
                (DeviceInfo) deviceComboBox.getSelectedItem(),
                wifiAddressField.getText(),
                maxSizeField.getText(),
                maxFpsField.getText(),
                videoBitRateField.getText(),
                noAudioCheckBox.isSelected(),
                turnScreenOffCheckBox.isSelected(),
                stayAwakeCheckBox.isSelected());
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
        JPanel wrapper = new JPanel(new BorderLayout(12, 12));
        wrapper.add(buildExecutablePanel(), BorderLayout.NORTH);
        wrapper.add(buildDevicePanel(), BorderLayout.CENTER);
        wrapper.add(buildOptionsPanel(), BorderLayout.SOUTH);
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
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(noAudioCheckBox, gbc);
        gbc.gridx = 2;
        panel.add(turnScreenOffCheckBox, gbc);
        gbc.gridx = 4;
        gbc.gridwidth = 2;
        panel.add(stayAwakeCheckBox, gbc);

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
        panel.add(label, gbc);
    }

    private void addField(JPanel panel, JTextField field, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
    }

    private void addButton(JPanel panel, JButton button, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
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
            logBorder.setTitle(messages.get("section.log"));
            scrcpyPathLabel.setText(messages.get("label.scrcpy"));
            adbPathLabel.setText(messages.get("label.adb"));
            deviceLabel.setText(messages.get("label.discoveredDevices"));
            wifiAddressLabel.setText(messages.get("label.wifiAddress"));
            maxSizeLabel.setText(messages.get("label.maxSize"));
            maxFpsLabel.setText(messages.get("label.maxFps"));
            videoBitRateLabel.setText(messages.get("label.videoBitRate"));
            scrcpyBrowseButton.setText(messages.get("button.browse"));
            adbBrowseButton.setText(messages.get("button.browse"));
            refreshDevicesButton.setText(messages.get("button.refreshDevices"));
            connectWifiButton.setText(messages.get("button.connectWifi"));
            startMirrorButton.setText(messages.get("button.startMirror"));
            stopMirrorButton.setText(messages.get("button.stopMirror"));
            noAudioCheckBox.setText(messages.get("checkbox.noAudio"));
            turnScreenOffCheckBox.setText(messages.get("checkbox.turnScreenOff"));
            stayAwakeCheckBox.setText(messages.get("checkbox.stayAwake"));
            deviceComboBox.repaint();
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
}
