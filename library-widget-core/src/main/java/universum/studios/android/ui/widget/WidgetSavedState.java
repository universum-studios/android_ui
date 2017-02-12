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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import universum.studios.android.ui.UiConfig;

/**
 * Saved state implementation like {@link View.BaseSavedState} that should be used by all custom
 * widgets from the UI library and also for theirs derivatives.
 *
 * @author Martin Albedinsky
 */
public abstract class WidgetSavedState implements Parcelable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "WidgetSavedState";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Empty widget state that should be used as return value for {@link View#onSaveInstanceState()}
	 * whenever that specific view does not need to save its state.
	 */
	public static final WidgetSavedState EMPTY_STATE = new WidgetSavedState() {};

	/**
	 * Creator used to create an instance or array of instances of WidgetSavedState from {@link android.os.Parcel}.
	 */
	public static final Creator<WidgetSavedState> CREATOR = new Creator<WidgetSavedState>() {

		/**
		 */
		@Override
		public WidgetSavedState createFromParcel(@NonNull Parcel source) {
			final Parcelable superState = source.readParcelable(UiConfig.class.getClassLoader());
			if (superState != null) {
				throw new IllegalStateException("superState must be null");
			}
			return EMPTY_STATE;
		}

		/**
		 */
		@Override
		public WidgetSavedState[] newArray(int size) {
			return new WidgetSavedState[size];
		}
	};

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Super state supplied during initialization of this saved state or its restoring.
	 */
	private final Parcelable mSuperState;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Constructor only for internal use.
	 */
	private WidgetSavedState() {
		this.mSuperState = null;
	}

	/**
	 * Should be called by a derived view classes when creating theirs SavedState objects to allow
	 * chaining of those states in {@link View#onSaveInstanceState()}.
	 *
	 * @param superState The state of the superclass of this widget.
	 */
	protected WidgetSavedState(@NonNull Parcelable superState) {
		this.mSuperState = superState != EMPTY_STATE ? superState : null;
	}

	/**
	 * Should be called from a {@link #CREATOR} of a derived class to create an instance of
	 * WidgetSavedState with super state from the given parcel <var>source</var>.
	 *
	 * @param source Source parcel for the new instance.
	 */
	protected WidgetSavedState(@NonNull Parcel source) {
		final Parcelable superState = source.readParcelable(UiConfig.class.getClassLoader());
		this.mSuperState = superState != null ? superState : EMPTY_STATE;
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mSuperState, flags);
	}

	/**
	 * Returns the super state of this saved state.
	 *
	 * @return The state supplied to {@link #WidgetSavedState(Parcelable)} or restored in
	 * {@link #WidgetSavedState(Parcel)}.
	 */
	@NonNull
	final public Parcelable getSuperState() {
		return mSuperState != null ? mSuperState : EMPTY_STATE;
	}

	/**
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
