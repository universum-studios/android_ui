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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import universum.studios.android.ui.controller.RefreshController;

/**
 * A {@link WidgetGroupDecorator} implementation for {@link Refreshable} widget groups.
 *
 * @param <W> Type of the refreshable widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class RefreshableDecorator<W extends ViewGroup & Refreshable> extends WidgetGroupDecorator<W> implements Refreshable {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "RefreshableDecorator";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Controller used to support refresh feature for attached widget group.
	 */
	private RefreshController<W> mRefreshController;

	/**
	 * Helper used to resolve whether the attached widget group is scrolled at its start or at its
	 * end or neither.
	 */
	private ScrollableWrapper<W> mScrollableWrapper;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of RefreshableDecorator for the given <var>widgetGroup</var>.
	 *
	 * @param widgetGroup    The refreshable widget group for which to create new decorator.
	 * @param styleableAttrs Set of styleable attributes specific for the widget group.
	 * @see WidgetGroupDecorator#WidgetGroupDecorator(ViewGroup, int[])
	 */
	RefreshableDecorator(W widgetGroup, int[] styleableAttrs) {
		super(widgetGroup, styleableAttrs);
		this.updatePrivateFlags(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED, true);
		this.updatePrivateFlags(PrivateFlags.PFLAG_DRAW_REFRESH_INDICATOR, true);
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	void processAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.setUpRefreshController(context, attrs, defStyleAttr, defStyleRes);
		super.processAttributes(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Performs set up process of the refresh controller from the given <var>attrs</var>.
	 *
	 * @param context      The context that can be used to access resource values.
	 * @param attrs        Set of attributes passed to the widget group's constructor from which
	 *                     to set up the refresh controller.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     the attached widget group within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the attached widget group.
	 */
	private void setUpRefreshController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.ensureRefreshController();
		if (!mRefreshController.setUpFromAttrs(context, attrs, defStyleAttr, defStyleRes)) {
			this.mRefreshController = null;
		}
	}

	/**
	 * Ensures that the refresh controller is initialized.
	 */
	private void ensureRefreshController() {
		if (mRefreshController == null) mRefreshController = new RefreshController<>(mWidget);
	}

	/**
	 */
	@Override
	public int getOrientation() {
		// Ignored.
		return 0;
	}

	/**
	 */
	@Override
	public void setRefreshEnabled(boolean enabled) {
		this.updatePrivateFlags(PrivateFlags.PFLAG_REFRESH_ENABLED, enabled);
		if (enabled) {
			this.ensureRefreshController();
			if ((mPrivateFlags & PrivateFlags.PFLAG_ATTACHED_TO_WINDOW) != 0) {
				mRefreshController.dispatchViewAttachedToWindow();
				mRefreshController.dispatchViewSizeChanged(mWidth, mHeight);
			}
		}
	}

	/**
	 */
	@Override
	public boolean isRefreshEnabled() {
		return hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED);
	}

	/**
	 */
	@Override
	public void setRefreshGestureEnabled(boolean enabled) {
		this.updatePrivateFlags(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED, enabled);
	}

	/**
	 */
	@Override
	public boolean isRefreshGestureEnabled() {
		return hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED);
	}

	/**
	 */
	@Override
	public void setRefreshing(boolean refreshing) {
		this.ensureRefreshController();
		mRefreshController.setRefreshing(refreshing);
	}

	/**
	 */
	@Override
	public boolean isRefreshing() {
		this.ensureRefreshController();
		return mRefreshController.isRefreshing();
	}

	/**
	 */
	@Override
	public void setRefreshIndicatorTransition(@IndicatorTransition int transition) {
		this.ensureRefreshController();
		mRefreshController.setRefreshIndicatorTransition(transition);
	}

	/**
	 */
	@Override
	@IndicatorTransition
	public int getRefreshIndicatorTransition() {
		this.ensureRefreshController();
		return mRefreshController.getRefreshIndicatorTransition();
	}

	/**
	 */
	@Override
	public void setDrawRefreshIndicator(boolean draw) {
		this.updatePrivateFlags(PrivateFlags.PFLAG_DRAW_REFRESH_INDICATOR, draw);
	}

	/**
	 */
	@Override
	public boolean drawsRefreshIndicator() {
		return hasPrivateFlag(PrivateFlags.PFLAG_DRAW_REFRESH_INDICATOR);
	}

	/**
	 */
	@Override
	public void setOnRefreshListener(@Nullable OnRefreshListener listener) {
		this.ensureRefreshController();
		mRefreshController.setOnRefreshListener(listener);
	}

	/**
	 * Returns a controller used to support the <b>refresh</b> feature for the attached view.
	 *
	 * @return RefreshController of the refreshable view.
	 */
	@NonNull
	RefreshController getRefreshController() {
		this.ensureRefreshController();
		return mRefreshController;
	}

	/**
	 */
	@Override
	boolean onInterceptTouchEvent(MotionEvent event) {
		final boolean refreshGestureEnabled = hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED) && hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED);
		return (refreshGestureEnabled && mRefreshController.shouldInterceptTouchEvent(event)) || super.onInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	boolean onTouchEvent(MotionEvent event) {
		final boolean refreshGestureEnabled = hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED) && hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED);
		return (refreshGestureEnabled && mRefreshController.processTouchEvent(event)) || super.onTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean isScrolledAtStart() {
		this.ensureScrollableWrapper();
		return mScrollableWrapper.isScrolledAtStart();
	}

	/**
	 */
	@Override
	public boolean isScrolledAtEnd() {
		this.ensureScrollableWrapper();
		return mScrollableWrapper.isScrolledAtEnd();
	}

	/**
	 * Ensures that the scrollable wrapper is initialized.
	 */
	private void ensureScrollableWrapper() {
		if (mScrollableWrapper == null) this.mScrollableWrapper = ScrollableWrapper.wrapScrollableView(mWidget);
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		super.onSizeChanged(width, height, oldWidth, oldHeight);
		if (mRefreshController != null) mRefreshController.dispatchViewSizeChanged(width, height);
	}

	/**
	 */
	@Override
	void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mRefreshController != null) mRefreshController.dispatchViewAttachedToWindow();
	}

	/**
	 */
	@Override
	boolean verifyDrawable(Drawable drawable) {
		return mRefreshController != null && mRefreshController.verifyRefreshIndicatorDrawable(drawable);
	}

	/**
	 */
	@Override
	void draw(Canvas canvas) {
		super.draw(canvas);
		if (hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED) && hasPrivateFlag(PrivateFlags.PFLAG_DRAW_REFRESH_INDICATOR)) {
			// Draw refresh indicator on the top of the attached widget group's content.
			mRefreshController.drawRefreshIndicator(canvas);
		}
	}

	/**
	 */
	@Override
	void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mRefreshController != null) mRefreshController.dispatchViewDetachedFromWindow();
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
