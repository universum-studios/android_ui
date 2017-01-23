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

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Set of utils used by extended widgets from this library.
 *
 * @author Martin Albedinsky
 */
public final class WidgetUtils {

	/**
	 * Shows the soft keyboard on the current window for the given <var>view</var>.
	 *
	 * @param view The view for which to show keyboard. If the view does not have focus, it will be
	 *             requested for it via {@link View#requestFocus()}.
	 * @return {@code True} if keyboard has been shown successfully, {@code false} if the view does
	 * not have focus and it did not took it when requested.
	 * @see InputMethodManager#showSoftInput(View, int)
	 * @see InputMethodManager#SHOW_FORCED
	 */
	public static boolean showSoftKeyboard(@NonNull View view) {
		if (!view.hasFocus() && !view.requestFocus()) return false;
		final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	/**
	 * Hides the soft keyboard from the current window using token of the given <var>view</var>.
	 *
	 * @param view The view for which to hide keyboard.
	 * @return {@code True} if keyboard has been hidden successfully, {@code false} if the view does
	 * not have focus.
	 * @see InputMethodManager#hideSoftInputFromWindow(IBinder, int)
	 * @see InputMethodManager#RESULT_UNCHANGED_SHOWN
	 */
	public static boolean hideSoftKeyboard(@NonNull View view) {
		if (!view.hasFocus()) return false;
		final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
	}

	/**
	 * Obtains a color from the specified <var>colorStateList</var> for the specified <var>stateSet</var>.
	 *
	 * @param colorStateList The colors state list from which to obtain the color.
	 * @param stateSet       The state set used to resolved the color.
	 * @return Resolved color for the state or the default one if the colors state list does not hold
	 * color for the specified state.
	 */
	public static int resolveColorForState(@NonNull ColorStateList colorStateList, @NonNull int[] stateSet) {
		return colorStateList.isStateful() ?
				colorStateList.getColorForState(stateSet, colorStateList.getDefaultColor()) :
				colorStateList.getDefaultColor();
	}
}
