package me.corny.spm;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static javax.swing.JOptionPane.*;

public class CheckerTask extends TimerTask {

    private static final String appTitle = "SACNR player monitor";
    private static final String appTitleSetup = "SACNR player monitor setup";
    private JFrame frame = new JFrame("SACNR player monitor");
    private JPanel panel;
    private JTextPane statusPane;
    private JTextPane targetsPane;
    private JTextPane onlineTargetsPane;
    private JTextPane onlinePlayersPane;

    // Sound from http://freesound.org, Name: buttonchime02up.wav, Author: JustinBW
    private MediaPlayer alert = new MediaPlayer(new Media(getClass().getClassLoader().getResource("alert.mp3").toExternalForm()));
    private static String serverIp = "server.sacnr.com";
    private static int serverPort = 7777;
    private static SampQuery sampQuery;
    private static List<String> targets = new ArrayList<>();
    private static List<String> onlineTargets = new ArrayList<>();

    boolean setup() {
        if (dialog(showConfirmDialog(null, "Monitor different server?", appTitleSetup, YES_NO_OPTION)) == 1) {
            if (dialog(showConfirmDialog(null, "Check for SACNR admins?", appTitleSetup, YES_NO_OPTION)) == 0) {
                getSacnrAdmins();
            }
        } else {
            String input = showInputDialog(null, "Enter server address:", appTitleSetup, INFORMATION_MESSAGE);
            if (input != null && !input.isEmpty()) {
                String[] address = input.split(":");
                serverIp = address[0];
                try {
                    if (address.length > 1) {
                        serverPort = Integer.valueOf(address[1]);
                    }
                } catch (NumberFormatException ignored) {}
            } else {
                return false;
            }
        }
        JDialog dialog = customDialog("Checking server address, please wait...");
        try {
            sampQuery = new SampQuery(serverIp, serverPort);
        } catch (Exception e) {
            dialog.dispose();
            showMessageDialog(null, "Invalid server address!", appTitleSetup, ERROR_MESSAGE);
            serverIp = "server.sacnr.com";
            serverPort = 7777;
            return false;
        }
        dialog.dispose();
        Optional.ofNullable(showInputDialog(null, "Add targets (separated by comma)", appTitleSetup, INFORMATION_MESSAGE)).
                ifPresent(input -> targets.addAll(Arrays.asList(input.split("\\s*,\\s*"))));
        targets.removeAll(Arrays.asList("", " "));
        if (targets.isEmpty()) {
            showMessageDialog(null, "No targets, exiting...", appTitleSetup, INFORMATION_MESSAGE);
            System.exit(0);
        }
        if (dialog(showConfirmDialog(null, "Targets: " + targets, appTitleSetup, OK_CANCEL_OPTION)) == 2) {
            return false;
        }
        targetsPane.setText(targets.toString());
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return true;
    }

    public void run() {
        statusPane.setText("Checking online players...");
        List<String> onlinePlayers = new ArrayList<>();
        if (sampQuery.connect()) {
            String[][] result = sampQuery.getBasicPlayers();
            for (String[] player : result) {
                onlinePlayers.add(player[0]);
            }
            Collections.sort(onlinePlayers);
            onlineTargets.retainAll(onlinePlayers);
            ArrayList<String> newOnlineTargets = new ArrayList<>(onlinePlayers);
            newOnlineTargets.retainAll(targets);
            newOnlineTargets.removeAll(onlineTargets);
            if (!newOnlineTargets.isEmpty()) {
                onlineTargets.addAll(newOnlineTargets);
                statusPane.setText("New online targets: " + newOnlineTargets + " Online since: " + getTimestamp());
                alert.seek(Duration.seconds(0));
                alert.play();
            } else {
                statusPane.setText("No new online targets. Last check: " + getTimestamp());
            }
            onlineTargetsPane.setText(onlineTargets.toString());
            onlinePlayersPane.setText(onlinePlayers.toString());
        } else {
            showMessageDialog(frame, "Server did not respond.", appTitle, INFORMATION_MESSAGE);
            statusPane.setText("Last check at " + getTimestamp() + " failed");
        }
    }

    private int dialog(int value) {
        if (value == -1) {
            System.exit(0);
        }
        return value;
    }

    private JDialog customDialog(String message) {
        JDialog dialog = new JOptionPane(message, INFORMATION_MESSAGE).createDialog(appTitleSetup);
        dialog.setModal(false);
        dialog.setVisible(true);
        return dialog;
    }

    private void getSacnrAdmins() {
        JDialog dialog = customDialog("Fetching admin list, please wait...");
        try {
            Document document = Jsoup.connect("https://sacnr.com/staff").followRedirects(false).timeout(30000).get();
            targets = document.body().select(".gendata>tbody>tr>td>p>span>a").eachText();
        } catch (IOException e) {
            showMessageDialog(null, "Couldn't retrieve admin list: " + e, appTitleSetup, INFORMATION_MESSAGE);
        }
        dialog.dispose();
    }

    private String getTimestamp() {
        return String.format("[%s]", new Timestamp(new Date().getTime()));
    }

}
