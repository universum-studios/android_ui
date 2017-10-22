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
 * Implementation of {@link Interpolator} to provide "resistance" like behaviour. Resistance interpolation
 * is simulated a way, where the bigger input is the bigger is resistance.
 *
 * <h3>Interpolation equation</h3>
 * <b>{@code input * Math.max((1 - input), minResistanceFactor)}</b>
 *
 * @author Martin Albedinsky
 */
public class ResistanceInterpolator implements Interpolator {

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Input value at which the resistance is started to be computed.
	 */
	private float mResistanceStartInput = 0f;

	/**
	 * Minimum factor for the resistance equation. This factor specifies minimum input to which can
	 * be interpolation applied.
	 */
	private float mMinResistanceFactor = 0.1f;

	/*
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	public float getInterpolation(float input) {
		if (input > mResistanceStartInput) {
			return input * Math.max((1 - input), mMinResistanceFactor);
		}
		return input;
	}

	/**
	 * Sets the start value for the interpolation input at which is resistance started to be computed.
	 *
	 * @param input The desired input value from the range {@code [0, 1]}.
	 */
	public void setResistanceStartInput(float input) {
		if (input >= 0.0 && input <= 1) {
			this.mResistanceStartInput = input;
		}
	}

	/**
	 * Returns a value of the interpolation input at which is resistance started to be computed.
	 * <p>
	 * Default value: <b>0</b>
	 *
	 * @return Start input value from the range {@code [0, 1]}.
	 */
	public float getResistanceStartInput() {
		return mResistanceStartInput;
	}

	/**
	 * Sets the minimum factor for the resistance equation. This factor specifies minimum input to
	 * which can be interpolation applied. See class {@link ResistanceInterpolator overview} overview
	 * for info.
	 *
	 * @param factor The desired factor from the range {@code [0, 1]}.
	 */
	public void setMinResistanceFactor(float factor) {
		if (factor >= 0 && factor <= 1) {
			this.mMinResistanceFactor = factor;
		}
	}

	/**
	 * Returns the minimum factor for the resistance equation.
	 * <p>
	 * Default value: <b>0.1f</b>
	 *
	 * @return Minimum factor from the range {@code [0, 1]}.
	 */
	public float getMinResistanceFactor() {
		return mMinResistanceFactor;
	}
}
