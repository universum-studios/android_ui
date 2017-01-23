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
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * A {@link ViewPager.PageTransformer} implementation that can be used to transform pages in a way
 * where one page is being translated from the start/end and the other one is being scaled up/down
 * with alpha change that is in this case called <b>depth transformation</b>.
 * <p>
 * Whether to apply depth transformation at the <b>start</b> or <b>end</b> can be specified via
 * {@link #DepthPageTransformer(DepthOrigin)} constructor.
 *
 * @author Martin Albedinsky
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "DepthPageTransformer";

	/**
	 * Default minimum scale factor for completely transformed pages.
	 *
	 * @see #setMinScaleFactor(float)
	 */
	public static final float MIN_SCALE_FACTOR = 0.75f;

	/**
	 * Default minimum alpha for completely transformed pages.
	 *
	 * @see #setMinAlpha(float)
	 */
	public static final float MIN_ALPHA = 0.0f;

	/**
	 * Represents origin on which to apply page depth transformation.
	 *
	 * @author Martin Albedinsky
	 * @see DepthPageTransformer#DepthPageTransformer(DepthOrigin)
	 */
	public enum DepthOrigin {
		/**
		 * Apply dept transformation on the start.
		 */
		START(false),
		/**
		 * Apply depth transformation on the end.
		 */
		END(true);

		/**
		 * Boolean flag indicating whether to draw pages in pager in reversed order or not.
		 *
		 * @see ViewPager#setPageTransformer(boolean, ViewPager.PageTransformer)
		 */
		public final boolean reverseDrawingOrder;

		/**
		 * Creates a new instance of DepthOrigin with the specified <var>reverseDrawing</var> boolean
		 * flag.
		 *
		 * @param reverseDrawing {@code True} to draw pages in pager in reversed order, {@code false}
		 *                       otherwise.
		 */
		DepthOrigin(boolean reverseDrawing) {
			this.reverseDrawingOrder = reverseDrawing;
		}
	}

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Origin used to resolve where to apply depth transformation.
	 */
	private final DepthOrigin mOrigin;

	/**
	 * Minimum scale factor for the completely transformed page at the depth origin.
	 */
	private float mMinScaleFactor = MIN_SCALE_FACTOR;

	/**
	 * Minimum alpha for the completely transformed page at the depth origin.
	 */
	private float mMinAlpha = MIN_ALPHA;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of DepthPageTransformer with {@link DepthOrigin#START} as <var>depthOrigin</var>.
	 */
	public DepthPageTransformer() {
		this(DepthOrigin.START);
	}

	/**
	 * Creates a new instance of DepthPageTransformer with the specified <var>depthOrigin</var>.
	 *
	 * @param depthOrigin The desired origin where to apply depth transformation.
	 */
	public DepthPageTransformer(DepthOrigin depthOrigin) {
		this.mOrigin = depthOrigin;
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
			switch (mOrigin) {
				case START:
					// Fade the page in/out.
					ViewCompat.setAlpha(page, Math.max(1 - factor, mMinAlpha));

					// Counteract the default slide transition.
					ViewCompat.setTranslationX(page, pageWidth * factor);

					// Scale the page down/up.
					final float scaleFactor = mMinScaleFactor + ((1 - mMinScaleFactor) * (1 - factor));
					ViewCompat.setScaleX(page, scaleFactor);
					ViewCompat.setScaleY(page, scaleFactor);
					break;
				case END:
					// Allow default slide transition.
					ViewCompat.setAlpha(page, 1);
					ViewCompat.setTranslationX(page, 0);
					ViewCompat.setScaleX(page, 1);
					ViewCompat.setScaleY(page, 1);
					break;
			}
		}
		/**
		 * Moving page from/to the end.
		 * (0,1]
		 */
		else if (position <= 1) {
			switch (mOrigin) {
				case START:
					// Allow default slide transition.
					ViewCompat.setAlpha(page, 1);
					ViewCompat.setTranslationX(page, 0);
					ViewCompat.setScaleX(page, 1);
					ViewCompat.setScaleY(page, 1);
					break;
				case END:
					// Fade the page in/out.
					ViewCompat.setAlpha(page, Math.max(1 - factor, mMinAlpha));

					// Counteract the default slide transition.
					ViewCompat.setTranslationX(page, pageWidth * -factor);

					// Scale the page down/up.
					final float scaleFactor = mMinScaleFactor + ((1 - mMinScaleFactor) * (1 - factor));
					ViewCompat.setScaleX(page, scaleFactor);
					ViewCompat.setScaleY(page, scaleFactor);
					break;
			}
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
	 * Specifies a scale factor for completely transformed page at the depth origin.
	 * <p>
	 * This scale factor value is also used as base when calculating scale value for a relative
	 * transformed position.
	 *
	 * @param scaleFactor The desired scale factor from the range {@code [0.0, 1.0]}. Value {@code 0.0}
	 *                    would result in fully down-scaled page and value {@code 1.0} would result
	 *                    in fully up-scaled page when completely transformed at the depth origin.
	 * @see #getMinScaleFactor()
	 */
	public void setMinScaleFactor(@FloatRange(from = 0, to = 1) float scaleFactor) {
		this.mMinScaleFactor = scaleFactor;
	}

	/**
	 * Returns the minimum scale factor used for completely transformed page.
	 *
	 * @return Factor value from the range {@code [0.0, 1.0]}.
	 * @see #setMinScaleFactor(float)
	 */
	@FloatRange(from = 0, to = 1)
	public float getMinScaleFactor() {
		return mMinScaleFactor;
	}

	/**
	 * Specifies an alpha value used for completely transformed page at the depth origin.
	 * <p>
	 * This alpha is also used as base when calculating alpha value for a relative transformed position.
	 *
	 * @param alpha The desired alpha from the range {@code [0.0, 1.0]}. Value {@code 0.0} would
	 *              result in fully transparent page and value {@code 1.0} would result in fully
	 *              opaque page when completely transformed at the depth origin.
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
