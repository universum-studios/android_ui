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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Extended version of {@link android.widget.Scroller}. This updated Scroller allows setting of custom
 * duration for the scroll used whenever {@link #startScroll(int, int, int, int, int)} is called
 * upon an instance of WidgetScroller. The custom duration can be set by {@link #setScrollDuration(int)}.
 * If you want to be used the default duration passed to {@code startScroll(...)} method, set the
 * scroll duration to <b>negative</b>.
 *
 * @author Martin Albedinsky
 */
final class WidgetScroller extends Scroller {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "WidgetScroller";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Duration used whenever {@link #startScroll(int, int, int, int, int)} is called. Must be
	 * <b>none negative</b> to be used.
	 */
	private int mScrollDuration = -1;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #WidgetScroller(android.content.Context, android.view.animation.Interpolator)}
	 * without interpolator.
	 */
	WidgetScroller(Context context) {
		this(context, null);
	}

	/**
	 * Creates a new instance of WidgetScroller with the given <var>interpolator</var>.
	 *
	 * @param interpolator Interpolator used to interpolate scroll.
	 */
	WidgetScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
	}

	/**
	 * Creates a new instance of WidgetScroller with the given <var>interpolator</var>.
	 *
	 * @param interpolator Interpolator used to interpolate scroll.
	 * @param flywheel     {@code True} to support "progressive" behaviour in fling, {@code false}
	 *                     otherwise.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	WidgetScroller(Context context, Interpolator interpolator, boolean flywheel) {
		super(context, interpolator, flywheel);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		super.startScroll(startX, startY, dx, dy, mScrollDuration >= 0 ? mScrollDuration : duration);
	}

	/**
	 * Sets the duration for scroll used whenever {@link #startScroll(int, int, int, int, int)} is
	 * called upon this scroller.
	 *
	 * @param duration The desired duration for scroll in milliseconds. Pass here <b>negative</b>
	 *                 duration to use the default one.
	 */
	void setScrollDuration(int duration) {
		this.mScrollDuration = duration;
	}

	/**
	 * Returns the current duration used for the scroll provided by this scroller.
	 *
	 * @return Scroll duration in milliseconds.
	 */
	int getScrollDuration() {
		return mScrollDuration;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
