package hu.daniel.vince.humanmobility.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.TimeUnit;

import hu.daniel.vince.humanmobility.model.typeHelpers.PlaceType;
import hu.daniel.vince.humanmobility.view.adapter.item.OverviewChildItem;
import hu.daniel.vince.humanmobility.view.adapter.item.OverviewGroupItem;
import hu.daniel.vince.humanmobility.R;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 20.
 */

public class OverviewAdapter extends BaseExpandableListAdapter {
    private static final String TIME_FORMAT = "MMMM d.";

    private Context context;
    private List<OverviewGroupItem> overviewGroupItems;

    public OverviewAdapter(Context context, List<OverviewGroupItem> overviewGroupItems) {
        this.context = context;
        this.overviewGroupItems = overviewGroupItems;
    }

    @Override
    public int getGroupCount() {
        return overviewGroupItems == null ? 0 : overviewGroupItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        final List<OverviewChildItem> overviewChildItems =
                overviewGroupItems.get(groupPosition).getOverviewChildItems();
        return overviewChildItems == null ? 0 : overviewChildItems.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return overviewGroupItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return overviewGroupItems.get(groupPosition).getOverviewChildItems().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final GroupHolder holder;

        if (convertView == null) {
            holder = new GroupHolder();

            convertView = View.inflate(context, R.layout.list_item_overview, null);
            holder.divider = convertView.findViewById(R.id.item_divider);
            holder.title = (TextView) convertView.findViewById(R.id.item_title);
            holder.recordsTime = (TextView) convertView.findViewById(R.id.item_records_in_time);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.item_progress);
            holder.records = (TextView) convertView.findViewById(R.id.item_records);
            holder.errors = (TextView) convertView.findViewById(R.id.item_errors);
            holder.places = (ViewGroup) convertView.findViewById(R.id.item_places);
            holder.dropdownIcon = (ImageView) convertView.findViewById(R.id.item_dropdown_icon);

            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

        final OverviewGroupItem item = (OverviewGroupItem) getGroup(groupPosition);

        final DateTime today = DateTime.now().withMillisOfDay(0).withMillisOfSecond(0);
        final DateTime yesterday = DateTime.now().withMillisOfDay(0).withMillisOfSecond(0).minusDays(1);

        if (item.isTotal()) {
            holder.title.setText(R.string.main_total);
        } else if (item.getDateTime().getMillis() >= today.getMillis()) {
            holder.title.setText(R.string.main_today);
        } else if (item.getDateTime().getMillis() >= yesterday.getMillis()) {
            holder.title.setText(R.string.main_yesterday);
        } else {
            holder.title.setText(item.getDateTime().toString(TIME_FORMAT));
        }

        holder.recordsTime.setText(String.format(getSortText(
                (int)item.getCount() + (int)item.getErrors())));
        holder.progressBar.setMax((int)item.getCount() + (int)item.getErrors());
        holder.progressBar.setProgress((int) (item.getCount()));

        holder.records.setText(String.valueOf(item.getCount()));
        holder.errors.setText(String.valueOf(item.getErrors()));

        if (getChildrenCount(groupPosition) == 0) {
            convertView.setBackgroundResource(R.color.background);
            holder.places.setVisibility(View.GONE);
        } else {
            convertView.setBackgroundResource(R.drawable.selector_list_item);
            holder.places.setVisibility(View.VISIBLE);
        }

        if (isExpanded) {
            holder.dropdownIcon.setImageResource(R.drawable.ic_dropup_24dp);
        } else {
            holder.dropdownIcon.setImageResource(R.drawable.ic_dropdown_24dp);
        }

        if (groupPosition == 0 || getGroupCount() == 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChildHolder holder;

        if (convertView == null) {
            holder = new ChildHolder();

            convertView = View.inflate(context, R.layout.list_item_overview_expandable, null);
            holder.leftIcon = (ImageView) convertView.findViewById(R.id.item_icon);
            holder.title = (TextView) convertView.findViewById(R.id.item_title);
            holder.description = (TextView) convertView.findViewById(R.id.item_desc);
            holder.rightInfo = (TextView) convertView.findViewById(R.id.item_right_desc);
            holder.divider = convertView.findViewById(R.id.item_divider);
            holder.topPadding = convertView.findViewById(R.id.item_top_padding);

            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }

        final OverviewChildItem item = (OverviewChildItem) getChild(groupPosition, childPosition);

        holder.leftIcon.setImageResource(getPlaceIconResource(item.getType()));
        holder.title.setText(getPlaceTitleResource(item.getType()));
        holder.description.setText(String.format("%s / %s", item.getCount(), item.getErrors()));
        holder.rightInfo.setText(getSortText(item.getCount()));

        if (childPosition == 0) {
            holder.topPadding.setVisibility(View.GONE);
        } else {
            holder.topPadding.setVisibility(View.VISIBLE);
        }

        if (childPosition == getChildrenCount(groupPosition) - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private int getPlaceTitleResource(PlaceType type) {
        switch (type) {
            case HOME:
                return R.string.place_home;
            case WORKPLACE:
                return R.string.place_work;
            case SCHOOL:
                return R.string.place_school;
            default:
                return R.string.place_others;
        }
    }

    private int getPlaceIconResource(PlaceType type) {
        switch (type) {
            case HOME:
                return R.drawable.ic_home_18dp;
            case WORKPLACE:
                return R.drawable.ic_work_18dp;
            case SCHOOL:
                return R.drawable.ic_school_18dp;
            default:
                return R.drawable.ic_place_18dp;
        }
    }

    public String getSortText(final int minutes) {
        if (minutes < 60)
            return String.format(context.getResources().getString(R.string.main_sort_minute), minutes);


        int mins = minutes % 60;
        int hours = (int) TimeUnit.MINUTES.toHours(minutes);
        int days = (int) TimeUnit.MINUTES.toDays(minutes);

        String result = "";

        if (days > 0) {
            hours = hours % 24;
            result = String.format(context.getResources().getString(R.string.main_sort_day), days);
        }

        if (hours > 0) {
            if (!result.isEmpty()) {
                result += " ";
            }
            result += String.format(context.getResources().getString(R.string.main_sort_hour), hours);
        }

        if (mins > 0) {
            if (!result.isEmpty()) {
                result += " ";
            }
            result += String.format(context.getResources().getString(R.string.main_sort_minute), mins);
        }

        return result;
    }


    // region Nested private classes

    private class GroupHolder {
        View divider;
        TextView title;
        TextView recordsTime;
        ProgressBar progressBar;
        TextView records;
        TextView errors;
        ViewGroup places;
        ImageView dropdownIcon;
    }

    private class ChildHolder {
        ImageView leftIcon;
        TextView title;
        TextView description;
        TextView rightInfo;
        View divider;
        View topPadding;
    }

    // endregion
}
