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

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.AppsGridAdapter;
import universum.studios.android.samples.ui.content.AppsAsyncTask;

/**
 * @author Martin Albedinsky
 */
public class AppsGridFragment extends BaseSamplesGridFragment<AppsGridAdapter> {

	@SuppressWarnings("unused")
	private static final String TAG = "AppsGridFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final AppsGridAdapter adapter = new AppsGridAdapter(getActivity());
		setAdapter(adapter);
		new AppsAsyncTask(adapter).execute();
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(R.string.components_collection_empty);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
		final ApplicationInfo appInfo = getAdapter().getItem(position);
		if (appInfo != null) {
			try {
				startActivity(getAdapter().getPackageManager().getLaunchIntentForPackage(appInfo.packageName));
			} catch (Exception e) {
				Toast.makeText(getActivity(), "No permission to launch " + appInfo.packageName, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
