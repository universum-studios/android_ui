/*
 * =================================================================================================
 *                             Copyright (C) 2017 Universum Studios
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
package universum.studios.android.samples.ui.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.fragment.NavigationFragments;
import universum.studios.android.samples.ui.ui.fragment.components.ComponentsNavigationFragment;
import universum.studios.android.samples.ui.ui.fragment.patterns.PatternsNavigationFragment;
import universum.studios.android.samples.ui.ui.fragment.style.StyleNavigationFragment;
import universum.studios.android.support.fragment.BackPressWatcher;
import universum.studios.android.support.fragment.annotation.FragmentAnnotations;
import universum.studios.android.support.fragment.manage.FragmentController;
import universum.studios.android.support.fragment.manage.FragmentRequest;
import universum.studios.android.support.fragment.manage.FragmentRequestInterceptor;
import universum.studios.android.support.fragment.transition.FragmentTransitions;
import universum.studios.android.support.samples.ui.SamplesMainFragment;
import universum.studios.android.support.samples.ui.SamplesNavigationActivity;

/**
 * @author Martin Albedinsky
 */
public final class MainActivity extends SamplesNavigationActivity implements FragmentRequestInterceptor {

	@SuppressWarnings("unused")
	private static final String TAG = "MainActivity";

	static {
		FragmentAnnotations.setEnabled(true);
	}

	private FragmentController mFragmentController;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mFragmentController = new FragmentController(this);
		this.mFragmentController.setFactory(new NavigationFragments());
		this.mFragmentController.setViewContainerId(R.id.samples_container);
	}

	@NonNull
	public FragmentController getFragmentController() {
		return mFragmentController;
	}

	@Nullable
	@Override
	public Fragment interceptFragmentRequest(@NonNull FragmentRequest request) {
		request.transition(FragmentTransitions.CROSS_FADE).replaceSame(true);
		return null;
	}

	@Override
	protected boolean onHandleNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.navigation_item_home:
				mFragmentController.newRequest(new SamplesMainFragment()).execute();
				return true;
			case R.id.navigation_item_style:
				mFragmentController.newRequest(new StyleNavigationFragment()).execute();
				return true;
			case R.id.navigation_item_layout:
				return true;
			case R.id.navigation_item_components:
				mFragmentController.newRequest(new ComponentsNavigationFragment()).execute();
				return true;
			case R.id.navigation_item_patterns:
				mFragmentController.newRequest(new PatternsNavigationFragment()).execute();
				return true;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (dispatchBackPressToCurrentFragment()) {
			return;
		}
		final FragmentManager fragmentManager = getSupportFragmentManager();
		if (fragmentManager.getBackStackEntryCount() > 0 && fragmentManager.popBackStackImmediate()) {
			return;
		}
		super.onBackPressed();
	}

	private boolean dispatchBackPressToCurrentFragment() {
		final Fragment fragment = mFragmentController.findCurrentFragment();
		return fragment instanceof BackPressWatcher && ((BackPressWatcher) fragment).dispatchBackPress();
	}
}
