package com.scarsz.discordsrv.todo.listeners;

import com.scarsz.discordsrv.BukkitPlatform.
import com.scarsz.discordsrv.Manager;
import com.scarsz.discordsrv.platforms.bukkit.BukkitPlatform;
import com.scarsz.discordsrv.todo.objects.SingleCommandSender;
import com.scarsz.discordsrv.util.DebugHandler;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.*;
import java.util.*;

@SuppressWarnings({"unchecked", "MismatchedQueryAndUpdateOfCollection"})
public class DiscordListener extends ListenerAdapter {

    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getMessage().getRawContent().matches("[0-9][0-9][0-9][0-9]"))
            handleLink(event);
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // if message is from self do not process
        if (event.getAuthor().getId() != null && event.getJDA().getSelfInfo().getId() != null && event.getAuthor().getId().equals(event.getJDA().getSelfInfo().getId())) return;

        // notify chat listeners
        BukkitPlatform.manager.notifyListeners(event);

        // canned responses
        boolean canned = false;
        for (Map.Entry<String, String> entry : BukkitPlatform.responses.entrySet()) {
            String trigger = entry.getKey();
            String message = entry.getValue();
            //System.out.println("\"" + trigger + "\" -> \"" + event.getMessage().getRawContent() + "\": " + event.getMessage().getRawContent().startsWith(trigger));
            if (event.getMessage().getRawContent().startsWith(trigger)) {
                Manager.sendMessage(event.getChannel(), message);
                canned = true;
            }
        }
        if (canned) return;

        if (event.getMessage().getRawContent().equalsIgnoreCase(BukkitPlatform.plugin.getConfig().getString("DiscordChannelPurgeCommand")))
            handleChannelPurge(event);
        else if ((event.getAuthor().getId().equals("95088531931672576") || event.getGuild().getOwner().getId().equals(event.getAuthor().getId())) && event.getMessage().getRawContent().equalsIgnoreCase("debug"))
            handleDebug(event);
        else if (BukkitPlatform.getDestinationChannelName(event.getChannel()) != null && BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelDiscordToMinecraft"))
            handleChat(event);
        else if (event.getChannel().equals(BukkitPlatform.consoleChannel))
            handleConsole(event);
    }

    private void handleChannelPurge(GuildMessageReceivedEvent event) {
        if (BukkitPlatform.plugin.getConfig().getString("DiscordChannelPurgeCommand").equals("") || event.getGuild().getRolesForUser(event.getAuthor()) == null || event.getGuild().getRolesByName(BukkitPlatform.plugin.getConfig().getString("DiscordChannelPurgeCommandRoles")) == null) return;

        List<Role> allowedRoles = new ArrayList<>();
        for (String allowedRole : BukkitPlatform.plugin.getConfig().getStringList("DiscordChannelPurgeCommandRoles"))
            event.getGuild().getRolesByName(allowedRole).forEach(allowedRoles::add);
        if (!CollectionUtils.containsAny(event.getGuild().getRolesForUser(event.getAuthor()), allowedRoles) && !event.getAuthor().getId().equals("95088531931672576")) return;

        if (!event.getChannel().checkPermission(event.getJDA().getSelfInfo(), Permission.MANAGE_CHANNEL)) {
            String message = "I have no permission to manage the channel, thus I can't purge it. Sorry.";
            if (event.getChannel().checkPermission(event.getJDA().getSelfInfo(), Permission.MESSAGE_WRITE))
                event.getChannel().sendMessageAsync(message, null);
            else
                event.getAuthor().getPrivateChannel().sendMessageAsync(message, null);
            return;
        }

        int deletions = BukkitPlatform.purgeChannel(event.getChannel());
        event.getChannel().sendMessageAsync("The current channel has been purged (" + deletions + " deletions). Some messages might still be visible but they're actually deleted. Press control + R (command + shift + R on OS X) to refresh your Discord client and get the latest messages.", null);
        BukkitPlatform.plugin.getLogger().info("Discord user " + event.getAuthor() + " purged channel " + event.getChannel());
    }
    private void handleDebug(GuildMessageReceivedEvent event) {
        BukkitPlatform.sendMessage(event.getChannel(), "A debug report has been generated and is available at " + DebugHandler.run());
    }
    private void handleChat(GuildMessageReceivedEvent event) {
        // return if should not send discord chat
        if (!BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelDiscordToMinecraft")) return;

        for (String phrase : BukkitPlatform.plugin.getConfig().getStringList("DiscordChatChannelBlockedPhrases")) if (event.getMessage().getContent().contains(phrase)) return;

        String message = event.getMessage().getStrippedContent();
        if (message.replace(" ", "").isEmpty()) return;
        if (processChannelListCommand(event, message)) return;
        if (processConsoleCommand(event, message)) return;

        if (message.length() > BukkitPlatform.plugin.getConfig().getInt("DiscordChatChannelTruncateLength")) message = message.substring(0, BukkitPlatform.plugin.getConfig().getInt("DiscordChatChannelTruncateLength"));

        List<String> rolesAllowedToColor = BukkitPlatform.plugin.getConfig().getStringList("DiscordChatChannelRolesAllowedToUseColorCodesInChat");

        String formatMessage = event.getGuild().getRolesForUser(event.getAuthor()).isEmpty()
                ? BukkitPlatform.plugin.getConfig().getString("DiscordToMinecraftChatMessageFormatNoRole")
                : BukkitPlatform.plugin.getConfig().getString("DiscordToMinecraftChatMessageFormat");

        // strip colors if role doesn't have permission
        boolean shouldStripColors = true;
        for (Role role : event.getGuild().getRolesForUser(event.getAuthor()))
            if (rolesAllowedToColor.contains(role.getName())) shouldStripColors = false;
        if (shouldStripColors) message = ChatColor.stripColor(message.replaceAll("&[0-9a-qs-z]", "")); // color stripping

        formatMessage = formatMessage
                .replace("%message%", message)
                .replace("%username%", BukkitPlatform.getDisplayName(event.getGuild(), event.getAuthor()))
                .replace("%toprole%", BukkitPlatform.getRoleName(BukkitPlatform.getTopRole(event)))
                .replace("%toprolecolor%", BukkitPlatform.convertRoleToMinecraftColor(BukkitPlatform.getTopRole(event)))
                .replace("%allroles%", BukkitPlatform.getAllRoles(event))
                .replace("\\~", "~") // get rid of badly escaped characters
                .replace("\\*", "") // get rid of badly escaped characters
                .replace("\\_", "_"); // get rid of badly escaped characters

        formatMessage = ChatColor.translateAlternateColorCodes('&', formatMessage);
        BukkitPlatform.broadcastMessageToMinecraftServer(formatMessage, event.getMessage().getRawContent(), BukkitPlatform.getDestinationChannelName(event.getChannel()));

        formatMessage = ChatColor.stripColor(formatMessage).replaceAll("º[0-9a-qs-z]", "");
        if (BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelBroadcastDiscordMessagesToConsole")) BukkitPlatform.plugin.getLogger().info("Chat: " + formatMessage);
    }
    private void handleConsole(GuildMessageReceivedEvent event) {
        // get if blacklist acts as whitelist
        boolean DiscordConsoleChannelBlacklistActsAsWhitelist = BukkitPlatform.plugin.getConfig().getBoolean("DiscordConsoleChannelBlacklistActsAsWhitelist");
        // get banned commands
        List<String> DiscordConsoleChannelBlacklistedCommands = BukkitPlatform.plugin.getConfig().getStringList("DiscordConsoleChannelBlacklistedCommands");
        // convert to all lower case
        for (int i = 0; i < DiscordConsoleChannelBlacklistedCommands.size(); i++) DiscordConsoleChannelBlacklistedCommands.set(i, DiscordConsoleChannelBlacklistedCommands.get(i).toLowerCase());
        // get message for manipulation
        String requestedCommand = event.getMessage().getContent();
        // remove all spaces at the beginning of the requested command to handle pricks trying to cheat the system
        while (requestedCommand.substring(0, 1).equals(" ")) requestedCommand = requestedCommand.substring(1);
        // select the first part of the requested command, being the main part of it we care about
        requestedCommand = requestedCommand.split(" ")[0].toLowerCase(); // *op* person
        // command white/blacklist checking
        boolean allowed = DiscordConsoleChannelBlacklistActsAsWhitelist == DiscordConsoleChannelBlacklistedCommands.contains(requestedCommand);

        // return if command not allowed
        if (!allowed) return;

        // log command to console log file, if this fails the command is not executed for safety reasons unless this is turned off
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(new File(".").getAbsolutePath() + "/./" + BukkitPlatform.plugin.getConfig().getString("DiscordConsoleChannelUsageLog")).getAbsolutePath(), true)))) {
            out.println("[" + new Date() + " | ID " + event.getAuthor().getId() + "] " + event.getAuthor().getUsername() + ": " + event.getMessage().getContent());
        }catch (IOException e) {
            BukkitPlatform.plugin.getLogger().warning("Error logging console action to " + BukkitPlatform.plugin.getConfig().getString("DiscordConsoleChannelUsageLog")); if (BukkitPlatform.plugin.getConfig().getBoolean("CancelConsoleCommandIfLoggingFailed")) return;}

        // if server is running paper spigot it has to have it's own little section of code because it whines about timing issues
        if (!BukkitPlatform.plugin.getConfig().getBoolean("UseOldConsoleCommandSender"))
            Bukkit.getScheduler().runTask(BukkitPlatform.plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), event.getMessage().getContent()));
        else
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), event.getMessage().getContent());
    }
    private void handleLink(PrivateMessageReceivedEvent event) {
        if (BukkitPlatform.linkingCodes.containsKey(event.getMessage().getRawContent())) {
            BukkitPlatform.accountLinkManager.link(BukkitPlatform.linkingCodes.get(event.getMessage().getRawContent()), event.getAuthor().getId());
            BukkitPlatform.linkingCodes.remove(event.getMessage().getRawContent());
            event.getChannel().sendMessageAsync("Your Discord account has been linked to UUID " + BukkitPlatform.accountLinkManager.getUuid(event.getAuthor().getId()), null);
            if (Bukkit.getPlayer(BukkitPlatform.accountLinkManager.getUuid(event.getAuthor().getId())).isOnline()) Bukkit.getPlayer(BukkitPlatform.accountLinkManager.getUuid(event.getAuthor().getId())).sendMessage(ChatColor.AQUA + "Your UUID has been linked to Discord ID " + event.getAuthor());
        } else {
            event.getChannel().sendMessageAsync("I don't know of such a code, try again.", null);
        }
    }

    private boolean userHasRole(GuildMessageReceivedEvent event, List<String> roles) {
        User user = event.getAuthor();
        List<Role> userRoles = event.getGuild().getRolesForUser(user);
        for (Role role : userRoles)
            for (String roleName : roles)
                if (roleName.equalsIgnoreCase(role.getName())) return true;
        return false;
    }
    private boolean processChannelListCommand(GuildMessageReceivedEvent event, String message) {
        if (!BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelListCommandEnabled")) return false;
        if (!message.toLowerCase().startsWith(BukkitPlatform.plugin.getConfig().getString("DiscordChatChannelListCommandMessage").toLowerCase())) return false;

        if (BukkitPlatform.getOnlinePlayers().size() == 0) {
            BukkitPlatform.sendMessage(event.getChannel(), BukkitPlatform.plugin.getConfig().getString("DiscordChatChannelListCommandFormatNoOnlinePlayers"));
            return true;
        }

        List<String> onlinePlayers = new ArrayList<>();
        BukkitPlatform.getOnlinePlayers().forEach(player -> onlinePlayers.add(ChatColor.stripColor(player.getDisplayName())));

        String playerlistMessage = "";
        playerlistMessage += "```\n";
        playerlistMessage += BukkitPlatform.plugin.getConfig().getString("DiscordChatChannelListCommandFormatOnlinePlayers").replace("%playercount%", BukkitPlatform.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
        playerlistMessage += "\n";
        playerlistMessage += String.join(", ", onlinePlayers);

        if (playerlistMessage.length() > 1996) playerlistMessage = playerlistMessage.substring(0, 1993) + "...";
        playerlistMessage += "\n```";
        BukkitPlatform.sendMessage(event.getChannel(), playerlistMessage, true, BukkitPlatform.plugin.getConfig().getInt("DiscordChatChannelListCommandExpiration") * 1000);

        // expire message after specified time
        if (BukkitPlatform.plugin.getConfig().getInt("DiscordChatChannelListCommandExpiration") > 0 && BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelListCommandExpirationDeleteRequest")) {
            try { Thread.sleep(BukkitPlatform.plugin.getConfig().getInt("DiscordChatChannelListCommandExpiration") * 1000); } catch (InterruptedException e) { e.printStackTrace(); }
            if (event.getChannel().checkPermission(BukkitPlatform.jda.getSelfInfo(), Permission.MESSAGE_MANAGE)) event.getMessage().deleteMessage(); else BukkitPlatform.plugin.getLogger().warning("Could not delete message in channel " + event.getChannel() + ", no permission to manage messages");
        }

        return true;
    }
    private boolean processConsoleCommand(GuildMessageReceivedEvent event, String message) {
        if (!BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelConsoleCommandEnabled")) return false;
        
        String[] parts = message.split(" ", 2);
        
        if (parts.length < 2) return false;
        if (!parts[0].equalsIgnoreCase(BukkitPlatform.plugin.getConfig().getString("DiscordChatChannelConsoleCommandPrefix"))) return false;

        // check if user has a role able to use this
        List<String> rolesAllowedToConsole = BukkitPlatform.plugin.getConfig().getStringList("DiscordChatChannelConsoleCommandRolesAllowed");
        boolean allowed = userHasRole(event, rolesAllowedToConsole);
        if (!allowed) {
            // tell user that they have no permission
            if (BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelConsoleCommandNotifyErrors"))
                event.getAuthor().getPrivateChannel().sendMessageAsync(BukkitPlatform.plugin.getConfig().getString("DiscordChatChannelConsoleCommandNotifyErrorsFormat")
                        .replace("%user%", event.getAuthor().getUsername())
                        .replace("%error%", "no permission")
                        , null);
            return true;
        }

        // check if user has a role that can bypass the white/blacklist
        boolean canBypass = false;
        for (String roleName : BukkitPlatform.plugin.getConfig().getStringList("DiscordChatChannelConsoleCommandWhitelistBypassRoles")) {
            boolean isAble = userHasRole(event, Collections.singletonList(roleName));
            canBypass = isAble || canBypass;
        }

        // check if requested command is white/blacklisted
        boolean commandIsAbleToBeUsed;
        
        if (canBypass) {
            commandIsAbleToBeUsed = true;
        }
        else { 
            // Check the white/black list
            String requestedCommand = parts[1].split(" ")[0];
            boolean whitelistActsAsBlacklist = BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelConsoleCommandWhitelistActsAsBlacklist");

            List<String> commandsToCheck = BukkitPlatform.plugin.getConfig().getStringList("DiscordChatChannelConsoleCommandWhitelist");
            boolean isListed = commandsToCheck.contains(requestedCommand);
            
            commandIsAbleToBeUsed = isListed ^ whitelistActsAsBlacklist;
        }
       
        if (!commandIsAbleToBeUsed) {
            // tell user that the command is not able to be used
            if (BukkitPlatform.plugin.getConfig().getBoolean("DiscordChatChannelConsoleCommandNotifyErrors"))
                event.getAuthor().getPrivateChannel().sendMessageAsync(BukkitPlatform.plugin.getConfig().getString("DiscordChatChannelConsoleCommandNotifyErrorsFormat")
                        .replace("%user%", event.getAuthor().getUsername())
                        .replace("%error%", "command is not able to be used")
                        , null);
            return true;
        }

        // at this point, the user has permission to run commands at all and is able to run the requested command, so do it
        Bukkit.getScheduler().runTask(BukkitPlatform.plugin, () -> Bukkit.getServer().dispatchCommand(new SingleCommandSender(event, Bukkit.getServer().getConsoleSender()), parts[1]));

        // log command to console log file, if this fails the command is not executed for safety reasons unless this is turned off
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(new File(".").getAbsolutePath() + "/./" + BukkitPlatform.plugin.getConfig().getString("DiscordConsoleChannelUsageLog")).getAbsolutePath(), true)))) {
            out.println("[" + new Date() + " | ID " + event.getAuthor().getId() + "] " + event.getAuthor().getUsername() + ": " + parts[1]);
        }catch (IOException e) {
            BukkitPlatform.plugin.getLogger().warning("Error logging console action to " + BukkitPlatform.plugin.getConfig().getString("DiscordConsoleChannelUsageLog")); if (BukkitPlatform.plugin.getConfig().getBoolean("CancelConsoleCommandIfLoggingFailed")) return true;}

        return true;
    }

}
