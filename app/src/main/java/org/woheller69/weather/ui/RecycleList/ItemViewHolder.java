package org.woheller69.weather.ui.RecycleList;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.woheller69.weather.R;

/**
 * This class holds instances of items that are to be displayed in the list.
 * The idea of this class has been taken from
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.hmhbe8sku
 * as of 2016-08-03. Parts of the code were copied from that source.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder {

    /**
     * Member variables
     */
    private TextView tvInformation;


    /**
     * Constructor.
     *
     * @param itemView The view that contains the fields that are to be set for each list item.
     */
    public ItemViewHolder(View itemView) {
        super(itemView);
        tvInformation = (TextView) itemView.findViewById(R.id.city_overview_list_item_text);
    }

    /**
     * @return Returns the TextView of the item.
     */
    public TextView getTvInformation() {
        return tvInformation;
    }


}