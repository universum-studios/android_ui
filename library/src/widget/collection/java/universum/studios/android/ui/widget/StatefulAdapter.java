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

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Interface for adapters which specifies API to support saving and restoring of the adapter's current
 * state.
 *
 * @author Martin Albedinsky
 */
public interface StatefulAdapter {

	/**
	 * Called to save the current state of this adapter.
	 *
	 * @return Saved state of this adapter or an <b>empty</b> state if this adapter does not need to
	 * save its state.
	 */
	@NonNull
	Parcelable saveInstanceState();

	/**
	 * Called to restore a previous state, saved by {@link #saveInstanceState()}, of this adapter.
	 *
	 * @param savedState Should be the same state as obtained via {@link #saveInstanceState()} before.
	 */
	void restoreInstanceState(@NonNull Parcelable savedState);
}
