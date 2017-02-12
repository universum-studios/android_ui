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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Martin Albedinsky
 */
public final class CapitalsRecyclerAdapter extends RecyclerView.Adapter<CapitalsRecyclerAdapter.Holder> {

	@SuppressWarnings("unused")
	private static final String TAG = "CapitalsListAdapter";

	private final CapitalsListAdapter mListAdapter;

	public CapitalsRecyclerAdapter(Context context) {
		this.mListAdapter = new CapitalsListAdapter(context);
	}

	@Override
	public int getItemCount() {
		return mListAdapter.getItemCount();
	}

	@Override
	public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
		return new Holder(mListAdapter.onCreateView(viewGroup, i));
	}

	@Override
	public void onBindViewHolder(Holder holder, int i) {
		mListAdapter.onBindViewHolder(holder.holder, i);
	}

	static final class Holder extends RecyclerView.ViewHolder {

		final CapitalsListAdapter.Holder holder;

		Holder(@NonNull View view) {
			super(view);
			this.holder = new CapitalsListAdapter.Holder(view);
		}
	}

}
