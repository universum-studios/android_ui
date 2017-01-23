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

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.StateSet;

import universum.studios.android.ui.R;

/**
 * This class holds common state sets for widgets.
 *
 * @author Martin Albedinsky
 */
@SuppressWarnings("unused")
public final class WidgetStateSet {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "WidgetStateSets";

	/**
	 * State set without any states.
	 */
	public static final int[] EMPTY = StateSet.WILD_CARD;

	/**
	 * State set containing single {@link R.attr#ui_state_input ui_state_input} state.
	 */
	public static final int[] INPUT = {R.attr.ui_state_input};

	/**
	 * State set containing single {@link R.attr#ui_state_error ui_state_error} state.
	 */
	public static final int[] ERROR = {R.attr.ui_state_error};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * </ul>
	 */
	public static final int[] ENABLED = {
			android.R.attr.state_enabled
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link R.attr#ui_state_error ui_state_error}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_ERROR = {
			android.R.attr.state_enabled,
			R.attr.ui_state_error
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_pressed android:state_pressed}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_PRESSED = {
			android.R.attr.state_enabled,
			android.R.attr.state_pressed
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_focused android:state_focused}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_FOCUSED = {
			android.R.attr.state_enabled,
			android.R.attr.state_focused
	};
	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_activated android:state_activated}</li>
	 * </ul>
	 * <b>Use this set only for Android versions above {@link android.os.Build.VERSION_CODES#HONEYCOMB HONEYCOMB}
	 * including.</b>
	 */
	@SuppressLint("NewApi")
	public static final int[] ENABLED_ACTIVATED = {
			android.R.attr.state_enabled,
			android.R.attr.state_activated
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_selected android:state_selected}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_SELECTED = {
			android.R.attr.state_enabled,
			android.R.attr.state_selected
	};


	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_checked android:state_checked}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_CHECKED = {
			android.R.attr.state_enabled,
			android.R.attr.state_checked
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>[-]{@link android.R.attr#state_checked android:state_checked}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_UNCHECKED = {
			android.R.attr.state_enabled,
			-android.R.attr.state_checked
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_selected android:state_selected}</li>
	 * <li>{@link android.R.attr#state_pressed android:state_pressed}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_SELECTED_PRESSED = {
			android.R.attr.state_enabled,
			android.R.attr.state_selected,
			android.R.attr.state_pressed
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_checked android:state_checked}</li>
	 * <li>{@link android.R.attr#state_pressed android:state_pressed}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_CHECKED_PRESSED = {
			android.R.attr.state_enabled,
			android.R.attr.state_checked,
			android.R.attr.state_pressed
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_checked android:state_checked}</li>
	 * <li>{@link android.R.attr#state_focused android:state_focused}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_CHECKED_FOCUSED = {
			android.R.attr.state_enabled,
			android.R.attr.state_checked,
			android.R.attr.state_focused
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>[-]{@link android.R.attr#state_checked android:state_checked}</li>
	 * <li>{@link android.R.attr#state_pressed android:state_pressed}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_UNCHECKED_PRESSED = {
			android.R.attr.state_enabled,
			-android.R.attr.state_checked,
			android.R.attr.state_pressed
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>[-]{@link android.R.attr#state_checked android:state_checked}</li>
	 * <li>{@link android.R.attr#state_focused android:state_focused}</li>
	 * </ul>
	 */
	public static final int[] ENABLED_UNCHECKED_FOCUSED = {
			android.R.attr.state_enabled,
			-android.R.attr.state_checked,
			android.R.attr.state_focused
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>[-]{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * </ul>
	 */
	public static final int[] DISABLED = {
			-android.R.attr.state_enabled
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>[-]{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_selected android:state_selected}</li>
	 * </ul>
	 */
	public static final int[] DISABLED_SELECTED = {
			-android.R.attr.state_enabled,
			android.R.attr.state_selected
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>[-]{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_checked android:state_checked}</li>
	 * </ul>
	 */
	public static final int[] DISABLED_CHECKED = {
			-android.R.attr.state_enabled,
			android.R.attr.state_checked
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>[-]{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>[-]{@link android.R.attr#state_checked android:state_checked}</li>
	 * </ul>
	 */
	public static final int[] DISABLED_UNCHECKED = {
			-android.R.attr.state_enabled,
			-android.R.attr.state_checked
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>[-]{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_selected android:state_selected}</li>
	 * <li>{@link android.R.attr#state_pressed android:state_pressed}</li>
	 * </ul>
	 */
	public static final int[] DISABLED_SELECTED_PRESSED = {
			-android.R.attr.state_enabled,
			android.R.attr.state_selected,
			android.R.attr.state_pressed
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>[-]{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>{@link android.R.attr#state_checked android:state_checked}</li>
	 * <li>{@link android.R.attr#state_focused android:state_focused}</li>
	 * </ul>
	 */
	public static final int[] DISABLED_CHECKED_FOCUSED = {
			-android.R.attr.state_enabled,
			android.R.attr.state_checked,
			android.R.attr.state_focused
	};

	/**
	 * State set containing states:
	 * <ul>
	 * <li>[-]{@link android.R.attr#state_enabled android:state_enabled}</li>
	 * <li>[-]{@link android.R.attr#state_checked android:state_checked}</li>
	 * <li>{@link android.R.attr#state_focused android:state_focused}</li>
	 * </ul>
	 */
	public static final int[] DISABLED_UNCHECKED_FOCUSED = {
			-android.R.attr.state_enabled,
			-android.R.attr.state_checked,
			android.R.attr.state_focused
	};

	/**
	 * Constructors ================================================================================
	 */

	/**
	 */
	private WidgetStateSet() {
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Merges the given <var>baseState</var> with <var>additionalState</var>.
	 *
	 * @param baseState       The base state to marge with the <var>additionalState</var>.
	 * @param additionalState The additional state to merge into the <var>baseState</var>.
	 * @return Merged state arrays.
	 */
	@NonNull
	public static int[] mergeStates(@NonNull int[] baseState, @NonNull int[] additionalState) {
		final int n = baseState.length;
		int i = n - 1;
		while (i >= 0 && baseState[i] == 0) {
			i--;
		}
		System.arraycopy(additionalState, 0, baseState, i + 1, additionalState.length);
		return baseState;
	}

	/**
	 * Expands size of the given <var>states</var> to the specified <var>newSize</var>.
	 *
	 * @param states  The states to be expanded.
	 * @param newSize The new size of the expanded state set.
	 * @return New expanded state sets in the requested size or the same states if the new size is
	 * the same as the size of the given states.
	 * @throws IllegalArgumentException If this method is used instead to trim the state set.
	 */
	@NonNull
	public static int[] expandStates(@NonNull int[] states, int newSize) {
		if (states.length > newSize) {
			throw new IllegalArgumentException("Did you want to expand or trim state set?");
		}
		if (states.length == newSize) {
			return states;
		}
		int[] expandedStates = new int[newSize];
		System.arraycopy(states, 0, expandedStates, 0, states.length);
		return expandedStates;
	}
}
