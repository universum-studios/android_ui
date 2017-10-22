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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
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
import universum.studios.android.ui.controller.RefreshController;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

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
public class GridViewWidget extends GridView implements WidgetGroup, Refreshable {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "GridViewWidget";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Decorator used to extend API of this widget group by functionality otherwise not supported or
	 * not available due to current API level.
	 */
	private Decorator mDecorator;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #GridViewWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public GridViewWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #GridViewWidget(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link android.R.attr#gridViewStyle} as attribute for default style.
	 */
	public GridViewWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, android.R.attr.gridViewStyle);
	}

	/**
	 * Same as {@link #GridViewWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public GridViewWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of GridViewWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@SuppressWarnings("unused")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public GridViewWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context, attrs, defStyleAttr, defStyleRes);
	}

	/*
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
	public final RefreshController getRefreshController() {
		this.ensureDecorator();
		return mDecorator.getRefreshController();
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
	public int getOrientation() {
		return Orientation.VERTICAL;
	}

	/**
	 */
	@Override
	public void setHideSoftKeyboardOnTouchEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.setHideSoftKeyboardOnTouchEnabled(enabled);
	}

	/**
	 */
	@Override
	public boolean isHideSoftKeyboardOnTouchEnabled() {
		this.ensureDecorator();
		return mDecorator.isHideSoftKeyboardOnTouchEnabled();
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
	public boolean isAttachedToWindow() {
		this.ensureDecorator();
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && super.isAttachedToWindow()) ||
				mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW);
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.ensureDecorator();
		mDecorator.onSizeChanged(w, h, oldw, oldh);
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
	public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
		this.ensureDecorator();
		if (mDecorator.onInterceptTouchEvent(event)) {
			final MotionEvent cancelEvent = WidgetUtils.createMotionCancelingEvent(event);
			super.onInterceptTouchEvent(cancelEvent);
			cancelEvent.recycle();
			return true;
		}
		return super.onInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		this.ensureDecorator();
		if (mDecorator.onTouchEvent(event)) {
			mDecorator.hideSoftKeyboardOnTouch();
			return true;
		}
		if (super.onTouchEvent(event)) {
			return true;
		}
		mDecorator.hideSoftKeyboardOnTouch();
		return false;
	}

	/**
	 */
	@Override
	public boolean verifyDrawable(@NonNull Drawable drawable) {
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
				final CollectionSavedState savedState = new CollectionSavedState(super.onSaveInstanceState());
				savedState.mAdapterState = adapterState;
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
		if (!(state instanceof CollectionSavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		final CollectionSavedState savedState = (CollectionSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		final Adapter adapter = getAdapter();
		if (adapter != null && savedState.mAdapterState != null) {
			this.restoreAdapterState(adapter, savedState.mAdapterState);
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

	/*
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
		void onProcessAttributes(Context context, TypedArray attributes) {
			super.onProcessAttributes(context, attributes);
			final int n = attributes.getIndexCount();
			for (int i = 0; i < n; i++) {
				int index = attributes.getIndex(i);
				if (index == R.styleable.Ui_GridView_uiRefreshEnabled) {
					setRefreshEnabled(attributes.getBoolean(index, false));
				} else if (index == R.styleable.Ui_GridView_uiRefreshGestureEnabled) {
					setRefreshGestureEnabled(attributes.getBoolean(index, false));
				}
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			if (UiConfig.MATERIALIZED) {
				if (tintAttributes.hasValue(R.styleable.Ui_GridView_uiBackgroundTint)) {
					setBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_GridView_uiBackgroundTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_GridView_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_GridView_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintAttributes.hasValue(R.styleable.Ui_GridView_uiBackgroundTint)) {
					mTintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_GridView_uiBackgroundTint);
				}
				mTintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_GridView_uiBackgroundTintMode, 0),
						mTintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
				);
			}
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
