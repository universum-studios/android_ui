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
package universum.studios.android.ui.widget;

import android.os.Build;
import android.view.Gravity;

/**
 * Utility class that supports features related to {@link Gravity} for backward compatibility.
 *
 * @author Martin Albedinsky
 */
final class WidgetGravity extends Gravity {

	/**
	 * Boolean flag indicating whether the current Android version supports <b>RTL</b> layout direction.
	 */
	static final boolean RTL_SUPPORT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

	/**
	 * Compatibility version of {@link Gravity#getAbsoluteGravity(int, int)}.
	 */
	@SuppressWarnings("NewApi")
	public static int getAbsoluteGravity(int gravity, int layoutDirection) {
		return RTL_SUPPORT ? Gravity.getAbsoluteGravity(gravity, layoutDirection) : gravity;
	}
}
