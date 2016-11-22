package github.scarsz.discordsrv.DiscordSRV.util;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import java.util.function.Consumer;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @on 11/21/2016
 * @at 11:02 PM
 */
public class JDAUtil {

    // I can't believe JDA's evolved to the point where I'd have to make a class for this

    public static boolean checkPermission(Channel channel, Permission permission) {
        return checkPermission(channel, channel.getJDA().getSelfUser(), permission);
    }
    public static boolean checkPermission(Channel channel, User user, Permission permission) {
        return channel.getGuild().getMember(user).hasPermission(channel, permission);
    }

    public static Message blockMessage(TextChannel channel, String message) {
        try {
            return channel.sendMessage(message).block();
        } catch (RateLimitedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Message blockMessage(TextChannel channel, Message message) {
        try {
            return channel.sendMessage(message).block();
        } catch (RateLimitedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void queueMessage(TextChannel channel, String message, Consumer<Message> consumer) {
        channel.sendMessage(message).queue(consumer);
    }
    public static void queueMessage(TextChannel channel, Message message, Consumer<Message> consumer) {
        channel.sendMessage(message).queue(consumer);
    }

}
