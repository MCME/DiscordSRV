package github.scarsz.discordsrv.DiscordSRV.api.events;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/8/2016
 */
public class GamePlayerQuitPreProcessEvent extends GamePlayerQuitEvent {

    private String player = null;

    public GamePlayerQuitPreProcessEvent(String player) {
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
