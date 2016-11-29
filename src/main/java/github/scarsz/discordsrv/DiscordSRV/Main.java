package github.scarsz.discordsrv.DiscordSRV;

import github.scarsz.discordsrv.DiscordSRV.api.DiscordSRVListener;
import github.scarsz.discordsrv.DiscordSRV.api.events.*;
import github.scarsz.discordsrv.DiscordSRV.platforms.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @on 11/28/2016
 * @at 4:59 PM
 */
public class Main implements Platform {

    static Manager manager;

    public static void main(String[] args) {
        manager = new Manager(new Main());
        manager.config.put("BotToken", "");
        manager.config.put("DiscordGameStatus", null);
        manager.initialize();
        manager.addListener(new DiscordSRVListener("DiscordSRV - Test Listener") {
            public void debug(String message) {
                System.out.println("DEBUG | " + message);
            }

            @Override
            public void onDiscordGeneric(DiscordGenericEvent event) {
                debug("CALLED " + event.getClass().getSimpleName() + ": " + event);
            }

            @Override
            public void onDiscordGuildChatMessage(DiscordGuildChatMessageEvent event) {
                debug("CALLED " + event.getClass().getSimpleName() + ": " + event);
            }

            @Override
            public void onDiscordPrivateMessageChatMessage(DiscordPrivateMessageChatMessageEvent event) {
                debug("CALLED " + event.getClass().getSimpleName() + ": " + event);
            }

            @Override
            public void onGameChatMessage(GameChatMessageEvent event) {
                debug("CALLED " + event.getClass().getSimpleName() + ": " + event);
            }

            @Override
            public void onGamePlayerDeath(GamePlayerDeathEvent event) {
                debug("CALLED " + event.getClass().getSimpleName() + ": " + event);
            }

            @Override
            public void onGamePlayerJoin(GamePlayerJoinEvent event) {
                debug("CALLED " + event.getClass().getSimpleName() + ": " + event);
            }

            @Override
            public void onGamePlayerQuit(GamePlayerQuitEvent event) {
                debug("CALLED " + event.getClass().getSimpleName() + ": " + event);
            }
        });
    }

    @Override
    public File getPluginConfigFile() {
        return new File("src/main/resources/config.yml");
    }

    @Override
    public void info(String message) {
        System.out.println("INFO | " + message);
    }

    @Override
    public void warning(String message) {
        System.out.println("WARN | " + message);
    }

    @Override
    public void severe(String message) {
        System.out.println("SEVERE | " + message);
    }

    @Override
    public void debug(String message) {
        System.out.println("DEBUG | " + message);
    }

    @Override
    public int queryMaxPlayers() {
        return 69;
    }

    @Override
    public String queryMotd() {
        return "IntelliJ best telliJ";
    }

    @Override
    public List<String> queryOnlinePlayers() {
        return new ArrayList<String>(){{add("someguy");}};
    }

    @Override
    public String queryServerVersion() {
        return "INTELLIJIDEA";
    }

    @Override
    public int queryTotalPlayers() {
        return 1;
    }

    @Override
    public String queryTps() {
        return "20.0";
    }
}
