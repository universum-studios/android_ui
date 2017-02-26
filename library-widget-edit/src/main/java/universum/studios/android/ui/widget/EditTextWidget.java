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
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import universum.studios.android.font.Font;
import universum.studios.android.font.FontWidget;
import universum.studios.android.ui.R;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * Extended version of {@link android.widget.EditText}. This updated EditText allows setting of custom
 * font (from assets) and tinting for the Android versions below
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other useful features described below.
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
 * This widget also overrides all SDK methods used to tint its components like {@link #setBackgroundTintList(android.content.res.ColorStateList)}
 * or {@link #setBackgroundTintMode(android.graphics.PorterDuff.Mode)}, so these can be used regardless
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
 *      &lt;style name="Widget.EditText" parent="android:Widget.Material.EditText"&gt;
 *          &lt;item name="uiFontPath"&gt;roboto/roboto_light.ttf&lt;/item&gt;
 *      &lt;/style&gt;
 *      - This font will be than applied only to widgets (EditTexts) from this library
 *        that has this style as theirs style set through theme global attribute
 *        (like &lt;item name="android:editTextStyle"&gt;@style/Widget.EditText&lt;/item&gt;)
 *        or set within Xml layout (like style="@style/Widget.EditText").
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
 *          &lt;universum.studios.android.ui.widget.EditTextWidget
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
 * See {@link EditText},
 * {@link R.styleable#Ui_EditText EditTextWidget Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#editTextStyle android:editTextStyle}
 *
 * @author Martin Albedinsky
 * @see TextViewWidget
 * @see AutoCompleteTextViewWidget
 */
public class EditTextWidget extends EditText implements Widget, FontWidget, ErrorWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "EditTextWidget";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Decorator used to extend API of this widget by functionality otherwise not supported or not
	 * available due to current API level.
	 */
	private Decorator mDecorator;

	/**
	 * Length of the current text input presented in this edit text.
	 */
	private int mInputLength;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #EditTextWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public EditTextWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #EditTextWidget(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link android.R.attr#editTextStyle} as attribute for default style.
	 */
	public EditTextWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}

	/**
	 * Same as {@link #EditTextWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public EditTextWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of EditTextWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public EditTextWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
		event.setClassName(EditTextWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(EditTextWidget.class.getName());
	}

	/**
	 * Same as calling {@link #setError(CharSequence)}. The drawable <var>icon</var> is ignored.
	 */
	@Override
	public void setError(@NonNull CharSequence error, @Nullable Drawable icon) {
		setError(error);
	}

	/**
	 */
	@Override
	public void setError(@NonNull CharSequence error) {
		this.ensureDecorator();
		mDecorator.setError(error);
	}

	/**
	 */
	@Override
	public boolean hasError() {
		this.ensureDecorator();
		return mDecorator.hasError();
	}

	/**
	 */
	@Override
	public CharSequence getError() {
		this.ensureDecorator();
		return mDecorator.getError();
	}

	/**
	 */
	@Override
	public void clearError() {
		this.ensureDecorator();
		mDecorator.clearError();
	}

	/**
	 */
	@Override
	public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
		super.setCompoundDrawables(left, top, right, bottom);
		this.ensureDecorator();
		mDecorator.applyCompoundDrawablesTint();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return array containing instances of {@link TintDrawable TintDrawable} (if any)
	 * if compound drawable tint has been applied via {@link #setCompoundDrawableTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawables can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@NonNull
	@Override
	public Drawable[] getCompoundDrawables() {
		return super.getCompoundDrawables();
	}

	/**
	 * Like {@link #setCompoundDrawablesRelativeWithIntrinsicBounds(int, int, int, int)} but this
	 * method will use {@link ResourceUtils#getVectorDrawable(Resources, int, Resources.Theme)} utility
	 * method to obtain the desired vector drawables and set them as compound {@link Drawable Drawables}
	 * via {@link #setCompoundDrawablesWithIntrinsicBounds(Drawable, Drawable, Drawable, Drawable)}
	 * method.
	 * <p>
	 * <b>Note</b>, that this method will fall back to
	 * {@link #setCompoundDrawablesWithIntrinsicBounds(Drawable, Drawable, Drawable, Drawable)} method
	 * on an Android API level that does not support relative compound drawables.
	 *
	 * @param startResId  Resource id of the desired compound <b>vector</b> drawable to be shown at
	 *                    the start of the text. Can be {@code 0} to clear the current one.
	 * @param topResId    Resource id of the desired compound <b>vector</b> drawable to be shown at
	 *                    the top of the text. Can be {@code 0} to clear the current one.
	 * @param endResId    Resource id of the desired compound <b>vector</b> drawable to be shown at
	 *                    the end of the text. Can be {@code 0} to clear the current one.
	 * @param bottomResId Resource id of the desired compound <b>vector</b> drawable to be shown at
	 *                    the bottom of the text. Can be {@code 0} to clear the current one.
	 */
	@SuppressLint("NewApi")
	public void setCompoundVectorDrawablesRelativeWithIntrinsicBounds(@DrawableRes int startResId, @DrawableRes int topResId, @DrawableRes int endResId, @DrawableRes int bottomResId) {
		this.ensureDecorator();
		if (TextViewDecorator.RELATIVE_COMPOUND_DRAWABLES_SUPPORTED) {
			setCompoundDrawablesRelativeWithIntrinsicBounds(
					startResId != 0 ? mDecorator.inflateVectorDrawable(startResId) : null,
					topResId != 0 ? mDecorator.inflateVectorDrawable(topResId) : null,
					endResId != 0 ? mDecorator.inflateVectorDrawable(endResId) : null,
					bottomResId != 0 ? mDecorator.inflateVectorDrawable(bottomResId) : null
			);
		} else {
			setCompoundDrawablesWithIntrinsicBounds(
					startResId != 0 ? mDecorator.inflateVectorDrawable(startResId) : null,
					topResId != 0 ? mDecorator.inflateVectorDrawable(topResId) : null,
					endResId != 0 ? mDecorator.inflateVectorDrawable(endResId) : null,
					bottomResId != 0 ? mDecorator.inflateVectorDrawable(bottomResId) : null
			);
		}
	}

	/**
	 */
	@Override
	public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
		super.setCompoundDrawablesRelative(start, top, end, bottom);
		this.ensureDecorator();
		mDecorator.applyCompoundDrawablesTint();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return array containing instances of {@link TintDrawable TintDrawable} (if any)
	 * if compound drawable tint has been applied via {@link #setCompoundDrawableTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawables can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@NonNull
	@Override
	public Drawable[] getCompoundDrawablesRelative() {
		return super.getCompoundDrawablesRelative();
	}

	/**
	 */
	@Override
	public void setCompoundDrawableTintList(ColorStateList tint) {
		this.ensureDecorator();
		mDecorator.setCompoundDrawableTintList(tint);
	}

	/**
	 */
	@Override
	public ColorStateList getCompoundDrawableTintList() {
		this.ensureDecorator();
		return mDecorator.getCompoundDrawableTintList();
	}

	/**
	 */
	@Override
	public void setCompoundDrawableTintMode(PorterDuff.Mode tintMode) {
		this.ensureDecorator();
		mDecorator.setCompoundDrawableTintMode(tintMode);
	}

	/**
	 */
	@Override
	public PorterDuff.Mode getCompoundDrawableTintMode() {
		this.ensureDecorator();
		return mDecorator.getCompoundDrawableTintMode();
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

	/**
	 */
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		this.mInputLength = lengthAfter;
		if (lengthBefore == 0 || lengthAfter == 0) {
			refreshDrawableState();
			invalidate();
		}
	}

	/**
	 */
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
		if (mInputLength > 0) {
			mergeDrawableStates(drawableState, WidgetStateSet.INPUT);
		}
		if (hasError()) {
			mergeDrawableStates(drawableState, WidgetStateSet.ERROR);
		}
		return drawableState;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends EditTextDecorator<EditTextWidget> {

		/**
		 * See {@link EditTextDecorator#EditTextDecorator(EditText)}.
		 */
		Decorator(EditTextWidget widget) {
			super(widget);
		}

		/**
		 */
		@Override
		void superSetCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
			EditTextWidget.super.setCompoundDrawables(left, top, right, bottom);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		void superSetCompoundDrawablesRelative(Drawable left, Drawable top, Drawable right, Drawable bottom) {
			EditTextWidget.super.setCompoundDrawablesRelative(left, top, right, bottom);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		void superSetCompoundDrawableTintList(ColorStateList tint) {
			EditTextWidget.super.setCompoundDrawableTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		ColorStateList superGetCompoundDrawableTintList() {
			return EditTextWidget.super.getCompoundDrawableTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		void superSetCompoundDrawableTintMode(PorterDuff.Mode tintMode) {
			EditTextWidget.super.setCompoundDrawableTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		PorterDuff.Mode superGetCompoundDrawableTintMode() {
			return EditTextWidget.super.getCompoundDrawableTintMode();
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			EditTextWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			EditTextWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return EditTextWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			EditTextWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return EditTextWidget.super.getBackgroundTintMode();
		}
	}
}
