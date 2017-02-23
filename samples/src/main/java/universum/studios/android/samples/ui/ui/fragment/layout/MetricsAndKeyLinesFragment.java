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
package universum.studios.android.samples.ui.ui.fragment.layout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.fragment.BaseSamplesFragment;
import universum.studios.android.support.fragment.annotation.ActionModeOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.ui.util.ResourceUtils;
import universum.studios.android.ui.widget.ActionTextButton;

/**
 * @author Martin Albedinsky
 */
@ActionModeOptions(menu = R.menu.form)
@ContentView(R.layout.fragment_layout_metrics_and_keylines)
public final class MetricsAndKeyLinesFragment extends BaseSamplesFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "MetricsAndKeyLinesFragment";

	private Toolbar mToolbar1;

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.mToolbar1 = (Toolbar) view.findViewById(R.id.fragment_layout_metrics_and_keylines_toolbar_1);
		this.mToolbar1.setNavigationIcon(ResourceUtils.getVectorDrawable(
				getResources(),
				R.drawable.samples_vc_menu_24dp,
				getContextTheme()
		));
		this.mToolbar1.setTitle("Toolbar");
		startActionMode();
	}

	@Override
	protected void onActionModeStarted(@NonNull ActionMode actionMode) {
		super.onActionModeStarted(actionMode);
		actionMode.setTitle("Action Mode");
		ActionTextButton.setText(actionMode.getMenu(), R.id.menu_action_confirm, "Confirm");
		ActionTextButton.setOnClickListener(actionMode.getMenu(), R.id.menu_action_confirm, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
