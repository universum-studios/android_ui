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
 * A {@link WidgetSavedState} implementation used to ensure that the state of collection view is
 * properly saved.
 *
 * @author Martin Albedinsky
 */
public class CollectionSavedState extends WidgetSavedState {

	/**
	 * Creator used to create an instance or array of instances of SavedState from {@link android.os.Parcel}.
	 */
	public static final Creator<CollectionSavedState> CREATOR = new Creator<CollectionSavedState>() {
		/**
		 */
		@Override
		public CollectionSavedState createFromParcel(@NonNull Parcel source) {
			return new CollectionSavedState(source);
		}

		/**
		 */
		@Override
		public CollectionSavedState[] newArray(int size) {
			return new CollectionSavedState[size];
		}
	};

	/**
	 * Saved state of the associated adapter.
	 */
	protected Parcelable mAdapterState;

	/**
	 * Creates a new instance of SavedState with the given <var>superState</var> to allow chaining
	 * of saved states in {@link View#onSaveInstanceState()} and also in {@link View#onRestoreInstanceState(Parcelable)}.
	 *
	 * @param superState The super state obtained from {@code super.onSaveInstanceState()} within
	 *                   {@code onSaveInstanceState()}.
	 */
	protected CollectionSavedState(@NonNull Parcelable superState) {
		super(superState);
	}

	/**
	 * Called from {@link #CREATOR} to create an instance of SavedState form the given parcel
	 * <var>source</var>.
	 *
	 * @param source Parcel with data for the new instance.
	 */
	protected CollectionSavedState(Parcel source) {
		super(source);
		this.mAdapterState = source.readParcelable(UiConfig.class.getClassLoader());
	}

	/**
	 */
	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeParcelable(mAdapterState, flags);
	}
}
