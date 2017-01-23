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
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link ProgressDrawable} implementation which draws a progress or an indeterminate graphics as
 * linear (rectangle) shape. Thickness (height) can be specified by {@link #setThickness(float)}.
 * <b>Note</b>, that as any drawable implementation also this drawable will draw its graphics into
 * bounds set by {@link #setBounds(int, int, int, int)}, the thickness set to this drawable is for
 * informational purpose for a {@link android.view.View} which hosts this drawable to determine how
 * much space (height) is needed to draw graphics of this drawable. The required height can be obtained
 * by {@link #getIntrinsicHeight()} and in case of width this drawable has no restrictions and will
 * draw its graphics into width of the specified bounds, so it is up to the hosting View to determine
 * its width.
 * <p>
 * A color of the progress can be specified by {@link #setColor(int)} or {@link #setColors(int[])}
 * where the specified set of colors will be used only in case of <b>indeterminate</b> mode. See
 * description for {@link #MODE_INDETERMINATE} below for more info. This progress drawable can also
 * draw its background of which color can be specified by {@link #setBackgroundColor(int)}. The
 * background is drawn as a full progress rectangle with the specified thickness. By default is the
 * background color set whenever {@link #setColor(int)} is called. See this method description for
 * more info.
 * <p>
 * <b>Note</b>, that this progress drawable handles also different layout directions that can be
 * specified by {@link #setLayoutDirection(int)}.
 * <p>
 * Whether to draw the progress or indeterminate graphics can be specified by setting a mode by
 * {@link #setMode(int)}. The supported modes for this progress drawable class are described below.
 *
 * <h3>Modes</h3>
 * <ul>
 * <li>
 * {@link #MODE_DETERMINATE}
 * <p>
 * A determinate mode should be used when an instance of LinearProgressDrawable should draw value of
 * progress specified by {@link #setProgress(int)}. The specified progress will be transformed into
 * rectangle with width relative {@code (getProgress() / getMax())} to the current bounds width.
 * <p>
 * <b>Note</b>, that calls like {@link #start()}, {@link #stop()} are for this mode ignored.
 * </li>
 * <li>
 * {@link #MODE_INDETERMINATE}
 * <p>
 * An indeterminate mode should be used when an instance of LinearProgressDrawable should draw the
 * indeterminate graphics. The indeterminate graphics drawing is implemented as infinite amount
 * of consecutive loops, where each of these loops is consisted of progressive translation
 * (along x axis) of two "indeterminate blocks" (rectangles). The first one is called "leading block"
 * which is progressively expanding and the second one is called "following block" which is started
 * to be drawn after the lading block is translated by a specific distance. The following block is
 * drawn in width of the lading block and it is progressively collapsing and "tries" to catch up with
 * the leading block. This is, for purpose of the LinearProgressDrawable, called described in class
 * overview of {@link ProgressDrawable} <b>indeterminate animation session</b>.
 * <p>
 * The animation session can be started by {@link #start()} and stopped by {@link #stop()} or
 * {@link #stopImmediate()}. The difference between those two stop methods is that if requested to not
 * stop immediately via {@code stop()}, the drawing of the indeterminate graphics will be stopped after
 * the block that is following the heading block is fully translated out from the available drawing
 * area otherwise it will look like the indeterminate blocks just "vanished".
 * <p>
 * This mode supports multi-coloring feature which can be enabled by {@link #setMultiColored(boolean)}.
 * If this feature is enabled, the set of colors specified by {@link #setColors(int[])} will be used
 * to change color of the indeterminate blocks and background whenever the following block is fully
 * translated out from the available drawing area. If the end of the colors array is reached, the next
 * color will  be picked again from the beginning of the array and so on.
 * <p>
 * <b>Note</b>, that calls like {@link #setProgress(int)} or {@link #setSecondaryProgress(int)} are
 * for this mode ignored.
 * </li>
 * <li>
 * {@link #MODE_BUFFER}
 * <p>
 * A buffer mode should be used when an instance of LinearProgressDrawable should draw value of progress
 * specified by {@link #setProgress(int)} with also value of secondary (buffer) progress specified by
 * {@link #setSecondaryProgress(int)}. A color of the buffer progress can be specified by {@link #setSecondaryColor(int)},
 * but by default this color is automatically set whenever {@link #setColor(int)} is called with
 * the same logic as background color.
 * <p>
 * This mode also supports one indeterminate animation which can be started by {@link #start()} and
 * stopped by {@link #stop()} or {@link #stopImmediate()} as described for {@link #MODE_INDETERMINATE}
 * above. This will draw a set of circles (buffer marks) aligned along x axis in the drawing area not
 * yet used by secondary progress graphics. The animation is indeterminate because, the circles are
 * progressively translated against the rectangle drawn as the secondary progress and also scaled down
 * and up in the predefined interval. For this animation purpose can be specified two parameters,
 * duration by {@link #setBufferIndeterminateMarksScaleDuration(long)} and interval, which determines
 * how often should be the scale up/down animation of the buffer marks run by
 * {@link #setBufferIndeterminateMarksScaleInterval(long)}.
 * <p>
 * <b>Note</b>, that this progress drawable does not handle any unnecessary logic for this mode purpose,
 * like stopping of the indeterminate animation session after the secondary progress is at its maximum
 * value specified by {@link #setMax(int)} when the indeterminate buffer marks do not need to be drawn,
 * so this is upon the progress view which uses this drawable to draw its progress.
 * </li>
 * <li>
 * {@link #MODE_QUERY_INDETERMINATE_DETERMINATE}
 * <p>
 * A query-indeterminate and determinate mode should be used when an instance of LinearProgressDrawable
 * should draw a reversed (in direction) indeterminate graphics like in {@link #MODE_INDETERMINATE},
 * to indicate at the start that a progress view which hosts this progress drawable is preparing for
 * progress updates, and than allow to draw the progress value specified by {@link #setProgress(int)}
 * like in {@link #MODE_DETERMINATE}.
 * <p>
 * <b>Note</b>, that this progress drawable does not handle any unnecessary logic for this mode purpose,
 * like stopping of the indeterminate animation session after first progress has been, so this is
 * upon to the progress view which uses this drawable to draw its progress.
 * </li>
 * </ul>
 *
 * <h3>Tinting</h3>
 * This progress drawable implementation extends base tinting API extended from its parent via
 * following setters:
 * <ul>
 * <li>{@link #setSecondaryProgressTintList(android.content.res.ColorStateList)}</li>
 * <li>{@link #setSecondaryProgressTintMode(android.graphics.PorterDuff.Mode)}</li>
 * </ul>
 *
 * @author Martin Albedinsky
 */
public class LinearProgressDrawable extends ProgressDrawable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "LinearProgressDrawable";

	/**
	 * Defines an annotation for determining set of allowed modes for LinearProgressDrawable.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({MODE_INDETERMINATE, MODE_DETERMINATE, MODE_BUFFER, MODE_QUERY_INDETERMINATE_DETERMINATE})
	public @interface ProgressMode {
	}

	/**
	 * Flag to request <b>DETERMINATE</b> mode for LinearProgressDrawable which allows to set a
	 * progress value by {@link #setProgress(int)}. See {@link LinearProgressDrawable class} overview
	 * for more info.
	 */
	public static final int MODE_DETERMINATE = DETERMINATE;

	/**
	 * Flag to request <b>INDETERMINATE</b> mode for LinearProgressDrawable which enables indeterminate
	 * animation. See {@link LinearProgressDrawable class} overview for more info.
	 */
	public static final int MODE_INDETERMINATE = INDETERMINATE;

	/**
	 * Flag to request <b>BUFFER</b> mode for LinearProgressDrawable which allows to set a progress
	 * value by {@link #setProgress(int)} and a buffered value by {@link #setSecondaryProgress(int)}.
	 * See {@link LinearProgressDrawable class} overview for more info.
	 */
	public static final int MODE_BUFFER = 0x03;

	/**
	 * Flag to request <b>QUERY INDETERMINATE and DETERMINATE</b> mode for LinearProgressDrawable
	 * which enables reversed indeterminate animation and allows to set a progress value by {@link #setProgress(int)}.
	 * See {@link LinearProgressDrawable class} overview for more info.
	 */
	public static final int MODE_QUERY_INDETERMINATE_DETERMINATE = 0x04;

	/**
	 * Ratio used to compute size of update for indeterminate blocks. The block size is computed from
	 * the current available bounds width.
	 */
	private static final float INDETERMINATE_BLOCK_UPDATE_RATIO = 0.0075f;

	/**
	 * Multiplier used to accelerate updating of the indeterminate blocks.
	 */
	private static final float INDETERMINATE_ACCELERATION_MULTIPLIER = 1.5f;

	/**
	 * Position (in consideration to width) that determines a boundary where the following indeterminate
	 * block should start follow the leader indeterminate block.
	 */
	private static final float INDETERMINATE_FOLLOWING_POSITION = 0.65f;

	/**
	 * Duration used to delay next indeterminate session after the current one finishes.
	 */
	private static final long INDETERMINATE_SILENCE_DURATION = 300;

	/**
	 * Duration for indeterminate buffer marks translation.
	 */
	private static final long BUFFER_INDETERMINATE_MARK_TRANSLATION_UPDATE_DURATION = 80;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Task to update translation (along x axis) of the indeterminate buffer marks (circles).
	 */
	private final Runnable BUFFER_INDETERMINATE_MARKS_TRANSLATION_UPDATE = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			final float maxTranslation = mProgressState.rawThickness * 2;
			if (mBufferIndeterminateMarksTranslation <= -maxTranslation) {
				mBufferIndeterminateMarksTranslation += maxTranslation + mProgressState.rawThickness / 2;
			} else {
				// Move origin by size of one circle mark per predefined duration.
				final float update = mProgressState.rawThickness / (
						BUFFER_INDETERMINATE_MARK_TRANSLATION_UPDATE_DURATION / FRAME_UPDATE_INTERVAL
				) * mProgressState.indeterminateSpeed;
				mBufferIndeterminateMarksTranslation -= update;
			}
			invalidateSelf();
			scheduleSelf(this, computeFramesScheduleTime());
		}
	};

	/**
	 * Task to schedule {@link #BUFFER_INDETERMINATE_MARKS_SCALE_UPDATE} task from the beginning.
	 */
	private final Runnable BUFFER_INDETERMINATE_MARKS_SCALE = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			BUFFER_INDETERMINATE_MARKS_SCALE_UPDATE.mode = BufferIndeterminateMarksScaleTask.MODE_SCALING_DOWN;
			scheduleSelf(BUFFER_INDETERMINATE_MARKS_SCALE_UPDATE, 0);
		}
	};

	/**
	 * Task to update scale of the indeterminate buffer marks.
	 */
	private final BufferIndeterminateMarksScaleTask BUFFER_INDETERMINATE_MARKS_SCALE_UPDATE = new BufferIndeterminateMarksScaleTask();

	/**
	 * Current indeterminate buffer marks translation (along x axis).
	 */
	private float mBufferIndeterminateMarksTranslation;

	/**
	 * Update interval for the indeterminate buffer marks scale animation.
	 * <p>
	 * Default value: <b>5000 ms</b>
	 */
	private long mBufferIndeterminateMarksScaleInterval = 1000 * 5;

	/**
	 * Duration of the indeterminate buffer marks scale animation.
	 * <p>
	 * Default value: <b>500 ms</b>
	 */
	private long mBufferIndeterminateMarksScaleDuration = 500;

	/**
	 * Current scale of the indeterminate buffer marks.
	 */
	private float mBufferIndeterminateMarkScale = 1.0f;

	/**
	 * Object holding current data for the indeterminate graphics.
	 */
	private IndeterminateInfo mIndeterminateInfo;

	/**
	 * Current secondary progress drawn by this drawable if it is in {@link #MODE_BUFFER}.
	 */
	private int mSecondaryProgress;

	/**
	 * Tint filter used to tint secondary progress graphics of this drawable.
	 */
	private PorterDuffColorFilter mSecondaryProgressTintFilter;

	/**
	 * Constant state of this drawable.
	 */
	private LinearState mProgressState;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of LinearProgressDrawable with default color and {@link #MODE_DETERMINATE}
	 * as default mode.
	 * <p>
	 * Default color: <b>#03a9f4</b>
	 */
	public LinearProgressDrawable() {
		this(DEFAULT_COLOR);
	}

	/**
	 * Creates a new instance of LinearProgressDrawable with the specified color and {@link #MODE_DETERMINATE}
	 * as default mode.
	 *
	 * @param color The color used to draw progress or indeterminate graphics of the new drawable.
	 */
	public LinearProgressDrawable(int color) {
		super(color);
		this.init();
	}

	/**
	 * Creates a new instance of LinearProgressDrawable with the specified set of <var>colors</var>
	 * and {@link #MODE_DETERMINATE} as default mode.
	 *
	 * @param colors The set of colors used to draw indeterminate graphics of the new drawable when
	 *               in <b>INDETERMINATE</b> mode.
	 */
	public LinearProgressDrawable(@NonNull int[] colors) {
		super(colors);
		this.init();
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Performs base initialization of this drawable.
	 */
	private void init() {
		this.mMode = MODE_DETERMINATE;
		setThickness(15);
	}

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
	 * Sets the current progress value of this progress drawable which determines size of secondary
	 * progress drawn by this drawable.
	 * <p>
	 * Does nothing if the current mode is not <b>BUFFER</b>.
	 *
	 * @param secondaryProgress The desired secondary progress. Should be from the range {@code [0, getMax()]}.
	 * @see #getSecondaryProgress()
	 */
	public void setSecondaryProgress(int secondaryProgress) {
		if (mMode == MODE_BUFFER && mSecondaryProgress != secondaryProgress && secondaryProgress >= 0 && secondaryProgress <= mMax) {
			this.mSecondaryProgress = secondaryProgress;
			invalidateSelf();
		}
	}

	/**
	 * Returns the secondary progress value set to this progress drawable by {@link #setSecondaryProgress(int)}.
	 *
	 * @return Current secondary progress or {@code 0} if the current mode is not <b>BUFFER</b>.
	 */
	public int getSecondaryProgress() {
		return mMode == MODE_BUFFER ? mSecondaryProgress : 0;
	}

	/**
	 * Sets the color used to draw graphics of this progress drawable.
	 * <p>
	 * For all modes supported by this progress drawable, excepts {@link #MODE_BUFFER}, this call
	 * will use the specified color to automatically set also background color using
	 * {@link #setBackgroundColor(int)} with modified alpha value for proper contrast between progress
	 * and background graphics.
	 * <p>
	 * <b>Note</b>, that this color is used only if the multicolored mode is disabled. See {@link #setMultiColored(boolean)}
	 * for more info.
	 *
	 * @param color The desired color.
	 * @see #getColor()
	 */
	@Override
	public void setColor(int color) {
		super.setColor(color);
		final int bgColor = Color.argb(
				BACKGROUND_COLOR_ALPHA,
				Color.red(color),
				Color.green(color),
				Color.blue(color)
		);
		switch (mMode) {
			case MODE_BUFFER:
				setSecondaryColor(bgColor);
				break;
			default:
				setBackgroundColor(bgColor);
				setSecondaryColor(bgColor);
		}
	}

	/**
	 * Sets the color used to draw secondary graphics (secondary progress) of this progress drawable.
	 *
	 * @param secondaryColor The desired color.
	 * @see #getSecondaryColor()
	 */
	public void setSecondaryColor(int secondaryColor) {
		if (mProgressState.secondaryRawColor != secondaryColor || mProgressState.secondaryDrawColor != secondaryColor) {
			mProgressState.secondaryRawColor = mProgressState.secondaryDrawColor = secondaryColor;
			invalidateSelf();
		}
	}

	/**
	 * Returns the color used to draw secondary graphics of this progress drawable.
	 *
	 * @return The current secondary color. This color can be modified whenever {@link #setAlpha(int)}
	 * is called.
	 * @see #setSecondaryColor(int)
	 */
	public int getSecondaryColor() {
		return mProgressState.secondaryDrawColor;
	}

	/**
	 */
	@Override
	public void setAlpha(int alpha) {
		super.setAlpha(alpha);
		this.updateSecondaryDrawingColor((mProgressState.secondaryRawColor >>> 24) * alpha >> 8);
	}

	/**
	 * Sets a tint for the secondary progress graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tint Color state list to use for tinting of the secondary progress graphics. Can be
	 *             {@code null} to clear the current secondary progress tint.
	 */
	public void setSecondaryProgressTintList(@Nullable ColorStateList tint) {
		mProgressState.secondaryProgressTint = tint;
		this.mSecondaryProgressTintFilter = TintDrawable.createTintFilter(this, tint, mProgressState.secondaryProgressTintMode);
		invalidateSelf();
	}

	/**
	 * Sets a tint blending mode for the secondary progress graphics of this drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(android.graphics.ColorFilter)} will override
	 * the tint.
	 *
	 * @param tintMode The desired Porter-Duff blending mode.
	 */
	public void setSecondaryProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
		mProgressState.secondaryProgressTintMode = tintMode;
		this.mSecondaryProgressTintFilter = TintDrawable.createTintFilter(this, mProgressState.secondaryProgressTint, tintMode);
		invalidateSelf();
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
	 * Returns size of the thickness of this progress drawable.
	 */
	@Override
	public int getIntrinsicHeight() {
		return Math.round(mProgressState.rawThickness);
	}

	/**
	 * Sets the interval for the indeterminate buffer marks scale animation. This interval determines
	 * how often should be the scale animation run, more closely, how often should be the buffer marks
	 * scaled up/down.
	 * <p>
	 * Default value: <b>5000 ms</b>
	 *
	 * @param interval The desired interval in milliseconds.
	 * @see #getBufferIndeterminateMarksScaleInterval()
	 */
	public void setBufferIndeterminateMarksScaleInterval(long interval) {
		this.mBufferIndeterminateMarksScaleInterval = interval;
	}

	/**
	 * Returns the interval determining how often is the indeterminate buffer marks scale animation
	 * played.
	 *
	 * @return Animation interval in milliseconds.
	 * @see #setBufferIndeterminateMarksScaleInterval(long)
	 */
	public long getBufferIndeterminateMarksScaleInterval() {
		return mBufferIndeterminateMarksScaleInterval;
	}

	/**
	 * Sets the duration for the indeterminate buffer marks scale animation.
	 * <p>
	 * Default value: <b>500 ms</b>
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see #getBufferIndeterminateMarksScaleDuration()
	 */
	public void setBufferIndeterminateMarksScaleDuration(long duration) {
		this.mBufferIndeterminateMarksScaleDuration = duration;
	}

	/**
	 * Returns the duration of the indeterminate buffer marks scale animation.
	 *
	 * @return Animation duration in milliseconds.
	 * @see #setBufferIndeterminateMarksScaleDuration(long)
	 */
	public long getBufferIndeterminateMarksScaleDuration() {
		return mBufferIndeterminateMarksScaleDuration;
	}

	/**
	 */
	@Override
	protected void onDrawBackground(@NonNull Canvas canvas, @NonNull Paint paint, @Nullable ColorFilter colorFilter) {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(
				mBounds.left,
				mBounds.bottom - mProgressState.useThickness,
				mBounds.right,
				mBounds.bottom,
				paint
		);
	}

	/**
	 */
	@Override
	protected void onDraw(@NonNull Canvas canvas, @NonNull Paint paint, @Nullable ColorFilter colorFilter) {
		int saveCount = 0;
		if (mProgressState.direction != 0) {
			saveCount = canvas.save();
			canvas.rotate(180, mBounds.centerX(), mBounds.centerY());
		}

		if (hasPrivateFlag(PFLAG_IN_EDIT_MODE)) {
			switch (mMode) {
				case MODE_DETERMINATE:
					this.mProgress = 35;
					break;
				case MODE_INDETERMINATE:
					final int availableWidth = mBounds.width();
					mIndeterminateInfo.leaderWidth = availableWidth / 5f;
					mIndeterminateInfo.leaderLeft = mBounds.right - mIndeterminateInfo.leaderWidth;
					mIndeterminateInfo.followerWidth = availableWidth / 3f;
					mIndeterminateInfo.followerRight = mBounds.left + mIndeterminateInfo.followerWidth;
					break;
				case MODE_BUFFER:
					this.mProgress = 55;
					this.mSecondaryProgress = 80;
					break;
				case MODE_QUERY_INDETERMINATE_DETERMINATE:
					this.mProgress = 15;
					break;
			}
		}

		paint.setStyle(Paint.Style.FILL);
		switch (mMode) {
			case MODE_DETERMINATE:
				this.drawProgress(canvas, mProgress, mProgressTintFilter);
				break;
			case MODE_INDETERMINATE:
				this.drawIndeterminate(canvas);
				break;
			case MODE_BUFFER:
				// Draw secondary progress first, than primary progress.
				if ((mProgressState.secondaryDrawColor >>> 24) != 0) {
					paint.setColor(mProgressState.secondaryDrawColor);
					this.drawProgress(canvas, mSecondaryProgress, mSecondaryProgressTintFilter);

					// Draw buffer thick marks.
					if (mSecondaryProgress < mMax && mBufferIndeterminateMarkScale > 0) {
						final float secProgressRight = (mSecondaryProgress / mMax) * mBounds.right;
						final float markRadius = mProgressState.useThickness / 2;
						final float markSpacing = mProgressState.rawThickness;
						float centerX = secProgressRight + markSpacing + mBufferIndeterminateMarksTranslation;
						final float centerY = (mBounds.bottom - mProgressState.useThickness / 2);
						while (centerX < mBounds.right) {
							centerX += markRadius;
							if ((centerX - markRadius) < secProgressRight) {
								centerX += markSpacing * 2;
								continue;
							}
							canvas.drawCircle(centerX, centerY, markRadius * mBufferIndeterminateMarkScale, paint);
							centerX += markSpacing * 2;
						}
					}
				}
				paint.setColor(mProgressState.drawColor);
				this.drawProgress(canvas, mProgress, mProgressTintFilter);
				break;
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				if (hasPrivateFlag(PFLAG_RUNNING) || hasPrivateFlag(PFLAG_FINISHING_INDETERMINATE)) {
					final int sc = canvas.save();
					canvas.rotate(180, mBounds.centerX(), mBounds.centerY());
					this.drawIndeterminate(canvas);
					canvas.restoreToCount(sc);
				} else {
					this.drawProgress(canvas, mProgress, mProgressTintFilter);
				}
				break;
		}
		if (mProgressState.direction != 0) {
			canvas.restoreToCount(saveCount);
		}
	}

	/**
	 * Draws a rect for the specified <var>progress</var> value on the given <var>canvas</var>.
	 *
	 * @param canvas      The canvas on which to draw the specified progress.
	 * @param progress    The progress which to draw.
	 * @param tintFilter  Current tint color filter for the specified progress.
	 */
	private void drawProgress(Canvas canvas, int progress, ColorFilter tintFilter) {
		final ColorFilter colorFilter = PAINT.getColorFilter();
		if (colorFilter == null && tintFilter != null) {
			PAINT.setColorFilter(tintFilter);
		}
		this.drawRect(canvas, mBounds.left, (progress / mMax) * mBounds.right);
		PAINT.setColorFilter(colorFilter);
	}

	/**
	 * Draws indeterminate graphics on the given <var>canvas</var> using the current {@link #mIndeterminateInfo}
	 * data.
	 *
	 * @param canvas The canvas on which to draw indeterminate graphics.
	 */
	private void drawIndeterminate(Canvas canvas) {
		final ColorFilter colorFilter = PAINT.getColorFilter();
		if (colorFilter == null && mIndeterminateTintFilter != null) {
			PAINT.setColorFilter(mIndeterminateTintFilter);
		}
		if (mIndeterminateInfo.leaderWidth > 0) {
			this.drawRect(
					canvas,
					Math.max(mIndeterminateInfo.leaderLeft, mBounds.left),
					Math.min(mIndeterminateInfo.leaderLeft + mIndeterminateInfo.leaderWidth, mBounds.right)
			);
		}
		if (mIndeterminateInfo.followerWidth > 0) {
			this.drawRect(
					canvas,
					Math.max(mIndeterminateInfo.followerRight - mIndeterminateInfo.followerWidth, mBounds.left),
					Math.min(mIndeterminateInfo.followerRight, mBounds.right)
			);
		}
		PAINT.setColorFilter(colorFilter);
	}

	/**
	 * Draws a rect with the specified <var>left</var> and <var>right</var> coordinate on the given
	 * canvas. The current {@link ProgressDrawable.ProgressState#useThickness} will be used to specify how thick should
	 * be the drawn rect.
	 *
	 * @param canvas The canvas on which to draw rect.
	 * @param left   The left coordinate for the rect to be drawn.
	 * @param right  The right coordinate for the rect to be drawn.
	 */
	private void drawRect(Canvas canvas, float left, float right) {
		canvas.drawRect(left, mBounds.bottom - mProgressState.useThickness, right, mBounds.bottom, PAINT);
	}

	/**
	 */
	@Override
	protected void onStart() {
		switch (mMode) {
			case MODE_INDETERMINATE:
				scheduleSelf(UPDATE, 0);
				notifyStarted();
				break;
			case MODE_BUFFER:
				if (mSecondaryProgress < mMax) {
					this.mBufferIndeterminateMarkScale = 1;
					this.scheduleBufferUpdates();
					notifyStarted();
				}
				break;
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				if (mProgress == 0) {
					scheduleSelf(UPDATE, 0);
					notifyStarted();
				}
				break;
		}
	}

	/**
	 */
	@Override
	protected void onStop() {
		switch (mMode) {
			case MODE_INDETERMINATE:
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				updatePrivateFlags(PFLAG_FINISHING_INDETERMINATE, true);
				break;
		}
	}

	/**
	 */
	@Override
	protected void onStopImmediate() {
		switch (mMode) {
			case MODE_INDETERMINATE:
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				unscheduleSelf(UPDATE);
				break;
			case MODE_BUFFER:
				this.unscheduleBufferUpdates();
				break;
		}
		notifyStopped();
	}

	/**
	 */
	@Override
	protected boolean onModeChange(int mode) {
		switch (mode) {
			case MODE_INDETERMINATE:
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				this.ensureIndeterminateInfo(true);
				break;
			case MODE_BUFFER:
				mProgressState.backgroundRawColor = mProgressState.backgroundDrawColor = 0;
			default:
				this.mIndeterminateInfo = null;
		}
		return false;
	}

	/**
	 */
	@Override
	protected boolean onThicknessChange(float thickness) {
		PAINT.setStrokeWidth(thickness);
		return true;
	}

	/**
	 */
	@Override
	protected boolean onStateChange(int[] state) {
		boolean appearanceChange = super.onStateChange(state);
		if (mProgressState.secondaryProgressTint != null && mProgressState.secondaryProgressTintMode != null) {
			this.mProgressTintFilter = TintDrawable.createTintFilter(
					this, mProgressState.secondaryProgressTint, mProgressState.secondaryProgressTintMode
			);
			appearanceChange = true;
		}
		return appearanceChange;
	}

	/**
	 */
	@Override
	protected boolean onProgressChange(int progress) {
		return true;
	}

	/**
	 */
	@Override
	protected boolean onExplodedChange(boolean exploded) {
		return true;
	}

	/**
	 */
	@Override
	boolean onUpdate() {
		switch (mMode) {
			case MODE_INDETERMINATE:
			case MODE_QUERY_INDETERMINATE_DETERMINATE:
				float leaderLeft = mIndeterminateInfo.leaderLeft;
				float leaderWidth = mIndeterminateInfo.leaderWidth;
				float followerRight = mIndeterminateInfo.followerRight;
				float followerWidth = mIndeterminateInfo.followerWidth;

				final float update = (INDETERMINATE_BLOCK_UPDATE_RATIO * mBounds.width()) * mProgressState.indeterminateSpeed;
				final float acceleratedUpdate = update * (1 + mIndeterminateInterpolator.getInterpolation(
						leaderLeft / mBounds.width()
				));
				final float multipliedAcceleratedUpdate = acceleratedUpdate * INDETERMINATE_ACCELERATION_MULTIPLIER;

				if (leaderLeft < mBounds.right * (INDETERMINATE_FOLLOWING_POSITION / 2)) {
					leaderWidth += update;
					leaderLeft += acceleratedUpdate;
				} else {
					if (leaderLeft + leaderWidth < mBounds.right) {
						leaderWidth += acceleratedUpdate;
					}
					leaderLeft += multipliedAcceleratedUpdate;
				}

				if (leaderLeft >= mBounds.right * INDETERMINATE_FOLLOWING_POSITION) {
					if (followerWidth < leaderWidth && (followerRight - followerWidth <= mBounds.left)) {
						followerWidth += multipliedAcceleratedUpdate;
						followerRight += multipliedAcceleratedUpdate;
					} else {
						followerWidth -= acceleratedUpdate;
						followerRight += multipliedAcceleratedUpdate * INDETERMINATE_ACCELERATION_MULTIPLIER;
					}
				}

				mIndeterminateInfo.leaderLeft = leaderLeft;
				mIndeterminateInfo.leaderWidth = leaderWidth;
				mIndeterminateInfo.followerRight = followerRight;
				mIndeterminateInfo.followerWidth = followerWidth;

				if ((mIndeterminateInfo.followerRight - mIndeterminateInfo.followerWidth) >= mBounds.right) {
					invalidateSelf();
					this.ensureIndeterminateInfo(true);
					if ((mPrivateFlags & PFLAG_FINISHING_INDETERMINATE) == 0) {
						changeNextColor();
						scheduleSelf(UPDATE, computeScheduleTime(INDETERMINATE_SILENCE_DURATION));
					} else {
						updatePrivateFlags(PFLAG_FINISHING_INDETERMINATE, false);
						notifyStopped();
					}
					return false;
				}
				break;
		}
		return true;
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
			changeConstantState(mProgressState = new LinearState());
		} else if (state instanceof LinearState) {
			changeConstantState(mProgressState = new LinearState((LinearState) state));
		}
	}

	/**
	 * Updates the current color used to draw secondary graphics of this progress drawable.
	 *
	 * @param alpha An alpha value to be updated for the current secondary color.
	 */
	private void updateSecondaryDrawingColor(int alpha) {
		final int drawColor = (mProgressState.secondaryRawColor << 8 >>> 8) | (alpha << 24);
		if (mProgressState.secondaryDrawColor != drawColor) {
			mProgressState.secondaryDrawColor = drawColor;
			invalidateSelf();
		}
	}

	/**
	 * Schedules all updates essential for {@link #MODE_BUFFER} mode. These are {@link #BUFFER_INDETERMINATE_MARKS_TRANSLATION_UPDATE}
	 * and {@link #BUFFER_INDETERMINATE_MARKS_SCALE}.
	 */
	private void scheduleBufferUpdates() {
		scheduleSelf(BUFFER_INDETERMINATE_MARKS_TRANSLATION_UPDATE, 0);
		scheduleSelf(BUFFER_INDETERMINATE_MARKS_SCALE, computeScheduleTime(mBufferIndeterminateMarksScaleInterval));
	}

	/**
	 * Un-schedules all updates essential for {@link #MODE_BUFFER} mode.
	 *
	 * @see #scheduleBufferUpdates()
	 */
	private void unscheduleBufferUpdates() {
		unscheduleSelf(BUFFER_INDETERMINATE_MARKS_TRANSLATION_UPDATE);
		unscheduleSelf(BUFFER_INDETERMINATE_MARKS_SCALE);
		unscheduleSelf(BUFFER_INDETERMINATE_MARKS_SCALE_UPDATE);
	}

	/**
	 * Ensures that the {@link #mIndeterminateInfo} is properly initialized.
	 *
	 * @param clear {@code True} to clear the current info, {@code false} otherwise.
	 */
	private void ensureIndeterminateInfo(boolean clear) {
		if (mIndeterminateInfo == null) {
			this.mIndeterminateInfo = new IndeterminateInfo();
		} else if (clear) {
			mIndeterminateInfo.clear(mBounds);
		}
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Constant state implementation for this drawable class.
	 */
	static final class LinearState extends ProgressState {

		/**
		 * Raw color set by {@link #setColor(int)}.
		 */
		int secondaryRawColor;

		/**
		 * Color modified by {@link #setAlpha(int)} used to draw progress.
		 */
		int secondaryDrawColor;

		/**
		 * Color state list used to apply tint to the secondary progress graphics.
		 */
		ColorStateList secondaryProgressTint;

		/**
		 * Blending mode used to apply the secondary progress graphics tint.
		 */
		PorterDuff.Mode secondaryProgressTintMode = TintDrawable.DEFAULT_TINT_MODE;

		/**
		 * Creates a new instance of empty LinearState.
		 */
		LinearState() {
		}

		/**
		 * Creates a new instance of LinearState with parameters copied from the specified
		 * <var>state</var>.
		 *
		 * @param state The state from which to create the new one.
		 */
		LinearState(LinearState state) {
			super(state);
			this.secondaryRawColor = state.secondaryRawColor;
			this.secondaryDrawColor = state.secondaryDrawColor;
			this.secondaryProgressTint = state.secondaryProgressTint;
			this.secondaryProgressTintMode = state.secondaryProgressTintMode;
		}

		/**
		 */
		@NonNull
		@Override
		public Drawable newDrawable() {
			return new LinearProgressDrawable(this, null, null);
		}

		/**
		 */
		@NonNull
		@Override
		public Drawable newDrawable(Resources res) {
			return new LinearProgressDrawable(this, res, null);
		}

		/**
		 */
		@NonNull
		@Override
		public Drawable newDrawable(Resources res, Resources.Theme theme) {
			return new LinearProgressDrawable(this, res, theme);
		}
	}

	/**
	 * Creates a new instance of LinearProgressDrawable from the specified <var>state</var>.
	 *
	 * @param state The state from which to create the new progress drawable instance.
	 * @param res   An application resources.
	 * @param theme A theme to be applied to the new progress drawable instance.
	 */
	private LinearProgressDrawable(LinearState state, Resources res, Resources.Theme theme) {
		if (theme != null && state.canApplyTheme()) {
			changeConstantState(mProgressState = new LinearState(state));
			applyTheme(theme);
		} else {
			changeConstantState(mProgressState = state);
		}
		this.mBackgroundTintFilter = TintDrawable.createTintFilter(this, state.backgroundTint, state.backgroundTintMode);
		this.mProgressTintFilter = TintDrawable.createTintFilter(this, state.progressTint, state.progressTintMode);
		this.mSecondaryProgressTintFilter = TintDrawable.createTintFilter(this, state.secondaryProgressTint, state.secondaryProgressTintMode);
		this.mIndeterminateTintFilter = TintDrawable.createTintFilter(this, state.indeterminateTint, state.indeterminateTintMode);
	}

	/**
	 * This class holds all data necessary to draw indeterminate graphics.
	 */
	private static final class IndeterminateInfo {

		/**
		 * Current left coordinate of the leading indeterminate block.
		 */
		float leaderLeft;

		/**
		 * Current width of the leading indeterminate block.
		 */
		float leaderWidth;

		/**
		 * Current right coordinate of the following indeterminate block.
		 */
		float followerRight;

		/**
		 * Current width of the following indeterminate block.
		 */
		float followerWidth;

		/**
		 * Clears the current indeterminate data.
		 *
		 * @param bounds A bounds of progress drawable used to set up initial indeterminate data.
		 */
		void clear(Rect bounds) {
			leaderLeft = followerRight = bounds.left;
			leaderWidth = followerWidth = 0;
		}
	}

	/**
	 * Runnable task to run scale up/down animation of the indeterminate buffer marks.
	 */
	private final class BufferIndeterminateMarksScaleTask implements Runnable {

		/**
		 * Flag for idle mode.
		 */
		static final int MODE_IDLE = 0x00;

		/**
		 * Flag for scaling mode to indicate that the scale down animation is running.
		 */
		static final int MODE_SCALING_DOWN = 0x01;

		/**
		 * Flag fro scaling mode to indicate that the scale up animation is running.
		 */
		static final int MODE_SCALING_UP = 0x02;

		/**
		 * Current scaling mode which determines whether to scale up or scale down the buffer marks.
		 */
		int mode = MODE_IDLE;

		/**
		 */
		@Override
		public void run() {
			switch (mode) {
				case MODE_SCALING_DOWN:
					if (mBufferIndeterminateMarkScale <= 0) {
						mBufferIndeterminateMarkScale = 0;
						this.mode = MODE_SCALING_UP;
						scheduleSelf(this, computeScheduleTime(mBufferIndeterminateMarksScaleDuration));
					} else {
						mBufferIndeterminateMarkScale -= 1 / (float) (mBufferIndeterminateMarksScaleDuration / FRAME_UPDATE_INTERVAL);
						scheduleSelf(this, computeFramesScheduleTime());
						invalidateSelf();
					}
					break;
				case MODE_SCALING_UP:
					if (mBufferIndeterminateMarkScale >= 1) {
						mBufferIndeterminateMarkScale = 1;
						this.mode = MODE_IDLE;
						scheduleSelf(BUFFER_INDETERMINATE_MARKS_SCALE, computeScheduleTime(mBufferIndeterminateMarksScaleInterval));
					} else {
						mBufferIndeterminateMarkScale += 1 / (float) (mBufferIndeterminateMarksScaleDuration / FRAME_UPDATE_INTERVAL);
						scheduleSelf(this, computeFramesScheduleTime());
						invalidateSelf();
					}
					break;
				default:
					unscheduleSelf(this);
			}
		}
	}
}
