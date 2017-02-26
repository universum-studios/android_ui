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

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Base class that can be used to hold parameters that are needed to draw a specific graphics of a
 * specific widget.
 *
 * @author Martin Albedinsky
 */
public abstract class GraphicsInfo {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "GraphicsInfo";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Paint used to draw graphics associated with this info.
	 */
	public final Paint paint;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #GraphicsInfo(Paint)} with default paint with {@link Paint#ANTI_ALIAS_FLAG}.
	 */
	public GraphicsInfo() {
		this(new Paint(Paint.ANTI_ALIAS_FLAG));
	}

	/**
	 * Creates a new instance of GraphicsInfo with the specified <var>paint</var>.
	 *
	 * @param paint Paint to be used to draw graphics of which parameters will be hold by this info.
	 */
	public GraphicsInfo(@NonNull Paint paint) {
		this.paint = paint;
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Updates this info's paint with settings hold by this info according to the specified <var>stateSet</var>.
	 *
	 * @param stateSet The state for which to update the paint.
	 * @return {@code True} if some setting of the paint has been changed, {@code false} otherwise.
	 */
	public abstract boolean updatePaint(@Nullable int[] stateSet);

	/**
	 * Inner classes ===============================================================================
	 */
}
