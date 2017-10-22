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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

/**
 * A {@link GraphicsInfo} implementation that can be used for <b>text drawing</b> purpose.
 *
 * @author Martin Albedinsky
 * @see ColorGraphicsInfo
 */
public class TextGraphicsInfo extends GraphicsInfo {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "TextGraphicsInfo";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Text appearance style resource.
	 */
	int mAppearanceRes;

	/**
	 * Text appearance attributes used to update this info's paint setting.
	 */
	final TextAppearance mAppearance = new TextAppearance();

	/**
	 * Bounds rect used when calculating text bounds via {@link #calculateTextBounds(String)}.
	 */
	Rect mBounds;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ColorGraphicsInfo with initial Paint.
	 *
	 * @see GraphicsInfo#GraphicsInfo()
	 */
	public TextGraphicsInfo() {
		super();
	}

	/**
	 * Creates a new instance of ColorGraphicsInfo with the specified <var>paint</var>.
	 *
	 * @see GraphicsInfo#GraphicsInfo(Paint)
	 */
	public TextGraphicsInfo(@NonNull Paint paint) {
		super(paint);
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Initializes text appearance parameters of this info from a style with the specified
	 * <var>resId</var>.
	 *
	 * @param context Context used to parse text appearance attributes.
	 * @param resId   Resource id of the TextAppearance style from which to parse attributes.
	 * @return {@code True} if text appearance attributes has been updated, {@code false} otherwise.
	 */
	public boolean fromTextAppearanceStyle(@NonNull Context context, @StyleRes int resId) {
		if (mAppearanceRes != resId) {
			this.mAppearanceRes = resId;
			return mAppearance.fromStyle(context, resId) && mAppearance.updatePaint(paint, null);
		}
		return false;
	}

	/**
	 * Returns the text appearance object holding all text graphics related attributes set to this
	 * info.
	 *
	 * @return TextAppearance of this text graphics info.
	 */
	@NonNull
	public TextAppearance getAppearance() {
		return mAppearance;
	}

	/**
	 * Updates a text size, color and typeface of this info's paint to the actual hold by this info.
	 *
	 * @param stateSet The state set for which to pick the color.
	 * @see #updatePaintTextSize()
	 * @see #updatePaintColor(int[])
	 * @see #updatePaintTypeface()
	 */
	@Override
	public boolean updatePaint(@Nullable int[] stateSet) {
		return mAppearance.updatePaint(paint, stateSet);
	}

	/**
	 * Updates a text size of this info to the specified one.
	 *
	 * @param textSize The desired text size to update to.
	 * @return {@code True} if text size has been changed and paint updated, {@code false} otherwise.
	 * @see TextAppearance#setTextSize(float)
	 * @see #updatePaintTextSize()
	 */
	public boolean updateTextSize(@FloatRange(from = 0) float textSize) {
		return mAppearance.setTextSize(textSize) && updatePaintTextSize();
	}

	/**
	 * Updates a text size of this info's paint to the actual hold by this info.
	 *
	 * @return {@code True} if text size within the paint has been changed, {@code false} otherwise.
	 * @see TextAppearance#updatePaintTextSize(Paint)
	 */
	public boolean updatePaintTextSize() {
		return mAppearance.updatePaintTextSize(paint);
	}

	/**
	 * Updates a text color of this info to the specified <var>colors</var>.
	 *
	 * @param colors   The colors list from which to pick one color and update paint with.
	 * @param stateSet State for which to pick actual color for paint.
	 * @return {@code True} if color has been changed and paint updated, {@code false} otherwise.
	 */
	public boolean updateTextColor(@Nullable ColorStateList colors, @Nullable int[] stateSet) {
		return mAppearance.setTextColor(colors) && updatePaintColor(stateSet);
	}

	/**
	 * Updates a color of this info's paint according to the specified <var>stateSet</var> from the
	 * actual colors hold by this info.
	 *
	 * @param stateSet The state set for which to pick the color.
	 * @return {@code True} if color within the paint has been changed, {@code false} otherwise.
	 * @see TextAppearance#updatePaintColor(Paint, int[])
	 */
	public boolean updatePaintColor(@Nullable int[] stateSet) {
		return mAppearance.updatePaintColor(paint, stateSet);
	}

	/**
	 * Updates a typeface of this info to the specified one.
	 *
	 * @param typeface The desired typeface to update to. May be {@code null} to create default one
	 *                 from the specified  <var>style</var>.
	 * @param style    One of {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link Typeface#ITALIC}
	 *                 or {@link Typeface#BOLD_ITALIC}.
	 * @return {@code True} if typeface has been changed and paint updated, {@code false} otherwise.
	 * @see #updatePaintTypeface()
	 */
	public boolean updateTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		return (mAppearance.setTextStyle(style) || mAppearance.setTypeface(typeface)) && updatePaintTypeface();
	}

	/**
	 * Updates a typeface of this info to the specified one.
	 *
	 * @param typeface The desired typeface to update to.
	 * @return {@code True} if typeface has been changed and paint updated, {@code false} otherwise.
	 * @see #updatePaintTypeface()
	 */
	public boolean updateTypeface(@Nullable Typeface typeface) {
		return mAppearance.setTypeface(typeface) && updatePaintTypeface();
	}

	/**
	 * Updates a typeface of this info's paint to the actual one hold by this info.
	 *
	 * @return {@code True} if typeface within the paint has been changed, {@code false} otherwise.
	 * @see TextAppearance#updatePaintTypeface(Paint)
	 */
	public boolean updatePaintTypeface() {
		return mAppearance.updatePaintTypeface(paint);
	}

	/**
	 * Calculates bounds for the specified <var>text</var> via {@link Paint#getTextBounds(String, int, int, Rect)}
	 * using this info's paint.
	 *
	 * @param text The text for which to calculate its bounds.
	 * @return Calculated bounds that should be used to properly position the text on a canvas.
	 */
	@NonNull
	public Rect calculateTextBounds(@NonNull String text) {
		if (mBounds == null) {
			this.mBounds = new Rect();
		}
		paint.getTextBounds(text, 0, text.length(), mBounds);
		return mBounds;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
