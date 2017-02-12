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
package universum.studios.android.samples.ui.ui.fragment.components.collection;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.CapitalsRecyclerAdapter;
import universum.studios.android.samples.ui.fragment.BaseExamplesFragment;
import universum.studios.android.ui.widget.RecyclerViewWidget;

/**
 * @author Martin Albedinsky
 */
@ContentView(R.layout.recycler_view_pullable)
@ActionBarOptions(title = R.string.components_navigation_lists_recycler_pullable)
public final class PullableRecyclerListFragment extends BaseExamplesFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "PullableRecyclerFragment";

	private RecyclerViewWidget mRecyclerView;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.mRecyclerView = (RecyclerViewWidget) view.findViewById(android.R.id.list);
		setUpRecyclerView();
	}

	private void setUpRecyclerView() {
		final Context context = getActivity();
		mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
		mRecyclerView.setAdapter(new CapitalsRecyclerAdapter(context));
	}
}
