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
package universum.studios.android.ui.interpolator;

import android.view.animation.Interpolator;

/**
 * Implementation of {@link Interpolator} for scrollable views to provide interpolation for scrolling
 * operations.
 *
 * <h3>Interpolation equation</h3>
 * <b>input -= 1.0f</b>
 * <p>
 * <b>input * input * input * input * input + 1.0f</b>
 *
 * @author Martin Albedinsky
 */
public class ScrollerInterpolator implements Interpolator {

	/**
	 */
	@Override
	public float getInterpolation(float input) {
		input -= 1.0f;
		return input * input * input * input * input + 1.0f;
	}
}
