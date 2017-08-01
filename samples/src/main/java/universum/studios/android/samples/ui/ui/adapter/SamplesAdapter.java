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
import universum.studios.android.support.samples.model.SampleItem;
import universum.studios.android.widget.adapter.SimpleListAdapter;
import universum.studios.android.widget.adapter.holder.ViewHolder;

/**
 * @author Martin Albedinsky
 */
public final class SamplesAdapter extends SimpleListAdapter<SamplesAdapter, ViewHolder, SampleItem> {

	@SuppressWarnings("unused")
	private static final String TAG = "SamplesAdapter";

	public SamplesAdapter(Context context) {
		super(context);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@NonNull
	@Override
	protected ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(inflateView(R.layout.item_list_sample, parent));
	}

	@Override
	protected void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		((TextView) holder.itemView).setText(getItem(position).getTitle());
	}
}
