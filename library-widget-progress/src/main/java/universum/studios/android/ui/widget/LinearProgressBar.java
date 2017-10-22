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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.ui.R;
import universum.studios.android.ui.graphics.drawable.LinearProgressDrawable;
import universum.studios.android.ui.graphics.drawable.ProgressDrawable;

/**
 * A {@link BaseProgressBar} implementation with {@link LinearProgressDrawable} used to present a
 * linear progress to a user. Like it is described in {@link BaseProgressBar BaseProgressBar's}
 * class overview, this progress bar only implements necessary logic required for proper working,
 * drawing and animating of its progress drawable.
 * <p>
 * <b>Note</b>, that this progress bar is not a replacement for the Android's {@link android.widget.ProgressBar},
 * but rather an implementation to provide the <b>material</b> based progress graphics for pre
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions. This progress bar provides
 * very similar API like the one from the Android SDK, only difference is that, it requires implementation
 * of {@link LinearProgressDrawable} as its drawable. Appearance of the progress drawable can be
 * changed via {@link #setProgressMode(int)}.
 * <p>
 * If you are building an application only for LOLLIPOP and above, feel free and use ProgressBar from
 * the Android SDK instead.
 *
 * <h3>Progress modes</h3>
 * The mode which can be specified via {@link #setProgressMode(int)} changes appearance/type of the
 * progress graphics (determinate|indeterminate|buffer|query indeterminate determinate).
 * See {@link LinearProgressDrawable LinearProgressDrawable's} class overview for supported modes to
 * decide which one of them best fits your needs.
 *
 * <h3>Tinting</h3>
 * Tinting is supported via Xml attributes listed below + {@link BaseProgressBar super's attributes}:
 * <ul>
 * <li>{@link R.attr#uiSecondaryProgressTint uiSecondaryProgressTint}</li>
 * <li>{@link R.attr#uiSecondaryProgressTintMode uiSecondaryProgressTintMode}</li>
 * </ul>
 * <p>
 * For custom tint attributes there are also provided theirs java methods like {@link #setSecondaryProgressTintList(ColorStateList)}
 * or {@link #setSecondaryProgressTintMode(PorterDuff.Mode)}.
 *
 * <h3>XML attributes</h3>
 * See {@link BaseProgressBar},
 * {@link R.styleable#Ui_ProgressBar_Linear LinearProgressBar Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiProgressBarLinearStyle uiProgressBarLinearStyle}
 *
 * @author Martin Albedinsky
 * @see CircularProgressBar
 */
public class LinearProgressBar extends BaseProgressBar<LinearProgressDrawable> {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "LinearProgressBar";

	/**
	 * Flag copied from {@link LinearProgressDrawable#MODE_DETERMINATE} for better access.
	 */
	public static final int MODE_DETERMINATE = LinearProgressDrawable.MODE_DETERMINATE;

	/**
	 * Flag copied from {@link LinearProgressDrawable#MODE_INDETERMINATE} for better access.
	 */
	public static final int MODE_INDETERMINATE = LinearProgressDrawable.MODE_INDETERMINATE;

	/**
	 * Flag copied from {@link LinearProgressDrawable#MODE_BUFFER} for better access.
	 */
	public static final int MODE_BUFFER = LinearProgressDrawable.MODE_BUFFER;

	/**
	 * Flag copied from {@link LinearProgressDrawable#MODE_QUERY_INDETERMINATE_DETERMINATE} for better
	 * access.
	 */
	public static final int MODE_QUERY_INDETERMINATE_DETERMINATE = LinearProgressDrawable.MODE_QUERY_INDETERMINATE_DETERMINATE;

	/**
	 * Defines an annotation for determining set of allowed modes for LinearProgressBar.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({
			MODE_INDETERMINATE,
			MODE_DETERMINATE,
			MODE_BUFFER,
			MODE_QUERY_INDETERMINATE_DETERMINATE
	})
	public @interface ProgressMode {
	}

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Secondary progress of this progress drawable set by {@link #setSecondaryProgress(int)}.
	 */
	private int mSecondaryProgress;

	/**
	 * Set of private flags specific for this widget.
	 */
	private int mPrivateFlags = PrivateFlags.PFLAG_ATTACHED_TO_WINDOW;

	/**
	 * Data used when tinting components of this view.
	 */
	private Widget.TintInfo mTintInfo;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #LinearProgressBar(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public LinearProgressBar(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #LinearProgressBar(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link R.attr#uiProgressBarLinearStyle} as attribute for default style.
	 */
	public LinearProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiProgressBarLinearStyle);
	}

	/**
	 * Same as {@link #LinearProgressBar(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public LinearProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of LinearProgressBar for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public LinearProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context, attrs, defStyleAttr, defStyleRes);
	}

	/*
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
		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_ProgressBar_Linear, defStyleAttr, defStyleRes);
		this.processTintValues(context, attributes);
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_ProgressBar_Linear_android_secondaryProgress) {
				setSecondaryProgress(attributes.getInt(index, mSecondaryProgress));
			} else if (index == R.styleable.Ui_ProgressBar_Linear_uiLinearProgressMode) {
				changeMode(attributes.getInt(index, getProgressMode()));
			}
		}
		this.applySecondaryProgressTint();
	}

	/**
	 */
	@Override
	void onAttachDrawable() {
		changeMode(MODE_DETERMINATE);
		setDrawable(new LinearProgressDrawable());
	}

	/**
	 * Called from the constructor to process tint values for this view.
	 *
	 * @param context    The context passed to constructor.
	 * @param typedArray TypedArray obtained for styleable attributes specific for this view.
	 */
	@SuppressWarnings("All")
	private void processTintValues(Context context, TypedArray typedArray) {
		this.ensureTintInfo();
		if (typedArray.hasValue(R.styleable.Ui_ProgressBar_Linear_uiSecondaryProgressTint)) {
			mTintInfo.tintList = typedArray.getColorStateList(R.styleable.Ui_ProgressBar_Linear_uiSecondaryProgressTint);
		}
		mTintInfo.tintMode = TintManager.parseTintMode(
				typedArray.getInt(R.styleable.Ui_ProgressBar_Linear_uiSecondaryProgressTintMode, 0),
				PorterDuff.Mode.SRC_IN
		);
		// If there is no tint mode specified within style/xml do not tint at all.
		if (mTintInfo.tintMode == null) {
			mTintInfo.tintList = null;
		}
		mTintInfo.hasTintList = mTintInfo.tintList != null;
		mTintInfo.hasTintMode = mTintInfo.tintMode != null;
	}

	/**
	 * Ensures that the tint info object is initialized.
	 */
	private void ensureTintInfo() {
		if (mTintInfo == null) this.mTintInfo = new Widget.TintInfo();
	}

	/**
	 */
	@Override
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(LinearProgressBar.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(LinearProgressBar.class.getName());
	}

	/**
	 * Applies a tint to the secondary progress graphics of the drawable, if specified. This call
	 * does not modify the current tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setDrawable(ProgressDrawable)} will automatically mutate the
	 * drawable and apply the specified tint and tint mode using
	 * {@link LinearProgressDrawable#setSecondaryProgressTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see R.attr#uiSecondaryProgressTint ui:uiSecondaryProgressTint
	 * @see #getSecondaryProgressTintList()
	 * @see LinearProgressDrawable#setSecondaryProgressTintList(android.content.res.ColorStateList)
	 */
	public void setSecondaryProgressTintList(@Nullable ColorStateList tint) {
		this.ensureTintInfo();
		mTintInfo.tintList = tint;
		mTintInfo.hasTintList = true;
		this.applySecondaryProgressTint();
	}

	/**
	 * Returns the tint applied to the secondary progress graphics of the progress drawable, if specified.
	 *
	 * @return The secondary progress graphics tint.
	 * @see #setSecondaryProgressTintList(android.content.res.ColorStateList)
	 */
	@Nullable
	public ColorStateList getSecondaryProgressTintList() {
		return mTintInfo != null ? mTintInfo.tintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setSecondaryProgressTintList(android.content.res.ColorStateList)}}
	 * to the secondary progress graphics of the progress drawable. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see R.attr#uiSecondaryProgressTintMode ui:uiSecondaryProgressTintMode
	 * @see #getSecondaryProgressTintMode()
	 * @see LinearProgressDrawable#setSecondaryProgressTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setSecondaryProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureTintInfo();
		mTintInfo.tintMode = tintMode;
		mTintInfo.hasTintMode = true;
		this.applySecondaryProgressTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the secondary progress graphics of the
	 * progress drawable, if specified.
	 *
	 * @return The secondary progress graphics blending mode used to apply the tint.
	 * @see #setSecondaryProgressTintMode(android.graphics.PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getSecondaryProgressTintMode() {
		return mTintInfo != null ? mTintInfo.tintMode : null;
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the secondary progress graphics of the current
	 * progress drawable.
	 *
	 * @return {@code True} if the tint has been applied or cleared, {@code false} otherwise.
	 */
	private boolean applySecondaryProgressTint() {
		if (mTintInfo == null ||
				(!mTintInfo.hasTintList && !mTintInfo.hasTintMode) ||
				mDrawable == null) {
			return false;
		}
		mDrawable.mutate();
		if (mTintInfo.hasTintList) {
			mDrawable.setSecondaryProgressTintList(mTintInfo.tintList);
		}
		if (mTintInfo.hasTintMode) {
			mDrawable.setSecondaryProgressTintMode(mTintInfo.tintMode);
		}
		return true;
	}

	/**
	 * @see R.attr#uiLinearProgressMode ui:uiLinearProgressMode
	 */
	@Override
	public void setProgressMode(@ProgressMode int mode) {
		super.setProgressMode(mode);
	}

	/**
	 */
	@Override
	@ProgressMode
	@SuppressWarnings("ResourceType")
	public int getProgressMode() {
		return super.getProgressMode();
	}

	/**
	 * Sets the current value of secondary progress displayed by this progress bar. This is only
	 * supported for the {@link #MODE_BUFFER} to specify buffer progress.
	 * <p>
	 * <b>Note</b>, that it is allowed to call this method also from the background thread.
	 *
	 * @param secondaryProgress The desired secondary progress value. Should be from the range
	 *                          {@code [0, getMax()]}.
	 * @see android.R.attr#secondaryProgress android:secondaryProgress
	 * @see #getSecondaryProgress()
	 * @see #getMax()
	 * @see #setProgressMode(int)
	 */
	public synchronized void setSecondaryProgress(int secondaryProgress) {
		if (mMode == MODE_BUFFER && mSecondaryProgress != secondaryProgress && secondaryProgress >= 0 && secondaryProgress <= mMax) {
			refreshProgress(android.R.id.secondaryProgress, mSecondaryProgress = secondaryProgress);
		}
	}

	/**
	 * Returns the current value of secondary progress displayed by this progress bar.
	 *
	 * @return Current secondary progress value from the range {@code [0, getMax()]} or {@code 0} if
	 * the current mode is not {@link #MODE_BUFFER}.
	 * @see #setSecondaryProgress(int)
	 */
	public synchronized int getSecondaryProgress() {
		if (mDrawable != null) {
			this.mSecondaryProgress = mDrawable.getSecondaryProgress();
		}
		return mMode == MODE_BUFFER ? mSecondaryProgress : 0;
	}

	/**
	 */
	@Override
	public synchronized void setMax(int max) {
		super.setMax(max);
		refreshProgress(android.R.id.secondaryProgress, mSecondaryProgress);
	}

	/**
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		switch (mMode) {
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				startIndeterminate();
				break;
		}
	}

	/**
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		this.updatePrivateFlags(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW, false);
	}

	/**
	 */
	@Override
	void handleVisibilityChange(boolean visible) {
		super.handleVisibilityChange(visible);
		switch (mMode) {
			case MODE_BUFFER:
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				if (visible) {
					startIndeterminate();
				} else {
					stopIndeterminateImmediate();
				}
				break;
		}
	}

	/**
	 */
	@Override
	void onModeChange(int mode) {
		super.onModeChange(mode);
		switch (mode) {
			case MODE_BUFFER:
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				startIndeterminate();
				break;
		}
	}

	/**
	 */
	@Override
	void onRestartMode(int mode) {
		switch (mode) {
			case MODE_BUFFER:
				setSecondaryProgress(0);
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				setProgress(0);
				startIndeterminate();
				break;
			default:
				super.onRestartMode(mode);
		}
	}

	/**
	 */
	@Override
	void onSetUpDrawable(@NonNull LinearProgressDrawable drawable) {
		super.onSetUpDrawable(drawable);
		drawable.setSecondaryProgress(mSecondaryProgress);
		this.applySecondaryProgressTint();
	}

	/**
	 */
	@Override
	synchronized void onRefreshProgress(int id, int progress, boolean notify) {
		super.onRefreshProgress(id, progress, notify);
		if (id == android.R.id.secondaryProgress) {
			if (mDrawable != null) {
				mDrawable.setSecondaryProgress(progress);
				if (mMode == MODE_BUFFER && (mPrivateFlags & PrivateFlags.PFLAG_ATTACHED_TO_WINDOW) != 0) {
					if (mSecondaryProgress == mMax) {
						mDrawable.stop();
					}
				}
			} else {
				invalidate();
			}
		}
	}

	/**
	 */
	@NonNull
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.secondaryProgress = mSecondaryProgress;
		return savedState;
	}

	/**
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		setSecondaryProgress(savedState.secondaryProgress);
	}

	/**
	 * Updates the current private flags.
	 *
	 * @param flag Value of the desired flag to add/remove to/from the current private flags.
	 * @param add  Boolean flag indicating whether to add or remove the specified <var>flag</var>.
	 */
	@SuppressWarnings("unused")
	private void updatePrivateFlags(int flag, boolean add) {
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
	@SuppressWarnings("unused")
	private boolean hasPrivateFlag(int flag) {
		return (mPrivateFlags & flag) != 0;
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link LinearProgressBar}
	 * is properly saved.
	 *
	 * @author Martin Albedinsky
	 */
	public static class SavedState extends WidgetSavedState {

		/**
		 * Creator used to create an instance or array of instances of SavedState from {@link android.os.Parcel}.
		 */
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			/**
			 */
			@Override
			public SavedState createFromParcel(@NonNull Parcel source) {
				return new SavedState(source);
			}

			/**
			 */
			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		/**
		 */
		int secondaryProgress;

		/**
		 * Creates a new instance of SavedState with the given <var>superState</var> to allow chaining
		 * of saved states in {@link #onSaveInstanceState()} and also in {@link #onRestoreInstanceState(android.os.Parcelable)}.
		 *
		 * @param superState The super state obtained from {@code super.onSaveInstanceState()} within
		 *                   {@code onSaveInstanceState()}.
		 */
		protected SavedState(@NonNull Parcelable superState) {
			super(superState);
		}

		/**
		 * Called from {@link #CREATOR} to create an instance of SavedState form the given parcel
		 * <var>source</var>.
		 *
		 * @param source Parcel with data for the new instance.
		 */
		protected SavedState(@NonNull Parcel source) {
			super(source);
			this.secondaryProgress = source.readInt();
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(secondaryProgress);
		}
	}
}
