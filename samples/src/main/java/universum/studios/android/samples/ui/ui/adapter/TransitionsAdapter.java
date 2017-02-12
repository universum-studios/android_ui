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
package universum.studios.android.samples.ui.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import universum.studios.android.support.examples.widget.ExListItemView;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.fragment.animation.TransitionFragmentsA;
import universum.studios.android.samples.ui.model.TransitionExample;
import universum.studios.android.widget.adapter.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Albedinsky
 */
public final class TransitionsAdapter extends SimpleAdapter<TransitionExample, View> {

	@SuppressWarnings("unused")
	private static final String TAG = "TransitionsAdapter";

	private static final int VIEW_TYPES_COUNT = 2;
	private static final int VIEW_TYPE_SUBHEADER = 0;
	private static final int VIEW_TYPE_ITEM = 1;

	public TransitionsAdapter(@NonNull Context context) {
		super(context);
		final List<TransitionExample> items = new ArrayList<>();
		final TransitionExample.Builder builder = new TransitionExample.Builder();
		/**
		 * Scale transitions ---------------------------------------------------------------
		 */
		items.add(createSubheader(R.string.animation_transitions_scale));
		items.add(builder.reset()
				.id(TransitionFragmentsA.SCALE_1)
				.title(R.string.animation_transitions_scale_1)
				.themeA(R.style.Theme_Transitions_Scale_1)
				.layoutResourceA(R.layout.fragment_fab)
				.build());

		/**
		 * Reveal transitions --------------------------------------------------------------
		 */
		items.add(createSubheader(R.string.animation_transitions_reveal));
		items.add(builder.reset()
				.id(TransitionFragmentsA.REVEAL_1)
				.title(R.string.animation_transitions_reveal_1)
				.themeA(R.style.Theme_Transitions_Reveal_1_A)
				.themeB(R.style.Theme_Transitions_Reveal_1_B)
				.layoutResourceA(R.layout.fragment_fab)
				.build());

		changeItems(items);
	}

	private static TransitionExample createSubheader(int titleRes) {
		return new TransitionExample.Subheader(titleRes);
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPES_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		final TransitionExample item = getItem(position);
		return item instanceof TransitionExample.Subheader ? VIEW_TYPE_SUBHEADER : VIEW_TYPE_ITEM;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == VIEW_TYPE_ITEM;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).id;
	}

	@NonNull
	@Override
	protected View onCreateView(@NonNull ViewGroup parent, int position) {
		switch (currentViewType()) {
			case VIEW_TYPE_SUBHEADER:
				return inflate(R.layout.ex_subheader_list, parent);
			default:
				return inflate(R.layout.ex_item_list, parent);
		}
	}

	@Override
	protected void onBindViewHolder(@NonNull View viewHolder, int position) {
		final TransitionExample item = getItem(position);
		switch (currentViewType()) {
			case VIEW_TYPE_SUBHEADER:
				((TextView) viewHolder).setText(item.titleRes);
				break;
			case VIEW_TYPE_ITEM:
				((ExListItemView) viewHolder).setTitle(item.titleRes);
				break;
		}
	}
}
