/*
 * =================================================================================================
 *                             Copyright (C) 2016 Martin Albedinsky
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
package universum.studios.android.samples.ui.ui.fragment.components.picker;


import universum.studios.android.support.fragment.annotation.FactoryFragment;
import universum.studios.android.support.fragment.manage.BaseFragmentFactory;

/**
 * @author Martin Albedinsky
 */
final class PickersFragments extends BaseFragmentFactory {

	@FactoryFragment(NumberPickerFragment.class)
	static final int NUMBER_PICKER = 0x01;

	@FactoryFragment(DatePickerFragment.class)
	static final int DATE_PICKER = 0x02;
}
