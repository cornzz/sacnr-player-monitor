package me.corny.spc;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class CheckerTask extends TimerTask {

    // Sound from http://freesound.org, Name: buttonchime02up.wav, Author: JustinBW
    private MediaPlayer alert = new MediaPlayer(new Media(getClass().getClassLoader().getResource("alert.mp3").toExternalForm()));
    private static String baseIp = "server.sacnr.com";
    private static int basePort = 7777;
    private static SampQuery sampQuery;
    private static List<String> targets = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public CheckerTask() {
        print("Press Ctrl+C at any time to exit the app.");
        print("Setting up SACNR player checker...");
    }

    boolean setup() {
        if (!yesNoDialog("Monitor different server?")) {
            if (yesNoDialog("Check for admins?")) {
                try {
                    print("Fetcching admin list, please wait...");
                    Document document = Jsoup.connect("https://sacnr.com/staff").followRedirects(false).timeout(30000).get();
                    targets = document.body().select(".gendata>tbody>tr>td>p>span>a").eachText();
                } catch (IOException e) {
                    print("Couldn't retrieve admin list: " + e);
                }
            }
        } else {
            print("Enter server address:");
            String[] nextLine = scanner.nextLine().split(":");
            baseIp = nextLine[0];
            try {
                if (nextLine.length > 1) {
                    basePort = Integer.valueOf(nextLine[1]);
                }
            } catch (NumberFormatException ignored) {}
        }
        try {
            sampQuery = new SampQuery(baseIp, basePort);
        } catch (Exception e) {
            print("Invalid server address!");
            return false;
        }
        print("Add targets (separated by comma), or press enter");
        List<String> t = new ArrayList<>(Arrays.asList(scanner.nextLine().split("\\s*,\\s*")));
        t.removeAll(Arrays.asList("", " "));
        targets.addAll(t);
        if (targets.isEmpty()) {
            print("No targets, exiting...");
            System.exit(0);
        }
        print("Targets: " + targets);
        return true;
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

    private static boolean yesNoDialog(String q) {
        while (true) {
            print(q + " [y/n]");
            String yn = scanner.nextLine();
            switch (yn) {
                case "y":
                    return true;
                case "n":
                    return false;
            }
        }
    }

    private static void print(Object o) {
        String ts = String.format("[%s] ", new Timestamp(new Date().getTime()));
        System.out.println(ts + o.toString());
    }

}
