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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AnyThread;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.util.Pools;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.ProgressDrawable;

/**
 * A {@link ViewWidget} implementation which represents a base container for {@link ProgressDrawable}
 * to draw a progress or an indeterminate graphics and primary to handle logic for which the ProgressDrawable
 * does not have enough capacity, like starting and stopping animations.
 * <p>
 * BaseProgressBar supports base logic to properly present the attached ProgressDrawable like
 * measuring based on the size of the drawable and also its drawing. This class also handles base
 * management, like starting/stopping of indeterminate animations for the progress drawable based
 * on its current mode (determinate or indeterminate), other modes need to be managed by a specific
 * implementation of the BaseProgressBar class.
 * <p>
 * Progress drawable can be specified via {@link #setDrawable(ProgressDrawable)} and can be accessed
 * via {@link #getDrawable()} which allows some customizations of the progress drawable's appearance.
 * There are also provided (delegated) some methods for direct access to the drawable like,
 * {@link #setProgressMode(int)} {@link #setProgress(int)} or {@link #startIndeterminate()} and {@link #stopIndeterminate()}.
 *
 * <h3>Tinting</h3>
 * Tinting is supported via Xml attributes listed below:
 * <ul>
 * <li>{@link R.attr#uiProgressTint uiProgressTint}</li>
 * <li>{@link R.attr#uiProgressTintMode uiProgressTintMode}</li>
 * <li>{@link R.attr#uiIndeterminateTint uiIndeterminateTint}</li>
 * <li>{@link R.attr#uiIndeterminateTintMode uiIndeterminateTintMode}</li>
 * <li>{@link R.attr#uiProgressBackgroundTint uiProgressBackgroundTint}</li>
 * <li>{@link R.attr#uiProgressBackgroundTintMode uiProgressBackgroundTintMode}</li>
 * <li>{@link R.attr#uiBackgroundTint uiBackgroundTint}</li>
 * <li>{@link R.attr#uiBackgroundTintMode uiBackgroundTintMode}</li>
 * </ul>
 * <p>
 * <b>Note, that on {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and above SDK versions
 * can be also used Xml attributes listed above where in such case will be used the native tinting.</b>
 * <p>
 * This widget also overrides all SDK methods used to tint its components like {@link #setBackgroundTintList(android.content.res.ColorStateList)}
 * or {@link #setBackgroundTintMode(android.graphics.PorterDuff.Mode)}, so these can be used regardless
 * the current version of SDK but invoking of these methods below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
 * can be done only directly upon instance of this widget otherwise {@link NoSuchMethodException}
 * will be thrown.
 * <p>
 * For custom tint attributes there are also provided theirs java methods like {@link #setProgressTintList(ColorStateList)}
 * or {@link #setIndeterminateTintList(ColorStateList)}.
 *
 * <h3>XML attributes</h3>
 * See {@link ViewWidget},
 * {@link R.styleable#Ui_ProgressBar BaseProgressBar Attributes}
 *
 * @author Martin Albedinsky
 */
public abstract class BaseProgressBar<D extends ProgressDrawable> extends ViewWidget
		implements
		ProgressDrawable.AnimationCallback,
		ProgressDrawable.ExplodeAnimationCallback {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Listener which can receive callbacks about <b>started</b> or <b>stopped</b> animation session
	 * of progress drawable.
	 */
	public interface OnProgressAnimationListener {

		/**
		 * Invoked whenever a new animation session is started for the specified progress <var>drawable</var>.
		 *
		 * @param progressBar A progress bar to which is the specified drawable attached to.
		 * @param drawable    The progress drawable for which has been requested new animation session
		 *                    by {@link ProgressDrawable#start()} and the drawable has been before that
		 *                    call in the idle mode.
		 */
		void onStarted(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable);

		/**
		 * Invoked whenever the current animation sessions is stopped for the specified progress
		 * <var>drawable</var>.
		 *
		 * @param progressBar A progress bar to which is the specified drawable attached to.
		 * @param drawable    The progress drawable for which has been stopped its current animation
		 *                    sessions by {@link ProgressDrawable#stop()} or {@link ProgressDrawable#stopImmediate()}
		 *                    and the drawable has been before that call in the animation mode.
		 */
		void onStopped(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable);
	}

	/**
	 * Listener which can receive callbacks about <b>exploded</b> and <b>imploded</b> thickness of
	 * progress drawable.
	 */
	public interface OnProgressExplodeAnimationListener {

		/**
		 * Invoked whenever an explosion of the specified progress <var>drawable</var> is finished.
		 *
		 * @param progressBar A progress bar to which is the specified drawable attached to.
		 * @param drawable    The progress drawable for which has been explosion of its thickness
		 *                    finished after {@link ProgressDrawable#explode()} has been called upon
		 *                    the drawable.
		 */
		void onExploded(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable);

		/**
		 * Invoked whenever an implosion of the specified progress <var>drawable</var> is finished.
		 *
		 * @param progressBar A progress bar to which is the specified drawable attached to.
		 * @param drawable    The progress drawable for which has been implosion of its thickness
		 *                    finished after {@link ProgressDrawable#implode()} has been called upon
		 *                    the drawable.
		 */
		void onImploded(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "BaseProgressBar";

	/**
	 * Flag copied from {@link ProgressDrawable#DETERMINATE} for better access.
	 */
	private static final int DETERMINATE = ProgressDrawable.DETERMINATE;

	/**
	 * Flag copied from {@link ProgressDrawable#INDETERMINATE} for better access.
	 */
	private static final int INDETERMINATE = ProgressDrawable.INDETERMINATE;

	/**
	 * Delay for posting of an accessibility events from this view.
	 */
	private static final long ACCESSIBILITY_EVENT_DELAY = 200;

	/**
	 * Flag indicating whether {@link #mRefreshProgressRunnable} has been posted or not.
	 */
	private static final int PFLAG_REFRESH_PROGRESS_POSTED = 0x00008000;

	/**
	 * Flag indicating whether an indeterminate animation should be stopped after the progress
	 * drawable has been imploded.
	 */
	private static final int PFLAG_STOP_INDETERMINATE_AFTER_IMPLOSION = 0x00010000;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Lock used for synchronized operations.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Drawable used to draw the progress.
	 */
	D mDrawable;

	/**
	 * Current mode of this progress bar determining the drawing behaviour of the progress drawable.
	 */
	int mMode;

	/**
	 * Progress drawable's dimension.
	 */
	private int mDrawableWidth, mDrawableHeight;

	/**
	 * Set of private flags specific for this widget.
	 */
	private int mPrivateFlags = PrivateFlags.PFLAG_ALLOWS_DEFAULT_SELECTION;

	/**
	 * Maximum allowed value of progress which can be set to this progress bar.
	 */
	int mMax = ProgressDrawable.MAX_PROGRESS;

	/**
	 * Id of the UI thread used to check if a specific method call is on UI thread or not.
	 */
	long mUiThreadId;

	/**
	 * Current progress value of this progress bar set by {@link #setProgress(int)}.
	 */
	private int mProgress;

	/**
	 * Animation callback delegate for the current ProgressDrawable.
	 */
	private OnProgressAnimationListener mProgressAnimationListener;

	/**
	 * Explode animation callback delegate for the current ProgressDrawable.
	 */
	private OnProgressExplodeAnimationListener mProgressExplodeAnimationListener;

	/**
	 * Data used when tinting components of this view.
	 */
	private TintInfo mTintInfo;

	/**
	 * Task used to refresh progress from the background thread.
	 */
	private RefreshProgressRunnable mRefreshProgressRunnable;

	/**
	 * Task used to post an accessibility event for the changed progress.
	 */
	private AccessibilityEventSender mAccessibilityEventSender;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #BaseProgressBar(android.content.Context, android.util.AttributeSet)}
	 * without attributes.
	 */
	BaseProgressBar(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #BaseProgressBar(android.content.Context, android.util.AttributeSet, int)}
	 * with <code>0</code> as attribute for default style.
	 */
	BaseProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Same as {@link #BaseProgressBar(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	BaseProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of BaseProgressBar for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	BaseProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
	@SuppressLint("NewApi")
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.mUiThreadId = Thread.currentThread().getId();
		// Use software layer that is required for proper drawing work of progress drawables.
		if (ProgressDrawable.REQUIRES_SOFTWARE_LAYER) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		onAttachDrawable();
		if (mDrawable == null) {
			throw new IllegalArgumentException("No progress drawable has been attached.");
		}
		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_ProgressBar, defStyleAttr, defStyleRes);
		this.processTintValues(context, attributes);
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_ProgressBar_android_max) {
				setMax(attributes.getInt(index, getMax()));
			} else if (index == R.styleable.Ui_ProgressBar_android_progress) {
				setProgress(attributes.getInt(index, mProgress));
			} else if (index == R.styleable.Ui_ProgressBar_uiColorProgress) {
				mDrawable.setColor(attributes.getColor(index, mDrawable.getColor()));
			} else if (index == R.styleable.Ui_ProgressBar_uiColorsProgress) {
				final int colorsResId = attributes.getResourceId(index, -1);
				if (colorsResId > 0 && !isInEditMode()) {
					mDrawable.setColors(context.getResources().getIntArray(colorsResId));
				}
			} else if (index == R.styleable.Ui_ProgressBar_uiMultiColored) {
				mDrawable.setMultiColored(attributes.getBoolean(index, mDrawable.isMultiColored()));
			} else if (index == R.styleable.Ui_ProgressBar_uiColorProgressBackground) {
				mDrawable.setBackgroundColor(attributes.getInt(index, Color.TRANSPARENT));
			} else if (index == R.styleable.Ui_ProgressBar_android_thickness) {
				mDrawable.setThickness(attributes.getDimensionPixelSize(index, 0));
			} else if (index == R.styleable.Ui_ProgressBar_uiRounded) {
				mDrawable.setRounded(!isInEditMode() && attributes.getBoolean(index, mDrawable.isRounded()));
			} else if (index == R.styleable.Ui_ProgressBar_uiIndeterminateSpeed) {
				mDrawable.setIndeterminateSpeed(attributes.getFloat(index, 1));
			}
		}
		mDrawable.setInEditMode(isInEditMode());
		this.applyProgressTint();
		this.applyIndeterminateTint();
		this.applyProgressBackgroundTint();
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
		if (typedArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressTint)) {
			mTintInfo.progressTintList = typedArray.getColorStateList(R.styleable.Ui_ProgressBar_uiProgressTint);
		}
		if (typedArray.hasValue(R.styleable.Ui_ProgressBar_uiIndeterminateTint)) {
			mTintInfo.indeterminateTintList = typedArray.getColorStateList(R.styleable.Ui_ProgressBar_uiIndeterminateTint);
		}
		if (typedArray.hasValue(R.styleable.Ui_ProgressBar_uiProgressBackgroundTint)) {
			mTintInfo.backgroundTintList = typedArray.getColorStateList(R.styleable.Ui_ProgressBar_uiProgressBackgroundTint);
		}
		mTintInfo.progressTintMode = TintManager.parseTintMode(
				typedArray.getInt(R.styleable.Ui_ProgressBar_uiProgressTintMode, 0),
				PorterDuff.Mode.SRC_IN
		);
		mTintInfo.indeterminateTintMode = TintManager.parseTintMode(
				typedArray.getInt(R.styleable.Ui_ProgressBar_uiIndeterminateTintMode, 0),
				PorterDuff.Mode.SRC_IN
		);
		mTintInfo.backgroundTintMode = TintManager.parseTintMode(
				typedArray.getInt(R.styleable.Ui_ProgressBar_uiProgressBackgroundTintMode, 0),
				PorterDuff.Mode.SRC_IN
		);
		// If there is no tint mode specified within style/xml do not tint at all.
		if (mTintInfo.backgroundTintMode == null) mTintInfo.backgroundTintList = null;
		if (mTintInfo.progressTintMode == null) mTintInfo.progressTintList = null;
		if (mTintInfo.indeterminateTintMode == null) mTintInfo.indeterminateTintList = null;
		mTintInfo.hasBackgroundTintList = mTintInfo.backgroundTintList != null;
		mTintInfo.hasBackgroundTinMode = mTintInfo.backgroundTintMode != null;
		mTintInfo.hasProgressTintList = mTintInfo.progressTintList != null;
		mTintInfo.hasProgressTintMode = mTintInfo.progressTintMode != null;
		mTintInfo.hasIndeterminateTintList = mTintInfo.indeterminateTintList != null;
		mTintInfo.hasIndeterminateTintMode = mTintInfo.indeterminateTintMode != null;
	}

	/**
	 * Ensures that the tint info object is initialized.
	 */
	private void ensureTintInfo() {
		if (mTintInfo == null) this.mTintInfo = new TintInfo();
	}

	/**
	 * Invoked to attach progress drawable to this progress bar. This is invoked when this progress
	 * bar is being first time created before parsing of any values from AttributeSet.
	 */
	abstract void onAttachDrawable();

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(BaseProgressBar.class.getName());
		event.setItemCount(mMax);
		event.setCurrentItemIndex(mProgress);
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(BaseProgressBar.class.getName());
	}

	/**
	 * Starts an indeterminate animation session (if not running already) for the progress
	 * drawable with graphics explosion if requested. Use this method to start the indeterminate
	 * animation if you stopped it before by {@link #stopIndeterminate()} or
	 * {@link #stopIndeterminateImmediate()}.
	 */
	public void startIndeterminate() {
		if (canStartIndeterminate()) mDrawable.start();
	}

	/**
	 * Checks whether we can start indeterminate animation on the current progress drawable or not.
	 *
	 * @return {@code True} if the drawable is available and it is not running currently any animation
	 * and the current mode supports indeterminate animation, {@code false} otherwise.
	 */
	private boolean canStartIndeterminate() {
		return mDrawable != null &&
				mMode != DETERMINATE &&
				!mDrawable.isRunning() &&
				(mPrivateFlags & PrivateFlags.PFLAG_ATTACHED_TO_WINDOW) != 0;
	}

	/**
	 * Stops the current (if running) indeterminate animation session for the progress drawable.
	 *
	 * @see ProgressDrawable#stopImmediate()
	 */
	public void stopIndeterminateImmediate() {
		if (canStopIndeterminate()) {
			mDrawable.stopImmediate();
		}
	}

	/**
	 * Like {@link #stopIndeterminateImmediate()}, but this will wait until the progress drawable
	 * finishes drawing of its indeterminate graphics and than stops its animation session.
	 *
	 * @see ProgressDrawable#stop()
	 */
	public void stopIndeterminate() {
		if (canStopIndeterminate()) mDrawable.stop();
	}

	/**
	 * Checks whether we can stop current indeterminate animation running on the current progress
	 * drawable or not.
	 *
	 * @return {@code True} if the drawable is available and it is running currently indeterminate
	 * animation and the current mode supports indeterminate animation, {@code false} otherwise.
	 */
	private boolean canStopIndeterminate() {
		return mDrawable != null && mMode != DETERMINATE && mDrawable.isRunning();
	}

	/**
	 * Delegate method for {@link ProgressDrawable#explode()}.
	 */
	public void explodeProgress() {
		if (mDrawable != null) {
			mDrawable.setExploded(false);
			mDrawable.explode();
		}
	}

	/**
	 * Delegate method for {@link ProgressDrawable#implode()}.
	 */
	public void implodeProgress() {
		if (mDrawable != null) {
			mDrawable.setExploded(true);
			mDrawable.implode();
		}
	}

	/**
	 * Delegate method for {@link ProgressDrawable#setExploded(boolean)}.
	 */
	public void setProgressExploded(boolean exploded) {
		if (mDrawable != null) mDrawable.setExploded(exploded);
	}

	/**
	 * Delegate method for {@link ProgressDrawable#isExploded()}.
	 */
	public boolean isProgressExploded() {
		return mDrawable != null && mDrawable.isExploded();
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mDrawable != null) mDrawable.jumpToCurrentState();
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void drawableHotspotChanged(float x, float y) {
		super.drawableHotspotChanged(x, y);
		if (mDrawable != null) mDrawable.setHotspot(x, y);
	}

	/**
	 */
	@Override
	public void onStarted(@NonNull ProgressDrawable drawable) {
		if (mProgressAnimationListener != null)
			mProgressAnimationListener.onStarted(this, drawable);
	}

	/**
	 */
	@Override
	public void onStopped(@NonNull ProgressDrawable drawable) {
		if (mProgressAnimationListener != null)
			mProgressAnimationListener.onStopped(this, drawable);
	}

	/**
	 */
	@Override
	public void onExploded(@NonNull ProgressDrawable drawable) {
		if (mProgressExplodeAnimationListener != null)
			mProgressExplodeAnimationListener.onExploded(this, drawable);
	}

	/**
	 */
	@Override
	public void onImploded(@NonNull ProgressDrawable drawable) {
		if ((mPrivateFlags & PFLAG_STOP_INDETERMINATE_AFTER_IMPLOSION) != 0) {
			this.updatePrivateFlags(PFLAG_STOP_INDETERMINATE_AFTER_IMPLOSION, false);
			drawable.stopImmediate();
		}
		if (mProgressExplodeAnimationListener != null) {
			mProgressExplodeAnimationListener.onImploded(this, drawable);
		}
	}

	/**
	 * Registers a callback to be invoked whenever a new animation session is <b>started</b> or
	 * the current one is <b>stopped</b> for the progress drawable attached to this progress bar.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	public void setOnProgressAnimationListener(@Nullable OnProgressAnimationListener listener) {
		this.mProgressAnimationListener = listener;
	}

	/**
	 * Registers a callback to be invoked whenever <b>explode</b> or <b>implode</b> animation is finished
	 * for the progress drawable attached to this progress bar.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	public void setOnProgressExplodeAnimationListener(@Nullable OnProgressExplodeAnimationListener listener) {
		this.mProgressExplodeAnimationListener = listener;
	}

	/**
	 * Applies a tint to the progress graphics of the drawable, if specified. This call does not modify
	 * the current tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setDrawable(ProgressDrawable)} will automatically mutate the drawable
	 * and apply the specified tint and tint mode using {@link ProgressDrawable#setProgressTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see R.attr#uiProgressTint ui:uiProgressTint
	 * @see #getProgressTintList()
	 * @see ProgressDrawable#setProgressTintList(android.content.res.ColorStateList)
	 */
	public void setProgressTintList(@Nullable ColorStateList tint) {
		this.ensureTintInfo();
		mTintInfo.progressTintList = tint;
		mTintInfo.hasProgressTintList = true;
		this.applyProgressTint();
	}

	/**
	 * Returns the tint applied to the progress graphics of the progress drawable, if specified.
	 *
	 * @return The progress graphics tint.
	 * @see #setProgressTintList(android.content.res.ColorStateList)
	 */
	@Nullable
	public ColorStateList getProgressTintList() {
		return mTintInfo != null ? mTintInfo.progressTintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setProgressTintList(android.content.res.ColorStateList)}}
	 * to the progress graphics of the progress drawable. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see R.attr#uiProgressTintMode ui:uiProgressTintMode
	 * @see #getProgressTintMode()
	 * @see ProgressDrawable#setProgressTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureTintInfo();
		mTintInfo.progressTintMode = tintMode;
		mTintInfo.hasProgressTintMode = true;
		this.applyProgressTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the progress graphics of the progress
	 * drawable, if specified.
	 *
	 * @return The progress graphics blending mode used to apply the tint.
	 * @see #setProgressTintMode(android.graphics.PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getProgressTintMode() {
		return mTintInfo != null ? mTintInfo.tintMode : null;
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the progress graphics of the current progress
	 * drawable.
	 *
	 * @return {@code True} if the tint has been applied or cleared, {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	private boolean applyProgressTint() {
		this.applyProgressBackgroundTint();
		if (mTintInfo == null ||
				(!mTintInfo.hasProgressTintList && !mTintInfo.hasProgressTintMode) ||
				mDrawable == null) {
			return false;
		}
		mDrawable = (D) mDrawable.mutate();
		if (mTintInfo.hasProgressTintList) {
			mDrawable.setProgressTintList(mTintInfo.progressTintList);
		}
		if (mTintInfo.hasProgressTintMode) {
			mDrawable.setProgressTintMode(mTintInfo.progressTintMode);
		}

		if (mDrawable.isStateful()) {
			mDrawable.setState(getDrawableState());
		}
		return true;
	}

	/**
	 * Applies a tint to the indeterminate graphics of the drawable, if specified. This call does not
	 * modify the current tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setDrawable(ProgressDrawable)} will automatically mutate the drawable
	 * and apply the specified tint and tint mode using {@link ProgressDrawable#setIndeterminateTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see R.attr#uiIndeterminateTint ui:uiIndeterminateTint
	 * @see #getIndeterminateTintList()
	 * @see ProgressDrawable#setIndeterminateTintList(android.content.res.ColorStateList)
	 */
	public void setIndeterminateTintList(@Nullable ColorStateList tint) {
		this.ensureTintInfo();
		mTintInfo.indeterminateTintList = tint;
		mTintInfo.hasIndeterminateTintList = true;
		this.applyIndeterminateTint();
	}

	/**
	 * Returns the tint applied to the indeterminate graphics of the progress drawable, if specified.
	 *
	 * @return The progress graphics tint.
	 * @see #setIndeterminateTintList(android.content.res.ColorStateList)
	 */
	@Nullable
	public ColorStateList getIndeterminateTintList() {
		return mTintInfo != null ? mTintInfo.indeterminateTintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setIndeterminateTintList(android.content.res.ColorStateList)}}
	 * to the indeterminate graphics of the progress drawable. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see R.attr#uiIndeterminateTintMode ui:uiIndeterminateTintMode
	 * @see #getIndeterminateTintMode()
	 * @see ProgressDrawable#setIndeterminateTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setIndeterminateTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureTintInfo();
		mTintInfo.indeterminateTintMode = tintMode;
		mTintInfo.hasIndeterminateTintMode = true;
		this.applyIndeterminateTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the indeterminate graphics of the progress
	 * drawable, if specified.
	 *
	 * @return The indeterminate graphics blending mode used to apply the tint.
	 * @see #setIndeterminateTintMode(android.graphics.PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getIndeterminateTintMode() {
		return mTintInfo != null ? mTintInfo.indeterminateTintMode : null;
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the indeterminate graphics of the current progress
	 * drawable.
	 *
	 * @return {@code True} if the tint has been applied or cleared, {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	private boolean applyIndeterminateTint() {
		this.applyProgressBackgroundTint();
		if (mTintInfo == null ||
				(!mTintInfo.hasIndeterminateTintList && !mTintInfo.hasIndeterminateTintMode) ||
				mDrawable == null) {
			return false;
		}
		this.mDrawable = (D) mDrawable.mutate();
		if (mTintInfo.hasIndeterminateTintList) {
			mDrawable.setIndeterminateTintList(mTintInfo.indeterminateTintList);
		}
		if (mTintInfo.hasIndeterminateTintMode) {
			mDrawable.setIndeterminateTintMode(mTintInfo.indeterminateTintMode);
		}

		if (mDrawable.isStateful()) {
			mDrawable.setState(getDrawableState());
		}
		return true;
	}

	/**
	 * Applies a tint to the background graphics of the drawable, if specified. This call does not
	 * modify the current tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setDrawable(ProgressDrawable)} will automatically mutate the drawable
	 * and apply the specified tint and tint mode using {@link ProgressDrawable#setBackgroundTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see R.attr#uiProgressBackgroundTint ui:uiProgressBackgroundTint
	 * @see #getProgressBackgroundTintList()
	 * @see ProgressDrawable#setBackgroundTintList(android.content.res.ColorStateList)
	 */
	public void setProgressBackgroundTintList(@Nullable ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setBackgroundTintList(tint);
			return;
		}
		this.ensureTintInfo();
		mTintInfo.backgroundTintList = tint;
		mTintInfo.hasBackgroundTintList = true;
		this.applyProgressBackgroundTint();
	}

	/**
	 * Returns the tint applied to the background graphics of the progress drawable, if specified.
	 *
	 * @return The background graphics tint.
	 * @see #setProgressBackgroundTintList(android.content.res.ColorStateList)
	 */
	@Nullable
	public ColorStateList getProgressBackgroundTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getBackgroundTintList();
		}
		return mTintInfo != null ? mTintInfo.backgroundTintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setProgressBackgroundTintList(android.content.res.ColorStateList)}}
	 * to the background graphics of the progress drawable. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see R.attr#uiProgressBackgroundTintMode ui:uiProgressBackgroundTintMode
	 * @see #getProgressBackgroundTintMode()
	 * @see ProgressDrawable#setBackgroundTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setProgressBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setBackgroundTintMode(tintMode);
			return;
		}
		this.ensureTintInfo();
		mTintInfo.backgroundTintMode = tintMode;
		mTintInfo.hasBackgroundTinMode = true;
		this.applyProgressBackgroundTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the background graphics of the progress
	 * drawable, if specified.
	 *
	 * @return The background graphics blending mode used to apply the tint.
	 * @see #setProgressBackgroundTintMode(android.graphics.PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getProgressBackgroundTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getBackgroundTintMode();
		}
		return mTintInfo != null ? mTintInfo.backgroundTintMode : null;
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the progress background graphics of the current
	 * progress drawable.
	 *
	 * @return {@code True} if the tint has been applied or cleared, {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	private boolean applyProgressBackgroundTint() {
		if (mTintInfo == null ||
				(!mTintInfo.hasBackgroundTintList && !mTintInfo.hasBackgroundTinMode) ||
				mDrawable == null) {
			return false;
		}
		this.mDrawable = (D) mDrawable.mutate();
		if (mTintInfo.hasBackgroundTintList) {
			mDrawable.setBackgroundTintList(mTintInfo.backgroundTintList);
		}
		if (mTintInfo.hasBackgroundTinMode) {
			mDrawable.setBackgroundTintMode(mTintInfo.backgroundTintMode);
		}
		if (mDrawable.isStateful()) {
			mDrawable.setState(getDrawableState());
		}
		return true;
	}

	/**
	 * Sets the drawable used to draw a progress or an indeterminate graphics of this progress bar.
	 * Whether this progress bar draws the progress or indeterminate graphics depends on its current
	 * mode specified by {@link #setProgressMode(int)}.
	 * <p>
	 * <b>Note</b>, that the specified drawable and its appearance can be updated directly by accessing
	 * it, using {@link #getDrawable()}, but there are some methods which are delegated by this progress
	 * bar to its attached drawable and should be called upon this progress bar like {@link #setProgress(int)}
	 * or {@link #setProgressMode(int)}. See {@link BaseProgressBar class} overview for more info.
	 *
	 * @param drawable The desired progress drawable. May be {@code null} to clear the current drawable.
	 */
	public void setDrawable(@Nullable D drawable) {
		if (mDrawable != drawable) {
			if (mDrawable != null) {
				mDrawable.setCallback(null);
				mDrawable.setAnimationCallback(null);
				mDrawable.setExplodeAnimationCallback(null);
				unscheduleDrawable(mDrawable);
			}
			if (drawable != null) {
				drawable.setCallback(this);
				drawable.setVisible(getVisibility() == View.VISIBLE, false);
				if (mDrawableWidth != drawable.getIntrinsicWidth() ||
						mDrawableHeight != drawable.getIntrinsicHeight()) {
					this.mDrawableWidth = drawable.getIntrinsicWidth();
					this.mDrawableHeight = drawable.getIntrinsicHeight();
					requestLayout();
				}
			} else {
				mDrawableWidth = mDrawableHeight = 0;
				requestLayout();
			}
			if ((mDrawable = drawable) != null) {
				onSetUpDrawable(mDrawable);
			}
		}
	}

	/**
	 * Called right after the new progress drawable has been attached/set to this progress bar to
	 * set up its initial parameters.
	 *
	 * @param drawable The attached progress drawable to set up.
	 */
	void onSetUpDrawable(@NonNull D drawable) {
		drawable.setMax(mMax);
		drawable.setMode(mMode);
		drawable.setProgress(mProgress);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			drawable.setLayoutDirection(getLayoutDirection());
		}
		this.applyProgressTint();
		this.applyIndeterminateTint();
	}

	/**
	 * Returns the drawable used to draw a progress or an indeterminate graphics of this progress bar
	 * depends on its current mode.
	 *
	 * @return An instance of {@link ProgressDrawable} or {@code null} if the drawable has been
	 * removed by passing {@code null} to {@link #setDrawable(ProgressDrawable)}.
	 * @see #setDrawable(ProgressDrawable)
	 * @see #setProgressMode(int)
	 */
	@Nullable
	public D getDrawable() {
		return mDrawable;
	}

	/**
	 * Delegate method for {@link ProgressDrawable#setMode(int)}.
	 *
	 * @param mode The desired progress mode.
	 * @see #getProgressMode()
	 */
	public void setProgressMode(int mode) {
		if (mMode != mode) changeMode(mode);
	}

	/**
	 * Returns the current progress mode of this progress bar.
	 *
	 * @return Current progress mode.
	 * @see #setProgressMode(int)
	 */
	public int getProgressMode() {
		if (mDrawable != null) this.mMode = mDrawable.getMode();
		return mMode;
	}

	/**
	 * Restart the current mode. This will stop all running progress animations (if any) and starts
	 * them again.
	 * <p>
	 * <b>Note</b>, that none <b>INDETERMINATE</b> mode, this will also clear the current progress
	 * and will set it to {@code 0}.
	 */
	public void restartMode() {
		if ((mPrivateFlags & PrivateFlags.PFLAG_ATTACHED_TO_WINDOW) != 0) {
			onRestartMode(mMode);
		}
	}

	/**
	 * Sets the current value of progress displayed by this progress bar. Does nothing if the current
	 * mode is <b>INDETERMINATE</b>.
	 * <p>
	 * <b>Note</b>, that it is allowed to call this method also from the background thread.
	 *
	 * @param progress The desired progress value. Should be from the range {@code [0, getMax()]}.
	 * @see android.R.attr#progress android:progress
	 * @see #getProgress()
	 * @see #getMax()
	 * @see #setProgressMode(int)
	 */
	@AnyThread
	public synchronized void setProgress(int progress) {
		if (mMode != INDETERMINATE && mProgress != progress && progress >= 0 && progress <= mMax) {
			this.refreshProgress(android.R.id.progress, mProgress = progress);
		}
	}

	/**
	 * Returns the current value of progress displayed by this progress bar.
	 *
	 * @return Current progress value from the range {@code [0, getMax()]} or {@code 0} if the
	 * current mode is <b>INDETERMINATE</b>.
	 * @see #setProgress(int)
	 */
	public synchronized int getProgress() {
		if (mDrawable != null) this.mProgress = mDrawable.getProgress();
		return mMode != INDETERMINATE ? mProgress : 0;
	}

	/**
	 * Delegate method for {@link ProgressDrawable#setMax(int)}.
	 */
	public synchronized void setMax(int max) {
		if (mMax != max) {
			this.mMax = max;
			if (mDrawable != null) {
				mDrawable.setMax(max);
			}
			this.refreshProgress(android.R.id.progress, mProgress);
		}
	}

	/**
	 * Delegate method for {@link ProgressDrawable#getMax()}.
	 */
	public int getMax() {
		return mMax;
	}

	/**
	 */
	@Override
	public void setVisibility(int visibility) {
		final boolean changed = visibility != getVisibility();
		super.setVisibility(visibility);
		if (changed) handleVisibilityChange(visibility == VISIBLE);
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void setLayoutDirection(int layoutDirection) {
		super.setLayoutDirection(layoutDirection);
		if (mDrawable != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mDrawable.setLayoutDirection(getLayoutDirection());
		}
	}

	/**
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.updatePrivateFlags(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW, true);
		mDrawable.setAnimationCallback(this);
		mDrawable.setExplodeAnimationCallback(this);
		if (getVisibility() == VISIBLE) {
			switch (mMode) {
				case INDETERMINATE:
					startIndeterminate();
					break;
			}
		}
	}

	/**
	 */
	@Override
	protected void onDetachedFromWindow() {
		stopIndeterminate();
		mDrawable.setAnimationCallback(null);
		mDrawable.setExplodeAnimationCallback(null);
		this.updatePrivateFlags(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW, false);
		super.onDetachedFromWindow();
	}

	/**
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int width = mDrawable.getIntrinsicWidth();
		int height = mDrawable.getIntrinsicHeight();

		// Take into count also padding.
		width += getPaddingLeft() + getPaddingRight();
		height += getPaddingTop() + getPaddingBottom();

		switch (widthMode) {
			case MeasureSpec.AT_MOST:
				width = Math.min(width, widthSize);
				break;
			case MeasureSpec.EXACTLY:
				width = widthSize;
				break;
			case MeasureSpec.UNSPECIFIED:
			default:
				break;
		}

		switch (heightMode) {
			case MeasureSpec.AT_MOST:
				height = Math.min(height, heightSize);
				break;
			case MeasureSpec.EXACTLY:
				height = heightSize;
				break;
			case MeasureSpec.UNSPECIFIED:
			default:
				break;
		}

		// Check also against minimum size.
		setMeasuredDimension(
				Math.max(width, getSuggestedMinimumWidth()),
				Math.max(height, getSuggestedMinimumHeight())
		);
	}

	/**
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mDrawable != null) {
			mDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
		}
	}

	/**
	 */
	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mDrawable || super.verifyDrawable(who);
	}

	/**
	 */
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mDrawable != null) mDrawable.setState(getDrawableState());
	}

	/**
	 */
	@Override
	protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (mDrawable != null)
			handleVisibilityChange(visibility == VISIBLE && getVisibility() == VISIBLE);
	}

	/**
	 * Handles change in visibility of this view. This will stop all animations if the specified
	 * <var>visible</var> flag is {@code false}, otherwise it will start indeterminate animations
	 * if the current mode is not <b>DETERMINATE</b>.
	 *
	 * @param visible {@code True} if this progress bar is visible, {@code false} otherwise..
	 */
	void handleVisibilityChange(boolean visible) {
		if (mDrawable != null) {
			mDrawable.setVisible(visible, false);
		}
		switch (mMode) {
			case INDETERMINATE:
				if (visible) {
					startIndeterminate();
				} else {
					stopIndeterminateImmediate();
				}
				postInvalidate();
				break;
		}
	}

	/**
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mDrawable != null && mDrawable.isVisible()) {
			mDrawable.draw(canvas);
		}
	}

	/**
	 * Changes the current mode of this progress bar. This will invoke {@link #onPreModeChange(int)}
	 * to allow perform some actions before the mode will be changed and than {@link #onModeChange(int)}
	 * will be invoked.
	 *
	 * @param mode The desired mode to be changed.
	 */
	void changeMode(int mode) {
		onPreModeChange(mode);
		if (mDrawable != null) {
			mDrawable.setMode(mode);
		}
		onModeChange(mMode = mode);
	}

	/**
	 * Invoked from {@link #changeMode(int)} before the requested mode is changed.
	 *
	 * @param mode The new mode which will be changed.
	 */
	void onPreModeChange(int mode) {
		switch (mode) {
			case DETERMINATE:
				stopIndeterminate();
				break;
		}
	}

	/**
	 * Invoked from {@link #changeMode(int)} after the requested mode has been changed.
	 *
	 * @param mode The changed mode.
	 */
	void onModeChange(int mode) {
		switch (mode) {
			case INDETERMINATE:
				startIndeterminate();
				break;
		}
	}

	/**
	 * Invoked to restart the current mode.
	 *
	 * @param mode The current mode that should be restarted.
	 */
	void onRestartMode(int mode) {
		if (mDrawable != null && mDrawable.isRunning()) {
			mDrawable.stopImmediate();
		}
		switch (mode) {
			case DETERMINATE:
				setProgress(0);
				break;
			case INDETERMINATE:
				startIndeterminate();
				break;
		}
	}

	/**
	 * Refreshes the current progress value displayed by this progress bar with respect to UI thread,
	 * so this can be also called from the background thread.
	 * <p>
	 * If called from the UI thread, {@link #onRefreshProgress(int, int, boolean)} will be called
	 * immediately, otherwise to refresh progress will be posted runnable.
	 *
	 * @param id       One of {@link android.R.id#progress} or {@link android.R.id#secondaryProgress}.
	 * @param progress The progress value to be refreshed.
	 */
	@AnyThread
	@SuppressWarnings("WrongThread")
	final synchronized void refreshProgress(int id, int progress) {
		if (mUiThreadId == Thread.currentThread().getId()) {
			onRefreshProgress(id, progress, true);
			return;
		}
		if (mRefreshProgressRunnable == null) {
			this.mRefreshProgressRunnable = new RefreshProgressRunnable();
		}
		final RefreshData refreshData = RefreshData.obtain(id, progress);
		mRefreshProgressRunnable.refreshData.add(refreshData);
		if ((mPrivateFlags & PrivateFlags.PFLAG_ATTACHED_TO_WINDOW) != 0 && (mPrivateFlags & PFLAG_REFRESH_PROGRESS_POSTED) == 0) {
			post(mRefreshProgressRunnable);
			this.updatePrivateFlags(PFLAG_REFRESH_PROGRESS_POSTED, true);
		}
	}

	/**
	 * Invoked directly from {@link #refreshProgress(int, int)} if such a method has been called
	 * from the UI thread, otherwise this is invoked from the posted refresh runnable.
	 *
	 * @param id       One of {@link android.R.id#progress} or {@link android.R.id#secondaryProgress}.
	 * @param progress The progress value to be refreshed.
	 * @param notify   {@code True} if this call should be dispatched also as accessibility event,
	 *                 {@code false} otherwise.
	 */
	synchronized void onRefreshProgress(int id, int progress, boolean notify) {
		if (id == android.R.id.progress) {
			if (mDrawable != null) {
				mDrawable.setProgress(progress);
			} else {
				invalidate();
			}
			if (notify) {
				scheduleAccessibilityEventSender();
			}
		}
	}

	/**
	 * Schedules an accessibility event for the changed/selected progress value.
	 */
	private void scheduleAccessibilityEventSender() {
		if (mAccessibilityEventSender == null) {
			mAccessibilityEventSender = new AccessibilityEventSender();
		} else {
			removeCallbacks(mAccessibilityEventSender);
		}
		postDelayed(mAccessibilityEventSender, ACCESSIBILITY_EVENT_DELAY);
	}

	/**
	 */
	@NonNull
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.mode = mMode;
		savedState.progress = mProgress;
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
		changeMode(savedState.mode);
		setProgress(savedState.progress);
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

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link BaseProgressBar}
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
		int mode, progress;

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
			this.mode = source.readInt();
			this.progress = source.readInt();
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(mode);
			dest.writeInt(progress);
		}
	}

	/**
	 * This class holds all data necessary to tint all components of this view.
	 */
	private static final class TintInfo extends Widget.BackgroundTintInfo {

		/**
		 * Color state list used to tint a specific states of the <b>progress</b> graphics of the
		 * progress drawable.
		 */
		ColorStateList progressTintList;

		/**
		 * Flag indicating whether the {@link #progressTintList} has been set or not.
		 */
		boolean hasProgressTintList;

		/**
		 * Blending mode used to apply tint to the <b>progress</b> graphics of the progress drawable.
		 */
		PorterDuff.Mode progressTintMode;

		/**
		 * Flag indicating whether the {@link #progressTintMode} has been set or not.
		 */
		boolean hasProgressTintMode;

		/**
		 * Color state list used to tint a specific states of the <b>indeterminate</b> graphics of the
		 * progress drawable.
		 */
		ColorStateList indeterminateTintList;

		/**
		 * Flag indicating whether the {@link #indeterminateTintList} has been set or not.
		 */
		boolean hasIndeterminateTintList;

		/**
		 * Blending mode used to apply tint to the <b>indeterminate</b> graphics of the progress drawable.
		 */
		PorterDuff.Mode indeterminateTintMode;

		/**
		 * Flag indicating whether the {@link #indeterminateTintMode} has been set or not.
		 */
		boolean hasIndeterminateTintMode;
	}

	/**
	 * Class holding all data necessary to properly refresh progress value from the background thread.
	 */
	private static final class RefreshData {

		/**
		 * Pool of RefreshData objects for better performance.
		 */
		static final Pools.SynchronizedPool<RefreshData> POOL = new Pools.SynchronizedPool<>(25);

		/**
		 * Id of progress to refresh.
		 */
		int id;

		/**
		 * Progress value to be refreshed.
		 */
		int progress;

		/**
		 * Obtains an instance of RefreshData from the pool or creates a new instance if the pool
		 * is currently empty.
		 *
		 * @param id       The id of progress for which to create the new data. One of {@link android.R.id#progress}
		 *                 or {@link android.R.id#secondaryProgress}.
		 * @param progress Value of progress to refresh.
		 * @return Acquired or new instance of RefreshData object with the specified data.
		 */
		static RefreshData obtain(int id, int progress) {
			RefreshData data = POOL.acquire();
			if (data == null) {
				data = new RefreshData();
			}
			data.id = id;
			data.progress = progress;
			return data;
		}

		/**
		 * Recycles this refresh data object by releasing it from the current refresh data pool.
		 */
		void recycle() {
			POOL.release(this);
		}
	}

	/**
	 * Task used to refresh current progress value from the background thread.
	 */
	private final class RefreshProgressRunnable implements Runnable {

		/**
		 * List of refresh data to be processed to properly refresh progress.
		 */
		final List<RefreshData> refreshData = new ArrayList<>();

		/**
		 */
		@Override
		public void run() {
			synchronized (LOCK) {
				if (!refreshData.isEmpty()) {
					final int count = refreshData.size();
					for (int i = 0; i < count; i++) {
						final RefreshData data = refreshData.get(i);
						if (data != null) {
							onRefreshProgress(data.id, data.progress, true);
							data.recycle();
						}
					}
					refreshData.clear();
				}
				updatePrivateFlags(PFLAG_REFRESH_PROGRESS_POSTED, false);
			}
		}
	}

	/**
	 * Task used to post an accessibility event for the changed progress.
	 */
	private class AccessibilityEventSender implements Runnable {

		/**
		 */
		@Override
		public void run() {
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
		}
	}
}
