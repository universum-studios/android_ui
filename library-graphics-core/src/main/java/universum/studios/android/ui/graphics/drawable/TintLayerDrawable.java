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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

/**
 * A {@link TintDrawable} implementation used to support tinting of {@link android.graphics.drawable.LayerDrawable},
 * the pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions.
 *
 * @author Martin Albedinsky
 */
public class TintLayerDrawable extends TintDrawable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "TintLayerDrawable";

	/**
	 * Flag indicating whether this drawable should update its tint or not.
	 * <p>
	 * Note that this is used only for pre LOLLIPOP versions of android.
	 */
	private static final int PFLAG_INVALIDATE_TINT = 0x00000001 << 16;

	/**
	 * Flag indicating whether this drawable should ignore calls to {@link #invalidateSelf()} or not.
	 */
	private static final int PFLAG_IGNORE_INVALIDATION = 0x00000001 << 17;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Array with tint options for all layers of the wrapped LayerDrawable.
	 */
	private SparseArray<DrawableLayerTint> mDrawableLayerTints;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of TintLayerDrawable to wrap the given <var>drawable</var> to which
	 * layers will be applied tint set to this drawable via {@link #setTint(int, int)} or
	 * {@link #setTintList(ColorStateList, int)}.
	 *
	 * @param drawable LayerDrawable to wrap and apply tint to.
	 * @see #setTintMode(PorterDuff.Mode, int)
	 */
	public TintLayerDrawable(@NonNull LayerDrawable drawable) {
		super(drawable);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	public void invalidateSelf() {
		if ((mPrivateFlags & PFLAG_IGNORE_INVALIDATION) == 0) {
			super.invalidateSelf();
		}
	}

	/**
	 */
	@Override
	public boolean isStateful() {
		if (mDrawableLayerTints == null) {
			return super.isStateful();
		}

		for (int i = 0; i < mDrawableLayerTints.size(); i++) {
			final ColorStateList tintList = mDrawableLayerTints.valueAt(i).tintList;
			if (tintList != null && tintList.isStateful()) {
				return true;
			}
		}
		return super.isStateful();
	}

	/**
	 */
	@Override
	public void setColorFilter(ColorFilter cf) {
		updatePrivateFlags(PFLAG_INVALIDATE_TINT, true);
		super.setColorFilter(cf);
	}

	/**
	 * Specifies a tint for one of the drawable layers of the wrapped layer drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(ColorFilter)} overrides tint.
	 *
	 * @param tint    The desired color to be used for tinting of the drawable layer.
	 * @param layerId Id of the drawable layer to which should be the specified tint applied.
	 * @see #setTintMode(PorterDuff.Mode, int)
	 */
	public void setTint(int tint, int layerId) {
		setTintList(ColorStateList.valueOf(tint), layerId);
	}

	/**
	 * Specifies a tint list for one of the drawable layers of the wrapped layer drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(ColorFilter)} overrides tint.
	 *
	 * @param tint    The desired colors list to be used for tinting of the drawable layer. Can be
	 *                {@code null} to clear the current tint.
	 * @param layerId Id of the drawable layer to which should be the specified tint applied.
	 * @see #setTintMode(PorterDuff.Mode, int)
	 */
	public void setTintList(@Nullable ColorStateList tint, int layerId) {
		this.ensureLayerTints();

		DrawableLayerTint layerTint = mDrawableLayerTints.get(layerId);
		if (layerTint != null) {
			layerTint.tintList = tint;
			layerTint.currentTint = Color.TRANSPARENT;
			invalidateDrawableLayerTint(layerId);
			return;
		}

		layerTint = new DrawableLayerTint();
		layerTint.tintList = tint;
		mDrawableLayerTints.put(layerId, layerTint);
		invalidateDrawableLayerTint(layerId);
	}

	/**
	 * Specifies a tint blending mode for one of the drawable layers of the wrapped layer drawable.
	 * <p>
	 * Setting a color filter via {@link #setColorFilter(ColorFilter)} overrides tint.
	 *
	 * @param tintMode A Porter-Duff blending mode.
	 * @param layerId  Id of the drawable layer which should be tinted using the specified tint mode.
	 */
	public void setTintMode(@Nullable PorterDuff.Mode tintMode, int layerId) {
		this.ensureLayerTints();

		DrawableLayerTint layerTint = mDrawableLayerTints.get(layerId);
		if (layerTint != null) {
			layerTint.tintMode = tintMode;
			layerTint.currentTint = Color.TRANSPARENT;
			invalidateDrawableLayerTint(layerId);
			return;
		}

		layerTint = new DrawableLayerTint();
		layerTint.tintMode = tintMode;
		mDrawableLayerTints.put(layerId, layerTint);
		invalidateDrawableLayerTint(layerId);
	}

	/**
	 * Ensures that the layer tints array is initialized.
	 */
	private void ensureLayerTints() {
		if (mDrawableLayerTints == null) this.mDrawableLayerTints = new SparseArray<>();
	}

	/**
	 * Invalidates current tint of a drawable layer with the specified <var>layerId</var>.
	 *
	 * @param layerId The id of layer of which tint should be invalidated according to the current
	 *                state of this drawable.
	 */
	private void invalidateDrawableLayerTint(int layerId) {
		updateDrawableLayerTint(layerId, getState());
		invalidateSelf();
	}

	/**
	 */
	@Override
	protected void onBoundsChange(Rect bounds) {
		updatePrivateFlags(PFLAG_INVALIDATE_TINT, true);
		super.onBoundsChange(bounds);
	}

	/**
	 */
	@Override
	protected boolean onLevelChange(int level) {
		return this.updateProgressDrawableLevel(level) || super.onLevelChange(level);
	}

	/**
	 * If one the drawable layers of the wrapped drawable is {@link android.R.id#progress} this will
	 * update its current level to the specified one.
	 *
	 * @param level The level to be set to the progress layer (if presented).
	 * @return {@code True} if the given level has been set and it actually changed to the current
	 * level of the progress layer, {@code false} otherwise.
	 */
	private boolean updateProgressDrawableLevel(int level) {
		final Drawable progressDrawable = ((LayerDrawable) mDrawable).findDrawableByLayerId(android.R.id.progress);
		return progressDrawable != null && progressDrawable.setLevel(level);
	}

	/**
	 */
	@Override
	protected boolean onStateChange(int[] stateSet) {
		updatePrivateFlags(PFLAG_INVALIDATE_TINT, true);
		boolean changed = mDrawable.isStateful() && mDrawable.setState(stateSet);
		return changed || updateDrawableLayerTints(stateSet);
	}

	/**
	 * Updates tint for all drawable layers of the wrapped drawable according to the specified
	 * <var>stateSet</var>.
	 *
	 * @param stateSet Current state set used to resolve which tint should be applied.
	 * @return {@code True} if tint of at least one layer has been updated, {@code false} otherwise.
	 */
	private boolean updateDrawableLayerTints(int[] stateSet) {
		if (mDrawableLayerTints != null) {
			boolean updated = false;
			for (int i = 0; i < mDrawableLayerTints.size(); i++) {
				updated |= updateDrawableLayerTint(mDrawableLayerTints.keyAt(i), stateSet);
			}
			return updated;
		}
		return false;
	}

	/**
	 * Updates tint of a layer with the specified <var>layerId</var> of the wrapped drawable
	 * depends on the specified <var>stateSet</var>.
	 *
	 * @param layerId  Id of the desired layer of which tint to update.
	 * @param stateSet State set to properly resolve tint color.
	 * @return {@code True} if tint has ben updated, {@code false} otherwise.
	 */
	private boolean updateDrawableLayerTint(int layerId, int[] stateSet) {
		if ((mPrivateFlags & PFLAG_HAS_COLOR_FILTER) == 0) {
			final Drawable drawable = ((LayerDrawable) mDrawable).findDrawableByLayerId(layerId);
			if (drawable == null) {
				return false;
			}

			final DrawableLayerTint layerTint = mDrawableLayerTints != null ? mDrawableLayerTints.get(layerId) : null;
			if (layerTint != null && layerTint.tintList != null && layerTint.tintMode != null) {
				final int tintColor = layerTint.tintList.getColorForState(stateSet, layerTint.currentTint);

				if (tintColor != layerTint.currentTint || (mPrivateFlags & PFLAG_TINT_COLOR_CACHING_ENABLED) == 0) {
					drawable.setColorFilter(new PorterDuffColorFilter(tintColor, layerTint.tintMode));
					layerTint.currentTint = tintColor;
				}
			} else {
				drawable.clearColorFilter();
			}
			return true;
		}
		return false;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Holder for drawable layer tint options.
	 */
	private static final class DrawableLayerTint {

		/**
		 * Set of tint colors used to tint graphics of a specific drawable layer.
		 */
		ColorStateList tintList;

		/**
		 * Blending mode used when applying tint to graphics of a specific drawable layer.
		 */
		PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;

		/**
		 * Current tint color applied to graphics of a specific drawable layer.
		 */
		int currentTint = Color.TRANSPARENT;
	}
}
