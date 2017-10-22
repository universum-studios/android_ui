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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
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

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "WidgetGroupDecorator";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/*
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

	/*
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	void onProcessAttributes(Context context, TypedArray attributes) {
		super.onProcessAttributes(context, attributes);
		if (attributes.hasValue(R.styleable.Ui_ViewGroup_uiHideSoftKeyboardOnTouch)) {
			setHideSoftKeyboardOnTouchEnabled(attributes.getBoolean(R.styleable.Ui_ViewGroup_uiHideSoftKeyboardOnTouch, false));
		}
	}

	/**
	 */
	@Override
	@SuppressWarnings("ResourceType")
	void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
		if (UiConfig.MATERIALIZED) {
			if (tintAttributes.hasValue(R.styleable.Ui_ViewGroup_uiBackgroundTint)) {
				setBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_ViewGroup_uiBackgroundTint));
			}
			if (tintAttributes.hasValue(R.styleable.Ui_ViewGroup_uiBackgroundTintMode)) {
				setBackgroundTintMode(TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_ViewGroup_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				));
			}
		} else {
			if (tintAttributes.hasValue(R.styleable.Ui_ViewGroup_uiBackgroundTint)) {
				mTintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_ViewGroup_uiBackgroundTint);
			}
			mTintInfo.backgroundTintMode = TintManager.parseTintMode(
					tintAttributes.getInt(R.styleable.Ui_ViewGroup_uiBackgroundTintMode, 0),
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
	 */
	@Override
	@SuppressWarnings("ResourceType")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setFractionX(float fraction) {
		if (SLIDEABLE) mWidget.setX(mWidth > 0 ?
				(mWidget.getLeft() + (fraction * mWidth)) :
				OUT_OF_SCREEN);
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public float getFractionX() {
		return (SLIDEABLE && mWidth > 0) ? (mWidget.getLeft() + (mWidget.getX() / mWidth)) : 0;
	}

	/**
	 */
	@Override
	@SuppressWarnings("ResourceType")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setFractionY(float fraction) {
		if (SLIDEABLE) mWidget.setY(mHeight > 0 ?
				(mWidget.getTop() + (fraction * mHeight)) :
				OUT_OF_SCREEN);
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public float getFractionY() {
		return (SLIDEABLE && mHeight > 0) ? (mWidget.getTop() + (mWidget.getY() / mHeight)) : 0;
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

	/*
	 * Inner classes ===============================================================================
	 */
}
