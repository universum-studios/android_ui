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
import android.view.View;
import android.widget.ImageView;

import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * A {@link WidgetDecorator} implementation that is used to decorate {@link ImageView} like widgets.
 *
 * @param <W> A type of the image widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class ImageDecorator<W extends ImageView> extends WidgetDecorator<W> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ImageDecorator";

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
	 * Creates a new instance of ImageDecorator for the given <var>widget</var>.
	 *
	 * @param widget         The image like widget for which to create new decorator.
	 * @param styleableAttrs Set of styleable attributes specific for the widget.
	 * @see WidgetDecorator#WidgetDecorator(View, int[])
	 */
	ImageDecorator(W widget, int[] styleableAttrs) {
		super(widget, styleableAttrs);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	void processTintValues(Context context, TypedArray tintArray) {
		super.processTintValues(context, tintArray);
		applyImageTint();
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the image drawable.
	 * <p>
	 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this
	 * method does nothing.
	 */
	void applyImageTint() {
		if (UiConfig.MATERIALIZED) {
			return;
		}
		final Drawable drawable = mWidget.getDrawable();
		if (mTintInfo == null ||
				(!mTintInfo.hasTintList && !mTintInfo.hasTintMode) ||
				drawable == null) {
			return;
		}
		final boolean isTintDrawable = drawable instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) drawable : new TintDrawable(drawable);
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
		superSetImageDrawable(tintDrawable);
		tintDrawable.attachCallback();
	}

	/**
	 * Delegate method for super's {@link ImageView#setImageDrawable(Drawable)}.
	 */
	abstract void superSetImageDrawable(Drawable drawable);

	/**
	 * Applies a tint to the image drawable. Does not modify the current tint
	 * mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
	 *
	 * @param tint The desired tint to be applied. May be {@code null} to clear the current tint.
	 */
	void setImageTintList(ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			superSetImageTintList(tint);
			return;
		}
		this.ensureTintInfo();
		if (!mTintInfo.hasTintMode) {
			mTintInfo.tintMode = PorterDuff.Mode.SRC_IN;
		}
		mTintInfo.tintList = tint;
		mTintInfo.hasTintList = true;
		this.applyImageTint();
	}

	/**
	 * Delegate method for super's {@link ImageView#setImageTintList(ColorStateList)}.
	 */
	abstract void superSetImageTintList(ColorStateList tint);

	/**
	 * Returns the tint applied to the image drawable.
	 *
	 * @return Image drawable's tint or {@code null} if no tint is applied.
	 */
	ColorStateList getImageTintList() {
		if (UiConfig.MATERIALIZED) {
			return superGetImageTintList();
		}
		return mTintInfo != null ? mTintInfo.tintList : null;
	}

	/**
	 * Delegate method for super's {@link ImageView#getImageTintList()}.
	 */
	abstract ColorStateList superGetImageTintList();

	/**
	 * Specifies a blending mode that should be used to apply tint specified via
	 * {@link #setImageTintList(ColorStateList)} to the image drawable.
	 *
	 * @param tintMode The desired Porter duff mode. May be {@code null} to clear the current
	 *                 tint.
	 */
	void setImageTintMode(PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			superSetImageTintMode(tintMode);
			return;
		}
		this.ensureTintInfo();
		mTintInfo.tintMode = tintMode;
		mTintInfo.hasTintMode = true;
		this.applyImageTint();
	}

	/**
	 * Delegate method for super's {@link ImageView#setImageTintMode(PorterDuff.Mode)}.
	 */
	abstract void superSetImageTintMode(PorterDuff.Mode tintMode);

	/**
	 * Returns the blending mode used to apply tint to the image drawable.
	 *
	 * @return One of Porter duff modes or {@code null} if no mode has been specified.
	 */
	PorterDuff.Mode getImageTintMode() {
		if (UiConfig.MATERIALIZED) {
			return superGetImageTintMode();
		}
		return mTintInfo != null ? mTintInfo.tintMode : null;
	}

	/**
	 * Delegate method for super's {@link ImageView#getImageTintMode()}.
	 */
	abstract PorterDuff.Mode superGetImageTintMode();

	/**
	 * Inner classes ===============================================================================
	 */
}
