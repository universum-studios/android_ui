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

/**
 * Interface for <b>slideable</b> views. Specifies API for transitioning view along its <b>X</b> and
 * <b>Y</b> axis. Position of the slideable view is changed by updating its <b>left</b> or <b>top</b>
 * position.
 * <p>
 * The slideable view can be moved along its X axis by {@link #setFractionX(float)} and along its Y
 * axis by {@link #setFractionY(float)}. Both of these methods will accept fraction values from
 * the range {@code [-1, 1]} where <b>-1</b> means to move the view by <b>-100 %</b> along the
 * requested axis "backward" and <b>1</b> means to move the view by <b>100 %</b> along the requested
 * axis "forward".
 *
 * @author Martin Albedinsky
 */
public interface Slideable {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Position used to situate a view out of the screen in case such a view does not have its size
	 * initialized yet, so it is unable to compute fraction for X or Y coordinate.
	 */
	int OUT_OF_SCREEN = -9999;

	/**
	 * Flag indicating whether the widgets can use API allowing to modify <b>x</b> and <b>y</b> position
	 * of a view introduced in {@link android.os.Build.VERSION_CODES#HONEYCOMB HONEYCOMB}.
	 */
	boolean SLIDEABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Sets the fraction for X axis of this view. This fraction determines how this view should be
	 * moved along its X axis depends on its current <b>width</b>.
	 * <p>
	 * Setting this value for example to {@code 1} will move this view by <b>100 %</b> of its width
	 * to the right.
	 * <p>
	 * <b>Note</b>, that setting this value out of the proper range can resolve into unpredictable
	 * results.
	 *
	 * @param fraction The desired fraction from the range {@code [-1, 1]}.
	 */
	void setFractionX(float fraction);

	/**
	 * Returns the current fraction for the X axis of this view.
	 *
	 * @return Fraction for X axis.
	 */
	float getFractionX();

	/**
	 * Sets the fraction for Y axis of this view. This fraction determines how this view should be
	 * moved along its Y axis depends on its current <b>height</b>.
	 * <p>
	 * Setting this value for example to {@code -1} will move this view by <b>100 %</b> of its height
	 * to the top.
	 * <p>
	 * <b>Note</b>, that setting this value out of the proper range can resolve into unpredictable
	 * results.
	 *
	 * @param fraction The desired fraction from the range {@code [-1, 1]}.
	 */
	void setFractionY(float fraction);

	/**
	 * Returns the current fraction for the Y axis of this view.
	 *
	 * @return Fraction for Y axis.
	 */
	float getFractionY();
}
