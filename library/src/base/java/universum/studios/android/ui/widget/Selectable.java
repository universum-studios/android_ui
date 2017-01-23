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

/**
 * Interface for <b>selectable</b> views. Provides API for enabling/disabling default support of the
 * selection state handled by the Android framework and provides custom method ({@link #setSelectionState(boolean)})
 * for selection state handling.
 * <p>
 * This can be useful when you don't want to be your views selected/unselected within for example
 * collection like {@link android.widget.ListView} by default because you want to handle deletion
 * of data set items represented by these views. In such a case, the Android framework can be a little
 * bit "stubborn" and will un-select your selected views.
 *
 * @author Martin Albedinsky
 */
public interface Selectable {

	/**
	 * Sets a flag indicating whether this view should allow default selection by
	 * {@link android.view.View#setSelected(boolean)} or not.
	 *
	 * @param allow {@code True} to allow default selection, {@code false} otherwise.
	 * @see #allowsDefaultSelection()
	 * @see #setSelectionState(boolean)
	 */
	void setAllowDefaultSelection(boolean allow);

	/**
	 * Returns a flag indicating whether this view allows default selection by
	 * {@link android.view.View#setSelected(boolean)} or not.
	 *
	 * @return {@code True} if default selection is allowed, {@code false} otherwise.
	 * @see #setAllowDefaultSelection(boolean)
	 * @see #setSelectionState(boolean)
	 */
	boolean allowsDefaultSelection();

	/**
	 * Sets the selection state of this selectable view.
	 *
	 * @param selected {@code True} for selected, {@code false} for unselected state.
	 */
	void setSelectionState(boolean selected);
}
