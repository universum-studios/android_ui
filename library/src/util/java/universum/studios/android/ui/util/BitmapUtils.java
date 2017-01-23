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
package universum.studios.android.ui.util;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import universum.studios.android.ui.UiConfig;

/**
 * Utility class that can be used for {@link Bitmap} modifications like <b>scaling</b>, <b>cropping</b>.
 *
 * @author Martin Albedinsky
 */
public class BitmapUtils {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "BitmapUtils";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Methods =====================================================================================
	 */

	// todo: cropBitmap(Bitmap bitmap, ...)

	// todo: scaleBitmap(Bitmap bitmap, float scaleX, float scaleY)

	/**
	 * Scales the given <var>bitmap</var> to the desired width and height.
	 *
	 * @param bitmap        The bitmap to be scaled.
	 * @param desiredWidth  The desired width of the new bitmap.
	 * @param desiredHeight The desired height of the new bitmap.
	 * @return New scaled bitmap with the desired dimensions or the same one if its dimensions meets
	 * the desired ones.
	 */
	@NonNull
	public static Bitmap scaleBitmap(@NonNull Bitmap bitmap, int desiredWidth, int desiredHeight) {
		final int bmpWidth = bitmap.getWidth();
		final int bmpHeight = bitmap.getHeight();
		if (bmpWidth == desiredWidth && bmpHeight == desiredHeight) {
			// No scaling needed.
			return bitmap;
		}

		// Compute scale ratios.
		final float widthRatio = desiredWidth / (float) bmpWidth;
		final float heightRatio = desiredHeight / (float) bmpHeight;

		if (UiConfig.DEBUG_LOG_ENABLED) {
			Log.v(TAG, "Computed scale ratios for bitmap's width(" + widthRatio + ") and height(" + heightRatio + ").");
		}

		int newWidth, newHeight;
		if (widthRatio >= heightRatio) {
			// Scale by width.
			newWidth = Math.round(widthRatio * bmpWidth);
			newHeight = Math.round(widthRatio * bmpHeight);
		} else {
			// Scale by height.
			newWidth = Math.round(heightRatio * bmpWidth);
			newHeight = Math.round(heightRatio * bmpHeight);
		}

		// Ensure that one of width or height changed.
		if (newWidth == bmpWidth && newHeight == bmpHeight) {
			return bitmap;
		}

		if (UiConfig.DEBUG_LOG_ENABLED) {
			Log.v(TAG, "Scaling bitmap(w:" + bmpWidth + ", h:" + bmpHeight + ") to new size (w:" + newWidth + ", h:" + newHeight + ").");
		}
		return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
