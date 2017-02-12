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
 * Interface for <b>scrollable</b> views.
 *
 * @author Martin Albedinsky
 */
public interface Scrollable {

	/**
	 * Returns the orientation of this scrollable view to determine how, along which axis, can be this
	 * view (its content) scrolled.
	 *
	 * @return Orientation of this view. One of {@link Orientation#VERTICAL} or {@link Orientation#HORIZONTAL}.
	 */
	int getOrientation();

	/**
	 * Returns boolean flag indicating whether this scrollable view (its content) is scrolled at the
	 * <b>start</b> or not.
	 *
	 * @return {@code True} if content of this view is scrolled at the start of this view, {@code false}
	 * otherwise.
	 */
	boolean isScrolledAtStart();

	/**
	 * Returns boolean flag indicating whether this scrollable view (its content) is scrolled at the
	 * <b>end</b> or not.
	 *
	 * @return {@code True} if content of this view is scrolled at the end of this view, {@code false}
	 * otherwise.
	 */
	boolean isScrolledAtEnd();
}
