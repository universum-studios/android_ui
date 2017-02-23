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
package universum.studios.android.samples.ui.ui.fragment.patterns;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import java.util.Random;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.CapitalsRecyclerAdapter;
import universum.studios.android.samples.ui.ui.fragment.BaseSamplesFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.support.fragment.annotation.MenuOptions;
import universum.studios.android.ui.widget.RecyclerViewWidget;
import universum.studios.android.ui.widget.Refreshable;

/**
 * @author Martin Albedinsky
 */
@MenuOptions(R.menu.refresh)
@ContentView(R.layout.fragment_patterns_refresh_recycler)
@ActionBarOptions(title = R.string.patterns_navigation_swipe_to_refresh_list)
public final class RefreshRecyclerFragment extends BaseSamplesFragment
		implements
		Refreshable.OnRefreshListener {

	@SuppressWarnings("unused")
	private static final String TAG = "RefreshRecyclerFragment";

	private final Random RANDOM = new Random();
	private final Runnable STOP_REFRESHING = new Runnable() {

		@Override
		public void run() {
			mRecyclerView.setRefreshing(false);
		}
	};

	private RecyclerViewWidget mRecyclerView;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.app_bar_layout);
		appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
				mRecyclerView.setRefreshGestureEnabled(verticalOffset == 0);
			}
		});
		this.mRecyclerView = (RecyclerViewWidget) view.findViewById(android.R.id.list);
		setUpRecyclerView();
	}

	private void setUpRecyclerView() {
		final Context context = getActivity();
		mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
		mRecyclerView.setOnRefreshListener(this);
		mRecyclerView.setAdapter(new CapitalsRecyclerAdapter(context));
	}

	@Override
	public void onRefresh(final @NonNull Refreshable refreshable) {
		this.performRefresh();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_refresh:
				mRecyclerView.setRefreshing(true);
				this.performRefresh();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void performRefresh() {
		mRecyclerView.postDelayed(STOP_REFRESHING, Math.round(RANDOM.nextFloat() * 10000));
	}
}
