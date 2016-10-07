package com.scarsz.discordsrv.objects;

public interface IPlatform {

    /**
     * Disable the platform through the platform's manager
     */
    void disablePlugin();

    /**
     * Loads the platform-dependant config to the Manager's config
     */
    void loadConfig();

}
