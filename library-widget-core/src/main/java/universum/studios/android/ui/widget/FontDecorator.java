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
import android.util.AttributeSet;
import android.view.View;

/**
 * A {@link WidgetDecorator} implementation that is used to decorate widgets of which text appearance
 * can be changed by supplying a custom font.
 *
 * @param <W> A type of the font widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class FontDecorator<W extends View & FontWidget> extends WidgetDecorator<W> implements FontWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "FontDecorator";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Helper used to apply custom font the the attached font widget.
	 */
	private final FontApplier mFontApplier;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of FontDecorator for the given <var>widget</var>.
	 *
	 * @param widget         The widget to which can be supplied custom font.
	 * @param styleableAttrs Set of styleable attributes specific for the widget.
	 * @see WidgetDecorator#WidgetDecorator(View, int[])
	 */
	FontDecorator(W widget, int[] styleableAttrs) {
		super(widget, styleableAttrs);
		this.mFontApplier = new FontApplier(widget);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	void processAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super.processAttributes(context, attrs, defStyleAttr, defStyleRes);
		if (!mWidget.isInEditMode()) {
			final Resources.Theme theme = context.getTheme();
			// Get font path presented within text appearance style.
			if (theme != null) {
				final TypedArray appearanceArray = theme.obtainStyledAttributes(attrs, new int[]{android.R.attr.textAppearance}, defStyleAttr, defStyleRes);
				if (appearanceArray != null) {
					final int appearance = appearanceArray.getResourceId(0, -1);
					if (appearance != -1) {
						mFontApplier.applyFont(appearance);
					}
					appearanceArray.recycle();
				}
			}
			// Get font path presented within xml attributes.
			mFontApplier.applyFont(attrs, defStyleAttr);
		}
	}

	/**
	 */
	@NonNull
	@Override
	public Context getContext() {
		return mWidget.getContext();
	}

	/**
	 */
	@Override
	public void setFont(@NonNull String fontPath) {
		mFontApplier.applyFont(fontPath);
	}

	/**
	 */
	@Override
	public void setFont(@Nullable Font font) {
		mFontApplier.applyFont(font);
	}

	/**
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		// Ignored.
	}

	/**
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface) {
		// Ignored.
	}

	/**
	 * Applies a font specified by a relative path within a style with the specified <var>resId</var>.
	 *
	 * @param resId Resource id of the text appearance style containing a relative path to the
	 *              .ttf file from which should be created custom font and applied to the attached
	 *              font widget as typeface.
	 */
	void applyTextAppearanceFont(@StyleRes int resId) {
		mFontApplier.applyFont(resId);
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
