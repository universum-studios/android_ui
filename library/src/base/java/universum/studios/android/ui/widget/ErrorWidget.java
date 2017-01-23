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

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import universum.studios.android.ui.R;

/**
 * Interface for widget to which an error can be specified.
 *
 * @author Martin Albedinsky
 */
public interface ErrorWidget {

	/**
	 * Sets an error for this widget. To clear the specified error, call {@link #clearError()}.
	 * <p>
	 * <b>Note</b>, that presentation of the given error to a user depends on a specific implementation
	 * of this error widget. Some implementations may not event display this error, but each widget
	 * should at least create its {@link Drawable} state in {@link View#onCreateDrawableState(int)}
	 * with {@link R.attr#ui_state_error ui_state_error} as extra state.
	 *
	 * @param error The desired error to be presented by this widget to the user.
	 * @see #getError()
	 * @see #hasError()
	 * @see #clearError()
	 */
	void setError(@NonNull CharSequence error);

	/**
	 * Checks whether this widget has error specified or not.
	 *
	 * @return {@code True} if error is specified, {@code false} otherwise.
	 * @see #setError(CharSequence)
	 * @see #getError()
	 */
	boolean hasError();

	/**
	 * Returns the error specified for this widget (if any).
	 *
	 * @return This widget's error or {@code null} if there was no error specified yet or it has been
	 * cleared.
	 * @see #setError(CharSequence)
	 * @see #hasError()
	 */
	@Nullable
	CharSequence getError();

	/**
	 * Clears the current error of this widget (if any).
	 */
	void clearError();
}
