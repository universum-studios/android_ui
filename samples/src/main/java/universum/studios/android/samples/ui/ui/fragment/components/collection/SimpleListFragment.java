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
package universum.studios.android.samples.ui.ui.fragment.components.collection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.fragment.CapitalsListFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(title = R.string.components_navigation_lists_simple)
public final class SimpleListFragment extends CapitalsListFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "SimpleListFragment";

	@Override
	@SuppressWarnings("ConstantConditions")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup view = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		view.removeView(view.findViewById(android.R.id.list));
		view.addView(inflater.inflate(R.layout.list_view_simple, container, false));
		return view;
	}
}
