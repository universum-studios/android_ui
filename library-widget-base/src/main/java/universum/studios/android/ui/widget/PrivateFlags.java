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
 * This class defines a group of private flags used by widgets within this package.
 *
 * @author Martin Albedinsky
 */
class PrivateFlags {

	/*
	 * NOTE THAT THESE FLAGS SHOULD BE SPECIFIED MAX TO (0x00000001 << 15) AS RELATIVE WIDGETS USE
	 * HEIR OWN PRIVATE FLAGS STARTING FROM (0x00000001 << 16) AND ARE USED ALONG WITH THESE GLOBAL
	 * PRIVATE FLAGS.
	 */

	/**
	 * Flag indicating whether a widget is attached to window or not.
	 */
	static final int PFLAG_ATTACHED_TO_WINDOW = 0x00000001;

	/**
	 * Flag indicating whether a widget allows default selection or not.
	 */
	static final int PFLAG_ALLOWS_DEFAULT_SELECTION = 0x00000001 << 1;

	/**
	 * Flag indicating whether a widget has pull effect enabled or not.
	 */
	static final int PFLAG_PULL_ENABLED = 0x00000001 << 2;

	/**
	 * Flag indicating whether a widget has refresh effect enabled or not.
	 */
	static final int PFLAG_REFRESH_ENABLED = 0x00000001 << 3;

	/**
	 * Flag indicating whether a widget has refresh gesture enabled or not.
	 */
	static final int PFLAG_REFRESH_GESTURE_ENABLED = 0x00000001 << 4;

	/**
	 * Flag indicating whether a refreshable view should draw its refresh indicator as part of its
	 * own drawing or not.
	 */
	static final int PFLAG_DRAW_REFRESH_INDICATOR = 0x00000001 << 5;

	/**
	 * Flag indicating whether a widget should request hiding of soft keyboard
	 * whenever it is touched or not.
	 */
	static final int PFLAG_HIDE_SOFT_KEYBOARD_ON_TOUCH = 0x00000001 << 6;
}
