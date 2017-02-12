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
import android.support.annotation.Nullable;

import universum.studios.android.support.fragment.manage.FragmentController;
import universum.studios.android.samples.ui.fragment.BaseSectionNavigationFragment;

/**
 * @author Martin Albedinsky
 */
public abstract class BasePatternsFragment extends BaseSectionNavigationFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "BasePatternsFragment";

	/**
	 * Factory with main navigation fragments.
	 */
	private FragmentController.FragmentFactory mNavigationFragments;

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Change fragment factory for the main fragment controller by the factory with fragments
		// specific for this section.
		this.mNavigationFragments = mFragmentController.getFragmentFactory();
		mFragmentController.setFragmentFactory(new PatternsFragments());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Change back navigation fragments factory.
		mFragmentController.setFragmentFactory(mNavigationFragments);
	}
}
