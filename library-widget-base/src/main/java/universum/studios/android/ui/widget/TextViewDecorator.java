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
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import universum.studios.android.font.FontWidget;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * A {@link FontWidgetDecorator} implementation that is used to decorate {@link TextView} like widgets.
 *
 * @param <W> A type of the text widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class TextViewDecorator<W extends TextView & FontWidget> extends FontWidgetDecorator<W> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "TextViewDecorator";

	/**
	 * Boolean flag indicating whether relative compound drawables are supported at the current
	 * Android API level or not.
	 */
	static final boolean RELATIVE_COMPOUND_DRAWABLES_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
			Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
			Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;

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
	 * Creates a new instance of TextViewDecorator for the given <var>widget</var>.
	 *
	 * @param widget         The widget that can present a text to a user.
	 * @param styleableAttrs Set of styleable attributes specific for the widget.
	 * @see FontWidgetDecorator#FontWidgetDecorator(View, int[])
	 */
	TextViewDecorator(W widget, int[] styleableAttrs) {
		super(widget, styleableAttrs);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	CompoundTintInfo onCreateTintInfo() {
		return new CompoundTintInfo();
	}

	/**
	 */
	@NonNull
	@Override
	CompoundTintInfo getTintInfo() {
		return (CompoundTintInfo) super.getTintInfo();
	}

	/**
	 */
	@Override
	void processTintAttributes(Context context, TypedArray tintAttributes) {
		super.processTintAttributes(context, tintAttributes);
		applyCompoundDrawablesTint();
	}

	/**
	 */
	@Override
	void onTintAttributesProcessed() {
		final CompoundTintInfo tintInfo = getTintInfo();
		// If there is no tint mode specified within style/xml do not tint at all.
		if (tintInfo.compoundTintMode == null) tintInfo.compoundTintList = null;
		tintInfo.hasCompoundTintList = tintInfo.compoundTintList != null;
		tintInfo.hasCompoundTintMode = tintInfo.compoundTintMode != null;
		super.onTintAttributesProcessed();
	}

	/**
	 */
	@Override
	boolean shouldInvalidateTintInfo(@NonNull BackgroundTintInfo tintInfo) {
		final CompoundTintInfo info = (CompoundTintInfo) tintInfo;
		return !info.hasCompoundTintList && !info.hasCompoundTintMode && super.shouldInvalidateTintInfo(tintInfo);
	}

	/**
	 * Applies a tint to the compound drawables. Does not modify the current tint mode, which is
	 * {@link PorterDuff.Mode#SRC_IN} by default.
	 *
	 * @param tint The desired tint to be applied. May be {@code null} to clear the current tint.
	 */
	public void setCompoundDrawableTintList(ColorStateList tint) {
		if (UiConfig.MATERIALIZED_MARSHMALLOW) {
			superSetCompoundDrawableTintList(tint);
			return;
		}
		final CompoundTintInfo tintInfo = getTintInfo();
		if (!tintInfo.hasCompoundTintMode) {
			tintInfo.compoundTintMode = PorterDuff.Mode.SRC_IN;
		}
		tintInfo.compoundTintList = tint;
		tintInfo.hasCompoundTintList = true;
		this.applyCompoundDrawablesTint();
	}

	/**
	 * Delegate method for super's {@link TextView#setCompoundDrawableTintList(ColorStateList)}.
	 */
	abstract void superSetCompoundDrawableTintList(ColorStateList tint);

	/**
	 * Returns the tint applied to the compound drawables.
	 *
	 * @return Compound drawable's tint or {@code null} if no tint is applied.
	 */
	public ColorStateList getCompoundDrawableTintList() {
		if (UiConfig.MATERIALIZED_MARSHMALLOW) {
			return superGetCompoundDrawableTintList();
		}
		return mTintInfo != null ? getTintInfo().compoundTintList : null;
	}

	/**
	 * Delegate method for super's {@link TextView#getCompoundDrawableTintList()}.
	 */
	abstract ColorStateList superGetCompoundDrawableTintList();

	/**
	 * Specifies a blending mode that should be used to apply tint specified via
	 * {@link #setCompoundDrawableTintList(ColorStateList)} to the compound drawables.
	 *
	 * @param tintMode The desired Porter duff mode. May be {@code null} to clear the current tint.
	 */
	public void setCompoundDrawableTintMode(PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED_MARSHMALLOW) {
			superSetCompoundDrawableTintMode(tintMode);
			return;
		}
		final CompoundTintInfo tintInfo = getTintInfo();
		tintInfo.compoundTintMode = tintMode;
		tintInfo.hasCompoundTintMode = true;
		this.applyCompoundDrawablesTint();
	}

	/**
	 * Delegate method for super's {@link TextView#setCompoundDrawableTintMode(PorterDuff.Mode)}.
	 */
	abstract void superSetCompoundDrawableTintMode(PorterDuff.Mode tintMode);

	/**
	 * Returns the blending mode used to apply tint to the compound drawables.
	 *
	 * @return One of Porter duff modes or {@code null} if no mode has been specified.
	 */
	public PorterDuff.Mode getCompoundDrawableTintMode() {
		if (UiConfig.MATERIALIZED_MARSHMALLOW) {
			return superGetCompoundDrawableTintMode();
		}
		return mTintInfo != null ? getTintInfo().compoundTintMode : null;
	}

	/**
	 * Delegate method for super's {@link TextView#getCompoundDrawableTintMode()}.
	 */
	abstract PorterDuff.Mode superGetCompoundDrawableTintMode();

	/**
	 * Applies current drawable tint from {@link #mTintInfo} to the compound drawables.
	 * <p>
	 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#M MARSHMALLOW} this method
	 * does nothing.
	 */
	@SuppressLint("NewApi")
	void applyCompoundDrawablesTint() {
		if (UiConfig.MATERIALIZED_MARSHMALLOW || mTintInfo == null) {
			return;
		}
		final CompoundTintInfo tintInfo = getTintInfo();
		if ((!tintInfo.hasCompoundTintList && !tintInfo.hasCompoundTintMode)) {
			return;
		}
		final int[] drawableState = mWidget.getDrawableState();
		final Drawable[] drawables = mWidget.getCompoundDrawables();
		if (tintCompoundDrawables(tintInfo, drawables, drawableState)) {
			superSetCompoundDrawables(
					drawables[0],
					drawables[1],
					drawables[2],
					drawables[3]
			);
			for (Drawable drawable : drawables) {
				if (drawable instanceof TintDrawable)
					((TintDrawable) drawable).attachCallback();
			}
		}
		if (RELATIVE_COMPOUND_DRAWABLES_SUPPORTED) {
			final Drawable[] relativeDrawables = mWidget.getCompoundDrawablesRelative();
			if (tintCompoundDrawables(tintInfo, relativeDrawables, drawableState)) {
				superSetCompoundDrawablesRelative(
						relativeDrawables[0],
						relativeDrawables[1],
						relativeDrawables[2],
						relativeDrawables[3]
				);
				for (Drawable drawable : relativeDrawables) {
					if (drawable instanceof TintDrawable)
						((TintDrawable) drawable).attachCallback();
				}
			}
		}
	}

	/**
	 * Applies tint from the specified <var>tintInfo</var> to the given array of <var>drawables</var>.
	 *
	 * @param tintInfo      Info object containing tint list and tint mode to be used for tinting process.
	 * @param drawables     Array of drawables to apply tint to.
	 * @param drawableState Current drawable state to be set to each drawable after tint has been
	 *                      applied to it.
	 * @return {@code True} if contents of the drawables array has been changed, {@code false}
	 * otherwise.
	 */
	private boolean tintCompoundDrawables(CompoundTintInfo tintInfo, Drawable[] drawables, int[] drawableState) {
		boolean drawablesChanged = false;
		for (int i = 0; i < drawables.length; i++) {
			final Drawable drawable = drawables[i];
			if (drawable == null) continue;
			final boolean isTintDrawable = drawable instanceof TintDrawable;
			final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) drawable : new TintDrawable(drawable);
			if (tintInfo.hasCompoundTintList) {
				tintDrawable.setTintList(tintInfo.compoundTintList);
			}
			if (tintInfo.hasCompoundTintMode) {
				tintDrawable.setTintMode(tintInfo.compoundTintMode);
			}
			if (tintDrawable.isStateful()) {
				tintDrawable.setState(drawableState);
			}
			if (!isTintDrawable) {
				drawablesChanged = true;
			}
			drawables[i] = tintDrawable;
		}
		return drawablesChanged;
	}

	/**
	 * Delegate method for super's {@link TextView#setCompoundDrawables(Drawable, Drawable, Drawable, Drawable)}.
	 */
	abstract void superSetCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom);

	/**
	 * Delegate method for super's {@link TextView#setCompoundDrawablesRelative(Drawable, Drawable, Drawable, Drawable)}.
	 */
	abstract void superSetCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom);

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * This class holds all data necessary to tint all components of widget wrapped by {@link TextViewDecorator} .
	 */
	static class CompoundTintInfo extends BackgroundTintInfo {

		/**
		 * Color state list used to tint a specific states of the <b>compound</b> drawable.
		 */
		ColorStateList compoundTintList;

		/**
		 * Flag indicating whether the {@link #compoundTintList} has been set or not.
		 */
		boolean hasCompoundTintList;

		/**
		 * Blending mode used to apply tint to the <b>compound</b> drawable.
		 */
		PorterDuff.Mode compoundTintMode;

		/**
		 * Flag indicating whether the {@link #compoundTintMode} has been set or not.
		 */
		boolean hasCompoundTintMode;
	}
}
