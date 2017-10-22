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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * A WidgetDecorator is used within all widgets from the UI library that are derived directly from
 * those within Android framework to support some of the features that has been added in newer
 * versions of Android and are not available on the lower ones, like drawables tinting, but to also
 * support some additional features of which implementation is same for such widgets regardless theirs
 * type.
 *
 * @param <W> A type of the widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class WidgetDecorator<W extends View> implements Widget, ErrorWidget {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "WidgetDecorator";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Widget for which will this decorator provide logic otherwise not supported by the widget.
	 */
	final W mWidget;

	/**
	 * Set of private flags specific for the attached widget.
	 */
	int mPrivateFlags = PrivateFlags.PFLAG_ALLOWS_DEFAULT_SELECTION;

	/**
	 * Data used when tinting components of this view.
	 */
	BackgroundTintInfo mTintInfo;

	/**
	 * Attached widget's dimension.
	 */
	int mWidth, mHeight;

	/**
	 * Animator used to animate size of the attached widget.
	 */
	private WidgetSizeAnimator mSizeAnimator;

	/**
	 * Set of attributes to be used to parse typed values for the attached widget whenever
	 * {@link #processAttributes(Context, AttributeSet, int, int)} is called.
	 */
	private final int[] mStyleableAttrs;

	/**
	 * Error specified for the attached widget (if any).
	 */
	private CharSequence mError;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of WidgetDecorator for the given <var>widget</var>.
	 *
	 * @param widget         The widget for which to create new decorator.
	 * @param styleableAttrs Set of styleable attributes specific for the widget. These attributes
	 *                       will be used to obtain an instance of {@link TypedArray} passed to
	 *                       {@link #onProcessAttributes(Context, TypedArray)} whenever
	 *                       {@link #processAttributes(Context, AttributeSet, int, int)} is called.
	 */
	WidgetDecorator(W widget, int[] styleableAttrs) {
		this.mWidget = widget;
		this.mStyleableAttrs = styleableAttrs;
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * This should be called from the attached widget during its initialization.
	 *
	 * @param context      The context that can be used to access resource values.
	 * @param attrs        Set of attributes passed to the widget's constructor.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     the attached widget within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the attached widget.
	 */
	void processAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		final TypedArray attributes = context.obtainStyledAttributes(attrs, mStyleableAttrs, defStyleAttr, defStyleRes);
		if (attributes != null) {
			onProcessAttributes(context, attributes);
			attributes.recycle();
		}
	}

	/**
	 * Invoked from {@link #processAttributes(Context, AttributeSet, int, int)} to process all values
	 * from the given <var>attributes</var> that are related to the attached widget.
	 *
	 * @param context    The context that can be used to access resource values.
	 * @param attributes The typed array obtained for the styleable attributes supplied to this decorator
	 *                   during its initialization.
	 */
	void onProcessAttributes(Context context, TypedArray attributes) {
		processTintAttributes(context, attributes);
	}

	/**
	 * Inflates an instance of VectorDrawableCompat from the given <var>resId</var>.
	 *
	 * @param resId Resource id of the vector drawable to inflate.
	 * @return Instance of inflated vector drawable or {@code null} if the drawable failed to be inflated.
	 */
	@Nullable
	@SuppressLint("NewApi")
	Drawable inflateVectorDrawable(@DrawableRes int resId) {
		return ResourceUtils.getVectorDrawable(mWidget.getResources(), resId, mWidget.getContext().getTheme());
	}

	/**
	 * Invoked from {@link #onProcessAttributes(Context, TypedArray)} to process only values from
	 * the given <var>tintArray</var> related to tint.
	 */
	void processTintAttributes(Context context, TypedArray tintAttributes) {
		this.ensureTintInfo();
		int tintColor = Color.TRANSPARENT;
		final Resources.Theme theme = context.getTheme();
		if (theme != null) {
			final TypedValue typedValue = new TypedValue();
			if (theme.resolveAttribute(R.attr.colorControlActivated, typedValue, true)) {
				tintColor = typedValue.data;
			}
		}
		onProcessTintAttributes(context, tintAttributes, tintColor);
		onTintAttributesProcessed();
		applyBackgroundTint();
	}

	/**
	 * Invoked from {@link #processTintAttributes(Context, TypedArray)}.
	 *
	 * @param tintColor Color obtained from the current theme for {@link R.attr#colorControlActivated}
	 *                  attribute.
	 */
	abstract void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor);

	/**
	 * Invoked after {@link #onProcessTintAttributes(Context, TypedArray, int)} has been completed.
	 */
	void onTintAttributesProcessed() {
		// If there is no tint mode specified within style/xml do not tint at all.
		if (mTintInfo.tintMode == null) mTintInfo.tintList = null;
		mTintInfo.hasTintList = mTintInfo.tintList != null;
		mTintInfo.hasTintMode = mTintInfo.tintMode != null;
		// If there is no background tint mode specified within style/xml do not tint at all.
		if (mTintInfo.backgroundTintMode == null) mTintInfo.backgroundTintList = null;
		mTintInfo.hasBackgroundTintList = mTintInfo.backgroundTintList != null;
		mTintInfo.hasBackgroundTinMode = mTintInfo.backgroundTintMode != null;
		if (shouldInvalidateTintInfo(mTintInfo)) {
			this.mTintInfo = null;
		}
	}

	/**
	 * Called to check whether the given <var>tintInfo</var> should be invalidated or not.
	 *
	 * @param tintInfo The tint info the check whether to invalidate.
	 * @return {@code True} if the tint info does not contain any tint data so it can be safely invalidated,
	 * {@code false} otherwise.
	 */
	boolean shouldInvalidateTintInfo(@NonNull BackgroundTintInfo tintInfo) {
		return !tintInfo.hasTintList && !tintInfo.hasTintMode && !tintInfo.hasBackgroundTintList && !tintInfo.hasBackgroundTinMode;
	}

	/**
	 * Applies a tint to the background drawable. Does not modify the current tint mode, which is
	 * {@link PorterDuff.Mode#SRC_IN} by default.
	 *
	 * @param tint The desired tint to be applied. May be {@code null} to clear the current tint.
	 */
	void setBackgroundTintList(@Nullable ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			superSetBackgroundTintList(tint);
			return;
		}
		this.ensureTintInfo();
		if (!mTintInfo.hasBackgroundTinMode) {
			mTintInfo.backgroundTintMode = PorterDuff.Mode.SRC_IN;
		}
		mTintInfo.backgroundTintList = tint;
		mTintInfo.hasBackgroundTintList = true;
		this.applyBackgroundTint();
	}

	/**
	 * Delegate method for super's {@link View#setBackgroundTintList(ColorStateList)}.
	 */
	abstract void superSetBackgroundTintList(ColorStateList tint);

	/**
	 * Returns the tint applied to the background drawable.
	 *
	 * @return Background drawable's tint or {@code null} if no tint is applied.
	 */
	ColorStateList getBackgroundTintList() {
		if (UiConfig.MATERIALIZED) {
			return superGetBackgroundTintList();
		}
		return mTintInfo != null ? mTintInfo.backgroundTintList : null;
	}

	/**
	 * Delegate method for super's {@link View#getBackgroundTintList()}.
	 */
	abstract ColorStateList superGetBackgroundTintList();

	/**
	 * Specifies a blending mode that should be used to apply tint specified via
	 * {@link #setBackgroundTintList(ColorStateList)} to the background drawable.
	 *
	 * @param tintMode The desired Porter duff mode. May be {@code null} to clear the current tint.
	 */
	void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			superSetBackgroundTintMode(tintMode);
			return;
		}
		this.ensureTintInfo();
		mTintInfo.backgroundTintMode = tintMode;
		mTintInfo.hasBackgroundTinMode = true;
		this.applyBackgroundTint();
	}

	/**
	 * Delegate method for super's {@link View#setBackgroundTintMode(PorterDuff.Mode)}.
	 */
	abstract void superSetBackgroundTintMode(PorterDuff.Mode tintMode);

	/**
	 * Returns the blending mode used to apply tint to the background drawable.
	 *
	 * @return One of Porter duff modes or {@code null} if no mode has been specified.
	 */
	PorterDuff.Mode getBackgroundTintMode() {
		if (UiConfig.MATERIALIZED) {
			return superGetBackgroundTintMode();
		}
		return mTintInfo != null ? mTintInfo.backgroundTintMode : null;
	}

	/**
	 * Delegate method for super's {@link View#getBackgroundTintMode()}.
	 */
	abstract PorterDuff.Mode superGetBackgroundTintMode();

	/**
	 * Applies current background tint from {@link #mTintInfo} to the background drawable.
	 * <p>
	 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this method
	 * does nothing.
	 */
	@SuppressWarnings("deprecation")
	void applyBackgroundTint() {
		final Drawable drawable = mWidget.getBackground();
		if (UiConfig.MATERIALIZED ||
				mTintInfo == null ||
				(!mTintInfo.hasBackgroundTintList && !mTintInfo.hasBackgroundTinMode) ||
				drawable == null) {
			return;
		}
		final boolean isTintDrawable = drawable instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) drawable : new TintDrawable(drawable);
		if (mTintInfo.hasBackgroundTintList) {
			tintDrawable.setTintList(mTintInfo.backgroundTintList);
		}
		if (mTintInfo.hasBackgroundTinMode) {
			tintDrawable.setTintMode(mTintInfo.backgroundTintMode);
		}
		if (tintDrawable.isStateful()) {
			tintDrawable.setState(mWidget.getDrawableState());
		}
		if (isTintDrawable) {
			return;
		}
		superSetBackgroundDrawable(tintDrawable);
		tintDrawable.attachCallback();
	}

	/**
	 * Delegate method for super's {@link View#setBackgroundDrawable(Drawable)}.
	 */
	abstract void superSetBackgroundDrawable(Drawable drawable);

	/**
	 * Checks whether this decorator has some tint info specified or not.
	 *
	 * @return {@code True} if tint info is available, {@code false} otherwise.
	 */
	boolean hasTintInfo() {
		return mTintInfo != null;
	}

	/**
	 * Returns the current tint info object of this decorator. If there is no tint info create yet,
	 * a new instance will be initialized.
	 *
	 * @return This decorator's tint info.
	 */
	@NonNull
	BackgroundTintInfo getTintInfo() {
		this.ensureTintInfo();
		return mTintInfo;
	}

	/**
	 * Ensures that the tint info object is initialized.
	 */
	void ensureTintInfo() {
		if (mTintInfo == null) this.mTintInfo = onCreateTintInfo();
	}

	/**
	 * Called to create tint info object for this decorator.
	 *
	 * @return Tint info that will be used to store all tint related information.
	 */
	BackgroundTintInfo onCreateTintInfo() {
		return new BackgroundTintInfo();
	}

	/**
	 */
	@Override
	public void setError(@Nullable CharSequence error) {
		if (error == null) {
			clearError();
		} else if (!TextUtils.equals(mError, error)) {
			this.mError = error;
			mWidget.refreshDrawableState();
			mWidget.invalidate();
		}
	}

	/**
	 */
	@Override
	public boolean hasError() {
		return mError != null;
	}

	/**
	 */
	@Nullable
	@Override
	public CharSequence getError() {
		return mError;
	}

	/**
	 */
	@Override
	public void clearError() {
		if (mError != null) {
			this.mError = null;
			mWidget.refreshDrawableState();
			mWidget.invalidate();
		}
	}

	/**
	 * This should be called from the attached widget whenever its {@link View#onSizeChanged(int, int, int, int)}
	 * method is invoked.
	 */
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		this.mWidth = width;
		this.mHeight = height;
	}

	/**
	 */
	@NonNull
	@Override
	public WidgetSizeAnimator animateSize() {
		return (mSizeAnimator != null) ? mSizeAnimator : (mSizeAnimator = new WidgetSizeAnimator(mWidget));
	}

	/**
	 * This should be called from the attached widget whenever its {@link View#onAttachedToWindow()}
	 * method is invoked.
	 */
	void onAttachedToWindow() {
		this.updatePrivateFlags(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW, true);
	}

	/**
	 * This should be called from the attached widget whenever its {@link View#onTouchEvent(MotionEvent)}
	 * method is invoked.
	 */
	boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	/**
	 * This should be called from the attached widget whenever its {@link View#verifyDrawable(Drawable)}
	 * method is invoked.
	 */
	boolean verifyDrawable(Drawable drawable) {
		return false;
	}

	/**
	 * This should be called from the attached widget whenever its {@link View#draw(Canvas)} method
	 * is invoked.
	 */
	void draw(Canvas canvas) {
	}

	/**
	 * This should be called from the attached widget whenever its {@link View#dispatchDraw(Canvas)}
	 * method is invoked.
	 */
	void dispatchDraw(Canvas canvas) {
	}

	/**
	 * This should be called from the attached widget whenever its {@link View#onDetachedFromWindow()}
	 * method is invoked.
	 */
	void onDetachedFromWindow() {
		this.updatePrivateFlags(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW, false);
	}

	/**
	 * Updates the current private flags.
	 *
	 * @param flag Value of the desired flag to add/remove to/from the current private flags.
	 * @param add  Boolean flag indicating whether to add or remove the specified <var>flag</var>.
	 */
	void updatePrivateFlags(int flag, boolean add) {
		if (add) this.mPrivateFlags |= flag;
		else this.mPrivateFlags &= ~flag;
	}

	/**
	 * Returns a boolean flag indicating whether the specified <var>flag</var> is contained within
	 * the current private flags or not.
	 *
	 * @param flag Value of the flag to check.
	 * @return {@code True} if the requested flag is contained, {@code false} otherwise.
	 */
	boolean hasPrivateFlag(int flag) {
		return (mPrivateFlags & flag) != 0;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
