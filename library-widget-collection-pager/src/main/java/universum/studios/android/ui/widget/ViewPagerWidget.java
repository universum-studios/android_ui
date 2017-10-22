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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.interpolator.ScrollerInterpolator;

/**
 * Extended version of {@link android.support.v4.view.ViewPager}. This updated ViewPager supports
 * tinting for the Android versions below {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
 * and other useful features described below including <b>pulling</b> feature.
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
 * See {@link ViewPager},
 * {@link R.styleable#Ui_ViewPager ViewPagerWidget Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiViewPagerStyle uiViewPagerStyle}
 *
 * @author Martin Albedinsky
 */
public class ViewPagerWidget extends ViewPager implements WidgetGroup {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "ViewPagerWidget";

	/**
	 * Flag indicating whether the swiping of pages on touch is enabled or not.
	 */
	private static final int PFLAG_PAGE_SWIPING_ENABLED = 0x00000001 << 16;

	/**
	 * Flag indicating whether the swiping of multiple pages on fling at once is enabled or not.
	 */
	private static final int PFLAG_PAGE_FLING_SWIPING_ENABLED = 0x00000001 << 17;

	/**
	 * Flag indicating whether scroll duration for pages scrolling should be computed as relative
	 * depends on the count of pages to be scrolled or just fixed.
	 */
	private static final int PFLAG_PAGE_SCROLL_RELATIVE_DURATION_ENABLED = 0x00000001 << 18;

	/**
	 * Name of the scroller field within the super ViewPager used to attach custom scroller to this
	 * ViewPager implementation.
	 */
	private static final String SCROLLER_FIELD_NAME = "mScroller";

	/*
	 * Static members ==============================================================================
	 */

	/**
	 * Interpolator used for scrolling operations of this view.
	 */
	private static final Interpolator SCROLLER_INTERPOLATOR = new ScrollerInterpolator();

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Decorator used to extend API of this widget group by functionality otherwise not supported or
	 * not available due to current API level.
	 */
	private Decorator mDecorator;

	/**
	 * Index of the currently selected page.
	 */
	private int mCurrentPage;

	/**
	 * Duration for transition used to scroll between pages. If <b>negative</b>, the default duration
	 * will be used.
	 */
	private int mPageScrollDuration = -1;

	/**
	 * Helper used to track velocity of fling to determine whether to initiate swipe of page or not.
	 */
	private VelocityTracker mVelocityTracker;

	/**
	 * Minimum sensitivity to initiate swipe of page on fling.
	 */
	private float mPageFlingSwipingSensitivity = 6000;

	/**
	 * Adapter with data set for this view pager.
	 */
	private PagerAdapter mAdapter;

	/**
	 * Custom scroller set to this view pager to provide custom scrolling logic.
	 */
	private Scroller mCustomScroller;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #ViewPagerWidget(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public ViewPagerWidget(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #ViewPagerWidget(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link R.attr#uiViewPagerStyle} as attribute for default style.
	 */
	public ViewPagerWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiViewPagerStyle);
	}

	/**
	 * Same as {@link #ViewPagerWidget(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public ViewPagerWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of ViewPagerWidget for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	public ViewPagerWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs);
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

		final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_ViewPager, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_ViewPager_android_background) {
					int resID = typedArray.getResourceId(index, -1);
					if (resID != -1) {
						setBackgroundResource(resID);
					} else {
						setBackgroundColor(typedArray.getColor(0, Color.TRANSPARENT));
					}
				} else if (index == R.styleable.Ui_ViewPager_uiPageMargin) {
					setPageMargin(typedArray.getDimensionPixelSize(index, 0));
				} else if (index == R.styleable.Ui_ViewPager_uiPageMarginDrawable) {
					setPageMarginDrawable(typedArray.getDrawable(index));
				} else if (index == R.styleable.Ui_ViewPager_uiPageSwipingEnabled) {
					mDecorator.updatePrivateFlags(PFLAG_PAGE_SWIPING_ENABLED, typedArray.getBoolean(index, true));
				} else if (index == R.styleable.Ui_ViewPager_uiPageFlingSwipingEnabled) {
					mDecorator.updatePrivateFlags(PFLAG_PAGE_FLING_SWIPING_ENABLED, typedArray.getBoolean(index, false));
				} else if (index == R.styleable.Ui_ViewPager_uiPageFlingSwipingSensitivity) {
					this.mPageFlingSwipingSensitivity = Math.max(0, typedArray.getFloat(index, mPageFlingSwipingSensitivity));
				} else if (index == R.styleable.Ui_ViewPager_uiCurrentPage) {
					this.mCurrentPage = typedArray.getInteger(index, 0);
				} else if (index == R.styleable.Ui_ViewPager_uiOffScreenPageLimit) {
					setOffscreenPageLimit(typedArray.getInt(index, getOffscreenPageLimit()));
				} else if (index == R.styleable.Ui_ViewPager_uiPageScrollDuration) {
					this.mPageScrollDuration = typedArray.getInteger(index, mPageScrollDuration);
				} else if (index == R.styleable.Ui_ViewPager_uiPageScrollRelativeDurationEnabled) {
					mDecorator.updatePrivateFlags(PFLAG_PAGE_SCROLL_RELATIVE_DURATION_ENABLED, typedArray.getBoolean(index, false));
				}
			}
			typedArray.recycle();
		}
		// Override default scroller so we can use custom durations for scroll if requested.
		setScroller(new WidgetScroller(context, SCROLLER_INTERPOLATOR));
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
		event.setClassName(ViewPagerWidget.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(ViewPagerWidget.class.getName());
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
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(mAdapter = adapter);
	}

	/**
	 */
	@Override
	public void setCurrentItem(int item) {
		if (mPageScrollDuration >= 0) {
			setCurrentItem(item, calculatePageScrollDuration(item));
			return;
		}
		this.resetPageScrollDuration();
		super.setCurrentItem(mCurrentPage = item);
	}

	/**
	 */
	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		if (mPageScrollDuration >= 0 && smoothScroll) {
			setCurrentItem(item, calculatePageScrollDuration(item));
			return;
		}
		super.setCurrentItem(mCurrentPage = item, smoothScroll);
	}

	/**
	 * Same as {@link #setCurrentItem(int)}, but this will use the specified <var>duration</var> for
	 * scroll. <b>Note</b>, that this duration will be used only for this particular call, if you
	 * want to use a specific duration for all {@link #setCurrentItem(int)} related calls, set your
	 * desired duration by {@link #setPageScrollDuration(int)}.
	 *
	 * @param item     A position of page to scroll to.
	 * @param duration The desired scroll duration used to scroll to the specified page in milliseconds.
	 */
	public void setCurrentItem(int item, int duration) {
		this.usePageScrollDuration(duration);
		super.setCurrentItem(mCurrentPage = item, duration > 0);
		this.resetPageScrollDuration();
	}

	/**
	 * Sets the scroller for this view pager used to apply and animate scroll to the pages of this
	 * pager.
	 *
	 * @param scroller The desired scroller to be used by this pager.
	 */
	public void setScroller(@NonNull Scroller scroller) {
		this.setSuperScroller(mCustomScroller = scroller);
	}

	/**
	 * Sets a custom scroller to the super of this view pager.
	 *
	 * @param scroller The scroller that should be set to the super ViewPager.
	 */
	private void setSuperScroller(Scroller scroller) {
		final Class parentClass = ViewPagerWidget.class.getSuperclass();
		try {
			final Field scrollerField = parentClass.getDeclaredField(SCROLLER_FIELD_NAME);
			scrollerField.setAccessible(true);
			scrollerField.set(this, scroller);
		} catch (Exception e) {
			Log.e(TAG, "Failed to attach custom scroller to the super ViewPager.", e);
		}
	}

	/**
	 * Sets a flag indicating whether the swiping of pages on a user's touch/drag is enabled or not.
	 * This enables/disables only interaction for the user, calls to {@link #setCurrentItem(int)} and
	 * similar methods will still be working.
	 *
	 * @param enabled {@code True} if page swiping should be enabled, {@code false} otherwise.
	 * @see R.attr#uiPageSwipingEnabled ui:uiPageSwipingEnabled
	 * @see #isPageFlingSwipingEnabled()
	 */
	public void setPageSwipingEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.updatePrivateFlags(PFLAG_PAGE_SWIPING_ENABLED, enabled);
	}

	/**
	 * Returns a flag indicating whether the swiping of pages is enabled for a user or not.
	 *
	 * @return {@code True} if page swiping is enabled, {@code false} otherwise.
	 * @see #setPageSwipingEnabled(boolean)
	 */
	public boolean isPageSwipingEnabled() {
		this.ensureDecorator();
		return mDecorator.hasPrivateFlag(PFLAG_PAGE_SWIPING_ENABLED);
	}

	/**
	 * Sets a flag indicating whether the swiping of multiple pages on a user's fling is enabled or
	 * not. Enabling this feature means, that the user can by fling scroll multiple pages at once.
	 * How many pages will be scrolled at fling depends on the velocity of a specific fling. Sensitivity
	 * for the fling can be set by {@link #setPageFlingSwipingSensitivity(float)}.
	 *
	 * @param enabled {@code True} if page swiping on fling should be enabled, {@code false} otherwise.
	 * @see R.attr#uiPageFlingSwipingEnabled ui:uiPageFlingSwipingEnabled
	 * @see #isPageFlingSwipingEnabled()
	 */
	public void setPageFlingSwipingEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.updatePrivateFlags(PFLAG_PAGE_FLING_SWIPING_ENABLED, enabled);
	}

	/**
	 * Returns a flag indicating whether the swiping of multiple pages on fling is enabled or not.
	 *
	 * @return {@code True} if page swiping on fling is enabled, {@code false} otherwise.
	 * @see #setPageFlingSwipingEnabled(boolean)
	 */
	public boolean isPageFlingSwipingEnabled() {
		this.ensureDecorator();
		return mDecorator.hasPrivateFlag(PFLAG_PAGE_FLING_SWIPING_ENABLED);
	}

	/**
	 * Sets a sensitivity for the velocity of a user's fling used to determine whether to handle a
	 * particular fling to swipe multiple pages at once or not.
	 * <p>
	 * <b>Note</b>, that velocity of fling is computed in {@link UiConfig#VELOCITY_UNITS}.
	 *
	 * @param sensitivity The desired sensitivity. The bigger sensitivity is, the less pages will
	 *                    be scrolled at once and in reverse.
	 * @see R.attr#uiPageFlingSwipingSensitivity ui:uiPageFlingSwipingSensitivity
	 * @see #getPageFlingSwipingSensitivity()
	 * @see #setPageFlingSwipingEnabled(boolean)
	 */
	public void setPageFlingSwipingSensitivity(float sensitivity) {
		this.mPageFlingSwipingSensitivity = Math.max(0, sensitivity);
	}

	/**
	 * Returns the current sensitivity for the fling velocity used to determine whether to handle
	 * a user's fling or not.
	 * <p>
	 * Default value: <b>6000</b>
	 *
	 * @return Swiping sensitivity.
	 * @see #setPageFlingSwipingSensitivity(float)
	 */
	public float getPageFlingSwipingSensitivity() {
		return mPageFlingSwipingSensitivity;
	}

	/**
	 * Sets the duration for scroll used whenever {@link #setCurrentItem(int)} or similar methods
	 * are called upon this view pager.
	 *
	 * @param duration The desired scroll duration in milliseconds. Pass here {@code -1} to clear
	 *                 the current one, so the default one will be used.
	 * @see #getPageScrollDuration()
	 */
	public void setPageScrollDuration(int duration) {
		this.mPageScrollDuration = duration;
	}

	/**
	 * Returns the current duration used for scrolling of pages of this pager.
	 *
	 * @return Scroll duration in milliseconds.
	 * @see #setPageScrollDuration(int)
	 */
	public int getPageScrollDuration() {
		return mPageScrollDuration;
	}

	/**
	 * Sets a flag indicating whether the duration of page scroll should be computed as a relative
	 * one depends on the count of pages to be scrolled, or as a fixed one regardless how many pages
	 * will be scrolled.
	 * <p>
	 * If the relative duration is enabled, the scroll duration requested by {@link #setPageScrollDuration(int)}
	 * is used as a base, so a duration for the current page scroll is computed like so:
	 * <b>{@code Math.abs(getCurrentItem() - position) * pageScrollDuration}</b>
	 *
	 * @param enabled {@code True} if relative duration of page scroll should be enabled, {@code false}
	 *                otherwise.
	 * @see #isPageScrollRelativeDurationEnabled()
	 */
	public void setPageScrollRelativeDurationEnabled(boolean enabled) {
		this.ensureDecorator();
		mDecorator.updatePrivateFlags(PFLAG_PAGE_SCROLL_RELATIVE_DURATION_ENABLED, enabled);
	}

	/**
	 * Returns a flag indicating whether the duration of pages scroll is computed as a relative one
	 * or not.
	 *
	 * @return {@code True} if duration of page scroll is computed as relative one, {@code false} as
	 * fixed one.
	 * @see #setPageScrollRelativeDurationEnabled(boolean)
	 */
	public boolean isPageScrollRelativeDurationEnabled() {
		this.ensureDecorator();
		return mDecorator.hasPrivateFlag(PFLAG_PAGE_SCROLL_RELATIVE_DURATION_ENABLED);
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
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.ensureDecorator();
		mDecorator.onAttachedToWindow();
		setCurrentItem(mCurrentPage);
	}

	/**
	 */
	@Override
	public boolean isAttachedToWindow() {
		this.ensureDecorator();
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && super.isAttachedToWindow()) ||
				mDecorator.hasPrivateFlag(PrivateFlags.PFLAG_ATTACHED_TO_WINDOW);
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
		if (!mDecorator.hasPrivateFlag(PFLAG_PAGE_SWIPING_ENABLED)) {
			final MotionEvent cancelEvent = WidgetUtils.createMotionCancelingEvent(event);
			super.onInterceptTouchEvent(cancelEvent);
			cancelEvent.recycle();
			return false;
		}
		if (mDecorator.onInterceptTouchEvent(event)) {
			final MotionEvent cancelEvent = WidgetUtils.createMotionCancelingEvent(event);
			super.onInterceptTouchEvent(cancelEvent);
			cancelEvent.recycle();
			this.requestParentDisallowInterceptTouchEvent(true);
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
		if (!mDecorator.hasPrivateFlag(PFLAG_PAGE_SWIPING_ENABLED)) {
			mDecorator.hideSoftKeyboardOnTouch();
			return false;
		}
		if (mDecorator.onTouchEvent(event)) {
			this.requestParentDisallowInterceptTouchEvent(true);
			return true;
		}
		if (mDecorator.hasPrivateFlag(PFLAG_PAGE_FLING_SWIPING_ENABLED)) {
			this.ensureVelocityTracker();
			mVelocityTracker.addMovement(event);
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					mVelocityTracker.computeCurrentVelocity(UiConfig.VELOCITY_UNITS);
					final float xVelocity = mVelocityTracker.getXVelocity();
					if (Math.abs(xVelocity) > mPageFlingSwipingSensitivity) {
						super.onTouchEvent(event);
						this.handleFling(xVelocity);
						return true;
					}
			}
		}
		if (super.onTouchEvent(event)) {
			return true;
		}
		mDecorator.hideSoftKeyboardOnTouch();
		return false;
	}

	/**
	 * Ensures that the {@link #mVelocityTracker} is initialized.
	 */
	private void ensureVelocityTracker() {
		if (mVelocityTracker == null) this.mVelocityTracker = VelocityTracker.obtain();
	}

	/**
	 * Handles fling event performed by a user with the specified <var>velocity</var>. This will
	 * compute how many pages should be scrolled and than will call {@link #setCurrentItem(int)}
	 * for the computed page position.
	 *
	 * @param velocity The velocity with which has been fling performed.
	 */
	private void handleFling(float velocity) {
		if (mAdapter == null) {
			return;
		}
		int scrollPages = Math.round(Math.abs(velocity) / mPageFlingSwipingSensitivity);
		if (velocity > 0) {
			setCurrentItem(Math.max(0, getCurrentItem() - scrollPages));
		} else {
			setCurrentItem(Math.min(mAdapter.getCount() - 1, getCurrentItem() + scrollPages));
		}
	}

	/**
	 * Updates the scroll duration for the current scroller (if instance of {@link WidgetScroller}),
	 * so this duration will be used when {@link WidgetScroller#startScroll(int, int, int, int, int)}
	 * will be next time called upon this scroller.
	 *
	 * @param duration The desired scroll duration in milliseconds. Pass here {@code -1} to clear
	 *                 the current one, so the default one will be used.
	 */
	private void usePageScrollDuration(int duration) {
		if (mCustomScroller instanceof WidgetScroller) {
			((WidgetScroller) mCustomScroller).setScrollDuration(duration);
		}
	}

	/**
	 * Resets the current scroll duration for pages.
	 */
	private void resetPageScrollDuration() {
		usePageScrollDuration(-1);
	}

	/**
	 * Calculates scroll duration for page at the specified <var>position</var>. The calculated
	 * duration will depends on if the relative duration is enabled or not via
	 * {@link #setPageScrollRelativeDurationEnabled(boolean)}.
	 *
	 * @param position The position of page for which to calculate scroll duration.
	 * @return Calculated scroll duration in milliseconds.
	 */
	private int calculatePageScrollDuration(int position) {
		this.ensureDecorator();
		if (mDecorator.hasPrivateFlag(PFLAG_PAGE_SCROLL_RELATIVE_DURATION_ENABLED)) {
			return Math.abs(getCurrentItem() - position) * mPageScrollDuration;
		}
		return mPageScrollDuration;
	}

	/**
	 * Requests the current parent to disallow intercepting of touch event by {@link ViewParent#requestDisallowInterceptTouchEvent(boolean)}.
	 *
	 * @param disallow {@code True} to disallow, {@code false} otherwise.
	 */
	private void requestParentDisallowInterceptTouchEvent(boolean disallow) {
		final ViewParent parent = getParent();
		if (parent != null) {
			parent.requestDisallowInterceptTouchEvent(disallow);
		}
	}

	/**
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		this.ensureDecorator();
		mDecorator.onDetachedFromWindow();
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * Decorator implementation for this widget group.
	 */
	private final class Decorator extends WidgetGroupDecorator<ViewPagerWidget> {

		/**
		 * See {@link WidgetGroupDecorator#WidgetGroupDecorator(ViewGroup)}.
		 */
		Decorator(ViewPagerWidget widgetGroup) {
			super(widgetGroup, R.styleable.Ui_ViewPager);
			updatePrivateFlags(PFLAG_PAGE_SWIPING_ENABLED, true);
		}

		/**
		 */
		@Override
		@SuppressWarnings("ResourceType")
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			if (UiConfig.MATERIALIZED) {
				if (tintAttributes.hasValue(R.styleable.Ui_ViewPager_uiBackgroundTint)) {
					setBackgroundTintList(tintAttributes.getColorStateList(R.styleable.Ui_ViewPager_uiBackgroundTint));
				}
				if (tintAttributes.hasValue(R.styleable.Ui_ViewPager_uiBackgroundTintMode)) {
					setBackgroundTintMode(TintManager.parseTintMode(
							tintAttributes.getInt(R.styleable.Ui_ViewPager_uiBackgroundTintMode, 0),
							PorterDuff.Mode.SRC_IN
					));
				}
			} else {
				if (tintAttributes.hasValue(R.styleable.Ui_ViewPager_uiBackgroundTint)) {
					mTintInfo.backgroundTintList = tintAttributes.getColorStateList(R.styleable.Ui_ViewPager_uiBackgroundTint);
				}
				mTintInfo.backgroundTintMode = TintManager.parseTintMode(
						tintAttributes.getInt(R.styleable.Ui_ViewPager_uiBackgroundTintMode, 0),
						mTintInfo.backgroundTintList != null ? PorterDuff.Mode.SRC_IN : null
				);
			}
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			ViewPagerWidget.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			ViewPagerWidget.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return ViewPagerWidget.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			ViewPagerWidget.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return ViewPagerWidget.super.getBackgroundTintMode();
		}
	}
}
