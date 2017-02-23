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

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.fragment.CapitalsListFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.MenuOptions;
import universum.studios.android.ui.widget.ListViewWidget;
import universum.studios.android.ui.widget.Refreshable;

/**
 * @author Martin Albedinsky
 */
@MenuOptions(R.menu.refresh)
@ActionBarOptions(title = R.string.patterns_navigation_swipe_to_refresh_list)
public class RefreshListFragment extends CapitalsListFragment
		implements
		Refreshable.OnRefreshListener {

	@SuppressWarnings("unused")
	private static final String TAG = "RefreshListFragment";

	private final Runnable STOP_REFRESHING = new Runnable() {

		@Override
		public void run() {
			mListView.setRefreshing(false);
		}
	};

	private ListViewWidget mListView;

	@Override
	@SuppressWarnings("ConstantConditions")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup view = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		view.removeView(view.findViewById(android.R.id.list));
		view.addView(mListView = (ListViewWidget) inflater.inflate(R.layout.list_view_swipe_to_refresh, container, false));
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView.setOnRefreshListener(this);
	}

	@Override
	public void onRefresh(final @NonNull Refreshable refreshable) {
		this.performRefresh();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_refresh:
				mListView.setRefreshing(true);
				this.performRefresh();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void performRefresh() {
		mListView.postDelayed(STOP_REFRESHING, 100);
	}
}
