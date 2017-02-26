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

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A {@link GraphicsInfo} implementation that can be used for <b>simple color drawing</b> purpose.
 *
 * @author Martin Albedinsky
 * @see TextGraphicsInfo
 */
public class ColorGraphicsInfo extends GraphicsInfo {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ColorGraphicsInfo";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Colors used to update this info's paint color.
	 */
	ColorStateList colors = ColorStateList.valueOf(Color.TRANSPARENT);

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ColorGraphicsInfo with initial Paint.
	 *
	 * @see GraphicsInfo#GraphicsInfo()
	 */
	public ColorGraphicsInfo() {
		super();
	}

	/**
	 * Creates a new instance of ColorGraphicsInfo with the specified <var>paint</var>.
	 *
	 * @see GraphicsInfo#GraphicsInfo(Paint)
	 */
	public ColorGraphicsInfo(@NonNull Paint paint) {
		super(paint);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * @see #updatePaintColor(int[])
	 */
	@Override
	public boolean updatePaint(@Nullable int[] stateSet) {
		return updatePaintColor(stateSet);
	}

	/**
	 * Updates colors of this info to the specified one.
	 *
	 * @param colors   The desired colors to update to.
	 * @param stateSet State for which to pick actual color for paint.
	 * @return {@code True} if colors has been changed and paint updated, {@code false} otherwise.
	 * @see #updatePaintColor(int[])
	 */
	public boolean updateColors(@Nullable ColorStateList colors, @Nullable int[] stateSet) {
		if (this.colors != colors) {
			this.colors = colors;
			return updatePaintColor(stateSet);
		}
		return true;
	}

	/**
	 * Updates a color of this info's paint according to the specified <var>stateSet</var> from the
	 * actual colors hold by this info.
	 *
	 * @param stateSet The state set for which to pick the color.
	 * @return {@code True} if color within the paint has been changed, {@code false} otherwise.
	 * @see ColorStateList#getColorForState(int[], int)
	 */
	@SuppressWarnings("ResourceType")
	public boolean updatePaintColor(@Nullable int[] stateSet) {
		if (colors != null) {
			final int color = colors.isStateful() ? colors.getColorForState(stateSet, colors.getDefaultColor()) : colors.getDefaultColor();
			if (color != paint.getColor()) {
				paint.setColor(color);
				return true;
			}
		}
		return false;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
