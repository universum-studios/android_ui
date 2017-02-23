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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import universum.studios.android.samples.ui.R;
import universum.studios.android.support.fragment.annotation.ContentView;


/**
 * @author Martin Albedinsky
 */
@ContentView(R.layout.samples_layout_list_view)
public abstract class BaseSamplesListFragment<A extends ListAdapter> extends BaseSamplesAdapterFragment<ListView, A>
		implements
		AdapterView.OnItemClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = "BaseSamplesListFragment";

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final ListView listView = findAdapterView();
		listView.setEmptyView(getEmptyView());
		listView.setOnItemClickListener(this);
	}

	@Override
	protected void onAttachAdapterToView(@NonNull A adapter) {
		findAdapterView().setAdapter(adapter);
	}

	@Override
	protected void onDetachAdapterFromView(@NonNull A adapter) {
		findAdapterView().setAdapter(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}
}
