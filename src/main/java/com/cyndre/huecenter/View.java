package com.cyndre.huecenter;

import com.cyndre.huecenter.hue.Error;
import com.cyndre.huecenter.hue.LightGroup;
import com.cyndre.huecenter.hue.LightState;
import com.cyndre.huecenter.program.Program;
import com.cyndre.huecenter.view.LightView;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class View
implements
        ActionListener
{
    private static final String newline = "\n";

    private JFrame rootFrame;
    private JTextArea logText;
    private RSyntaxTextArea inputField;
    private JComboBox<LightGroup> lightGroupComboBox;
    private JPanel lightsContainer;

    private final Map<JMenuItem, Runnable> menuActionMap = new HashMap<>();
    private final CommandInterface commandInterface;

    private Program.Context runningProgram = null;

    public View(final CommandInterface commandInterface) {
        this.commandInterface = commandInterface;

        this.rootFrame = new JFrame();
        this.rootFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.rootFrame.setSize(400,500);

        this.rootFrame.setJMenuBar(createMenu());

        final JPanel panel = (JPanel) this.rootFrame.getContentPane();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        this.buildBaseInterface(panel, c);
    }

    public void run() {
        this.rootFrame.setVisible(true);
    }

    private JMenuItem createMenuItem(final String name, final int mnemonic, final Runnable action) {
        JMenuItem menuItem = new JMenuItem(name, mnemonic);

        this.menuActionMap.put(menuItem, action);

        menuItem.addActionListener(this);

        return menuItem;
    }

    private JMenuBar createMenu() {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu menu = new JMenu("File");

        menuBar.add(menu);

        final JMenuItem refreshMenuItem = createMenuItem("Refresh (Requires tap)", KeyEvent.VK_F5, this::onRefresh);
        final KeyStroke f5KeyStroke = KeyStroke.getKeyStroke("F5");
        refreshMenuItem.setAccelerator(f5KeyStroke);

        final JMenuItem executeMenuItem = createMenuItem("Execute", KeyEvent.VK_F6, this::onExecute);
        final KeyStroke f6KeyStroke = KeyStroke.getKeyStroke("F6");
        executeMenuItem.setAccelerator(f6KeyStroke);

        final JMenuItem stopExecuteMenuItem = createMenuItem("Stop", KeyEvent.VK_F7, this::onStopExecute);
        final KeyStroke f7KeyStroke = KeyStroke.getKeyStroke("F7");
        stopExecuteMenuItem.setAccelerator(f7KeyStroke);

        final JMenuItem clearLogMenuItem = createMenuItem("Clear Log", KeyEvent.VK_F8, this::onClearLog);
        final KeyStroke f8KeyStroke = KeyStroke.getKeyStroke("F8");
        clearLogMenuItem.setAccelerator(f8KeyStroke);

        menu.add(refreshMenuItem);
        menu.add(executeMenuItem);
        menu.add(stopExecuteMenuItem);
        menu.add(clearLogMenuItem);

        menu.add(createMenuItem("Quit", KeyEvent.VK_F5, this::onQuit));

        return menuBar;
    }

    private void buildBaseInterface(final Container container, GridBagConstraints c) {
        this.lightsContainer = new JPanel();
        this.logText = new JTextArea();
        this.inputField = new RSyntaxTextArea();
        this.lightGroupComboBox = new JComboBox();
        this.lightGroupComboBox.addActionListener((e) -> this.onLightGroupListChanged());

        this.inputField.setText("output.putAll(input)\n" +
                "output.each { key, light ->\n" +
                "  light.on = !light.on\n" +
                "}\n");

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;

        final int actionsCount = c.gridx;// + 1;


        c.insets=new Insets(10,10,10,10);

        container.add(this.lightsContainer, c);

        c.gridy = ++c.gridy;
        container.add(this.lightGroupComboBox, c);


        c.gridwidth = actionsCount;
        c.gridy = ++c.gridy;
        c.gridx = 0;

        c.fill = GridBagConstraints.BOTH;
        c.gridheight=1;
        c.weightx=0;
        c.weighty=200;

        this.inputField.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        this.inputField.setCodeFoldingEnabled(true);
        RTextScrollPane scrollInput = new RTextScrollPane(this.inputField);
        container.add(scrollInput, c);

        c.weightx = 0.0;
        c.gridy = ++c.gridy;

        JScrollPane scrollLogText = new JScrollPane (
                this.logText,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        container.add(scrollLogText, c);
    }

    public void log(final String log, Object... args) {
        final String line = String.format(log, args) + "\n";

        this.logText.append(line);
    }

    private void logErrors(final String context, Collection<Error> orErrors) {
        orErrors.forEach((e) -> {
            log("[%1$s] Error %2$d %3$s %4$s", context, e.getType(), e.getAddress(), e.getDescription());
        });

    }

    private void log(final String context, Optional<?> result, Collection<Error> orErrors) {
        if (result.isPresent()) {
            log("[%s] Success", context);
        } else {
            logErrors(context, orErrors);
        }
    }

    private void onQuit() {
        this.commandInterface.Quit();
    }

    private void onRefresh() {
        this.log("onRefresh");
        this.commandInterface.RegisterUsername("joe", (result, errors) -> {
            this.log("onRefresh", result, errors);

            if (result.isPresent()) {
                this.commandInterface.ListGroups(this::onListGroups);
            }
        });
    }

    private void onLightGroupListChanged() {
        final LightGroup lightGroup = (LightGroup)this.lightGroupComboBox.getSelectedItem();

        if (lightGroup == null) {
            return;
        }

        this.commandInterface.GetLights(lightGroup.getLights(), this::onLights);
    }

    public void onLights(final Optional<Map<String, LightState>> results, Collection<Error> onErrors) {
        this.lightsContainer.removeAll();

        if (!results.isEmpty()) {
            results.get().entrySet().stream().forEach((kv) -> {
                Color color = LightState.toColor(kv.getValue());
                this.lightsContainer.add(new LightView(color, kv.getKey()));
            });
        }

        this.lightsContainer.revalidate();
        this.lightsContainer.repaint();
    }

    private void onListGroups(Optional<Map<String,LightGroup>> result, Collection<Error> orErrors) {
        this.log("Error onListGroups", result, orErrors);

        if (result.isEmpty()) {
            return;
        }

        this.lightGroupComboBox.removeAllItems();

        result.get()
            .entrySet()
            .stream()
            .forEach((kv) -> {
                this.lightGroupComboBox.addItem(
                        kv.getValue()
                );
            });
    }

    private void onExecute() {
        onStopExecute();

        this.log("Execute");
        final CommandInterface.ExecutionContext executionContext = new CommandInterface.ExecutionContext();

        executionContext.scriptText = this.inputField.getText();

        this.runningProgram = this.commandInterface.Execute(executionContext);
    }

    private void onStopExecute() {
        if (this.runningProgram == null) {
            return;
        }

        this.runningProgram.setRunning(false);

        this.runningProgram = null;
    }

    private void onClearLog() {
        this.logText.setText("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());

        final Runnable action = this.menuActionMap.get(source);

        if (action != null) {
            action.run();
        }
    }
}
