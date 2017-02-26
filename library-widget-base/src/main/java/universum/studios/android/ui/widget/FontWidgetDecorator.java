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

import universum.studios.android.font.Font;
import universum.studios.android.font.FontWidget;
import universum.studios.android.font.util.FontApplier;

/**
 * A {@link WidgetDecorator} implementation that is used to decorate widgets of which text appearance
 * can be changed by supplying a custom font.
 *
 * @param <W> Type of the font widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class FontWidgetDecorator<W extends View & FontWidget> extends WidgetDecorator<W> implements FontWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "FontWidgetDecorator";

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
	 * Creates a new instance of FontDecorator for the given <var>widget</var>.
	 *
	 * @param widget         The widget to which can be supplied custom font.
	 * @param styleableAttrs Set of styleable attributes specific for the widget.
	 * @see WidgetDecorator#WidgetDecorator(View, int[])
	 */
	FontWidgetDecorator(W widget, int[] styleableAttrs) {
		super(widget, styleableAttrs);
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
			// Try to apply font presented within text appearance style.
			final TypedArray appearanceAttributes = theme.obtainStyledAttributes(attrs, new int[]{android.R.attr.textAppearance}, defStyleAttr, defStyleRes);
			final int appearance = appearanceAttributes.getResourceId(0, -1);
			if (appearance != -1) {
				FontApplier.applyFont(mWidget, appearance);
			}
			appearanceAttributes.recycle();
			// Try to apply font presented within xml attributes.
			FontApplier.applyFont(mWidget, attrs, defStyleAttr, defStyleRes);
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
		FontApplier.applyFont(mWidget, fontPath);
	}

	/**
	 */
	@Override
	public void setFont(@Nullable Font font) {
		FontApplier.applyFont(mWidget, font);
	}

	/**
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface) {
		// Ignored.
	}

	/**
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		// Ignored.
	}

	/**
	 * Sets a font specified by a relative path within a style with the specified <var>resId</var>.
	 *
	 * @param resId Resource id of the text appearance style containing a relative path to the
	 *              .ttf file from which should be created custom font and applied to the attached
	 *              font widget as typeface.
	 */
	void setFontFromStyle(@StyleRes int resId) {
		FontApplier.applyFont(mWidget, resId);
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
