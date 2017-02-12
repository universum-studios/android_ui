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
package universum.studios.android.samples.ui.ui.fragment.patterns;

import universum.studios.android.support.fragment.annotation.FactoryFragment;
import universum.studios.android.samples.ui.fragment.BaseExamplesFragmentFactory;

/**
 * @author Martin Albedinsky
 */
public final class PatternsFragments extends BaseExamplesFragmentFactory {

	@SuppressWarnings("unused")
	private static final String TAG = "PatternsFragments";

	/**
	 * ERRORS ======================================================================================
	 */

	public static final int ERRORS_USER_INPUT = 0x20000001;

	public static final int ERRORS_APP = 0x20000002;

	public static final int ERRORS_INCOMPATIBLE_STATE = 0x20000003;

	/**
	 * NAVIGATION DRAWER ===========================================================================
	 */

	// No sub-sections.

	/**
	 * SWIPE TO REFRESH ============================================================================
	 */

	@FactoryFragment(RefreshListFragment.class)
	public static final int SWIPE_TO_REFRESH_LIST = 0xb0000001;

	@FactoryFragment(RefreshGridFragment.class)
	public static final int SWIPE_TO_REFRESH_GRID = 0xb0000002;

	@FactoryFragment(RefreshRecyclerFragment.class)
	public static final int SWIPE_TO_REFRESH_RECYCLER = 0xb0000003;

	@FactoryFragment(RefreshWebFragment.class)
	public static final int SWIPE_TO_REFRESH_WEB = 0xb0000004;

	@FactoryFragment(RefreshLayoutFragment.class)
	public static final int SWIPE_TO_REFRESH_LAYOUT = 0xb0000005;

	/**
	 * SEARCH ======================================================================================
	 */

	// No sub-sections.
}
