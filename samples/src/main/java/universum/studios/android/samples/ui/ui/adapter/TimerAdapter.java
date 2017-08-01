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
import android.view.ViewGroup;
import android.widget.TextView;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.data.model.TimerItem;
import universum.studios.android.widget.adapter.SimpleSpinnerAdapter;
import universum.studios.android.widget.adapter.holder.ViewHolder;

/**
 * @author Martin Albedinsky
 */
public final class TimerAdapter extends SimpleSpinnerAdapter<TimerAdapter, ViewHolder, ViewHolder, TimerItem> {

	@SuppressWarnings("unused")
	private static final String TAG = "TimerAdapter";

	private static final int ONE_MINUTE = 1000 * 60;
	private final String mFormatMinutes;

	public TimerAdapter(Context context) {
		super(context, new TimerItem[] {
				new TimerItem(5 * ONE_MINUTE),
				new TimerItem(10 * ONE_MINUTE),
				new TimerItem(15 * ONE_MINUTE),
				new TimerItem(30 * ONE_MINUTE)
		});
		this.mFormatMinutes = "%d mins";
	}

	@NonNull
	@Override
	protected ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(inflateView(R.layout.view_spinner_timer, parent));
	}

	@NonNull
	@Override
	protected ViewHolder onCreateDropDownViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(inflateView(R.layout.item_spinner_timer, parent));
	}

	@Override
	protected void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		updateView((TextView) holder.itemView.findViewById(R.id.view_spinner_timer_text_view), position);
	}

	@Override
	protected void onBindDropDownViewHolder(@NonNull ViewHolder viewHolder, int position) {
		updateView((TextView) viewHolder.itemView, position);
	}

	private void updateView(@NonNull TextView view, int position) {
		view.setText(String.format(mFormatMinutes, getItem(position).value / ONE_MINUTE));
	}
}
