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
package universum.studios.android.samples.ui.ui.fragment.components;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import universum.studios.android.support.fragment.BaseFragment;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.samples.ui.R;

/**
 * @author Martin Albedinsky
 */
@ContentView(R.layout.fragment_page)
public class PageFragment extends BaseFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "PageFragment";

	private static final String PAGE_LABEL_FORMAT = "%d PAGE";
	private static final String PARAM_INDEX = "universum.studios.android.samples.ui.fragment.components.PageFragment.PARAM.Index";

	private int mIndex;

	public static PageFragment newInstance(int index) {
		final PageFragment fragment = new PageFragment();
		final Bundle params = new Bundle();
		params.putInt(PARAM_INDEX, index);
		fragment.setArguments(params);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mIndex = getArguments().getInt(PARAM_INDEX);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((TextView) view.findViewById(R.id.fragment_page_text_view_label)).setText(
			String.format(PAGE_LABEL_FORMAT, mIndex + 1)
		);
	}
}
