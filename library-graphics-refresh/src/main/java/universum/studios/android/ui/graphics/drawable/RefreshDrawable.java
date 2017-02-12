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

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.view.View;

/**
 * This drawable wraps background drawable with {@link universum.studios.android.ui.graphics.drawable.CircularProgressDrawable}
 * into the one drawable which can be used to show indicator for running refresh operation for some
 * collection view like {@link android.widget.ListView} or {@link android.widget.GridView} at its
 * top edge.
 * <p>
 * All necessary methods of the <b>CircularProgressDrawable</b> are delegated by this RefreshDrawable
 * class to support some customization of the progress drawable, like its thickness, radius, colors
 * and more.
 *
 * @author Martin Albedinsky
 */
public class RefreshDrawable extends DrawableWrapper implements Animatable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "RefreshDrawable";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Drawable used to draw circular progress.
	 */
	private final CircularProgressDrawable mDrawable;

	/**
	 * Background of this refresh drawable.
	 */
	private Drawable mBackground;

	/**
	 * Radius of the progress drawable.
	 */
	private int mProgressRadius;

	/**
	 * Dimension of this drawable.
	 */
	private int mWidth, mHeight;

	/**
	 * Flag indicating whether the rounded feature of progress drawable has been overridden by calling
	 * {@link #setProgressRounded(boolean)} or not.
	 */
	private boolean mProgressRoundedOverride;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of RefreshDrawable to wrap {@link CircularProgressDrawable} and
	 * background drawable into one drawable.
	 */
	public RefreshDrawable() {
		super(new CircularProgressDrawable());
		this.mDrawable = (CircularProgressDrawable) super.getDrawable();
		mDrawable.setRounded(false);
		mDrawable.setRotateOnProgressChangeEnabled(false);
		mDrawable.setMode(CircularProgressDrawable.MODE_DETERMINATE);
		mDrawable.setArrowEnabled(true);
		mDrawable.setArrowVisible(true);
		mDrawable.setArrowScale(0f);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Sets a boolean flag determining whether this drawable should draw its graphics in edit (preview)
	 * mode or not.
	 *
	 * @param inEditMode {@code True} to draw graphics in edit mode, {@code false} otherwise.
	 * @see #isInEditMode()
	 * @see View#isInEditMode()
	 */
	public final void setInEditMode(boolean inEditMode) {
		if (mDrawable.isInEditMode() != inEditMode) {
			mDrawable.setMode(inEditMode ?
					CircularProgressDrawable.MODE_INDETERMINATE :
					CircularProgressDrawable.MODE_DETERMINATE
			);
			mDrawable.setArrowScale(inEditMode ? 1f : 0f);
			mDrawable.setInEditMode(inEditMode);
			invalidateSelf();
		}
	}

	/**
	 * Returns flag indicating whether this drawable draws its graphics in edit mode or not.
	 *
	 * @return {@code True} if graphics is drawn in edit (preview) mode, {@code false} otherwise.
	 */
	public final boolean isInEditMode() {
		return mDrawable.isInEditMode();
	}

	/**
	 */
	@Override
	public void draw(Canvas canvas) {
		if (mBackground != null) {
			mBackground.draw(canvas);
		}
		mDrawable.draw(canvas);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#start()}.
	 */
	@Override
	public void start() {
		mDrawable.start();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#stop()}.
	 */
	@Override
	public void stop() {
		mDrawable.stop();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#stopImmediate()}.
	 */
	public void stopImmediate() {
		mDrawable.stopImmediate();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#isRunning()}.
	 */
	@Override
	public boolean isRunning() {
		return mDrawable.isRunning();
	}

	/**
	 * Returns this drawable instance.
	 */
	@NonNull
	@Override
	public Drawable getCurrent() {
		return this;
	}

	/**
	 * <b>This is not supported operation.</b>
	 *
	 * @throws UnsupportedOperationException
	 */
	@NonNull
	@Override
	public Drawable getDrawable() {
		throw new UnsupportedOperationException("Cannot to access wrapped drawable.");
	}

	/**
	 * Sets the background for this refresh drawable.
	 *
	 * @param background The desired background.
	 */
	public void setBackground(@Nullable Drawable background) {
		this.mBackground = background;
		this.handleDrawablesSizeChange();
		invalidateSelf();
	}

	/**
	 * Handles update of the current background's or progress drawable's size.
	 */
	private void handleDrawablesSizeChange() {
		if (mBackground != null) {
			this.mWidth = Math.max(mBackground.getIntrinsicWidth(), mProgressRadius * 2);
			this.mHeight = Math.max(mBackground.getIntrinsicHeight(), mProgressRadius * 2);
		} else {
			this.mWidth = mHeight = mProgressRadius * 2;
		}
	}

	/**
	 * Returns the background of this refresh drawable.
	 *
	 * @return Background drawable or {@code null} if this refresh drawable does not have background
	 * set.
	 */
	@Nullable
	public Drawable getBackground() {
		return mBackground;
	}

	/**
	 */
	@Override
	public int getIntrinsicWidth() {
		return mWidth;
	}

	/**
	 */
	@Override
	public int getIntrinsicHeight() {
		return mHeight;
	}

	/**
	 */
	@Override
	public void setAlpha(int alpha) {
		super.setAlpha(alpha);
		if (mBackground != null) mBackground.setAlpha(alpha);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setAlpha(int)}.
	 */
	public void setProgressAlpha(int alpha) {
		mDrawable.setAlpha(alpha);
	}

	/**
	 * Sets the radius of the progress arc.
	 *
	 * @param radius The desired radius.
	 * @see #getProgressRadius()
	 */
	public void setProgressRadius(@Px int radius) {
		if (mProgressRadius != radius) {
			this.mProgressRadius = radius;
			this.handleDrawablesSizeChange();
		}
	}

	/**
	 * Returns radius of the progress arc.
	 *
	 * @return Progress arc's radius.
	 * @see #setProgressRadius(int)
	 */
	@Px
	public int getProgressRadius() {
		return mProgressRadius;
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getAlpha()}.
	 */
	public int getProgressAlpha() {
		return mDrawable.getAlpha();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setMode(int)}.
	 *
	 * @see #getProgressMode()
	 */
	public void setProgressMode(@CircularProgressDrawable.ProgressMode int mode) {
		switch (mode) {
			case CircularProgressDrawable.MODE_DETERMINATE:
				if (!mProgressRoundedOverride) {
					mDrawable.setRounded(false);
				}
				stopImmediate();
				mDrawable.setMode(mode);
				break;
			case CircularProgressDrawable.MODE_INDETERMINATE:
				mDrawable.setMode(mode);
				if (!mProgressRoundedOverride) {
					mDrawable.setRounded(true);
				}
				start();
				break;
		}
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getMode()}.
	 *
	 * @see #setProgressMode(int)
	 */
	@CircularProgressDrawable.ProgressMode
	public int getProgressMode() {
		return mDrawable.getMode();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setProgress(int)}.
	 *
	 * @see #getProgress()
	 */
	public void setProgress(int progress) {
		mDrawable.setProgress(progress);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getProgress()}.
	 *
	 * @see #setProgress(int)
	 */
	public int getProgress() {
		return mDrawable.getProgress();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setRotation(float)}.
	 *
	 * @see #getProgressRotation()
	 */
	public void setProgressRotation(float rotation) {
		mDrawable.setRotation(rotation);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getRotation()}.
	 *
	 * @see #setProgressRotation(float)
	 */
	public float getProgressRotation() {
		return mDrawable.getRotation();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setThickness(float)}.
	 *
	 * @see #getProgressThickness()
	 */
	public void setProgressThickness(float thickness) {
		mDrawable.setThickness(thickness);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getThickness()}.
	 *
	 * @see #setProgressThickness(float)
	 */
	public float getProgressThickness() {
		return mDrawable.getThickness();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setColor(int)}.
	 *
	 * @see #getProgressColor()
	 */
	public void setProgressColor(@ColorInt int color) {
		mDrawable.setColor(color);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getColor()}.
	 *
	 * @see #setProgressColor(int)
	 */
	@ColorInt
	public int getProgressColor() {
		return mDrawable.getColor();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setColors(int[])}.
	 *
	 * @see #getProgressColors()
	 */
	public void setProgressColors(int[] colors) {
		mDrawable.setColors(colors);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getColors()}.
	 *
	 * @see #setProgressColors(int[])
	 */
	public int[] getProgressColors() {
		return mDrawable.getColors();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setMultiColored(boolean)}.
	 *
	 * @see #isProgressMultiColored()
	 */
	public void setProgressMultiColored(boolean multiColored) {
		mDrawable.setMultiColored(multiColored);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#isMultiColored()}.
	 *
	 * @see #setProgressMultiColored(boolean)
	 */
	public boolean isProgressMultiColored() {
		return mDrawable.isMultiColored();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setRounded(boolean)}.
	 *
	 * @see #isProgressRounded()
	 */
	public void setProgressRounded(boolean rounded) {
		this.mProgressRoundedOverride = true;
		mDrawable.setRounded(rounded);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#isRounded()}.
	 *
	 * @see #setProgressRounded(boolean)
	 */
	public boolean isProgressRounded() {
		return mDrawable.isRounded();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setArrowVisible(boolean)}.
	 *
	 * @see #isProgressArrowVisible()
	 */
	public void setProgressArrowVisible(boolean visible) {
		mDrawable.setArrowVisible(visible);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#isArrowVisible()}.
	 *
	 * @see #setProgressArrowVisible(boolean)
	 */
	public boolean isProgressArrowVisible() {
		return mDrawable.isArrowVisible();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setArrowScale(float)}.
	 *
	 * @see #getProgressArrowScale()
	 */
	public void setProgressArrowScale(float scale) {
		mDrawable.setArrowScale(scale);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getArrowScale()}.
	 *
	 * @see #setProgressArrowScale(float)
	 */
	public float getProgressArrowScale() {
		return mDrawable.getArrowScale();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setIndeterminateSpeed(float)}.
	 *
	 * @see #getProgressIndeterminateSpeed()
	 */
	public void setProgressIndeterminateSpeed(float speed) {
		mDrawable.setIndeterminateSpeed(speed);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getIndeterminateSpeed()}.
	 *
	 * @see #setProgressIndeterminateSpeed(float)
	 */
	public float getProgressIndeterminateSpeed() {
		return mDrawable.getIndeterminateSpeed();
	}

	/**
	 */
	@Override
	protected void onBoundsChange(Rect bounds) {
		if (mBackground != null) {
			final int progressDiameter = mProgressRadius * 2;
			final int bgWidth = mBackground.getIntrinsicWidth();
			final int bgHeight = mBackground.getIntrinsicHeight();
			if (bgWidth > progressDiameter || bgHeight > progressDiameter) {
				mBackground.setBounds(bounds);
				this.centerDrawableBounds(mDrawable, progressDiameter, progressDiameter, bounds);
			} else {
				mDrawable.setBounds(bounds);
				this.centerDrawableBounds(mBackground, bgWidth, bgHeight, bounds);
			}
			return;
		}
		super.onBoundsChange(bounds);
	}

	/**
	 * Centers bounds of the given <var>drawable</var> for the given <var>bounds</var>.
	 *
	 * @param drawable The drawable which to center for the given bounds.
	 * @param width    Width of the specified drawable.
	 * @param height   Height of the specified drawable.
	 * @param bounds   The bounds within which to center the drawable.
	 */
	private void centerDrawableBounds(Drawable drawable, int width, int height, Rect bounds) {
		final int deltaX = bounds.width() - width;
		final int deltaY = bounds.height() - height;
		final int left = bounds.left + deltaX / 2;
		final int top = bounds.top + deltaY / 2;
		drawable.setBounds(
				left,
				top,
				left + width,
				top + height
		);
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
