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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Adapter;
import android.widget.GridView;
import android.widget.WrapperListAdapter;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.controller.PullController;
import universum.studios.android.ui.controller.RefreshController;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.widget.AdapterViewWidget.SavedState;

/**
 * Extended version of {@link android.widget.GridView}. This updated GridView supports tinting
 * for the Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other
 * useful features described below including <b>swipe to refresh</b> and <b>pulling</b> feature.
 *
 * <h3>Tinting</h3>
 * Tinting is supported via Xml attributes listed below:
 * <ul>
 * <li>{@link R.attr#uiBackgroundTint uiBackgroundTint}</li>
 * <li>{@link R.attr#uiBackgroundTintMode uiBackgroundTintMode}</li>
 * </ul>
 * <p>
 * <b>Note, that on {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and above SDK versions
 * can be also used Xml attributes listed above where in such case will be used the native tinting.</b>
 * <p>
 * This widget group also overrides all SDK methods used to tint its components like {@link #setBackgroundTintList(android.content.res.ColorStateList)}
 * or {@link #setBackgroundTintMode(android.graphics.PorterDuff.Mode)}, so these can be used regardless
 * the current version of SDK but invoking of these methods below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
 * can be done only directly upon instance of this widget group otherwise {@link NoSuchMethodException}
 * will be thrown.
 *
 * <h3>Swipe to refresh</h3>
 * This feature can be enabled/disabled via {@link #setRefreshEnabled(boolean)} or via Xml attribute
 * {@link R.attr#uiRefreshEnabled uiRefreshEnabled}. To receive callback about initiated refresh you
 * need to register {@link OnRefreshListener} via {@link #setOnRefreshListener(OnRefreshListener)}.
 * Whenever its {@link OnRefreshListener#onRefresh(Refreshable)} callback is invoked a refresh indicator
 * is already visible and 'spinning'. When refresh process if finished the indicator can be hided
 * (dismissed) via {@link #setRefreshing(boolean) setRefreshing(false)}. This method can be also used
 * to show (pop) the indicator manually, for example when a refresh button has been pressed. For
 * configuration of refresh indicator and also all parameters used to support refresh feature see
 * {@link RefreshController} by which is this feature supported within this widget. Each refreshable
 * widget uses its own RefreshController that can be accessed via {@link #getRefreshController()}.
 *
 * <h3>Pulling</h3>
 * This feature can be enabled/disabled via {@link #setPullEnabled(boolean)} or via Xml attribute
 * {@link R.attr#uiPullEnabled uiPullEnabled}. For configuration of all parameters used to support
 * pulling feature see {@link PullController} by which is this feature supported within this widget.
 * To receive callback about initiated and performed pull gesture you need to register {@link OnPullListener}
 * via {@link PullController#registerOnPullListener(OnPullListener)}. Each pullable widget uses its
 * own PullController that can be accessed via {@link #getPullController()}. This widget, according
 * to its {@link Orientation#VERTICAL VERTICAL} orientation, can be pulled at its top or bottom
 * whenever its content is scrolled at the start or at the end of its total size.
 *
 * <h3>Sliding</h3>
 * This updated view group allows updating of its current position along <b>x</b> and <b>y</b> axis
 * by changing <b>fraction</b> of these properties depending on its current size using the new animation
 * framework introduced in {@link android.os.Build.VERSION_CODES#HONEYCOMB HONEYCOMB} via
 * {@link android.animation.ObjectAnimator ObjectAnimator}s API.
 * <p>
 * Changing of fraction of X or Y is supported via these two methods:
 * <ul>
 * <li>{@link #setFractionX(float)}</li>
 * <li>{@link #setFractionY(float)}</li>
 * </ul>
 * <p>
 * For example if an instance of this view group class needs to be slided to the right by its whole
 * width, an Xml file with ObjectAnimator would look like this:
 * <pre>
 *  &lt;objectAnimator xmlns:android="http://schemas.android.com/apk/res/android"
 *                  android:propertyName="fractionX"
 *                  android:valueFrom="0.0"
 *                  android:valueTo="1.0"
 *                  android:duration="300"/&gt;
 * </pre>
 * This can be especially useful for fragment transitions framework, where this view group would be
 * used as a root for a view hierarchy of a specific fragment.
 *
 * <h3>XML attributes</h3>
 * See {@link GridView},
 * {@link R.styleable#Ui_GridView GridViewWidget Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#gridViewStyle android:gridViewStyle}
 *
 * @author Martin Albedinsky
 * @see ListViewWidget
 * @see RecyclerViewWidget
 */
public class GridViewWidget extends GridView implements Widget, Pullable, Refreshable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "GridViewWidget";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Decorator used to extend API of this widget group by functionality otherwise not supported or
	 * not available due to current API level.
	 */
	private Decorator mDecorator;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #GridViewWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public GridViewWidget(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #GridViewWidget(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link android.R.attr#gridViewStyle} as attribute for default style.
	 */
	public GridViewWidget(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.gridViewStyle);
	}

	/**
	 * Same as {@link #GridViewWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public GridViewWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of GridViewWidget within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public GridViewWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		this.ensureDecorator();
		mDecorator.processAttributes(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Ensures that the decorator for this view is initialized.
	 */
	private void ensureDecorator() {
		if (mDecorator == null) this.mDecorator = new Decorator(this);
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(GridViewWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(GridViewWidget.class.getName());
	}

	/**
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(background);
		this.ensureDecorator();
		mDecorator.applyBackgroundTint();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setBackgroundTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Override
	public Drawable getBackground() {
		return super.getBackground();
	}

	/**
	 */
	@Override
	public void setBackgroundTintList(@Nullable ColorStateList tint) {
		this.ensureDecorator();
		mDecorator.setBackgroundTintList(tint);
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getBackgroundTintList() {
		this.ensureDecorator();
		return mDecorator.getBackgroundTintList();
	}

	/**
	 */
	@Override
	public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureDecorator();
		mDecorator.setBackgroundTintMode(tintMode);
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getBackgroundTintMode() {
		this.ensureDecorator();
		return mDecorator.getBackgroundTintMode();
	}

	/**
	 */
	@Override
	public void setFractionX(float fraction) {
		this.ensureDecorator();
		mDecorator.setFractionX(fraction);
	}

	/**
	 */
	@Override
	public float getFractionX() {
		this.ensureDecorator();
		return mDecorator.getFractionX();
	}

	/**
	 */
	@Override
	public void setFractionY(float fraction) {
		this.ensureDecorator();
		mDecorator.setFractionY(fraction);
	}

	/**
	 */
	@Override
	public float getFractionY() {
		this.ensureDecorator();
		return mDecorator.getFractionY();
	}

	/**
	 */
	@Override
	public void setPressed(boolean pressed) {
		final boolean isPressed = isPressed();
		super.setPressed(pressed);
		if (!isPressed && pressed) onPressed();
		else if (isPressed) onReleased();
	}

	/**
	 * Invoked whenever {@link #setPressed(boolean)} is called with {@code true} and this view
	 * isn't in the pressed state yet.
	 */
	protected void onPressed() {
	}

	/**
	 * Invoked whenever {@link #setPressed(boolean)} is called with {@code false} and this view
	 * is currently in the pressed state.
	 */
	protected void onReleased() {
	}

	/**
	 */
	@Override
	public void setSelected(boolean selected) {
		this.ensureDecorator();
		mDecorator.setSelected(selected);
	}

	/**
	 */
	@Override
	public void setSelectionState(boolean selected) {
		this.ensureDecorator();
		mDecorator.setSelectionState(selected);
	}

	/**
	 */
	@Override
	public void setAllowDefaultSelection(boolean allow) {
		this.ensureDecorator();
		mDecorator.setAllowDefaultSelection(allow);
	}

	/**
	 */
	@Override
	public boolean allowsDefaultSelection() {
		this.ensureDecorator();
		return mDecorator.allowsDefaultSelection();
	}

	/**
	 */
	@NonNull
	@Override
	public WidgetSizeAnimator animateSize() {
		this.ensureDecorator();
		return mDecorator.animateSize();
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mDecorator.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 */
	@Override
	public void setRefreshEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.setRefreshEnabled(enabled);
	}

	/**
	 */
	@Override
	public boolean isRefreshEnabled() {
		this.ensureDecorator();
		return mDecorator.isRefreshEnabled();
	}

	/**
	 */
	@Override
	public void setRefreshGestureEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.setRefreshGestureEnabled(enabled);
	}

	/**
	 */
	@Override
	public boolean isRefreshGestureEnabled() {
		this.ensureDecorator();
		return mDecorator.isRefreshGestureEnabled();
	}

	/**
	 */
	@Override
	public void setRefreshing(boolean refreshing) {
		this.ensureDecorator();
		mDecorator.setRefreshing(refreshing);
	}

	/**
	 */
	@Override
	public boolean isRefreshing() {
		this.ensureDecorator();
		return mDecorator.isRefreshing();
	}

	/**
	 */
	@Override
	public void setRefreshIndicatorTransition(@IndicatorTransition int transition) {
		this.ensureDecorator();
		mDecorator.setRefreshIndicatorTransition(transition);
	}

	/**
	 */
	@Override
	@IndicatorTransition
	public int getRefreshIndicatorTransition() {
		this.ensureDecorator();
		return mDecorator.getRefreshIndicatorTransition();
	}

	/**
	 */
	@Override
	public void setDrawRefreshIndicator(boolean draw) {
		this.ensureDecorator();
		mDecorator.setDrawRefreshIndicator(draw);
	}

	/**
	 */
	@Override
	public boolean drawsRefreshIndicator() {
		this.ensureDecorator();
		return mDecorator.drawsRefreshIndicator();
	}

	/**
	 */
	@Override
	public void setOnRefreshListener(@Nullable OnRefreshListener listener) {
		this.ensureDecorator();
		mDecorator.setOnRefreshListener(listener);
	}

	/**
	 * Returns a controller used to support the <b>refresh</b> feature for this view.
	 *
	 * @return RefreshController of this refreshable view.
	 */
	@NonNull
	public RefreshController getRefreshController() {
		this.ensureDecorator();
		return mDecorator.getRefreshController();
	}

	/**
	 */
	@Override
	public void setPullEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.setPullEnabled(enabled);
	}

	/**
	 */
	@Override
	public boolean isPullEnabled() {
		this.ensureDecorator();
		return mDecorator.isPullEnabled();
	}

	/**
	 * Returns the controller used to support the <b>pullable</b> feature for this view.
	 *
	 * @return PullController of this pullable view.
	 */
	@NonNull
	public PullController getPullController() {
		this.ensureDecorator();
		return mDecorator.getPullController();
	}

	/**
	 */
	@Override
	public boolean isScrolledAtStart() {
		this.ensureDecorator();
		return mDecorator.isScrolledAtStart();
	}

	/**
	 */
	@Override
	public boolean isScrolledAtEnd() {
		this.ensureDecorator();
		return mDecorator.isScrolledAtEnd();
	}

	/**
	 */
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
		this.ensureDecorator();
		mDecorator.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

	/**
	 */
	@Override
	public int getOrientation() {
		return Orientation.VERTICAL;
	}

	/**
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.ensureDecorator();
		mDecorator.onAttachedToWindow();
	}

	/**
	 */
	@Override
	public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
		this.ensureDecorator();
		return mDecorator.onInterceptTouchEvent(event) || super.onInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		this.ensureDecorator();
		return mDecorator.onTouchEvent(event) || super.onTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean verifyDrawable(Drawable drawable) {
		this.ensureDecorator();
		return mDecorator.verifyDrawable(drawable) || super.verifyDrawable(drawable);
	}

	/**
	 */
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		this.ensureDecorator();
		mDecorator.draw(canvas);
	}

	/**
	 */
	@Override
	@SuppressWarnings("NewApi")
	public boolean isAttachedToWindow() {
		this.ensureDecorator();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			return super.isAttachedToWindow();
		else
			return mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW);
	}

	/**
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		this.ensureDecorator();
		mDecorator.onDetachedFromWindow();
	}

	/**
	 */
	@Override
	public Parcelable onSaveInstanceState() {
		final Adapter adapter = getAdapter();
		if (adapter != null) {
			final Parcelable adapterState = saveAdapterState(adapter);
			if (adapterState != null) {
				final SavedState savedState = new SavedState(super.onSaveInstanceState());
				savedState.adapterState = adapterState;
				return savedState;
			}
		}
		return super.onSaveInstanceState();
	}

	/**
	 * Saves the current state of the specified <var>adapter</var>.
	 *
	 * @param adapter The adapter of which state to save. Should be instance of {@link StatefulAdapter}
	 *                or one of wrapper adapter ({@link android.widget.WrapperListAdapter}) implementations.
	 * @return Adapter's saved state or {@code null} or empty state if the adapter does not save its
	 * state or it is not a stateful adapter.
	 */
	private Parcelable saveAdapterState(Adapter adapter) {
		if (adapter instanceof StatefulAdapter) {
			return ((StatefulAdapter) adapter).saveInstanceState();
		} else if (adapter instanceof WrapperListAdapter) {
			return saveAdapterState(((WrapperListAdapter) adapter).getWrappedAdapter());
		}
		return null;
	}

	/**
	 */
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		final Adapter adapter = getAdapter();
		if (adapter != null && savedState.adapterState != null) {
			this.restoreAdapterState(adapter, savedState.adapterState);
		}
	}

	/**
	 * Restores the saved state of the specified <var>adapter</var>.
	 *
	 * @param adapter      The adapter of which state to restore. Should be instance of {@link StatefulAdapter}
	 *                     or one of wrapper adapter ({@link android.widget.WrapperListAdapter}) implementations.
	 * @param adapterState The previously saved adapter state via {@link #saveAdapterState(Adapter)}.
	 */
	private void restoreAdapterState(Adapter adapter, Parcelable adapterState) {
		if (adapter instanceof StatefulAdapter) {
			((StatefulAdapter) adapter).restoreInstanceState(adapterState);
		} else if (adapter instanceof WrapperListAdapter) {
			restoreAdapterState(((WrapperListAdapter) adapter).getWrappedAdapter(), adapterState);
		}
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget group.
	 */
	private final class Decorator extends RefreshableDecorator<GridViewWidget> {

		/**
		 * See {@link RefreshableDecorator#RefreshableDecorator(ViewGroup, int[])}.
		 */
		Decorator(GridViewWidget widgetGroup) {
			super(widgetGroup, R.styleable.Ui_GridView);
		}

		/**
		 */
		@Override
		void onProcessTypedValues(Context context, TypedArray typedArray) {
			super.onProcessTypedValues(context, typedArray);
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_GridView_uiPullEnabled) {
					setPullEnabled(typedArray.getBoolean(index, false));
				} else if (index == R.styleable.Ui_GridView_uiRefreshEnabled) {
					setRefreshEnabled(typedArray.getBoolean(index, false));
				} else if (index == R.styleable.Ui_GridView_uiRefreshGestureEnabled) {
					setRefreshGestureEnabled(typedArray.getBoolean(index, false));
				}
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintValues(Context context, TypedArray tintArray, int tintColor) {
			if (UiConfig.MATERIALIZED) {
				if (tintArray.hasValue(R.styleable.Ui_GridView_uiBackgroundTint)) {
					setBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_GridView_uiBackgroundTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_GridView_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_GridView_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintArray.hasValue(R.styleable.Ui_GridView_uiBackgroundTint)) {
					mTintInfo.backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_GridView_uiBackgroundTint);
				}
				mTintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_GridView_uiBackgroundTintMode, 0),
						mTintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
				);
			}
		}

		/**
		 */
		@Override
		void superSetSelected(boolean selected) {
			GridViewWidget.super.setSelected(selected);
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			GridViewWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			GridViewWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return GridViewWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			GridViewWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return GridViewWidget.super.getBackgroundTintMode();
		}
	}
}
