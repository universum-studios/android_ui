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

import universum.studios.android.samples.ui.ui.fragment.components.ButtonsFragment;
import universum.studios.android.samples.ui.ui.fragment.components.ComponentsNavigationFragment;
import universum.studios.android.samples.ui.ui.fragment.components.MenusFragment;
import universum.studios.android.samples.ui.ui.fragment.components.ProgressAndActivityFragment;
import universum.studios.android.samples.ui.ui.fragment.components.SlidersFragment;
import universum.studios.android.samples.ui.ui.fragment.components.SwitchesFragment;
import universum.studios.android.samples.ui.ui.fragment.components.TextFieldsFragment;
import universum.studios.android.samples.ui.ui.fragment.components.collection.GridsFragment;
import universum.studios.android.samples.ui.ui.fragment.components.collection.ListsFragment;
import universum.studios.android.samples.ui.ui.fragment.components.picker.PickersFragment;
import universum.studios.android.samples.ui.ui.fragment.layout.MetricsAndKeyLinesFragment;
import universum.studios.android.samples.ui.ui.fragment.patterns.PatternsNavigationFragment;
import universum.studios.android.samples.ui.ui.fragment.patterns.RefreshNavigationFragment;
import universum.studios.android.samples.ui.ui.fragment.style.IconsFragment;
import universum.studios.android.samples.ui.ui.fragment.style.StyleNavigationFragment;
import universum.studios.android.samples.ui.ui.fragment.style.TypographyFragment;
import universum.studios.android.support.fragment.annotation.FactoryFragment;
import universum.studios.android.support.fragment.manage.BaseFragmentFactory;

/**
 * @author Martin Albedinsky
 */
public final class NavigationFragments extends BaseFragmentFactory {

	@SuppressWarnings("unused")
	private static final String TAG = "NavigationFragments";

	/*
	 * ANIMATION ===================================================================================
	 */

	public static final int ANIMATION = 0x10000000;
	public static final int ANIMATION_TRANSITIONS = 0x10000001;
	public static final int ANIMATION_WINDOW_TRANSITIONS = 0x10000002;

	/*
	 * STYLE =======================================================================================
	 */

	@FactoryFragment(StyleNavigationFragment.class)
	public static final int STYLE = 0x20000000;

	@FactoryFragment(IconsFragment.class)
	public static final int STYLE_ICONS = 0x20000002;

	@FactoryFragment(TypographyFragment.class)
	public static final int STYLE_TYPOGRAPHY = 0x20000004;

	/*
	 * LAYOUT ======================================================================================
	 */

	public static final int LAYOUT = 0x30000000;

	@FactoryFragment(MetricsAndKeyLinesFragment.class)
	public static final int LAYOUT_METRICS_AND_KEYLINES = 0x30000001;

	/*
	 * COMPONENTS ==================================================================================
	 */

	@FactoryFragment(ComponentsNavigationFragment.class)
	public static final int COMPONENTS = 0x40000000;

	// public static final int COMPONENTS_BOTTOM_SHEETS = 0x40000001;

	@FactoryFragment(ButtonsFragment.class)
	public static final int COMPONENTS_BUTTONS = 0x40000002;

	// public static final int COMPONENTS_CARDS = 0x40000003;
	// public static final int COMPONENTS_CHIPS = 0x40000004;
	// public static final int COMPONENTS_DIALOGS = 0x40000005;
	// public static final int COMPONENTS_DIVIDERS = 0x40000006;

	@FactoryFragment(GridsFragment.class)
	public static final int COMPONENTS_GRIDS = 0x40000007;

	@FactoryFragment(ListsFragment.class)
	public static final int COMPONENTS_LISTS = 0x40000008;

	// public static final int COMPONENTS_LIST_CONTROLS = 0x40000009;

	@FactoryFragment
	public static final int COMPONENTS_PAGERS = 0x4000000a;

	@FactoryFragment(MenusFragment.class)
	public static final int COMPONENTS_MENUS = 0x4000000b;

	@FactoryFragment(PickersFragment.class)
	public static final int COMPONENTS_PICKERS = 0x4000000c;

	@FactoryFragment(ProgressAndActivityFragment.class)
	public static final int COMPONENTS_PROGRESS_AND_ACTIVITY = 0x4000000d;

	@FactoryFragment(SlidersFragment.class)
	public static final int COMPONENTS_SLIDERS = 0x4000000e;

	// public static final int COMPONENTS_SNACKBARS_AND_TOASTS = 0x4000000f;
	// public static final int COMPONENTS_SUBHEADERS = 0x40000010;

	@FactoryFragment(SwitchesFragment.class)
	public static final int COMPONENTS_SWITCHES = 0x40000011;

	// public static final int COMPONENTS_TABS = 0x40000012;

	@FactoryFragment(TextFieldsFragment.class)
	public static final int COMPONENTS_TEXT_FIELDS = 0x40000013;

	// public static final int COMPONENTS_TOOLTIPS = 0x40000014;

	/*
	 * PATTERNS ====================================================================================
	 */

	@FactoryFragment(PatternsNavigationFragment.class)
	public static final int PATTERNS = 0x50000000;

	// todo
	public static final int PATTERNS_ERRORS = 0x50000002;

	public static final int PATTERNS_NAVIGATION_DRAWER = 0x50000005;

	// Used only for id purpose because this example is situated within a single Activity context.
	public static final int PATTERNS_SEARCH = 0x50000006;

	@FactoryFragment(RefreshNavigationFragment.class)
	public static final int PATTERNS_SWIPE_TO_REFRESH = 0x5000000b;
}
