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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Spinner;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * Extended version of {@link android.widget.Spinner}. This updated Spinner supports tinting for the
 * Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other useful
 * features described below.
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
 * <h3>XML attributes</h3>
 * See {@link Spinner},
 * {@link R.styleable#Ui_Spinner SpinnerWidget Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#spinnerStyle android:spinnerStyle}
 *
 * @author Martin Albedinsky
 */
public class SpinnerWidget extends Spinner implements Widget, ErrorWidget {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "SpinnerWidget";

	/**
	 * Flag indicating whether the popup window of this spinner can overlap this spinner or not.
	 */
	private static final int PFLAG_POPUP_WINDOW_OVERLAPS = 0x00008000;

	/*
	 * Static members ==============================================================================
	 */

	/**
	 * Flag indicating whether the SDK version is pre JELLY_BEAN or not.
	 */
	private static final boolean PRE_JELLY_BEAN = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN;

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
	 * Same as {@link #SpinnerWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public SpinnerWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #SpinnerWidget(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link android.R.attr#spinnerStyle} as attribute for default style.
	 */
	public SpinnerWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, android.R.attr.spinnerStyle);
	}

	/**
	 * Same as {@link #SpinnerWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public SpinnerWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of SpinnerWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SpinnerWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
		event.setClassName(SpinnerWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(SpinnerWidget.class.getName());
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
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (hasError()) {
			mergeDrawableStates(drawableState, WidgetStateSet.ERROR);
		}
		return drawableState;
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * This class holds all data necessary to tint all components of this view.
	 */
	private static final class SpinnerTintInfo extends BackgroundTintInfo {

		/**
		 * Color used to tint popup background.
		 */
		int popupBackgroundTint;
	}

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends WidgetDecorator<SpinnerWidget> {

		/**
		 * See {@link WidgetDecorator#WidgetDecorator(View, int[])}.
		 */
		Decorator(SpinnerWidget widget) {
			super(widget, R.styleable.Ui_Spinner);
			updatePrivateFlags(PFLAG_POPUP_WINDOW_OVERLAPS, true);
		}

		/**
		 */
		@Override
		BackgroundTintInfo onCreateTintInfo() {
			return new SpinnerTintInfo();
		}

		/**
		 */
		@Override
		void onProcessAttributes(Context context, TypedArray attributes) {
			super.onProcessAttributes(context, attributes);
			if (attributes.hasValue(R.styleable.Ui_Spinner_uiOverlapAnchor)) {
				this.updatePrivateFlags(PFLAG_POPUP_WINDOW_OVERLAPS, attributes.getBoolean(R.styleable.Ui_Spinner_uiOverlapAnchor, true));
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			if (UiConfig.MATERIALIZED) {
				if (tintAttributes.hasValue(R.styleable.Ui_Spinner_uiBackgroundTint)) {
					setBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_Spinner_uiBackgroundTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_Spinner_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintAttributes.getInteger(R.styleable.Ui_Spinner_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				final SpinnerTintInfo tintInfo = (SpinnerTintInfo) mTintInfo;
				tintInfo.backgroundTintList = TintManager.createSpinnerTintColors(getContext(), tintColor);
				if (tintAttributes.hasValue(R.styleable.Ui_Spinner_uiBackgroundTint)) {
					tintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_Spinner_uiBackgroundTint);
				}
				if (tintAttributes.hasValue(R.styleable.Ui_Spinner_uiColorPopupBackground)) {
					tintInfo.popupBackgroundTint = tintAttributes.getColor(R.styleable.Ui_Spinner_uiColorPopupBackground, 0);
				}
				tintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintAttributes.getInteger(R.styleable.Ui_Spinner_uiBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
			}
		}

		/**
		 */
		@Override
		void processTintAttributes(Context context, TypedArray tintAttributes) {
			super.processTintAttributes(context, tintAttributes);
			applyPopupTint();
		}

		/**
		 * Applies current popup background tint color from {@link #mTintInfo} to the popup background
		 * drawable.
		 * <p>
		 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this
		 * method does nothing.
		 */
		private void applyPopupTint() {
			if (UiConfig.MATERIALIZED || PRE_JELLY_BEAN) {
				return;
			}
			final Drawable popupBg = getPopupBackground();
			if (popupBg == null) {
				return;
			}
			final SpinnerTintInfo tintInfo = (SpinnerTintInfo) mTintInfo;
			if (tintInfo.popupBackgroundTint != Color.TRANSPARENT) {
				TintManager.tintDrawable(popupBg, tintInfo.popupBackgroundTint, PorterDuff.Mode.MULTIPLY);
			} else {
				popupBg.clearColorFilter();
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			SpinnerWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			SpinnerWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return SpinnerWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			SpinnerWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return SpinnerWidget.super.getBackgroundTintMode();
		}

		/**
		 */
		@Override
		protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
			super.onSizeChanged(width, height, oldWidth, oldHeight);
			if (!PRE_JELLY_BEAN && !UiConfig.MATERIALIZED) {
				setDropDownVerticalOffset(hasPrivateFlag(PFLAG_POPUP_WINDOW_OVERLAPS) ? -height : 0);
			}
		}
	}
}
