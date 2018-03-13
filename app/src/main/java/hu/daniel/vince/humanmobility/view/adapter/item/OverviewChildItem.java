package hu.daniel.vince.humanmobility.view.adapter.item;

import hu.daniel.vince.humanmobility.model.typeHelpers.PlaceType;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 21.
 */

public class OverviewChildItem {

    // region Members

    private PlaceType type;
    private int count;
    private int errors;

    // endregion

    // region Constructor

    public OverviewChildItem(PlaceType type, int count, int errors) {
        this.type = type;
        this.count = count;
        this.errors = errors;
    }

    // endregion

    // region Getters

    public PlaceType getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public int getErrors() {
        return errors;
    }

    // endregion

    // region toString

    @Override
    public String toString() {
        return "OverviewChildItem{" +
                "type=" + type +
                ", count=" + count +
                ", errors=" + errors +
                '}';
    }

    // endregion
}
