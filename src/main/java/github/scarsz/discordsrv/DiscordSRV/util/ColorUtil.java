package github.scarsz.discordsrv.DiscordSRV.util;

import java.util.regex.Pattern;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class ColorUtil {

    private static final Pattern colorStripPattern = Pattern.compile("(?i)" + String.valueOf('\u00A7') + "[0-9A-FK-OR]");

    /**
     * Return the given input message after being stripped of coloring
     * @param input the message to have colors stripped upon
     * @return the stripped message
     */
    public static String stripColor(String input) {
        return input != null ? colorStripPattern.matcher(input).replaceAll("") : null;
    }

}
