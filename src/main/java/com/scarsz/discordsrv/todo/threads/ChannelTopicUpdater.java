package com.scarsz.discordsrv.todo.threads;

import com.scarsz.discordsrv.Legacy;
import com.scarsz.discordsrv.todo.objects.Lag;
import com.scarsz.discordsrv.util.MemUtil;
import net.dv8tion.jda.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ChannelTopicUpdater extends Thread {

    public void run() {
        int rate = Legacy.plugin.getConfig().getInt("ChannelTopicUpdaterRateInSeconds") * 1000;

        while (!isInterrupted())
        {
            try {
                String chatTopic = applyFormatters(Legacy.plugin.getConfig().getString("ChannelTopicUpdaterChatChannelTopicFormat"));
                String consoleTopic = applyFormatters(Legacy.plugin.getConfig().getString("ChannelTopicUpdaterConsoleChannelTopicFormat"));

                if ((Legacy.chatChannel == null && Legacy.consoleChannel == null) || (chatTopic.isEmpty() && consoleTopic.isEmpty())) interrupt();
                if (Legacy.jda == null || Legacy.jda.getSelfInfo() == null) continue;

                if (!chatTopic.isEmpty() && Legacy.chatChannel != null && !Legacy.chatChannel.checkPermission(Legacy.jda.getSelfInfo(), Permission.MANAGE_CHANNEL))
                    Legacy.plugin.getLogger().warning("Unable to update chat channel; no permission to manage channel");
                if (!consoleTopic.isEmpty() && Legacy.consoleChannel != null && !Legacy.consoleChannel.checkPermission(Legacy.jda.getSelfInfo(), Permission.MANAGE_CHANNEL))
                    Legacy.plugin.getLogger().warning("Unable to update console channel; no permission to manage channel");

                if (!chatTopic.isEmpty() && Legacy.chatChannel != null && Legacy.chatChannel.checkPermission(Legacy.jda.getSelfInfo(), Permission.MANAGE_CHANNEL))
                    Legacy.chatChannel.getManager().setTopic(chatTopic).update();
                if (!consoleTopic.isEmpty() && Legacy.consoleChannel != null && Legacy.consoleChannel.checkPermission(Legacy.jda.getSelfInfo(), Permission.MANAGE_CHANNEL))
                    Legacy.consoleChannel.getManager().setTopic(consoleTopic).update();
            } catch (NullPointerException ignored) {}

            try { Thread.sleep(rate); } catch (InterruptedException ignored) {}
        }
    }

    @SuppressWarnings({"SpellCheckingInspection", "ConstantConditions"})
    private String applyFormatters(String input) {
        if (Legacy.plugin.getConfig().getBoolean("PrintTiming")) Legacy.plugin.getLogger().info("Format start: " + input);
        long startTime = System.nanoTime();

        Map<String, String> mem = MemUtil.get();

        input = input
                .replace("%playercount%", Integer.toString(Legacy.getOnlinePlayers().size()))
                .replace("%playermax%", Integer.toString(Bukkit.getMaxPlayers()))
                .replace("%date%", new Date().toString())
                .replace("%totalplayers%", Bukkit.getWorlds().size() != 0 ? Integer.toString(new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath(), "/playerdata").listFiles(f -> {return f.getName().endsWith(".dat");}).length) : String.valueOf(0))
                .replace("%uptimemins%", Long.toString(TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - Legacy.startTime)))
                .replace("%uptimehours%", Long.toString(TimeUnit.NANOSECONDS.toHours(System.nanoTime() - Legacy.startTime)))
                .replace("%motd%", ChatColor.stripColor(Bukkit.getMotd().replaceAll("&([0-9a-qs-z])", "")))
                .replace("%serverversion%", Bukkit.getBukkitVersion())
                .replace("%freememory%", mem.get("freeMB"))
                .replace("%usedmemory%", mem.get("usedMB"))
                .replace("%totalmemory%", mem.get("totalMB"))
                .replace("%maxmemory%", mem.get("maxMB"))
                .replace("%freememorygb%", mem.get("freeGB"))
                .replace("%usedmemorygb%", mem.get("usedGB"))
                .replace("%totalmemorygb%", mem.get("totalGB"))
                .replace("%maxmemorygb%", mem.get("maxGB"))
                .replace("%tps%", Lag.getTPSString())
        ;

        if (Legacy.plugin.getConfig().getBoolean("PrintTiming")) Legacy.plugin.getLogger().info("Format done in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + "ms: " + input);

        return input;
    }
}