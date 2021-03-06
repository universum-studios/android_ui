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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * Extended version of {@link android.widget.HorizontalScrollView}. This updated HorizontalScrollView
 * supports tinting for the Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
 * and other useful features described below.
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
 * <h3>Scroll change callbacks</h3>
 * To receive callbacks about changes in scroll position there can be registered {@link ViewWidget.OnScrollChangeListener}
 * via {@link #addOnScrollChangeListener(ViewWidget.OnScrollChangeListener)}. Already registered listener
 * can be removed via {@link #removeOnScrollChangeListener(ViewWidget.OnScrollChangeListener)}.
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
 * See {@link HorizontalScrollView},
 * {@link R.styleable#Ui_HorizontalScrollView HorizontalScrollViewWidget Attributes},
 * {@link R.styleable#Ui_PullController PullController Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#horizontalScrollViewStyle android:horizontalScrollViewStyle}
 *
 * @author Martin Albedinsky
 * @see ScrollViewWidget
 */
public class HorizontalScrollViewWidget extends HorizontalScrollView implements WidgetGroup, Scrollable {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "HorizontalScrollViewWidget";

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

	/**
	 * List with registered OnScrollChangeListener listeners.
	 */
	private List<ViewWidget.OnScrollChangeListener> mScrollChangeListeners;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #HorizontalScrollViewWidget(android.content.Context, android.util.AttributeSet)}
	 * without attributes.
	 */
	public HorizontalScrollViewWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #HorizontalScrollViewWidget(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link android.R.attr#horizontalScrollViewStyle} as attribute for default style.
	 */
	@SuppressLint("InlinedApi")
	public HorizontalScrollViewWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.attr.horizontalScrollViewStyle : 0);
	}

	/**
	 * Same as {@link #HorizontalScrollViewWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public HorizontalScrollViewWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of HorizontalScrollViewWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@SuppressWarnings("unused")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public HorizontalScrollViewWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(HorizontalScrollViewWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(HorizontalScrollViewWidget.class.getName());
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
	 * Adds a callback to be invoked whenever {@link #onScrollChanged(int, int, int, int)} is
	 * invoked for this view.
	 *
	 * @param listener The desired listener callback to add.
	 * @see #removeOnScrollChangeListener(ViewWidget.OnScrollChangeListener)
	 */
	public void addOnScrollChangeListener(@NonNull ViewWidget.OnScrollChangeListener listener) {
		if (mScrollChangeListeners == null) this.mScrollChangeListeners = new ArrayList<>(1);
		if (!mScrollChangeListeners.contains(listener)) mScrollChangeListeners.add(listener);
	}

	/**
	 * Removes the previously added scroll change callback.
	 *
	 * @param listener The desired listener callback to remove.
	 * @see #addOnScrollChangeListener(ViewWidget.OnScrollChangeListener)
	 */
	public void removeOnScrollChangeListener(@NonNull ViewWidget.OnScrollChangeListener listener) {
		if (mScrollChangeListeners != null) mScrollChangeListeners.remove(listener);
	}

	/**
	 */
	@Override
	public int getOrientation() {
		return Orientation.HORIZONTAL;
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
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mScrollChangeListeners != null && !mScrollChangeListeners.isEmpty()) {
			for (ViewWidget.OnScrollChangeListener listener : mScrollChangeListeners) {
				listener.onScrollChanged(this, l, t, oldl, oldt);
			}
		}
	}

	/**
	 */
	@Override
	public boolean isScrolledAtStart() {
		this.ensureDecorator();
		return mDecorator.scrollableWrapper.isScrolledAtStart();
	}

	/**
	 */
	@Override
	public boolean isScrolledAtEnd() {
		this.ensureDecorator();
		return mDecorator.scrollableWrapper.isScrolledAtEnd();
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends WidgetGroupDecorator<HorizontalScrollViewWidget> {

		/**
		 * Helper used to check if the attached scrollable view is scrolled at start or at end.
		 */
		final ScrollableWrapper scrollableWrapper;

		/**
		 * See {@link WidgetGroupDecorator#WidgetGroupDecorator(ViewGroup, int[])}.
		 */
		Decorator(HorizontalScrollViewWidget widgetGroup) {
			super(widgetGroup, R.styleable.Ui_HorizontalScrollView);
			this.scrollableWrapper = ScrollableWrapper.wrapScrollableView(widgetGroup);
		}

		@Override
		void onProcessAttributes(Context context, TypedArray attributes) {
			super.onProcessAttributes(context, attributes);
			for (int i = 0; i < attributes.getIndexCount(); i++) {
				final int index = attributes.getIndex(i);
				if (index == R.styleable.Ui_HorizontalScrollView_uiHideSoftKeyboardOnTouch) {
					setHideSoftKeyboardOnTouchEnabled(attributes.getBoolean(index, false));
				}
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			super.onProcessTintAttributes(context, tintAttributes, tintColor);
			if (UiConfig.MATERIALIZED) {
				if (tintAttributes.hasValue(R.styleable.Ui_HorizontalScrollView_uiBackgroundTint)) {
					setBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_HorizontalScrollView_uiBackgroundTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_HorizontalScrollView_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_HorizontalScrollView_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintAttributes.hasValue(R.styleable.Ui_HorizontalScrollView_uiBackgroundTint)) {
					mTintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_HorizontalScrollView_uiBackgroundTint);
				}
				mTintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_HorizontalScrollView_uiBackgroundTintMode, 0),
						mTintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
				);
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			HorizontalScrollViewWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			HorizontalScrollViewWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return HorizontalScrollViewWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			HorizontalScrollViewWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return HorizontalScrollViewWidget.super.getBackgroundTintMode();
		}
	}
}
