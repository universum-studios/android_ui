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

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.DisplayMetrics;

/**
 * <b>Utils providing compatibility support.</b>
 * <p>
 * Utility class that can be used to obtain a desired values from resources regardless the current
 * Android version.
 *
 * @author Martin Albedinsky
 */
public final class ResourceUtils {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ResourceUtils";

	/**
	 * Boolean flag indicating whether we can use resources access in a way appropriate for
	 * {@link Build.VERSION_CODES#LOLLIPOP} Android version.
	 */
	private static final boolean ACCESS_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

	/**
	 * Boolean flag indicating whether we can use resources access in a way appropriate for
	 * {@link Build.VERSION_CODES#M} Android version.
	 */
	private static final boolean ACCESS_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Constructors ================================================================================
	 */

	/**
	 */
	private ResourceUtils() {
		// Creation of instances of this class is not publicly allowed.
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Obtains color with the specified <var>resId</var> using the given <var>resources</var>.
	 * <p>
	 * This utility method will obtain the requested color in a way that is appropriate for the
	 * current Android version.
	 *
	 * @param resources The resources that should be used to obtain the color.
	 * @param resId     Resource id of the desired color to obtain.
	 * @param theme     Theme that will be used to resolve theme attributes for the requested list on
	 *                  {@link Build.VERSION_CODES#M} and above Android versions.
	 * @return Instance of the requested color or {@code 0} if the specified resource id is {@code 0}.
	 * @see Resources#getColorStateList(int, Resources.Theme)
	 * @see Resources#getColorStateList(int)
	 */
	@SuppressWarnings({"NewApi", "deprecation"})
	public static int getColor(@NonNull Resources resources, @ColorRes int resId, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
		if (resId == 0) return 0;
		else return ACCESS_MARSHMALLOW ? resources.getColor(resId, theme) : resources.getColor(resId);
	}

	/**
	 * Obtains color state list with the specified <var>resId</var> using the given <var>resources</var>.
	 * <p>
	 * This utility method will obtain the requested color state list in a way that is appropriate
	 * for the current Android version.
	 *
	 * @param resources The resources that should be used to obtain.
	 * @param resId     Resource id of the desired color state list to be obtained.
	 * @param theme     Theme that will be used to resolve theme attributes for the requested list on
	 *                  {@link Build.VERSION_CODES#M} and above Android versions.
	 * @return Instance of the requested color state list or {@code null} if the specified resource
	 * id is {@code 0}.
	 * @see Resources#getColorStateList(int, Resources.Theme)
	 * @see Resources#getColorStateList(int)
	 */
	@Nullable
	@SuppressWarnings({"NewApi", "deprecation"})
	public static ColorStateList getColorStateList(@NonNull Resources resources, @ColorRes int resId, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
		if (resId == 0) return null;
		else return ACCESS_MARSHMALLOW ? resources.getColorStateList(resId, theme) : resources.getColorStateList(resId);
	}

	/**
	 * Obtains vector drawable with the specified <var>resId</var> using the given <var>resources</var>.
	 * <p>
	 * This utility method will obtain the requested vector drawable in a way that is appropriate
	 * for the current Android version.
	 *
	 * @param resources The resources that should be used to obtain the vector drawable.
	 * @param resId     Resource id of the desired vector drawable to obtain.
	 * @param theme     Theme that will be used to resolve theme attributes for the requested drawable
	 *                  on {@link Build.VERSION_CODES#LOLLIPOP} and above Android versions.
	 * @return Instance of the requested vector drawable or {@code null} if the specified resource
	 * id is {@code 0}.
	 * @see #getDrawable(Resources, int, Resources.Theme)
	 * @see VectorDrawableCompat#create(Resources, int, Resources.Theme)
	 */
	@Nullable
	public static Drawable getVectorDrawable(@NonNull Resources resources, @DrawableRes int resId, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
		if (resId == 0) return null;
		else return ACCESS_LOLLIPOP ? getDrawable(resources, resId, theme) : VectorDrawableCompat.create(resources, resId, theme);
	}

	/**
	 * Obtains drawable with the specified <var>resId</var> using the given <var>resources</var>.
	 * <p>
	 * This utility method will obtain the requested drawable in a way that is appropriate for the
	 * current Android version.
	 *
	 * @param resources The resources that should be used to obtain the drawable.
	 * @param resId     Resource id of the desired drawable to obtain.
	 * @param theme     Theme that will be used to resolve theme attributes for the requested drawable
	 *                  on {@link Build.VERSION_CODES#LOLLIPOP} and above Android versions.
	 * @return Instance of the requested drawable or {@code null} if the specified resource id is {@code 0}.
	 * @see Resources#getDrawable(int, Resources.Theme)
	 * @see Resources#getDrawable(int)
	 */
	@Nullable
	@SuppressWarnings({"NewApi", "deprecation"})
	public static Drawable getDrawable(@NonNull Resources resources, @DrawableRes int resId, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
		if (resId == 0) return null;
		else return ACCESS_LOLLIPOP ? resources.getDrawable(resId, theme) : resources.getDrawable(resId);
	}

	/**
	 * Converts the specified <var>density independent pixels</var> value into pixels using density
	 * obtained from the given <var>resources</var>.
	 *
	 * @param resources The application resources used to access current device's density.
	 * @param dp The desired density independent pixels value to convert to raw pixels.
	 * @return Converted value in pixels rounded via {@link Math#round(float)}.
	 * @see DisplayMetrics#density
	 */
	public static int dpToPixels(@NonNull Resources resources, float dp) {
		return Math.round(resources.getDisplayMetrics().density * dp);
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
