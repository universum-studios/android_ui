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
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

/**
 * Helper used to simplify setting of custom font to all {@link FontWidget} implementations.
 *
 * @author Martin Albedinsky
 */
public final class FontApplier {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "FontApplier";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * An instance of font widget to which apply custom font typefaces.
	 */
	final FontWidget mFontWidget;

	/**
	 * Current valid context obtained from the font widget.
	 */
	private final Context mContext;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of FontApplier for the given <var>fontWidget</var>.
	 *
	 * @param fontWidget An instance of widget to which will be applied all font typefaces requested
	 *                   trough this font applier.
	 */
	public FontApplier(@NonNull FontWidget fontWidget) {
		this.mFontWidget = fontWidget;
		this.mContext = fontWidget.getContext();
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Same as {@link #applyFont(android.util.AttributeSet, int)} with {@code 0} as attribute for
	 * <var>defStyle</var>.
	 */
	public boolean applyFont(@NonNull AttributeSet attrs) {
		return applyFont(attrs, 0);
	}

	/**
	 * Creates and applies font from the passed <var>attrs</var> to the current font widget.
	 * See {@link Font#create(android.content.Context, android.util.AttributeSet, int)} for more info.
	 *
	 * @return {@code True} if font was applied, {@code false} otherwise.
	 */
	public boolean applyFont(@NonNull AttributeSet attrs, @AttrRes int defStyle) {
		return applyFont(Font.create(mContext, attrs, defStyle));
	}

	/**
	 * Creates and applies font from the passed <var>style</var> to the current font widget.
	 * See {@link Font#create(android.content.Context, int)} for more info.
	 *
	 * @return {@code True} if font was applied, {@code false} otherwise.
	 */
	public boolean applyFont(@StyleRes int style) {
		return applyFont(Font.create(mContext, style));
	}

	/**
	 * Created and applies font from the passed <var>fontPath</var> to the current font widget.
	 * See {@link Font#create(String)} for more info.
	 *
	 * @return {@code True} if font was applied, {@code false} otherwise.
	 */
	public boolean applyFont(@NonNull String fontPath) {
		return applyFont(Font.create(fontPath));
	}

	/**
	 * Applies the given <var>font</var> to the current font widget.
	 *
	 * @param font An instance of valid font to apply.
	 * @return {@code True} if font was applied, {@code false} otherwise.
	 */
	public boolean applyFont(@Nullable Font font) {
		return applyFontInternal(font);
	}

	/**
	 * Returns the font widget of this font applier.
	 *
	 * @return An instance of the font widget for which was this font applier instance created.
	 */
	@NonNull
	public FontWidget getFontWidget() {
		return mFontWidget;
	}

	/**
	 * Applies the given font's type face to the current font view.
	 *
	 * @param font Font which type face to apply.
	 * @return {@code True} if font was applied, {@code false} otherwise.
	 */
	private boolean applyFontInternal(Font font) {
		if (font != null) {
			mFontWidget.setTypeface(font.obtainTypeface(mContext));
			return true;
		}
		return false;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
