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
package universum.studios.android.ui.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.Arrays;

import universum.studios.android.ui.UiConfig;

/**
 * This Drawable class represents base implementation for drawables that can draw a progress or an
 * indeterminate graphics. <b>Note</b>, that this drawable class does not draw any progress or
 * indeterminate graphics, such a logic depends on the concrete implementation of the ProgressDrawable
 * like {@link CircularProgressDrawable} or {@link LinearProgressDrawable}.
 * <p>
 * The ProgressDrawable implements base API to support setting of progress value by {@link #setProgress(int)},
 * supplying color for progress/indeterminate graphics by {@link #setColor(int)} or {@link #setColors(int[])}
 * for <b>multicolored</b> graphics feature which can be enabled {@link #setMultiColored(boolean)}.
 * <p>
 * All features related to appearance of the ProgressDrawable like changing its alpha value by {@link #setAlpha(int)}
 * or tinting of its graphics by {@link #setTintList(android.content.res.ColorStateList)} and
 * {@link #setTintMode(android.graphics.PorterDuff.Mode)} are supported.
 *
 * <h3>Animating</h3>
 * The ProgressDrawable also provides base implementation to support running of animations within
 * an implementation of this class. For purpose of this documentation, a time interval between call
 * of {@link #start()} and call of {@link #stop()} is called <b>animation session</b> or
 * <b>indeterminate animation session</b> in case of <b>indeterminate</b> mode.
 *
 * <h3>Tinting</h3>
 * This drawable provides extended tinting API via following setters:
 * <ul>
 * <li>{@link #setProgressTintList(android.content.res.ColorStateList)}</li>
 * <li>{@link #setProgressTintMode(android.graphics.PorterDuff.Mode)}</li>
 * <li>{@link #setIndeterminateTintList(android.content.res.ColorStateList)}</li>
 * <li>{@link #setIndeterminateTintMode(android.graphics.PorterDuff.Mode)}</li>
 * <li>{@link #setBackgroundTintList(android.content.res.ColorStateList)}</li>
 * <li>{@link #setBackgroundTintMode(android.graphics.PorterDuff.Mode)}</li>
 * </ul>
 *
 * <h3>Callbacks</h3>
 * <ul>
 * <li>
 * {@link ProgressDrawable.AnimationCallback}
 * <p>
 * This callback can be used to receive callback about <b>started</b> and <b>stopped</b> animation
 * for the ProgressDrawable to which is this callback attached.
 * </li>
 * <li>
 * {@link ProgressDrawable.ExplodeAnimationCallback}
 * <p>
 * This callback can be used to receive callback about <b>exploded</b> or <b>imploded</b> thickness
 * of the ProgressDrawable to which is this callback attached.
 * </li>
 * </ul>
 *
 * @author Martin Albedinsky
 */
public abstract class ProgressDrawable extends Drawable implements Animatable {

	/*
	 * Interface ===================================================================================
	 */

	/**
	 * Listener which can receive callbacks about <b>started</b> or <b>stopped</b> animation session
	 * of progress drawable.
	 */
	public interface AnimationCallback {

		/**
		 * Invoked whenever a new animation session is started for the specified progress <var>drawable</var>.
		 *
		 * @param drawable The progress drawable for which has been requested new animation session
		 *                 by {@link #start()} and the drawable has been before that call in the
		 *                 idle mode.
		 */
		void onStarted(@NonNull ProgressDrawable drawable);

		/**
		 * Invoked whenever the current animation sessions is stopped for the specified progress
		 * <var>drawable</var>.
		 *
		 * @param drawable The progress drawable for which has been stopped its current animation
		 *                 sessions by {@link #stop()} or {@link #stopImmediate()} and the drawable
		 *                 has been before that call in the animation mode.
		 */
		void onStopped(@NonNull ProgressDrawable drawable);
	}

	/**
	 * Listener which can receive callbacks about <b>exploded</b> and <b>imploded</b> thickness of
	 * progress drawable.
	 */
	public interface ExplodeAnimationCallback {

		/**
		 * Invoked whenever an explosion of the specified progress <var>drawable</var> is finished.
		 *
		 * @param drawable The progress drawable for which has been explosion of its thickness
		 *                 finished after {@link #explode()} has been called upon the drawable.
		 */
		void onExploded(@NonNull ProgressDrawable drawable);

		/**
		 * Invoked whenever an implosion of the specified progress <var>drawable</var> is finished.
		 *
		 * @param drawable The progress drawable for which has been implosion of its thickness
		 *                 finished after {@link #implode()} has been called upon the drawable.
		 */
		void onImploded(@NonNull ProgressDrawable drawable);
	}

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "BaseProgressDrawable";

	/**
	 * Base flag for DETERMINATE mode.
	 * <p>
	 * This flag is only for internal purpose, use mode flags specified by implementations of this
	 * progress drawable instead for convenience.
	 */
	public static final int DETERMINATE = 0x01;

	/**
	 * Base flag for INDETERMINATE mode. In this mode a value of progress is not accepted by progress
	 * drawable and call to {@link #setProgress(int)} is ignored.
	 * <p>
	 * This flag is only for internal purpose, use mode flags specified by implementations of this
	 * progress drawable instead for convenience.
	 */
	public static final int INDETERMINATE = 0x02;

	/**
	 * Default maximum value of the progress which can be set to progress drawable.
	 */
	public static final int MAX_PROGRESS = 100;

	/**
	 * Maximum level value that can be set to this progress bar instead progress value.
	 */
	private static int MAX_LEVEL = 10000;

	/**
	 * Boolean flag indicating whether an instance of {@link ProgressDrawable} requires the software
	 * layer to be used by a view that is hosting such progress drawable so its drawing will work
	 * properly.
	 * <p>
	 * Software layer for the view can be requested via {@link android.view.View#setLayerType(int, android.graphics.Paint)}.
	 */
	public static final boolean REQUIRES_SOFTWARE_LAYER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN;

	/**
	 * Update interval for animations running by this drawable in milliseconds.
	 * <p>
	 * Value: <b>60 fps</b>
	 */
	static final long FRAME_UPDATE_INTERVAL = UiConfig.ANIMATION_FRAME_UPDATE_INTERVAL;

	/**
	 * Alpha value of the default background color.
	 */
	static final int BACKGROUND_COLOR_ALPHA = 54;

	/**
	 * Default color for progress drawable's graphics.
	 */
	static final int DEFAULT_COLOR = Color.parseColor("#03a9f4");

	/**
	 * Flag indicating whether this drawable is mutated or not. That's it, if it shares its current
	 * state or not.
	 */
	static final int PFLAG_MUTATED = 0x00000001;

	/**
	 * Flag indicating whether this drawable's progress can be multi colored. That's it, can be drawn
	 * with multiple colors (but one at the time) when in {@link #INDETERMINATE} mode.
	 */
	static final int PFLAG_MULTI_COLORED = 0x00000002;

	/**
	 * Flag indicating whether this drawable is running some animation or not.
	 */
	static final int PFLAG_RUNNING = 0x00000004;

	/**
	 * Flag indicating whether this drawable is expanded (visible in thickness) or not.
	 */
	static final int PFLAG_EXPLODED = 0x00000008;

	/**
	 * Flag indicating whether the progress of this drawable should be rounded or not.
	 */
	static final int PFLAG_ROUNDED = 0x00000010;

	/**
	 * Flag indicating whether the indeterminate is being finished or not.
	 */
	static final int PFLAG_FINISHING_INDETERMINATE = 0x00000020;

	/**
	 * Flag indicating whether the graphics of this drawable should be drawn in edit mode or not.
	 *
	 * @see View#isInEditMode()
	 */
	static final int PFLAG_IN_EDIT_MODE = 0x00000040;

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Task to update current data of this drawable. This task should be used primary for update of
	 * indeterminate data.
	 */
	final Runnable UPDATE = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			if (onUpdate()) {
				invalidateSelf();
				scheduleSelf(this, computeFramesScheduleTime());
			}
		}
	};

	/**
	 * Task used to explode thickness of this progress drawable after {@link #explode()} has been
	 * called.
	 */
	private final Runnable EXPLODE_THICKNESS = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			if (mProgressState.useThickness < mProgressState.rawThickness) {
				final float update = computeExplodeImplodeUpdate();
				if (onThicknessChange(mProgressState.useThickness += update)) {
					invalidateSelf();
				}
				scheduleSelf(this, computeFramesScheduleTime());
			} else {
				onExploded(true);
			}
		}
	};

	/**
	 * Task used to implode thickness of this progress drawable after {@link #implode()} has been
	 * called.
	 */
	private final Runnable IMPLODE_THICKNESS = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			if (mProgressState.useThickness > 0) {
				final float update = computeExplodeImplodeUpdate();
				if (onThicknessChange(mProgressState.useThickness -= update)) {
					invalidateSelf();
				}
				scheduleSelf(this, computeFramesScheduleTime());
			} else {
				onExploded(false);
			}
		}
	};

	/**
	 * Paint used to draw graphics (background if any, progress, ...) of this drawable.
	 */
	final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

	/**
	 * Current maximum for progress which can be set to this progress drawable.
	 */
	float mMax = MAX_PROGRESS;

	/**
	 * Current progress drawn by this drawable if it is in {@link #DETERMINATE}.
	 */
	int mProgress;

	/**
	 * Tint filter used to tint background graphics of this drawable.
	 */
	PorterDuffColorFilter mBackgroundTintFilter;

	/**
	 * Tint filter used to tint progress graphics of this drawable.
	 */
	PorterDuffColorFilter mProgressTintFilter;

	/**
	 * Tint filter used to tint indeterminate graphics of this drawable.
	 */
	PorterDuffColorFilter mIndeterminateTintFilter;

	/**
	 * Current mode of this drawable. This mode determines behaviour of this drawable and also how
	 * how should by this drawable drawn.
	 */
	int mMode = DETERMINATE;

	/**
	 * Bounds set to this drawable by {@link #setBounds(int, int, int, int)} or {@link #setBounds(android.graphics.Rect)}.
	 */
	Rect mBounds = new Rect();

	/**
	 * Interpolator used to interpolate computed update values for indeterminate mode.
	 */
	Interpolator mIndeterminateInterpolator = new AccelerateDecelerateInterpolator();

	/**
	 * Index of the color used to draw this drawbale's progress when it is in the {@link #INDETERMINATE}.
	 * mode. This index is used when this drawable is set to behave like multi colored drawable
	 * ({@link #PFLAG_MULTI_COLORED}), so it uses set of specified colors (set by {@link #setColors(int[])},
	 * to draw its current progress.
	 * <p>
	 * How the colors set is used really depends on the implementation, but basic logic is to use
	 * next color from the set for the "next" indeterminate block of this drawable.
	 */
	private int mCurrentColorIndex;

	/**
	 * Set of private flags of this progress drawable.
	 */
	int mPrivateFlags = PFLAG_EXPLODED;

	/**
	 * Duration used for the thickness explode/implode animation.
	 */
	private long mExplodeDuration = 300;

	/**
	 * Current state of this drawable. Updating and managing of this state (its values) relies on
	 * the specific implementation of this drawable.
	 */
	private ProgressState mProgressState;

	/**
	 * Listener to receive callbacks about started and stopped animation session of this progress
	 * drawable.
	 */
	private AnimationCallback mAnimationCallback;

	/**
	 * Listener to receive callbacks about exploded and imploded thickness of this progress drawable.
	 */
	private ExplodeAnimationCallback mExplodeAnimationCallback;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ProgressDrawable.
	 */
	protected ProgressDrawable() {
		ensureConstantState(null);
	}

	/**
	 * Creates a new instance of ProgressDrawable with the specified <var>color</var>.
	 *
	 * @param color The color used to draw progress or indeterminate graphics of the new drawable.
	 */
	protected ProgressDrawable(int color) {
		ensureConstantState(null);
		setColor(color);
	}

	/**
	 * Creates a new instance of ProgressDrawable with the specified set of <var>colors</var>.
	 *
	 * @param colors The set of colors used to draw indeterminate graphics of the new drawable when
	 *               in <b>INDETERMINATE</b> mode.
	 */
	protected ProgressDrawable(int[] colors) {
		ensureConstantState(null);
		setColors(colors);
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Ensures that the constant state of this progress drawable is initialized.
	 *
	 * @param state A current state which should be used to initialize new one.
	 */
	abstract void ensureConstantState(ProgressState state);

	/**
	 * Sets a boolean flag determining whether this drawable should draw its graphics in edit (preview)
	 * mode or not.
	 *
	 * @param inEditMode {@code True} to draw graphics in edit mode, {@code false} otherwise.
	 * @see #isInEditMode()
	 * @see View#isInEditMode()
	 */
	public final void setInEditMode(boolean inEditMode) {
		if (hasPrivateFlag(PFLAG_IN_EDIT_MODE) != inEditMode) {
			this.updatePrivateFlags(PFLAG_IN_EDIT_MODE, inEditMode);
			invalidateSelf();
		}
	}

	/**
	 * Returns flag indicating whether this drawable draws its graphics in edit mode or not.
	 *
	 * @return {@code True} if graphics is drawn in edit (preview) mode, {@code false} otherwise.
	 */
	public final boolean isInEditMode() {
		return hasPrivateFlag(PFLAG_IN_EDIT_MODE);
	}

	/**
	 */
	@Override
	public void draw(Canvas canvas) {
		if (mProgressState.useThickness > 0) {
			final ColorFilter colorFilter = PAINT.getColorFilter();
			if ((mProgressState.drawColor >>> 24) != 0 || colorFilter != null || mProgressTintFilter != null || mIndeterminateTintFilter != null) {
				// Draw background whenever we have none-transparent color for it.
				if ((mProgressState.backgroundDrawColor >>> 24) != 0 || mBackgroundTintFilter != null) {
					if (colorFilter == null && mBackgroundTintFilter != null) {
						PAINT.setColorFilter(mBackgroundTintFilter);
					}

					PAINT.setColor(mProgressState.backgroundDrawColor);
					onDrawBackground(canvas, PAINT, colorFilter);
					// Restore paint's original color filter.
					PAINT.setColorFilter(colorFilter);
				}

				PAINT.setColor(mProgressState.drawColor);
				onDraw(canvas, PAINT, colorFilter);
				// Restore paint's original color filter.
				PAINT.setColorFilter(colorFilter);
			}
		}
	}

	/**
	 * Invoked whenever {@link #draw(android.graphics.Canvas)} is called for this progress drawable
	 * and there is background color set and its alpha value is not {@code 0}.
	 * <p>
	 * <b>Note</b>, that any color filter set to the given <var>paint</var> will be clear after this
	 * call.
	 *
	 * @param canvas      Canvas on which to draw background graphics.
	 * @param paint       A paint that can be used for drawing operations. This paint has already
	 *                    tint filter specified, based on {@link #setBackgroundTintList(android.content.res.ColorStateList)}
	 *                    and {@link #setBackgroundTintMode(android.graphics.PorterDuff.Mode)} parameters.
	 * @param colorFilter Color filter obtained from the specified paint.
	 */
	protected void onDrawBackground(@NonNull Canvas canvas, @NonNull Paint paint, @Nullable ColorFilter colorFilter) {
	}

	/**
	 * Invoked whenever {@link #draw(android.graphics.Canvas)} is called for this progress drawable
	 * and the current alpha value of the current color is not {@code 0}.
	 * <p>
	 * <b>Note</b>, that any color filter set to the given <var>paint</var> will be clear after this
	 * call.
	 *
	 * @param canvas      Canvas on which to draw progress or indeterminate graphics.
	 * @param paint       A paint that can be used for drawing operations.
	 * @param colorFilter Color filter obtained from the specified paint.
	 */
	protected abstract void onDraw(@NonNull Canvas canvas, @NonNull Paint paint, @Nullable ColorFilter colorFilter);

	/**
	 * Starts a new animation session of this progress drawable. The new session is started only if
	 * this drawable does not have animation session running at this time and its current mode is not
	 * <b>DETERMINATE</b>.
	 * <p>
	 * Do not forget to stop the animation session by {@link #stop()} or {@link #stopImmediate()}
	 * whenever this drawable is no longer visible or drawn.
	 */
	@Override
	public void start() {
		if (mMode != DETERMINATE && (mPrivateFlags & PFLAG_RUNNING) == 0) onStart();
	}

	/**
	 * Stops the current animation session of this progress drawable. The sessions is stopped only
	 * if this drawable does have animation session running at this time and its current mode is not
	 * <b>DETERMINATE</b>.
	 */
	public void stopImmediate() {
		if ((mPrivateFlags & PFLAG_RUNNING) != 0) onStopImmediate();
	}

	/**
	 * Like {@link #stopImmediate()}, but this will stop the current animation session after drawing
	 * of the currently visible indeterminate graphics is finished, so it will not look like the
	 * current graphics just "vanished".
	 */
	@Override
	public void stop() {
		if ((mPrivateFlags & PFLAG_RUNNING) != 0) onStop();
	}

	/**
	 * Returns a flag indicating whether this drawable does have animation sessions currently running
	 * or not.
	 *
	 * @return {@code True} if animation session is running, {@code false} otherwise.
	 */
	@Override
	public boolean isRunning() {
		return mMode != DETERMINATE && (mPrivateFlags & PFLAG_RUNNING) != 0;
	}

	/**
	 * Animates thickness of this progress drawable from {@code 0} to value specified by {@link #setThickness(float)}.
	 * <p>
	 * Does nothing if thickness of this drawable is already exploded.
	 * <p>
	 * <b>Note</b>, that this will not affect in any way currently running animation session started
	 * by {@link #start()}.
	 *
	 * @see #implode()
	 * @see #isExploded()
	 */
	public void explode() {
		if ((mPrivateFlags & PFLAG_EXPLODED) == 0) onExplode();
	}

	/**
	 * Animates thickness of this progress drawable from value specified by {@link #setThickness(float)}
	 * to {@code 0}.
	 * <p>
	 * Does nothing if thickness of this drawable is already imploded.
	 * <p>
	 * <b>Note</b>, that this will not affect in any way currently running animation session started
	 * by {@link #start()}.
	 *
	 * @see #explode()
	 * @see #isExploded()
	 */
	public void implode() {
		if ((mPrivateFlags & PFLAG_EXPLODED) != 0) onImplode();
	}

	/**
	 * Sets a flag indicating whether thickness of this progress drawable is exploded or imploded.
	 * This will also update thickness of this drawable.
	 *
	 * @param exploded {@code True} to set thickness to value specified by {@link #setThickness(float)}
	 *                 {@code false} to {@code 0} so this drawable will be invisible.
	 * @see #isExploded()
	 */
	public void setExploded(boolean exploded) {
		if (hasPrivateFlag(PFLAG_EXPLODED) != exploded) {
			this.updatePrivateFlags(PFLAG_EXPLODED, exploded);
			if (exploded) {
				mProgressState.useThickness = mProgressState.rawThickness;
			} else {
				mProgressState.useThickness = 0;
			}
			if (onExplodedChange(exploded)) {
				invalidateSelf();
			}
		}
	}

	/**
	 * Returns a flag indicating whether thickness of this progress drawable is exploded or imploded.
	 * <p>
	 * <b>Note</b>, that if progress drawable is imploded, it means its current thickness is at {@code 0},
	 * so it is not being drawn.
	 *
	 * @return {@code True} if thickness is value specified by {@link #setThickness(float)},
	 * {@code false}  otherwise.
	 */
	public boolean isExploded() {
		return (mPrivateFlags & PFLAG_EXPLODED) != 0;
	}

	/**
	 * Sets the duration for explode/implode animation of this progress drawable.
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see #getExplodeDuration()
	 * @see #explode()
	 * @see #implode()
	 */
	public void setExplodeDuration(long duration) {
		this.mExplodeDuration = duration;
	}

	/**
	 * Returns the duration used for explode/implode animation.
	 *
	 * @return Animation duration in milliseconds.
	 * @see #setExplodeDuration(long)
	 * @see #explode()
	 * @see #implode()
	 */
	public long getExplodeDuration() {
		return mExplodeDuration;
	}

	/**
	 */
	@Override
	public Drawable mutate() {
		if ((mPrivateFlags & PFLAG_MUTATED) == 0 && super.mutate() == this) {
			ensureConstantState(mProgressState);
			updatePrivateFlags(PFLAG_MUTATED, true);
		}
		return this;
	}

	/**
	 */
	@Override
	public void applyTheme(@NonNull Resources.Theme theme) {
	}

	/**
	 */
	@Override
	public boolean canApplyTheme() {
		return false;
	}

	/**
	 * Sets the maximum value of progress which can be set to this progress drawable by {@link #setProgress(int)}.
	 * <p>
	 * Default value: <b>100</b>
	 *
	 * @param max The desired progress maximum.
	 * @see #getMax()
	 */
	public void setMax(int max) {
		if (mMax != max) {
			this.mMax = Math.max(0, max);
			onProgressChange(mProgress);
		}
	}

	/**
	 * Sets the current maximum of this progress drawable.
	 *
	 * @return The maximum value of progress which can be set by {@link #setProgress(int)}.
	 */
	public int getMax() {
		return (int) mMax;
	}

	/**
	 * @see #setProgress(int)
	 */
	@Override
	protected boolean onLevelChange(int level) {
		return setProgress((int) (level / (float) MAX_LEVEL * mMax));
	}

	/**
	 * Sets the current progress value of this progress drawable which determines size of progress
	 * drawn by this drawable.
	 * <p>
	 * Does nothing if the current mode is <b>INDETERMINATE</b>.
	 *
	 * @param progress The desired progress. Should be from the range {@code [0, getMax()]}.
	 * @return {@code True} if progress has been changed, {@code false} otherwise.
	 * @see #getProgress()
	 */
	public boolean setProgress(int progress) {
		if (mMode != INDETERMINATE && mProgress != progress && progress >= 0 && progress <= mMax) {
			if (onProgressChange(mProgress = progress)) {
				invalidateSelf();
				return true;
			}
		}
		return false;
	}

	/**
	 * Invoked whenever {@link #setProgress(int)} is called and the current progress has been changed.
	 * <p>
	 * This implementation does nothing and returns {@code false}.
	 *
	 * @param progress The currently changed progress.
	 * @return {@code True} if this drawable should be invalidated due to this change, {@code false}
	 * otherwise.
	 */
	protected boolean onProgressChange(int progress) {
		return false;
	}

	/**
	 * Returns the progress value set to this progress drawable by {@link #setProgress(int)}.
	 *
	 * @return Current progress or {@code 0} if the current mode is not <b>DETERMINATE</b>.
	 */
	public int getProgress() {
		return mMode != INDETERMINATE ? mProgress : 0;
	}

	/**
	 * Changes mode of this progress drawable. The progress mode specifies behaviour and drawing
	 * of this drawable and its progress.
	 * <p>
	 * <b>Note</b>, that changing the current mode will not affect in any way currently running
	 * animation session, just drawing behaviour of the progress/indeterminate graphics. This means,
	 * that changing mode to for example <b>INDETERMINATE</b> will not start indeterminate animation
	 * session by {@link #start()}, this is responsibility of the user (view) of this drawable to
	 * start or stop animations properly. This is due to performance, so changing mode will not start
	 * animations at time when the view which hosts this drawable is not attached to window yet.
	 *
	 * @param mode A flag of the desired mode.
	 * @see #getMode()
	 */
	public void setMode(int mode) {
		if (mMode != mode) {
			onPreModeChange(mode);
			if (onModeChange(mMode = mode)) {
				invalidateSelf();
			}
		}
	}

	/**
	 * Invoked whenever {@link #setMode(int)} is called and the current mode should be changed.
	 * <p>
	 * This implementation does nothing.
	 *
	 * @param mode The mode which will be changed after this call.
	 */
	protected void onPreModeChange(int mode) {
	}

	/**
	 * Invoked whenever {@link #setMode(int)} is called and the current mode has been changed.
	 * <p>
	 * This implementation does nothing and returns {@code false}.
	 *
	 * @param mode The currently changed mode.
	 * @return {@code True} if this drawable should be invalidated due to this change, {@code false}
	 * otherwise.
	 */
	protected boolean onModeChange(int mode) {
		return false;
	}

	/**
	 * Returns the current mode of this progress drawable set by {@link #setMode(int)}.
	 *
	 * @return Current mode.
	 * @see #setMode(int)
	 */
	public int getMode() {
		return mMode;
	}

	/**
	 * Sets the color used to draw graphics of this progress drawable.
	 * <p>
	 * <b>Note</b>, that this color is used only if the multicolored mode is disabled. See {@link #setMultiColored(boolean)}
	 * for more info.
	 *
	 * @param color The desired color.
	 * @see #getColor()
	 */
	public void setColor(@ColorInt int color) {
		if (mProgressState.rawColor != color || mProgressState.drawColor != color) {
			mProgressState.color = mProgressState.rawColor = mProgressState.drawColor = color;
			invalidateSelf();
		}
	}

	/**
	 * Returns the color used to draw graphics of this progress drawable.
	 *
	 * @return The current color. This color can be modified whenever {@link #setAlpha(int)} is called.
	 * @see #setColor(int)
	 */
	@ColorInt
	public int getColor() {
		return mProgressState.drawColor;
	}

	/**
	 * Sets the set of colors used to draw graphics of this progress drawable. The given colors
	 * are used only if the mode of this drawable is <b>INDETERMINATE</b> and the multicolored mode
	 * is enabled by {@link #setMultiColored(boolean)}.
	 * <p>
	 * How a specific color is picked from the given set depends on a specific implementation of this
	 * progress drawable, but basically the current color is changed whenever this drawable enters a
	 * next indeterminate "session".
	 *
	 * @param colors The desired set of colors. May be {@code null} but not empty.
	 * @see #getColors()
	 */
	public void setColors(@Nullable int[] colors) {
		if (colors != null && colors.length == 0) {
			throw new IllegalArgumentException("Empty array with colors is not allowed.");
		}

		if (!Arrays.equals(mProgressState.colors, colors)) {
			this.mProgressState.colors = colors;
			if ((mPrivateFlags & PFLAG_MULTI_COLORED) != 0) {
				changeNextColor();
				invalidateSelf();
			}
		}
	}

	/**
	 * Returns the set of colors used to draw graphics of this progress drawable in the
	 * <b>INDETERMINATE</b> mode.
	 *
	 * @return Set of colors or {@code null} if no colors were specified.
	 * @see #setColors(int[])
	 */
	public int[] getColors() {
		return mProgressState.colors;
	}

	/**
	 * Sets a flag indicating whether this progress drawable should use set of colors specified by
	 * {@link #setColors(int[])} to draw its graphics whenever in the <b>INDETERMINATE</b> mode.
	 *
	 * @param multiColored {@code True} to use set of colors, {@code false} to use color specified
	 *                     by {@link #setColor(int)}.
	 * @see #isMultiColored()
	 */
	public void setMultiColored(boolean multiColored) {
		if (hasPrivateFlag(PFLAG_MULTI_COLORED) != multiColored) {
			this.updatePrivateFlags(PFLAG_MULTI_COLORED, multiColored);
			if (!multiColored) {
				changeColor(mProgressState.color);
				if (mMode == INDETERMINATE) {
					changeBackgroundColor(mProgressState.backgroundColor);
				}
			}
		}
	}

	/**
	 * Returns a flag indicating whether this progress drawable uses set of colors or single color
	 * to draw its graphics.
	 *
	 * @return {@code True} if this drawable is multicolored, {@code false} otherwise.
	 * @see #setMultiColored(boolean)
	 */
	public boolean isMultiColored() {
		return (mPrivateFlags & PFLAG_MULTI_COLORED) != 0;
	}

	/**
	 * Sets the color used to draw background of this progress drawable.
	 *
	 * @param color The desired background color.
	 * @see #getBackgroundColor()
	 */
	public void setBackgroundColor(@ColorInt int color) {
		if (mProgressState.backgroundRawColor != color || mProgressState.backgroundDrawColor != color) {
			mProgressState.backgroundColor = mProgressState.backgroundRawColor = mProgressState.backgroundDrawColor = color;
			invalidateSelf();
		}
	}

	/**
	 * Returns the background color of this progress drawable.
	 *
	 * @return Background color. This color is modified whenever {@link #setAlpha(int)} is called.
	 * @see #setBackgroundColor(int)
	 */
	@ColorInt
	public int getBackgroundColor() {
		return mProgressState.backgroundDrawColor;
	}

	/**
	 * Sets the thickness of this progress drawable. The specified thickness can be animated by
	 * {@link #explode()} or {@link #implode()}.
	 * <p>
	 * How the thickness is used when drawing this drawable depends on a specific implementation of
	 * this drawable.
	 *
	 * @param thickness The desired thickness in pixels.
	 * @see #getThickness()
	 */
	public void setThickness(float thickness) {
		if (mProgressState.rawThickness != thickness || mProgressState.useThickness != thickness) {
			mProgressState.rawThickness = mProgressState.useThickness = Math.max(0, thickness);
			if (onThicknessChange(mProgressState.useThickness)) {
				invalidateSelf();
			}
		}
	}

	/**
	 * Invoked whenever {@link #setThickness(float)} is called and the current thickness has been changed.
	 * This will be also invoked during running <b>explode</b> or <b>implode</b> animation.
	 * <p>
	 * This implementation does nothing and returns {@code false}.
	 *
	 * @param thickness The currently changed thickness.
	 * @return {@code True} if this drawable should be invalidated due to this change, {@code false}
	 * otherwise.
	 */
	protected boolean onThicknessChange(float thickness) {
		return false;
	}

	/**
	 * Returns the thickness of this progress drawable.
	 *
	 * @return Current thickness in pixels.
	 * @see #setThickness(float)
	 */
	public float getThickness() {
		return mProgressState.useThickness;
	}

	/**
	 * Sets a flag indicating whether a "shape" of this progress drawable should be rounded or not.
	 *
	 * @param rounded {@code True} to enable rounded feature, {@code false} otherwise.
	 * @see #isRounded()
	 */
	public void setRounded(boolean rounded) {
		if (hasPrivateFlag(PFLAG_ROUNDED) != rounded) {
			this.updatePrivateFlags(PFLAG_ROUNDED, rounded);
			if (onRoundedChange(rounded)) {
				invalidateSelf();
			}
		}
	}

	/**
	 * Invoked whenever {@link #setRounded(boolean)} is called and the current boolean flag for
	 * rounded feature has been changed.
	 * <p>
	 * This implementation does nothing and returns {@code false}.
	 *
	 * @param rounded The current boolean flag for rounded feature.
	 * @return {@code True} if this drawable should be invalidated due to this change, {@code false}
	 * otherwise.
	 */
	protected boolean onRoundedChange(boolean rounded) {
		return false;
	}

	/**
	 * Returns a flag indicating whether a "shape" of this progress drawable is rounded or not.
	 *
	 * @return {@code True} if shape is rounded, {@code false} otherwise.
	 * @see #setRounded(boolean)
	 */
	public boolean isRounded() {
		return (mPrivateFlags & PFLAG_ROUNDED) != 0;
	}

	/**
	 * Sets the interpolator used to interpolate computed update values for indeterminate blocks
	 * whenever animation session for none <b>DETERMINATE</b> mode is running.
	 * <p>
	 * Default interpolator is {@link android.view.animation.AccelerateDecelerateInterpolator}.
	 *
	 * @param interpolator The desired interpolator.
	 * @see #getIndeterminateInterpolator()
	 */
	public void setIndeterminateInterpolator(@NonNull Interpolator interpolator) {
		this.mIndeterminateInterpolator = interpolator;
	}

	/**
	 * Returns the interpolator used whenever the current mode is none <b>DETERMINATE</b> to interpolate
	 * update values for indeterminate blocks.
	 *
	 * @return Indeterminate interpolator.
	 * @see #setIndeterminateInterpolator(android.view.animation.Interpolator)
	 */
	@NonNull
	public Interpolator getIndeterminateInterpolator() {
		return mIndeterminateInterpolator;
	}

	/**
	 * Sets the speed for indeterminate blocks update used whenever the current mode is none
	 * <b>DETERMINATE</b>.
	 * <p>
	 * Default speed is {@code 1f}. This speed should be changed only with a good reason to.
	 *
	 * @param speed The desired speed. Should be from the range {@code [0, infinite)}.
	 * @see #getIndeterminateSpeed()
	 */
	public void setIndeterminateSpeed(float speed) {
		if (mProgressState.indeterminateSpeed != speed) {
			this.mProgressState.indeterminateSpeed = Math.max(0, speed);
		}
	}

	/**
	 * Returns the current speed to speed up indeterminate blocks update.
	 *
	 * @return Current indeterminate speed.
	 * @see #setIndeterminateSpeed(float)
	 */
	public float getIndeterminateSpeed() {
		return mProgressState.indeterminateSpeed;
	}

	/**
	 * Sets the progress color's alpha value.
	 *
	 * @param alpha The desired alpha value to set. Should be from the range [0, 255].
	 */
	@Override
	public void setAlpha(int alpha) {
		alpha += alpha >> 7; // Ensure range [0, 255]
		this.updateDrawingColor((mProgressState.rawColor >>> 24) * alpha >> 8);
		this.updateBackgroundDrawingColor((mProgressState.backgroundRawColor >>> 24) * alpha >> 8);
	}

	/**
	 * Returns the alpha value of this progress drawable's color.
	 *
	 * @return The alpha value in the range [0, 255].
	 */
	@Override
	public int getAlpha() {
		return mProgressState.drawColor >>> 24;
	}

	/**
	 */
	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		PAINT.setColorFilter(colorFilter);
	}

	/**
	 * Groups:
	 * <ul>
	 * <li>{@link #setBackgroundTintList(android.content.res.ColorStateList)}</li>
	 * <li>{@link #setProgressTintList(android.content.res.ColorStateList)}</li>
	 * <li>{@link #setIndeterminateTintList(android.content.res.ColorStateList)}</li>
	 * </ul>
	 * into one call.
	 */
	@Override
	public void setTintList(ColorStateList tint) {
		setBackgroundTintList(tint);
		setProgressTintList(tint);
		setIndeterminateTintList(tint);
	}

	/**
	 * Groups:
	 * <ul>
	 * <li>{@link #setBackgroundTintMode(android.graphics.PorterDuff.Mode)}</li>
	 * <li>{@link #setProgressTintMode(android.graphics.PorterDuff.Mode)}</li>
	 * <li>{@link #setIndeterminateTintMode(android.graphics.PorterDuff.Mode)}</li>
	 * </ul>
	 * into one call.
	 */
	@Override
	public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
		setBackgroundTintMode(tintMode);
		setProgressTintMode(tintMode);
		setIndeterminateTintMode(tintMode);
	}

	/**
	 * Sets a tint for the background graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tint Color state list to use for tinting of the background graphics. May be {@code null}
	 *             to clear the current background tint.
	 */
	public void setBackgroundTintList(@Nullable ColorStateList tint) {
		mProgressState.backgroundTint = tint;
		this.mBackgroundTintFilter = TintDrawable.createTintFilter(this, tint, mProgressState.backgroundTintMode);
		invalidateSelf();
	}

	/**
	 * Sets a tint blending mode for the background graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tintMode The desired Porter-Duff blending mode.
	 */
	public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
		mProgressState.backgroundTintMode = tintMode;
		this.mBackgroundTintFilter = TintDrawable.createTintFilter(this, mProgressState.backgroundTint, tintMode);
		invalidateSelf();
	}

	/**
	 * Sets a tint for the progress graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tint Color state list to use for tinting of the progress graphics. May be {@code null}
	 *             to clear the current progress tint.
	 */
	public void setProgressTintList(@Nullable ColorStateList tint) {
		mProgressState.progressTint = tint;
		this.mProgressTintFilter = TintDrawable.createTintFilter(this, tint, mProgressState.progressTintMode);
		invalidateSelf();
	}

	/**
	 * Sets a tint blending mode for the progress graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tintMode The desired Porter-Duff blending mode.
	 */
	public void setProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
		mProgressState.progressTintMode = tintMode;
		this.mProgressTintFilter = TintDrawable.createTintFilter(this, mProgressState.progressTint, tintMode);
		invalidateSelf();
	}

	/**
	 * Sets a tint for the indeterminate graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tint Color state list to use for tinting of the indeterminate graphics. May be {@code null}
	 *             to clear the current indeterminate tint.
	 */
	public void setIndeterminateTintList(@Nullable ColorStateList tint) {
		mProgressState.indeterminateTint = tint;
		this.mIndeterminateTintFilter = TintDrawable.createTintFilter(this, tint, mProgressState.indeterminateTintMode);
		invalidateSelf();
	}

	/**
	 * Sets a tint blending mode for the indeterminate graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tintMode The desired Porter-Duff blending mode.
	 */
	public void setIndeterminateTintMode(@Nullable PorterDuff.Mode tintMode) {
		mProgressState.indeterminateTintMode = tintMode;
		this.mIndeterminateTintFilter = TintDrawable.createTintFilter(this, mProgressState.indeterminateTint, tintMode);
		invalidateSelf();
	}

	/**
	 */
	@Override
	public int getOpacity() {
		if (mProgressTintFilter != null || mIndeterminateTintFilter != null || PAINT.getColorFilter() != null) {
			return PixelFormat.TRANSLUCENT;
		}
		switch (mProgressState.drawColor >>> 24) {
			case 255:
				return PixelFormat.OPAQUE;
			case 0:
				return PixelFormat.TRANSPARENT;
		}
		return PixelFormat.TRANSLUCENT;
	}

	/**
	 */
	@Override
	public ConstantState getConstantState() {
		return mProgressState;
	}

	/**
	 * Registers a callback to be invoked whenever the animation session for this progress drawable
	 * is started or stopped.
	 *
	 * @param callback Listener callback. {@code Null} is allowed to clear the current callback.
	 */
	public void setAnimationCallback(@Nullable AnimationCallback callback) {
		this.mAnimationCallback = callback;
	}

	/**
	 * Registers a callback to be invoked whenever the thickness of this progress drawable is
	 * exploded or imploded.
	 *
	 * @param callback Listener callback. {@code Null} is allowed to clear the current callback.
	 */
	public void setExplodeAnimationCallback(@Nullable ExplodeAnimationCallback callback) {
		this.mExplodeAnimationCallback = callback;
	}

	/**
	 */
	@Override
	public boolean onLayoutDirectionChanged(int layoutDirection) {
		if (mProgressState.direction != layoutDirection) {
			switch (layoutDirection) {
				case View.LAYOUT_DIRECTION_LTR:
				case View.LAYOUT_DIRECTION_RTL:
					mProgressState.direction = layoutDirection;
					invalidateSelf();
					return true;
			}
		}
		return super.onLayoutDirectionChanged(layoutDirection);
	}

	/**
	 * Returns the layout direction of this progress drawable.
	 *
	 * @return One of {@link android.view.View#LAYOUT_DIRECTION_LTR LAYOUT_DIRECTION_LTR},
	 * {@link android.view.View#LAYOUT_DIRECTION_RTL LAYOUT_DIRECTION_RTL}.
	 */
	public int getLayoutDirection() {
		return mProgressState.direction;
	}

	/**
	 */
	@Override
	protected boolean onStateChange(int[] state) {
		boolean appearanceChange = false;
		if (mProgressState.progressTint != null && mProgressState.progressTintMode != null) {
			this.mProgressTintFilter = TintDrawable.createTintFilter(
					this, mProgressState.progressTint, mProgressState.progressTintMode
			);
			appearanceChange = true;
		}
		if (mProgressState.indeterminateTint != null && mProgressState.indeterminateTintMode != null) {
			this.mIndeterminateTintFilter = TintDrawable.createTintFilter(
					this, mProgressState.indeterminateTint, mProgressState.indeterminateTintMode
			);
			appearanceChange = true;
		}
		if (mProgressState.backgroundTint != null && mProgressState.backgroundTintMode != null) {
			this.mBackgroundTintFilter = TintDrawable.createTintFilter(
					this, mProgressState.backgroundTint, mProgressState.backgroundTintMode
			);
			appearanceChange = true;
		}
		return super.onStateChange(state) || appearanceChange;
	}

	/**
	 */
	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(mBounds = bounds);
	}

	/**
	 * Invoked whenever {@link #setExploded(boolean)} is called and the current boolean flag for
	 * exploded state has been changed.
	 * <p>
	 * This implementation does nothing and returns {@code false}.
	 *
	 * @param exploded The current boolean flag for exploded state.
	 * @return {@code True} if this drawable should be invalidated due to this change, {@code false}
	 * otherwise.
	 */
	protected boolean onExplodedChange(boolean exploded) {
		return false;
	}

	/**
	 * Invoked whenever {@link #start()} is called and this progress drawable does not have animation
	 * session currently running and the current mode is none <b>DETERMINATE</b>.
	 */
	protected abstract void onStart();

	/**
	 * Invoked whenever {@link #stop()} is called and this progress drawable does have animation
	 * sessions currently running and the current mode is none <b>DETERMINATE</b>.
	 */
	protected abstract void onStop();

	/**
	 * Invoked whenever {@link #stopImmediate()} is called and this progress drawable does have animation
	 * sessions currently running and the current mode is none <b>DETERMINATE</b>.
	 */
	protected abstract void onStopImmediate();

	/**
	 * Invoked whenever {@link #explode()} is called and thickness of this progress drawable is not
	 * exploded yet.
	 * <p>
	 * This implementation will schedule updates to increase the current thickness to the value
	 * specified by {@link #setThickness(float)}.
	 */
	protected void onExplode() {
		scheduleSelf(EXPLODE_THICKNESS, 0);
	}

	/**
	 * Invoked whenever {@link #implode()} is called and thickness of this progress drawable is not
	 * imploded yet.
	 * <p>
	 * This implementation will schedule updates to decrease the current thickness to {@code 0}.
	 */
	protected void onImplode() {
		scheduleSelf(IMPLODE_THICKNESS, 0);
	}

	/**
	 * Invoked whenever {@link #UPDATE} task has been scheduled or this method returns {@code true}.
	 *
	 * @return {@code True} to invalidate this drawable and re-schedule the {@link #UPDATE} task so
	 * this method will be invoked again after {@link #FRAME_UPDATE_INTERVAL}, {@code false} to
	 * cancel UPDATE task scheduling loop.
	 */
	boolean onUpdate() {
		return false;
	}

	/**
	 * Notifies the current AnimationCallback (if any), that the animation session of this progress
	 * drawable has been started.
	 */
	final void notifyStarted() {
		updatePrivateFlags(PFLAG_RUNNING, true);
		if (mAnimationCallback != null) {
			mAnimationCallback.onStarted(this);
		}
	}

	/**
	 * Notifies the current AnimationCallback (if any), that the current animation session of this
	 * progress drawable has been stopped.
	 */
	final void notifyStopped() {
		updatePrivateFlags(PFLAG_RUNNING, false);
		if (mAnimationCallback != null) {
			mAnimationCallback.onStopped(this);
		}
	}

	/**
	 * Invoked whenever {@link #explode()} or {@link #implode()} has been called and the related
	 * animation has been finished.
	 * <p>
	 * This will also notifies the current ExplodeAnimationCallback (if any).
	 *
	 * @param exploded {@code True} if thickness has been just now exploded, {@code false} if has
	 *                 been imploded.
	 */
	void onExploded(boolean exploded) {
		this.updatePrivateFlags(PFLAG_EXPLODED, exploded);
		if (mExplodeAnimationCallback != null) {
			if (exploded) {
				mExplodeAnimationCallback.onExploded(this);
			} else {
				mExplodeAnimationCallback.onImploded(this);
			}
		}
	}

	/**
	 * Changes the current background color to the specified one.
	 *
	 * @param color The new background color.
	 */
	void changeBackgroundColor(int color) {
		if (mProgressState.backgroundColor == Color.TRANSPARENT) {
			return;
		}

		if (mProgressState.backgroundRawColor != color || mProgressState.backgroundDrawColor != color) {
			mProgressState.backgroundRawColor = color;
			// Recompute alpha value for the new background color.
			this.updateBackgroundDrawingColor(mProgressState.backgroundDrawColor >>> 24);
		}
	}

	/**
	 * Changes the current color to the specified one.
	 *
	 * @param color The new color.
	 */
	void changeColor(int color) {
		if (mProgressState.rawColor != color || mProgressState.drawColor != color) {
			mProgressState.rawColor = color;
			// Recompute alpha value for the new color.
			this.updateDrawingColor(mProgressState.drawColor >>> 24);
		}
	}

	/**
	 * Updates current drawing color to a color for the current color index for <b>multicolored</b>
	 * mode, or to the raw color set by {@link #setColor(int)}, to ensure that all colors are properly
	 * set including background color.
	 */
	void updateColor() {
		this.mCurrentColorIndex--;
		changeNextColor();
	}

	/**
	 * Changes the next color from the current set of colors (if any) as current color for this
	 * progress drawable.
	 * <p>
	 * If the multicolored mode is disabled, this will reset the current color to the default color
	 * specified by {@link #setColor(int)}.
	 */
	void changeNextColor() {
		if (mProgressState.colors == null) {
			this.mCurrentColorIndex = 0;
			return;
		}

		if ((mPrivateFlags & PFLAG_MULTI_COLORED) != 0) {
			this.mCurrentColorIndex++;
			if (mCurrentColorIndex >= mProgressState.colors.length || mCurrentColorIndex < 0) {
				this.mCurrentColorIndex = 0;
			}
			final int color = mProgressState.colors[mCurrentColorIndex];
			changeColor(color);
			if (mMode == INDETERMINATE) {
				changeBackgroundColor(Color.argb(
						BACKGROUND_COLOR_ALPHA,
						Color.red(color),
						Color.green(color),
						Color.blue(color)
				));
			}
		} else if (mCurrentColorIndex != 0) {
			this.mCurrentColorIndex = 0;
			changeColor(mProgressState.color);
			if (mMode == INDETERMINATE) {
				changeBackgroundColor(mProgressState.backgroundColor);
			}
		}
	}

	/**
	 * Resets the current color of this progress drawable to the default color specified by {@link #setColor(int)}
	 * or to the first color from the colors set specified by {@link #setColors(int[])} depends
	 * on if the multicolored mode is enabled or not.
	 */
	void resetCurrentColor() {
		this.mCurrentColorIndex = -1;
		changeNextColor();
	}

	/**
	 * Changes the current constant state of this progress drawable.
	 *
	 * @param state The new constant state.
	 */
	void changeConstantState(ProgressState state) {
		this.mProgressState = state;
	}

	/**
	 * Updates the given <var>paint</var> in the way, that changes its options in order to enable
	 * or disable rounded feature for the paint.
	 *
	 * @param paint   The paint to update.
	 * @param rounded {@code True} if the paint should support rounded feature so a graphics drawn
	 *                using the paint will have rounded corners, {@code false} to clear the current
	 *                rounded feature.
	 * @return The specified paint with updated <b>stroke join, stroke cap</b> and <b>path effect</b>.
	 */
	Paint updatePaintToRounded(Paint paint, boolean rounded) {
		if (rounded) {
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setPathEffect(new CornerPathEffect(mProgressState.useThickness));
		} else {
			paint.setStrokeJoin(Paint.Join.MITER);
			paint.setStrokeCap(Paint.Cap.BUTT);
			paint.setPathEffect(null);
		}
		return paint;
	}

	/**
	 * Updates the current constant state from the values in the typed array.
	 */
	void updateStateFromTypedArray(TypedArray typedArray) {
		// Cannot be properly implemented.
	}

	/**
	 * Computes value of update for explode/implode task.
	 *
	 * @return Computed update value.
	 */
	final float computeExplodeImplodeUpdate() {
		return mProgressState.rawThickness / (mExplodeDuration / FRAME_UPDATE_INTERVAL);
	}

	/**
	 * Updates the current private flags.
	 *
	 * @param flag Value of the desired flag to add/remove to/from the current private flags.
	 * @param add  Boolean flag indicating whether to add or remove the specified <var>flag</var>.
	 */
	void updatePrivateFlags(int flag, boolean add) {
		if (add) {
			this.mPrivateFlags |= flag;
		} else {
			this.mPrivateFlags &= ~flag;
		}
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

	/**
	 * Computes schedule time for {@link #scheduleSelf(Runnable, long)} method for the specified
	 * <var>delay</var>.
	 *
	 * @param delay The desired delay for the scheduling task.
	 * @return Computed schedule time with the specified delay and {@link android.os.SystemClock#uptimeMillis()}
	 * as base.
	 */
	static long computeScheduleTime(long delay) {
		return SystemClock.uptimeMillis() + delay;
	}

	/**
	 * Computes schedule time for tasks/animations run by this progress drawable in <b>60 fps</b> rate.
	 *
	 * @return Computed schedule time with {@link #FRAME_UPDATE_INTERVAL} delay and {@link android.os.SystemClock#uptimeMillis()}
	 * as base.
	 */
	static long computeFramesScheduleTime() {
		return computeScheduleTime(FRAME_UPDATE_INTERVAL);
	}

	/**
	 * Updates the current color used to draw graphics of this progress drawable.
	 *
	 * @param alpha An alpha value to be updated for the current color.
	 */
	private void updateDrawingColor(int alpha) {
		final int drawColor = (mProgressState.rawColor << 8 >>> 8) | (alpha << 24);
		if (mProgressState.drawColor != drawColor) {
			mProgressState.drawColor = drawColor;
			invalidateSelf();
		}
	}

	/**
	 * Updates the current color used to draw background of this progress drawable.
	 *
	 * @param alpha An alpha value to be updated for the background color.
	 */
	private void updateBackgroundDrawingColor(int alpha) {
		final int drawColor = (mProgressState.backgroundRawColor << 8 >>> 8) | (alpha << 24);
		if (mProgressState.backgroundDrawColor != drawColor) {
			mProgressState.backgroundDrawColor = drawColor;
			invalidateSelf();
		}
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * Constant state implementation for this drawable class.
	 */
	static abstract class ProgressState extends ConstantState {

		/**
		 * Default indeterminate speed used.
		 */
		private static final float DEFAULT_INDETERMINATE_SPEED = 1.0f;

		/**
		 * Multicolored set of colors for indeterminate mode.
		 */
		int[] colors;

		/**
		 * Raw "untouched" color set by {@link #setColor(int)}.
		 */
		int color;

		/**
		 * Current color from multicolored set ({@link #colors}) used to update {@link #drawColor}.
		 */
		int rawColor;

		/**
		 * Color modified by {@link #setAlpha(int)} used to draw progress or indeterminate blocks.
		 */
		int drawColor;

		/**
		 * Raw "untouched" color set by {@link #setBackgroundColor(int)}.
		 */
		int backgroundColor;

		/**
		 * Current color from multicolored set ({@link #colors}) with modified alpha for background
		 * used to update {@link #backgroundDrawColor}.
		 */
		int backgroundRawColor;

		/**
		 * Color modified by {@link #setAlpha(int)} used to draw background.
		 */
		int backgroundDrawColor;

		/**
		 * Thickness of progress drawable's shape set by {@link #setThickness(float)}.
		 */
		float rawThickness;

		/**
		 * Thickness modified during exploding/imploding of progress drawable.
		 */
		float useThickness;

		/**
		 * Speed for the indeterminate updates.
		 * <p>
		 * <b>Note</b>, that this does not influences the FPS for the running indeterminate animation,
		 * but rather the ratio for the update deltas for a start and sweep angle of the progress arc.
		 */
		float indeterminateSpeed = DEFAULT_INDETERMINATE_SPEED;

		/**
		 * Direction in which to draw progress drawable's graphics.
		 */
		int direction;

		/**
		 * Color state list used to apply tint to the background graphics.
		 */
		ColorStateList backgroundTint;

		/**
		 * Blending mode used to apply the progress background tint.
		 */
		PorterDuff.Mode backgroundTintMode = TintDrawable.DEFAULT_TINT_MODE;

		/**
		 * Color state list used to apply tint to the progress graphics.
		 */
		ColorStateList progressTint;

		/**
		 * Blending mode used to apply the progress graphics tint.
		 */
		PorterDuff.Mode progressTintMode = TintDrawable.DEFAULT_TINT_MODE;

		/**
		 * Color state list used to apply tint to the indeterminate graphics.
		 */
		ColorStateList indeterminateTint;

		/**
		 * Blending mode used to apply the indeterminate graphics tint.
		 */
		PorterDuff.Mode indeterminateTintMode = TintDrawable.DEFAULT_TINT_MODE;

		/**
		 */
		int[] themeAttrs;

		/**
		 */
		int changingConfigurations;

		/**
		 * Creates a new instance of empty ProgressState.
		 */
		ProgressState() {
		}

		/**
		 * Creates a new instance of ProgressState with parameters copied from the specified <var>state</var>.
		 *
		 * @param state The state from which to create the new one.
		 */
		ProgressState(@NonNull ProgressState state) {
			this.colors = state.colors;
			this.color = state.color;
			this.rawColor = state.rawColor;
			this.drawColor = state.drawColor;
			this.backgroundColor = state.backgroundColor;
			this.backgroundRawColor = state.backgroundRawColor;
			this.backgroundDrawColor = state.backgroundDrawColor;
			this.rawThickness = state.rawThickness;
			this.useThickness = state.useThickness;
			this.indeterminateSpeed = state.indeterminateSpeed;
			this.backgroundTint = state.backgroundTint;
			this.backgroundTintMode = state.backgroundTintMode;
			this.progressTint = state.progressTint;
			this.progressTintMode = state.progressTintMode;
			this.indeterminateTint = state.indeterminateTint;
			this.indeterminateTintMode = state.indeterminateTintMode;
			this.changingConfigurations = state.changingConfigurations;
			this.themeAttrs = state.themeAttrs;
		}

		/**
		 */
		@Override
		public boolean canApplyTheme() {
			return themeAttrs != null;
		}

		/**
		 */
		@Override
		public int getChangingConfigurations() {
			return changingConfigurations;
		}
	}
}
