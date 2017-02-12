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
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;

import universum.studios.android.ui.R;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * Extended version of {@link android.support.v7.widget.SwitchCompat} compound button. This updated
 * Switch allows setting of custom font (from assets) and tinting for the Android versions below
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other useful features described below.
 *
 * <h3>Tinting</h3>
 * Tinting is supported via Xml attributes listed below:
 * <ul>
 * <li>{@link R.attr#uiThumbTint uiThumbTint}</li>
 * <li>{@link R.attr#uiThumbTintMode uiThumbTintMode}</li>
 * <li>{@link R.attr#uiTrackTint uiTrackTint}</li>
 * <li>{@link R.attr#uiTrackTintMode uiTrackTintMode}</li>
 * <li>{@link R.attr#uiBackgroundTint uiBackgroundTint}</li>
 * <li>{@link R.attr#uiBackgroundTintMode uiBackgroundTintMode}</li>
 * </ul>
 * <p>
 * <b>Note, that on {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and above SDK versions
 * can be also used Xml attributes listed above where in such case will be used the native tinting.</b>
 * <p>
 * This widget also overrides all SDK methods used to tint its components like {@link #setThumbTintList(android.content.res.ColorStateList)}
 * or {@link #setThumbTintMode(android.graphics.PorterDuff.Mode)}, so these can be used regardless
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
 *      &lt;style name="Widget.CompoundButton.Switch" parent="android:Widget.Material.CompoundButton.Switch"&gt;
 *          &lt;item name="uiFontPath"&gt;roboto/roboto_light.ttf&lt;/item&gt;
 *      &lt;/style&gt;
 *      - This font will be than applied only to widgets (Switches) from this library that has this
 *        style as theirs style set through theme global attribute
 *        (like &lt;item name="switchStyle"&gt;@style/Widget.CompoundButton.Switch&lt;/item&gt;)
 *        or set within Xml layout (like style="@style/Widget.CompoundButton.Switch").
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
 *          &lt;universum.studios.android.ui.widget.SwitchWidget
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
 * See {@link SwitchCompat},
 * {@link R.styleable#Ui_CompoundButton_Switch Switch Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#switchStyle switchStyle}
 *
 * @author Martin Albedinsky
 *
 * @see RadioButtonWidget
 * @see CheckBoxWidget
 * @see ToggleButtonWidget
 */
public class SwitchWidget extends SwitchCompat implements Widget, FontWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "SwitchWidget";

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
	 * Drawable representing the thumb of this switch view.
	 */
	private Drawable mThumb;

	/**
	 * Drawable representing the track of this switch view.
	 */
	private Drawable mTrack;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #SwitchWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public SwitchWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #SwitchWidget(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#switchMinWidth} as attribute for default style.
	 */
	@SuppressLint("InlinedApi")
	public SwitchWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.switchStyle);
	}

	/**
	 * Same as {@link #SwitchWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public SwitchWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of SwitchWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@SuppressWarnings("unused")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SwitchWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs, defStyleAttr);
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
		this.mThumb = getThumbDrawable();
		this.mTrack = getTrackDrawable();
		this.applyTrackTint();
		this.applyThumbTint();
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
		event.setClassName(SwitchWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(SwitchWidget.class.getName());
	}

	/**
	 */
	@Override
	public void setThumbDrawable(Drawable thumb) {
		super.setThumbDrawable(mThumb = thumb);
		this.applyThumbTint();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setThumbTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Override
	public Drawable getThumbDrawable() {
		return super.getThumbDrawable();
	}

	/**
	 * Applies current tint from {@link Decorator#mTintInfo} to the current thumb drawable.
	 */
	@SuppressWarnings("unused")
	private void applyThumbTint() {
		this.ensureDecorator();
		if (mThumb == null || !mDecorator.hasTintInfo()) {
			return;
		}
		final SwitchTintInfo tintInfo = mDecorator.getTintInfo();
		if (!tintInfo.hasTintList && !tintInfo.hasTintMode) {
			return;
		}
		final boolean isTintDrawable = mThumb instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) mThumb : new TintDrawable(mThumb);
		if (tintInfo.hasTintList) {
			tintDrawable.setTintList(tintInfo.tintList);
		}
		if (tintInfo.hasTintMode) {
			tintDrawable.setTintMode(tintInfo.tintMode);
		}
		if (tintDrawable.isStateful()) {
			tintDrawable.setState(getDrawableState());
		}
		if (isTintDrawable) {
			return;
		}
		super.setThumbDrawable(mThumb = tintDrawable);
		tintDrawable.attachCallback();
	}

	/**
	 * Applies a tint to the thumb drawable, if specified. This call does not modify the current
	 * tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setThumbDrawable(Drawable)} will automatically mutate the drawable
	 * and apply the specified tint and tint mode using {@link android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see #getThumbTintList()
	 * @see android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)
	 */
	public void setThumbTintList(@Nullable ColorStateList tint) {
		this.ensureDecorator();
		final SwitchTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.tintList = tint;
		tintInfo.hasTintList = true;
		this.applyThumbTint();
	}

	/**
	 * Returns the tint applied to the thumb drawable, if specified.
	 *
	 * @return The thumb drawable tint.
	 * @see #setTrackTintList(ColorStateList)
	 */
	@Nullable
	public ColorStateList getThumbTintList() {
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().tintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setThumbTintList(ColorStateList)}}
	 * to the thumb drawable. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see #getThumbTintMode()
	 * @see android.graphics.drawable.Drawable#setTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setThumbTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureDecorator();
		final SwitchTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.tintMode = tintMode;
		tintInfo.hasTintMode = true;
		this.applyThumbTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the thumb drawable, if specified.
	 *
	 * @return The thumb drawable blending mode used to apply the tint.
	 * @see #setThumbTintMode(PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getThumbTintMode() {
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().tintMode : null;
	}

	/**
	 */
	@Override
	public void setTrackDrawable(Drawable track) {
		super.setTrackDrawable(mTrack = track);
		this.applyTrackTint();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setTrackTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Override
	public Drawable getTrackDrawable() {
		return super.getTrackDrawable();
	}

	/**
	 * Applies current tint from {@link Decorator#mTintInfo} to the current track drawable.
	 */
	private void applyTrackTint() {
		this.ensureDecorator();
		if (mTrack == null || !mDecorator.hasTintInfo()) {
			return;
		}
		final SwitchTintInfo tintInfo = mDecorator.getTintInfo();
		if (!tintInfo.hasTrackTintList && !tintInfo.hasTrackTintMode) {
			return;
		}
		final boolean isTintDrawable = mTrack instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) mTrack : new TintDrawable(mTrack);
		if (tintInfo.hasTrackTintList) {
			tintDrawable.setTintList(tintInfo.trackTintList);
		}
		if (tintInfo.hasTrackTintMode) {
			tintDrawable.setTintMode(tintInfo.trackTintMode);
		}
		if (tintDrawable.isStateful()) {
			tintDrawable.setState(getDrawableState());
		}
		if (isTintDrawable) {
			return;
		}
		super.setTrackDrawable(mTrack = tintDrawable);
		tintDrawable.attachCallback();
	}

	/**
	 * Applies a tint to the track drawable, if specified. This call does not modify the current
	 * tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setTrackDrawable(Drawable)} will automatically mutate the drawable
	 * and apply the specified tint and tint mode using {@link android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see #getTrackTintList()
	 * @see android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)
	 */
	public void setTrackTintList(@Nullable ColorStateList tint) {
		this.ensureDecorator();
		final SwitchTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.trackTintList = tint;
		tintInfo.hasTrackTintList = true;
		this.applyTrackTint();
	}

	/**
	 * Returns the tint applied to the track drawable, if specified.
	 *
	 * @return The track drawable tint.
	 * @see #setTrackTintList(ColorStateList)
	 */
	@Nullable
	public ColorStateList getTrackTintList() {
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().trackTintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setTrackTintList(ColorStateList)}}
	 * to the track drawable. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see #getTrackTintMode()
	 * @see android.graphics.drawable.Drawable#setTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setTrackTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureDecorator();
		final SwitchTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.trackTintMode = tintMode;
		tintInfo.hasTrackTintList = true;
		this.applyTrackTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the track drawable, if specified.
	 *
	 * @return The track drawable blending mode used to apply the tint.
	 * @see #setTrackTintMode(PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getTrackTintMode() {
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().trackTintMode : null;
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
	 * via {@link #setButtonTintList(ColorStateList)}.</b>
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
	 * Inner classes ===============================================================================
	 */

	/**
	 * This class holds all data necessary to tint all components of this view.
	 */
	private static final class SwitchTintInfo extends BackgroundTintInfo {

		/**
		 * Color state list used to tint a specific states of the <b>track</b> drawable.
		 */
		ColorStateList trackTintList;

		/**
		 * Flag indicating whether the {@link #trackTintList} has been set or not.
		 */
		boolean hasTrackTintList;

		/**
		 * Blending mode used to apply tint to the <b>track</b> drawable.
		 */
		PorterDuff.Mode trackTintMode;

		/**
		 * Flag indicating whether the {@link #trackTintMode} has been set or not.
		 */
		boolean hasTrackTintMode;
	}

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends CompoundButtonDecorator<SwitchWidget> {

		/**
		 * See {@link CompoundButtonDecorator#CompoundButtonDecorator(CompoundButton)}.
		 */
		Decorator(SwitchWidget widget) {
			super(widget, R.styleable.Ui_CompoundButton_Switch);
		}

		/**
		 */
		@Override
		BackgroundTintInfo onCreateTintInfo() {
			return new SwitchTintInfo();
		}

		/**
		 */
		@NonNull
		@Override
		SwitchTintInfo getTintInfo() {
			return (SwitchTintInfo) super.getTintInfo();
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			super.onProcessTintAttributes(context, tintAttributes, tintColor);
			final SwitchTintInfo tintInfo = getTintInfo();
			if (tintAttributes.hasValue(R.styleable.Ui_CompoundButton_Switch_uiThumbTint)) {
				tintInfo.tintList = tintAttributes.getColorStateList(R.styleable.Ui_CompoundButton_Switch_uiThumbTint);
			}
			if (tintAttributes.hasValue(R.styleable.Ui_CompoundButton_Switch_uiTrackTint)) {
				tintInfo.trackTintList = tintAttributes.getColorStateList(R.styleable.Ui_CompoundButton_Switch_uiTrackTint);
			}
			if (tintAttributes.hasValue(R.styleable.Ui_CompoundButton_Switch_uiBackgroundTint)) {
				tintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_CompoundButton_Switch_uiBackgroundTint);
			}
			tintInfo.tintMode = TintManager.parseTintMode(
					tintAttributes.getInteger(R.styleable.Ui_CompoundButton_Switch_uiThumbTintMode, 0),
					tintInfo.tintList != null ? PorterDuff.Mode.SRC_IN : null
			);
			tintInfo.trackTintMode = TintManager.parseTintMode(
					tintAttributes.getInteger(R.styleable.Ui_CompoundButton_Switch_uiTrackTintMode, 0),
					tintInfo.trackTintList != null ? PorterDuff.Mode.SRC_IN : null
			);
			tintInfo.backgroundTintMode = TintManager.parseTintMode(
					tintAttributes.getInt(R.styleable.Ui_CompoundButton_uiBackgroundTintMode, 0),
					tintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
			);
		}

		/**
		 */
		@Override
		void onTintAttributesProcessed() {
			final SwitchTintInfo tintInfo = getTintInfo();
			// If there is no tint mode specified within style/xml do not tint at all.
			if (tintInfo.trackTintMode == null) tintInfo.trackTintList = null;
			tintInfo.hasTrackTintList = tintInfo.trackTintList != null;
			tintInfo.hasTrackTintMode = tintInfo.trackTintMode != null;
			super.onTintAttributesProcessed();
		}

		/**
		 */
		@Override
		boolean shouldInvalidateTintInfo(@NonNull BackgroundTintInfo tintInfo) {
			final SwitchTintInfo info = (SwitchTintInfo) tintInfo;
			return !info.hasTrackTintList && !info.hasTrackTintMode && super.shouldInvalidateTintInfo(tintInfo);
		}

		/**
		 */
		@Override
		void superSetButtonDrawable(Drawable button) {
			SwitchWidget.super.setButtonDrawable(button);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetButtonTintList(ColorStateList tint) {
			SwitchWidget.super.setButtonTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetButtonTintList() {
			return SwitchWidget.super.getButtonTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetButtonTintMode(PorterDuff.Mode tintMode) {
			SwitchWidget.super.setButtonTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetButtonTintMode() {
			return SwitchWidget.super.getButtonTintMode();
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			SwitchWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			SwitchWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return SwitchWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			SwitchWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return SwitchWidget.super.getBackgroundTintMode();
		}
	}
}
