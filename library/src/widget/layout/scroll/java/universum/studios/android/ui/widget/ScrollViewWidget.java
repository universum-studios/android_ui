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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ScrollView;

import universum.studios.android.ui.controller.PullController;
import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended version of {@link android.widget.ScrollView}. This updated ScrollView supports tinting
 * for the Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other
 * useful features described below.
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
 * <h3>Pulling</h3>
 * This widget group can be pulled at its start and also at its end. This feature is supported via
 * {@link PullController PullController}. ScrollViewWidget has
 * {@link Orientation#VERTICAL} orientation, so its content can be pulled at the top or at the bottom
 * by offsetting its current position via {@link #offsetTopAndBottom(int)} what the PullController
 * pretty much does.
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
 * See {@link ScrollView},
 * {@link R.styleable#Ui_ScrollView ScrollViewWidget Attributes},
 * {@link R.styleable#Ui_PullController PullController Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#scrollViewStyle android:scrollViewStyle}
 *
 * @author Martin Albedinsky
 * @see HorizontalScrollViewWidget
 */
public class ScrollViewWidget extends ScrollView implements WidgetGroup, Pullable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ScrollViewWidget";

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
	 * List with registered OnScrollChangeListener listeners.
	 */
	private List<ViewWidget.OnScrollChangeListener> mScrollChangeListeners;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #ScrollViewWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public ScrollViewWidget(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #ScrollViewWidget(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link android.R.attr#scrollViewStyle} as attribute for default style.
	 */
	public ScrollViewWidget(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.scrollViewStyle);
	}

	/**
	 * Same as {@link #ScrollViewWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public ScrollViewWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of ScrollViewWidget within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ScrollViewWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(ScrollViewWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(ScrollViewWidget.class.getName());
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
		this.ensureDecorator();
		mDecorator.onSizeChanged(w, h, oldw, oldh);
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
	public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
		this.ensureDecorator();
		return mDecorator.onInterceptTouchEvent(event) || super.onInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		this.ensureDecorator();
		mDecorator.hideSoftKeyboardOnTouch();
		return mDecorator.onTouchEvent(event) || super.onTouchEvent(event);
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
	public int getOrientation() {
		return Orientation.HORIZONTAL;
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
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
		this.ensureDecorator();
		mDecorator.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
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
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends PullableDecorator<ScrollViewWidget> {

		/**
		 * See {@link PullableDecorator#PullableDecorator(ViewGroup, int[])}.
		 */
		Decorator(ScrollViewWidget widgetGroup) {
			super(widgetGroup, R.styleable.Ui_ScrollView);
		}

		/**
		 */
		@Override
		void onProcessTypedValues(Context context, TypedArray typedArray) {
			super.onProcessTypedValues(context, typedArray);

			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_ScrollView_uiHideSoftKeyboardOnTouch) {
					setHideSoftKeyboardOnTouchEnabled(typedArray.getBoolean(index, false));
				} else if (index == R.styleable.Ui_ScrollView_uiPullEnabled) {
					setPullEnabled(typedArray.getBoolean(index, false));
				}
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintValues(Context context, TypedArray tintArray, int tintColor) {
			if (UiConfig.MATERIALIZED) {
				if (tintArray.hasValue(R.styleable.Ui_ScrollView_uiBackgroundTint)) {
					setBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_ScrollView_uiBackgroundTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_ScrollView_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_ScrollView_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintArray.hasValue(R.styleable.Ui_ScrollView_uiBackgroundTint)) {
					mTintInfo.backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_ScrollView_uiBackgroundTint);
				}
				mTintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintArray.getInt(R.styleable.Ui_ScrollView_uiBackgroundTintMode, 0),
						mTintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
				);
			}
		}

		/**
		 */
		@Override
		void superSetSelected(boolean selected) {
			ScrollViewWidget.super.setSelected(selected);
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			ScrollViewWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			ScrollViewWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return ScrollViewWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			ScrollViewWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return ScrollViewWidget.super.getBackgroundTintMode();
		}
	}
}
