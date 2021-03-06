/*
 * =================================================================================================
 *                             Copyright (C) 2014 Martin Albedinsky
 * =================================================================================================
 *         Licensed under the Apache License, Version 2.0 or later (further "License" only).
 * -------------------------------------------------------------------------------------------------
 * You may use this file only in compliance with the License. More details and copy of this License 
 * you may obtain at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * You can redistribute, modify or publish any part of the code written within this file but as it 
 * is described in the License, the software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES or CONDITIONS OF ANY KIND.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 * =================================================================================================
 */
package universum.studios.android.samples.ui.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.data.model.CapitalItem;
import universum.studios.android.widget.adapter.SimpleListAdapter;
import universum.studios.android.widget.adapter.holder.ViewHolder;

/**
 * @author Martin Albedinsky
 */
public final class CapitalsListAdapter extends SimpleListAdapter<CapitalsListAdapter, CapitalsListAdapter.ItemHolder, CapitalItem> {

	@SuppressWarnings("unused")
	private static final String TAG = "CapitalsListAdapter";

	public CapitalsListAdapter(Context context) {
		super(context);
		final String[] capitalsArray = context.getResources().getStringArray(R.array.capitals);
		final List<CapitalItem> capitals = new ArrayList<>();
		for (final String item : capitalsArray) {
			final String[] data = item.split(":");
			capitals.add(new CapitalItem(
					data[0],
					data[1],
					Integer.parseInt(data[2])
			));
		}
		changeItems(capitals);
	}

	@NonNull
	@Override
	protected ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ItemHolder(inflateView(R.layout.item_list_capital, parent));
	}

	@Override
	protected void onBindViewHolder(@NonNull ItemHolder viewHolder, int position) {
		final CapitalItem item = getItem(position);
		viewHolder.name.setText(item.name);
		viewHolder.population.setText(item.population >= 0 ? Integer.toString(item.population) : "Unknown");
	}

	static final class ItemHolder extends ViewHolder {

		TextView name, population;

		ItemHolder(@NonNull View view) {
			super(view);
			this.name = (TextView) view.findViewById(R.id.item_list_capital_text_view_name);
			this.population = (TextView) view.findViewById(R.id.item_list_capital_text_view_population);
		}
	}
}
