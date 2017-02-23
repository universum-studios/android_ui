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
package universum.studios.android.samples.ui.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import universum.studios.android.support.fragment.ActionBarFragment;
import universum.studios.android.support.samples.ui.SamplesNavigationActivity;

/**
 * @author Martin Albedinsky
 */
public abstract class BaseSamplesFragment extends ActionBarFragment
		implements
		View.OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = "BaseSamplesFragment";

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Activity activity = getActivity();
		if (activity instanceof SamplesNavigationActivity) {
			((SamplesNavigationActivity) activity).setNavigationAccessible(false);
		}
	}

	@Override
	public void onClick(View view) {
		onViewClick(view);
	}
}
