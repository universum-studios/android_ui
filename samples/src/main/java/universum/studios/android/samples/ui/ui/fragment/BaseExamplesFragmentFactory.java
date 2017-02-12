/*
 * =================================================================================================
 *                             Copyright (C) 2015 Martin Albedinsky
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
package universum.studios.android.samples.ui.ui.fragment;

import android.support.annotation.NonNull;

import universum.studios.android.support.fragment.manage.BaseFragmentFactory;
import universum.studios.android.support.fragment.manage.FragmentTransactionOptions;
import universum.studios.android.support.fragment.manage.FragmentTransition;

/**
 * @author Martin Albedinsky
 */
public abstract class BaseExamplesFragmentFactory extends BaseFragmentFactory {

	@SuppressWarnings("unused")
	private static final String TAG = "BaseExamplesFragmentFactory";

	/**
	 * Options used for all 'none-primary' fragments provided by this factory.
	 */
	private final FragmentTransactionOptions OPTIONS = new FragmentTransactionOptions()
			.addToBackStack(true)
			.transition(FragmentTransition.SLIDE_TO_LEFT);

	@NonNull
	@Override
	protected FragmentTransactionOptions onConfigureTransactionOptions(@NonNull FragmentTransactionOptions options) {
		return getSlideableTransactionOptions(options);
	}

	protected FragmentTransactionOptions configureDefaultTransactionOptions(FragmentTransactionOptions options) {
		return super.onConfigureTransactionOptions(options);
	}

	protected FragmentTransactionOptions getSlideableTransactionOptions(FragmentTransactionOptions options) {
		return OPTIONS.tag(getFragmentTag(options.incomingFragmentId));
	}
}
