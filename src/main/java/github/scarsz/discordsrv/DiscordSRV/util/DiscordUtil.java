package github.scarsz.discordsrv.DiscordSRV.util;

import github.scarsz.discordsrv.DiscordSRV.Manager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class DiscordUtil {

    /**
     * Get the Role's name. If the Role is null, an empty string is returned.
     * @param role
     * @return
     */
    public static String getRoleName(Role role) {
        return role == null ? "" : role.getName();
    }

    /**
     * Get the top hierarchical Role of the User
     * @param user
     * @param guild
     * @return
     */
    public static Role getTopRole(User user, Guild guild) {
        Role highestRole = null;
        for (Role role : guild.getMember(user).getRoles()) {
            if (highestRole == null) highestRole = role;
            else if (highestRole.getPosition() < role.getPosition()) highestRole = role;
        }
        return highestRole;
    }

    /**
     * Convert @mentions into Discord-compatible <@12345678901234567890> mentions
     * @param message
     * @param guild
     * @return
     */
    public static String convertMentionsFromNames(String message, Guild guild) {
        if (!message.contains("@")) return message;
        List<String> splitMessage = new ArrayList<>(Arrays.asList(message.split("@| ")));
        for (Member member : guild.getMembers())
            for (String segment : splitMessage)
                if (member.getEffectiveName().toLowerCase().equals(segment.toLowerCase()))
                    splitMessage.set(splitMessage.indexOf(segment), member.getAsMention());
        splitMessage.removeAll(Arrays.asList("", null));
        return String.join(" ", splitMessage);
    }

    /**
     * Trigger a mass exodus of messages in the TextChannel given the bot has the Manage Messages permission
     * @param channel
     * @return
     */
    public static int purgeChannel(TextChannel channel) {
        if (!channel.getGuild().getMember(channel.getJDA().getSelfUser()).hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            String message = "I was told to purge the current channel but I don't have the `Manage Messages` permission.";
            if (channel.getGuild().getMember(channel.getJDA().getSelfUser()).hasPermission(channel, Permission.MESSAGE_WRITE))
                channel.sendMessage(message).queue();
            else Manager.instance.platform.warning(message);
            return -1;
        }

        try {
            int deletions = 0;
            List<Message> messages = channel.getHistory().retrievePast(100).block();
            while (messages.size() == 100) {
                channel.deleteMessages(messages);
                deletions += messages.size();
                messages = channel.getHistory().retrievePast(100).block();
            }
            if (messages.size() > 0) channel.deleteMessages(messages);
            deletions += messages.size();
            return deletions;
        } catch (RateLimitedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Return the given String with Markdown escaped. Useful for sending things to Discord.
     * @param text
     * @return
     */
    public static String escapeMarkdown(String text) {
        return text.replace("_", "\\_").replace("*", "\\*").replace("~", "\\~");
    }

    /**
     * Strip the given String of Minecraft coloring. Useful for sending things to Discord.
     * @param text the given String to strip colors from
     * @return the given String with coloring stripped
     */
    public static String stripColor(String text) {
        return text == null ? null : stripColorPattern.matcher(text).replaceAll("");
    }
    private static final Pattern stripColorPattern = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-OR]");

    /**
     * Send the given String message to the given TextChannel
     * @param channel
     * @param message
     */
    public static void sendMessage(TextChannel channel, String message) {
        sendMessage(channel, message, 0);
    }

    /**
     * Send the given String message to the given TextChannel that will expire
     * @param channel the TextChannel to send the message to
     * @param message the message to send to the TextChannel
     * @param expiration milliseconds until expiration of message. if this is 0, the message will not expire
     */
    public static void sendMessage(TextChannel channel, String message, int expiration) {
        if (channel == null ||
                channel.getJDA() == null ||
                !JDAUtil.checkPermission(channel, channel.getJDA().getSelfUser(), Permission.MESSAGE_READ) ||
                !JDAUtil.checkPermission(channel, channel.getJDA().getSelfUser(), Permission.MESSAGE_WRITE))
            return;

        message = ColorUtil.stripColor(message);

        String overflow = null;
        if (message.length() > 2000) {
            Manager.instance.platform.warning("Tried sending message with length of " + message.length() + " (" + (message.length() - 2000) + " over limit)");
            overflow = message.substring(2000);
            message = message.substring(0, 2000);
        }

        JDAUtil.queueMessage(channel, message, m -> {
            if (expiration > 0 && JDAUtil.checkPermission(channel, Permission.MESSAGE_MANAGE)) {
                try { Thread.sleep(expiration); } catch (InterruptedException e) { e.printStackTrace(); }
                if (JDAUtil.checkPermission(channel, Permission.MESSAGE_MANAGE)) m.deleteMessage().queue(); else Manager.instance.platform.warning("Could not delete message in channel " + channel + ", no permission to manage messages");
            }
        });
        if (overflow != null) sendMessage(channel, overflow, expiration);
    }

}
