package hu.daniel.vince.humanmobility.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.entities.Place;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 03. 07.
 */

public class PlacesAdapter  extends BaseAdapter{

    // region Members

    private Context context;
    private List<Place> items;

    // endregion

    // region Constructor

    public PlacesAdapter(Context context, List<Place> items) {
        this.context = context;
        this.items = items;
    }

    // endregion

    // region Setters

    public void setItems(List<Place> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // endregion

    // region Getters

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = View.inflate(context, R.layout.list_item_place, null);
            holder.title = (TextView) convertView.findViewById(R.id.item_place_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Place item = (Place) getItem(position);
        holder.title.setText(item.getTitle());

        return convertView;
    }

    private class ViewHolder {
        public TextView title;
    }

    // endregion
}
