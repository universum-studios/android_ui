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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import universum.studios.android.ui.R;
import universum.studios.android.ui.controller.RefreshController;

/**
 * <b>Note, that this layout is not finished yet and the refresh feature is not working.</b>
 * <p>
 * Layout that can host a single <b>refreshable</b> view.
 *
 * <h4>Usage in Xml layout</h4>
 * <pre>
 * &lt;universum.studios.android.ui.widget.RefreshableLayout
 *          xmlns:android="http://schemas.android.com/apk/res/android"
 *          xmlns:app="http://schemas.android.com/apk/res-auto"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"&gt;
 *
 *          &lt;universum.studios.android.ui.widget.EmptyView
 *                  android:layout_width="wrap_content"
 *                  android:layout_height="wrap_content"
 *                  android:layout_gravity="center"/&gt;
 *
 *          &lt;ListView
 *                  android:id="@android:id/list"
 *                  android:layout_width="wrap_content"
 *                  android:layout_height="wrap_content"
 *                  app:uiRefreshable="true"/&gt;
 *
 * &lt;/universum.studios.android.ui.widget.RefreshableLayout&gt;
 * </pre>
 *
 * @author Martin Albedinsky
 */
class RefreshableLayout extends FrameLayoutWidget implements Refreshable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "RefreshableLayout";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Controller used to support refresh feature for single refreshable child view of this layout.
	 */
	private RefreshController mRefreshController;

	/**
	 * Helper used to resolve whether the refreshable child view is scrolled at its start or at
	 * its end or neither.
	 */
	private ScrollableWrapper mScrollableWrapper;

	/**
	 * Single refreshable view presented within this layout.
	 */
	private View mRefreshableView;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #RefreshableLayout(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public RefreshableLayout(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #RefreshableLayout(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link R.attr#uiRefreshableLayoutStyle} as attribute for default style.
	 */
	public RefreshableLayout(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.uiRefreshableLayoutStyle);
	}

	/**
	 * Same as {@link #RefreshableLayout(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public RefreshableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of RefreshLayout within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public RefreshableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Called from one of constructors of this view to perform its initialization.
	 * <p>
	 * Initialization is done via parsing of the specified <var>attrs</var> set and obtaining for
	 * this view specific data from it that can be used to configure this new view instance. The
	 * specified <var>defStyleAttr</var> and <var>defStyleRes</var> are used to obtain default data
	 * from the current theme provided by the specified <var>context</var>.
	 */
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		ensureDecorator();
		mDecorator.updatePrivateFlags(PrivateFlags.PFLAG_REFRESH_ENABLED, true);
		this.ensureRefreshController();
		mRefreshController.setUpFromAttrs(context, attrs, defStyleAttr, defStyleRes);

		final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_RefreshableLayout, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_RefreshableLayout_uiRefreshEnabled) {
					setRefreshEnabled(typedArray.getBoolean(index, true));
				}
			}
			typedArray.recycle();
		}
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(RefreshableLayout.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(RefreshableLayout.class.getName());
	}

	/**
	 */
	@Override
	protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
		return new RefreshableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	/**
	 */
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
		return new RefreshableLayout.LayoutParams(params);
	}

	/**
	 */
	@Override
	public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new RefreshableLayout.LayoutParams(getContext(), attrs);
	}

	/**
	 */
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
		return params instanceof RefreshableLayout.LayoutParams;
	}

	/**
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.attachRefreshableView();
	}

	/**
	 */
	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		this.attachRefreshableView();
	}

	/**
	 * Called to attach a new refreshable view due to fact that view hierarchy of this view group
	 * has changed.
	 */
	private void attachRefreshableView() {
		if (mRefreshableView == null) {
			final int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = getChildAt(i);
				if (((LayoutParams) child.getLayoutParams()).refreshable) {
					this.mScrollableWrapper = ScrollableWrapper.wrapScrollableView(mRefreshableView = child);
					return;
				}
			}
		}
	}

	/**
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (mRefreshableView != null) mRefreshableView.setEnabled(enabled);
	}

	/**
	 */
	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (mRefreshableView != null) mRefreshableView.setPressed(pressed);
	}

	/**
	 */
	@Override
	public void offsetTopAndBottom(int offset) {
		if (mRefreshableView != null) mRefreshableView.offsetTopAndBottom(offset);
		else super.offsetTopAndBottom(offset);
	}

	/**
	 */
	@Override
	public void setRefreshEnabled(boolean enabled) {
		ensureDecorator();
		mDecorator.updatePrivateFlags(PrivateFlags.PFLAG_REFRESH_ENABLED, enabled);
		if (enabled) {
			this.ensureRefreshController();
			if (isAttachedToWindow()) {
				mRefreshController.dispatchViewAttachedToWindow();
				mRefreshController.dispatchViewSizeChanged(getWidth(), getHeight());
			}
		}
	}

	/**
	 */
	@Override
	public boolean isRefreshEnabled() {
		ensureDecorator();
		return mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED);
	}

	/**
	 */
	@Override
	public void setRefreshGestureEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.updatePrivateFlags(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED, enabled);
	}

	/**
	 */
	@Override
	public boolean isRefreshGestureEnabled() {
		this.ensureDecorator();
		return mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED);
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
		ensureDecorator();
		mDecorator.updatePrivateFlags(PrivateFlags.PFLAG_DRAW_REFRESH_INDICATOR, draw);
	}

	/**
	 */
	@Override
	public boolean drawsRefreshIndicator() {
		ensureDecorator();
		return mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_DRAW_REFRESH_INDICATOR);
	}

	/**
	 */
	@Override
	public void setOnRefreshListener(@Nullable OnRefreshListener listener) {
		this.ensureRefreshController();
		mRefreshController.setOnRefreshListener(listener);
	}

	/**
	 * Returns a controller used to support the <b>refresh</b> feature for this view.
	 *
	 * @return RefreshController of this refreshable view.
	 */
	@NonNull
	public RefreshController getRefreshController() {
		this.ensureRefreshController();
		return mRefreshController;
	}

	/**
	 * Ensures that the refresh controller is initialized.
	 */
	private void ensureRefreshController() {
		if (mRefreshController == null) this.mRefreshController = new RefreshController<>(this);
	}

	/**
	 * Always returns {@link Orientation#VERTICAL}.
	 */
	@Override
	public int getOrientation() {
		// Support refresh feature only for VERTICALLY oriented views.
		return Orientation.VERTICAL;
	}

	/**
	 */
	@Override
	public boolean isScrolledAtStart() {
		return mScrollableWrapper == null || mScrollableWrapper.isScrolledAtStart();
	}

	/**
	 * Always returns {@code false}.
	 */
	@Override
	public boolean isScrolledAtEnd() {
		// This information is not relevant for the refresh feature.
		return false;
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mRefreshController != null) mRefreshController.dispatchViewSizeChanged(w, h);
	}

	/**
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mRefreshController != null) mRefreshController.dispatchViewAttachedToWindow();
	}

	/**
	 */
	@Override
	public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
		ensureDecorator();
		final boolean refreshGestureEnabled = mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED) && mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED);
		return (refreshGestureEnabled && mRefreshController.shouldInterceptTouchEvent(event)) || super.onInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		ensureDecorator();
		final boolean refreshGestureEnabled = mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED) && mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_GESTURE_ENABLED);
		return (refreshGestureEnabled && mRefreshController.processTouchEvent(event)) || super.onTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean verifyDrawable(Drawable drawable) {
		return (mRefreshController != null && mRefreshController.verifyRefreshIndicatorDrawable(drawable)) || super.verifyDrawable(drawable);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		ensureDecorator();
		if (mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_REFRESH_ENABLED | PrivateFlags.PFLAG_DRAW_REFRESH_INDICATOR)) {
			// Draw refresh indicator on the top of the attached widget group's content.
			mRefreshController.drawRefreshIndicator(canvas);
		}
	}

	/**
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mRefreshController != null) mRefreshController.dispatchViewDetachedFromWindow();
	}

	/**
	 */
	@Override
	public void removeAllViews() {
		super.removeAllViews();
		this.detachRefreshableView();
	}

	/**
	 */
	@Override
	public void removeView(View view) {
		super.removeView(view);
		this.detachRefreshableView();
	}

	/**
	 */
	@Override
	public void removeViewAt(int index) {
		super.removeViewAt(index);
		this.detachRefreshableView();
	}

	/**
	 */
	@Override
	public void removeViews(int start, int count) {
		super.removeViews(start, count);
		this.detachRefreshableView();
	}

	/**
	 * Called to detach the refreshable view due to fact that view hierarchy of this view group has
	 * changed and the refreshable view may not be already presented within it.
	 */
	private void detachRefreshableView() {
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			if (((LayoutParams) child.getLayoutParams()).refreshable) return;
		}
		this.mRefreshableView = null;
		this.mScrollableWrapper = null;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Per-child layout information for child views of {@link RefreshableLayout}.
	 * <p>
	 * See {@link R.styleable#Ui_RefreshableLayout_LayoutParams Layout Attributes} for a list of all
	 * child view attributes that this class supports.
	 *
	 * @author Martin Albedinsky
	 */
	public static class LayoutParams extends FrameLayoutWidget.LayoutParams {

		/**
		 * Boolean flag indicating that a view associated with these parameters is refreshable.
		 */
		public boolean refreshable;

		/**
		 * Creates a new instance of LayoutParams configured from the specified <var>attrs</var>.
		 *
		 * @param context Context used to parse the given attributes set.
		 * @param attrs   The attributes set from which to configure the new layout params.
		 */
		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_RefreshableLayout_LayoutParams, 0, 0);
			if (typedArray != null) {
				final int n = typedArray.getIndexCount();
				for (int i = 0; i < n; i++) {
					final int index = typedArray.getIndex(i);
					if (index == R.styleable.Ui_RefreshableLayout_LayoutParams_uiRefreshable) {
						this.refreshable = typedArray.getBoolean(index, refreshable);
					}
				}
				typedArray.recycle();
			}
		}

		/**
		 * Creates a new instance of LayoutParams with the specified size parameters.
		 *
		 * @param width  The width attribute for the new layout params.
		 * @param height The height attribute for the new layout params.
		 */
		public LayoutParams(int width, int height) {
			super(width, height);
		}

		/**
		 * Creates a new instance of LayoutParams with the specified size and gravity parameters.
		 *
		 * @param width   The width attribute for the new layout params.
		 * @param height  The height attribute for the new layout params.
		 * @param gravity The gravity attribute for the new layout params.
		 */
		public LayoutParams(int width, int height, int gravity) {
			super(width, height, gravity);
		}

		/**
		 * Creates a new instance of LayoutParams from the given <var>source</var> parameters.
		 *
		 * @param source The source layout parameters from which to configure the new layout params.
		 */
		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		/**
		 * Creates a new instance of LayoutParams from the given <var>source</var> parameters.
		 *
		 * @param source The source layout parameters from which to configure the new layout params.
		 */
		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		/**
		 * Creates a new instance of LayoutParams from the given <var>source</var> parameters.
		 *
		 * @param source The source layout parameters from which to configure the new layout params.
		 */
		@TargetApi(Build.VERSION_CODES.KITKAT)
		public LayoutParams(FrameLayout.LayoutParams source) {
			super(source);
		}
	}
}
