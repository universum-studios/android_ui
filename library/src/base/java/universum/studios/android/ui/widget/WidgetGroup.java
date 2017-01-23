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
 * Interface for the widget groups presented within the UI library.
 *
 * @author Martin Albedinsky
 */
public interface WidgetGroup extends Widget {

    /**
     * Specifies a boolean flag indicating whether this view group should request hiding of the soft
     * keyboard whether it is touched.
     *
     * @param enabled {@code True} to hide soft keyboard automatically on touch, {@code false} otherwise.
     */
    void setHideSoftKeyboardOnTouchEnabled(boolean enabled);

    /**
     * Checks whether this view group hides the soft keyboard on its touch or not.
     *
     * @return {@code True} if this view group will hide soft keyboard on its touch automatically,
     * {@code false} otherwise.
     */
    boolean isHideSoftKeyboardOnTouchEnabled();
}
