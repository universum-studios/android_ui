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
package universum.studios.android.samples.ui.ui.fragment.components.collection;

import universum.studios.android.support.fragment.annotation.FactoryFragment;
import universum.studios.android.samples.ui.fragment.BaseExamplesFragmentFactory;

/**
 * @author Martin Albedinsky
 */
public class CollectionFragments extends BaseExamplesFragmentFactory {

	@SuppressWarnings("unused")
	private static final String TAG = "CollectionFragments";

	/**
	 * GRIDS =======================================================================================
	 */

	@FactoryFragment(SimpleGridFragment.class)
	public static final int GRID_SIMPLE = 0x10000000;

	@FactoryFragment(PullableGridFragment.class)
	public static final int GRID_PULLABLE = 0x10000001;

	/**
	 * LISTS =======================================================================================
	 */

	@FactoryFragment(SimpleListFragment.class)
	public static final int LIST_SIMPLE = 0x20000000;

	@FactoryFragment(PullableListFragment.class)
	public static final int LIST_PULLABLE = 0x20000001;

	@FactoryFragment(PullableRecyclerListFragment.class)
	public static final int LIST_RECYCLER_PULLABLE = 0x20000002;

	/**
	 * PAGERS ======================================================================================
	 */

	public static final int PAGER = 0x30000000;
}
