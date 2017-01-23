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
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * Extended version of {@link android.widget.TextView}. This updated TextView allows setting of
 * custom font (from assets) and tinting for the Android versions below
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
 *      &lt;style name="Widget.TextView" parent="android:Widget.Material.TextView"&gt;
 *          &lt;item name="uiFontPath"&gt;roboto/roboto_light.ttf&lt;/item&gt;
 *      &lt;/style&gt;
 *      - This font will be than applied only to widgets (TextViews) from this library
 *        that has this style as theirs style set through theme global attribute
 *        (like &lt;item name="android:textViewStyle"&gt;@style/Widget.TextView&lt;/item&gt;)
 *        or set within Xml layout (like style="@style/Widget.TextView").
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
 *          &lt;universum.studios.android.ui.widget.TextViewWidget
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
 * <h3>Sliding</h3>
 * This updated view allows updating of its current position along <b>x</b> and <b>y</b> axis by
 * changing <b>fraction</b> of these properties depending on its current size using the new animation
 * framework introduced in {@link android.os.Build.VERSION_CODES#HONEYCOMB HONEYCOMB} via
 * {@link android.animation.ObjectAnimator ObjectAnimator}s API.
 * <p>
 * Changing of fraction of X or Y is supported via these two methods:
 * <ul>
 * <li>{@link #setFractionX(float)}</li>
 * <li>{@link #setFractionY(float)}</li>
 * </ul>
 * <p>
 * For example if an instance of this view class needs to be slided to the right by its whole width,
 * an Xml file with ObjectAnimator would look like this:
 * <pre>
 *  &lt;objectAnimator xmlns:android="http://schemas.android.com/apk/res/android"
 *                  android:propertyName="fractionX"
 *                  android:valueFrom="0.0"
 *                  android:valueTo="1.0"
 *                  android:duration="300"/&gt;
 * </pre>
 *
 * <h3>XML attributes</h3>
 * See {@link TextView},
 * {@link R.styleable#Ui_TextView TextViewWidget Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#textViewStyle android:textViewStyle}
 *
 * @author Martin Albedinsky
 * @see EditTextWidget
 * @see AutoCompleteTextViewWidget
 * @see EmptyView
 */
public class TextViewWidget extends TextView implements Widget, FontWidget, ErrorWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "TextViewWidget";

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
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #TextViewWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public TextViewWidget(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #TextViewWidget(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link android.R.attr#textViewStyle} as attribute for default style.
	 */
	public TextViewWidget(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	/**
	 * Same as {@link #TextViewWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public TextViewWidget(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of TextViewWidget within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TextViewWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		event.setClassName(TextViewWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(TextViewWidget.class.getName());
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
		mDecorator.applyTextAppearanceFont(resId);
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
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
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
	private final class Decorator extends TextViewDecorator<TextViewWidget> {

		/**
		 * See {@link TextViewDecorator#TextViewDecorator(TextView, int[])}.
		 */
		Decorator(TextViewWidget widget) {
			super(widget, R.styleable.Ui_TextView);
		}

		/**
		 */
		@Override
		void onProcessTypedValues(Context context, TypedArray typedArray) {
			super.onProcessTypedValues(context, typedArray);
			final Drawable[] compoundDrawables = RELATIVE_COMPOUND_DRAWABLES_SUPPORTED ?
					getCompoundDrawablesRelative() :
					getCompoundDrawables();
			boolean compoundDrawablesChanged = false;
			if (typedArray.hasValue(R.styleable.Ui_TextView_uiVectorDrawableStart)) {
				compoundDrawables[0] = inflateVectorDrawable(typedArray.getResourceId(
						R.styleable.Ui_TextView_uiVectorDrawableStart,
						0
				));
				compoundDrawablesChanged = true;
			}
			if (typedArray.hasValue(R.styleable.Ui_TextView_uiVectorDrawableTop)) {
				compoundDrawables[1] = inflateVectorDrawable(typedArray.getResourceId(
						R.styleable.Ui_TextView_uiVectorDrawableTop,
						0
				));
				compoundDrawablesChanged = true;
			}
			if (typedArray.hasValue(R.styleable.Ui_TextView_uiVectorDrawableEnd)) {
				compoundDrawables[2] = inflateVectorDrawable(typedArray.getResourceId(
						R.styleable.Ui_TextView_uiVectorDrawableEnd,
						0
				));
				compoundDrawablesChanged = true;
			}
			if (typedArray.hasValue(R.styleable.Ui_TextView_uiVectorDrawableBottom)) {
				compoundDrawables[3] = inflateVectorDrawable(typedArray.getResourceId(
						R.styleable.Ui_TextView_uiVectorDrawableBottom,
						0
				));
				compoundDrawablesChanged = true;
			}
			if (compoundDrawablesChanged) {
				if (RELATIVE_COMPOUND_DRAWABLES_SUPPORTED) {
					setCompoundDrawablesRelativeWithIntrinsicBounds(
							compoundDrawables[0],
							compoundDrawables[1],
							compoundDrawables[2],
							compoundDrawables[3]
					);
				} else {
					setCompoundDrawablesWithIntrinsicBounds(
							compoundDrawables[0],
							compoundDrawables[1],
							compoundDrawables[2],
							compoundDrawables[3]
					);
				}
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintValues(Context context, TypedArray tintArray, int tintColor) {
			final CompoundTintInfo tintInfo = getTintInfo();
			// Process compound drawable tint values.
			if (UiConfig.MATERIALIZED_MARSHMALLOW) {
				if (tintArray.hasValue(R.styleable.Ui_TextView_uiDrawableTint)) {
					setCompoundDrawableTintList(tintArray.getColorStateList(R.styleable.Ui_TextView_uiDrawableTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_TextView_uiDrawableTintMode)) {
					setCompoundDrawableTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_TextView_uiDrawableTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintArray.hasValue(R.styleable.Ui_TextView_uiDrawableTint)) {
					tintInfo.compoundTintList = tintArray.getColorStateList(R.styleable.Ui_TextView_uiDrawableTint);
				}
				tintInfo.compoundTintMode = TintManager.parseTintMode(
						tintArray.getInteger(R.styleable.Ui_TextView_uiDrawableTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
			}
			// Process background tint values.
			if (UiConfig.MATERIALIZED) {
				if (tintArray.hasValue(R.styleable.Ui_TextView_uiBackgroundTint)) {
					setBackgroundTintList(tintArray.getColorStateList(R.styleable.Ui_TextView_uiBackgroundTint));
				}
				if (tintArray.hasValue(R.styleable.Ui_TextView_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintArray.getInt(R.styleable.Ui_TextView_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintArray.hasValue(R.styleable.Ui_TextView_uiBackgroundTint)) {
					tintInfo.backgroundTintList = tintArray.getColorStateList(R.styleable.Ui_TextView_uiBackgroundTint);
				}
				tintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintArray.getInteger(R.styleable.Ui_TextView_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
			}
		}

		/**
		 */
		@Override
		void superSetSelected(boolean selected) {
			TextViewWidget.super.setSelected(selected);
		}

		/**
		 */
		@Override
		void superSetCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
			TextViewWidget.super.setCompoundDrawables(left, top, right, bottom);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		void superSetCompoundDrawablesRelative(Drawable left, Drawable top, Drawable right, Drawable bottom) {
			TextViewWidget.super.setCompoundDrawablesRelative(left, top, right, bottom);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		void superSetCompoundDrawableTintList(ColorStateList tint) {
			TextViewWidget.super.setCompoundDrawableTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		ColorStateList superGetCompoundDrawableTintList() {
			return TextViewWidget.super.getCompoundDrawableTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		void superSetCompoundDrawableTintMode(PorterDuff.Mode tintMode) {
			TextViewWidget.super.setCompoundDrawableTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.M)
		PorterDuff.Mode superGetCompoundDrawableTintMode() {
			return TextViewWidget.super.getCompoundDrawableTintMode();
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			TextViewWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			TextViewWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return TextViewWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			TextViewWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return TextViewWidget.super.getBackgroundTintMode();
		}
	}
}
