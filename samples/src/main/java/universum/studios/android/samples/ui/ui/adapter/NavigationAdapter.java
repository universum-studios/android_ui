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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import universum.studios.android.samples.ui.R;
import universum.studios.android.ui.navigation.BaseNavigationAdapter;
import universum.studios.android.ui.navigation.NavigationItem;

import java.util.List;

/**
 * @author Martin Albedinsky
 */
public final class NavigationAdapter extends BaseNavigationAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = "NavigationAdapter";

	public static final int MY_LIBRARY = 0x1001;
	public static final int MY_FAVORITES = 0x1002;
	public static final int DESIGN_PRINCIPLES = 0x1003;
	public static final int ALL_IMAGES = 0x2001;
	public static final int ALL_VIDEO = 0x2002;
	public static final int ALL_MUSIC = 0x2003;
	public static final int SETTINGS = 0x0001;
	public static final int FEEDBACK = 0x0002;

	public NavigationAdapter(@NonNull Context context) {
		super(context);
	}

	@Nullable
	@Override
	protected List<NavigationItem> onCreateItems(@NonNull List<NavigationItem> items) {
		final int navigationItemTint = resolveNavigationItemTint(mContext);

		NavigationItem item = null;
		items.add(item = createItem(
				MY_LIBRARY,
				R.drawable.ex_vc_navigation_android,
				true,
				R.string.patterns_navigation_drawer_label_my_library,
				true
		));
		item.setIconTintList(ColorStateList.valueOf(Color.parseColor("#cddc39")));

		items.add(item = new NavigationItem.Builder(mResources, mContext.getTheme())
				.viewType(VIEW_TYPE_ITEM)
				.id(MY_FAVORITES)
				.vectorIcon(R.drawable.ic_vc_pets)
				.title(R.string.patterns_navigation_drawer_label_my_favorites)
				.build()
		);
		item.setIconTintList(ColorStateList.valueOf(Color.parseColor("#f44336")));

		items.add(item = createItem(
				DESIGN_PRINCIPLES,
				R.drawable.ic_action_action_polymer,
				R.string.patterns_navigation_drawer_label_design_principles
		));
		item.setIconTintList(ColorStateList.valueOf(Color.parseColor("#4caf50")));
		// -----------------------------------------------------------------------------------------
		items.add(createDivider());
		items.add(createSubheader(0, R.string.patterns_navigation_drawer_subheader_gallery));

		items.add(item = createItem(
				ALL_IMAGES,
				R.drawable.ic_action_image_collections,
				false,
				R.string.patterns_navigation_drawer_label_all_images,
				true
		));
		item.setIconTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));

		items.add(item = createItem(
				ALL_VIDEO,
				R.drawable.ic_action_av_video_collection,
				false,
				R.string.patterns_navigation_drawer_label_all_video,
				true
		));
		item.setIconTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));

		items.add(item = createItem(
				ALL_MUSIC,
				R.drawable.ic_action_av_my_library_music,
				false,
				R.string.patterns_navigation_drawer_label_all_music,
				true
		));
		item.setIconTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
		// -----------------------------------------------------------------------------------------
		items.add(createDivider());
		items.add(item = createItem(
				SETTINGS,
				R.drawable.ic_action_action_settings,
				false,
				R.string.patterns_navigation_drawer_label_settings,
				false
		));
		item.setIconTintList(ColorStateList.valueOf(navigationItemTint));

		items.add(item = createItem(
				FEEDBACK,
				R.drawable.ic_action_communication_email,
				false,
				R.string.patterns_navigation_drawer_label_feedback,
				false
		));
		item.setIconTintList(ColorStateList.valueOf(navigationItemTint));
		return items;
	}

	private int resolveNavigationItemTint(Context context) {
		final TypedValue typedValue = new TypedValue();
		if (context.getTheme().resolveAttribute(R.attr.uiNavigationItemTint, typedValue, true)) {
			return typedValue.data;
		}
		return 0;
	}
}
