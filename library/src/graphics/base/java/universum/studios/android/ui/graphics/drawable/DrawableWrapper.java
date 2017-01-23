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

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;

import universum.studios.android.ui.UiConfig;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A {@link android.graphics.drawable.Drawable} implementation which can be used to wrap another instance of Drawable.
 *
 * @author Martin Albedinsky
 */
public class DrawableWrapper extends Drawable implements Drawable.Callback {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "DrawableWrapper";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Instance of wrapped drawable.
	 */
	Drawable mDrawable;

	/**
	 * Flat indicating whether this drawable is already mutated or not.
	 */
	private boolean mMutated;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of DrawableWrapper which wraps the given <var>drawable</var>.
	 *
	 * @param drawable The drawable to wrap.
	 */
	public DrawableWrapper(@NonNull Drawable drawable) {
		this.mDrawable = drawable;

		/**
		 * Copy current state of the wrapped drawable.
		 */
		setState(drawable.getState());
		setBounds(drawable.getBounds());
		setLevel(drawable.getLevel());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			setCallback(drawable.getCallback());
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setAlpha(drawable.getAlpha());
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setColorFilter(drawable.getColorFilter());
		}
		mDrawable.setCallback(this);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	public void inflate(@NonNull Resources resources, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs) throws XmlPullParserException, IOException {
		mDrawable.inflate(resources, parser, attrs);
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void inflate(@NonNull Resources resources, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
		if (UiConfig.MATERIALIZED) mDrawable.inflate(resources, parser, attrs, theme);
	}

	/**
	 * Re-attaches this drawable wrapper as {@link android.graphics.drawable.Drawable.Callback} to
	 * the wrapped drawable. This should be called in case when the callback of the wrapped drawable
	 * has been since initialization removed by framework, this means that this parent actually has
	 * been removed as callback.
	 */
	public void attachCallback() {
		mDrawable.setCallback(this);
		invalidateSelf();
	}

	/**
	 */
	@Override
	public Drawable mutate() {
		if (!mMutated && super.mutate() == this) {
			this.mDrawable = mDrawable.mutate();
			this.mMutated = true;
		}
		return this;
	}

	/**
	 */
	@Override
	public void draw(Canvas canvas) {
		mDrawable.draw(canvas);
	}

	/**
	 */
	@Override
	public void invalidateDrawable(Drawable who) {
		invalidateSelf();
	}

	/**
	 */
	@Override
	public void scheduleDrawable(Drawable who, Runnable what, long when) {
		scheduleSelf(what, when);
	}

	/**
	 */
	@Override
	public void unscheduleDrawable(Drawable who, Runnable what) {
		unscheduleSelf(what);
	}

	/**
	 * Returns the wrapped drawable of this tint drawable instance.
	 *
	 * @return Wrapped drawable.
	 */
	@NonNull
	public Drawable getDrawable() {
		return mDrawable;
	}

	/**
	 */
	@Override
	public void setChangingConfigurations(int configs) {
		mDrawable.setChangingConfigurations(configs);
	}

	/**
	 */
	@Override
	public int getChangingConfigurations() {
		return mDrawable.getChangingConfigurations();
	}

	/**
	 */
	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public void setDither(boolean dither) {
		mDrawable.setDither(dither);
	}

	/**
	 */
	@Override
	public void setFilterBitmap(boolean filter) {
		mDrawable.setFilterBitmap(filter);
	}

	/**
	 */
	@Override
	public void setAlpha(int alpha) {
		mDrawable.setAlpha(alpha);
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public int getAlpha() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? mDrawable.getAlpha() : 0;
	}

	/**
	 */
	@Override
	public void setColorFilter(ColorFilter cf) {
		mDrawable.setColorFilter(cf);
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public ColorFilter getColorFilter() {
		return UiConfig.MATERIALIZED ? mDrawable.getColorFilter() : null;
	}

	/**
	 */
	@Override
	public boolean isStateful() {
		return mDrawable.isStateful();
	}

	/**
	 */
	@Override
	public void jumpToCurrentState() {
		DrawableCompat.jumpToCurrentState(mDrawable);
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public Drawable getCurrent() {
		if (mDrawable instanceof InsetDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			final Drawable innerDrawable = ((InsetDrawable) mDrawable).getDrawable();
			return innerDrawable != null ? innerDrawable.getCurrent() : null;
		}
		return mDrawable.getCurrent();
	}

	/**
	 */
	@Override
	public boolean setVisible(boolean visible, boolean restart) {
		return super.setVisible(visible, restart) || mDrawable.setVisible(visible, restart);
	}

	/**
	 */
	@Override
	public int getOpacity() {
		return mDrawable.getOpacity();
	}

	/**
	 */
	@Override
	public Region getTransparentRegion() {
		return mDrawable.getTransparentRegion();
	}

	/**
	 */
	@Override
	public int getIntrinsicWidth() {
		return mDrawable.getIntrinsicWidth();
	}

	/**
	 */
	@Override
	public int getIntrinsicHeight() {
		return mDrawable.getIntrinsicHeight();
	}

	/**
	 */
	@Override
	public int getMinimumWidth() {
		return mDrawable.getMinimumWidth();
	}

	/**
	 */
	@Override
	public int getMinimumHeight() {
		return mDrawable.getMinimumHeight();
	}

	/**
	 */
	@Override
	public boolean getPadding(@NonNull Rect padding) {
		return mDrawable.getPadding(padding);
	}

	/**
	 */
	@Override
	public void setAutoMirrored(boolean mirrored) {
		DrawableCompat.setAutoMirrored(mDrawable, mirrored);
	}

	/**
	 */
	@Override
	public boolean isAutoMirrored() {
		return DrawableCompat.isAutoMirrored(mDrawable);
	}

	/**
	 */
	@Override
	public void setTint(int tint) {
		DrawableCompat.setTint(mDrawable, tint);
	}

	/**
	 */
	@Override
	public void setTintList(ColorStateList tint) {
		DrawableCompat.setTintList(mDrawable, tint);
	}

	/**
	 */
	@Override
	public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
		DrawableCompat.setTintMode(mDrawable, tintMode);
	}

	/**
	 */
	@Override
	public void setHotspot(float x, float y) {
		DrawableCompat.setHotspot(mDrawable, x, y);
	}

	/**
	 */
	@Override
	public void setHotspotBounds(int left, int top, int right, int bottom) {
		DrawableCompat.setHotspotBounds(mDrawable, left, top, right, bottom);
	}

	/**
	 */
	@Override
	public void getHotspotBounds(Rect outRect) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mDrawable.getHotspotBounds(outRect);
	}

	/**
	 */
	@Override
	protected boolean onStateChange(int[] stateSet) {
		return mDrawable.setState(stateSet);
	}

	/**
	 */
	@Override
	protected void onBoundsChange(Rect bounds) {
		mDrawable.setBounds(bounds);
	}

	/**
	 */
	@Override
	protected boolean onLevelChange(int level) {
		return mDrawable.setLevel(level);
	}

	/**
	 */
	@Override
	public ConstantState getConstantState() {
		return mDrawable.getConstantState();
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void getOutline(@NonNull Outline outline) {
		if (UiConfig.MATERIALIZED) mDrawable.getOutline(outline);
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public boolean canApplyTheme() {
		return UiConfig.MATERIALIZED && mDrawable.canApplyTheme();
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void applyTheme(@NonNull Resources.Theme theme) {
		if (UiConfig.MATERIALIZED) mDrawable.applyTheme(theme);
	}

	/**
	 */
	@Override
	public boolean onLayoutDirectionChanged(int layoutDirection) {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mDrawable.onLayoutDirectionChanged(layoutDirection);
	}

	/**
	 */
	@Override
	public boolean isFilterBitmap() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mDrawable.isFilterBitmap();
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public Rect getDirtyBounds() {
		return UiConfig.MATERIALIZED ? mDrawable.getDirtyBounds() : null;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
