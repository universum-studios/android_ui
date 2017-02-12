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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import universum.studios.android.ui.controller.PullController;

/**
 * A {@link WidgetGroupDecorator} implementation for {@link Pullable} widget groups.
 *
 *
 * @param <W> A type of the pullable widget that will use this decorator.
 * @author Martin Albedinsky
 */
abstract class PullableDecorator<W extends ViewGroup & Pullable> extends WidgetGroupDecorator<W> implements Pullable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "PullableDecorator";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Controller used to support pull feature for attached widget group.
	 */
	PullController mPullController;

	/**
	 * Helper used to resolve whether the attached widget group is scrolled at its start or at
	 * its end or neither.
	 */
	private ScrollableWrapper<W> mScrollableWrapper;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of PullableDecorator for the given <var>widgetGroup</var>.
	 *
	 * @param widgetGroup    The pullable widget group for which to create new decorator.
	 * @param styleableAttrs Set of styleable attributes specific for the widget group.
	 * @see WidgetGroupDecorator#WidgetGroupDecorator(ViewGroup, int[])
	 */
	PullableDecorator(W widgetGroup, int[] styleableAttrs) {
		super(widgetGroup, styleableAttrs);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	void processAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.setUpPullController(context, attrs, defStyleAttr, defStyleRes);
		super.processAttributes(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Performs set up process of the pull controller from the given <var>attrs</var>.
	 *
	 * @param context      The context that can be used to access resource values.
	 * @param attrs        Set of attributes passed to the widget group's constructor from which
	 *                     to set up the pull controller.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     the attached widget group within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the attached widget group.
	 */
	private void setUpPullController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.ensurePullController();
		if (!mPullController.setUpFromAttrs(context, attrs, defStyleAttr, defStyleRes)) {
			this.mPullController = null;
		}
	}

	/**
	 * Ensures that the pull controller is initialized.
	 */
	void ensurePullController() {
		if (mPullController == null) mPullController = new PullController<>(mWidget);
	}

	/**
	 */
	@Override
	public void setPullEnabled(boolean enabled) {
		this.updatePrivateFlags(PrivateFlags.PFLAG_PULL_ENABLED, enabled);
		if (enabled) this.ensurePullController();
	}

	/**
	 */
	@Override
	public boolean isPullEnabled() {
		return hasPrivateFlag(PrivateFlags.PFLAG_PULL_ENABLED);
	}

	/**
	 */
	@Override
	public int getOrientation() {
		// Ignored.
		return 0;
	}

	/**
	 * Returns the controller used to support the <b>pullable</b> feature for the attached view.
	 *
	 * @return PullController of the attached pullable view.
	 */
	@NonNull
	PullController getPullController() {
		this.ensurePullController();
		return mPullController;
	}

	/**
	 */
	@Override
	boolean onInterceptTouchEvent(MotionEvent event) {
		return (hasPrivateFlag(PrivateFlags.PFLAG_PULL_ENABLED) && mPullController.shouldInterceptTouchEvent(event)) || super.onInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	boolean onTouchEvent(MotionEvent event) {
		return (hasPrivateFlag(PrivateFlags.PFLAG_PULL_ENABLED) && mPullController.processTouchEvent(event)) || super.onTouchEvent(event);
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
	 * This should be called from the attached widget group whenever its
	 * {@link ViewGroup#onOverScrolled(int, int, boolean, boolean)} is invoked.
	 */
	void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		if (hasPrivateFlag(PrivateFlags.PFLAG_PULL_ENABLED)) {
			mPullController.dispatchOverScroll(scrollX, scrollY, clampedX, clampedY);
		}
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
