package com.scarsz.discordsrv.todo.threads;

import com.scarsz.discordsrv.Legacy;
import org.bukkit.ChatColor;

import java.io.*;

public class ServerLogWatcher extends Thread {

    public void run() {
        int rate = Legacy.plugin.getConfig().getInt("DiscordConsoleChannelLogRefreshRate");
        String message = "";

        FileReader fr = null;
        try {
            fr = new FileReader(new File(new File(".").getAbsolutePath() + "/logs/latest.log").getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fr != null;
        BufferedReader br = new BufferedReader(fr);

        boolean done = false;
        while (!done)
        {
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) done = true;
        }

        while (!isInterrupted())
        {
            try {
                // interrupt if console channel isn't available
                if (Legacy.consoleChannel == null) interrupt();

                // grab next line in console
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (line == null) {
                    // next line in console wasn't found, nothing's happened since the last check

                    // we have a message previously built needed to be sent
                    if (message.length() > 0) {
                        if (message.length() > 2000) message = message.substring(0, 1999);
                        sendMessage(message);
                        message = "";
                    }

                    // sleep until next check
                    try { Thread.sleep(rate); } catch (InterruptedException ignored) {}
                } else {
                    // new line in console found

                    // if line contains a blocked phrase don't send it
                    boolean shouldSkip = false;
                    boolean doNotSendActsAsWhitelist = Legacy.plugin.getConfig().getBoolean("DiscordConsoleChannelDoNotSendPhrasesActsAsWhitelist");
                    for (String phrase : Legacy.plugin.getConfig().getStringList("DiscordConsoleChannelDoNotSendPhrases"))
                        if (line.contains(phrase) == !doNotSendActsAsWhitelist) shouldSkip = true;
                    if (shouldSkip) continue;

                    // apply regex filter
                    line = applyRegex(line);

                    // remove coloring shit
                    line = ChatColor.stripColor(line)
                            .replaceAll("[&§][0-9a-fklmnor]", "") // removing &'s with addition of non-caught §'s if they get through somehow
                            .replaceAll("\\[[0-9]{1,2};[0-9]{1,2};[0-9]{1,2}m", "")
                            .replaceAll("\\[[0-9]{1,3}m", "")
                            .replace("[m", "");

                    if (message.length() + line.length() + 2 <= 2000 && line.length() > 0) {
                        // length of line added to already existing messages will not go over message length limit, add to message
                        if (lineIsOk(line)) message += line + "\n";
                    } else {
                        // length of line added to already existing messages WILL go over message length limit, send
                        // currently built message and reset message to contain only the new line
                        sendMessage(message);
                        if (lineIsOk(line)) message += line + "\n";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean lineIsOk(String input) {
        return !input.replace(" ", "").replace("\n", "").isEmpty();
    }
    private void sendMessage(String input) {
        input = applyRegex(input);

        if (lineIsOk(input)) Legacy.sendMessage(Legacy.consoleChannel, input);
    }
    private String applyRegex(String input) {
        return input.replaceAll(Legacy.plugin.getConfig().getString("DiscordConsoleChannelRegexFilter"), Legacy.plugin.getConfig().getString("DiscordConsoleChannelRegexReplacement"));
    }

}