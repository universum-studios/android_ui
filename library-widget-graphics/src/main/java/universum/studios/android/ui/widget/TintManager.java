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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.util.TypedValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.graphics.Colors;
import universum.studios.android.ui.R;

/**
 * Manager used to create tint lists for the most commonly used widgets.
 *
 * @author Martin Albedinsky
 */
public final class TintManager {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	@SuppressWarnings("unused")
	private static final String TAG = "TintManager";

	/**
	 * Alpha ratio for view's disabled state.
	 */
	public static final float ALPHA_RATIO_DISABLED = 0.1f;

	/**
	 * Alpha ratio for view's disabled state when tinted.
	 */
	public static final float ALPHA_RATIO_DISABLED_TINT = 0.25f;

	/**
	 * Default color for view's normal state.
	 */
	public static final int COLOR_NORMAL = Color.parseColor("#c1c1c1");

	/**
	 * Default color for light view's normal state.
	 */
	public static final int COLOR_NORMAL_LIGHT = Color.parseColor("#6d6d6d");

	/**
	 * Default color for view's disabled state.
	 */
	public static final int COLOR_DISABLED = Colors.withAlpha(COLOR_NORMAL, ALPHA_RATIO_DISABLED);

	/**
	 * Default color for light view's disabled state.
	 */
	public static final int COLOR_DISABLED_LIGHT = Colors.withAlpha(COLOR_NORMAL_LIGHT, ALPHA_RATIO_DISABLED);

	/**
	 * Default color for button's disabled state.
	 */
	public static final int COLOR_DISABLED_BUTTON = Color.parseColor("#555555");

	/**
	 * Default color for view's error state.
	 */
	public static final int COLOR_ERROR = Color.parseColor("#f44336");

	/**
	 * Defines an annotation for determining set of allowed tint modes for {@link #parseTintMode(int, android.graphics.PorterDuff.Mode)}.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({TINT_SRC_OVER, TINT_SRC_IN, TINT_SRC_ATOP, TINT_MULTIPLY, TINT_SCREEN, TINT_ADD})
	public @interface TintMode {
	}

	/**
	 * Flag for none tint mode.
	 */
	static final int TINT_NONE = -1;

	/**
	 * Flag for {@link android.graphics.PorterDuff.Mode#SRC_OVER SRC_OVER} tint mode.
	 */
	static final int TINT_SRC_OVER = 3;

	/**
	 * Flag for {@link android.graphics.PorterDuff.Mode#SRC_IN SRC_IN} tint mode.
	 */
	static final int TINT_SRC_IN = 5;

	/**
	 * Flag for {@link android.graphics.PorterDuff.Mode#SRC_ATOP SRC_ATOP} tint mode.
	 */
	static final int TINT_SRC_ATOP = 9;

	/**
	 * Flag for {@link android.graphics.PorterDuff.Mode#MULTIPLY MULTIPLY} tint mode.
	 */
	static final int TINT_MULTIPLY = 14;

	/**
	 * Flag for {@link android.graphics.PorterDuff.Mode#SCREEN SCREEN} tint mode.
	 */
	static final int TINT_SCREEN = 15;

	/**
	 * Flag for {@link android.graphics.PorterDuff.Mode#ADD ADD} tint mode.
	 */
	static final int TINT_ADD = 16;

	/**
	 * Index of color for <b>normal</b> state that can be obtained from theme colors array requested
	 * via {@link #obtainThemeColors(Context)}.
	 */
	private static final int THEME_COLOR_INDEX_NORMAL = 0;

	/**
	 * Index of color for <b>disabled</b> state that can be obtained from theme colors array requested
	 * via {@link #obtainThemeColors(Context)}.
	 */
	private static final int THEME_COLOR_INDEX_DISABLED = 1;

	/**
	 * Index of color for <b>error</b> state that can be obtained from theme colors array requested
	 * via {@link #obtainThemeColors(Context)}.
	 */
	private static final int THEME_COLOR_INDEX_ERROR = 2;

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Parses a {@link android.graphics.PorterDuff.Mode} from the specified <var>value</var>.
	 *
	 * @param value       One of {@link #TINT_SRC_OVER}, {@link #TINT_SRC_IN}, {@link #TINT_SRC_ATOP},
	 *                    {@link #TINT_MULTIPLY}, {@link #TINT_SCREEN}, {@link #TINT_ADD}.
	 * @param defaultMode Default mode to return if the specified value flag has not been recognized.
	 */
	@Nullable
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static PorterDuff.Mode parseTintMode(@TintMode int value, @Nullable PorterDuff.Mode defaultMode) {
		switch (value) {
			case TINT_NONE:
				return null;
			case TINT_SRC_OVER:
				return PorterDuff.Mode.SRC_OVER;
			case TINT_SRC_IN:
				return PorterDuff.Mode.SRC_IN;
			case TINT_SRC_ATOP:
				return PorterDuff.Mode.SRC_ATOP;
			case TINT_MULTIPLY:
				return PorterDuff.Mode.MULTIPLY;
			case TINT_SCREEN:
				return PorterDuff.Mode.SCREEN;
			case TINT_ADD:
				return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? PorterDuff.Mode.ADD : defaultMode;
			default:
				return defaultMode;
		}
	}
	/**
	 * Creates a new instance of ColorStateList as tint list for background of EditText widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for background of EditText widget.
	 */
	@Nullable
	static ColorStateList createEditTextTintColors(@NonNull Context context, int tintColor) {
		final int[] colors = obtainThemeColors(context);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return tintColor != Color.TRANSPARENT ? new ColorStateList(
					new int[][]{
							// Enabled states ------------------------------------------------------
							WidgetStateSet.ENABLED_ERROR,
							WidgetStateSet.ENABLED_FOCUSED,
							WidgetStateSet.ENABLED_ACTIVATED,
							WidgetStateSet.ENABLED_PRESSED,
							WidgetStateSet.ENABLED,
							// Disabled states -----------------------------------------------------
							WidgetStateSet.DISABLED
					},
					new int[]{
							// Error state colors --------------------------------------------------
							colors[THEME_COLOR_INDEX_ERROR],
							// Enabled state colors ------------------------------------------------
							tintColor,
							tintColor,
							tintColor,
							colors[0],
							// Disabled state colors -----------------------------------------------
							colors[1]
					}
			) : null;
		}
		return tintColor != Color.TRANSPARENT ? new ColorStateList(
				new int[][]{
						// Enabled states ------------------------------------------------------
						WidgetStateSet.ENABLED_ERROR,
						WidgetStateSet.ENABLED_FOCUSED,
						WidgetStateSet.ENABLED_PRESSED,
						WidgetStateSet.ENABLED,
						// Disabled states ---------------------------------------------------------
						WidgetStateSet.DISABLED
				},
				new int[]{
						// Error state colors ------------------------------------------------------
						colors[THEME_COLOR_INDEX_ERROR],
						// Enabled state colors ----------------------------------------------------
						tintColor,
						tintColor,
						colors[THEME_COLOR_INDEX_NORMAL],
						// Disabled state colors ---------------------------------------------------
						colors[THEME_COLOR_INDEX_DISABLED]
				}
		) : null;
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for text of label widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for text of label widget.
	 */
	@Nullable
	static ColorStateList createLabelTintColors(@NonNull Context context, int tintColor) {
		final int[] colors = obtainThemeColors(context);
		final int normalColor = colors[THEME_COLOR_INDEX_NORMAL];
		final int disabledColor = colors[THEME_COLOR_INDEX_DISABLED];
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return tintColor != Color.TRANSPARENT ? new ColorStateList(
					new int[][]{
							// Enabled states ------------------------------------------------------
							WidgetStateSet.ENABLED_ACTIVATED,
							WidgetStateSet.ENABLED,
							// Disabled states -----------------------------------------------------
							WidgetStateSet.DISABLED
					},
					new int[]{
							// Enabled state colors ------------------------------------------------
							tintColor,
							normalColor,
							// Disabled state colors -----------------------------------------------
							disabledColor
					}
			) : null;
		}
		return tintColor != Color.TRANSPARENT ? new ColorStateList(
				new int[][]{
						// Enabled states ----------------------------------------------------------
						WidgetStateSet.ENABLED_SELECTED,
						WidgetStateSet.ENABLED,
						// Disabled states =========================================================
						WidgetStateSet.DISABLED
				},
				new int[]{
						// Enabled state colors ----------------------------------------------------
						tintColor,
						normalColor,
						// Disabled state colors ---------------------------------------------------
						disabledColor
				}
		) : null;
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for background of Button widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for background of Button widget.
	 */
	@Nullable
	static ColorStateList createButtonBackgroundTintColors(@NonNull Context context, int tintColor) {
		final int colorPressed = Colors.darker(tintColor, 0.1f);
		return tintColor != Color.TRANSPARENT ? new ColorStateList(
				new int[][]{
						// Enabled states ----------------------------------------------------------
						WidgetStateSet.ENABLED_PRESSED,
						WidgetStateSet.ENABLED_FOCUSED,
						WidgetStateSet.ENABLED,
						// Disabled states ---------------------------------------------------------
						WidgetStateSet.DISABLED
				},
				new int[]{
						// Enabled state colors ----------------------------------------------------
						colorPressed,
						colorPressed,
						tintColor,
						// Disabled state colors ---------------------------------------------------
						COLOR_DISABLED_BUTTON
				}
		) : null;
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for CompoundButton based widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for CompoundButton based widget.
	 */
	@Nullable
	static ColorStateList createCompoundButtonTintColors(@NonNull Context context, int tintColor) {
		final int[] colors = obtainThemeColors(context);
		final int colorNormal = colors[THEME_COLOR_INDEX_NORMAL];
		final int colorDisabled = colors[THEME_COLOR_INDEX_DISABLED];
		return tintColor != Color.TRANSPARENT ? new ColorStateList(
				new int[][]{
						// Enabled states ----------------------------------------------------------
						WidgetStateSet.ENABLED_CHECKED_PRESSED,
						WidgetStateSet.ENABLED_CHECKED_FOCUSED,
						WidgetStateSet.ENABLED_UNCHECKED_PRESSED,
						WidgetStateSet.ENABLED_UNCHECKED_FOCUSED,
						WidgetStateSet.ENABLED_CHECKED,
						WidgetStateSet.ENABLED_UNCHECKED,
						// Disabled states ---------------------------------------------------------
						WidgetStateSet.DISABLED
				},
				new int[]{
						// Enabled state colors ----------------------------------------------------
						tintColor,
						tintColor,
						colorNormal,
						colorNormal,
						tintColor,
						colorNormal,
						// Disabled state colors ---------------------------------------------------
						colorDisabled
				}
		) : null;
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for thumb of SeekBar widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for thumb of SeekBar widget.
	 */
	@Nullable
	static ColorStateList createSeekBarThumbTintColors(@NonNull Context context, int tintColor) {
		final int[] colors = obtainThemeColors(context);
		return tintColor != Color.TRANSPARENT ? new ColorStateList(
				new int[][]{
						// Enabled states ----------------------------------------------------------
						WidgetStateSet.ENABLED,
						// Disabled states ---------------------------------------------------------
						WidgetStateSet.DISABLED
				},
				new int[]{
						// Enabled state colors ----------------------------------------------------
						tintColor,
						// Disabled state colors ---------------------------------------------------
						colors[THEME_COLOR_INDEX_DISABLED]
				}
		) : null;
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for progress of SeekBar widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for progress of SeekBar widget.
	 */
	@Nullable
	static ColorStateList createSeekBarProgressTintColors(@NonNull Context context, int tintColor) {
		return tintColor != Color.TRANSPARENT ? ColorStateList.valueOf(tintColor) : null;
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for progress background of SeekBar widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for progress background of SeekBar widget.
	 */
	@Nullable
	static ColorStateList createSeekBarProgressBackgroundTintColors(@NonNull Context context, int tintColor) {
		final int[] colors = obtainThemeColors(context);
		return tintColor != Color.TRANSPARENT ? new ColorStateList(
				new int[][]{
						// Enabled states ----------------------------------------------------------
						WidgetStateSet.ENABLED,
						// Disabled states ---------------------------------------------------------
						WidgetStateSet.DISABLED
				},
				new int[]{
						// Enabled state colors ----------------------------------------------------
						colors[THEME_COLOR_INDEX_NORMAL],
						// Disabled state colors ---------------------------------------------------
						colors[THEME_COLOR_INDEX_DISABLED]
				}
		) : null;
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for background of Spinner widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New instance of ColorStateList specific for background of Spinner widget.
	 */
	@Nullable
	static ColorStateList createSpinnerTintColors(@NonNull Context context, int tintColor) {
		return createEditTextTintColors(context, tintColor);
	}

	/**
	 * Creates a new instance of ColorStateList as tint list for number selection indicator of
	 * NumberPicker widget.
	 *
	 * @param context   Context used to resolve theme attributes that can be used to create the
	 *                  requested tint list.
	 * @param tintColor Color to be used as primary tint (accent) color.
	 * @return New ColorStateList instance.
	 */
	@Nullable
	static ColorStateList createNumberPickerIndicatorTintColors(@NonNull Context context, int tintColor) {
		final int[] colors = obtainThemeColors(context);
		return tintColor != Color.TRANSPARENT ? new ColorStateList(
				new int[][]{
						// Enabled states ----------------------------------------------------------
						WidgetStateSet.ENABLED_PRESSED,
						WidgetStateSet.ENABLED,
						// Disabled states ---------------------------------------------------------
						WidgetStateSet.DISABLED
				},
				new int[]{
						// Enabled state colors ----------------------------------------------------
						Colors.darker(tintColor, 0.2f),
						tintColor,
						// Disabled state colors ---------------------------------------------------
						colors[THEME_COLOR_INDEX_DISABLED]
				}
		) : null;
	}

	/**
	 * Obtains an array of colors from the current theme containing colors for control's normal,
	 * disabled and error state.
	 *
	 * @param context Context used to access current theme and process also its attributes.
	 * @return Array with following colors:
	 * <ul>
	 * <li>[{@link #THEME_COLOR_INDEX_NORMAL}] = {@link R.attr#colorControlNormal colorControlNormal}</li>
	 * <li>[{@link #THEME_COLOR_INDEX_DISABLED}] = colorControlNormal with alpha value of {@link android.R.attr#disabledAlpha android:disabledAlpha}</li>
	 * <li>[{@link #THEME_COLOR_INDEX_ERROR}] = {@link R.attr#uiColorErrorHighlight uiColorErrorHighlight}</li>
	 * </ul>
	 */
	@Size(3)
	private static int[] obtainThemeColors(Context context) {
		final TypedValue typedValue = new TypedValue();
		final Resources.Theme theme = context.getTheme();
		final boolean isDarkTheme = !theme.resolveAttribute(R.attr.isLightTheme, typedValue, true) || typedValue.data == 0;
		final TypedArray typedArray = context.obtainStyledAttributes(null, R.styleable.Ui_Theme_Tint);
		int colorNormal = isDarkTheme ? COLOR_NORMAL : COLOR_NORMAL_LIGHT;
		int colorDisabled = isDarkTheme ? COLOR_DISABLED : COLOR_DISABLED_LIGHT;
		int colorError = COLOR_ERROR;
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_Theme_Tint_colorControlNormal) {
					colorNormal = typedArray.getColor(index, colorNormal);
				} else if (index == R.styleable.Ui_Theme_Tint_android_disabledAlpha) {
					colorDisabled = Colors.withAlpha(colorNormal, typedArray.getFloat(index, ALPHA_RATIO_DISABLED));
				} else if (index == R.styleable.Ui_Theme_Tint_uiColorErrorHighlight) {
					colorError = typedArray.getColor(index, colorError);
				}
			}
			typedArray.recycle();
		}
		return new int[] {
				colorNormal,
				colorDisabled,
				colorError
		};
	}

	/**
	 * Applies tint to the specified <var>drawable</var> using the specified <var>tintMode</var>.
	 *
	 * @param drawable  The drawable to tint.
	 * @param tintColor A color used as tint for the drawable.
	 * @param tintMode  The blending mode used to apply tint to the drawable.
	 * @return Same but <b>muted</b> drawable with applied tint.
	 */
	@NonNull
	static Drawable tintDrawable(@NonNull Drawable drawable, int tintColor, @NonNull PorterDuff.Mode tintMode) {
		return tintRawDrawable(drawable.mutate(), tintColor, tintMode);
	}

	/**
	 * Applies tint to the specified <var>drawable</var> using the specified <var>tintMode</var>.
	 *
	 * @param drawable  The drawable to tint.
	 * @param tintColor A color used as tint for the drawable.
	 * @param tintMode  The blending mode used to apply tint to the drawable.
	 * @return Same <b>not muted</b> drawable with applied tint.
	 */
	@NonNull
	static Drawable tintRawDrawable(@NonNull Drawable drawable, int tintColor, @NonNull PorterDuff.Mode tintMode) {
		drawable.setColorFilter(tintColor, tintMode);
		return drawable;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
