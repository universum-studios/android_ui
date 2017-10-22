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
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A {@link DrawableWrapper} implementation used to support tinting of a Drawable for the pre
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions.
 *
 * @author Martin Albedinsky
 */
public class TintDrawable extends DrawableWrapper {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "TintDrawable";

	/**
	 * Default blending mode used to apply tint to a Drawable.
	 */
	public static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;

	/**
	 * Flag indicating whether this drawable has color filter set or not.
	 */
	static final int PFLAG_HAS_COLOR_FILTER = 0x00000001;

	/**
	 * Flag indicating whether this drawable should cache current tint color or not.
	 */
	static final int PFLAG_TINT_COLOR_CACHING_ENABLED = 0x00000001 << 1;

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Set of tint colors used to tint graphics of this drawable.
	 */
	private ColorStateList mTintList;

	/**
	 * Blending mode used when applying tint to graphics of this drawable.
	 */
	private PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;

	/**
	 * Current tint color applied to graphics of this drawable.
	 */
	private int mCurrentTint = Color.TRANSPARENT;

	/**
	 * Set of private flags.
	 */
	int mPrivateFlags = PFLAG_TINT_COLOR_CACHING_ENABLED;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of TintDrawable to wrap the given <var>drawable</var> to which will be
	 * applied tint set to this drawable via {@link #setTint(int)} or {@link #setTintList(ColorStateList)}.
	 *
	 * @param drawable Drawable to wrap and apply tint to.
	 * @see #setTintMode(PorterDuff.Mode)
	 */
	public TintDrawable(@NonNull Drawable drawable) {
		this(drawable, true);
	}

	/**
	 *  Creates a new instance of TintDrawable to wrap the given <var>drawable</var> to which will be
	 * applied tint set to this drawable via {@link #setTint(int)} or {@link #setTintList(ColorStateList)}.
	 *
	 * @param drawable       Drawable to wrap and apply tint to.
	 * @param cacheTintColor {@code True} if current tint color should be always saved (for performance),
	 *                       {@code false} otherwise.
	 */
	public TintDrawable(@NonNull Drawable drawable, boolean cacheTintColor) {
		super(drawable);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			// fixme: Unfortunately on pre GINGERBREAD_MR1 (including), mutating the drawable disables
			// fixme: somehow ??? its drawing.
			mutate();
		}
		this.updatePrivateFlags(PFLAG_TINT_COLOR_CACHING_ENABLED, cacheTintColor);
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Crates a new instance of PorterDuffColorFilter with color obtained from the specified <var>tint</var>
	 * colors for the current state of the specified <var>drawable</var>.
	 *
	 * @param drawable The drawable used to resolve proper tint color based on its current state.
	 * @param tint     Set of available tint colors.
	 * @param tintMode Blending mode for the new color filter.
	 * @return New PorterDuffColorFilter with the resolved color and the specified <var>tintMode</var>.
	 */
	@Nullable
	public static PorterDuffColorFilter createTintFilter(@NonNull Drawable drawable, @Nullable ColorStateList tint, @Nullable PorterDuff.Mode tintMode) {
		if (tint == null || tintMode == null) {
			return null;
		}
		return new PorterDuffColorFilter(tint.getColorForState(drawable.getState(), Color.TRANSPARENT), tintMode);
	}

	/**
	 */
	@Override
	public boolean isStateful() {
		return super.isStateful() || (mTintList != null && mTintList.isStateful());
	}

	/**
	 */
	@Override
	public void clearColorFilter() {
		updatePrivateFlags(PFLAG_HAS_COLOR_FILTER, false);
		super.clearColorFilter();
	}

	/**
	 */
	@Override
	public void setTint(int tint) {
		setTintList(ColorStateList.valueOf(tint));
	}

	/**
	 */
	@Override
	public void setTintList(@Nullable ColorStateList tint) {
		this.mTintList = tint;
		this.invalidateTint();
	}

	/**
	 */
	@Override
	public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.mTintMode = tintMode;
		this.invalidateTint();
	}

	/**
	 * Requests invalidation of this drawable due to change in tint. This will clear the current
	 * tint color value and update current color filter according to the current tint list and tint
	 * mode.
	 */
	private void invalidateTint() {
		this.mCurrentTint = Color.TRANSPARENT;
		this.updateTint(getState());
		invalidateSelf();
	}

	/**
	 */
	@Override
	public void setColorFilter(ColorFilter cf) {
		updatePrivateFlags(PFLAG_HAS_COLOR_FILTER, cf != null);
		super.setColorFilter(cf);
	}

	/**
	 */
	@Override
	protected boolean onStateChange(int[] stateSet) {
		final boolean changed = super.onStateChange(stateSet);
		return this.updateTint(stateSet) || changed;
	}

	/**
	 * Updates tint of the wrapped drawable depends on the specified <var>stateSet</var>.
	 *
	 * @param stateSet State set to properly resolve tint color.
	 * @return {@code True} if tint has ben updated, {@code false} otherwise.
	 */
	private boolean updateTint(int[] stateSet) {
		if ((mPrivateFlags & PFLAG_HAS_COLOR_FILTER) == 0) {
			if (mTintList != null && mTintMode != null) {
				final int tintColor = mTintList.getColorForState(stateSet, mCurrentTint);
				if (tintColor != mCurrentTint || (mPrivateFlags & PFLAG_TINT_COLOR_CACHING_ENABLED) == 0) {
					super.setColorFilter(new PorterDuffColorFilter(tintColor, mTintMode));
					this.mCurrentTint = tintColor;
				}
			} else {
				super.clearColorFilter();
			}
		}
		return false;
	}

	/**
	 * Updates the current private flags.
	 *
	 * @param flag Value of the desired flag to add/remove to/from the current private flags.
	 * @param add  Boolean flag indicating whether to add or remove the specified <var>flag</var>.
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	boolean hasPrivateFlag(int flag) {
		return (mPrivateFlags & flag) != 0;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
