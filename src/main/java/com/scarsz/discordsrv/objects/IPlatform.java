package com.scarsz.discordsrv.objects;

public interface IPlatform {

    /**
     * Disable the platform through the platform's manager
     * @return whether or not the platform supported disabling itself
     */
    boolean disablePlatform();

    /**
     * Loads the platform-dependant config to the Manager's config
     */
    void loadConfigToManager();

    void info(String message);
    void warning(String message);
    void severe(String message);

}
