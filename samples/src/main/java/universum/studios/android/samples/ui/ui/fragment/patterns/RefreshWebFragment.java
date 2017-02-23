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
import universum.studios.android.samples.ui.ui.fragment.BaseSamplesFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.support.fragment.annotation.MenuOptions;
import universum.studios.android.ui.widget.Refreshable;
import universum.studios.android.ui.widget.WebViewWidget;

/**
 * @author Martin Albedinsky
 */
@MenuOptions(R.menu.refresh)
@ContentView(R.layout.fragment_patterns_refresh_web)
@ActionBarOptions(title = R.string.patterns_navigation_swipe_to_refresh_web)
public final class RefreshWebFragment extends BaseSamplesFragment
		implements
		Refreshable.OnRefreshListener {

	@SuppressWarnings("unused")
	private static final String TAG = "RefreshWebFragment";

	private final Random RANDOM = new Random();
	private final Runnable STOP_REFRESHING = new Runnable() {

		@Override
		public void run() {
			mWebView.setRefreshing(false);
		}
	};

	private WebViewWidget mWebView;

	@Override
	@SuppressWarnings("ConstantConditions")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.mWebView = (WebViewWidget) super.onCreateView(inflater, container, savedInstanceState);
		mWebView.setOnRefreshListener(this);
		return mWebView;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mWebView.loadUrl("http://developer.android.com");
	}

	@Override
	public void onRefresh(final @NonNull Refreshable refreshable) {
		this.performRefresh();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_refresh:
				mWebView.setRefreshing(true);
				this.performRefresh();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void performRefresh() {
		mWebView.postDelayed(STOP_REFRESHING, Math.round(RANDOM.nextFloat() * 10000));
	}
}
