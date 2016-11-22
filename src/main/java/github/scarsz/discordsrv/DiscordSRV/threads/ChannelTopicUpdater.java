package github.scarsz.discordsrv.DiscordSRV.threads;

import github.scarsz.discordsrv.DiscordSRV.Manager;
import github.scarsz.discordsrv.DiscordSRV.util.DiscordUtil;
import github.scarsz.discordsrv.DiscordSRV.util.JDAUtil;
import github.scarsz.discordsrv.DiscordSRV.util.MemUtil;
import net.dv8tion.jda.core.Permission;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @on 11/22/2016
 * @at 1:59 AM
 */
public class ChannelTopicUpdater extends Thread {

    public ChannelTopicUpdater() {
        setName("DiscordSRV - Channel Topic Updater");
    }

    public void run() {
        int rate = Manager.instance.config.getInt("ChannelTopicUpdaterRateInSeconds") * 1000;

        while (!isInterrupted())
        {
            try {
                String chatTopic = applyFormatters(Manager.instance.config.getString("ChannelTopicUpdaterChatChannelTopicFormat"));
                String consoleTopic = applyFormatters(Manager.instance.config.getString("ChannelTopicUpdaterConsoleChannelTopicFormat"));

                if ((Manager.instance.chatChannel == null && Manager.instance.consoleChannel == null) || (chatTopic.isEmpty() && consoleTopic.isEmpty())) interrupt();
                if (Manager.instance.jda == null || Manager.instance.jda.getSelfUser() == null) continue;

                if (!chatTopic.isEmpty() && Manager.instance.chatChannel != null && !JDAUtil.checkPermission(Manager.instance.chatChannel, Permission.MANAGE_CHANNEL))
                    Manager.instance.platform.warning("Unable to update chat channel; no permission to manage channel");
                if (!consoleTopic.isEmpty() && Manager.instance.consoleChannel != null && !JDAUtil.checkPermission(Manager.instance.consoleChannel, Permission.MANAGE_CHANNEL))
                    Manager.instance.platform.warning("Unable to update console channel; no permission to manage channel");

                if (!chatTopic.isEmpty() && Manager.instance.chatChannel != null && JDAUtil.checkPermission(Manager.instance.chatChannel, Permission.MANAGE_CHANNEL))
                    JDAUtil.setTextChannelTopic(Manager.instance.chatChannel, chatTopic);
                if (!consoleTopic.isEmpty() && Manager.instance.consoleChannel != null && JDAUtil.checkPermission(Manager.instance.chatChannel, Permission.MANAGE_CHANNEL))
                    JDAUtil.setTextChannelTopic(Manager.instance.consoleChannel, consoleTopic);
            } catch (NullPointerException ignored) {}

            try { Thread.sleep(rate); } catch (InterruptedException ignored) {}
        }
    }

    @SuppressWarnings({"SpellCheckingInspection", "ConstantConditions"})
    private String applyFormatters(String input) {
        Map<String, String> mem = MemUtil.get();

        input = input
                .replace("%playercount%", Integer.toString(Manager.instance.platform.queryOnlinePlayers().size()))
                .replace("%playermax%", Integer.toString(Manager.instance.platform.queryMaxPlayers()))
                .replace("%date%", new Date().toString())
                .replace("%totalplayers%", Integer.toString(Manager.instance.platform.queryTotalPlayers()))
                .replace("%uptimemins%", Long.toString(TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - Manager.instance.startTime)))
                .replace("%uptimehours%", Long.toString(TimeUnit.NANOSECONDS.toHours(System.nanoTime() - Manager.instance.startTime)))
                .replace("%motd%", DiscordUtil.stripColor(Manager.instance.platform.queryMotd()))
                .replace("%serverversion%", Manager.instance.platform.queryServerVersion())
                .replace("%freememory%", mem.get("freeMB"))
                .replace("%usedmemory%", mem.get("usedMB"))
                .replace("%totalmemory%", mem.get("totalMB"))
                .replace("%maxmemory%", mem.get("maxMB"))
                .replace("%freememorygb%", mem.get("freeGB"))
                .replace("%usedmemorygb%", mem.get("usedGB"))
                .replace("%totalmemorygb%", mem.get("totalGB"))
                .replace("%maxmemorygb%", mem.get("maxGB"))
                .replace("%tps%", Manager.instance.platform.queryTps())
        ;

        return input;
    }
}
