/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ragnarok
 */
public class LanternaTerminalGUIPoller {

    LanternaTerminalGUIPoller(Terminal terminal, WindowBasedTextGUI gui, String mediaDirectory) throws IOException {

        gui.getWindows().forEach(window -> window.close());

        // Polling window GUI
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.NO_DECORATIONS, Window.Hint.CENTERED));
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        Panel titlePanel = new Panel();
        Label title = new Label(""
                + "██████╗ ███████╗██╗   ██╗ ██████╗ ██╗   ██╗██████╗ ███████╗██████╗ \n"
                + "██╔══██╗██╔════╝██║   ██║██╔═══██╗██║   ██║██╔══██╗██╔════╝██╔══██╗\n"
                + "██║  ██║█████╗  ██║   ██║██║   ██║██║   ██║██████╔╝█████╗  ██████╔╝\n"
                + "██║  ██║██╔══╝  ╚██╗ ██╔╝██║   ██║██║   ██║██╔══██╗██╔══╝  ██╔══██╗\n"
                + "██████╔╝███████╗ ╚████╔╝ ╚██████╔╝╚██████╔╝██║  ██║███████╗██║  ██║\n"
                + "╚═════╝ ╚══════╝  ╚═══╝   ╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝");
        title.setForegroundColor(TextColor.ANSI.WHITE);
        title.setBackgroundColor(TextColor.ANSI.BLUE);
        Label subtitle = new Label("Polling directory at: \n" + mediaDirectory + "\n"
                + "");
        titlePanel.addComponent(title);
        titlePanel.addComponent(subtitle);
        mainPanel.addComponent(titlePanel);

        mainPanel.addComponent(new Button("Start", new Runnable() {
            @Override
            public void run() {
                DirectoryPoller dp;
                dp = new DirectoryPoller(mediaDirectory);
                dp.start();

                new MessageDialogBuilder()
                        .setTitle("Polling...")
                        .setText("OK to Stop")
                        .build()
                        .showDialog(gui);
                dp.stopPolling();
            }
        }));

        // exit button panel
        mainPanel.addComponent(new Button("Exit", new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }));

        window.setComponent(mainPanel);

        gui.addWindowAndWait(window);
        // END GUI

    }

}
