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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageButton;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * Extended version of {@link android.widget.ImageButton}. This updated ImageButton supports tinting
 * for the Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other
 * useful features described below.
 *
 * <h3>Tinting</h3>
 * Tinting is supported via Xml attributes listed below:
 * <ul>
 * <li>{@link R.attr#uiTint uiTint}</li>
 * <li>{@link R.attr#uiTintMode uiTintMode}</li>
 * <li>{@link R.attr#uiBackgroundTint uiBackgroundTint}</li>
 * <li>{@link R.attr#uiBackgroundTintMode uiBackgroundTintMode}</li>
 * </ul>
 * <p>
 * <b>Note, that on {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and above SDK versions
 * can be also used Xml attributes listed above where in such case will be used the native tinting.</b>
 * <p>
 * This widget also overrides all SDK methods used to tint its components like {@link #setImageTintList(android.content.res.ColorStateList)}
 * or {@link #setImageTintMode(android.graphics.PorterDuff.Mode)}, so these can be used regardless
 * the current version of SDK but invoking of these methods below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
 * can be done only directly upon instance of this widget otherwise {@link NoSuchMethodException}
 * will be thrown.
 *
 * <h3>XML attributes</h3>
 * See {@link ImageButton},
 * {@link R.styleable#Ui_ImageButton ImageButtonWidget Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#imageButtonStyle android:imageButtonStyle}
 *
 * @author Martin Albedinsky
 * @see ImageViewWidget
 */
public class ImageButtonWidget extends ImageButton implements Widget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ImageButtonWidget";

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
	 * Same as {@link #ImageButtonWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public ImageButtonWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #ImageButtonWidget(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link android.R.attr#imageButtonStyle} as attribute for default style.
	 */
	public ImageButtonWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, android.R.attr.imageButtonStyle);
	}

	/**
	 * Same as {@link #ImageButtonWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public ImageButtonWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of ImageButtonWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ImageButtonWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
		event.setClassName(ImageButtonWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(ImageButtonWidget.class.getName());
	}

	/**
	 * Like {@link #setImageResource(int)} but this method will use {@link ResourceUtils#getVectorDrawable(Resources, int, Resources.Theme)}
	 * utility method to obtain the desired vector drawable and set it as image {@link Drawable} via
	 * {@link #setImageDrawable(Drawable)} method.
	 *
	 * @param resId Resource id of the desired <b>vector</b> drawable. Can be {@code 0} to clear the
	 *              current image drawable.
	 */
	public void setImageVectorResource(@DrawableRes int resId) {
		this.ensureDecorator();
		setImageDrawable(resId != 0 ? mDecorator.inflateVectorDrawable(resId) : null);
	}

	/**
	 */
	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		this.ensureDecorator();
		mDecorator.applyImageTint();
	}

	/**
	 */
	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		this.ensureDecorator();
		mDecorator.applyImageTint();
	}

	/**
	 */
	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		this.ensureDecorator();
		mDecorator.applyImageTint();
	}

	/**
	 */
	@Override
	public void setImageTintList(ColorStateList tint) {
		this.ensureDecorator();
		mDecorator.setImageTintList(tint);
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getImageTintList() {
		this.ensureDecorator();
		return mDecorator.getImageTintList();
	}

	/**
	 */
	@Override
	public void setImageTintMode(PorterDuff.Mode tintMode) {
		this.ensureDecorator();
		mDecorator.setImageTintMode(tintMode);
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getImageTintMode() {
		this.ensureDecorator();
		return mDecorator.getImageTintMode();
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
	 * The original wrapped background drawable can be obtained via {@link TintDrawable#getDrawable()}.
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
	 * Called to create ColorStateList for the background of this button to support its tinting
	 * for the pre LOLLIPOP Android versions.
	 *
	 * @param tintColor Tint color parsed from the current theme.
	 * @return ColorStateList for tint process or {@code null} if one cannot be created from the
	 * specified parameters.
	 */
	@Nullable
	ColorStateList createBackgroundTintColors(int tintColor) {
		return null;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends ImageDecorator<ImageButtonWidget> {

		/**
		 * See {@link ImageDecorator#ImageDecorator(android.widget.ImageView, int[])}.
		 */
		Decorator(ImageButtonWidget widget) {
			super(widget, R.styleable.Ui_ImageButton);
		}

		/**
		 */
		@Override
		void onProcessAttributes(Context context, TypedArray attributes) {
			super.onProcessAttributes(context, attributes);
			if (attributes.hasValue(R.styleable.Ui_ImageButton_android_enabled)) {
				setEnabled(attributes.getBoolean(R.styleable.Ui_ImageButton_android_enabled, isEnabled()));
			}
			if (attributes.hasValue(R.styleable.Ui_ImageButton_uiVectorSrc)) {
				final Drawable drawable = inflateVectorDrawable(attributes.getResourceId(
						R.styleable.Ui_ImageButton_uiVectorSrc,
						0
				));
				if (drawable != null) setImageDrawable(drawable);
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			if (UiConfig.MATERIALIZED) {
				if (tintAttributes.hasValue(R.styleable.Ui_ImageButton_uiTint)) {
					setImageTintList(tintAttributes.getColorStateList(R.styleable.Ui_ImageButton_uiTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_ImageButton_uiBackgroundTint)) {
					setBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_ImageButton_uiBackgroundTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_ImageButton_uiTintMode)) {
					setImageTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_ImageButton_uiTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_ImageButton_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintAttributes.getInteger(R.styleable.Ui_ImageButton_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintAttributes.hasValue(R.styleable.Ui_ImageButton_uiTint)) {
					mTintInfo.tintList = tintAttributes.getColorStateList(R.styleable.Ui_ImageButton_uiTint);
				}
				mTintInfo.backgroundTintList = createBackgroundTintColors(tintColor);
				if (tintAttributes.hasValue(R.styleable.Ui_ImageButton_uiBackgroundTint)) {
					mTintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_ImageButton_uiBackgroundTint);
				}
				mTintInfo.tintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_ImageButton_uiTintMode, 0),
						mTintInfo.tintList != null ? PorterDuff.Mode.SRC_IN : null
				);
				mTintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintAttributes.getInteger(R.styleable.Ui_ImageButton_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
			}
		}

		/**
		 */
		@Override
		void superSetImageDrawable(Drawable drawable) {
			ImageButtonWidget.super.setImageDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetImageTintList(ColorStateList tint) {
			ImageButtonWidget.super.setImageTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetImageTintList() {
			return ImageButtonWidget.super.getImageTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetImageTintMode(PorterDuff.Mode tintMode) {
			ImageButtonWidget.super.setImageTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetImageTintMode() {
			return ImageButtonWidget.super.getImageTintMode();
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			ImageButtonWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			ImageButtonWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return ImageButtonWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			ImageButtonWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return ImageButtonWidget.super.getBackgroundTintMode();
		}
	}
}
