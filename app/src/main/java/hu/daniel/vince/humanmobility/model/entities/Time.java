package hu.daniel.vince.humanmobility.model.entities;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-10-09.
 */

public class Time {

    // region Members

    private short minutes;
    private int hours;

    // endregion

    // region Getters

    public short getMinutes() {
        return minutes;
    }

    public int getHours() {
        return hours;
    }

    // endregion

    // region Incrementation

    public void incrementTime() {
        minutes++;

        if(minutes == 60) {
            hours++;
            minutes = 0;
        }
    }

    // endregion

}
