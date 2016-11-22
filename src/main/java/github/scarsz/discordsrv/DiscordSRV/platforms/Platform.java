package github.scarsz.discordsrv.DiscordSRV.platforms;

import java.io.File;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public interface Platform {

    /**
     * Get the File object representing this platform's File for storing DiscordSRV's config
     * @return the config file object
     */
    File getPluginConfigFile();

    /**
     * Trigger an info message on the platform's logger
     * @param message the message to be triggered
     */
    void info(String message);
    /**
     * Trigger a warning message on the platform's logger
     * @param message the message to be triggered
     */
    void warning(String message);
    /**
     * Trigger a severe message on the platform's logger
     * @param message the message to be triggered
     */
    void severe(String message);

}
