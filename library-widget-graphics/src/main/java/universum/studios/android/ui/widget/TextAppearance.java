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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.font.Font;
import universum.studios.android.ui.R;

/**
 * TextAppearance contains attributes that can be specified to 'describe' appearance of a text that
 * is drawn on {@link Canvas}. See {@link R.styleable#Ui_TextAppearance TextAppearance attributes}
 * to see which attributes can be specified.
 * <p>
 * An instance of TextAppearance object can be instantiated via {@link #TextAppearance()} constructor
 * and then values for the desired attributes can be specified vie one of setter methods or they can
 * be parsed form a desired <b>text appearance</b> style via {@link #fromStyle(Context, int)} and than
 * accessed via one of getter methods.
 *
 * @author Martin Albedinsky
 */
public final class TextAppearance {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "TextAppearance";

	/**
	 * Defines an annotation for determining set of available text styles.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC})
	public @interface TextStyle {
	}

	/**
	 * Defines an annotation for determining set of typeface indexes used to identify a specific
	 * typeface.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({SANS_SERIF, SERIF, MONOSPACE})
	public @interface TypefaceIndex {
	}

	/**
	 * Flag for <b>sans-serif</b> typeface.
	 */
	public static final int SANS_SERIF = 1;

	/**
	 * Flag for <b>serif</b> typeface.
	 */
	public static final int SERIF = 2;

	/**
	 * Flag for <b>monospace</b> typeface.
	 */
	public static final int MONOSPACE = 3;

	/*
	 * Static members ==============================================================================
	 */

	/**
	 * Default colors state list used for text of TextAppearance instances.
	 * <p>
	 * Contains single {@link Color#BLACK BLACK} color.
	 */
	public static final ColorStateList TEXT_COLORS = ColorStateList.valueOf(Color.BLACK);

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Raw text size in pixels.
	 */
	private float mTextSize;

	/**
	 * State list of text colors.
	 */
	private ColorStateList mTextColors = TEXT_COLORS;

	/**
	 * Style of text.
	 */
	private int mTextStyle = Typeface.NORMAL;

	/**
	 * Index of text typeface.
	 */
	private int mTypefaceIndex;

	/**
	 * Font family of text.
	 */
	private String mFontFamily;

	/**
	 * Typeface created from {@link #mFontFamily} or {@link #mTypefaceIndex}.
	 */
	private Typeface mTypeface;

	/**
	 * Boolean flag indicating whether we should re-create the typeface instance the next time it is
	 * needed or not.
	 */
	private boolean mInvalidateTypeface;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of empty TextAppearance.
	 */
	public TextAppearance() {
		this.reset();
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Set ups this text appearance holder with text appearance values from the specified <var>resId</var>
	 * style.
	 *
	 * @param context Context used to parse text appearance attributes from the specified style resource.
	 * @param resId   Resource id of the desired TextAppearance style from which to parse attributes.
	 * @return {@code True} if some of attributes of this text appearance has changed, {@code false}
	 * otherwise.
	 */
	public boolean fromStyle(@NonNull Context context, @StyleRes int resId) {
		this.reset();
		final TypedArray typedArray = context.obtainStyledAttributes(resId, R.styleable.Ui_TextAppearance);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_TextAppearance_android_textSize) {
					this.mTextSize = typedArray.getDimensionPixelSize(index, (int) mTextSize);
				} else if (index == R.styleable.Ui_TextAppearance_android_textColor) {
					this.mTextColors = typedArray.getColorStateList(index);
				} else if (index == R.styleable.Ui_TextAppearance_android_textStyle) {
					this.mTextStyle = typedArray.getInt(index, mTextStyle);
				} else if (index == R.styleable.Ui_TextAppearance_android_typeface) {
					this.mTypefaceIndex = typedArray.getInt(index, mTypefaceIndex);
				} else if (index == R.styleable.Ui_TextAppearance_android_fontFamily) {
					this.mFontFamily = typedArray.getString(index);
				} else if (index == R.styleable.Ui_TextAppearance_uiFont) {
					final String fontPath = typedArray.getString(index);
					if (!TextUtils.isEmpty(fontPath)) {
						this.mTypeface = Font.create(fontPath).getTypeface(context);
					}
				}
			}
			typedArray.recycle();
			return n > 0;
		}
		return false;
	}

	/**
	 * Sets a size for text.
	 *
	 * @param size The desired text size in floating pixels.
	 * @return {@code True} if text size of this text appearance has changed, {@code false}
	 * otherwise.
	 * @see #getTextSize()
	 */
	public boolean setTextSize(float size) {
		if (mTextSize != size) {
			this.mTextSize = size;
			return true;
		}
		return false;
	}

	/**
	 * Returns the size for text specified for this text appearance.
	 * <p>
	 * Default value: <b>{@code 0}</b>
	 *
	 * @return This appearance's text size.
	 * @see #setTextSize(float)
	 * @see #updatePaintTextSize(Paint)
	 */
	public float getTextSize() {
		return mTextSize;
	}

	/**
	 * Sets a stateful set of colors for text.
	 *
	 * @param colors The desired colors. May be {@code null} to clear the current ones.
	 * @return {@code True} if colors of this text appearance have changed, {@code false} otherwise.
	 * @see #getTextColor()
	 */
	public boolean setTextColor(@Nullable ColorStateList colors) {
		if (mTextColors != colors) {
			this.mTextColors = colors;
			return true;
		}
		return false;
	}

	/**
	 * Returns the colors for text specified for this text appearance.
	 * <p>
	 * If no colors has been specified the {@link #TEXT_COLORS} is returned by default.
	 *
	 * @return This appearance's text colors.
	 * @see #setTextColor(ColorStateList)
	 * @see #updatePaintColor(Paint, int[])
	 */
	@NonNull
	public ColorStateList getTextColor() {
		return mTextColors;
	}

	/**
	 * Sets a style for text.
	 *
	 * @param style The desired text style. One of {@link Typeface#NORMAL}, {@link Typeface#ITALIC},
	 *              {@link Typeface#BOLD}, {@link Typeface#BOLD_ITALIC}.
	 * @return {@code True} if text style of this text appearance has changed, {@code false} otherwise.
	 * @see #getTextStyle()
	 */
	public boolean setTextStyle(@TextStyle int style) {
		if (mTextStyle != style) {
			this.mTextStyle = style;
			this.mInvalidateTypeface = true;
			return true;
		}
		return false;
	}

	/**
	 * Returns the style for text specified for this text appearance.
	 *
	 * @return This appearance's text size.
	 * @see #setTextStyle(int)
	 * @see #updatePaintTypeface(Paint)
	 */
	@TextStyle
	public int getTextStyle() {
		return mTextStyle;
	}

	/**
	 * Sets an index of typeface for text.
	 * <p>
	 * This index will be used to resolve proper instance of typeface whenever it is requested via
	 * {@link #getTypeface()} and some of typeface related attributes have changed.
	 *
	 * @param index The desired typeface index. One of {@link #SANS_SERIF}, {@link #SERIF}, {@link #MONOSPACE}.
	 * @return {@code True} if index of typeface of this text appearance has changed, {@code false}
	 * otherwise.
	 * @see #getTypefaceIndex()
	 * @see #setTypeface(Typeface)
	 * @see #setFontFamily(String)
	 */
	public boolean setTypefaceIndex(@TypefaceIndex int index) {
		if (mTypefaceIndex != index) {
			this.mTypefaceIndex = index;
			this.mInvalidateTypeface = true;
			return true;
		}
		return false;
	}

	/**
	 * Returns the index of typeface for text specified for this text appearance.
	 *
	 * @return This appearance's typeface index.
	 * @see #setTypefaceIndex(int)
	 * @see #updatePaintTypeface(Paint)
	 */
	@TypefaceIndex
	public int getTypefaceIndex() {
		return mTypefaceIndex;
	}

	/**
	 * Sets a typeface for text.
	 *
	 * @param typeface The desired typeface. May be {@code null} to clear the current one.
	 * @return {@code True} if typeface of this text appearance has changed, {@code false} otherwise.
	 * @see #getTypeface()
	 * @see #setTypefaceIndex(int)
	 * @see #setFontFamily(String)
	 */
	public boolean setTypeface(@Nullable Typeface typeface) {
		if (mTypeface != typeface) {
			this.mTypeface = typeface;
			this.mInvalidateTypeface = false;
			return true;
		}
		return false;
	}

	/**
	 * Returns the typeface for text specified for this text appearance.
	 * <p>
	 * <b>Note</b>, that whenever one of typeface related attributes (text style, typeface index,
	 * font family) changes and this method is invoked a new proper instance of typeface will be
	 * resolved based on current values of these attributes.
	 *
	 * @return This appearance's typeface.
	 * @see #updatePaintTypeface(Paint)
	 */
	@Nullable
	public Typeface getTypeface() {
		this.ensureTypeface();
		return mTypeface;
	}

	/**
	 * Ensures that the typeface is initialized (if possible).
	 */
	private void ensureTypeface() {
		if (mTypeface == null || mInvalidateTypeface) {
			this.mInvalidateTypeface = false;
			if (!TextUtils.isEmpty(mFontFamily)) {
				this.mTypeface = Typeface.create(mFontFamily, mTextStyle);
			} else {
				switch (mTypefaceIndex) {
					case SANS_SERIF:
						this.mTypeface = Typeface.SANS_SERIF;
						break;
					case SERIF:
						this.mTypeface = Typeface.SERIF;
						break;
					case MONOSPACE:
						this.mTypeface = Typeface.MONOSPACE;
						break;
				}
			}
		}
	}

	/**
	 * Sets a font family for text's typeface.
	 * <p>
	 * Font family will be used to resolve proper instance of typeface whenever it is requested via
	 * {@link #getTypeface()} and some of typeface related attributes have changed.
	 *
	 * @param fontFamily The desired font family. May be {@code null} to clear the current one.
	 * @return {@code True} if font family of this text appearance has changed, {@code false} otherwise.
	 * @see #getFontFamily()
	 */
	public boolean setFontFamily(@Nullable String fontFamily) {
		if (mFontFamily == null || !mFontFamily.equals(fontFamily)) {
			this.mFontFamily = fontFamily;
			this.mTypeface = null;
			return true;
		}
		return false;
	}

	/**
	 * Checks whether this text appearance has its font family specified or not.
	 *
	 * @return {@code True} if font family is specified, {@code false} otherwise.
	 * @see #setFontFamily(String)
	 * @see #getFontFamily()
	 */
	public boolean hasFontFamily() {
		return !TextUtils.isEmpty(mFontFamily);
	}

	/**
	 * Returns the font family for text's typeface specified for this text appearance.
	 *
	 * @return This appearance's font family.
	 * @see #hasFontFamily()
	 * @see #setFontFamily(String)
	 * @see #updatePaintTypeface(Paint)
	 */
	@Nullable
	public String getFontFamily() {
		return mFontFamily;
	}

	/**
	 * Updates all text related settings for the given <var>paint</var> with current configuration
	 * of this text appearance.
	 *
	 * @param paint    The paint to be updated.
	 * @param stateSet Set of states used to resolve actual value of stateful text setting like its
	 *                 color.
	 * @return {@code True} if at least one setting of the paint has changed, {@code false} otherwise.
	 * @see #updatePaintTextSize(Paint)
	 * @see #updatePaintColor(Paint, int[])
	 * @see #updatePaintTypeface(Paint)
	 */
	public boolean updatePaint(@NonNull Paint paint, @Nullable int[] stateSet) {
		boolean updated = updatePaintTextSize(paint);
		updated |= updatePaintColor(paint, stateSet);
		updated |= updatePaintTypeface(paint);
		return updated;
	}

	/**
	 * Updates text size setting for the given <var>paint</var> to the one specified for this
	 * text appearance.
	 *
	 * @param paint The paint to be updated.
	 * @return {@code True} if paint's text setting has changed, {@code false} otherwise.
	 * @see #setTextSize(float)
	 * @see #updatePaintTextSize(Paint, float)
	 */
	public boolean updatePaintTextSize(@NonNull Paint paint) {
		return updatePaintTextSize(paint, mTextSize);
	}

	/**
	 * Updates text size setting for the given <var>paint</var> to the specified one.
	 *
	 * @param paint    The paint to be updated.
	 * @param textSize The desired text size in floating pixels.
	 * @return {@code True} if paint's text setting has changed, {@code false} otherwise.
	 * @see Paint#setTextSize(float)
	 */
	public static boolean updatePaintTextSize(@NonNull Paint paint, float textSize) {
		if (paint.getTextSize() != textSize) {
			paint.setTextSize(textSize);
			return true;
		}
		return false;
	}

	/**
	 * Updates color setting for the given <var>paint</var> to the one specified for this text
	 * appearance.
	 *
	 * @param paint    The paint to be updated.
	 * @param stateSet Set of states used to resolve actual color from the stateful text colors
	 *                 specified for this text appearance (if any).
	 * @return {@code True} if paint's color setting has changed, {@code false} otherwise.
	 * @see #setTextColor(ColorStateList)
	 * @see #updatePaintColor(Paint, int[], ColorStateList)
	 */
	public boolean updatePaintColor(@NonNull Paint paint, @Nullable int[] stateSet) {
		return updatePaintColor(paint, stateSet, mTextColors);
	}

	/**
	 * Updates color setting for the given <var>paint</var> to the actual one picked from the given
	 * <var>colors</var> according to the specified <var>stateSet</var>.
	 *
	 * @param paint    The paint to be updated.
	 * @param stateSet Set of states used pick the actual color to set to the paint.
	 * @param colors   Stateful colors list to the actual color from.
	 * @return {@code True} if paint's color setting has changed, {@code false} otherwise.
	 * @see #updatePaintColor(Paint, int)
	 */
	@SuppressWarnings("ResourceType")
	public static boolean updatePaintColor(@NonNull Paint paint, @Nullable int[] stateSet, @Nullable ColorStateList colors) {
		return colors != null && updatePaintColor(paint, colors.isStateful() ?
				colors.getColorForState(stateSet, colors.getDefaultColor()) :
				colors.getDefaultColor()
		);
	}

	/**
	 * Updates color setting for the given <var>paint</var> the the specified one.
	 *
	 * @param paint The paint to be updated.
	 * @param color The desired color.
	 * @return {@code True} if paint's color setting has changed, {@code false} otherwise.
	 * @see Paint#setColor(int)
	 */
	public static boolean updatePaintColor(@NonNull Paint paint, @ColorInt int color) {
		if (paint.getColor() != color) {
			paint.setColor(color);
			return true;
		}
		return false;
	}

	/**
	 * Updates typeface setting for the given <var>paint</var> to the one specified for this text
	 * appearance.
	 *
	 * @param paint The paint to be updated.
	 * @return {@code True} if paint's typeface setting has changed, {@code false} otherwise.
	 * @see #setTypeface(Typeface)
	 * @see #updatePaintTypeface(Paint, Typeface, int)
	 */
	public boolean updatePaintTypeface(@NonNull Paint paint) {
		this.ensureTypeface();
		return updatePaintTypeface(paint, mTypeface, mTextStyle);
	}

	/**
	 * Updates typeface setting for the given <var>paint</var> to the specified one.
	 *
	 * @param paint    The paint to be updated.
	 * @param typeface The desired typeface. May be {@code null} to resolve instance of typeface
	 *                 from the specified style.
	 * @param style    The desired text style used to resolve proper instance of typeface.
	 * @return {@code True} if paint's typeface setting has changed, {@code false} otherwise.
	 * @see Paint#setTypeface(Typeface)
	 */
	public static boolean updatePaintTypeface(@NonNull Paint paint, @Nullable Typeface typeface, @TextStyle int style) {
		if (style > 0) {
			if (typeface == null) {
				typeface = Typeface.defaultFromStyle(style);
			} else {
				typeface = Typeface.create(typeface, style);
			}
			final int typefaceStyle = typeface != null ? typeface.getStyle() : 0;
			final int need = style & ~typefaceStyle;
			paint.setFakeBoldText((need & Typeface.BOLD) != 0);
			paint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
			paint.setTypeface(typeface);
		} else {
			paint.setFakeBoldText(false);
			paint.setTextSkewX(0);
			paint.setTypeface(typeface);
		}
		return true;
	}

	/**
	 * Updates typeface setting for the given <var>paint</var> to the specified one.
	 *
	 * @param paint    The paint to be updated.
	 * @param typeface The desired typeface.
	 * @return {@code True} if paint's typeface setting has changed, {@code false} otherwise.
	 * @see Paint#setTypeface(Typeface)
	 */
	public static boolean updatePaintTypeface(@NonNull Paint paint, @Nullable Typeface typeface) {
		if (paint.getTypeface() != typeface) {
			paint.setTypeface(typeface);
			return true;
		}
		return false;
	}

	/**
	 * Resets the current text appearance values.
	 */
	private void reset() {
		this.mTextSize = -1;
		this.mTextColors = null;
		this.mTextStyle = -1;
		this.mTypefaceIndex = -1;
		this.mFontFamily = null;
		this.mTypeface = null;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
