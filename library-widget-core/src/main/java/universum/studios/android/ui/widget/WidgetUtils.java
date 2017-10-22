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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Set of utils used by extended widgets from this library.
 *
 * @author Martin Albedinsky
 */
@SuppressWarnings("UnusedReturnValue")
public final class WidgetUtils {

	/**
	 */
	private WidgetUtils() {
		// Not allowed to be instantiated publicly.
	}

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
		if (view.hasFocus() || view.requestFocus()) {
			final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			return imm != null && imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
		return false;
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
		if (view.hasFocus()) {
			final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			return imm != null && imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
		}
		return false;
	}

	/**
	 * Obtains a color from the specified <var>colorStateList</var> for the specified <var>stateSet</var>.
	 *
	 * @param colorStateList The colors state list from which to obtain the color.
	 * @param stateSet       The state set used to resolved the color.
	 * @return Resolved color for the state or the default one if the colors state list does not hold
	 * color for the specified state.
	 */
	@ColorInt
	public static int resolveColorForState(@NonNull ColorStateList colorStateList, @NonNull int[] stateSet) {
		return colorStateList.isStateful() ?
				colorStateList.getColorForState(stateSet, colorStateList.getDefaultColor()) :
				colorStateList.getDefaultColor();
	}

	/**
	 * Creates a motion event with {@link MotionEvent#ACTION_CANCEL} action with data copied from
	 * the given <var>source</var> event.
	 * <p>
	 * <b>Do not forget to recycle the acquired event via {@link MotionEvent#recycle()} when you are
	 * done with it.</b>
	 *
	 * @param source The desired motion event from which to copy data for the new event.
	 * @return Motion event that may be used to cancel current motion session.
	 *
	 * @see MotionEvent#obtain(MotionEvent)
	 */
	@NonNull
	public static MotionEvent createMotionCancelingEvent(@NonNull MotionEvent source) {
		final MotionEvent cancelEvent = MotionEvent.obtain(source);
		cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
		return cancelEvent;
	}
}
