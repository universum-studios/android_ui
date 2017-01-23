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
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;

/**
 * A {@link WidgetDecorator} implementation to support additional features for {@link ViewGroup}
 * based widgets, like pulling, refreshing and other features.
 *
 * @author Martin Albedinsky
 */
abstract class WidgetGroupDecorator<G extends ViewGroup> extends WidgetDecorator<G> implements WidgetGroup {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "WidgetGroupDecorator";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #WidgetGroupDecorator(ViewGroup, int[])} with default {@link R.styleable#Ui_ViewGroup}
	 * as styleable attributes set.
	 */
	WidgetGroupDecorator(G widgetGroup) {
		super(widgetGroup, R.styleable.Ui_ViewGroup);
	}

	/**
	 * Creates a new instance of WidgetGroupDecorator for the given <var>widgetGroup</var>.
	 *
	 * @param widgetGroup    The widget group for which to create new decorator.
	 * @param styleableAttrs Set of styleable attributes specific for the widget group.
	 * @see WidgetDecorator#WidgetDecorator(View, int[])
	 */
	WidgetGroupDecorator(G widgetGroup, int[] styleableAttrs) {
		super(widgetGroup, styleableAttrs);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	void onProcessTypedValues(Context context, TypedArray typedArray) {
		super.onProcessTypedValues(context, typedArray);
		if (typedArray.hasValue(R.styleable.Ui_ViewGroup_uiAllowDefaultSelection)) {
			setAllowDefaultSelection(typedArray.getBoolean(R.styleable.Ui_ViewGroup_uiAllowDefaultSelection, true));
		}
		if (typedArray.hasValue(R.styleable.Ui_ViewGroup_uiHideSoftKeyboardOnTouch)) {
			setHideSoftKeyboardOnTouchEnabled(typedArray.getBoolean(R.styleable.Ui_ViewGroup_uiHideSoftKeyboardOnTouch, false));
		}
	}

	/**
	 */
	@Override
	@SuppressWarnings("ResourceType")
	void onProcessTintValues(Context context, TypedArray tintArray, int tintColor) {
		if (UiConfig.MATERIALIZED) {
			if (tintArray.hasValue(R.styleable.Ui_ViewGroup_uiBackgroundTint)) {
				setBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_ViewGroup_uiBackgroundTint));
			}
			if (tintArray.hasValue(R.styleable.Ui_ViewGroup_uiBackgroundTintMode)) {
				setBackgroundTintMode(TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_ViewGroup_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				));
			}
		} else {
			if (tintArray.hasValue(R.styleable.Ui_ViewGroup_uiBackgroundTint)) {
				mTintInfo.backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_ViewGroup_uiBackgroundTint);
			}
			mTintInfo.backgroundTintMode = TintManager.parseTintMode(
					tintArray.getInt(R.styleable.Ui_ViewGroup_uiBackgroundTintMode, 0),
					mTintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
			);
		}
	}

	/**
	 */
	@Override
	public void setHideSoftKeyboardOnTouchEnabled(boolean enabled) {
		this.updatePrivateFlags(PrivateFlags.PFLAG_HIDE_SOFT_KEYBOARD_ON_TOUCH, enabled);
	}

	/**
	 */
	@Override
	public boolean isHideSoftKeyboardOnTouchEnabled() {
		return hasPrivateFlag(PrivateFlags.PFLAG_HIDE_SOFT_KEYBOARD_ON_TOUCH);
	}

	/**
	 * Requests hiding of the soft keyboard for the attached group widget due to touch event if such
	 * feature is enabled.
	 *
	 * @see #isHideSoftKeyboardOnTouchEnabled()
	 * @see #setHideSoftKeyboardOnTouchEnabled(boolean)
	 */
	void hideSoftKeyboardOnTouch() {
		if (hasPrivateFlag(PrivateFlags.PFLAG_HIDE_SOFT_KEYBOARD_ON_TOUCH)) {
			WidgetUtils.hideSoftKeyboard(mWidget);
		}
	}

	/**
	 * This should be called from the attached widget group whenever its
	 * {@link ViewGroup#onInterceptTouchEvent(MotionEvent)} is invoked.
	 */
	boolean onInterceptTouchEvent(MotionEvent event) {
		return false;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
