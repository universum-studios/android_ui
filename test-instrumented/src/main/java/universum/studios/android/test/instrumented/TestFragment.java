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
package universum.studios.android.test.instrumented;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Simple fragment that may be used in <b>Android Instrumented Tests</b>.
 *
 * @author Martin Albedinsky
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class TestFragment extends Fragment {

	/**
	 * Log TAG.
	 */
	@SuppressWarnings("unused")
	private static final String TAG = "TestFragment";

	/**
	 * Id of the TestFragment's content view.
	 */
	public static final int CONTENT_VIEW_ID = android.R.id.custom;

	/**
	 */
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		final FrameLayout contentView = new FrameLayout(inflater.getContext());
		contentView.setId(CONTENT_VIEW_ID);
		return contentView;
	}
}
