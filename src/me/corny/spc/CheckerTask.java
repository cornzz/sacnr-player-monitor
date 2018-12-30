package me.corny.spc;

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
    // Sound from http://freesound.org, Name: buttonchime02up.wav, Author: JustinBW
    private MediaPlayer alert = new MediaPlayer(new Media(getClass().getClassLoader().getResource("alert.mp3").toExternalForm()));
    private static String baseIp = "server.sacnr.com";
    private static int basePort = 7777;
    private static SampQuery sampQuery;
    private static List<String> targets = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public CheckerTask() {
        print("Setting up SACNR player checker...");
    }

    boolean setup() {
        if (dialog(showConfirmDialog(null, "Monitor different server?", appTitleSetup, YES_NO_OPTION)) == 1) {
            if (dialog(showConfirmDialog(null, "Check for SACNR admins?", appTitleSetup, YES_NO_OPTION)) == 0) {
                try {
                    Document document = Jsoup.connect("https://sacnr.com/staff").followRedirects(false).timeout(30000).get();
                    targets = document.body().select(".gendata>tbody>tr>td>p>span>a").eachText();
                } catch (IOException e) {
                    showMessageDialog(null, "Couldn't retrieve admin list: " + e, appTitleSetup, INFORMATION_MESSAGE);
                }
            }
        } else {
            Optional.ofNullable(showInputDialog(null, "Enter server address:", appTitleSetup, INFORMATION_MESSAGE)).
                    ifPresent(input -> {
                        String[] address = input.split(":");
                        baseIp = address[0];
                        try {
                            if (address.length > 1) {
                                basePort = Integer.valueOf(address[1]);
                            }
                        } catch (NumberFormatException ignored) {}
                    });
        }

        try {
            sampQuery = new SampQuery(baseIp, basePort);
        } catch (Exception e) {
            showMessageDialog(null, "Invalid server address!", appTitleSetup, ERROR_MESSAGE);
            baseIp = "server.sacnr.com";
            basePort = 7777;
            return false;
        }

        Optional.ofNullable(showInputDialog(null, "Add targets (separated by comma)", appTitleSetup, INFORMATION_MESSAGE)).
                ifPresent(input -> {
                    List<String> t = new ArrayList<>(Arrays.asList(input.split("\\s*,\\s*")));
                    t.removeAll(Arrays.asList("", " "));
                    targets.addAll(t);
                });

        if (targets.isEmpty()) {
            showMessageDialog(null, "No targets, exiting...", appTitle, 0);
            System.exit(0);
        }

        return showConfirmDialog(null, "Targets: " + targets, appTitleSetup, OK_CANCEL_OPTION) == 0;
    }

    private int dialog(int value) {
        if (value == -1) {
            System.exit(0);
        }
        return value;
    }

    public void run() {
        print("Checking online players...");
        List<String> onlinePlayers = new ArrayList<>();
        if (sampQuery.connect()) {
            String[][] result = sampQuery.getBasicPlayers();
            for (String[] player : result) {
                onlinePlayers.add(player[0]);
            }
            Collections.sort(onlinePlayers);
            ArrayList<String> onlineTargets = new ArrayList<>(onlinePlayers);
            onlineTargets.retainAll(targets);
            if (!onlineTargets.isEmpty()) {
                alert.seek(Duration.seconds(0));
                alert.play();
            }
            print("Online targets: " + onlineTargets);
            print("Online players: " + onlinePlayers);
        } else {
            print("Server did not respond.");
        }
    }



    private static void print(Object o) {
        String ts = String.format("[%s] ", new Timestamp(new Date().getTime()));
        System.out.println(ts + o.toString());
    }

}