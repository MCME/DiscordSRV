package github.scarsz.discordsrv.DiscordSRV.api;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @on 11/25/2016
 * @at 10:42 PM
 */
public class Cancelable {

    /**
     * Check whether the event is able to be canceled
     * @return true for pre-process events, otherwise false
     */
    public boolean isCurrentlyCancelable() {
        return this.getClass().getSimpleName().endsWith("PreProcessEvent");
    }
    private boolean canceled = false;
    /**
     * Check if the event is canceled
     * @return whether or not the event is canceled
     */
    public boolean isCanceled() {
        return canceled;
    }
    /**
     * Set the current event to be canceled. If it is unable to be canceled, a RuntimeException will be thrown.
     * @param canceled whether or not the event should be canceled
     */
    public void setCanceled(boolean canceled) {
        if (!isCurrentlyCancelable()) throw new RuntimeException(getClass().getSimpleName() + " is not able to be canceled. Try using a pre-process event.");
        else this.canceled = canceled;
    }

}
