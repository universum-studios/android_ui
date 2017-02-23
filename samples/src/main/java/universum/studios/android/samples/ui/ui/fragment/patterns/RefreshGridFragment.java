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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.fragment.AppsGridFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.MenuOptions;
import universum.studios.android.ui.widget.GridViewWidget;
import universum.studios.android.ui.widget.Refreshable;

/**
 * @author Martin Albedinsky
 */
@MenuOptions(R.menu.refresh)
@ActionBarOptions(title = R.string.patterns_navigation_swipe_to_refresh_grid)
public final class RefreshGridFragment extends AppsGridFragment
		implements
		Refreshable.OnRefreshListener {

	@SuppressWarnings("unused")
	private static final String TAG = "RefreshGridFragment";

	private final Random RANDOM = new Random();
	private final Runnable CANCEL_REFRESHING = new Runnable() {

		@Override
		public void run() {
			mGridView.setRefreshing(false);
		}
	};

	private GridViewWidget mGridView;

	@Override
	@SuppressWarnings("ConstantConditions")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup view = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		view.removeView(view.findViewById(android.R.id.list));
		view.addView(mGridView = (GridViewWidget) inflater.inflate(R.layout.grid_view_swipe_to_refresh, container, false));
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mGridView.setOnRefreshListener(this);
	}

	@Override
	public void onRefresh(@NonNull Refreshable refreshable) {
		this.startRefresh();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_refresh:
				mGridView.setRefreshing(true);
				this.startRefresh();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startRefresh() {
		mGridView.postDelayed(CANCEL_REFRESHING, Math.round(RANDOM.nextFloat() * 10000));
	}
}
