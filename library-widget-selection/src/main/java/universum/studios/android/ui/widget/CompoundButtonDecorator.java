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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * A {@link FontDecorator} implementation that is used to decorate {@link CompoundButtonDecorator}
 * like widgets.
 *
 * @param <W> A type of the compound button widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class CompoundButtonDecorator<W extends CompoundButton & FontWidget> extends FontDecorator<W> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "CompoundButtonDecorator";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Drawable representing the button of the attached widget.
	 */
	private Drawable mButton;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #CompoundButtonDecorator(CompoundButton, int[])} with default
	 * {@link R.styleable#Ui_CompoundButton_Switch} as styleable attributes set.
	 */
	CompoundButtonDecorator(W widget) {
		super(widget, R.styleable.Ui_CompoundButton);
	}

	/**
	 * Creates a new instance of CompoundButtonDecorator for the given <var>widget</var>.
	 *
	 * @param widget         The compound button like widget for which to create new decorator.
	 * @param styleableAttrs Set of styleable attributes specific for the widget.
	 * @see FontDecorator#FontDecorator(View, int[])
	 */
	CompoundButtonDecorator(W widget, int[] styleableAttrs) {
		super(widget, styleableAttrs);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	@SuppressWarnings("ResourceType")
	void onProcessTintValues(Context context, TypedArray tintArray, int tintColor) {
		if (UiConfig.MATERIALIZED) {
			if (tintArray.hasValue(R.styleable.Ui_CompoundButton_uiButtonTint)) {
				setButtonTintList(tintArray.getColorStateList(R.styleable.Ui_CompoundButton_uiButtonTint));
			}
			if (tintArray.hasValue(R.styleable.Ui_CompoundButton_uiBackgroundTint)) {
				setBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_CompoundButton_uiBackgroundTint));
			}
			if (tintArray.hasValue(R.styleable.Ui_CompoundButton_uiButtonTintMode)) {
				setButtonTintMode(TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_CompoundButton_uiButtonTintMode, 0),
						PorterDuff.Mode.SRC_IN
				));
			}
			if (tintArray.hasValue(R.styleable.Ui_CompoundButton_uiBackgroundTintMode)) {
				setBackgroundTintMode(TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_CompoundButton_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				));
			}
		} else {
			mTintInfo.tintList = TintManager.createCompoundButtonTintColors(getContext(), tintColor);
			if (tintArray.hasValue(R.styleable.Ui_CompoundButton_uiButtonTint)) {
				mTintInfo.tintList = tintArray.getColorStateList(R.styleable.Ui_CompoundButton_uiButtonTint);
			}
			if (tintArray.hasValue(R.styleable.Ui_CompoundButton_uiBackgroundTint)) {
				mTintInfo.backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_CompoundButton_uiBackgroundTint);
			}
			mTintInfo.tintMode = TintManager.parseTintMode(
					tintArray.getInt(R.styleable.Ui_CompoundButton_uiButtonTintMode, 0),
					PorterDuff.Mode.SRC_IN
			);
			mTintInfo.backgroundTintMode = TintManager.parseTintMode(
					tintArray.getInt(R.styleable.Ui_CompoundButton_uiBackgroundTintMode, 0),
					mTintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
			);
		}
	}

	/**
	 */
	@Override
	void processTintValues(Context context, TypedArray tintArray) {
		super.processTintValues(context, tintArray);
		applyButtonTint();
	}

	/**
	 * This should be called from the attached widget whenever its {@link CompoundButton#setButtonDrawable(Drawable)}
	 * is invoked.
	 */
	void setButtonDrawable(Drawable d) {
		superSetButtonDrawable(mButton = d);
		applyButtonTint();
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the button.
	 * <p>
	 * <b>Note</b>, that for post {@link Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this method
	 * does nothing.
	 */
	void applyButtonTint() {
		if (UiConfig.MATERIALIZED ||
				mTintInfo == null ||
				(!mTintInfo.hasTintList && !mTintInfo.hasTintMode) ||
				mButton == null) {
			return;
		}
		final boolean isTintDrawable = mButton instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) mButton : new TintDrawable(mButton);
		if (mTintInfo.hasTintList) {
			tintDrawable.setTintList(mTintInfo.tintList);
		}
		if (mTintInfo.hasTintMode) {
			tintDrawable.setTintMode(mTintInfo.tintMode);
		}
		if (tintDrawable.isStateful()) {
			tintDrawable.setState(mWidget.getDrawableState());
		}
		if (isTintDrawable) {
			return;
		}
		superSetButtonDrawable(mButton = tintDrawable);
		tintDrawable.attachCallback();
	}

	/**
	 * Delegate method for super's {@link CompoundButton#setButtonDrawable(Drawable)}.
	 */
	abstract void superSetButtonDrawable(Drawable button);

	/**
	 * Applies a tint to the button drawable. Does not modify the current tint
	 * mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
	 *
	 * @param tint The desired tint to be applied. May be {@code null} to clear the current tint.
	 */
	void setButtonTintList(@Nullable ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			superSetButtonTintList(tint);
			return;
		}
		this.ensureTintInfo();
		mTintInfo.tintList = tint;
		mTintInfo.hasTintList = true;
		this.applyButtonTint();
	}

	/**
	 * Delegate method for super's {@link CompoundButton#setButtonTintList(ColorStateList)}.
	 */
	abstract void superSetButtonTintList(ColorStateList tint);

	/**
	 * Returns the tint applied to the button drawable.
	 *
	 * @return Button drawable's tint or {@code null} if no tint is applied.
	 */
	ColorStateList getButtonTintList() {
		if (UiConfig.MATERIALIZED) {
			return superGetButtonTintList();
		}
		return mTintInfo != null ? mTintInfo.tintList : null;
	}

	/**
	 * Delegate method for super's {@link CompoundButton#getButtonTintList()}.
	 */
	abstract ColorStateList superGetButtonTintList();

	/**
	 * Specifies a blending mode that should be used to apply tint specified via
	 * {@link #setButtonTintList(ColorStateList)} to the button drawable.
	 *
	 * @param tintMode The desired Porter duff mode. May be {@code null} to clear the current
	 *                 tint.
	 */
	void setButtonTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			superSetButtonTintMode(tintMode);
			return;
		}
		ensureTintInfo();
		mTintInfo.tintMode = tintMode;
		mTintInfo.hasTintMode = true;
		this.applyButtonTint();
	}

	/**
	 * Delegate method for super's {@link CompoundButton#setButtonTintMode(PorterDuff.Mode)}.
	 */
	abstract void superSetButtonTintMode(PorterDuff.Mode tintMode);

	/**
	 * Returns the blending mode used to apply tint to the button drawable.
	 *
	 * @return One of Porter duff modes or {@code null} if no mode has been specified.
	 */
	PorterDuff.Mode getButtonTintMode() {
		if (UiConfig.MATERIALIZED) {
			return superGetButtonTintMode();
		}
		return mTintInfo != null ? mTintInfo.tintMode : null;
	}

	/**
	 * Delegate method for super's {@link CompoundButton#getButtonTintMode()}.
	 */
	abstract PorterDuff.Mode superGetButtonTintMode();

	/**
	 * Inner classes ===============================================================================
	 */
}
