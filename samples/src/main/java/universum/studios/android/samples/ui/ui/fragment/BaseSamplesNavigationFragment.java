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
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.AdapterView;

import universum.studios.android.samples.ui.ui.MainActivity;
import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;
import universum.studios.android.support.fragment.manage.FragmentController;
import universum.studios.android.support.samples.model.SampleItem;
import universum.studios.android.support.samples.ui.SamplesNavigationActivity;

/**
 * @author Martin Albedinsky
 */
public abstract class BaseSamplesNavigationFragment extends BaseSamplesListFragment<SamplesAdapter> {

	@SuppressWarnings("unused")
	private static final String TAG = "BaseSamplesNavigationFragment";

	protected FragmentController mFragmentController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SamplesAdapter adapter = new SamplesAdapter(getActivity());
		onBindSamples(adapter);
		setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mFragmentController = ((MainActivity) getActivity()).getFragmentController();
	}

	@Override
	public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
		final SampleItem item = getAdapter().getItem(position);
		mFragmentController.newRequest((int) item.getId())
				.addToBackStack(true)
				.execute();
	}

	protected abstract void onBindSamples(@NonNull SamplesAdapter adapter);

	@NonNull
	protected SampleItem createItem(int id, @StringRes int titleResId) {
		return createItem(new SampleItem.Builder(getResources()), id, titleResId);
	}

	@NonNull
	protected SampleItem createItem(SampleItem.Builder builder, int id, @StringRes int titleResId) {
		return builder.reset().id(id).title(titleResId).build();
	}

	@Override
	public void onResume() {
		super.onResume();
		final Activity activity = getActivity();
		if (activity instanceof SamplesNavigationActivity) {
			((SamplesNavigationActivity) activity).setNavigationAccessible(true);
		}
	}
}
