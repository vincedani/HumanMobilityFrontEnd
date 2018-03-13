package hu.daniel.vince.humanmobility.view.adapter.item;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 21.
 */

public class OverviewGroupItem {

    // region Members

    private DateTime dateTime;
    private List<OverviewChildItem> overviewChildItems = new ArrayList<>();
    private long count;
    private long errors;
    private boolean total;

    // endregion

    // region Constructor

    public OverviewGroupItem(DateTime dateTime,
                             List<OverviewChildItem> overviewChildItems,
                             long count,
                             long errors,
                             boolean total) {
        this.dateTime = dateTime;
        this.overviewChildItems = overviewChildItems;
        this.count = count;
        this.errors = errors;
        this.total = total;
    }

    // endregion

    // region Getters

    public DateTime getDateTime() {
        return dateTime;
    }

    public List<OverviewChildItem> getOverviewChildItems() {
        return overviewChildItems;
    }

    public long getCount() {
        return count;
    }

    public long getErrors() {
        return errors;
    }

    public boolean isTotal() {
        return total;
    }

    // endregion
}
