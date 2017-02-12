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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import universum.studios.android.ui.UiConfig;

/**
 * todo: description
 *
 * @author Martin Albedinsky
 */
final class WidgetSizeAssistant {

	// fixme: move some logic from here to WidgetSizeAnimator

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "ViewSizeAssistant";

	/**
	 * Flag indicating whether the output trough log-cat is enabled or not.
	 */
	// private final boolean LOG_ENABLED = true;

	/**
	 * Flag indicating whether the debug output trough log-cat is enabled or not.
	 */
	private final boolean DEBUG_ENABLED = UiConfig.DEBUG_LOG_ENABLED;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 *
	 */
	PendingAnimation mPendingAnimation;

	/**
	 *
	 */
	private final View mView;

	/**
	 *
	 */
	private final ViewGroup.LayoutParams mViewParams;

	/**
	 *
	 */
	private final ViewInfo mViewInfo;

	/**
	 *
	 */
	private SizeAnimator mSizeAnimator;

	/**
	 *
	 */
	private boolean mAnimationRunning;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 *
	 * @param view
	 */
	public WidgetSizeAssistant(View view) {
		if (view == null) {
			throw new NullPointerException("Invalid view.");
		}
		this.mView = view;
		this.mViewParams = view.getLayoutParams();
		this.mViewInfo = new ViewInfo(view);
		/**
		 * Resolve preferred view height.
		 */
		final int width = view.getWidth();
		final int height = view.getHeight();
		if (width > 0 && height > 0) {
			mViewInfo.preferredWidth = width;
			mViewInfo.preferredHeight = height;
		}

		/**
		 * Resolve view collapsible state.
		 */
		final boolean visibilityGone = mView.getVisibility() == View.GONE;
		/*this.bCollapsedHorizontally = mPreferredWidth == 0 || visibilityGone;
		this.bCollapsedVertically = mPreferredHeight == 0 || visibilityGone;*/

		if (visibilityGone) {
			//final float originalAlpha = mView.getAlpha();
			mView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					mView.removeOnLayoutChangeListener(this);
					mViewInfo.preferredWidth = mView.getWidth();
					mViewInfo.preferredHeight = mView.getHeight();
					mView.setVisibility(View.GONE);
					mViewInfo.ignoreViewInvisibility = false;
					//mView.setAlpha(originalAlpha);
					if (mPendingAnimation != null) {
						mPendingAnimation.start();
						mPendingAnimation = null;
					}
				}
			});
			//mView.setAlpha(0);
			// Request layout by changing visibility to find out view size.
			mViewInfo.ignoreViewInvisibility = true;
			mView.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Public --------------------------------------------------------------------------------------
	 */

	/**
	 *
	 * @return
	 */
	public SizeAnimator animateSize() {
		if (mSizeAnimator == null) {
			mSizeAnimator = new SizeAnimator(this);
		}
		mSizeAnimator.innerRequest = false;
		return mSizeAnimator;
	}

	/**
	 *
	 * @param width
	 * @param height
	 * @return
	 */
	public boolean requestViewNewSize(int width, int height) {
		return this.updateViewSize(width, height);
	}

	/**
	 *
	 * @param width
	 * @return
	 */
	public boolean requestViewNewWidth(int width) {
		return this.updateViewSize(width, mView.getHeight());
	}

	/**
	 *
	 * @param height
	 * @return
	 */
	public boolean requestViewNewHeight(int height) {
		return this.updateViewSize(mView.getWidth(), height);
	}

	/**
	 *
	 * @param desiredWidth
	 * @param desiredHeight
	 * @return
	 */
	public int[] obtainViewSize(int desiredWidth, int desiredHeight) {
		return this.measureViewInner(desiredWidth, desiredHeight);
	}

	/**
	 *
	 * @param desiredHeight
	 * @return
	 */
	public int obtainViewHeight(int desiredHeight) {
		return this.measureViewInner(0, desiredHeight)[1];
	}

	/**
	 *
	 * @param desiredWidth
	 * @return
	 */
	public int obtainViewWidth(int desiredWidth) {
		return this.measureViewInner(desiredWidth, 0)[0];
	}

	/**
	 *
	 * @param newWidth
	 * @param newHeight
	 */
	public void dispatchViewSizeChanged(int newWidth, int newHeight) {
		if (!mAnimationRunning) {
			mViewInfo.preferredWidth = newWidth;
			mViewInfo.preferredHeight = newHeight;
		}
	}

	/**
	 * Getters + Setters ---------------------------------------------------------------------------
	 */

	/**
	 *
	 * @return
	 */
	public View getView() {
		return mView;
	}

	/**
	 *
	 * @return
	 */
	public boolean isViewCollapsed() {
		return isViewWidthCollapsed() && isViewHeightCollapsed();
	}

	/**
	 *
	 * @return
	 */
	public boolean isViewWidthCollapsed() {
		return mViewInfo.visibleWidth() == 0;
	}

	/**
	 *
	 * @return
	 */
	public boolean isViewHeightCollapsed() {
		return mViewInfo.visibleHeight() == 0;
	}

	/**
	 *
	 * @return
	 */
	public boolean isViewSizeAvailable() {
		return mView.getWidth() > 0 && mView.getHeight() > 0;
	}

	/**
	 *
	 * @return
	 */
	public boolean isViewHeightAvailable() {
		return mView.getHeight() > 0;
	}

	/**
	 *
	 * @return
	 */
	public boolean isViewWidthAvailable() {
		return mView.getWidth() > 0;
	}

	/**
	 * Protected -----------------------------------------------------------------------------------
	 */

	/**
	 *
	 */
	void dispatchAnimationStarted() {
		this.mAnimationRunning = true;
		if (mView.getVisibility() == View.VISIBLE && isViewSizeAvailable()) {
			mViewInfo.preferredWidth = mView.getWidth();
			mViewInfo.preferredHeight = mView.getHeight();
		}
	}

	/**
	 *
	 */
	void dispatchAnimationFinished() {
		this.mAnimationRunning = false;
		if (mView.getVisibility() == View.VISIBLE && isViewSizeAvailable()) {
			mViewInfo.preferredWidth = mView.getWidth();
			mViewInfo.preferredHeight = mView.getHeight();
		}
	}

	/**
	 * Private -------------------------------------------------------------------------------------
	 */

	/**
	 * @return
	 */
	private boolean isViewMeasured() {
		return mViewParams.width > 0 && mViewParams.height > 0;
	}

	/**
	 * @param desiredWidth
	 * @param desiredHeight
	 * @return
	 */
	private int[] measureViewInner(int desiredWidth, int desiredHeight) {
		if (!isViewMeasured()) {
			return measureViewInner(
					desiredWidth, View.MeasureSpec.UNSPECIFIED,
					desiredHeight, View.MeasureSpec.UNSPECIFIED
			);
		}
		final int[] size = new int[2];
		size[0] = mView.getMeasuredWidth();
		size[1] = mView.getMeasuredHeight();
		return size;
	}

	/**
	 * @param desiredWidth
	 * @param widthMode
	 * @param desiredHeight
	 * @param heightMode
	 * @return
	 */
	private int[] measureViewInner(int desiredWidth, int widthMode, int desiredHeight, int heightMode) {
		final int[] size = new int[2];
		mView.measure(
				View.MeasureSpec.makeMeasureSpec(desiredWidth, widthMode),
				View.MeasureSpec.makeMeasureSpec(desiredHeight, heightMode)
		);
		mView.requestLayout();
		size[0] = mView.getMeasuredWidth();
		size[1] = mView.getMeasuredHeight();
		if (DEBUG_ENABLED) {
			Log.d(TAG, "measureViewInner() width:" + size[0] + " height:" + size[1]);
		}
		return size;
	}

	/**
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	boolean updateViewSize(int newWidth, int newHeight) {
		boolean changed = false;
		if (newWidth != mView.getWidth() || newHeight != mView.getHeight()) {
			if (mViewParams != null) {
				mViewParams.width = newWidth;
				mViewParams.height = newHeight;
				mView.setLayoutParams(mViewParams);
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Abstract methods ----------------------------------------------------------------------------
	 */

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * todo: description
	 *
	 * @author Martin Albedinsky
	 */
	public static class SizeAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

		/**
		 * Constants ===============================================================================
		 */

		/**
		 *
		 */
		private static final int HEIGHT = 0x01;

		/**
		 *
		 */
		private static final int WIDTH = 0x02;

		/**
		 *
		 */
		private static final int RESET_ALPHA_OFFSET = 5;

		/**
		 * Members =================================================================================
		 */

		/**
		 *
		 */
		int property = 0;

		/**
		 *
		 */
		boolean collapsing;

		/**
		 *
		 */
		private final ViewInfo info;

		/**
		 *
		 */
		private final WidgetSizeAssistant sizeAssistant;

		/**
		 *
		 */
		private AnimatorListener listener;

		/**
		 *
		 */
		private boolean innerRequest = false;

		/**
		 *
		 */
		private float originalAlpha = 1;

		/**
		 *
		 */
		private boolean resetAlpha = false;

		/**
		 * Constructors ============================================================================
		 */

		/**
		 *
		 * @param assistant
		 */
		public SizeAnimator(WidgetSizeAssistant assistant) {
			addListener(this);
			addUpdateListener(this);
			this.sizeAssistant = assistant;
			this.info = assistant.mViewInfo;
		}

		/**
		 * Methods =================================================================================
		 */

		/**
		 *
		 * @return
		 */
		public SizeAnimator collapseHeight() {
			this.collapsing = true;
			this.innerRequest = true;
			this.property = HEIGHT;
			setIntValues(info.visibleHeight(), 0);
			return this;
		}

		/**
		 *
		 * @return
		 */
		public SizeAnimator expandHeight() {
			this.collapsing = false;
			this.innerRequest = true;
			this.property = HEIGHT;
			setIntValues(info.visibleHeight(), info.preferredHeight);
			return this;
		}

		/**
		 *
		 * @return
		 */
		public SizeAnimator collapseWidth() {
			this.collapsing = true;
			this.innerRequest = true;
			this.property = WIDTH;
			setIntValues(info.preferredWidth, 0);
			return this;
		}

		/**
		 *
		 * @return
		 */
		public SizeAnimator expandWidth() {
			this.collapsing = false;
			this.innerRequest = true;
			this.property = WIDTH;
			setIntValues(info.visibleWidth(), info.preferredWidth);
			return this;
		}

		/**
		 *
		 * @param value
		 * @return
		 */
		public SizeAnimator height(int value) {
			// todo:
			/*final int height = info.visibleHeight();
			this.collapsing = value < height;
			this.innerRequest = true;
			this.property = HEIGHT;
			setIntValues(height, value);*/
			return this;
		}

		/**
		 *
		 * @param value
		 * @return
		 */
		public SizeAnimator width(int value) {
			// todo:
			return this;
		}

		/**
		 *
		 * @param value
		 * @return
		 */
		public SizeAnimator heightBy(int value) {
			// todo:
			return this;
		}

		/**
		 *
		 * @param value
		 * @return
		 */
		public SizeAnimator widthBy(int value) {
			// todo:
			return this;
		}

		/**
		 */
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			final int value = (int) animation.getAnimatedValue();
			switch (property) {
				case HEIGHT:
					sizeAssistant.requestViewNewHeight(value);
					break;
				case WIDTH:
					sizeAssistant.requestViewNewWidth(value);
					break;
			}
			if (resetAlpha && value > RESET_ALPHA_OFFSET) {
				info.view.setAlpha(originalAlpha);
				this.resetAlpha = false;
			}
		}

		/**
		 */
		@Override
		public void onAnimationStart(Animator animation) {
			sizeAssistant.dispatchAnimationStarted();
			if (innerRequest) {
				if (!collapsing && info.visibility() != View.VISIBLE) {
					// Do not show the view blink when changing visibility to View.VISIBLE, so
					// to hide such a behaviour we will set for initial phase of animation its
					// alpha property to 0 and than after RESET_ALPHA_OFFSET this alpha property
					// will be returned to its original value.
					this.originalAlpha = info.alpha();
					info.view.setAlpha(0);
					this.resetAlpha = true;
					info.view.setVisibility(View.VISIBLE);
				}
			}
			if (listener != null) {
				listener.onAnimationStart(animation);
			}
		}

		/**
		 */
		@Override
		public void onAnimationEnd(Animator animation) {
			if (innerRequest) {
				if (collapsing) {
					info.view.setVisibility(View.GONE);
				}
				switch (property) {
					case HEIGHT:
						if (collapsing) {
							sizeAssistant.requestViewNewHeight(0);
						}
						break;
					case WIDTH:
						if (collapsing) {
							sizeAssistant.requestViewNewWidth(0);
						}
						break;
				}
			}
			sizeAssistant.dispatchAnimationFinished();
			if (listener != null) {
				listener.onAnimationEnd(animation);
			}
		}

		/**
		 */
		@Override
		public void onAnimationCancel(Animator animation) {
			sizeAssistant.dispatchAnimationFinished();
			if (listener != null) {
				listener.onAnimationCancel(animation);
			}
		}

		/**
		 */
		@Override
		public void onAnimationRepeat(Animator animation) {
			if (listener != null) {
				listener.onAnimationRepeat(animation);
			}
		}

		/**
		 */
		@Override
		public void setIntValues(int... values) {
			super.setIntValues(values);
		}

		/**
		 */
		@Override
		public void start() {
			if (!collapsing) {
				switch (property) {
					case HEIGHT:
						if (info.preferredHeight == 0) {
							sizeAssistant.mPendingAnimation = new PendingAnimation(this);
							return;
						}
						break;
					case WIDTH:
						if (info.preferredWidth == 0) {
							sizeAssistant.mPendingAnimation = new PendingAnimation(this);
							return;
						}
						break;
				}
			}
			super.start();
		}

		/**
		 *
		 * @param listener
		 * @return
		 */
		public SizeAnimator setListener(AnimatorListener listener) {
			this.listener = listener;
			return this;
		}

		/**
		 *
		 */
		void performStart() {
			super.start();
		}
	}

	/**
	 *
	 */
	private static class PendingAnimation {

		/**
		 * Members =================================================================================
		 */

		/**
		 *
		 */
		SizeAnimator animator;

		/**
		 * Constructors ============================================================================
		 */

		/**
		 *
		 * @param animator
		 */
		PendingAnimation(SizeAnimator animator) {
			this.animator = animator;
		}

		/**
		 * Methods =================================================================================
		 */

		/**
		 *
		 */
		void start() {
			final boolean collapseRequest = animator.collapsing;
			switch (animator.property) {
				case SizeAnimator.HEIGHT:
					if (collapseRequest) {
						animator.collapseHeight();
					} else {
						animator.expandHeight();
					}
					break;
				case SizeAnimator.WIDTH:
					if (collapseRequest) {
						animator.collapseWidth();
					} else {
						animator.expandWidth();
					}
					break;
			}
			animator.performStart();
		}
	}

	/**
	 *
	 */
	private static class ViewInfo {

		/**
		 * Members =================================================================================
		 */

		/**
		 *
		 */
		final View view;

		/**
		 *
		 */
		final ViewGroup.LayoutParams viewParams;

		/**
		 *
		 */
		boolean ignoreViewInvisibility = false;

		/**
		 *
		 */
		int preferredWidth, preferredHeight;

		/**
		 * Constructors ============================================================================
		 */

		/**
		 *
		 * @param view
		 */
		ViewInfo(View view) {
			this.view = view;
			this.viewParams = view.getLayoutParams();
		}

		/**
		 * Methods =================================================================================
		 */

		/**
		 *
		 * @return
		 */
		int height() {
			return view.getHeight();
		}

		/**
		 *
		 * @return
		 */
		int visibleHeight() {
			return (visibility() != View.GONE && !ignoreViewInvisibility) ? viewParams.height : 0;
		}

		/**
		 *
		 * @return
		 */
		int paramsHeight() {
			return viewParams.height;
		}

		/**
		 *
		 * @return
		 */
		int width() {
			return view.getWidth();
		}

		/**
		 *
		 * @return
		 */
		int visibleWidth() {
			return (visibility() != View.GONE && !ignoreViewInvisibility) ? viewParams.width : 0;
		}

		/**
		 *
		 * @return
		 */
		int paramsWidth() {
			return viewParams.width;
		}

		/**
		 *
		 * @return
		 */
		int visibility() {
			return view.getVisibility();
		}

		/**
		 *
		 * @return
		 */
		float alpha() {
			return view.getAlpha();
		}
	}
}
