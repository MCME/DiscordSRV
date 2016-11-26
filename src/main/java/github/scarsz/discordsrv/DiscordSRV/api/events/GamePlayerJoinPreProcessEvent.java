package github.scarsz.discordsrv.DiscordSRV.api.events;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/8/2016
 */
public class GamePlayerJoinPreProcessEvent extends GamePlayerJoinEvent {

    private String player = null;

    public GamePlayerJoinPreProcessEvent(String player) {
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
