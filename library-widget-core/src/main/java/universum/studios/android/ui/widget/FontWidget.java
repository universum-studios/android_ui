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
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import universum.studios.android.ui.R;

/**
 * Interface for a widget that want to use {@link FontApplier} to set up its custom font.
 *
 * @author Martin Albedinsky
 */
public interface FontWidget {

	/**
	 * Delegates to {@link FontApplier#applyFont(String)}.
	 *
	 * @see R.attr#uiFontPath uiFontPath
	 */
	void setFont(@NonNull String fontPath);

	/**
	 * Delegates to {@link FontApplier#applyFont(Font)}.
	 */
	void setFont(@Nullable Font font);

	/**
	 * Delegates to {@link android.view.View#getContext()}.
	 */
	@NonNull
	Context getContext();

	/**
	 * Sets a typeface and style in which the text of this view should be displayed, and
	 * turns on the fake bold and italic bits in the Paint if the Typeface that you provided does
	 * not have all the bits in the style that you specified.
	 *
	 * @param typeface The desired typeface. May be {@code null} to create default one from the specified
	 *                 <var>style</var>.
	 * @param style    One of {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link Typeface#ITALIC}
	 *                 or {@link Typeface#BOLD_ITALIC}.
	 * @see #setTypeface(Typeface)
	 */
	void setTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style);

	/**
	 * Sets a typeface in which the text of this view should be displayed.
	 * <p>
	 * <b>Note</b>, that not all Typeface families actually have bold and italic variants, so you
	 * may need to use {@link #setTypeface(Typeface, int)} to get the appearance that you actually
	 * want.
	 *
	 * @param typeface The desired typeface. May be {@code null} to use a default one.
	 */
	void setTypeface(@Nullable Typeface typeface);
}
