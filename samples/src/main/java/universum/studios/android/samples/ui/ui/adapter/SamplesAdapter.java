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

import universum.studios.android.support.examples.model.ExListItem;
import universum.studios.android.support.examples.widget.ExListItemView;
import universum.studios.android.samples.ui.R;
import universum.studios.android.widget.adapter.SimpleAdapter;

/**
 * @author Martin Albedinsky
 */
public final class SamplesAdapter extends SimpleAdapter<ExListItem, ExListItemView> {

	@SuppressWarnings("unused")
	private static final String TAG = "ExamplesAdapter";

	public SamplesAdapter(Context context) {
		super(context);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@NonNull
	@Override
	protected View onCreateView(@NonNull ViewGroup parent, int position) {
		return inflate(R.layout.ex_item_list, parent);
	}

	@Override
	protected void onBindViewHolder(@NonNull ExListItemView itemView, int position) {
		itemView.setTitle(getItem(position).getText());
	}
}
