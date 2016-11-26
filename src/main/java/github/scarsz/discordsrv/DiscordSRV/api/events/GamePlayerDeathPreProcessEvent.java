package github.scarsz.discordsrv.DiscordSRV.api.events;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/8/2016
 */
public class GamePlayerDeathPreProcessEvent extends GamePlayerDeathEvent {

    private String player = null;

    public GamePlayerDeathPreProcessEvent(String player, double damage, String deathMessage, String world) {
        this.player = player;
    }

    @Override
    public String getPlayer() {
        return player;
    }
    @Override
    public void setPlayer(String player) {
        this.player = player;
    }

    //TODO

}
