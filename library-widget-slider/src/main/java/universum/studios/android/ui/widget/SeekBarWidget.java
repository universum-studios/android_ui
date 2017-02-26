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

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.SeekBar;

import universum.studios.android.font.Font;
import universum.studios.android.font.FontWidget;
import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.graphics.drawable.TintLayerDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * Extended version of {@link android.widget.SeekBar}. This updated SeekBar supports tinting for the
 * Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and other useful
 * features described below including <b>discrete</b> mode that should be enabled whenever a particular
 * SeekBarWidget represents setting for which a user needs to know the exact value of the setting.
 *
 * <h3>Tinting</h3>
 * <b>Note, that in the current version of SeekBarWidget, tinting for thumb and track below LOLLIPOP
 * is somehow not working properly on all supported Android versions. This issue is scheduled to be
 * resolved in the feature.</b>
 * <p>
 * Tinting is supported via Xml attributes listed below:
 * <ul>
 * <li>{@link R.attr#uiThumbTint uiThumbTint}</li>
 * <li>{@link R.attr#uiThumbTintMode uiThumbTintMode}</li>
 * <li>{@link R.attr#uiProgressTint uiProgressTint}</li>
 * <li>{@link R.attr#uiProgressTintMode uiProgressTintMode}</li>
 * <li>{@link R.attr#uiDiscreteIndicatorTint uiDiscreteIndicatorTint}</li>
 * <li>{@link R.attr#uiDiscreteIndicatorTintMode uiDiscreteIndicatorTintMode}</li>
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
 * <h3>Discrete mode</h3>
 * SeekBarWidget with the <b>discrete mode</b> enabled via {@link #setDiscrete(boolean)} will draw
 * above its progress track and thumb discrete indicator to show to a user current progress value.
 * There will be also drawn tick marks for discrete interval of which range can be specified via
 * {@link #setDiscreteIntervalRatio(float)}. These tick marks should serve as a clue for user so
 * he/she will know more precisely where to drag the thumb to pick its desired progress value.
 * <p>
 * <b>Simple view hierarchy model:</b>
 * <pre>
 *        __
 *       |28|
 *        \/
 * .---.--O---.---.---.---.---.
 *
 * </pre>
 *
 *
 * <h3>XML attributes</h3>
 * See {@link SeekBar},
 * {@link R.styleable#Ui_SeekBar SeekBarWidget Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link android.R.attr#seekBarStyle android:seekBarStyle}
 *
 * @author Martin Albedinsky
 */
public class SeekBarWidget extends SeekBar implements Widget, FontWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "SeekBarWidget";

	/**
	 * Duration for how long will be discrete components previewed (visible) after this SeekBar has
	 * been attached to window.
	 */
	private static final long PREVIEW_DISCRETE_COMPONENTS_DURATION = 2000;

	/**
	 * Maximum level value that can be applied to this seek bar as progress value.
	 */
	private static int MAX_LEVEL = 10000;

	/**
	 * Boolean flag indicating whether we can draw discrete interval at thumb position or not.
	 */
	private static final boolean CAN_DRAW_DISCRETE_INTERVAL_OVER_THUMB = !UiConfig.MATERIALIZED;

	/**
	 * Flag indicating whether this seek bar is discrete or not.
	 */
	private static final int PFLAG_DISCRETE = 0x00000001 << 16;

	/**
	 * Flag indicating whether this seek bar should indicate to a user that it is discrete or not.
	 */
	private static final int PFLAG_DISCRETE_PREVIEW_ENABLED = 0x00000001 << 17;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Graphics info for discrete indicator's text.
	 */
	private final DiscreteIndicatorTextInfo DISCRETE_INDICATOR_TEXT_INFO = new DiscreteIndicatorTextInfo();

	/**
	 * Graphics info for discrete interval tick marks.
	 */
	private final DiscreteIntervalTickMarkInfo DISCRETE_INTERVAL_TICK_MARK_INFO = new DiscreteIntervalTickMarkInfo();

	/**
	 * Decorator used to extend API of this widget by functionality otherwise not supported or not
	 * available due to current API level.
	 */
	private Decorator mDecorator;

	/**
	 * Drawable used to draw thumb of this seek bar.
	 */
	private Drawable mThumb;

	/**
	 * Drawable used to draw discrete indicator of this seek bar.
	 */
	private Drawable mDiscreteIndicator;

	/**
	 * Resource id of the {@link #mDiscreteIndicator} if indicator has been specified via
	 * {@link #setDiscreteIndicator(int)}.
	 */
	private int mDiscreteIndicatorRes;

	/**
	 * Drawable used to draw progress of this seek bar.
	 */
	private Drawable mProgressDrawable;

	/**
	 * Ratio used to compute count of tick marks of the discrete interval.
	 */
	private float mDiscreteIntervalRatio = 0.2f;

	/**
	 * Dimension size of the discrete indicator's drawable.
	 */
	private int mDiscreteIndicatorWidth, mDiscreteIndicatorHeight;

	/**
	 * Current progress set to this seek bar by {@link #setProgress(int)}.
	 */
	private int mProgress;

	/**
	 * Animations interface used to hide implementation details of animations performed upon this view.
	 */
	private Animations mAnimations;

	/**
	 * Helper rect used when we need to obtain bounds or padding of some component.
	 */
	private Rect mRect;

	/**
	 * Ripple background drawable that has been detached in discrete mode if not {@code null}. Should
	 * be re-attached when discrete mode is disabled.
	 */
	private Drawable mRippleBackgroundDrawable;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #SeekBarWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public SeekBarWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #SeekBarWidget(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link android.R.attr#seekBarStyle} as attribute for default style.
	 */
	public SeekBarWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, android.R.attr.seekBarStyle);
	}

	/**
	 * Same as {@link #SeekBarWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public SeekBarWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of SeekBarWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SeekBarWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
	@SuppressWarnings("ConstantConditions")
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.ensureRect();
		this.ensureDecorator();
		mDecorator.processAttributes(context, attrs, defStyleAttr, defStyleRes);
		this.mAnimations = Animations.get(this);

		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_SeekBar, defStyleAttr, defStyleRes);
		final Rect indicatorTextPadding = new Rect();
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_SeekBar_android_enabled) {
				setEnabled(attributes.getBoolean(index, true));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscrete) {
				setDiscrete(attributes.getBoolean(index, false));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscretePreviewEnabled) {
				setDiscretePreviewEnabled(attributes.getBoolean(index, true));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIntervalRatio) {
				setDiscreteIntervalRatio(attributes.getFloat(index, mDiscreteIntervalRatio));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIndicator) {
				setDiscreteIndicator(attributes.getResourceId(index, 0));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIndicatorTextAppearance) {
				setDiscreteIndicatorTextAppearance(attributes.getResourceId(index, 0));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIndicatorTextGravity) {
				setDiscreteIndicatorTextGravity(attributes.getInteger(index, DISCRETE_INDICATOR_TEXT_INFO.gravity));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIndicatorTextPaddingStart) {
				indicatorTextPadding.left = attributes.getDimensionPixelSize(index, 0);
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIndicatorTextPaddingTop) {
				indicatorTextPadding.top = attributes.getDimensionPixelSize(index, 0);
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIndicatorTextPaddingEnd) {
				indicatorTextPadding.right = attributes.getDimensionPixelSize(index, 0);
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIndicatorTextPaddingBottom) {
				indicatorTextPadding.bottom = attributes.getDimensionPixelSize(index, 0);
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIntervalTickMarkColor) {
				setDiscreteIntervalTickMarkColor(attributes.getColorStateList(index));
			} else if (index == R.styleable.Ui_SeekBar_uiDiscreteIntervalTickMarkRadius) {
				setDiscreteIntervalTickMarkRadius(attributes.getDimensionPixelSize(index, 0));
			}
		}
		setDiscreteIndicatorTextPadding(
				indicatorTextPadding.left,
				indicatorTextPadding.top,
				indicatorTextPadding.right,
				indicatorTextPadding.bottom
		);
		attributes.recycle();
		this.applyProgressTints();
		this.applyThumbTint();
	}

	/**
	 * Ensures that the {@link #mRect} is initialized.
	 */
	private void ensureRect() {
		if (mRect == null) this.mRect = new Rect();
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
		event.setClassName(SeekBarWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(SeekBarWidget.class.getName());
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
	@SuppressLint("NewApi")
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		final boolean processed = super.onTouchEvent(event);
		final int progress = getProgress();
		if (processed) {
			if (progress != mProgress) {
				this.handleProgressChange(progress);
			}
			this.ensureDecorator();
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE_PREVIEW_ENABLED)) {
						this.revealDiscreteComponents();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					final Drawable background = getBackground();
					if (background != null && mAnimations.shouldDraw() && UiConfig.MATERIALIZED) {
						// Cancel the revealed circle around the thumb.
						background.setHotspotBounds(0, 0, 0, 0);
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE_PREVIEW_ENABLED)) {
						this.concealDiscreteComponents();
					}
					break;
			}
		}
		return processed;
	}

	/**
	 */
	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (mDiscreteIndicator != null) mDiscreteIndicator.setVisible(visibility == VISIBLE, false);
	}

	/**
	 */
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		if (mProgress != progress) this.handleProgressChange(progress);
	}

	/**
	 * Handles change in the current progress. If discrete indicator is enabled for this seek bar,
	 * its position will be updated according to the specified progress value.
	 *
	 * @param progress The current progress of this SeekBarWidget.
	 */
	private void handleProgressChange(int progress) {
		this.mProgress = progress;
		this.ensureDecorator();
		if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE) && mDiscreteIndicatorHeight > 0) {
			this.updateDiscreteIndicatorPosition(getWidth(), getHeight());
			if (!UiConfig.MATERIALIZED) {
				this.updateThumbPosition();
			}
			// todo: update current tint to gray color if progress is 0 otherwise to the specified tint
		}
		invalidate();
	}

	/**
	 * Sets a flag indicating whether this seek bar is <b>discrete</b> or not.
	 * <p>
	 * SeekBarWidget in the discrete mode draws, above the progress track and the thumb, a discrete
	 * indicator to show to a user current progress value. Discrete indicator's drawable can be set
	 * via {@link #setDiscreteIndicator(android.graphics.drawable.Drawable)}.
	 *
	 * @param discrete {@code True} to enable discrete mode, {@code false} otherwise.
	 * @see R.attr#uiDiscrete ui:uiDiscrete
	 * @see #isDiscrete()
	 */
	public void setDiscrete(boolean discrete) {
		if (discrete && UiConfig.MATERIALIZED) {
			final Drawable background = getBackground();
			if (background instanceof RippleDrawable) {
				// This is a little bit harsh, but the RippleDrawable background is showing a ripple
				// in discrete mode in top left corner of this view's bounds which is kind of weird
				// behaviour.
				this.mRippleBackgroundDrawable = background;
				setBackgroundDrawable(null);
			}
		} else if (!discrete && mRippleBackgroundDrawable != null) {
			setBackgroundDrawable(mRippleBackgroundDrawable);
			this.mRippleBackgroundDrawable = null;
		}
		this.ensureDecorator();
		if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE) != discrete) {
			mDecorator.updatePrivateFlags(PFLAG_DISCRETE, discrete);
			this.updateThumb(mThumb);
			this.updateDiscreteIndicator(mDiscreteIndicator);
			requestLayout();
		}
	}

	/**
	 * Returns the flag indicating whether this seek bar is <b>discrete</b> or not.
	 *
	 * @return {@code True} if the discrete mode is enabled, {@code false} otherwise.
	 * @see #setDiscrete(boolean)
	 */
	public boolean isDiscrete() {
		this.ensureDecorator();
		return mDecorator.hasPrivateFlag(PFLAG_DISCRETE);
	}

	/**
	 * Sets a flag indicating whether a preview of the discrete indicator is enabled or not. This
	 * feature is by default disabled.
	 * <p>
	 * When the preview is enabled, the discrete indicator will be showed whenever a user presses this
	 * seek bar widget and will be dismissed after a while if the user does not moves it.
	 * <p>
	 * If this feature is enabled, the indicator will be also previewed whenever is this seek bar
	 * attached to the window.
	 *
	 * @param enabled {@code True} to enabled discrete preview, {@code false} otherwise.
	 * @see R.attr#uiDiscretePreviewEnabled ui:uiDiscretePreviewEnabled
	 * @see #isDiscretePreviewEnabled()
	 */
	public void setDiscretePreviewEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.updatePrivateFlags(PFLAG_DISCRETE_PREVIEW_ENABLED, enabled);
	}

	/**
	 * Returns the flag indicating whether a preview of the discrete indicator is enabled or not.
	 *
	 * @return {@code True} if discrete preview is enabled, {@code false} otherwise.
	 * @see #setDiscretePreviewEnabled(boolean)
	 */
	public boolean isDiscretePreviewEnabled() {
		this.ensureDecorator();
		return mDecorator.hasPrivateFlag(PFLAG_DISCRETE_PREVIEW_ENABLED);
	}

	/**
	 */
	@Override
	public void setThumb(Drawable thumb) {
		this.updateThumb(thumb);
	}

	/**
	 * Updates current thumb to the specified one. If this seek bar has discrete mode enabled
	 * ({@link #isDiscrete()}), the given thumb will be updated to scaleable drawable if it is not yet.
	 *
	 * @param thumb The new thumb to update to.
	 */
	private void updateThumb(Drawable thumb) {
		this.ensureDecorator();
		if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE)) {
			thumb = mAnimations.makeThumbScaleable(thumb, Gravity.CENTER);
		}
		if (mThumb != thumb) {
			super.setThumb(mThumb = thumb);
			this.applyThumbTint();
		}
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setThumbTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Override
	public Drawable getThumb() {
		if (mThumb instanceof ScaleDrawable) return ((ScaleDrawable) mThumb).getDrawable();
		return mThumb;
	}

	/**
	 * Applies current thumb tint from {@link Decorator#mTintInfo} to the current thumb drawable.
	 * <p>
	 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this
	 * method does nothing.
	 */
	@SuppressWarnings("ConstantConditions")
	private void applyThumbTint() {
		this.ensureDecorator();
		if (UiConfig.MATERIALIZED || mThumb == null || !mDecorator.hasTintInfo()) {
			return;
		}
		final Drawable thumb = mThumb instanceof ScaleDrawable ? ((ScaleDrawable) mThumb).getDrawable() : mThumb;
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		if ((!tintInfo.hasTintList && !tintInfo.hasTintMode)) {
			return;
		}
		final boolean isTintDrawable = thumb instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) thumb : new TintDrawable(thumb);
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
		final int thumbOffset = getThumbOffset();
		this.mThumb = mDecorator.hasPrivateFlag(PFLAG_DISCRETE) ?
				mAnimations.makeThumbScaleable(tintDrawable, Gravity.CENTER) :
				tintDrawable;

		super.setThumb(mThumb);
		tintDrawable.attachCallback();
		setThumbOffset(thumbOffset);
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setThumbTintList(@Nullable ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setThumbTintList(tint);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.tintList = tint;
		tintInfo.hasTintList = true;
		this.applyThumbTint();
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public ColorStateList getThumbTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getThumbTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().tintList : null;
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setThumbTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setThumbTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.tintMode = tintMode;
		tintInfo.hasTintMode = true;
		this.applyThumbTint();
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public PorterDuff.Mode getThumbTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getThumbTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().tintMode : null;
	}

	/**
	 */
	@Override
	public void setProgressDrawable(Drawable drawable) {
		super.setProgressDrawable(mProgressDrawable = drawable);
		this.applyProgressTints();
	}

	/**
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintLayerDrawable TintLayerDrawable} if tint has
	 * been applied to one of progress layers via {@link #setProgressTintList(ColorStateList)} or
	 * {@link #setSecondaryProgressTintList(ColorStateList)} or {@link #setProgressBackgroundTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 */
	@Override
	public Drawable getProgressDrawable() {
		return super.getProgressDrawable();
	}

	/**
	 * Applies current progress tints from {@link Decorator#mTintInfo} to the progress drawable (its
	 * layers respectively if it is instance of LayerDrawable or to the whole drawable if it is just
	 * simple drawable).
	 * <p>
	 * <b>Note</b>, that for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} this method
	 * does nothing.
	 */
	private void applyProgressTints() {
		if (UiConfig.MATERIALIZED) {
			return;
		}
		this.ensureDecorator();
		if (mProgressDrawable == null || !mDecorator.hasTintInfo()) {
			return;
		}
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		if (!tintInfo.hasPrimaryProgressTintList && !tintInfo.hasPrimaryProgressTintMode &&
				!tintInfo.hasSecondaryProgressTintList && !tintInfo.hasSecondaryProgressTintMode &&
				!tintInfo.hasProgressBackgroundTintList && !tintInfo.hasProgressBackgroundTintMode) {
			return;
		}
		if (mProgressDrawable instanceof TintLayerDrawable) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasProgressBackgroundTintList) {
				tintDrawable.setTintList(tintInfo.progressBackgroundTintList, android.R.id.background);
			}
			if (tintInfo.hasProgressBackgroundTintMode) {
				tintDrawable.setTintMode(tintInfo.progressBackgroundTintMode, android.R.id.background);
			}
			if (tintInfo.hasSecondaryProgressTintList) {
				tintDrawable.setTintList(tintInfo.secondaryProgressTintList, android.R.id.secondaryProgress);
			}
			if (tintInfo.hasSecondaryProgressTintMode) {
				tintDrawable.setTintMode(tintInfo.secondaryProgressTintMode, android.R.id.secondaryProgress);
			}
			if (tintInfo.hasPrimaryProgressTintList) {
				tintDrawable.setTintList(tintInfo.primaryProgressTintList, android.R.id.progress);
			}
			if (tintInfo.hasPrimaryProgressTintMode) {
				tintDrawable.setTintMode(tintInfo.primaryProgressTintMode, android.R.id.progress);
			}
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
			return;
		} else if (mProgressDrawable instanceof TintDrawable) {
			final TintDrawable tintDrawable = (TintDrawable) mProgressDrawable;
			if (tintInfo.hasPrimaryProgressTintList) {
				tintDrawable.setTintList(tintInfo.primaryProgressTintList);
			}
			if (tintInfo.hasPrimaryProgressTintMode) {
				tintDrawable.setTintMode(tintInfo.primaryProgressTintMode);
			}
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
			return;
		}
		if (mProgressDrawable instanceof LayerDrawable) {
			final TintLayerDrawable tintDrawable = new TintLayerDrawable((LayerDrawable) mProgressDrawable);
			this.mProgressDrawable = tintDrawable;
			this.applyProgressTint();
			this.applySecondaryProgressTint();
			this.applyProgressBackgroundTint();
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
			super.setProgressDrawable(mProgressDrawable);
			tintDrawable.attachCallback();
			tintDrawable.setLevel((int) (getProgress() / (float) getMax() * MAX_LEVEL));
		} else {
			final TintDrawable tintDrawable = new TintDrawable(mProgressDrawable);
			this.mProgressDrawable = tintDrawable;
			this.applySimpleProgressTint();
			super.setProgressDrawable(mProgressDrawable);
			tintDrawable.attachCallback();
			tintDrawable.setLevel((int) (getProgress() / (float) getMax() * MAX_LEVEL));
		}
	}

	/**
	 * Applies current first valid tint from {@link Decorator#mTintInfo} to the progress drawable as
	 * whole.
	 *
	 * @see #applyProgressTints()
	 */
	private void applySimpleProgressTint() {
		if (mProgressDrawable instanceof TintDrawable) {
			final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
			final TintDrawable tintDrawable = (TintDrawable) mProgressDrawable;
			boolean hasTintList, hasTintMode;
			hasTintList = hasTintMode = false;
			ColorStateList tintList = null;
			PorterDuff.Mode tintMode = null;
			if (tintInfo.hasPrimaryProgressTintList || tintInfo.hasPrimaryProgressTintMode) {
				hasTintList = tintInfo.hasPrimaryProgressTintList;
				tintList = tintInfo.primaryProgressTintList;
				hasTintMode = tintInfo.hasPrimaryProgressTintMode;
				tintMode = tintInfo.primaryProgressTintMode;
			} else if (tintInfo.hasSecondaryProgressTintList || tintInfo.hasSecondaryProgressTintMode) {
				hasTintList = tintInfo.hasSecondaryProgressTintList;
				tintList = tintInfo.secondaryProgressTintList;
				hasTintMode = tintInfo.hasSecondaryProgressTintMode;
				tintMode = tintInfo.secondaryProgressTintMode;
			} else if (tintInfo.hasProgressBackgroundTintList || tintInfo.hasProgressBackgroundTintMode) {
				hasTintList = tintInfo.hasProgressBackgroundTintList;
				tintList = tintInfo.progressBackgroundTintList;
				hasTintMode = tintInfo.hasProgressBackgroundTintMode;
				tintMode = tintInfo.progressBackgroundTintMode;
			}
			if (hasTintList) tintDrawable.setTintList(tintList);
			if (hasTintMode) tintDrawable.setTintMode(tintMode);
			if (mProgressDrawable.isStateful()) {
				mProgressDrawable.setState(getDrawableState());
			}
		}
	}

	/**
	 * Applies current primary progress tint from {@link Decorator#mTintInfo} to the progress layer
	 * of the progress drawable.
	 *
	 * @see #applyProgressTints()
	 */
	private void applyProgressTint() {
		if (!(mProgressDrawable instanceof TintLayerDrawable) || !mDecorator.hasTintInfo()) return;
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		if (tintInfo.hasPrimaryProgressTintList || tintInfo.hasPrimaryProgressTintMode) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasPrimaryProgressTintList) {
				tintDrawable.setTintList(tintInfo.primaryProgressTintList, android.R.id.progress);
			}
			if (tintInfo.hasPrimaryProgressTintMode) {
				tintDrawable.setTintMode(tintInfo.primaryProgressTintMode, android.R.id.progress);
			}
		}
	}

	/**
	 * Applies current secondary progress tint from {@link Decorator#mTintInfo} to the secondary
	 * progress layer of the progress drawable.
	 *
	 * @see #applyProgressTints()
	 */
	private void applySecondaryProgressTint() {
		if (!(mProgressDrawable instanceof TintLayerDrawable) || !mDecorator.hasTintInfo()) return;
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		if (tintInfo.hasSecondaryProgressTintList || tintInfo.hasSecondaryProgressTintMode) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasSecondaryProgressTintList) {
				tintDrawable.setTintList(tintInfo.secondaryProgressTintList, android.R.id.secondaryProgress);
			}
			if (tintInfo.hasSecondaryProgressTintMode) {
				tintDrawable.setTintMode(tintInfo.secondaryProgressTintMode, android.R.id.secondaryProgress);
			}
		}
	}

	/**
	 * Applies current progress background tint from {@link Decorator#mTintInfo} to the background
	 * layer of the progress drawable.
	 *
	 * @see #applyProgressTints()
	 */
	private void applyProgressBackgroundTint() {
		if (!(mProgressDrawable instanceof TintLayerDrawable) || !mDecorator.hasTintInfo()) return;
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		if (tintInfo.hasProgressBackgroundTintList || tintInfo.hasProgressBackgroundTintMode) {
			final TintLayerDrawable tintDrawable = (TintLayerDrawable) mProgressDrawable;
			if (tintInfo.hasProgressBackgroundTintList) {
				tintDrawable.setTintList(tintInfo.progressBackgroundTintList, android.R.id.background);
			}
			if (tintInfo.hasProgressBackgroundTintMode) {
				tintDrawable.setTintMode(tintInfo.progressBackgroundTintMode, android.R.id.background);
			}
		}
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setProgressTintList(@Nullable ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressTintList(tint);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.primaryProgressTintList = tint;
		tintInfo.hasPrimaryProgressTintList = true;
		this.applyProgressTints();
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public ColorStateList getProgressTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().primaryProgressTintList : null;
	}

	/**
	 */
	@Override
	@SuppressLint("NewApi")
	public void setProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.primaryProgressTintMode = tintMode;
		tintInfo.hasPrimaryProgressTintMode = true;
		this.applyProgressTints();
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressLint("NewApi")
	public PorterDuff.Mode getProgressTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().primaryProgressTintMode : null;
	}

	/**
	 */
	@Override
	public void setSecondaryProgressTintList(ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setSecondaryProgressTintList(tint);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.secondaryProgressTintList = tint;
		tintInfo.hasSecondaryProgressTintList = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applySecondaryProgressTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getSecondaryProgressTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getSecondaryProgressTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().secondaryProgressTintList : null;
	}

	/**
	 */
	@Override
	public void setSecondaryProgressTintMode(PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setSecondaryProgressTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.secondaryProgressTintMode = tintMode;
		tintInfo.hasSecondaryProgressTintMode = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applySecondaryProgressTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getSecondaryProgressTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getSecondaryProgressTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().secondaryProgressTintMode : null;
	}

	/**
	 */
	@Override
	public void setProgressBackgroundTintList(ColorStateList tint) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressBackgroundTintList(tint);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.progressBackgroundTintList = tint;
		tintInfo.hasProgressBackgroundTintList = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applyProgressBackgroundTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public ColorStateList getProgressBackgroundTintList() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressBackgroundTintList();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().progressBackgroundTintList : null;
	}

	/**
	 */
	@Override
	public void setProgressBackgroundTintMode(PorterDuff.Mode tintMode) {
		if (UiConfig.MATERIALIZED) {
			super.setProgressBackgroundTintMode(tintMode);
			return;
		}
		this.ensureDecorator();
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.progressBackgroundTintMode = tintMode;
		tintInfo.hasProgressBackgroundTintMode = true;
		if (mProgressDrawable instanceof TintLayerDrawable) {
			this.applyProgressBackgroundTint();
		} else {
			this.applyProgressTints();
		}
	}

	/**
	 */
	@Nullable
	@Override
	public PorterDuff.Mode getProgressBackgroundTintMode() {
		if (UiConfig.MATERIALIZED) {
			return super.getProgressBackgroundTintMode();
		}
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().progressBackgroundTintMode : null;
	}

	/**
	 * Sets a ratio in which should be drawn tick marks of discrete interval of this seek bar if
	 * the discrete mode is enabled by {@link #setDiscrete(boolean)}.
	 * <p>
	 * By default is this ratio set to {@code 0.2f} so there will be drawn 6 tick marks.
	 * <p>
	 * <b>Note</b>, that discrete interval has only informational character and do not force a user
	 * to pick exactly value represented by one of interval's tick marks.
	 *
	 * @param ratio The desired interval ratio from the range {@code [0.0, 1.0]}.
	 * @see R.attr#uiDiscreteIntervalRatio ui:uiDiscreteIntervalRatio
	 * @see #getDiscreteIntervalRatio()
	 */
	public void setDiscreteIntervalRatio(@FloatRange(from = 0, to = 1) float ratio) {
		if (mDiscreteIntervalRatio != ratio) {
			this.mDiscreteIntervalRatio = ratio;
			if (mProgressDrawable != null) {
				invalidate(mProgressDrawable.getBounds());
			}
		}
	}

	/**
	 * Returns the ratio in which are drawn tick marks of discrete interval whenever is the discrete
	 * mode enabled.
	 *
	 * @return Ratio for discrete interval from the range {@code [0.0, 1.0]}.
	 * @see #setDiscreteIntervalRatio(float)
	 * @see #isDiscrete()
	 */
	@FloatRange(from = 0, to = 1)
	public float getDiscreteIntervalRatio() {
		return mDiscreteIntervalRatio;
	}

	/**
	 * Sets a radius for the tick mark of a discrete interval. The discrete interval are drawn whenever
	 * the discrete mode for this seek bar is enabled via {@link #setDiscrete(boolean)}.
	 *
	 * @param radius The desired radius for tick mark.
	 * @see R.attr#uiDiscreteIntervalTickMarkRadius ui:uiDiscreteIntervalTickMarkRadius
	 * @see #getDiscreteIntervalTickMarkRadius()
	 */
	public void setDiscreteIntervalTickMarkRadius(@FloatRange(from = 0) float radius) {
		if (DISCRETE_INTERVAL_TICK_MARK_INFO.radius != radius) {
			DISCRETE_INTERVAL_TICK_MARK_INFO.radius = Math.max(0, radius);
			this.invalidateDiscreteIntervalsArea();
		}
	}

	/**
	 * Returns the radius of the tick mark of a discrete interval.
	 *
	 * @return Tick mark radius.
	 * @see #setDiscreteIntervalTickMarkRadius(float)
	 */
	@FloatRange(from = 0)
	public float getDiscreteIntervalTickMarkRadius() {
		return DISCRETE_INTERVAL_TICK_MARK_INFO.radius;
	}

	/**
	 * Sets a single color used to draw the discrete interval's tick mark.
	 *
	 * @param color The desired color.
	 * @see R.attr#uiDiscreteIntervalTickMarkColor ui:uiDiscreteIntervalTickMarkColor
	 */
	public void setDiscreteIntervalTickMarkColor(@ColorInt int color) {
		setDiscreteIntervalTickMarkColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets the colors state list used to draw different states of the discrete interval's tick mark.
	 *
	 * @param colors The desired list of colors for the tick mark.
	 * @see R.attr#uiDiscreteIntervalTickMarkColor ui:uiDiscreteIntervalTickMarkColor
	 * @see #getDiscreteIntervalTickMarkColors()
	 * @see #getDiscreteIntervalTickMarkCurrentColor()
	 */
	public void setDiscreteIntervalTickMarkColor(@NonNull ColorStateList colors) {
		DISCRETE_INTERVAL_TICK_MARK_INFO.colors = colors;
		this.updateDiscreteIntervalTickMarksState(getDrawableState(), true);
	}

	/**
	 * Returns the list of colors used to draw different states of the discrete interval's tick mark.
	 *
	 * @return Colors state list for interval's tick mark.
	 * @see #setDiscreteIntervalTickMarkColor(android.content.res.ColorStateList)
	 */
	@NonNull
	public ColorStateList getDiscreteIntervalTickMarkColors() {
		return DISCRETE_INTERVAL_TICK_MARK_INFO.colors;
	}

	/**
	 * Returns the current color used to draw the discrete interval's tick mark.
	 *
	 * @return Current interval's tick mark color.
	 */
	@ColorInt
	public int getDiscreteIntervalTickMarkCurrentColor() {
		return DISCRETE_INTERVAL_TICK_MARK_INFO.paint.getColor();
	}

	/**
	 * Same as {@link #setDiscreteIndicator(android.graphics.drawable.Drawable)} for resource id.
	 *
	 * @param resId Resource id of the desired drawable for discrete indicator. May be {@code 0} to
	 *              remove the current indicator.
	 */
	public void setDiscreteIndicator(@DrawableRes int resId) {
		if (resId == 0) {
			setDiscreteIndicator(null);
		} else if (mDiscreteIndicatorRes != resId) {
			setDiscreteIndicator(ResourceUtils.getDrawable(
					getResources(),
					mDiscreteIndicatorRes = resId,
					getContext().getTheme())
			);
		}
	}

	/**
	 * Sets the drawable used to draw the discrete indicator in <b>discrete mode</b>.
	 *
	 * @param indicator The desired drawable for discrete indicator. May be {@code null} to clear
	 *                  the current one.
	 * @see R.attr#uiDiscreteIndicator ui:uiDiscreteIndicator
	 * @see #setDiscreteIndicatorTintList(ColorStateList)
	 * @see #setDiscreteIndicatorTintMode(PorterDuff.Mode)
	 * @see #getDiscreteIndicator()
	 * @see #setDiscrete(boolean)
	 */
	public void setDiscreteIndicator(@Nullable Drawable indicator) {
		this.updateDiscreteIndicator(indicator);
	}

	/**
	 * Updates current indicator to the specified one. If this seek bar has discrete mode enabled
	 * ({@link #isDiscrete()}), the given indicator will be updated to scaleable drawable if it is
	 * not yet.
	 *
	 * @param indicator The new indicator to update to.
	 */
	private void updateDiscreteIndicator(Drawable indicator) {
		if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE)) {
			indicator = mAnimations.makeDiscreteIndicatorScaleable(
					indicator,
					Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
			);
		}
		if (mDiscreteIndicator != indicator) {
			final boolean needUpdate;
			if (mDiscreteIndicator != null) {
				mDiscreteIndicator.setCallback(null);
				unscheduleDrawable(mDiscreteIndicator);
				needUpdate = true;
			} else {
				needUpdate = false;
			}
			if (indicator != null) {
				indicator.setCallback(this);
				indicator.setVisible(getVisibility() == VISIBLE, false);

				if (indicator.getIntrinsicWidth() != mDiscreteIndicatorWidth ||
						indicator.getIntrinsicHeight() != mDiscreteIndicatorHeight) {
					this.mDiscreteIndicatorWidth = indicator.getIntrinsicWidth();
					this.mDiscreteIndicatorHeight = indicator.getIntrinsicHeight();
					requestLayout();
				}
			} else {
				this.mDiscreteIndicatorRes = 0;
				this.mDiscreteIndicatorWidth = mDiscreteIndicatorHeight = 0;
				requestLayout();
			}
			this.mDiscreteIndicator = indicator;
			this.applyDiscreteIndicatorTint();
			if (needUpdate) {
				this.updateDiscreteIndicatorPosition(getWidth(), getHeight());
				if (mDiscreteIndicator.isStateful()) {
					mDiscreteIndicator.setState(getDrawableState());
				}
				this.invalidateDiscreteIndicatorArea();
			}
		}
	}

	/**
	 * Returns the current discrete indicator's drawable.
	 * <p>
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setDiscreteIndicatorTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped indicator drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 *
	 * @return Discrete indicator's drawable.
	 * @see #setDiscreteIndicator(android.graphics.drawable.Drawable)
	 */
	@Nullable
	public Drawable getDiscreteIndicator() {
		if (mDiscreteIndicator instanceof ScaleDrawable)
			return ((ScaleDrawable) mDiscreteIndicator).getDrawable();
		else
			return mDiscreteIndicator;
	}

	/**
	 * Applies a tint to the discrete indicator, if specified. This call does not modify the current
	 * tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setDiscreteIndicator(android.graphics.drawable.Drawable)} will
	 * automatically mutate the drawable and apply the specified tint and tint mode using
	 * {@link android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see R.attr#uiDiscreteIndicatorTint ui:uiDiscreteIndicatorTint
	 * @see #getDiscreteIndicatorTintList()
	 * @see android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)
	 */
	public void setDiscreteIndicatorTintList(@Nullable ColorStateList tint) {
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.discreteIndicatorTintList = tint;
		tintInfo.hasDiscreteIndicatorTintList = true;
		this.applyDiscreteIndicatorTint();
	}

	/**
	 * Returns the tint applied to the discrete indicator's drawable, if specified.
	 *
	 * @return The discrete indicator's drawable tint.
	 * @see #setDiscreteIndicatorTintList(android.content.res.ColorStateList)
	 */
	@Nullable
	public ColorStateList getDiscreteIndicatorTintList() {
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().discreteIndicatorTintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setDiscreteIndicatorTintList(android.content.res.ColorStateList)}}
	 * to the discrete indicator. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see R.attr#uiDiscreteIndicatorTintMode ui:uiDiscreteIndicatorTintMode
	 * @see #getDiscreteIndicatorTintMode()
	 * @see android.graphics.drawable.Drawable#setTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setDiscreteIndicatorTintMode(@Nullable PorterDuff.Mode tintMode) {
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		tintInfo.discreteIndicatorTintMode = tintMode;
		tintInfo.hasDiscreteIndicatorTintMode = true;
		this.applyDiscreteIndicatorTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the discrete indicator's drawable, if
	 * specified.
	 *
	 * @return The discrete indicator's drawable blending mode used to apply the tint.
	 * @see #setDiscreteIndicatorTintMode(android.graphics.PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getDiscreteIndicatorTintMode() {
		this.ensureDecorator();
		return mDecorator.hasTintInfo() ? mDecorator.getTintInfo().discreteIndicatorTintMode : null;
	}

	/**
	 * Applies current discrete indicator tint from {@link Decorator#mTintInfo} to the current discrete
	 * indicator's drawable.
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("ConstantConditions")
	private void applyDiscreteIndicatorTint() {
		this.ensureDecorator();
		if (mDiscreteIndicator == null || !mDecorator.hasTintInfo()) {
			return;
		}
		final Drawable indicator = mDiscreteIndicator instanceof ScaleDrawable ? ((ScaleDrawable) mDiscreteIndicator).getDrawable() : mDiscreteIndicator;
		final SeekBarTintInfo tintInfo = mDecorator.getTintInfo();
		if ((!tintInfo.hasDiscreteIndicatorTintList && !tintInfo.hasDiscreteIndicatorTintMode)) {
			return;
		}
		if (UiConfig.MATERIALIZED) {
			this.mDiscreteIndicator = mDiscreteIndicator.mutate();
			if (tintInfo.hasDiscreteIndicatorTintList) {
				mDiscreteIndicator.setTintList(tintInfo.discreteIndicatorTintList);
			}
			if (tintInfo.hasDiscreteIndicatorTintMode) {
				mDiscreteIndicator.setTintMode(tintInfo.discreteIndicatorTintMode);
			}
			if (mDiscreteIndicator.isStateful()) {
				mDiscreteIndicator.setState(getDrawableState());
			}
			return;
		}
		final boolean isTintDrawable = indicator instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) indicator : new TintDrawable(indicator);
		if (tintInfo.hasDiscreteIndicatorTintList) {
			tintDrawable.setTintList(tintInfo.discreteIndicatorTintList);
		}
		if (tintInfo.hasDiscreteIndicatorTintMode) {
			tintDrawable.setTintMode(tintInfo.discreteIndicatorTintMode);
		}
		if (isTintDrawable) {
			return;
		}
		this.mDiscreteIndicator = mDecorator.hasPrivateFlag(PFLAG_DISCRETE) ?
				mAnimations.makeDiscreteIndicatorScaleable(tintDrawable, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL) :
				tintDrawable;
		mDiscreteIndicator.setCallback(this);
		updateDiscreteIndicatorState(getDrawableState(), false);
	}

	/**
	 * Sets a text color, size, and style for the discrete indicator's text from the specified
	 * TextAppearance resource.
	 *
	 * @param resId Resource id of the desired TextAppearance style.
	 * @see R.attr#uiDiscreteIndicatorTextAppearance ui:uiDiscreteIndicatorTextAppearance
	 * @see #setDiscreteIndicatorTextSize(int, float)
	 * @see #setDiscreteIndicatorTextColor(ColorStateList)
	 * @see #setDiscreteIndicatorTypeface(Typeface)
	 */
	public void setDiscreteIndicatorTextAppearance(@StyleRes int resId) {
		if (DISCRETE_INDICATOR_TEXT_INFO.fromTextAppearanceStyle(getContext(), resId) && DISCRETE_INDICATOR_TEXT_INFO.updatePaint(getDrawableState())) {
			this.invalidateDiscreteIndicatorArea();
		}
	}

	/**
	 * Same as {@link #setDiscreteIndicatorTextSize(int, float)} in {@link TypedValue#COMPLEX_UNIT_SP}
	 * and the specified <var>size</var>.
	 *
	 * @see #getDiscreteIndicatorTextSize()
	 */
	public void setDiscreteIndicatorTextSize(float size) {
		setDiscreteIndicatorTextSize(TypedValue.COMPLEX_UNIT_SP, size);
	}

	/**
	 * Sets a size for the discrete indicator's text to the given <var>unit</var> and <var>size</var>.
	 *
	 * @param unit The desired dimension unit. See {@link TypedValue} for possible units.
	 * @param size The desired size in the specified unit.
	 * @see #setDiscreteIndicatorTextSize(float)
	 * @see #getDiscreteIndicatorTextSize()
	 */
	public void setDiscreteIndicatorTextSize(int unit, float size) {
		setDiscreteIndicatorRawTextSize(TypedValue.applyDimension(
				unit,
				size,
				getResources().getDisplayMetrics()
		));
	}

	/**
	 * Sets the raw text size for the Paint used to draw numbers graphics.
	 *
	 * @param size The desired raw size in pixels.
	 */
	private void setDiscreteIndicatorRawTextSize(float size) {
		if (DISCRETE_INDICATOR_TEXT_INFO.updateTextSize(size)) {
			this.invalidateDiscreteIndicatorArea();
		}
	}

	/**
	 * Returns the size of the discrete indicator's text.
	 *
	 * @return Size used when drawing discrete indicator's text graphics.
	 * @see #setDiscreteIndicatorTextSize(int, float)
	 * @see #setDiscreteIndicatorTextAppearance(int)
	 */
	public float getDiscreteIndicatorTextSize() {
		return DISCRETE_INDICATOR_TEXT_INFO.paint.getTextSize();
	}

	/**
	 * Sets a single color for the discrete indicator's text.
	 *
	 * @param color The desired color.
	 * @see #setDiscreteIndicatorTextColor(ColorStateList)
	 */
	public void setDiscreteIndicatorTextColor(@ColorInt int color) {
		setDiscreteIndicatorTextColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for the discrete indicator's text.
	 *
	 * @param colors The desired colors state list.
	 * @see #setDiscreteIndicatorTextColor(int)
	 * @see #getDiscreteIndicatorTextColors()
	 * @see #getDiscreteIndicatorCurrentTextColor()
	 */
	public void setDiscreteIndicatorTextColor(@NonNull ColorStateList colors) {
		if (DISCRETE_INDICATOR_TEXT_INFO.updateTextColor(colors, getDrawableState())) {
			this.invalidateDiscreteIndicatorArea();
		}
	}

	/**
	 * Returns the colors for the discrete indicator's text.
	 *
	 * @return List of colors used when drawing discrete indicator's text graphics.
	 * @see #setDiscreteIndicatorTextColor(android.content.res.ColorStateList)
	 * @see #setDiscreteIndicatorTextColor(int)
	 * @see #getDiscreteIndicatorCurrentTextColor()
	 */
	@Nullable
	public ColorStateList getDiscreteIndicatorTextColors() {
		return DISCRETE_INDICATOR_TEXT_INFO.mAppearance.getTextColor();
	}

	/**
	 * Returns the current color used to draw the discrete indicator's text.
	 *
	 * @return Current discrete indicator's text color.
	 * @see #getDiscreteIndicatorTextColors()
	 */
	@ColorInt
	public int getDiscreteIndicatorCurrentTextColor() {
		return DISCRETE_INDICATOR_TEXT_INFO.paint.getColor();
	}

	/**
	 * Sets a typeface and style in which the discrete indicator's text should be displayed, and
	 * turns on the fake bold and italic bits in the Paint if the Typeface that you provided does
	 * not have all the bits in the style that you specified.
	 *
	 * @param typeface The desired typeface. May be {@code null} to create default one from the specified
	 *                 <var>style</var>.
	 * @param style    One of {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link Typeface#ITALIC}
	 *                 or {@link Typeface#BOLD_ITALIC}.
	 * @see #setDiscreteIndicatorTypeface(Typeface)
	 * @see #getDiscreteIndicatorTypeface()
	 * @see #getDiscreteIndicatorTypefaceStyle()
	 */
	public void setDiscreteIndicatorTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		if (DISCRETE_INDICATOR_TEXT_INFO.updateTypeface(typeface, style)) {
			this.invalidateDiscreteIndicatorArea();
		}
	}

	/**
	 * Sets a typeface in which the discrete indicator's text should be displayed.
	 * <p>
	 * <b>Note</b>, that not all Typeface families actually have bold and italic variants, so you
	 * may need to use {@link #setDiscreteIndicatorTypeface(Typeface, int)} to get the appearance that you actually
	 * want.
	 *
	 * @param typeface The desired typeface.
	 * @see #getDiscreteIndicatorTypeface()
	 * @see #getDiscreteIndicatorTypefaceStyle()
	 */
	public void setDiscreteIndicatorTypeface(@Nullable Typeface typeface) {
		if (DISCRETE_INDICATOR_TEXT_INFO.updateTypeface(typeface)) {
			this.invalidateDiscreteIndicatorArea();
		}
	}

	/**
	 * Returns the typeface used to draw text of the discrete indicator.
	 *
	 * @return Discrete indicator's text typeface.
	 * @see #setDiscreteIndicatorTypeface(Typeface, int)
	 * @see #setDiscreteIndicatorTypeface(Typeface)
	 * @see #getDiscreteIndicatorTypefaceStyle()
	 * @see #setDiscreteIndicatorTextAppearance(int)
	 */
	@Nullable
	public Typeface getDiscreteIndicatorTypeface() {
		return DISCRETE_INDICATOR_TEXT_INFO.paint.getTypeface();
	}

	/**
	 * Returns the style of the typeface used to draw the discrete indicator's text.
	 *
	 * @return Typeface style.
	 * @see #getDiscreteIndicatorTypeface()
	 * @see #setDiscreteIndicatorTypeface(Typeface, int)
	 */
	@TextAppearance.TextStyle
	@SuppressWarnings("ResourceType")
	public int getDiscreteIndicatorTypefaceStyle() {
		final Typeface typeface = DISCRETE_INDICATOR_TEXT_INFO.paint.getTypeface();
		return typeface != null ? typeface.getStyle() : Typeface.NORMAL;
	}

	/**
	 * Sets a gravity to apply to the discrete indicator's text.
	 *
	 * @param gravity The desired gravity flags. One of {@link Gravity#TOP}, {@link Gravity#BOTTOM},
	 *                {@link Gravity#START}, {@link Gravity#END}, {@link Gravity#CENTER_HORIZONTAL},
	 *                {@link Gravity#CENTER_VERTICAL}, {@link Gravity#CENTER} or theirs relevant combination.
	 * @see R.attr#uiDiscreteIndicatorTextGravity ui:uiDiscreteIndicatorTextGravity
	 * @see #getDiscreteIndicatorTextGravity()
	 * @see #setDiscreteIndicatorTextPadding(int, int, int, int)
	 */
	public void setDiscreteIndicatorTextGravity(int gravity) {
		if (DISCRETE_INDICATOR_TEXT_INFO.gravity != gravity) {
			this.DISCRETE_INDICATOR_TEXT_INFO.gravity = gravity;
			this.invalidateDiscreteIndicatorArea();
		}
	}

	/**
	 * Returns the gravity applied to the discrete indicator's text.
	 *
	 * @return Discrete indicator's text gravity.
	 * @see #setDiscreteIndicatorTextGravity(int)
	 */
	public int getDiscreteIndicatorTextGravity() {
		return DISCRETE_INDICATOR_TEXT_INFO.gravity;
	}

	/**
	 * Sets a padding for the discrete indicator's text. The specified padding will be used to
	 * position the indicator's text within the indicator's bounds, but will not cause the indicator
	 * bounds to change.
	 *
	 * @param start  The desired relative start padding. Properly used according to {@link #getLayoutDirection()}.
	 * @param top    The desired top padding.
	 * @param end    The desired relative end padding. Properly used according to {@link #getLayoutDirection()}.
	 * @param bottom The desired bottom padding.
	 * @see R.attr#uiDiscreteIndicatorTextPaddingStart ui:uiDiscreteIndicatorTextPaddingStart
	 * @see R.attr#uiDiscreteIndicatorTextPaddingTop ui:uiDiscreteIndicatorTextPaddingTop
	 * @see R.attr#uiDiscreteIndicatorTextPaddingEnd ui:uiDiscreteIndicatorTextPaddingEnd
	 * @see R.attr#uiDiscreteIndicatorTextPaddingBottom ui:uiDiscreteIndicatorTextPaddingBottom
	 * @see #getDiscreteIndicatorTextPaddingStart()
	 * @see #getDiscreteIndicatorTextPaddingTop()
	 * @see #getDiscreteIndicatorTextPaddingEnd()
	 * @see #getDiscreteIndicatorTextPaddingBottom()
	 * @see #setDiscreteIndicatorTextGravity(int)
	 */
	public void setDiscreteIndicatorTextPadding(int start, int top, int end, int bottom) {
		final Rect padding = DISCRETE_INDICATOR_TEXT_INFO.padding;
		if (padding.left != start || padding.top != top || padding.right != end || padding.bottom != bottom) {
			padding.left = start;
			padding.top = top;
			padding.right = end;
			padding.bottom = bottom;
			this.invalidateDiscreteIndicatorArea();
		}
	}

	/**
	 * Returns the start padding of the discrete indicator's text.
	 *
	 * @return Indicator's text left padding if layout direction is {@link #LAYOUT_DIRECTION_LTR},
	 * right padding if it is {@link #LAYOUT_DIRECTION_RTL}.
	 * @see #setDiscreteIndicatorTextPadding(int, int, int, int)
	 * @see #setDiscreteIndicatorTextGravity(int)
	 */
	public int getDiscreteIndicatorTextPaddingStart() {
		return hasRTLDirection() ?
				DISCRETE_INDICATOR_TEXT_INFO.padding.right :
				DISCRETE_INDICATOR_TEXT_INFO.padding.left;
	}

	/**
	 * Returns the top padding of the discrete indicator's text.
	 *
	 * @return Indicator's text top padding.
	 * @see #setDiscreteIndicatorTextPadding(int, int, int, int)
	 * @see #setDiscreteIndicatorTextGravity(int)
	 */
	public int getDiscreteIndicatorTextPaddingTop() {
		return DISCRETE_INDICATOR_TEXT_INFO.padding.top;
	}

	/**
	 * Returns the end padding of the discrete indicator's text.
	 *
	 * @return Indicator's text right padding if layout direction is {@link #LAYOUT_DIRECTION_LTR},
	 * left padding if it is {@link #LAYOUT_DIRECTION_RTL}.
	 * @see #setDiscreteIndicatorTextPadding(int, int, int, int)
	 * @see #setDiscreteIndicatorTextGravity(int)
	 */
	public int getDiscreteIndicatorTextPaddingEnd() {
		return hasRTLDirection() ?
				DISCRETE_INDICATOR_TEXT_INFO.padding.left :
				DISCRETE_INDICATOR_TEXT_INFO.padding.right;
	}

	/**
	 * Returns the bottom padding of the discrete indicator's text.
	 *
	 * @return Indicator's text bottom padding.
	 * @see #setDiscreteIndicatorTextPadding(int, int, int, int)
	 * @see #setDiscreteIndicatorTextGravity(int)
	 */
	public int getDiscreteIndicatorTextPaddingBottom() {
		return DISCRETE_INDICATOR_TEXT_INFO.padding.bottom;
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
	public void setFont(@NonNull String fontPath) {
		this.ensureDecorator();
		mDecorator.setFont(fontPath);
	}

	/**
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		setDiscreteIndicatorTypeface(typeface, style);
	}

	/**
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeFace) {
		setDiscreteIndicatorTypeface(typeFace);
	}

	/**
	 * Checks whether this view has specified {@link #LAYOUT_DIRECTION_RTL} as its layout direction
	 * via {@link #setLayoutDirection(int)} or not.
	 *
	 * @return {@code True} if layout direction of this view is {@link #LAYOUT_DIRECTION_RTL}, false
	 * if it is {@link #LAYOUT_DIRECTION_LTR}.
	 */
	private boolean hasRTLDirection() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && getLayoutDirection() == LAYOUT_DIRECTION_RTL;
	}

	/**
	 */
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		updateDrawablesState(false);
		if (!UiConfig.MATERIALIZED) {
			this.applyProgressTints();
			this.applyThumbTint();
		}
	}

	/**
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.previewDiscreteComponents(0, PREVIEW_DISCRETE_COMPONENTS_DURATION);
	}

	/**
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAnimations.cancel();
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
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE) && mDiscreteIndicatorHeight > 0) {
			// Measure extra space for discrete indicator.
			setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + mDiscreteIndicatorHeight);
		}
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.ensureDecorator();
		mDecorator.onSizeChanged(w, h, oldw, oldh);
		if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE) && mDiscreteIndicatorHeight > 0 && h != oldh) {
			this.updateTrackPosition();
			this.updateThumbPosition();
			this.updateDiscreteIndicatorPosition(getMeasuredWidth(), getMeasuredHeight());
		}
	}

	/**
	 * Updates the current bounds of the progress track. This will move the track below the discrete
	 * indicator if it is enabled for this SeekBarWidget.
	 */
	private void updateTrackPosition() {
		if (mProgressDrawable == null) {
			return;
		}
		final Rect bounds = mProgressDrawable.getBounds();
		if (UiConfig.MATERIALIZED) {
			mProgressDrawable.setBounds(
					bounds.left,
					bounds.top + (mDiscreteIndicatorHeight / 2),
					bounds.right,
					bounds.bottom + (mDiscreteIndicatorHeight / 2)
			);
		} else {
			final int top = bounds.top + mDiscreteIndicatorHeight;
			mProgressDrawable.setBounds(
					bounds.left,
					top,
					bounds.right,
					top + mProgressDrawable.getIntrinsicHeight()
			);
		}
	}

	/**
	 * Updates the current bounds of the thumb. This will move the thumb below the discrete indicator
	 * if it is enabled for this SeekBarWidget.
	 */
	private void updateThumbPosition() {
		if (mThumb == null) {
			return;
		}
		final Rect bounds = mThumb.getBounds();
		final int top = getPaddingTop() + mDiscreteIndicatorHeight;
		mThumb.setBounds(
				bounds.left,
				top,
				bounds.right,
				top + mThumb.getIntrinsicHeight()
		);
	}

	/**
	 * Updates the current bounds of the discrete indicator's drawable depends on the specified
	 * <var>width</var> and <var>height</var> and the current value of progress.
	 *
	 * @param width  Current width of this view.
	 * @param height Current height of this view.
	 */
	private void updateDiscreteIndicatorPosition(int width, int height) {
		if (mDiscreteIndicator == null) {
			return;
		}
		final float progressRatio = getProgress() / (float) getMax();
		width -= getPaddingLeft() + getPaddingRight();
		final int left = Math.round(progressRatio * width);
		final int thumbHeight = mThumb != null ? mThumb.getIntrinsicHeight() : 0;
		final int top = getPaddingTop() + thumbHeight / 4;
		mDiscreteIndicator.setBounds(
				left,
				top,
				left + mDiscreteIndicatorWidth,
				top + mDiscreteIndicatorHeight
		);
	}

	/**
	 * Like {@link #revealDiscreteComponents()} but this will show the discrete components only for
	 * a the specified amount of time (thus just preview) and then the discrete components will be
	 * automatically hided again.
	 *
	 * @param delay    Delay with which should be the reveal animation of discrete components started.
	 * @param duration The duration for how long should be the discrete components previewed.
	 */
	private void previewDiscreteComponents(long delay, long duration) {
		if (isEnabled() && mDecorator.hasPrivateFlag(PFLAG_DISCRETE) && mDecorator.hasPrivateFlag(PFLAG_DISCRETE_PREVIEW_ENABLED))
			mAnimations.previewDiscreteComponents(delay, duration);
	}

	/**
	 * Reveals all discrete components in order to get this SeekBar (if discrete) to its discrete
	 * state where such components should be visible with an animation.
	 *
	 * @see #concealDiscreteComponents()
	 */
	private void revealDiscreteComponents() {
		if (isEnabled() && mDecorator.hasPrivateFlag(PFLAG_DISCRETE)) {
			mAnimations.revealDiscreteComponents();
		}
	}

	/**
	 * Conceals all discrete components in order to revert this SeekBar (if discrete) back to its idle
	 * state with an animation.
	 *
	 * @see #revealDiscreteComponents()
	 */
	private void concealDiscreteComponents() {
		if (isEnabled() && mDecorator.hasPrivateFlag(PFLAG_DISCRETE))
			mAnimations.concealDiscreteComponents();
	}

	/**
	 */
	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mDiscreteIndicator || super.verifyDrawable(who);
	}

	/**
	 * Updates state of all drawables and drawing related object of this view to the current state
	 * of this view specified by {@link #getDrawableState()}.
	 *
	 * @param invalidate {@code True} to perform invalidation of this view, {@code false} otherwise.
	 */
	private void updateDrawablesState(boolean invalidate) {
		final int[] state = getDrawableState();
		this.updateDiscreteIndicatorState(state, false);
		this.updateDiscreteIntervalTickMarksState(state, false);
		if (invalidate) invalidate();
	}

	/**
	 * Updates the current state of the discrete indicator's graphics.
	 *
	 * @param state      The state according to which to update the graphics.
	 * @param invalidate {@code True} to perform invalidation of this view, {@code false} otherwise.
	 */
	@SuppressWarnings("CheckResult")
	private void updateDiscreteIndicatorState(int[] state, boolean invalidate) {
		if (mDiscreteIndicator != null && mDiscreteIndicator.isStateful()) {
			mDiscreteIndicator.setState(state);
		}
		DISCRETE_INDICATOR_TEXT_INFO.updatePaint(state);
		if (invalidate) this.invalidateDiscreteIndicatorArea();
	}

	/**
	 * Updates the current state of the discrete interval's graphics.
	 *
	 * @param state      The state according to which to update the graphics.
	 * @param invalidate {@code True} to perform invalidation of this view, {@code false} otherwise.
	 */
	private void updateDiscreteIntervalTickMarksState(int[] state, boolean invalidate) {
		if (DISCRETE_INTERVAL_TICK_MARK_INFO.updatePaint(state) && invalidate) {
			this.invalidateDiscreteIntervalsArea();
		}
	}

	/**
	 * Invalidates this view in area where the discrete indicator is at this time presented using
	 * its current bounds.
	 *
	 * @see #invalidate(Rect)
	 */
	private void invalidateDiscreteIndicatorArea() {
		if (mDiscreteIndicator != null) invalidate(mDiscreteIndicator.getBounds());
	}

	/**
	 * Invalidates this view in area where the progress track is presented using its current bounds.
	 *
	 * @see #invalidate(Rect)
	 */
	private void invalidateDiscreteIntervalsArea() {
		if (mProgressDrawable != null) invalidate(mProgressDrawable.getBounds());
	}

	/**
	 */
	@Override
	@SuppressWarnings("NewApi")
	protected synchronized void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);
		if (mDecorator.hasPrivateFlag(PFLAG_DISCRETE) && mAnimations.shouldDraw()) {
			this.drawDiscreteInterval(canvas);
			this.drawDiscreteIndicator(canvas);
			if (mAnimations.areRunning() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				postInvalidateOnAnimation();
			}
		}
	}

	/**
	 * Draws discrete interval, its thick marks depending on the current {@link #mDiscreteIntervalRatio}
	 * value.
	 *
	 * @param canvas Canvas on which to draw discrete interval.
	 */
	private void drawDiscreteInterval(Canvas canvas) {
		if (mDiscreteIntervalRatio == 0 || mProgressDrawable == null) {
			return;
		}

		final Rect trackBounds = mProgressDrawable.getBounds();
		final int trackLeft = getPaddingLeft();
		final int cy = trackBounds.centerY();
		float trackWidth = trackBounds.width();
		final float discreteInterval = mDiscreteIntervalRatio * trackWidth;
		trackWidth += DISCRETE_INTERVAL_TICK_MARK_INFO.radius;
		final Rect thumbBounds = mThumb != null ? mThumb.getBounds() : null;
		final int thumbOffset = getThumbOffset();

		float cx = 0;
		while (cx <= trackWidth) {
			// Ensure to not draw over thumb if it is not expected behaviour.
			final boolean isAtThumbPosition = thumbBounds != null &&
					trackLeft + cx >= thumbBounds.left + thumbOffset &&
					trackLeft + cx <= thumbBounds.right + thumbOffset;
			if (CAN_DRAW_DISCRETE_INTERVAL_OVER_THUMB || !isAtThumbPosition) {
				canvas.drawCircle(
						trackLeft + cx,
						cy,
						DISCRETE_INTERVAL_TICK_MARK_INFO.radius,
						DISCRETE_INTERVAL_TICK_MARK_INFO.paint
				);
			}
			cx += discreteInterval;
		}
	}

	/**
	 * Draws discrete indicator of this SeekBarWidget at its current position updated by
	 * {@link #updateDiscreteIndicatorPosition(int, int)} according to the current progress.
	 *
	 * @param canvas Canvas on which to draw discrete indicator's drawable.
	 */
	private void drawDiscreteIndicator(Canvas canvas) {
		if (mDiscreteIndicatorHeight == 0) {
			return;
		}
		// todo: draw according to LTR/RTL layout direction.
		mDiscreteIndicator.draw(canvas);

		// Draw current progress over indicator's graphics.
		final Rect indicatorBounds = mDiscreteIndicator.getBounds();
		final Paint textPaint = DISCRETE_INDICATOR_TEXT_INFO.paint;
		textPaint.getTextBounds("0", 0, 1, mRect);
		final float textSize = mRect.height();
		final Rect textPadding = DISCRETE_INDICATOR_TEXT_INFO.padding;
		final int absoluteTextGravity = WidgetGravity.getAbsoluteGravity(
				DISCRETE_INDICATOR_TEXT_INFO.gravity,
				ViewCompat.getLayoutDirection(this)
		);

		final float textX, textY;
		// Resolve horizontal text position according to the requested gravity.
		switch (absoluteTextGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
			case Gravity.CENTER_HORIZONTAL:
				textPaint.setTextAlign(Paint.Align.CENTER);
				textX = indicatorBounds.centerX();
				break;
			case Gravity.RIGHT:
				textPaint.setTextAlign(Paint.Align.RIGHT);
				textX = indicatorBounds.right - textPadding.right;
				break;
			case Gravity.LEFT:
			default:
				textPaint.setTextAlign(Paint.Align.LEFT);
				textX = indicatorBounds.left + textPadding.left;
				break;
		}
		// Resolve vertical text position according to the requested gravity.
		switch (absoluteTextGravity & Gravity.VERTICAL_GRAVITY_MASK) {
			case Gravity.CENTER_VERTICAL:
				textY = indicatorBounds.centerY() + textSize / 2f;
				break;
			case Gravity.BOTTOM:
				textY = indicatorBounds.bottom - textPadding.bottom;
				break;
			case Gravity.TOP:
			default:
				textY = indicatorBounds.top + textSize + textPadding.top;
				break;
		}
		canvas.drawText(Integer.toString(getProgress()), textX, textY, textPaint);
	}

	/**
	 */
	@NonNull
	@Override
	public Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.privateFlags = mDecorator.mPrivateFlags;
		return savedState;
	}

	/**
	 */
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		setDiscrete((savedState.privateFlags & PFLAG_DISCRETE) != 0);
		setDiscretePreviewEnabled((savedState.privateFlags & PFLAG_DISCRETE_PREVIEW_ENABLED) != 0);
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link SeekBarWidget}
	 * is properly saved.
	 *
	 * @author Martin Albedinsky
	 */
	public static class SavedState extends WidgetSavedState {

		/**
		 * Creator used to create an instance or array of instances of SavedState from {@link android.os.Parcel}.
		 */
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			/**
			 */
			@Override
			public SavedState createFromParcel(@NonNull Parcel source) {
				return new SavedState(source);
			}

			/**
			 */
			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

		/**
		 */
		int privateFlags;

		/**
		 * Creates a new instance of SavedState with the given <var>superState</var> to allow chaining
		 * of saved states in {@link #onSaveInstanceState()} and also in {@link #onRestoreInstanceState(android.os.Parcelable)}.
		 *
		 * @param superState The super state obtained from {@code super.onSaveInstanceState()} within
		 *                   {@code onSaveInstanceState()}.
		 */
		protected SavedState(@NonNull Parcelable superState) {
			super(superState);
		}

		/**
		 * Called from {@link #CREATOR} to create an instance of SavedState form the given parcel
		 * <var>source</var>.
		 *
		 * @param source Parcel with data for the new instance.
		 */
		protected SavedState(@NonNull Parcel source) {
			super(source);
			this.privateFlags = source.readInt();
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(privateFlags);
		}
	}

	/**
	 * This class holds all data necessary to tint all components of this view.
	 */
	private static final class SeekBarTintInfo extends BackgroundTintInfo {

		/**
		 * Color state list used to tint a specific states of the <b>primary progress</b> drawable.
		 */
		ColorStateList primaryProgressTintList;

		/**
		 * Flag indicating whether the {@link #primaryProgressTintList} has been set or not.
		 */
		boolean hasPrimaryProgressTintList;

		/**
		 * Blending mode used to apply tint to the <b>primary progress</b> drawable.
		 */
		PorterDuff.Mode primaryProgressTintMode;

		/**
		 * Flag indicating whether the {@link #primaryProgressTintMode} has been set or not.
		 */
		boolean hasPrimaryProgressTintMode;

		/**
		 * Color state list used to tint a specific states of the <b>secondary progress</b> drawable.
		 */
		ColorStateList secondaryProgressTintList;

		/**
		 * Flag indicating whether the {@link #secondaryProgressTintList} has been set or not.
		 */
		boolean hasSecondaryProgressTintList;

		/**
		 * Blending mode used to apply tint to the <b>secondary progress</b> drawable.
		 */
		PorterDuff.Mode secondaryProgressTintMode;

		/**
		 * Flag indicating whether the {@link #secondaryProgressTintMode} has been set or not.
		 */
		boolean hasSecondaryProgressTintMode;

		/**
		 * Color state list used to tint a specific states of the <b>progress background</b> drawable.
		 */
		ColorStateList progressBackgroundTintList;

		/**
		 * Flag indicating whether the {@link #progressBackgroundTintList} has been set or not.
		 */
		boolean hasProgressBackgroundTintList;

		/**
		 * Blending mode used to apply tint to the <b>progress background</b> drawable.
		 */
		PorterDuff.Mode progressBackgroundTintMode;

		/**
		 * Flag indicating whether the {@link #progressBackgroundTintMode} has been set or not.
		 */
		boolean hasProgressBackgroundTintMode;

		/**
		 * Color state list used to tint a specific states of the <b>discrete indicator</b> drawable.
		 */
		ColorStateList discreteIndicatorTintList;

		/**
		 * Blending mode used to apply tint to the <b>discrete indicator</b> drawable.
		 */
		boolean hasDiscreteIndicatorTintList;

		/**
		 * Blending mode used to apply tint to the <b>discrete indicator</b> drawable.
		 */
		PorterDuff.Mode discreteIndicatorTintMode;

		/**
		 * Flag indicating whether the {@link #discreteIndicatorTintMode} has been set or not.
		 */
		boolean hasDiscreteIndicatorTintMode;
	}

	/**
	 * Graphics info that holds all parameters necessary to draw progress text within discrete indicator.
	 */
	private static final class DiscreteIndicatorTextInfo extends TextGraphicsInfo {

		/**
		 * Flags determining where in the discrete indicator's area position the progress text.
		 */
		int gravity = Gravity.CENTER;

		/**
		 * Padding for the progress text.
		 */
		final Rect padding;

		/**
		 * Creates a new instance of DiscreteIndicatorTextInfo.
		 */
		DiscreteIndicatorTextInfo() {
			super();
			this.padding = new Rect();
		}
	}

	/**
	 * Graphics info that holds all parameters necessary to draw tick marks for discrete interval.
	 */
	private static final class DiscreteIntervalTickMarkInfo extends ColorGraphicsInfo {

		/**
		 * Radius of the tick marks of discrete interval.
		 */
		float radius;

		/**
		 * Creates a new instance of DiscreteIntervalTickMarkInfo.
		 */
		DiscreteIntervalTickMarkInfo() {
			super();
		}
	}

	/**
	 * Animations interface for this view.
	 */
	private static abstract class Animations {

		/**
		 * Duration for all animations related to discrete components.
		 */
		static final long DISCRETE_COMPONENTS_ANIMATION_DURATION = 350;

		/**
		 * Action to hide discrete components from the preview mode.
		 */
		final Runnable HIDE_DISCRETE_COMPONENTS_FROM_PREVIEW = new Runnable() {

			/**
			 */
			@Override
			public void run() {
				if (!view.isPressed() && onConcealDiscreteComponents()) {
					discreteComponentsActive = false;
				}
			}
		};

		/**
		 * View upon which will be animations performed.
		 */
		final SeekBarWidget view;

		/**
		 * Boolean flag indicating whether the preview of discrete components is active or not.
		 */
		boolean discreteComponentsActive;

		/**
		 * Current transformation value of the discrete components.
		 */
		float transformation = 0;

		/**
		 * Creates a new instance of Animations for the specified view.
		 *
		 * @param view The empty view for upon which to run animations.
		 */
		Animations(SeekBarWidget view) {
			this.view = view;
		}

		/**
		 * Returns a new instance of Animations implementation specific for the current animations
		 * API capabilities.
		 *
		 * @param view The view upon which will the returned Animations object perform all requested
		 *             animations.
		 * @return New instance of Animations implementation.
		 */
		static Animations get(SeekBarWidget view) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				return new LollipopAnimations(view);
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				return new HoneyCombAnimations(view);
			}
			return new DefaultAnimations(view);
		}

		/**
		 * Makes the given <var>thumb</var> drawable scaleable along a pivot determined by the specified
		 * <var>gravity</var> flags.
		 *
		 * @see #makeDrawableScaleable(Drawable, int)
		 */
		Drawable makeThumbScaleable(Drawable thumb, int gravity) {
			return makeDrawableScaleable(thumb, gravity);
		}

		/**
		 * Makes the given <var>indicator</var> drawable scaleable along a pivot determined
		 * by the specified <var>gravity</var> flags.
		 *
		 * @see #makeDrawableScaleable(Drawable, int)
		 */
		Drawable makeDiscreteIndicatorScaleable(Drawable indicator, int gravity) {
			return makeDrawableScaleable(indicator, gravity);
		}

		/**
		 * Wraps the given <var>drawable</var> into instance of {@link ScaleDrawable} if it is valid
		 * and not ScaleDrawable yet.
		 *
		 * @param drawable The drawable to wrap and make scaleable.
		 * @param gravity  The gravity determining a pivot along which can be the given drawable scaled.
		 * @return Instance of ScaleDrawable with the specified drawable wrapped or {@code null}
		 * if the given drawable was also {@code null}.
		 */
		static Drawable makeDrawableScaleable(Drawable drawable, int gravity) {
			if (drawable == null || drawable instanceof ScaleDrawable) return drawable;
			final ScaleDrawable scaleDrawable = new ScaleDrawable(drawable, gravity, 1f, 1f);
			scaleDrawable.setLevel(MAX_LEVEL);
			return scaleDrawable;
		}

		/**
		 * Like {@link #revealDiscreteComponents()} but this will reveal the discrete components
		 * only temporarily and conceals them after the specified duration has been reached.
		 *
		 * @param delay    Delay with which should be the reveal animation started.
		 * @param duration The duration for how long should be the discrete components previewed.
		 */
		void previewDiscreteComponents(long delay, long duration) {
			view.removeCallbacks(HIDE_DISCRETE_COMPONENTS_FROM_PREVIEW);
			if (discreteComponentsActive) {
				view.postDelayed(HIDE_DISCRETE_COMPONENTS_FROM_PREVIEW, duration);
				return;
			}

			// Preview the discrete components for a while so a user can detect that the seek bar
			// is really in discrete mode.
			onRevealDiscreteComponents(delay);
			view.postDelayed(HIDE_DISCRETE_COMPONENTS_FROM_PREVIEW, duration);
			this.discreteComponentsActive = true;
		}

		/**
		 * Reveals all discrete components with an animation if they are not active (visible) at this
		 * time yet.
		 */
		void revealDiscreteComponents() {
			if (!discreteComponentsActive) {
				onRevealDiscreteComponents(0);
				this.discreteComponentsActive = true;
			}
		}

		/**
		 * Invoked whenever {@link #revealDiscreteComponents()} or {@link #previewDiscreteComponents(long, long)}
		 * is called an the discrete components are not active (visible) at the time.
		 *
		 * @param delay The delay with which should be the reveal animation started.
		 * @return {@code True} if the reveal animation for the discrete components has been started,
		 * {@code false otherwise}.
		 */
		abstract boolean onRevealDiscreteComponents(long delay);

		/**
		 * Conceals all discrete components with an animation if they are active (visible) at this time.
		 */
		void concealDiscreteComponents() {
			if (discreteComponentsActive) {
				view.removeCallbacks(HIDE_DISCRETE_COMPONENTS_FROM_PREVIEW);
				this.discreteComponentsActive = false;
				onConcealDiscreteComponents();
			}
		}

		/**
		 * Invoked whenever {@link #concealDiscreteComponents()} is called an the discrete components
		 * are active (visible) at the time.
		 *
		 * @return {@code True} if the conceal animation for the discrete components has been started,
		 * {@code false otherwise}.
		 */
		abstract boolean onConcealDiscreteComponents();

		/**
		 * Specifies a transformation for the discrete components. Depends on the implementation,
		 * this can change alpha value of some discrete components or theirs current scale for example.
		 * <p>
		 * This method can be used to animate revealing/concealing of the discrete components or theirs
		 * immediate hiding/showing.
		 *
		 * @param transformation The desired transformation from the range {@code [0.0, 1.0]}.
		 *                       Transformation {@code 0.0} means that discrete components will be
		 *                       hided, {@code 1.0} means that they will be visible.
		 */
		@Keep
		void setDiscreteTransformation(float transformation) {
			if (this.transformation != transformation) {
				this.transformation = transformation;
				// Scale up/down the discrete indicator and the thumb in a way when one is fully scaled
				// up the other is fully scaled down and reversed.
				setThumbScale(1 - transformation);
				setDiscreteIndicatorScale(transformation);

				// Fade in/out the text of discrete indicator during the indicator is at least 75% transformed/visible.
				if (transformation > 0.75) {
					final int alpha = Math.round((transformation - 0.75f) / 0.25f * 255);
					setDiscreteIndicatorTextAlpha(alpha);
				} else {
					setDiscreteIndicatorTextAlpha(0);
				}

				// Fade in/out discrete interval.
				setDiscreteIntervalAlpha(Math.round(transformation * 255));
				invalidate();
			}
		}

		/**
		 * Updates a scale level of the thumb's drawable.
		 *
		 * @param scale The scale value from the range {@code [0.0, 1.0]}.
		 */
		@SuppressWarnings("Range")
		void setThumbScale(float scale) {
			if (view.mThumb instanceof ScaleDrawable) {
				final int scaleLevel = Math.round(scale * MAX_LEVEL);
				view.mThumb.setLevel(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN ?
						scaleLevel :
						// Correct scale level for pre JELLY_BEAN Android versions.
						// scaleLevel(10000) = scale(1.0) [expected scale(1.0)]
						// scaleLevel(5000)  = scale(0.0) [expected scale(0.5)]
						// scaleLevel(0)     = scale(1.0) [expected scale(0.0)]
						scaleLevel + (int) ((10000 - scaleLevel) / 10000f * 5000)
				);
			}
		}

		/**
		 * Updates a scale level of the discrete indicator's drawable along with its text size.
		 *
		 * @param scale The scale value from the range {@code [0.0, 1.0]}.
		 */
		void setDiscreteIndicatorScale(float scale) {
			updateDrawableScale(view.mDiscreteIndicator, scale);
			view.DISCRETE_INDICATOR_TEXT_INFO.paint.setTextSize(
					view.DISCRETE_INDICATOR_TEXT_INFO.mAppearance.getTextSize() * scale
			);
		}

		/**
		 * Updates a scale level of the given <var>drawable</var> according to the specified scale
		 * value.
		 *
		 * @param drawable The drawable of which scale level to update.
		 * @param scale    The scale value from the range {@code [0.0, 1.0]}.
		 */
		private void updateDrawableScale(Drawable drawable, float scale) {
			if (drawable instanceof ScaleDrawable) drawable.setLevel(Math.round(scale * MAX_LEVEL));
		}

		/**
		 * Specifies an alpha value for the graphics of the text of discrete indicator.
		 *
		 * @param alpha The desired alpha from the range {@code [0, 255]}.
		 */
		void setDiscreteIndicatorTextAlpha(int alpha) {
			view.DISCRETE_INDICATOR_TEXT_INFO.paint.setAlpha(alpha);
		}

		/**
		 * Specifies an alpha value for the graphics of the discrete interval.
		 *
		 * @param alpha The desired alpha from the range {@code [0, 255]}.
		 */
		void setDiscreteIntervalAlpha(int alpha) {
			view.DISCRETE_INTERVAL_TICK_MARK_INFO.paint.setAlpha(alpha);
		}

		/**
		 * Causes invalidation of the attached view.
		 */
		final void invalidate() {
			view.invalidate();
		}

		/**
		 * Checks whether components that are animated should be drawn or not.
		 *
		 * @return {@code True} to draw components animated by this object, {@code false} otherwise.
		 */
		boolean shouldDraw() {
			return transformation > 0;
		}

		/**
		 * Checks whether some animations are running or not.
		 *
		 * @return {@code True} if at least one animation is running at this time, {@code false} otherwise.
		 */
		abstract boolean areRunning();

		/**
		 * Cancels all running animations.
		 */
		abstract void cancel();
	}

	/**
	 * Default implementation of {@link Animations}.
	 */
	private static final class DefaultAnimations extends Animations {

		/**
		 * Animation used to reveal discrete components.
		 */
		private final Animation revealDiscreteComponentsAnimation;

		/**
		 * Animation used to conceal discrete components.
		 */
		private final Animation concealDiscreteComponentsAnimation;

		/**
		 * See {@link Animations#Animations(SeekBarWidget)}.
		 */
		DefaultAnimations(SeekBarWidget view) {
			super(view);
			this.revealDiscreteComponentsAnimation = new DiscreteComponentsAnimation(this, 0.0f, 1.0f);
			revealDiscreteComponentsAnimation.setDuration(DISCRETE_COMPONENTS_ANIMATION_DURATION);

			this.concealDiscreteComponentsAnimation = new DiscreteComponentsAnimation(this, 1.0f, 0.0f);
			concealDiscreteComponentsAnimation.setDuration(DISCRETE_COMPONENTS_ANIMATION_DURATION);
		}

		/**
		 */
		@Override
		boolean onRevealDiscreteComponents(long delay) {
			revealDiscreteComponentsAnimation.setStartOffset(delay);
			view.startAnimation(revealDiscreteComponentsAnimation);
			return false;
		}

		/**
		 */
		@Override
		boolean onConcealDiscreteComponents() {
			view.startAnimation(concealDiscreteComponentsAnimation);
			return false;
		}

		/**
		 */
		@Override
		boolean areRunning() {
			final Animation animation = view.getAnimation();
			return animation == revealDiscreteComponentsAnimation || animation == concealDiscreteComponentsAnimation;
		}

		/**
		 */
		@Override
		void cancel() {
			view.clearAnimation();
		}
	}

	/**
	 * An {@link Animations} implementation used for post {@link android.os.Build.VERSION_CODES#HONEYCOMB HONEYCOMB}
	 * Android versions.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class HoneyCombAnimations extends Animations {

		/**
		 * Animator used to animate discrete components.
		 */
		private final ObjectAnimator discreteComponentsAnimator;

		/**
		 * See {@link Animations#Animations(SeekBarWidget)}.
		 */
		HoneyCombAnimations(SeekBarWidget view) {
			super(view);
			this.discreteComponentsAnimator = ObjectAnimator.ofFloat(this, "discreteTransformation", 0, 0);
			this.discreteComponentsAnimator.setDuration(DISCRETE_COMPONENTS_ANIMATION_DURATION);
		}

		/**
		 */
		@Override
		boolean onRevealDiscreteComponents(long delay) {
			if (transformation == 1) return false;
			discreteComponentsAnimator.setFloatValues(transformation, 1f);
			discreteComponentsAnimator.setStartDelay(delay);
			discreteComponentsAnimator.start();
			return true;
		}

		/**
		 */
		@Override
		boolean onConcealDiscreteComponents() {
			if (transformation == 0) return false;
			discreteComponentsAnimator.setFloatValues(transformation, 0f);
			discreteComponentsAnimator.setStartDelay(0);
			discreteComponentsAnimator.start();
			return true;
		}

		/**
		 */
		@Override
		boolean areRunning() {
			return discreteComponentsAnimator.isRunning();
		}

		/**
		 */
		@Override
		void cancel() {
			if (discreteComponentsAnimator.isRunning()) {
				discreteComponentsAnimator.cancel();
			}
		}
	}

	/**
	 * An {@link Animations} implementation used for post {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
	 * Android versions.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static final class LollipopAnimations extends HoneyCombAnimations {

		/**
		 * See {@link HoneyCombAnimations#HoneyCombAnimations(SeekBarWidget)}.
		 */
		LollipopAnimations(SeekBarWidget view) {
			super(view);
		}

		/**
		 */
		@Override
		Drawable makeThumbScaleable(Drawable thumb, int gravity) {
			// fixme: Unfortunately on LOLLIPOP and higher the thumb wrapped into ScaleDrawable is
			// fixme: drawn by the framework with some alpha mask or whatever and that causes the
			// fixme: progress graphics not to be drawn behind the thumb, and also mThumb.setLevel(int)
			// fixme: with current scale level is not working so the thumb is not being scaled at all
			// fixme: because the thumb drawable on LOLLIPOP is actually the animated-selector and
			// fixme: it appears that such drawable cannot be scaled.
			return thumb;
		}
	}

	/**
	 * An {@link Animation} implementation that can be used to animate transformation value of
	 * discrete components.
	 */
	private static final class DiscreteComponentsAnimation extends Animation {

		/**
		 * Animations instance that can handle transformation change of discrete components.
		 */
		final Animations animations;

		/**
		 * Start transformation value from which to animate.
		 */
		final float fromTransformation;

		/**
		 * End transformation value to which to animate.
		 */
		final float toTransformation;

		/**
		 * Creates a new instance of DiscreteComponentsAnimation with the specified parameters.
		 *
		 * @param animations         The animations instance to which we can delegate animated transformation
		 *                           change.
		 * @param fromTransformation The transformation value from which to animate.
		 * @param toTransformation   The transformation value to which to animate.
		 */
		DiscreteComponentsAnimation(Animations animations, float fromTransformation, float toTransformation) {
			this.animations = animations;
			this.fromTransformation = fromTransformation;
			this.toTransformation = toTransformation;
		}

		/**
		 */
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation transformation) {
			final float trans = fromTransformation;
			animations.setDiscreteTransformation(trans + ((toTransformation - trans) * interpolatedTime));
		}
	}

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends FontWidgetDecorator<SeekBarWidget> {

		/**
		 * See {@link WidgetDecorator#WidgetDecorator(View, int[])}.
		 */
		Decorator(SeekBarWidget widget) {
			super(widget, R.styleable.Ui_SeekBar);
		}

		/**
		 */
		@Override
		BackgroundTintInfo onCreateTintInfo() {
			return new SeekBarTintInfo();
		}

		/**
		 */
		@NonNull
		@Override
		SeekBarTintInfo getTintInfo() {
			return (SeekBarTintInfo) super.getTintInfo();
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			final SeekBarTintInfo tintInfo = getTintInfo();
			tintInfo.discreteIndicatorTintList = TintManager.createSeekBarThumbTintColors(getContext(), tintColor);
			if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiDiscreteIndicatorTint)) {
				tintInfo.discreteIndicatorTintList = tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiDiscreteIndicatorTint);
			}
			tintInfo.discreteIndicatorTintMode = TintManager.parseTintMode(
					tintAttributes.getInt(R.styleable.Ui_SeekBar_uiDiscreteIndicatorTintMode, 0),
					PorterDuff.Mode.SRC_IN
			);
			if (UiConfig.MATERIALIZED) {
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiThumbTint)) {
					setThumbTintList(tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiThumbTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiProgressTint)) {
					setProgressTintList(tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiProgressTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiProgressBackgroundTint)) {
					setProgressBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiProgressBackgroundTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiBackgroundTint)) {
					setBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiBackgroundTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiThumbTintMode)) {
					setThumbTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_SeekBar_uiThumbTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiProgressTintMode)) {
					setProgressTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_SeekBar_uiProgressTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiProgressBackgroundTintMode)) {
					setProgressBackgroundTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_SeekBar_uiProgressBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_SeekBar_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				tintInfo.tintList = TintManager.createSeekBarThumbTintColors(getContext(), tintColor);
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiThumbTint)) {
					tintInfo.tintList = tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiThumbTint);
				}
				tintInfo.primaryProgressTintList = TintManager.createSeekBarProgressTintColors(getContext(), tintColor);
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiProgressTint)) {
					tintInfo.primaryProgressTintList = tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiProgressTint);
				}
				tintInfo.progressBackgroundTintList = TintManager.createSeekBarProgressBackgroundTintColors(getContext(), tintColor);
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiProgressBackgroundTint)) {
					tintInfo.progressBackgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiProgressBackgroundTint);
				}
				if (tintAttributes.hasValue(R.styleable.Ui_SeekBar_uiBackgroundTint)) {
					tintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_SeekBar_uiBackgroundTint);
				}
				tintInfo.tintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_SeekBar_uiThumbTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
				tintInfo.primaryProgressTintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_SeekBar_uiProgressTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
				tintInfo.progressBackgroundTintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_SeekBar_uiProgressBackgroundTintMode, 0),
						PorterDuff.Mode.SRC_IN
				);
				tintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_SeekBar_uiBackgroundTintMode, 0),
						tintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
				);
			}
		}

		/**
		 */
		@Override
		void onTintAttributesProcessed() {
			final SeekBarTintInfo tintInfo = getTintInfo();
			// If there is no tint modes specified within style/xml do not tint at all.
			if (tintInfo.primaryProgressTintMode == null) tintInfo.primaryProgressTintList = null;
			if (tintInfo.secondaryProgressTintMode == null)
				tintInfo.secondaryProgressTintList = null;
			if (tintInfo.progressBackgroundTintMode == null)
				tintInfo.progressBackgroundTintList = null;
			if (tintInfo.discreteIndicatorTintMode == null)
				tintInfo.discreteIndicatorTintList = null;
			tintInfo.hasPrimaryProgressTintList = tintInfo.primaryProgressTintList != null;
			tintInfo.hasPrimaryProgressTintMode = tintInfo.primaryProgressTintMode != null;
			tintInfo.hasSecondaryProgressTintList = tintInfo.secondaryProgressTintList != null;
			tintInfo.hasSecondaryProgressTintMode = tintInfo.secondaryProgressTintMode != null;
			tintInfo.hasProgressBackgroundTintList = tintInfo.progressBackgroundTintList != null;
			tintInfo.hasProgressBackgroundTintMode = tintInfo.progressBackgroundTintMode != null;
			tintInfo.hasDiscreteIndicatorTintList = tintInfo.discreteIndicatorTintList != null;
			tintInfo.hasDiscreteIndicatorTintMode = tintInfo.discreteIndicatorTintMode != null;
			super.onTintAttributesProcessed();
		}

		/**
		 */
		@Override
		boolean shouldInvalidateTintInfo(@NonNull BackgroundTintInfo tintInfo) {
			final SeekBarTintInfo info = (SeekBarTintInfo) tintInfo;
			return !info.hasPrimaryProgressTintList && !info.hasPrimaryProgressTintMode &&
					!info.hasSecondaryProgressTintList && !info.hasSecondaryProgressTintMode &&
					!info.hasProgressBackgroundTintList && !info.hasProgressBackgroundTintMode &&
					!info.hasDiscreteIndicatorTintList && !info.hasDiscreteIndicatorTintMode &&
					super.shouldInvalidateTintInfo(tintInfo);
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			SeekBarWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			SeekBarWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return SeekBarWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			SeekBarWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return SeekBarWidget.super.getBackgroundTintMode();
		}
	}
}
