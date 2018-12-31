package me.corny.spm;

import javafx.embed.swing.JFXPanel;

import java.util.Timer;

public class Main {

    public static void main(String[] args) {
        JFXPanel fxPanel = new JFXPanel();
        CheckerTask checkerTask = new CheckerTask();
        boolean ready = false;
        while (!ready) {
            ready = checkerTask.setup();
        }
        Timer timer = new Timer();
        timer.schedule(checkerTask, 0, 60000);
    }

}
