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

import android.support.annotation.NonNull;

import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.MenuOptions;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.fragment.CapitalsListFragment;
import universum.studios.android.ui.widget.Refreshable;

/**
 * @author Martin Albedinsky
 */
@MenuOptions(R.menu.refresh)
@ActionBarOptions(title = R.string.patterns_navigation_swipe_to_refresh_layout)
public final class RefreshLayoutFragment extends CapitalsListFragment
		implements
		Refreshable.OnRefreshListener {

	@SuppressWarnings("unused")
	private static final String TAG = "RefreshLayoutFragment";

	// todo:
	/*
	private final Random RANDOM = new Random();
	private final Runnable CANCEL_REFRESHING = new Runnable() {

		@Override
		public void run() {
			mLayout.setRefreshing(false);
		}
	};

	/*private RefreshableLayout mLayout;

	@Override
	@SuppressWarnings("ConstantConditions")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return mLayout = (RefreshableLayout) inflater.inflate(R.layout.fragment_patterns_refresh_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mLayout.setOnRefreshListener(this);
	}

	*/
	@Override
	public void onRefresh(final @NonNull Refreshable refreshable) {
		// this.startRefresh();
	}
	/*

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_refresh:
				mLayout.setRefreshing(true);
				this.startRefresh();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startRefresh() {
		mLayout.postDelayed(CANCEL_REFRESHING, Math.round(RANDOM.nextFloat() * 10000));
	}*/
}
