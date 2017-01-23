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
package universum.studios.android.ui;

import android.os.Build;

import java.util.Locale;

/**
 * Configuration options for the UI library.
 *
 * @author Martin Albedinsky
 */
public class UiConfig {

	/**
	 * Flag indicating whether the output for the UI library trough log-cat is enabled or not.
	 */
	public static boolean LOG_ENABLED = true;

	/**
	 * Flag indicating whether the debug output for the UI library trough log-cat is enabled or not.
	 */
	public static boolean DEBUG_LOG_ENABLED = false;

	/**
	 * Flag indicating whether the current device is running Android edition that supports material
	 * design features or not.
	 *
	 * @see #MATERIALIZED_LOLLIPOP
	 * @see #MATERIALIZED_MARSHMALLOW
	 */
	public static final boolean MATERIALIZED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

	/**
	 * Flag indicating whether the current device is running materialized {@link Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
	 * Android version or newest version.
	 */
	public static final boolean MATERIALIZED_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

	/**
	 * Flag indicating whether the current device is running materialized {@link Build.VERSION_CODES#M MARSHMALLOW}
	 * Android version or newest version.
	 */
	public static final boolean MATERIALIZED_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

	/**
	 * MOTION EVENTS ===============================================================================
	 */

	/**
	 * Units for velocity used in touch tracking operations.
	 * <p>
	 * Value: <b>one pixel per second</b>
	 */
	public static final int VELOCITY_UNITS = 1000;

	/**
	 * ANIMATIONS ==================================================================================
	 */

	/**
	 * Update interval for animation frames in milliseconds. This interval determines how often should
	 * be frames of a specific animation invalidated.
	 * <p>
	 * Value: <b>60 fps</b>
	 */
	public static final long ANIMATION_FRAME_UPDATE_INTERVAL = 1000 / 60;

	/**
	 * Short duration for animation.
	 * <p>
	 * Value: <b>150 ms</b>
	 */
	public static final long ANIMATION_DURATION_SHORT = 150;

	/**
	 * Medium duration for animation.
	 * <p>
	 * Value: <b>300 ms</b>
	 */
	public static final long ANIMATION_DURATION_MEDIUM = 300;

	/**
	 * Long duration for animation.
	 * <p>
	 * Value: <b>500 ms</b>
	 */
	public static final long ANIMATION_DURATION_LONG = 500;

	/**
	 * LOCALE ======================================================================================
	 */

	/**
	 * Locale used as default across the UI widgets when the current locale does not provide valid
	 * values to be displayed in the UI, for example when displaying names of days of week or months.
	 */
	public static final Locale DEFAULT_LOCALE = Locale.US;
}