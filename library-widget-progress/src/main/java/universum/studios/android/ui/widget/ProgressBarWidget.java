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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ProgressBar;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.graphics.drawable.TintLayerDrawable;

/**
 * Extended version of {@link ProgressBar}.
 *
 * <h3>Tinting</h3>
 * Tinting is supported for the <b>progress and indeterminate</b> components of this widget.
 * There are several Xml attributes for this purpose:
 * <ul>
 * <li>{@link R.attr#uiProgressTint uiProgressTint}</li>
 * <li>{@link R.attr#uiProgressTintMode uiProgressTintMode}</li>
 * <li>{@link R.attr#uiIndeterminateTint uiIndeterminateTint}</li>
 * <li>{@link R.attr#uiIndeterminateTintMode uiIndeterminateTintMode}</li>
 * </ul>
 * <p>
 * This widget also overrides all SDK methods used to tint its components like {@link #setProgressTintList(android.content.res.ColorStateList)}
 * or {@link #setProgressTintMode(android.graphics.PorterDuff.Mode)} and others, so these can be used
 * regardless the current version of SDK but invoking of these methods below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
 * can be done only directly upon instance of this widget or by casting a general instance of ProgressBar
 * to this one otherwise {@link java.lang.NoSuchMethodException} will be thrown.
 * <p>
 * <b>Note, that tinting of the background is also supported by this widget for the versions below
 * LOLLIPOP.</b>
 *
 * <h3>Sliding</h3>
 * This updated view allows updating of its current position along <b>x</b> and <b>y</b> axis by
 * changing <b>fraction</b> of these properties depending on its current size using the new animation
 * framework introduced in {@link android.os.Build.VERSION_CODES#HONEYCOMB HONEYCOMB} by
 * {@link android.animation.ObjectAnimator ObjectAnimator}s API.
 * <p>
 * Changing of fraction of X or Y is supported by these two methods:
 * <ul>
 * <li>{@link #setFractionX(float)}</li>
 * <li>{@link #setFractionY(float)}</li>
 * </ul>
 * <p>
 * For example if an instance of this view class needs to be slided to the right by whole width of
 * such a view, an Xml file with ObjectAnimator will look like this:
 * <pre>
 *  &lt;objectAnimator xmlns:android="http://schemas.android.com/apk/res/android"
 *                  android:propertyName="fractionX"
 *                  android:valueFrom="0.0"
 *                  android:valueTo="1.0"
 *                  android:duration="300"/&gt;
 * </pre>
 *
 * @author Martin Albedinsky
 */
public class ProgressBarWidget extends ProgressBar implements Widget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ProgressBarWidget";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Maximum level value that can be applied to this progress bar as progress value.
	 */
	private static int MAX_LEVEL = 10000;

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Decorator used to extend API of this widget by functionality otherwise not supported or not
	 * available due to current API level.
	 */
	private Decorator mDecorator;

	/**
	 * Drawable used to draw progress of this progress bar.
	 */
	private Drawable mProgressDrawable;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #ProgressBarWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public ProgressBarWidget(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #ProgressBarWidget(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link android.R.attr#progressBarStyle} as attribute for default style.
	 */
	public ProgressBarWidget(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.progressBarStyle);
	}

	/**
	 * Same as {@link #ProgressBarWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public ProgressBarWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of ProgressBarWidget within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ProgressBarWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Called from one of constructors of this view to perform its initialization.
	 * <p>
	 * Initialization is done via parsing of the specified <var>attrs</var> set and obtaining for
	 * this view specific data from it that can be used to configure this new view instance. The
	 * specified <var>defStyleAttr</var> and <var>defStyleRes</var> are used to obtain default data
	 * from the current theme provided by the specified <var>context</var>.
	 */
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.ensureDecorator();
		mDecorator.processAttributes(context, attrs, defStyleAttr, defStyleRes);
		this.applyProgressTints();
		this.applyIndeterminateTint();
	}

	/**
	 * Ensures that the decorator for this view is initialized.
	 */
	private void ensureDecorator() {
		if (mDecorator == null) this.mDecorator = new Decorator(this);
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(ProgressBarWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(ProgressBarWidget.class.getName());
	}

	/**
	 */
	@Override
	public void setProgressDrawable(Drawable drawable) {
		super.setProgressDrawable(mProgressDrawable = drawable);
		this.applyProgressTints();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintLayerDrawable TintLayerDrawable} if tint has
	 * been applied to one of progress layers via {@link #setProgressTintList(ColorStateList)} or
	 * {@link #setSecondaryProgressTintList(ColorStateList)} or {@link #setProgressBackgroundTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Override
	public Drawable getProgressDrawable() {
		return super.getProgressDrawable();
	}

	/**
	 * Applies current progress tints from {@link Decorator#mTintInfo} to the progress drawable (its
	 * layers respectively if it is instance of LayerDrawable or to the whole drawable if it is just
	 * simple drawable).
	 * <p>
	 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this method
	 * does nothing.
	 */
	private void applyProgressTints() {
		if (UiConfig.MATERIALIZED) {
			return;
		}
		this.ensureDecorator();
		if (mProgressDrawable == null || !mDecorator.hasTintInfo()) {
			return;
		}
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		if (!tintInfo.hasTintList && !tintInfo.hasTintMode &&
				!tintInfo.hasSecondaryProgressTintList && !tintInfo.hasSecondaryProgressTintMode &&
				!tintInfo.hasProgressBackgroundTintList && !tintInfo.hasProgressBackgroundTintMode) {
			return;
		}
		if (mProgressDrawable instanceof TintLayerDrawable) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasProgressBackgroundTintList) {
				tintDrawable.setTintList(tintInfo.progressBackgroundTintList, android.R.id.background);
			}
			if (tintInfo.hasProgressBackgroundTintMode) {
				tintDrawable.setTintMode(tintInfo.progressBackgroundTintMode, android.R.id.background);
			}
			if (tintInfo.hasSecondaryProgressTintList) {
				tintDrawable.setTintList(tintInfo.secondaryProgressTintList, android.R.id.secondaryProgress);
			}
			if (tintInfo.hasSecondaryProgressTintMode) {
				tintDrawable.setTintMode(tintInfo.secondaryProgressTintMode, android.R.id.secondaryProgress);
			}
			if (tintInfo.hasTintList) {
				tintDrawable.setTintList(tintInfo.tintList, android.R.id.progress);
			}
			if (tintInfo.hasTintMode) {
				tintDrawable.setTintMode(tintInfo.tintMode, android.R.id.progress);
			}
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
			return;
		} else if (mProgressDrawable instanceof TintDrawable) {
			final TintDrawable tintDrawable = (TintDrawable) mProgressDrawable;
			if (tintInfo.hasTintList) {
				tintDrawable.setTintList(tintInfo.tintList);
			}
			if (tintInfo.hasTintMode) {
				tintDrawable.setTintMode(tintInfo.tintMode);
			}
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
			return;
		}
		if (mProgressDrawable instanceof LayerDrawable) {
			final TintLayerDrawable tintDrawable = new TintLayerDrawable((LayerDrawable) mProgressDrawable);
			this.mProgressDrawable = tintDrawable;
			this.applyProgressTint();
			this.applySecondaryProgressTint();
			this.applyProgressBackgroundTint();
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
			super.setProgressDrawable(mProgressDrawable);
			tintDrawable.attachCallback();
			tintDrawable.setLevel((int) (getProgress() / (float) getMax() * MAX_LEVEL));
		} else {
			final TintDrawable tintDrawable = new TintDrawable(mProgressDrawable);
			this.mProgressDrawable = tintDrawable;
			this.applySimpleProgressTint();
			super.setProgressDrawable(mProgressDrawable);
			tintDrawable.attachCallback();
			tintDrawable.setLevel((int) (getProgress() / (float) getMax() * MAX_LEVEL));
		}
	}

	/**
	 * Applies current first valid tint from {@link Decorator#mTintInfo} to the progress drawable as
	 * whole.
	 *
	 * @see #applyProgressTints()
	 */
	private void applySimpleProgressTint() {
		if (mProgressDrawable instanceof TintDrawable) {
			final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
			final TintDrawable tintDrawable = (TintDrawable) mProgressDrawable;
			boolean hasTintList, hasTintMode;
			hasTintList = hasTintMode = false;
			ColorStateList tintList = null;
			PorterDuff.Mode tintMode = null;
			if (tintInfo.hasTintList || tintInfo.hasTintMode) {
				hasTintList = tintInfo.hasTintList;
				tintList = tintInfo.tintList;
				hasTintMode = tintInfo.hasTintMode;
				tintMode = tintInfo.tintMode;
			} else if (tintInfo.hasSecondaryProgressTintList || tintInfo.hasSecondaryProgressTintMode) {
				hasTintList = tintInfo.hasSecondaryProgressTintList;
				tintList = tintInfo.secondaryProgressTintList;
				hasTintMode = tintInfo.hasSecondaryProgressTintMode;
				tintMode = tintInfo.secondaryProgressTintMode;
			} else if (tintInfo.hasProgressBackgroundTintList || tintInfo.hasProgressBackgroundTintMode) {
				hasTintList = tintInfo.hasProgressBackgroundTintList;
				tintList = tintInfo.progressBackgroundTintList;
				hasTintMode = tintInfo.hasProgressBackgroundTintMode;
				tintMode = tintInfo.progressBackgroundTintMode;
			}
			if (hasTintList) tintDrawable.setTintList(tintList);
			if (hasTintMode) tintDrawable.setTintMode(tintMode);
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
		}
	}

	/**
	 * Applies current primary progress tint from {@link Decorator#mTintInfo} to the progress layer
	 * of the progress drawable.
	 *
	 * @see #applyProgressTints()
	 */
	private void applyProgressTint() {
		if (!(mProgressDrawable instanceof TintLayerDrawable) || !mDecorator.hasTintInfo()) return;
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		if (tintInfo.hasTintList || tintInfo.hasTintMode) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasTintList) {
				tintDrawable.setTintList(tintInfo.tintList, android.R.id.progress);
			}
			if (tintInfo.hasTintMode) {
				tintDrawable.setTintMode(tintInfo.tintMode, android.R.id.progress);
			}
		}
	}

	/**
	 * Applies current secondary progress tint from {@link Decorator#mTintInfo} to the secondary
	 * progress layer of the progress drawable.
	 *
	 * @see #applyProgressTints()
	 */
	private void applySecondaryProgressTint() {
		if (!(mProgressDrawable instanceof TintLayerDrawable) || !mDecorator.hasTintInfo()) return;
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		if (tintInfo.hasSecondaryProgressTintList || tintInfo.hasSecondaryProgressTintMode) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasSecondaryProgressTintList) {
				tintDrawable.setTintList(tintInfo.secondaryProgressTintList, android.R.id.secondaryProgress);
			}
			if (tintInfo.hasSecondaryProgressTintMode) {
				tintDrawable.setTintMode(tintInfo.secondaryProgressTintMode, android.R.id.secondaryProgress);
			}
		}
	}

	/**
	 * Applies current progress background tint from {@link Decorator#mTintInfo} to the background
	 * layer of the progress drawable.
	 *
	 * @see #applyProgressTints()
	 */
	private void applyProgressBackgroundTint() {
		if (!(mProgressDrawable instanceof TintLayerDrawable) || !mDecorator.hasTintInfo()) return;
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		if (tintInfo.hasProgressBackgroundTintList || tintInfo.hasProgressBackgroundTintMode) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasProgressBackgroundTintList) {
				tintDrawable.setTintList(tintInfo.progressBackgroundTintList, android.R.id.background);
			}
			if (tintInfo.hasProgressBackgroundTintMode) {
				tintDrawable.setTintMode(tintInfo.progressBackgroundTintMode, android.R.id.background);
			}
		}
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setProgressTintList(@Nullable ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressTintList(tint);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.tintList = tint;
		tintInfo.hasTintList = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applyProgressTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public ColorStateList getProgressTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().tintList : null;
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.tintMode = tintMode;
		tintInfo.hasTintMode = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applyProgressTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public PorterDuff.Mode getProgressTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().tintMode : null;
	}

	/**
	 */
	@Override
	public void setSecondaryProgressTintList(ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setSecondaryProgressTintList(tint);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.secondaryProgressTintList = tint;
		tintInfo.hasSecondaryProgressTintList = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applySecondaryProgressTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getSecondaryProgressTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getSecondaryProgressTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().secondaryProgressTintList : null;
	}

	/**
	 */
	@Override
	public void setSecondaryProgressTintMode(PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setSecondaryProgressTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.secondaryProgressTintMode = tintMode;
		tintInfo.hasSecondaryProgressTintMode = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applySecondaryProgressTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getSecondaryProgressTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getSecondaryProgressTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().secondaryProgressTintMode : null;
	}

	/**
	 */
	@Override
	public void setProgressBackgroundTintList(ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressBackgroundTintList(tint);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.progressBackgroundTintList = tint;
		tintInfo.hasProgressBackgroundTintList = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applyProgressBackgroundTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getProgressBackgroundTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressBackgroundTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().progressBackgroundTintList : null;
	}

	/**
	 */
	@Override
	public void setProgressBackgroundTintMode(PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressBackgroundTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.progressBackgroundTintMode = tintMode;
		tintInfo.hasProgressBackgroundTintMode = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applyProgressBackgroundTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getProgressBackgroundTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressBackgroundTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().progressBackgroundTintMode : null;
	}

	/**
	 */
	@Override
	public void setIndeterminateDrawable(Drawable drawable) {
		super.setIndeterminateDrawable(drawable);
		this.applyIndeterminateTint();
	}

	/**
	 * Applies current indeterminate tint from {@link Decorator#mTintInfo} to the current indeterminate
	 * drawable.
	 * <p>
	 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this method
	 * does nothing.
	 */
	@SuppressWarnings("deprecation")
	private void applyIndeterminateTint() {
		final Drawable drawable = getIndeterminateDrawable();
		this.ensureDecorator();
		if (UiConfig.MATERIALIZED || drawable == null || !mDecorator.hasTintInfo()) {
			return;
		}
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		if (!tintInfo.hasIndeterminateTintList && !tintInfo.hasIndeterminateTintMode) {
			return;
		}
		final boolean isTintDrawable = drawable instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) drawable : new TintDrawable(drawable);
		if (tintInfo.hasIndeterminateTintList) {
			tintDrawable.setTintList(tintInfo.indeterminateTintList);
		}
		if (tintInfo.hasIndeterminateTintMode) {
			tintDrawable.setTintMode(tintInfo.indeterminateTintMode);
		}
		if (tintDrawable.isStateful()) {
			tintDrawable.setState(getDrawableState());
		}
		if (isTintDrawable) {
			return;
		}
		super.setIndeterminateDrawable(tintDrawable);
		tintDrawable.attachCallback();
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setIndeterminateTintList(@Nullable ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setIndeterminateTintList(tint);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.indeterminateTintList = tint;
		tintInfo.hasIndeterminateTintList = true;
		this.applyIndeterminateTint();
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public ColorStateList getIndeterminateTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getIndeterminateTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().indeterminateTintList : null;
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setIndeterminateTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setIndeterminateTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final ProgressTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.indeterminateTintMode = tintMode;
		tintInfo.hasIndeterminateTintMode = true;
		this.applyIndeterminateTint();
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public PorterDuff.Mode getIndeterminateTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getIndeterminateTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().indeterminateTintMode : null;
	}

	/**
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(background);
		this.ensureDecorator();
		mDecorator.applyBackgroundTint();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setBackgroundTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Override
	public Drawable getBackground() {
		return super.getBackground();
	}

	/**
	 */
	@Override
	public void setBackgroundTintList(@Nullable ColorStateList tint) {
		this.ensureDecorator();
		mDecorator.setBackgroundTintList(tint);
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getBackgroundTintList() {
		this.ensureDecorator();
		return mDecorator.getBackgroundTintList();
	}

	/**
	 */
	@Override
	public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureDecorator();
		mDecorator.setBackgroundTintMode(tintMode);
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getBackgroundTintMode() {
		this.ensureDecorator();
		return mDecorator.getBackgroundTintMode();
	}

	/**
	 */
	@Override
	public void setFractionX(float fraction) {
		this.ensureDecorator();
		mDecorator.setFractionX(fraction);
	}

	/**
	 */
	@Override
	public float getFractionX() {
		this.ensureDecorator();
		return mDecorator.getFractionX();
	}

	/**
	 */
	@Override
	public void setFractionY(float fraction) {
		this.ensureDecorator();
		mDecorator.setFractionY(fraction);
	}

	/**
	 */
	@Override
	public float getFractionY() {
		this.ensureDecorator();
		return mDecorator.getFractionY();
	}

	/**
	 */
	@Override
	public void setPressed(boolean pressed) {
		final boolean isPressed = isPressed();
		super.setPressed(pressed);
		if (!isPressed && pressed) onPressed();
		else if (isPressed) onReleased();
	}

	/**
	 * Invoked whenever {@link #setPressed(boolean)} is called with {@code true} and this view
	 * isn't in the pressed state yet.
	 */
	protected void onPressed() {
	}

	/**
	 * Invoked whenever {@link #setPressed(boolean)} is called with {@code false} and this view
	 * is currently in the pressed state.
	 */
	protected void onReleased() {
	}

	/**
	 */
	@Override
	public void setSelected(boolean selected) {
		this.ensureDecorator();
		mDecorator.setSelected(selected);
	}

	/**
	 */
	@Override
	public void setSelectionState(boolean selected) {
		this.ensureDecorator();
		mDecorator.setSelectionState(selected);
	}

	/**
	 */
	@Override
	public void setAllowDefaultSelection(boolean allow) {
		this.ensureDecorator();
		mDecorator.setAllowDefaultSelection(allow);
	}

	/**
	 */
	@Override
	public boolean allowsDefaultSelection() {
		this.ensureDecorator();
		return mDecorator.allowsDefaultSelection();
	}

	/**
	 */
	@NonNull
	@Override
	public WidgetSizeAnimator animateSize() {
		this.ensureDecorator();
		return mDecorator.animateSize();
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.ensureDecorator();
		mDecorator.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * This class holds all data necessary to tint all components of this view.
	 */
	private static final class ProgressTintInfo extends BackgroundTintInfo {

		/**
		 * Color state list used to tint a specific states of the <b>secondary progress</b> drawable.
		 */
		ColorStateList secondaryProgressTintList;

		/**
		 * Flag indicating whether the {@link #secondaryProgressTintList} has been set or not.
		 */
		boolean hasSecondaryProgressTintList;

		/**
		 * Blending mode used to apply tint to the <b>secondary progress</b> drawable.
		 */
		PorterDuff.Mode secondaryProgressTintMode;

		/**
		 * Flag indicating whether the {@link #secondaryProgressTintMode} has been set or not.
		 */
		boolean hasSecondaryProgressTintMode;

		/**
		 * Color state list used to tint a specific states of the <b>progress background</b> drawable.
		 */
		ColorStateList progressBackgroundTintList;

		/**
		 * Flag indicating whether the {@link #progressBackgroundTintList} has been set or not.
		 */
		boolean hasProgressBackgroundTintList;

		/**
		 * Blending mode used to apply tint to the <b>progress background</b> drawable.
		 */
		PorterDuff.Mode progressBackgroundTintMode;

		/**
		 * Flag indicating whether the {@link #progressBackgroundTintMode} has been set or not.
		 */
		boolean hasProgressBackgroundTintMode;

		/**
		 * Color state list used to tint a specific states of the <b>indeterminate</b> drawable.
		 */
		ColorStateList indeterminateTintList;

		/**
		 * Flag indicating whether the {@link #indeterminateTintList} has been set or not.
		 */
		boolean hasIndeterminateTintList;

		/**
		 * Blending mode used to apply tint to the <b>indeterminate</b> drawable.
		 */
		PorterDuff.Mode indeterminateTintMode;

		/**
		 * Flag indicating whether the {@link #indeterminateTintMode} has been set or not.
		 */
		boolean hasIndeterminateTintMode;
	}

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends WidgetDecorator<ProgressBarWidget> {

		/**
		 * See {@link WidgetDecorator#WidgetDecorator(View, int[])}.
		 */
		Decorator(ProgressBarWidget widget) {
			super(widget, R.styleable.Ui_ProgressBar);
		}

		/**
		 */
		@Override
		BackgroundTintInfo onCreateTintInfo() {
			return new ProgressTintInfo();
		}

		/**
		 */
		@NonNull
		@Override
		ProgressTintInfo getTintInfo() {
			return (ProgressTintInfo) super.getTintInfo();
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintValues(Context context, TypedArray tintArray, int tintColor) {
			if (UiConfig.MATERIALIZED) {
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressTint)) {
					setProgressTintList(tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiProgressTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiSecondaryProgressTint)) {
					setSecondaryProgressTintList(tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiSecondaryProgressTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressBackgroundTint)) {
					setProgressBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiProgressBackgroundTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiIndeterminateTint)) {
					setIndeterminateTintList(tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiIndeterminateTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiBackgroundTint)) {
					setBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiBackgroundTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressTintMode)) {
					setProgressTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_ProgressBar_uiProgressTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiSecondaryProgressTintMode)) {
					setSecondaryProgressTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_ProgressBar_uiSecondaryProgressTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressBackgroundTintMode)) {
					setProgressBackgroundTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_ProgressBar_uiProgressBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiIndeterminateTintMode)) {
					setIndeterminateTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_ProgressBar_uiIndeterminateTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_ProgressBar_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				final ProgressTintInfo tintInfo = getTintInfo();
				tintInfo.tintList = ColorStateList.valueOf(tintColor);
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressTint)) {
					tintInfo.tintList = tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiProgressTint);
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiSecondaryProgressTint)) {
					tintInfo.secondaryProgressTintList = tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiSecondaryProgressTint);
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressBackgroundTint)) {
					tintInfo.progressBackgroundTintList = tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiProgressBackgroundTint);
				}
				tintInfo.indeterminateTintList = ColorStateList.valueOf(tintColor);
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiIndeterminateTint)) {
					tintInfo.indeterminateTintList = tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiIndeterminateTint);
				}
				if (tintArray.hasValue(R.styleable.Ui_ProgressBar_uiBackgroundTint)) {
					tintInfo.backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_ProgressBar_uiBackgroundTint);
				}
				tintInfo.tintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_ProgressBar_uiProgressTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
				tintInfo.secondaryProgressTintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_ProgressBar_uiSecondaryProgressTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
				tintInfo.progressBackgroundTintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_ProgressBar_uiProgressBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
				tintInfo.indeterminateTintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_ProgressBar_uiIndeterminateTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
				tintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_ProgressBar_uiBackgroundTintMode, 0),
						tintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
				);
				tintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintArray.getInteger(R.styleable.Ui_ProgressBar_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
			}
		}

		/**
		 */
		@Override
		void onTintValuesProcessed() {
			final ProgressTintInfo tintInfo = getTintInfo();
			// If there is no tint mode specified within style/xml do not tint at all.
			if (tintInfo.indeterminateTintMode == null) tintInfo.indeterminateTintList = null;
			if (tintInfo.secondaryProgressTintMode == null) tintInfo.secondaryProgressTintList = null;
			if (tintInfo.progressBackgroundTintMode == null) tintInfo.progressBackgroundTintList = null;
			tintInfo.hasSecondaryProgressTintList = tintInfo.secondaryProgressTintList != null;
			tintInfo.hasSecondaryProgressTintMode = tintInfo.secondaryProgressTintMode != null;
			tintInfo.hasProgressBackgroundTintList = tintInfo.progressBackgroundTintList != null;
			tintInfo.hasProgressBackgroundTintMode = tintInfo.progressBackgroundTintMode != null;
			tintInfo.hasIndeterminateTintList = tintInfo.indeterminateTintList != null;
			tintInfo.hasIndeterminateTintMode = tintInfo.indeterminateTintMode != null;
			super.onTintValuesProcessed();
		}

		/**
		 */
		@Override
		boolean shouldInvalidateTintInfo(@NonNull BackgroundTintInfo tintInfo) {
			final ProgressTintInfo info = (ProgressTintInfo) tintInfo;
			return !info.hasIndeterminateTintList && !info.hasIndeterminateTintMode &&
					!info.hasSecondaryProgressTintList && !info.hasSecondaryProgressTintMode &&
					!info.hasProgressBackgroundTintList && !info.hasProgressBackgroundTintMode &&
					super.shouldInvalidateTintInfo(tintInfo);
		}

		/**
		 */
		@Override
		void superSetSelected(boolean selected) {
			ProgressBarWidget.super.setSelected(selected);
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			ProgressBarWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			ProgressBarWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return ProgressBarWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			ProgressBarWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return ProgressBarWidget.super.getBackgroundTintMode();
		}
	}
}
