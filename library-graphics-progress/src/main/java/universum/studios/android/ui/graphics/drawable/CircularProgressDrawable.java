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

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link ProgressDrawable} implementation which draws a progress or an indeterminate graphics as
 * circular (oval) shape. Size of the oval can be specified by {@link #setRadius(int)} and its thickness
 * can be specified by {@link #setThickness(float)}. <b>Note</b>, that as any drawable implementation
 * also this drawable will draw its graphics into bounds set by {@link #setBounds(int, int, int, int)},
 * the radius set to this drawable is only for informational purpose for a {@link android.view.View}
 * which hosts this drawable to determine how much space is needed to draw graphics of this drawable.
 * The required size can be obtained by {@link #getIntrinsicWidth()} and {@link #getIntrinsicHeight()}.
 * <p>
 * The oval's color can be specified by {@link #setColor(int)} or {@link #setColors(int[])} where
 * the specified set of colors will be used only in case of <b>indeterminate</b> mode. See description
 * for {@link #MODE_INDETERMINATE} below for more info. This progress drawable can also draw its
 * background of which color can be specified by {@link #setBackgroundColor(int)}. The background is
 * drawn as a full progress oval with the specified thickness.
 * <p>
 * Whether to draw the progress or indeterminate graphics can be specified by setting a mode by
 * {@link #setMode(int)}. The supported modes for this progress drawable class are described below.
 *
 * <h3>Modes</h3>
 * <ul>
 * <li>
 * {@link #MODE_DETERMINATE}
 * <p>
 * A determinate mode should be used when an instance of CircularProgressDrawable should draw value
 * of progress specified by {@link #setProgress(int)}. The specified progress will be transformed
 * into degrees value and than drawn as oval. A start angle of the progress oval can be specified by
 * {@link #setStartAngle(float)}, the default one is at <b>270</b> degrees, so at 12 a clock. When
 * a max progress is set to the CircularProgressDrawable the full progress oval => circle is drawn.
 * <p>
 * This mode supports auto-rotation feature of the progress oval and this feature is by default
 * <b>enabled</b>, so whenever {@link #setProgress(int)} is called, the progress oval will be rotated
 * clockwise by degrees delta (NEW_ANGLE - OLD_ANGLE). This rotation logic will ensures hat a tail
 * and a head of the progress oval will be meeting (before they collapse) at the start angle.
 * <p>
 * <b>Note</b>, that calls like {@link #start()}, {@link #stop()} are for this mode ignored.
 * </li>
 * <li>
 * {@link #MODE_INDETERMINATE}
 * <p>
 * An indeterminate mode should be used when an instance of CircularProgressDrawable should draw the
 * indeterminate graphics. The indeterminate graphics drawing is implemented as infinite amount
 * of consecutive loops, where each of these loops is consisted of progressive expanding followed by
 * progressive collapsing of the oval. This is, for purpose of the CircularProgressDrawable, called
 * as described in class overview of {@link ProgressDrawable} <b>indeterminate animation session</b>.
 * During this animation session is the oval also progressively rotated.
 * <p>
 * The animation session can be started by {@link #start()} and stopped by {@link #stop()} or
 * {@link #stopImmediate()}. The difference between those two stop methods is that if requested to not
 * stop immediately via {@code stop()}, the drawing of the indeterminate graphics will be stopped after
 * the oval is fully collapsed otherwise will be stopped immediately regardless how much is the oval
 * expanded/collapsed so it will look like it just "vanished".
 * <p>
 * This mode supports multi-coloring feature which can be enabled by {@link #setMultiColored(boolean)}.
 * If this feature is enabled, the set of colors specified by {@link #setColors(int[])} will be used
 * to change color of the oval whenever the indeterminate oval is fully collapsed. If the end of the
 * colors array is reached, the next color will be picked again from the beginning of the array and
 * so on.
 * <p>
 * <b>Note</b>, that calls like {@link #setProgress(int)}, {@link #setRotation(float)} are for this
 * mode ignored.
 * </li>
 * </ul>
 *
 * <h3>Arrow</h3>
 * An arrow feature can be enabled by {@link #setArrowEnabled(boolean)} for both {@link #MODE_DETERMINATE}
 * and {@link #MODE_INDETERMINATE} modes. If enabled, the arrow is drawn at the head of the
 * progress/indeterminate oval. This feature can be useful for refresh drawable. Enabling of this
 * feature also changes (expands) size of the CircularProgressDrawable, so the radius specified by
 * {@link #setRadius(int)} will be preserved. Size of the arrow unfortunately cannot be specified,
 * but its size will be computed according to the current thickness of the oval to ensure that the
 * arrow's appearance will fit to the whole progress drawable's appearance properly. The arrow can
 * be also scaled up/down by {@link #setArrowScale(float)} which can be used to progressively show/hide
 * the arrow.
 *
 * <h3>Rotating</h3>
 * The progress oval can be also rotated (supported only for {@link #MODE_DETERMINATE}) by {@link #setRotation(float)}.
 * This method is used for the <b>auto-rotation</b> feature described for determinate mode above.
 *
 * @author Martin Albedinsky
 */
public class CircularProgressDrawable extends ProgressDrawable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "CircularProgressDrawable";

	/**
	 * Defines an annotation for determining set of allowed modes for CircularProgressDrawable.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({MODE_INDETERMINATE, MODE_DETERMINATE})
	public @interface ProgressMode {
	}

	/**
	 * Flag to request <b>DETERMINATE</b> mode for CircularProgressDrawable which allows to set
	 * a progress value by {@link #setProgress(int)}. See {@link CircularProgressDrawable class}
	 * overview for more info.
	 */
	public static final int MODE_DETERMINATE = DETERMINATE;

	/**
	 * Flag to request <b>INDETERMINATE</b> mode for CircularProgressDrawable which enables indeterminate
	 * animation. See {@link CircularProgressDrawable class} overview for more info.
	 */
	public static final int MODE_INDETERMINATE = INDETERMINATE;

	/**
	 * Max angle used in computations for progress/indeterminate oval of this drawable.
	 */
	private static final float MAX_ANGLE = 360;

	/**
	 * Default initial start angle for the indeterminate oval.
	 */
	private static final float INITIAL_START_ANGLE = 270;

	/**
	 * Max sweep angle for the indeterminate oval graphics.
	 */
	private static final float INDETERMINATE_MAX_SWEEP_ANGLE = 280;

	/**
	 * Size of buffer used to slow rotation of the indeterminate oval while it is fully collapsed or
	 * fully expanded.
	 */
	private static final float INDETERMINATE_SLOW_MOTION_ANGLE_BUFFER_SIZE = 45;

	/**
	 * Amount of angles used to update start angle of the indeterminate oval while indeterminate
	 * animation session is running.
	 */
	private static final float INDETERMINATE_START_ANGLE_UPDATE = 3.5f;

	/**
	 * Amount of angles used to update sweep angle of the indeterminate oval to expand it, while
	 * indeterminate animation session is running.
	 */
	private static final float INDETERMINATE_EXPANDING_ANGLE_UPDATE = 8.0f;

	/**
	 * Amount of angles used to update sweep angle of the indeterminate oval to collapse it, while
	 * indeterminate animation session is running.
	 */
	private static final float INDETERMINATE_COLLAPSING_ANGLE_UPDATE = 6.0f;

	/**
	 * Flag for indeterminate state of this drawable used while indeterminate animation session is
	 * running. This flag is used only to indicate initial state.
	 */
	private static final int INDETERMINATE_STATE_IDLE = 0x00;

	/**
	 * Flag for indeterminate state of this drawable used while indeterminate animation session is
	 * running to indicate that the indeterminate oval is being expanded.
	 */
	private static final int INDETERMINATE_STATE_EXPANDING = 0x01;

	/**
	 * Flag for indeterminate state of this drawable used while indeterminate animation session is
	 * running to indicate that the indeterminate oval is being rotated in slow motion after it was
	 * fully expanded.
	 */
	private static final int INDETERMINATE_STATE_EXPANDING_SLOW_MOTION = 0x02;

	/**
	 * Flag for indeterminate state of this drawable used while indeterminate animation session is
	 * running to indicate that the indeterminate oval is being collapsed.
	 */
	private static final int INDETERMINATE_STATE_COLLAPSING = 0x03;

	/**
	 * Flag for indeterminate state of this drawable used while indeterminate animation session is
	 * running to indicate that the indeterminate oval is being rotated in slow motion after it was
	 * fully collapsed.
	 */
	private static final int INDETERMINATE_STATE_COLLAPSING_SLOW_MOTION = 0x04;

	/**
	 * Flag indicating whether the arrow feature for this drawable is enabled or not.
	 */
	private static final int PFLAG_ARROW_ENABLED = 0x00008000;

	/**
	 * Flag indicating whether the arrow is visible or not.
	 */
	private static final int PFLAG_ARROW_VISIBLE = 0x00010000;

	/**
	 * Flag indicating whether the automatic rotation on progress change is enabled or not.
	 */
	private static final int PFLAG_ROTATE_ON_PROGRESS_CHANGE = 0x00020000;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Oval used when drawing current progress or indeterminate graphics of this drawable.
	 */
	private final RectF OVAL = new RectF();

	/**
	 * Full size of drawing area for this drawable determined by its current bounds.
	 */
	private int mSize;

	/**
	 * Initial start angle for progress/indeterminate oval set by {@link #setStartAngle(float)}.
	 * This angle is used whenever current state of this drawable is reset.
	 */
	private float mUserStartAngle = INITIAL_START_ANGLE;

	/**
	 * Constant state of this drawable.
	 */
	private CircularState mProgressState;

	/**
	 * Current start angle of progress/indeterminate oval.
	 * <p>
	 * This angle is changed whenever {@link #onUpdate()} occurs for this drawable while in the
	 * <b>INDETERMINATE</b> mode.
	 */
	private float mStartAngle = mUserStartAngle;

	/**
	 * Current sweep angle of progress/indeterminate oval.
	 * <p>
	 * This angle is changed whenever {@link #onUpdate()} occurs for this drawable while in the
	 * <b>INDETERMINATE</b> mode depends on the current {@link #mIndeterminateState} or whenever
	 * {@link #onProgressChange(int)} occurs for this drawable while in the <b>DETERMINATE</b> mode.
	 */
	private float mSweepAngle;

	/**
	 * Object holding current data for the arrow.
	 */
	private ArrowInfo mArrowInfo;

	/**
	 * Indeterminate state used to manage updating behaviour of the indeterminate oval block.
	 */
	private int mIndeterminateState = INDETERMINATE_STATE_IDLE;

	/**
	 * Buffer used to "froze" updating of the indeterminate oval block while it is fully collapsed
	 * or fully expanded.
	 */
	private float mIndeterminateFrozenAngleBuffer;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of CircularProgressDrawable with default color and {@link #MODE_INDETERMINATE}
	 * as default mode.
	 * <p>
	 * Default color: <b>#03a9f4</b>
	 */
	public CircularProgressDrawable() {
		this(DEFAULT_COLOR);
	}

	/**
	 * Creates a new instance of CircularProgressDrawable with the specified color and {@link #MODE_INDETERMINATE}
	 * as default mode.
	 *
	 * @param color The color used to draw progress or indeterminate graphics of the new drawable.
	 */
	public CircularProgressDrawable(int color) {
		super(color);
		this.init();
	}

	/**
	 * Creates a new instance of CircularProgressDrawable with the specified set of <var>colors</var>
	 * and {@link #MODE_INDETERMINATE} as default mode.
	 *
	 * @param colors The set of colors used to draw indeterminate graphics of the new drawable when
	 *               in <b>INDETERMINATE</b> mode.
	 */
	public CircularProgressDrawable(@NonNull int[] colors) {
		super(colors);
		this.init();
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	public void applyTheme(@NonNull Resources.Theme theme) {
		super.applyTheme(theme);
		// Unfortunately this method cannot be properly implemented for drawable not directly within
		// android.drawable package.
		/*if (mProgressState == null || mProgressState.themeAttrs == null) {
			return;
		}*/
	}

	/**
	 * Sets the radius for the progress/indeterminate oval of this progress drawable. If the arrow
	 * feature is enabled by {@link #setArrowEnabled(boolean)}, the size of this drawable will be
	 * defined by the radius and also by size of the arrow. If arrow is not enabled size of this
	 * drawable will be {@code radius * 2} for both width and height of which values can be obtained
	 * by {@link #getIntrinsicWidth()} and {@link #getIntrinsicHeight()}.
	 * <p>
	 * Default radius is: <b>-1</b>
	 *
	 * @param radius The desired radius.
	 * @see #getRadius()
	 */
	public void setRadius(@Px int radius) {
		mProgressState.radius = radius;
	}

	/**
	 * Returns the radius of the progress/indeterminate oval of this progress drawable.
	 * <p>
	 * <b>Note</b>, that as described in {@link #setRadius(int)}, the radius does not necessarily
	 * specify size of this drawable.
	 *
	 * @return This drawable's progress/indeterminate oval's radius.
	 * @see #setRadius(int)
	 */
	@Px
	public int getRadius() {
		return mProgressState.radius;
	}

	/**
	 * Returns {@code (radius * 2)} if the arrow feature is not enabled, {@code (radius * 2 + arrowOverflow)}
	 * if it is enabled.
	 */
	@Override
	public int getIntrinsicWidth() {
		return mProgressState.radius * 2 + ((mPrivateFlags & PFLAG_ARROW_VISIBLE) != 0 ? (int) (mArrowInfo.wide * 3 / 8) : 0);
	}

	/**
	 * Returns {@code (radius * 2)} if the arrow feature is not enabled, {@code (radius * 2 + arrowOverflow)}
	 * if it is enabled.
	 */
	@Override
	public int getIntrinsicHeight() {
		return mProgressState.radius * 2 + ((mPrivateFlags & PFLAG_ARROW_VISIBLE) != 0 ? (int) (mArrowInfo.wide * 3 / 8) : 0);
	}

	/**
	 * Sets the initial start angle for the progress/indeterminate oval.
	 * <p>
	 * If in <b>INDETERMINATE</b> mode the specified start angle will be applied after the running
	 * indeterminate animation sessions is finished. If in <b>DETERMINATE</b> mode the specified
	 * angle will be immediately applied so it should be set before the progress updates starts.
	 * <p>
	 * The default start angle is: <b>270</b>
	 *
	 * @param startAngle The desired angle from the range {@code [0, 360]}.
	 * @see #getStartAngle()
	 */
	public void setStartAngle(float startAngle) {
		if (startAngle >= 0 && startAngle <= MAX_ANGLE) {
			this.mUserStartAngle = startAngle;
			if (mMode == MODE_DETERMINATE) {
				this.mStartAngle = startAngle;
			}
		}
	}

	/**
	 * Returns the initial start angle for the progress/indeterminate oval of this progress drawable.
	 *
	 * @return Initial start angle from the range {@code [0, 360]}.
	 * @see #setStartAngle(float)
	 */
	public float getStartAngle() {
		return mUserStartAngle;
	}

	/**
	 */
	@Override
	public void setMode(@ProgressMode int mode) {
		super.setMode(mode);
	}

	/**
	 */
	@Override
	@ProgressMode
	@SuppressWarnings("ResourceType")
	public int getMode() {
		return super.getMode();
	}

	/**
	 * Sets the rotation for the progress oval of this progress drawable. The specified rotation will
	 * be applied only if the current mode is <b>DETERMINATE</b>.
	 * <p>
	 * This method will update both the start and sweep angle of the progress oval. If the arrow feature
	 * is enabled, the arrow position will be also updated.
	 *
	 * @param rotation The desired rotation from the range {@code [0, 360]}.
	 * @see #getRotation()
	 */
	public void setRotation(float rotation) {
		if (mMode == MODE_DETERMINATE && mProgressState.rotation != rotation && rotation >= 0 && rotation <= MAX_ANGLE) {
			final float delta = rotation - mProgressState.rotation;
			if (delta != 0) {
				this.mProgressState.rotation = rotation;
				this.mStartAngle = correctAngle(mStartAngle + delta);
				this.updateArrowPosition();
				invalidateSelf();
			}
		}
	}

	/**
	 * Returns the rotation of the progress oval.
	 *
	 * @return Current progress oval's rotation from the range {@code [0, 360]} or {@code 0} if the
	 * current mode is not <b>DETERMINATE</b>.
	 * @see #setRotation(float)
	 */
	public float getRotation() {
		return mMode == MODE_DETERMINATE ? mProgressState.rotation : 0;
	}

	/**
	 * Sets a flag indicating whether to draw the arrow of this progress drawable or not.
	 *
	 * @param visible {@code True} to draw the arrow, {@code false} to not draw the arrow.
	 * @see #isArrowVisible()
	 */
	public void setArrowVisible(boolean visible) {
		if (hasPrivateFlag(PFLAG_ARROW_VISIBLE) != visible) {
			this.updatePrivateFlags(PFLAG_ARROW_VISIBLE, visible);
			if (visible && (mPrivateFlags & PFLAG_ARROW_ENABLED) == 0) {
				setArrowEnabled(true);
			}
			invalidateSelf();
		}
	}

	/**
	 * Returns a flag indicating whether the arrow of this progress drawable is visible, so it is
	 * drawn, or not.
	 *
	 * @return {@code True} if the arrow is drawn, {@code false} otherwise.
	 * @see #setArrowVisible(boolean)
	 */
	public boolean isArrowVisible() {
		return (mPrivateFlags & PFLAG_ARROW_VISIBLE) != 0;
	}

	/**
	 * Sets a flag indicating whether the arrow feature for this progress drawable should be enabled
	 * or not.
	 * <p>
	 * If the arrow feature is enabled, the arrow is drawn at the head of the current progress/indeterminate
	 * oval. The arrow feature is not tied to the current mode. <b>Note</b>, that if the arrow is
	 * enabled, size (width and height) of this progress drawable will expand by size of the arrow.
	 * <p>
	 * The arrow feature is <b>disabled</b> by default.
	 *
	 * @param hasArrow {@code True} to enable arrow feature, {@code false} otherwise.
	 * @see #isArrowEnabled()
	 */
	public void setArrowEnabled(boolean hasArrow) {
		if (hasPrivateFlag(PFLAG_ARROW_ENABLED) != hasArrow) {
			this.updatePrivateFlags(PFLAG_ARROW_ENABLED, hasArrow);
			this.updateOval();
		}
	}

	/**
	 * Returns a flag indicating whether the arrow feature is enabled for this progress drawable or
	 * not.
	 *
	 * @return {@code True} if the arrow feature is enabled, {@code false} otherwise.
	 * @see #setArrowEnabled(boolean)
	 */
	public boolean isArrowEnabled() {
		return (mPrivateFlags & PFLAG_ARROW_ENABLED) != 0;
	}

	/**
	 * Sets the scale for the arrow of this progress drawable.
	 * <p>
	 * This can be useful when the arrow need to be progressively shown or hidden.
	 *
	 * @param scale The desired scale from the range {@code [0, 1]}.
	 * @see #getArrowScale()
	 */
	public void setArrowScale(float scale) {
		this.ensureArrowInfo();
		if (mArrowInfo.scale != scale && scale >= 0 && scale <= 1) {
			mArrowInfo.scale = scale;
			invalidateSelf();
		}
	}

	/**
	 * Returns the current scale of the arrow.
	 *
	 * @return Arrow's current scale.
	 * @see #setArrowScale(float)
	 */
	public float getArrowScale() {
		this.ensureArrowInfo();
		return mArrowInfo.scale;
	}

	/**
	 * Ensures that the info for arrow is initialized.
	 */
	private void ensureArrowInfo() {
		if (mArrowInfo == null) this.mArrowInfo = new ArrowInfo();
	}

	/**
	 * Sets a flag indicating whether to auto-rotate the progress oval after a change in progress
	 * occurs or not.
	 * <p>
	 * This feature is supported only for <b>DETERMINATE</b> mode and if enabled the progress oval
	 * will be rotated in such a way that when it is at its max value defined by {@link #setMax(int)},
	 * both the tail and the head of the progress oval will meet at the 12 a clock.
	 * <p>
	 * This feature is <b>enabled</b> by default.
	 *
	 * @param enabled {@code True} to enable this feature, {@code false} to disable it.
	 * @see #isRotateOnProgressChangeEnabled()
	 */
	public void setRotateOnProgressChangeEnabled(boolean enabled) {
		if (hasPrivateFlag(PFLAG_ROTATE_ON_PROGRESS_CHANGE) != enabled) {
			updatePrivateFlags(PFLAG_ROTATE_ON_PROGRESS_CHANGE, enabled);
			invalidateSelf();
		}
	}

	/**
	 * Returns a flag indicating whether to auto-rotate the progress oval whenever a change in progress
	 * occurs in <b>DETERMINATE</b> mode.
	 *
	 * @return {@code True} if auto-rotation is enabled, {@code false} otherwise.
	 * @see #setRotateOnProgressChangeEnabled(boolean)
	 */
	public boolean isRotateOnProgressChangeEnabled() {
		return (mPrivateFlags & PFLAG_ROTATE_ON_PROGRESS_CHANGE) != 0;
	}

	/**
	 */
	@Override
	protected void onDrawBackground(@NonNull Canvas canvas, @NonNull Paint paint, @Nullable ColorFilter colorFilter) {
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawArc(OVAL, 0, MAX_ANGLE, false, paint);
	}

	/**
	 */
	@Override
	protected void onDraw(@NonNull Canvas canvas, @NonNull Paint paint, @Nullable ColorFilter colorFilter) {
		if (colorFilter == null) {
			switch (mMode) {
				case MODE_DETERMINATE:
					if (mProgressTintFilter != null) {
						paint.setColorFilter(mProgressTintFilter);
					}
					break;
				case MODE_INDETERMINATE:
					if (mIndeterminateTintFilter != null) {
						paint.setColorFilter(mIndeterminateTintFilter);
					}
					break;
			}
		}

		if (hasPrivateFlag(PFLAG_IN_EDIT_MODE)) {
			switch (mMode) {
				case MODE_DETERMINATE:
					this.mStartAngle = INITIAL_START_ANGLE;
					this.mSweepAngle = 100;
					this.updateArrowPosition();
					break;
				case MODE_INDETERMINATE:
					this.mStartAngle = 210;
					this.mSweepAngle = INDETERMINATE_MAX_SWEEP_ANGLE;
					this.updateArrowPosition();
					break;
			}
		}

		updatePaintToRounded(paint, hasPrivateFlag(PFLAG_ROUNDED));
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawArc(OVAL, mStartAngle, mSweepAngle, false, paint);

		// Draw arrow at the head of the progress if it is visible.
		if (hasPrivateFlag(PFLAG_ARROW_VISIBLE)) {
			updatePaintToRounded(paint, false);
			paint.setStyle(Paint.Style.FILL);
			final int sc = canvas.save();
			canvas.clipPath(mArrowInfo.path);
			canvas.scale(
					mArrowInfo.scale,
					mArrowInfo.scale,
					mArrowInfo.backCenterPoint.x,
					mArrowInfo.backCenterPoint.y
			);
			canvas.drawPath(mArrowInfo.path, paint);
			canvas.restoreToCount(sc);
		}
	}

	/**
	 */
	@Override
	protected void onStart() {
		this.mIndeterminateState = INDETERMINATE_STATE_EXPANDING;
		updateColor();
		scheduleSelf(UPDATE, 0);
		notifyStarted();
	}

	/**
	 */
	@Override
	protected void onStop() {
		if (mSweepAngle > 0) updatePrivateFlags(PFLAG_FINISHING_INDETERMINATE, true);
		else onStopImmediate();
	}

	/**
	 */
	@Override
	protected void onStopImmediate() {
		this.clearIndeterminate();
	}

	/**
	 * Clears the current indeterminate data. This will also stops all updates for running indeterminate
	 * animation session and will call {@link #notifyStopped()} to notify that animation has been
	 * stopped.
	 */
	private void clearIndeterminate() {
		unscheduleSelf(UPDATE);
		updatePrivateFlags(PFLAG_FINISHING_INDETERMINATE, false);
		this.mIndeterminateState = INDETERMINATE_STATE_IDLE;
		this.mStartAngle = mUserStartAngle;
		this.mSweepAngle = 0;
		resetCurrentColor();
		notifyStopped();
	}

	/**
	 */
	@Override
	protected boolean onModeChange(int mode) {
		switch (mode) {
			case MODE_DETERMINATE:
				changeColor(mProgressState.color);
				break;
			case MODE_INDETERMINATE:
				resetCurrentColor();
				break;
		}
		return super.onModeChange(mode);
	}

	/**
	 */
	@Override
	protected boolean onProgressChange(int progress) {
		return onUpdate();
	}

	/**
	 */
	@Override
	protected boolean onThicknessChange(float thickness) {
		PAINT.setStrokeWidth(thickness);
		this.updateOval();
		return true;
	}

	/**
	 */
	@Override
	protected boolean onRoundedChange(boolean rounded) {
		return true;
	}

	/**
	 */
	@Override
	protected boolean onExplodedChange(boolean exploded) {
		if (exploded) updateOval();
		return true;
	}

	/**
	 */
	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		this.mSize = bounds.width();
		this.updateOval();
	}

	/**
	 */
	@Override
	boolean onUpdate() {
		boolean schedule = false;
		final float indeterminateSpeed = mProgressState.indeterminateSpeed;
		switch (mMode) {
			case MODE_DETERMINATE:
				final float sweepAngle = mSweepAngle;
				this.mSweepAngle = mProgress / mMax * MAX_ANGLE;
				if (hasPrivateFlag(PFLAG_ROTATE_ON_PROGRESS_CHANGE)) {
					this.mStartAngle += mSweepAngle - sweepAngle;
				}
				invalidateSelf();
				break;
			case MODE_INDETERMINATE:
				schedule = true;
				if (hasPrivateFlag(PFLAG_FINISHING_INDETERMINATE)) {
					if (mSweepAngle <= 0) {
						this.clearIndeterminate();
					} else {
						final float collapseBy = INDETERMINATE_COLLAPSING_ANGLE_UPDATE * indeterminateSpeed;
						this.mStartAngle += collapseBy;
						this.mSweepAngle -= collapseBy / 2;
					}
					break;
				}

				// Update start angle always with constant value.
				this.mStartAngle = this.correctAngle(mStartAngle + INDETERMINATE_START_ANGLE_UPDATE * indeterminateSpeed);
				this.mIndeterminateFrozenAngleBuffer += INDETERMINATE_START_ANGLE_UPDATE * indeterminateSpeed;

				switch (mIndeterminateState) {
					case INDETERMINATE_STATE_EXPANDING:
						final float expandingInterpolation = (1 + mIndeterminateInterpolator.getInterpolation(mSweepAngle / INDETERMINATE_MAX_SWEEP_ANGLE));
						this.mSweepAngle += INDETERMINATE_EXPANDING_ANGLE_UPDATE * expandingInterpolation * indeterminateSpeed;
						if (mSweepAngle >= INDETERMINATE_MAX_SWEEP_ANGLE) {
							this.mIndeterminateFrozenAngleBuffer = 0;
							this.mIndeterminateState = INDETERMINATE_STATE_EXPANDING_SLOW_MOTION;
						}
						break;
					case INDETERMINATE_STATE_EXPANDING_SLOW_MOTION:
						if (mIndeterminateFrozenAngleBuffer >= INDETERMINATE_SLOW_MOTION_ANGLE_BUFFER_SIZE) {
							this.mIndeterminateState = INDETERMINATE_STATE_COLLAPSING;
						}
						break;
					case INDETERMINATE_STATE_COLLAPSING:
						final float collapsingInterpolation = (1 + mIndeterminateInterpolator.getInterpolation(mSweepAngle / INDETERMINATE_MAX_SWEEP_ANGLE));
						final float collapseBy = INDETERMINATE_COLLAPSING_ANGLE_UPDATE * collapsingInterpolation * indeterminateSpeed;
						this.mStartAngle += collapseBy;
						this.mSweepAngle -= collapseBy;
						if (mSweepAngle <= 0) {
							this.mSweepAngle = 1;
							this.mIndeterminateFrozenAngleBuffer = 0;
							this.mIndeterminateState = INDETERMINATE_STATE_COLLAPSING_SLOW_MOTION;
							changeNextColor();
						}
						break;
					case INDETERMINATE_STATE_COLLAPSING_SLOW_MOTION:
						if (mIndeterminateFrozenAngleBuffer >= INDETERMINATE_SLOW_MOTION_ANGLE_BUFFER_SIZE) {
							this.mIndeterminateState = INDETERMINATE_STATE_EXPANDING;
						}
						break;
					case INDETERMINATE_STATE_IDLE:
					default:
						schedule = false;
				}
				break;
		}
		if ((mPrivateFlags & PFLAG_ARROW_ENABLED) != 0) {
			this.updateArrowPosition();
		}
		return schedule;
	}

	/**
	 */
	@Override
	void updateStateFromTypedArray(TypedArray typedArray) {
		super.updateStateFromTypedArray(typedArray);
		// Cannot be properly implemented.
	}

	/**
	 */
	@Override
	void ensureConstantState(ProgressState state) {
		if (mProgressState == null) {
			changeConstantState(mProgressState = new CircularState());
		} else if (state instanceof CircularState) {
			changeConstantState(mProgressState = new CircularState((CircularState) state));
		}
	}

	/**
	 * Updates the progress/indeterminate oval depends on the enabled features for this progress
	 * drawable and the current bounds and thickness of this drawable.
	 */
	private void updateOval() {
		final boolean hasArrow = hasPrivateFlag(PFLAG_ARROW_ENABLED);
		float arrowWide = 0;
		if (hasArrow) {
			this.ensureArrowInfo();
			mArrowInfo.wide = mProgressState.useThickness * 4;
			arrowWide = mArrowInfo.wide / 2;
		}

		final float useThickness = mProgressState.useThickness / 2;
		final float thicknessDelta = (mProgressState.rawThickness - mProgressState.useThickness) / 2;
		final float ovalCorrection = useThickness + thicknessDelta + arrowWide;
		OVAL.set(
				mBounds.left + ovalCorrection,
				mBounds.top + ovalCorrection,
				mBounds.left + mSize - ovalCorrection,
				mBounds.top + mSize - ovalCorrection
		);

		if (hasArrow) {
			this.updateArrowPosition();
		}
	}

	/**
	 * Updates the current position of the arrow drawn at the head of the progress/indeterminate oval
	 * according to the current start + sweep angle.
	 */
	private void updateArrowPosition() {
		this.ensureArrowInfo();

		/**
		 * Compute the path points of the arrow's triangle.
		 */
		final float center = OVAL.centerX();
		final float radius = OVAL.width() / 2;
		final float circumference = (float) (Math.PI * 2 * radius);

		// Compute the one PX per ANGLE to move the arrow's head forward so the arrow will be drawn
		// after the head of the current progress.
		final float ppd = circumference / MAX_ANGLE;

		// Compute rotation of the arrow based on the start angle + current sweep angle of the current progress.
		// Take into count also height of the arrow to move it after the head of the current progress.
		final float arrowWide = mArrowInfo.wide;
		final float progressAngle = mStartAngle + mSweepAngle - 1;
		final float heightAngles = (arrowWide / 2 / ppd);

		double rotationRadii = Math.toRadians(correctAngle(progressAngle + heightAngles));
		final PointF headPoint = new PointF(
				Math.round(center + (radius + mProgressState.useThickness / 2) * Math.cos(rotationRadii)),
				Math.round(center + (radius + mProgressState.useThickness / 2) * Math.sin(rotationRadii))
		);
		mArrowInfo.headPoint = headPoint;

		final float arrowHalfWide = arrowWide / 2;
		rotationRadii = Math.toRadians(correctAngle(progressAngle));
		final PointF backRgtPoint = new PointF(
				Math.round(center + (radius - arrowHalfWide) * Math.cos(rotationRadii)),
				Math.round(center + (radius - arrowHalfWide) * Math.sin(rotationRadii))
		);
		mArrowInfo.backCenterPoint = new PointF(
				Math.round(center + radius * Math.cos(rotationRadii)),
				Math.round(center + radius * Math.sin(rotationRadii))
		);
		final PointF backLftPoint = new PointF(
				Math.round(center + (radius + arrowHalfWide) * Math.cos(rotationRadii)),
				Math.round(center + (radius + arrowHalfWide) * Math.sin(rotationRadii))
		);

		/**
		 * Update the path of the arrow.
		 */
		final Path path = mArrowInfo.path;
		path.reset();
		path.moveTo(headPoint.x, headPoint.y);
		path.lineTo(backLftPoint.x, backLftPoint.y);
		path.lineTo(backRgtPoint.x, backRgtPoint.y);
		path.close();
	}

	/**
	 * Corrects the specified angle, so it will be from the range {@code [0, 360]}.
	 *
	 * @param angle The angle to correct.
	 * @return Corrected angle.
	 */
	private float correctAngle(float angle) {
		return angle <= MAX_ANGLE ? angle : angle - MAX_ANGLE;
	}

	/**
	 * Performs base initialization of this drawable.
	 */
	private void init() {
		this.mMode = MODE_INDETERMINATE;
		setRounded(true);
		setRotateOnProgressChangeEnabled(true);
		setThickness(15);
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Constant state implementation for this drawable class.
	 */
	static final class CircularState extends ProgressState {

		/**
		 * Radius for the progress/indeterminate oval.
		 */
		int radius = -1;

		/**
		 * Current rotation of the progress/indeterminate oval.
		 */
		float rotation;

		/**
		 * Creates a new instance of empty CircularState.
		 */
		CircularState() {
		}

		/**
		 * Creates a new instance of CircularState with parameters copied from the specified
		 * <var>state</var>.
		 *
		 * @param state The state from which to create the new one.
		 */
		CircularState(CircularState state) {
			super(state);
			this.radius = state.radius;
			this.rotation = state.rotation;
		}

		/**
		 */
		@NonNull
		@Override
		public Drawable newDrawable() {
			return new CircularProgressDrawable(this, null, null);
		}

		/**
		 */
		@NonNull
		@Override
		public Drawable newDrawable(Resources res) {
			return new CircularProgressDrawable(this, res, null);
		}

		/**
		 */
		@NonNull
		@Override
		public Drawable newDrawable(Resources res, Resources.Theme theme) {
			return new CircularProgressDrawable(this, res, theme);
		}
	}

	/**
	 * Creates a new instance of CircularProgressDrawable from the specified <var>state</var>.
	 *
	 * @param state The state from which to create the new progress drawable instance.
	 * @param res   An application resources.
	 * @param theme A theme to be applied to the new progress drawable instance.
	 */
	private CircularProgressDrawable(CircularState state, Resources res, Resources.Theme theme) {
		if (theme != null && state.canApplyTheme()) {
			changeConstantState(mProgressState = new CircularState(state));
			applyTheme(theme);
		} else {
			changeConstantState(mProgressState = state);
		}
		this.mBackgroundTintFilter = TintDrawable.createTintFilter(this, state.backgroundTint, state.backgroundTintMode);
		this.mProgressTintFilter = TintDrawable.createTintFilter(this, state.progressTint, state.progressTintMode);
		this.mIndeterminateTintFilter = TintDrawable.createTintFilter(this, state.indeterminateTint, state.indeterminateTintMode);
	}

	/**
	 * This class holds all data about the arrow.
	 */
	private static final class ArrowInfo {

		/**
		 * Wide of the arrow. This determines size of the arrow's triangle base.
		 */
		float wide;

		/**
		 * Path determining the arrow.
		 */
		Path path = new Path();

		/**
		 * Heading point of the arrow's {@link #path}.
		 */
		PointF headPoint;

		/**
		 * Back centered point of the arrow's {@link #path}.
		 */
		PointF backCenterPoint;

		/**
		 * Current scale of the arrow.
		 */
		float scale = 1;
	}
}
