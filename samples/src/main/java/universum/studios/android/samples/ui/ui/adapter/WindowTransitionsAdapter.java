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
package universum.studios.android.samples.ui.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import universum.studios.android.support.examples.widget.ExListItemView;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.model.WindowTransitionExample;
import universum.studios.android.ui.transition.WindowTransition;
import universum.studios.android.widget.adapter.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Albedinsky
 */
public final class WindowTransitionsAdapter extends SimpleAdapter<WindowTransitionExample, ExListItemView> {

	@SuppressWarnings("unused")
	private static final String TAG = "WindowTransitionsAdapter";

	public WindowTransitionsAdapter(Context context) {
		super(context);
		final List<WindowTransitionExample> transitions = new ArrayList<>();
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_none,
				WindowTransition.NONE,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_cross_fade,
				WindowTransition.CROSS_FADE,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_cross_fade_hold,
				WindowTransition.CROSS_FADE_AND_HOLD,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_right,
				WindowTransition.SLIDE_TO_RIGHT,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_right_and_hold,
				WindowTransition.SLIDE_TO_RIGHT_AND_HOLD,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_left,
				WindowTransition.SLIDE_TO_LEFT,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_left_and_hold,
				WindowTransition.SLIDE_TO_LEFT_AND_HOLD,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_top,
				WindowTransition.SLIDE_TO_TOP,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_top_and_hold,
				WindowTransition.SLIDE_TO_TOP_AND_HOLD,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_bottom,
				WindowTransition.SLIDE_TO_BOTTOM,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_bottom_and_hold,
				WindowTransition.SLIDE_TO_BOTTOM_AND_HOLD,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_left_and_scale_out,
				WindowTransition.SLIDE_TO_LEFT_AND_SCALE_OUT,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_right_and_scale_out,
				WindowTransition.SLIDE_TO_RIGHT_AND_SCALE_OUT,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_top_and_scale_out,
				WindowTransition.SLIDE_TO_TOP_AND_SCALE_OUT,
				mResources
		));
		transitions.add(WindowTransitionExample.create(
				R.string.animation_window_transitions_slide_to_bottom_and_scale_out,
				WindowTransition.SLIDE_TO_BOTTOM_AND_SCALE_OUT,
				mResources
		));
		changeItems(transitions);
	}

	@NonNull
	@Override
	protected View onCreateView(@NonNull ViewGroup parent, int position) {
		return inflate(R.layout.ex_item_list, parent);
	}

	@Override
	protected void onBindViewHolder(@NonNull ExListItemView viewHolder, int position) {
		viewHolder.setTitle(getItem(position).titleText);
	}
}
