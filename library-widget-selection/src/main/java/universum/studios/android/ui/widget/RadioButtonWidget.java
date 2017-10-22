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
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import universum.studios.android.font.Font;
import universum.studios.android.font.FontWidget;
import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * Extended version of {@link android.widget.RadioButton} compound button. This updated RadioButton
 * allows setting of custom font (from assets) and tinting for the Android versions below
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other useful features described below.
 *
 * <h3>Tinting</h3>
 * Tinting is supported via Xml attributes listed below:
 * <ul>
 * <li>{@link R.attr#uiButtonTint uiButtonTint}</li>
 * <li>{@link R.attr#uiButtonTintMode uiButtonTintMode}</li>
 * <li>{@link R.attr#uiBackgroundTint uiBackgroundTint}</li>
 * <li>{@link R.attr#uiBackgroundTintMode uiBackgroundTintMode}</li>
 * </ul>
 * <p>
 * <b>Note, that on {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and above SDK versions
 * can be also used Xml attributes listed above where in such case will be used the native tinting.</b>
 * <p>
 * This widget also overrides all SDK methods used to tint its components like {@link #setButtonTintList(android.content.res.ColorStateList)}
 * or {@link #setButtonTintMode(android.graphics.PorterDuff.Mode)}, so these can be used regardless
 * the current version of SDK but invoking of these methods below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
 * can be done only directly upon instance of this widget otherwise {@link NoSuchMethodException}
 * will be thrown.
 *
 * <h3>Custom font</h3>
 * Custom font (the path to it) can be specified basically at <b>4 places</b> from where it can be
 * by this font widget referenced. <b>Note</b>, that your custom font must be placed within Android
 * <b>assets</b> folder within sub-folder named <b>"font"</b>, this is requirement of this library,
 * within this sub-folder you can specify your custom hierarchy to organize your fonts.
 * <p>
 * <pre>
 *      1) Declare path to custom font in an Application theme:
 *      &lt;style name="Theme" parent="android:Theme.Material"&gt;
 *          &lt;item name="uiFontPath"&gt;roboto/roboto_light.ttf&lt;/item&gt;
 *      &lt;/style&gt;
 *      - This font will be than applied to all widgets from this library.
 *
 *      --------------------------------------------------------------------------------------------
 *
 *      2) Declare path to custom font in a global TextAppearance style:
 *      &lt;style name="TextAppearance.Widget" parent="android:TextAppearance.Material.Widget"&gt;
 *          &lt;item name="uiFontPath"&gt;roboto/roboto_light.ttf&lt;/item&gt;
 *      &lt;/style&gt;
 *      - This font will be than applied only to widgets from this library that has this text
 *        appearance as theirs style or just reference to this text appearance within theirs style.
 *
 *      --------------------------------------------------------------------------------------------
 *
 *      3) Declare path to custom font in a specific widget's style:
 *      &lt;style name="Widget.CompoundButton.RadioButton" parent="android:Widget.Material.CompoundButton.RadioButton"&gt;
 *          &lt;item name="uiFontPath"&gt;roboto/roboto_light.ttf&lt;/item&gt;
 *      &lt;/style&gt;
 *      - This font will be than applied only to widgets (RadioButtons) from this library that has
 *        this style as theirs style set through theme global attribute
 *        (like &lt;item name="android:radioButtonStyle"&gt;@style/Widget.CompoundButton.RadioButton&lt;/item&gt;)
 *        or set within Xml layout (like style="@style/Widget.CompoundButton.RadioButton").
 *
 *      --------------------------------------------------------------------------------------------
 *
 *      4) Declare path to custom font in an Xml layout:
 *      &lt;LinearLayout
 *          xmlns:android="http://schemas.android.com/apk/res/android"
 *          xmlns:ui="http://schemas.android.com/apk/res-auto"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          android:orientation="vertical"&gt;
 *
 *          // ... Some views
 *
 *          &lt;universum.studios.android.ui.widget.RadioButtonWidget
 *              android:layout_width="wrap_content"
 *              android:layout_height="wrap_content"
 *              android:text="Text view with custom font."
 *              ui:uiFontPath="roboto/roboto_light"&gt;
 *
 *          // ... Some other views
 *
 *      &lt;/LinearLayout&gt;
 *      - This font will be than applied only to that particular widget.
 * </pre>
 * <p>
 * <b>Note, that it is not necessary to specify also '.ttf' suffix for custom font paths, the library
 * will add one if it is missing.</b>
 *
 * <h3>XML attributes</h3>
 * See {@link RadioButton},
 * {@link R.styleable#Ui_CompoundButton CompoundButton Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#radioButtonStyle android:radioButtonStyle}
 *
 * @author Martin Albedinsky
 *
 * @see CheckBoxWidget
 * @see SwitchWidget
 * @see ToggleButtonWidget
 */
public class RadioButtonWidget extends RadioButton implements Widget, FontWidget {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "RadioButtonWidget";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Decorator used to extend API of this widget by functionality otherwise not supported or not
	 * available due to current API level.
	 */
	private Decorator mDecorator;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #RadioButtonWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public RadioButtonWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #RadioButtonWidget(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link android.R.attr#radioButtonStyle} as attribute for default style.
	 */
	public RadioButtonWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, android.R.attr.radioButtonStyle);
	}

	/**
	 * Same as {@link #RadioButtonWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public RadioButtonWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of RadioButtonWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@SuppressWarnings("unused")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public RadioButtonWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
		event.setClassName(RadioButtonWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(RadioButtonWidget.class.getName());
	}

	/**
	 */
	@Override
	public void setButtonDrawable(Drawable d) {
		this.ensureDecorator();
		mDecorator.setButtonDrawable(d);
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setButtonTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Nullable
	@Override
	public Drawable getButtonDrawable() {
		return super.getButtonDrawable();
	}

	/**
	 */
	@Override
	public void setButtonTintList(@Nullable ColorStateList tint) {
		this.ensureDecorator();
		mDecorator.setButtonTintList(tint);
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getButtonTintList() {
		this.ensureDecorator();
		return mDecorator.getButtonTintList();
	}

	/**
	 */
	@Override
	public void setButtonTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureDecorator();
		mDecorator.setButtonTintMode(tintMode);
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getButtonTintMode() {
		this.ensureDecorator();
		return mDecorator.getButtonTintMode();
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
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (!UiConfig.MATERIALIZED) {
			this.ensureDecorator();
			mDecorator.applyButtonTint();
		}
	}

	/**
	 */
	@Override
	public void setFont(@NonNull String fontPath) {
		this.ensureDecorator();
		mDecorator.setFont(fontPath);
	}

	/**
	 */
	@Override
	public void setFont(@Nullable Font font) {
		this.ensureDecorator();
		mDecorator.setFont(font);
	}

	/**
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void setTextAppearance(@NonNull Context context, @StyleRes int resId) {
		super.setTextAppearance(context, resId);
		this.ensureDecorator();
		mDecorator.setFontFromStyle(resId);
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

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends CompoundButtonDecorator<RadioButtonWidget> {

		/**
		 * See {@link CompoundButtonDecorator#CompoundButtonDecorator(CompoundButton)}.
		 */
		Decorator(RadioButtonWidget widget) {
			super(widget);
		}

		/**
		 */
		@Override
		void superSetButtonDrawable(Drawable button) {
			RadioButtonWidget.super.setButtonDrawable(button);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetButtonTintList(ColorStateList tint) {
			RadioButtonWidget.super.setButtonTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetButtonTintList() {
			return RadioButtonWidget.super.getButtonTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetButtonTintMode(PorterDuff.Mode tintMode) {
			RadioButtonWidget.super.setButtonTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetButtonTintMode() {
			return RadioButtonWidget.super.getButtonTintMode();
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			RadioButtonWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			RadioButtonWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return RadioButtonWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			RadioButtonWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return RadioButtonWidget.super.getBackgroundTintMode();
		}
	}
}
