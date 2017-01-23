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
import android.widget.RelativeLayout;

import universum.studios.android.ui.R;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * Extended version of {@link android.widget.RelativeLayout}. This updated RelativeLayout supports
 * tinting for the Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
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
 * See {@link RelativeLayout},
 * {@link R.styleable#Ui_ViewGroup ViewGroup Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@code 0}
 *
 * @author Martin Albedinsky
 * @see FrameLayoutWidget
 * @see LinearLayoutWidget
 */
public class RelativeLayoutWidget extends RelativeLayout implements WidgetGroup {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "RelativeLayoutWidget";

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
	WidgetGroupDecorator mDecorator;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #RelativeLayoutWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public RelativeLayoutWidget(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #RelativeLayoutWidget(android.content.Context, android.util.AttributeSet, int)}
	 * with {@code 0} as attribute for default style.
	 */
	public RelativeLayoutWidget(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Same as {@link #RelativeLayoutWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public RelativeLayoutWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of RelativeLayoutWidget within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public RelativeLayoutWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		event.setClassName(RelativeLayoutWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(RelativeLayoutWidget.class.getName());
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
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (!super.onTouchEvent(event)) {
			this.ensureDecorator();
			mDecorator.hideSoftKeyboardOnTouch();
			return false;
		}
		return true;
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
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget group.
	 */
	private final class Decorator extends WidgetGroupDecorator<RelativeLayoutWidget> {

		/**
		 * See {@link WidgetGroupDecorator#WidgetGroupDecorator(ViewGroup)}.
		 */
		Decorator(RelativeLayoutWidget widgetGroup) {
			super(widgetGroup);
		}

		/**
		 */
		@Override
		void superSetSelected(boolean selected) {
			RelativeLayoutWidget.super.setSelected(selected);
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			RelativeLayoutWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			RelativeLayoutWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return RelativeLayoutWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			RelativeLayoutWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return RelativeLayoutWidget.super.getBackgroundTintMode();
		}
	}
}
