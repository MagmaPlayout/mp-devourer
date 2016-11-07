/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ragnarok
 */
public class IngestServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Config config = null;
            try {
                config = new Config();
            } catch (IOException ex) {
                Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Exec gui
            LanternaTerminalGUI tgui = new LanternaTerminalGUI();
            tgui.executeGUI();

        } catch (IOException ex) {
            Logger.getLogger(IngestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
