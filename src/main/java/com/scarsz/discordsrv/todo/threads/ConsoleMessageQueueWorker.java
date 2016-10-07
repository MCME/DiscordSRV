package com.scarsz.discordsrv.todo.threads;

import com.scarsz.discordsrv.Legacy;

public class ConsoleMessageQueueWorker extends Thread {

    public void run() {
        while (!isInterrupted()) {
            String message = "";

            synchronized (Legacy.messageQueue) {
                for (String line : Legacy.messageQueue) {
                    if (message.length() + line.length() + 1 > 2000) {
                        Legacy.sendMessageToConsoleChannel(message);
                        message = "";
                    }
                    message += line + "\n";
                }
                Legacy.messageQueue.clear();
            }
            if (!"".equals(message.replace(" ", "").replace("\n", "")))
                Legacy.sendMessageToConsoleChannel(message);

            try { Thread.sleep(Legacy.plugin.getConfig().getInt("DiscordConsoleChannelLogRefreshRate")); } catch (Exception ignored) {}
        }
    }

}
