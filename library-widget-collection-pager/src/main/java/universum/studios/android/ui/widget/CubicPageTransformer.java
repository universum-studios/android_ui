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

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * A {@link ViewPager.PageTransformer} implementation that can be used to transform pages like in
 * 3D cube space horizontally.
 * <p>
 * Whether to transform pages with camera positioned <b>outside</b> or <b>inside</b> the 3D cube
 * can be specified via {@link #CubicPageTransformer(CameraPosition)} constructor.
 *
 * @author Martin Albedinsky
 */
public class CubicPageTransformer implements ViewPager.PageTransformer {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "CubicPageTransformer";

	/**
	 * Default minimum alpha for completely transformed pages.
	 *
	 * @see #setMinAlpha(float)
	 */
	public static final float MIN_ALPHA = 0.6f;

	/**
	 * Represents position of an imaginary camera used to resolve how to rotate pages in 3D cubic space.
	 *
	 * @author Martin Albedinsky
	 * @see CubicPageTransformer#CubicPageTransformer(CameraPosition)
	 */
	public enum CameraPosition {
		/**
		 * Camera is positioned <b>outside</b> of a 3D cube of rotating pages.
		 */
		OUT(
				90, -90,
				0, 1
		),
		/**
		 * Camera is positioned <b>inside</b> of a 3D cube of rotating pages.
		 */
		IN(
				-90, 90,
				0, 1
		);

		/**
		 * Rotation degrees for the origin page position.
		 * <p>
		 * Used for a page being transformed from/to the end.
		 */
		public final float originDegrees;

		/**
		 * Rotation degrees for the destination page position.
		 * <p>
		 * Used for a page being transformed from/to the start.
		 */
		public final float destDegrees;

		/**
		 * Rotation pivot x factor for the origin page position.
		 * <p>
		 * Used for a page being transformed from/to the end.
		 */
		public final float originPivotXFactor;

		/**
		 * Rotation pivot x factor for the destination page position.
		 * <p>
		 * Used for a page being transformed from/to the start.
		 */
		public final float destPivotXFactor;

		/**
		 * Creates a new instance of CameraPosition with the specified ration degrees and rotation
		 * pivots.
		 *
		 * @param originDeg   Rotation degrees for the origin position.
		 * @param destDeg     Rotation degrees for the destination position.
		 * @param originPivot Rotation pivot for the x origin position.
		 * @param destPivot   Rotation pivot for the x destination position.
		 */
		CameraPosition(float originDeg, float destDeg, float originPivot, float destPivot) {
			this.originDegrees = originDeg;
			this.destDegrees = destDeg;
			this.originPivotXFactor = originPivot;
			this.destPivotXFactor = destPivot;
		}
	}

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Minimum alpha for the completely transformed page at the start/end.
	 */
	private float mMinAlpha = MIN_ALPHA;

	/**
	 * Imaginary camera position used when rotating pages in 3D cubic space.
	 */
	private final CameraPosition mCameraPosition;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of CubicPageTransformer with {@link CameraPosition#OUT} as <var>cameraPosition</var>.
	 */
	public CubicPageTransformer() {
		this(CameraPosition.OUT);
	}

	/**
	 * Creates a new instance of CubicPageTransformer with the specified <var>cameraPosition</var>.
	 *
	 * @param cameraPosition The imaginary camera position used when rotating pages in 3D cubic space.
	 */
	public CubicPageTransformer(@NonNull CameraPosition cameraPosition) {
		this.mCameraPosition = cameraPosition;
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	public void transformPage(View page, float position) {
		final float factor = Math.abs(position);
		final int pageWidth = page.getWidth();
		final int pageHeight = page.getHeight();

		/**
		 * This page is way off-screen to the start.
		 * [-Infinity,-1)
		 */
		if (position < -1) {
			ViewCompat.setAlpha(page, 0);
		}
		/**
		 * Moving page from/to the start.
		 * [-1,0]
		 */
		else if (position <= 0) {
			// Fade in/out the page.
			ViewCompat.setAlpha(page, Math.max(1 - factor, mMinAlpha));

			// Rotate the page along its y axis.
			ViewCompat.setPivotX(page, pageWidth * mCameraPosition.destPivotXFactor);
			ViewCompat.setPivotX(page, pageHeight * 0.5f);
			ViewCompat.setRotationY(page, factor * mCameraPosition.destDegrees);
		}
		/**
		 * Moving page from/to the end.
		 * (0,1]
		 */
		else if (position <= 1) {
			// Fade in/out the page.
			ViewCompat.setAlpha(page, Math.max(1 - factor, mMinAlpha));

			// Rotate the page along its y axis.
			ViewCompat.setPivotX(page, pageWidth * mCameraPosition.originPivotXFactor);
			ViewCompat.setPivotX(page, pageHeight * 0.5f);
			ViewCompat.setRotationY(page, factor * mCameraPosition.destDegrees);
		}
		/**
		 * This page is way off-screen to the end.
		 * (1,+Infinity]
		 */
		else {
			ViewCompat.setAlpha(page, 0);
		}
	}

	/**
	 * Specifies an alpha value used for completely transformed page at the start/end.
	 * <p>
	 * This alpha is also used as base when calculating alpha value for a relative transformed position.
	 *
	 * @param alpha The desired alpha from the range {@code [0.0, 1.0]}. Value {@code 0.0} would
	 *              result in fully transparent page and value {@code 1.0} would result in fully
	 *              opaque page when completely transformed at the start/end.
	 * @see #getMinAlpha()
	 */
	public void setMinAlpha(@FloatRange(from = 0, to = 1) float alpha) {
		this.mMinAlpha = alpha;
	}

	/**
	 * Returns the minimum alpha used for completely transformed page.
	 *
	 * @return Alpha value from the range {@code [0.0, 1.0]}.
	 * @see #setMinAlpha(float)
	 */
	@FloatRange(from = 0, to = 1)
	public float getMinAlpha() {
		return mMinAlpha;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
