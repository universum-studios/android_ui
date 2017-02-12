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
import android.view.MenuItem;

import universum.studios.android.samples.ui.R;
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

	private FragmentController fragmentController;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.fragmentController = new FragmentController(this);
		this.fragmentController.setViewContainerId(R.id.samples_container);
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
				fragmentController.newRequest(new SamplesMainFragment()).execute();
				return true;
			case R.id.navigation_item_style:
				return true;
			case R.id.navigation_item_layout:
				return true;
			case R.id.navigation_item_components:
				return true;
			case R.id.navigation_item_patterns:
				return true;
		}
		return true;
	}

	// todo:
	/*@Override
	protected void onAttachFragmentFactory(@NonNull FragmentController controller) {
		controller.setFragmentFactory(new NavigationFragments());
	}
	*/

	// todo:
	/*@Override
	protected boolean onBackPress() {
		if (dispatchBackPressToCurrentFragment()) return true;
		final FragmentManager fragmentManager = getSupportFragmentManager();
		return (fragmentManager.getBackStackEntryCount() > 0 && fragmentManager.popBackStackImmediate()) || super.onBackPress();
	}

	private boolean dispatchBackPressToCurrentFragment() {
		final Fragment fragment = getCurrentFragment();
		return fragment instanceof BackPressWatcher && ((BackPressWatcher) fragment).dispatchBackPressed();
	}*/
}
