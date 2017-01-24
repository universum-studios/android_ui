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
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.EditText;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * A {@link TextViewDecorator} implementation that is used to decorate {@link EditText} like widgets.
 *
 * @param <W> A type of the editable widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class EditTextDecorator<W extends EditText & FontWidget> extends TextViewDecorator<W> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "EditTextDecorator";

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
	 * Creates a new instance of EditTextDecorator for the given <var>widget</var> with
	 * {@link R.styleable#Ui_EditText} attributes.
	 *
	 * @param widget The compound button like widget for which to create new decorator.
	 * @see FontDecorator#FontDecorator(View, int[])
	 */
	EditTextDecorator(W widget) {
		super(widget, R.styleable.Ui_EditText);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	void onProcessTypedValues(Context context, TypedArray typedArray) {
		super.onProcessTypedValues(context, typedArray);
		final Drawable[] compoundDrawables = RELATIVE_COMPOUND_DRAWABLES_SUPPORTED ?
				mWidget.getCompoundDrawablesRelative() :
				mWidget.getCompoundDrawables();
		boolean compoundDrawablesChanged = false;
		if (typedArray.hasValue(R.styleable.Ui_EditText_uiVectorDrawableStart)) {
			compoundDrawables[0] = inflateVectorDrawable(typedArray.getResourceId(
					R.styleable.Ui_EditText_uiVectorDrawableStart,
					0
			));
			compoundDrawablesChanged = true;
		}
		if (typedArray.hasValue(R.styleable.Ui_EditText_uiVectorDrawableTop)) {
			compoundDrawables[1] = inflateVectorDrawable(typedArray.getResourceId(
					R.styleable.Ui_EditText_uiVectorDrawableTop,
					0
			));
			compoundDrawablesChanged = true;
		}
		if (typedArray.hasValue(R.styleable.Ui_EditText_uiVectorDrawableEnd)) {
			compoundDrawables[2] = inflateVectorDrawable(typedArray.getResourceId(
					R.styleable.Ui_EditText_uiVectorDrawableEnd,
					0
			));
			compoundDrawablesChanged = true;
		}
		if (typedArray.hasValue(R.styleable.Ui_EditText_uiVectorDrawableBottom)) {
			compoundDrawables[3] = inflateVectorDrawable(typedArray.getResourceId(
					R.styleable.Ui_EditText_uiVectorDrawableBottom,
					0
			));
			compoundDrawablesChanged = true;
		}
		if (compoundDrawablesChanged) {
			if (RELATIVE_COMPOUND_DRAWABLES_SUPPORTED) {
				mWidget.setCompoundDrawablesRelativeWithIntrinsicBounds(
						compoundDrawables[0],
						compoundDrawables[1],
						compoundDrawables[2],
						compoundDrawables[3]
				);
			} else {
				mWidget.setCompoundDrawablesWithIntrinsicBounds(
						compoundDrawables[0],
						compoundDrawables[1],
						compoundDrawables[2],
						compoundDrawables[3]
				);
			}
		}
	}

	/**
	 */
	@Override
	@SuppressWarnings("ResourceType")
	void onProcessTintValues(Context context, TypedArray tintArray, int tintColor) {
		final CompoundTintInfo tintInfo = getTintInfo();
		// Process compound drawable tint values.
		if (UiConfig.MATERIALIZED_MARSHMALLOW) {
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiDrawableTint)) {
				setCompoundDrawableTintList(tintArray.getColorStateList(R.styleable.Ui_EditText_uiDrawableTint));
			}
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiDrawableTintMode)) {
				setCompoundDrawableTintMode(TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_EditText_uiDrawableTintMode, 0),
						PorterDuff.Mode.SRC_IN
				));
			}
		} else {
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiDrawableTint)) {
				tintInfo.compoundTintList = tintArray.getColorStateList(R.styleable.Ui_EditText_uiDrawableTint);
			}
			tintInfo.compoundTintMode = TintManager.parseTintMode(
					tintArray.getInteger(R.styleable.Ui_EditText_uiDrawableTintMode, 0),
					PorterDuff.Mode.SRC_IN
			);
		}
		// Process background tint values.
		if (UiConfig.MATERIALIZED_MARSHMALLOW) {
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiBackgroundTint)) {
				setBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_EditText_uiBackgroundTint));
			}
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiBackgroundTintMode)) {
				setBackgroundTintMode(TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_EditText_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				));
			}
		} else if (UiConfig.MATERIALIZED_LOLLIPOP) {
			PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiBackgroundTintMode)) {
				tintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_EditText_uiBackgroundTintMode, 0),
						tintMode
				);
			}
			if (tintMode != null) {
				ColorStateList backgroundTintList = TintManager.createEditTextTintColors(getContext(), tintColor);
				if (tintArray.hasValue(R.styleable.Ui_EditText_uiBackgroundTint)) {
					backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_EditText_uiBackgroundTint);
				}
				setBackgroundTintMode(tintMode);
				setBackgroundTintList(backgroundTintList);
			}
		} else {
			mTintInfo.tintList = ColorStateList.valueOf(tintColor);
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiTint)) {
				mTintInfo.tintList = tintArray.getColorStateList(R.styleable.Ui_EditText_uiTint);
			}
			mTintInfo.backgroundTintList = TintManager.createEditTextTintColors(getContext(), tintColor);
			if (tintArray.hasValue(R.styleable.Ui_EditText_uiBackgroundTint)) {
				mTintInfo.backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_EditText_uiBackgroundTint);
			}
			mTintInfo.tintMode = TintManager.parseTintMode(
					tintArray.getInt(R.styleable.Ui_EditText_uiTintMode, 0),
					PorterDuff.Mode.SRC_IN
			);
			mTintInfo.backgroundTintMode = TintManager.parseTintMode(
					tintArray.getInt(R.styleable.Ui_EditText_uiBackgroundTintMode, 0),
					PorterDuff.Mode.SRC_IN
			);
		}
	}

	/**
	 */
	@Override
	void processTintValues(Context context, TypedArray tintArray) {
		super.processTintValues(context, tintArray);
		applyTint();
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the components of the attached widget.
	 * <p>
	 * <b>Note</b>, that for post {@link Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this method does
	 * nothing.
	 */
	@SuppressWarnings("ConstantConditions")
	private void applyTint() {
		if (UiConfig.MATERIALIZED ||
				mTintInfo == null ||
				(!mTintInfo.hasTintList && !mTintInfo.hasTintMode)) {
			return;
		}
		// Tint also left/right and middle selection handles. These handles can not be unfortunately
		// set programmatically so we will tint directly drawables within resources.
		final Resources resources = mWidget.getResources();
		final Resources.Theme theme = mWidget.getContext().getTheme();
		final int tintColor = mTintInfo.tintList != null ? mTintInfo.tintList.getDefaultColor() : Color.TRANSPARENT;
		TintManager.tintRawDrawable(
				ResourceUtils.getDrawable(
						resources,
						R.drawable.ui_text_select_handle_left_alpha,
						theme
				),
				tintColor,
				mTintInfo.tintMode
		);
		TintManager.tintRawDrawable(
				ResourceUtils.getDrawable(
						resources,
						R.drawable.ui_text_select_handle_middle_alpha,
						theme
				),
				tintColor,
				mTintInfo.tintMode
		);
		TintManager.tintRawDrawable(
				ResourceUtils.getDrawable(
						resources,
						R.drawable.ui_text_select_handle_right_alpha,
						theme
				),
				tintColor,
				mTintInfo.tintMode
		);
	}

	/**
	 * Inner classes ===============================================================================
	 */
}