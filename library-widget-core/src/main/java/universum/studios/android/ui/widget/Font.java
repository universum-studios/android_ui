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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple wrapper for {@link android.graphics.Typeface} used by {@link FontApplier} to simplify using
 * of custom fonts within an Android application.
 * <p>
 * <b>Note</b>, that all fonts used by this API must be placed within an assets folder inside {@link #FONT_FOLDER}.
 *
 * @author Martin Albedinsky
 */
public final class Font {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "Font";

	/**
	 * Suffix for original <b>true Typeface</b> fonts.
	 */
	public static final String TTF_SUFFIX = ".ttf";

	/**
	 * Sub-folder within an application assets folder, where must be placed all custom fonts.
	 * Can also contain custom sub-folders to group fonts.
	 * <p>
	 * Constant value: <b>font/</b>
	 */
	public static final String FONT_FOLDER = "font" + File.separator;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Matcher for <b>.ttf</b> file suffix.
	 */
	private static final Matcher TTF_SUFFIX_MATCHER = Pattern.compile("(.*)\\.ttf").matcher("");

	/**
	 * Cache of used fonts.
	 */
	private static final HashMap<String, Font> CACHE = new HashMap<>();

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Typeface for this font.
	 */
	private Typeface mTypeFace;

	/**
	 * Path to the .ttf file within an application assets folder, which should be represented by this
	 * font.
	 */
	private final String mFontPath;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of Font with the specified path to font file within assets folder.
	 * <b>Note, that empty path or {@code null} are not valid attributes.</b>
	 * <p>
	 * <b>Only .ttf font is allowed!</b>
	 *
	 * @param fontPath Relative path to the .ttf file (with or without .ttf suffix) placed within
	 *                 an application assets folder (within {@link #FONT_FOLDER} folder) which will
	 *                 be represented by this newly created Font instance.
	 * @throws IllegalArgumentException If the given <var>fontPath</var> is empty.
	 */
	public Font(@NonNull String fontPath) {
		if (TextUtils.isEmpty(fontPath)) {
			throw new IllegalArgumentException("Font path cannot be empty!");
		}
		if (!TTF_SUFFIX_MATCHER.reset(fontPath).matches()) {
			fontPath = FONT_FOLDER + fontPath + TTF_SUFFIX;
		} else {
			fontPath = FONT_FOLDER + fontPath;
		}
		this.mFontPath = fontPath;
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Creates a new instance of Font with the given font path and text style.
	 * <p>
	 * See {@link #Font(String)} for more info about attributes for Font.
	 *
	 * @return New or cached instance of Font for the given path and text style.
	 * @see #create(android.content.Context, android.util.AttributeSet, int)
	 * @see #create(android.content.Context, int)
	 * @see #create(String)
	 */
	public static Font create(@NonNull String fontPath) {
		final Font font = new Font(fontPath);
		if (CACHE.containsKey(font.mFontPath)) {
			if (UiConfig.DEBUG_LOG_ENABLED) {
				Log.v(TAG, "Re-using cached font for path(" + font.mFontPath + ").");
			}
			return CACHE.get(font.mFontPath);
		}
		if (UiConfig.DEBUG_LOG_ENABLED) {
			Log.v(TAG, "Caching new font for path(" + font.mFontPath + ").");
		}
		CACHE.put(font.mFontPath, font);
		return font;
	}

	/**
	 * Creates a new instance of Font with a font path obtained from the given <var>attrs</var>.
	 * <p>
	 * See {@link #Font(String)} for more info about attributes for Font.
	 *
	 * @param context      Valid context used to process the given attributes set.
	 * @param attrs        Attributes set which should contain {@link R.attr#uiFontPath uiFontPath}
	 *                     attribute parsed from xml file.
	 * @param defStyleAttr An attribute of the default style presented within the current theme which
	 *                     supplies default attributes for {@link android.content.res.TypedArray}.
	 * @return New or cached instance of Font or {@code null} if a theme of the given <var>context</var>
	 * is invalid.
	 * @see #create(String)
	 * @see #create(android.content.Context, int)
	 */
	@Nullable
	public static Font create(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
		final Resources.Theme theme = context.getTheme();
		return theme != null ? create(
				theme.obtainStyledAttributes(attrs, new int[]{R.attr.uiFontPath}, defStyleAttr, 0)
		) : null;
	}

	/**
	 * Creates a new instance of Font with the font path obtained from the given <var>style</var>.
	 * <p>
	 * See {@link #Font(String)} for more info about attributes for Font.
	 *
	 * @param context Valid context used to process the given style.
	 * @param style   A resource id of the style which should contain {@link R.attr#uiFontPath uiFontPath}
	 *                attribute.
	 * @return New or cached instance of Font or {@code null} if a theme of the given <var>context</var>
	 * is invalid.
	 * @see #create(String)
	 * @see #create(android.content.Context, android.util.AttributeSet, int)
	 */
	@Nullable
	public static Font create(@NonNull Context context, @StyleRes int style) {
		final Resources.Theme theme = context.getTheme();
		return theme != null ? create(
				theme.obtainStyledAttributes(style, new int[]{R.attr.uiFontPath})
		) : null;
	}

	/**
	 * Creates a new instance of Font with the font path obtained from the {@code 0} index of the
	 * passed <var>typedArray</var>.
	 *
	 * @param typedArray An instance of typed array to obtain font path from.
	 * @return New or cached instance of Font.
	 */
	private static Font create(TypedArray typedArray) {
		if (typedArray != null) {
			final String fontPath = typedArray.getString(0);
			typedArray.recycle();
			return !TextUtils.isEmpty(fontPath) ? Font.create(fontPath) : null;
		}
		return null;
	}

	/**
	 * Returns an instance of the Typeface which is created for the .ttf file placed within an application
	 * assets folder under the current font path.
	 * <p>
	 * See {@link android.graphics.Typeface#createFromAsset(android.content.res.AssetManager, String)} for more info.
	 *
	 * @param context Valid context used to create requested TypeFace.
	 * @return Instance of the requested Typeface.
	 */
	@NonNull
	public Typeface obtainTypeface(@NonNull Context context) {
		return (mTypeFace == null) ? (mTypeFace = this.createTypeface(context)) : mTypeFace;
	}

	/**
	 * Returns the current font path of this Font instance.
	 *
	 * @return Full path to the .ttf file within an application assets folder.
	 * @see #obtainTypeface(android.content.Context)
	 */
	@NonNull
	public String getFontPath() {
		return mFontPath;
	}

	/**
	 * Creates a new instance of Typeface from the current font path. If creating of typeface form
	 * the current {@link #mFontPath} fails, the raw {@link #mFontPath} will be used to create
	 * type face.
	 *
	 * @param context Valid context used to create requested TypeFace.
	 * @return Typeface instance.
	 */
	private Typeface createTypeface(Context context) {
		// First try to create from the styled font path.
		try {
			return Typeface.createFromAsset(context.getAssets(), mFontPath);
		} catch (Exception e) {
			throw new RuntimeException("Type face with path('" + mFontPath + "') not found within assets folder or is not a valid '.ttf' file.", e);
		}
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
