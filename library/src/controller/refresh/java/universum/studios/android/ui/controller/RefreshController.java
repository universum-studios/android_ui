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
package universum.studios.android.ui.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.CircularProgressDrawable;
import universum.studios.android.ui.graphics.drawable.ProgressDrawable;
import universum.studios.android.ui.graphics.drawable.RefreshDrawable;
import universum.studios.android.ui.util.ResourceUtils;
import universum.studios.android.ui.widget.Pullable;
import universum.studios.android.ui.widget.Refreshable;
import universum.studios.android.ui.widget.Scrollable;

import java.util.LinkedList;

/**
 * RefreshController can be used to support <b>refresh</b> feature for views with <b>refreshable</b>
 * data set like {@link android.widget.ListView} or {@link android.widget.GridView}. A view that want
 * to provide refresh feature need to implement required {@link Scrollable} interface that specifies
 * simple API needed for this controller to properly handle this feature.
 * <p>
 * RefreshController uses {@link RefreshDrawable} as refresh indicator that is drawn near the top
 * edge of the refreshable view. To show/hide the indicator a user needs to pull down/up from/to the
 * view's top edge. Refresh callback will be fired after the indicator reaches a specific position
 * that can be specified via {@link #setRefreshPosition(float)}. There are a lot of properties that
 * can be set to customize the refresh behaviour, for example how much can be the  refresh indicator
 * pulled ({@link #setRefreshDistance(float)}) or the refresh indicator's transition
 * ({@link #setRefreshIndicatorTransition(int)}) determining how is the indicator transitioned to be
 * showed/hided during pull.
 * <p>
 * To receive callback about the initiated refresh, a watcher needs to register {@link Refreshable.OnRefreshListener}
 * listener of which {@link Refreshable.OnRefreshListener#onRefresh(Refreshable)} callback will be
 * fired whenever a user releases the refresh indicator at or after the refresh position. When there
 * is refreshing process finished, {@link RefreshController#setRefreshing(boolean)} need to be called
 * upon an instance of this controller class with {@code false} to dismiss the refresh indicator. This
 * method can be used also to show (pop) the refresh indicator for example, when the user clicks on
 * a refresh button.
 *
 * <h3>XML attributes</h3>
 * RefreshController is meant to be used within views so it also supports set up from {@link AttributeSet}
 * via {@link #setUpFromAttrs(Context, AttributeSet, int, int)} method that should be called from
 * within a constructor of the refreshable view that uses this controller to provide the refresh
 * feature. See {@link R.styleable#Ui_RefreshController RefreshController Attributes}.
 *
 * <h3>XML attributes for refresh indicator</h3>
 * Refresh indicator drawable can be also styled by referencing the desired style via
 * {@link R.attr#uiRefreshIndicatorStyle uiRefreshIndicatorStyle} attribute.
 * See {@link R.styleable#Ui_RefreshIndicator RefreshIndicator Attributes}.
 * Also, the refresh indicator can be accessed via {@link #getRefreshIndicator()} method for styling
 * through Java code.
 *
 * @param <V> Type of the refreshable view.
 * @author Martin Albedinsky
 */
public class RefreshController<V extends View & Refreshable> extends BasePullController<V> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "RefreshController";

	/**
	 * Default refresh position. See {@link #mRefreshPosition}
	 */
	private static final float REFRESH_POINT = 0.6f;

	/**
	 * Radius of the refresh (circular) drawable in <b>dp</b> units also with arrow size taken into
	 * count.
	 */
	private static final int REFRESH_INDICATOR_RADIUS = 17;

	/**
	 * Thickness of the refresh (circular) drawable in <b>dp</b> units.
	 */
	private static final int REFRESH_INDICATOR_THICKNESS = 3;

	/**
	 * Maximum progress to be set to indicator during pull.
	 */
	private static final int MAX_INDICATOR_PULL_PROGRESS = 80;

	/**
	 * Maximum rotation which can be applied to indicator during pull.
	 */
	private static final float MAX_INDICATOR_PULL_ROTATION = 360f;

	/**
	 * Flag indicating whether the view using this helper is attached to window or not.
	 */
	private static final int PFLAG_ATTACHED_TO_WINDOW = 0x00000001;

	/**
	 * Flag indicating whether the view using this helper is being pulled or not.
	 */
	private static final int PFLAG_PULLING = 0x00000002;

	/**
	 * Flag indicating whether the refresh process is running or not.
	 */
	private static final int PFLAG_REFRESHING = 0x00000004;

	/**
	 * Flag indicating whether there was started animation to pop indicator or not.
	 */
	private static final int PFLAG_POPPING_INDICATOR = 0x00000008;

	/**
	 * Flag indicating whether there was started animation to dismiss indicator or not.
	 */
	private static final int PFLAG_DISMISSING_INDICATOR = 0x00000010;

	/**
	 * Flag indicating whether there is running some of the scheduled animations or not.
	 */
	private static final int PFLAG_RUNNING_SCHEDULED_ANIMATION = 0x00000020;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Runnable action used to schedule refreshable view's invalidation.
	 */
	private final Runnable INVALIDATE_VIEW = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			mView.invalidate();
		}
	};

	/**
	 * Runnable action used to schedule refreshable view's unfrozing.
	 */
	private final Runnable UNFROZE_VIEW = new Runnable() {
		@Override
		public void run() {
			setViewFrozen(false);
		}
	};

	/**
	 * Runnable action used to schedule refresh indicator's pop animation.
	 */
	private final Runnable POP_INDICATOR = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			popIndicator();
			updatePrivateFlags(PFLAG_REFRESHING, true);
		}
	};

	/**
	 * Runnable action used to schedule refresh indicator's dismiss animation.
	 */
	private final Runnable DISMISS_INDICATOR = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			dismissIndicator();
			updatePrivateFlags(PFLAG_REFRESHING, false);
		}
	};

	/**
	 * Object holding data about the refresh indicator.
	 */
	private final IndicatorInfo INDICATOR_INFO = new IndicatorInfo();

	/**
	 * Drawable used to draw refresh indicator.
	 */
	private final RefreshDrawable mIndicator;

	/**
	 * Flag determining type of the transition used to show/hide the indicator using pull.
	 */
	private int mIndicatorTransition = Refreshable.TRANSITION_BELOW;

	/**
	 * Offset for the originating position of the refresh indicator.
	 */
	private int mIndicatorOffset;

	/**
	 * Animations helper used to run animations upon this controller regardless current Android version.
	 */
	private final Animations mAnimations;

	/**
	 * Dimension of the refreshable view.
	 */
	private int mViewWidth, mViewHeight;

	/**
	 * Callback to be invoked whenever the refresh has been initiated.
	 */
	private Refreshable.OnRefreshListener mRefreshListener;

	/**
	 * Position determining the boundary for refresh process to be initiated after a user releases the
	 * indicator.
	 * <p>
	 * Default value: <b>{@link #REFRESH_POINT}</b>
	 */
	private float mRefreshPosition = REFRESH_POINT;

	/**
	 * Position determining the boundary where should be the indicator settled after the refresh process
	 * has been initiated. This position is also used when popping the indicator by {@link #popIndicator()}
	 * method.
	 * <p>
	 * Default value: <b>{@link #mRefreshPosition}</b>
	 */
	private float mRefreshSettlePosition = mRefreshPosition;

	/**
	 * Time of the last invalidation of the refreshable view.
	 */
	private long mLastViewInvalidationTime = SystemClock.uptimeMillis();

	/**
	 * Queue containing a set of runnables for the scheduled animations. This queue is used for animations
	 * that should be played one after another not at the same time.
	 */
	private LinkedList<Runnable> mScheduledAnimations;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of RefreshHelper to handle refreshing of the specified <var>view</var>.
	 * <p>
	 * <b>Note</b>, that the new RefreshHelper instance will register self as {@link Pullable.OnPullListener}
	 * into the PullController of the given view, so it will receive pull callbacks necessary to handle
	 * refresh feature.
	 *
	 * @param view The view of which refresh to handle by the new RefreshHelper.
	 */
	@SuppressWarnings("NewApi")
	public RefreshController(@NonNull V view) {
		super(view);
		// Use software layer that is required for proper drawing work of refresh drawable.
		if (ProgressDrawable.REQUIRES_SOFTWARE_LAYER) {
			view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		this.mAnimations = Animations.get(this);

		/**
		 * Initialize refresh indicator drawable.
		 */
		final Resources resources = view.getResources();
		final float density = resources.getDisplayMetrics().density;
		this.mIndicator = new RefreshDrawable();
		mIndicator.setProgressRadius(Math.round(density * REFRESH_INDICATOR_RADIUS));
		mIndicator.setProgressThickness(Math.round(density * REFRESH_INDICATOR_THICKNESS));
		mIndicator.setBackground(ResourceUtils.getDrawable(resources, R.drawable.ui_sdw_drop_refresh_indicator, view.getContext().getTheme()));
		if (!view.isInEditMode()) {
			mIndicator.setProgressColors(resources.getIntArray(R.array.ui_colors_indeterminate_progress));
		}
		mIndicator.setProgressMultiColored(true);
		mIndicator.setVisible(false, false);
		mIndicator.setCallback(view);
		final int indicatorHeight = mIndicator.getIntrinsicHeight();
		mIndicator.setBounds(0, 0, mIndicator.getIntrinsicWidth(), indicatorHeight);

		this.handleViewSizeChanged(view.getWidth(), view.getHeight());
		this.resetIndicator(false);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Performs set up of this controller from the given <var>attrs</var>.
	 *
	 * @param context      Context used to obtain values for the given attributes.
	 * @param attrs        Set of attributes with values to be used to set up this controller.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource, for
	 *                     the view attached to this controller, within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the view attached to this controller.
	 * @return {@code True} if some setting of this controller has been changed, {@code false} otherwise.
	 */
	@SuppressWarnings("ResourceType")
	public boolean setUpFromAttrs(@NonNull Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_RefreshController, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_RefreshController_uiRefreshDistance) {
					setRefreshDistance(typedArray.getDimensionPixelSize(index, (int) mPullDistance));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshDistanceFraction) {
					setRefreshDistanceFraction(typedArray.getFloat(index, mPullDistanceFraction));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshPosition) {
					setRefreshPosition(typedArray.getFloat(index, mRefreshPosition));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshSettlePosition) {
					setRefreshSettlePosition(typedArray.getFloat(index, mRefreshSettlePosition));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshSettleDuration) {
					setRefreshSettleDuration(typedArray.getInt(index, (int) mAnimations.refreshSettleDuration));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshGestureInterpolator) {
					setRefreshGestureInterpolator(AnimationUtils.loadInterpolator(context, typedArray.getResourceId(index, 0)));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshIndicatorTransition) {
					setRefreshIndicatorTransition(typedArray.getInteger(index, mIndicatorTransition));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshIndicatorHideDuration) {
					setRefreshIndicatorHideDuration(typedArray.getInt(index, (int) mAnimations.indicatorHideDuration));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshIndicatorPopDuration) {
					setRefreshIndicatorPopDuration(typedArray.getInt(index, (int) mAnimations.indicatorPopDuration));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshIndicatorDismissDuration) {
					setRefreshIndicatorDismissDuration(typedArray.getInt(index, (int) mAnimations.indicatorDismissDuration));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshIndicatorArrowScaleDuration) {
					setRefreshIndicatorArrowScaleDuration(typedArray.getInt(index, (int) mAnimations.indicatorArrowScaleDuration));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshIndicatorOffset) {
					setRefreshIndicatorOffset(typedArray.getDimensionPixelSize(index, mIndicatorOffset));
				} else if (index == R.styleable.Ui_RefreshController_uiRefreshIndicatorStyle) {
					final int styleRes = typedArray.getResourceId(index, -1);
					if (styleRes != -1) {
						final TypedArray indArray = context.obtainStyledAttributes(styleRes, R.styleable.Ui_RefreshIndicator);
						if (indArray != null) {
							final int indN = indArray.getIndexCount();
							for (int j = 0; j < indN; j++) {
								final int indIndex = indArray.getIndex(j);
								if (indIndex == R.styleable.Ui_RefreshIndicator_uiRefreshColor) {
									mIndicator.setProgressColor(indArray.getInt(index, mIndicator.getProgressColor()));
								} else if (indIndex == R.styleable.Ui_RefreshIndicator_uiRefreshColors) {
									final int colorsRes = indArray.getResourceId(index, -1);
									if (colorsRes != -1) {
										mIndicator.setProgressColors(context.getResources().getIntArray(colorsRes));
									}
								} else if (indIndex == R.styleable.Ui_RefreshIndicator_uiRefreshMultiColored) {
									mIndicator.setProgressMultiColored(indArray.getBoolean(index, mIndicator.isProgressMultiColored()));
								} else if (indIndex == R.styleable.Ui_RefreshIndicator_uiRefreshIndeterminateSpeed) {
									mIndicator.setProgressIndeterminateSpeed(indArray.getFloat(index, mIndicator.getProgressIndeterminateSpeed()));
								} else if (indIndex == R.styleable.Ui_RefreshIndicator_android_thickness) {
									mIndicator.setProgressThickness(indArray.getDimensionPixelSize(index, (int) mIndicator.getProgressThickness()));
								}
							}
							indArray.recycle();
						}
					}
				}
			}
			typedArray.recycle();
			this.resetIndicator(false);
			return n > 0;
		}
		return false;
	}

	/**
	 * Sets the transition type determining how to transition the refresh indicator when it is being
	 * pulled down/up by a user.
	 * <p>
	 * <b>Note</b>, that {@link Refreshable#TRANSITION_COPLANAR} transition will for now behave like
	 * {@link Refreshable#TRANSITION_ABOVE}. This is scheduled to be changed in the feature.
	 *
	 * @param transition The desired indicator's transition. One of {@link Refreshable#TRANSITION_BELOW TRANSITION_BELOW},
	 *                   {@link Refreshable#TRANSITION_COPLANAR TRANSITION_COPLANAR} or {@link Refreshable#TRANSITION_ABOVE TRANSITION_ABOVE}.
	 * @see R.attr#uiRefreshIndicatorTransition ui:uiRefreshIndicatorTransition
	 * @see #getRefreshIndicatorTransition()
	 */
	public void setRefreshIndicatorTransition(@Refreshable.IndicatorTransition int transition) {
		this.mIndicatorTransition = transition;
	}

	/**
	 * Returns the current transition type of the refresh indicator.
	 * <p>
	 * Default value: <b>{@link Refreshable#TRANSITION_BELOW}</b>
	 *
	 * @return One of {@link Refreshable#TRANSITION_BELOW TRANSITION_BELOW}, {@link Refreshable#TRANSITION_COPLANAR TRANSITION_COPLANAR}
	 * or {@link Refreshable#TRANSITION_ABOVE TRANSITION_ABOVE}.
	 * @see #setRefreshIndicatorTransition(int)
	 */
	@Refreshable.IndicatorTransition
	public int getRefreshIndicatorTransition() {
		return mIndicatorTransition;
	}

	/**
	 * Sets the duration for the refresh indicator's hide animation.
	 * <p>
	 * The hide animation is played whenever user releases the indicator and its current position is
	 * before the {@link #getRefreshPosition()}.
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see R.attr#uiRefreshIndicatorHideDuration ui:uiRefreshIndicatorHideDuration
	 * @see #getRefreshIndicatorHideDuration()
	 */
	public void setRefreshIndicatorHideDuration(long duration) {
		mAnimations.indicatorHideDuration = Math.max(0, duration);
	}

	/**
	 * Returns the duration used for the refresh indicator's hide animation.
	 * <p>
	 * Default value: <b>300 ms</b>
	 *
	 * @return Duration in milliseconds.
	 * @see #setRefreshIndicatorHideDuration(long)
	 */
	public long getRefreshIndicatorHideDuration() {
		return mAnimations.indicatorPopDuration;
	}

	/**
	 * Sets the duration for the refresh indicator's pop animation.
	 * <p>
	 * The pop animation is played whenever {@link #setRefreshing(boolean)} is called with {@code true}.
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see R.attr#uiRefreshIndicatorPopDuration ui:uiRefreshIndicatorPopDuration
	 * @see #getRefreshIndicatorPopDuration()
	 */
	public void setRefreshIndicatorPopDuration(long duration) {
		mAnimations.indicatorPopDuration = Math.max(0, duration);
	}

	/**
	 * Returns the duration used for the refresh indicator's pop animation.
	 * <p>
	 * Default value: <b>300 ms</b>
	 *
	 * @return Duration in milliseconds.
	 * @see #setRefreshIndicatorPopDuration(long)
	 */
	public long getRefreshIndicatorPopDuration() {
		return mAnimations.indicatorPopDuration;
	}

	/**
	 * Sets the duration for the refresh indicator's dismiss animation.
	 * <p>
	 * The dismiss animation is played whenever {@link #setRefreshing(boolean)} is called with
	 * {@code false}.
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see R.attr#uiRefreshIndicatorDismissDuration ui:uiRefreshIndicatorDismissDuration
	 * @see #getRefreshIndicatorDismissDuration()
	 */
	public void setRefreshIndicatorDismissDuration(long duration) {
		mAnimations.indicatorDismissDuration = Math.max(0, duration);
	}

	/**
	 * Returns the duration used for the refresh indicator's dismiss animation.
	 * <p>
	 * Default value: <b>300 ms</b>
	 *
	 * @return Duration in milliseconds.
	 * @see #setRefreshIndicatorDismissDuration(long)
	 */
	public long getRefreshIndicatorDismissDuration() {
		return mAnimations.indicatorDismissDuration;
	}

	/**
	 * Sets the duration for the refresh indicator's arrow scale down animation.
	 * <p>
	 * The arrow's scale down animation is played whenever the refresh process is initiated by releasing
	 * of the indicator when mode of the refresh drawable is changed to <b>indeterminate</b> so the
	 * arrow needs to be hided.
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see R.attr#uiRefreshIndicatorArrowScaleDuration ui:uiRefreshIndicatorArrowScaleDuration
	 * @see #getRefreshIndicatorArrowScaleDuration()
	 */
	public void setRefreshIndicatorArrowScaleDuration(long duration) {
		mAnimations.indicatorArrowScaleDuration = Math.max(0, duration);
	}

	/**
	 * Returns the duration used for the refresh indicator's arrow scale animation.
	 * <p>
	 * Default value: <b>1000 ms</b>
	 *
	 * @return Duration in milliseconds.
	 * @see #setRefreshIndicatorArrowScaleDuration(long)
	 */
	public long getRefreshIndicatorArrowScaleDuration() {
		return mAnimations.indicatorArrowScaleDuration;
	}

	/**
	 * Sets an offset for the originating position of the refresh indicator. This offset determines
	 * where should be the indicator positioned when a user starts the drag gesture to pull it down
	 * to initiate refresh.
	 * <p>
	 * This can be used for example in case of {@link Refreshable#TRANSITION_ABOVE} indicator's transition
	 * mode to move the originating position to the bottom if the refreshable view is offset from the
	 * top due to its layout margin or due to another view.
	 *
	 * @param offset The desired offset in pixels. Positive value will move the originating position
	 *               to the bottom, negative value to the top.
	 * @see R.attr#uiRefreshIndicatorOffset ui:uiRefreshIndicatorOffset
	 * @see #getRefreshIndicatorOffset()
	 * @see #setRefreshIndicatorTransition(int)
	 */
	public void setRefreshIndicatorOffset(@Px int offset) {
		this.mIndicatorOffset = offset;
	}

	/**
	 * Returns the offset of the refresh indicator's originating position.
	 *
	 * @return Indicator's originating offset in pixels.
	 * @see #setRefreshIndicatorOffset(int)
	 */
	@Px
	public int getRefreshIndicatorOffset() {
		return mIndicatorOffset;
	}

	/**
	 * Sets a position determining the boundary for refresh process to be initiated after a user releases
	 * the refresh indicator.
	 * <p>
	 * The specified position is checked during the pull against the current position of the pull to
	 * resolve whether to initiate refresh or not.
	 *
	 * @param position The desired refresh position from the range {@code [0, 1]}.
	 * @see R.attr#uiRefreshPosition ui:uiRefreshPosition
	 * @see #getRefreshPosition()
	 */
	public void setRefreshPosition(@FloatRange(from = 0, to = 1) float position) {
		if (position >= 0 && position <= 1) this.mRefreshPosition = position;
	}

	/**
	 * Returns the current refresh position.
	 *
	 * @return Position from the range {@code [0, 1]}.
	 * @see #setRefreshPosition(float)
	 */
	@FloatRange(from = 0, to = 1)
	public float getRefreshPosition() {
		return mRefreshPosition;
	}

	/**
	 * Sets a position determining the boundary for the refresh indicator to be settled on after a
	 * user releases it. This position needs to be smaller or equal than a refresh position specified
	 * by {@link #setRefreshPosition(float)}.
	 * <p>
	 * The specified position is used when preparing the settle animation to determine how much needs
	 * to be the refresh indicator moved back from its current (released) pull position.
	 *
	 * @param settlePosition The desired refresh settle position from the range {@code [0, refreshPosition]}.
	 * @see R.attr#uiRefreshSettlePosition ui:uiRefreshSettlePosition
	 * @see #getRefreshSettlePosition()
	 */
	public void setRefreshSettlePosition(float settlePosition) {
		if (settlePosition >= 0 && settlePosition <= 1 && settlePosition <= mRefreshPosition) {
			this.mRefreshSettlePosition = settlePosition;
		}
	}

	/**
	 * Returns the current refresh settle position.
	 *
	 * @return Position from the range {@code [0, refreshPosition]}.
	 * @see #setRefreshSettlePosition(float)
	 */
	public float getRefreshSettlePosition() {
		return mRefreshSettlePosition;
	}

	/**
	 * Sets the duration for the refresh indicator's settle animation.
	 * <p>
	 * The settle animation is played whenever a user releases the indicator and its current position
	 * is after the <b>settle position</b> specified by {@link #setRefreshSettlePosition(float)}.
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see R.attr#uiRefreshSettleDuration ui:uiRefreshSettleDuration
	 * @see #getRefreshSettleDuration()
	 */
	public void setRefreshSettleDuration(int duration) {
		mAnimations.refreshSettleDuration = Math.max(0, duration);
	}

	/**
	 * Returns the duration for the refresh indicator's settle animation.
	 * <p>
	 * Default value: <b>75 ms</b>
	 *
	 * @return Duration in milliseconds.
	 * @see #setRefreshSettleDuration(int)
	 */
	public long getRefreshSettleDuration() {
		return mAnimations.refreshSettleDuration;
	}

	/**
	 * Sets the distance which determines how much can be the the refresh indicator pulled.
	 *
	 * @param distance The desired value in pixels to determine maximum refresh distance.
	 * @see R.attr#uiRefreshDistance ui:uiRefreshDistance
	 */
	public void setRefreshDistance(float distance) {
		setPullDistance(distance);
	}

	/**
	 * Returns the value of the maximum refresh distance.
	 * <p>
	 * Default value: <b>{@link #getPullDistance()}</b>
	 *
	 * @return Maximum refresh distance in pixels or {@code -1} if the refresh distance cannot be
	 * right now  resolved due to not initialized size of the attached refreshable view yet.
	 */
	public float getRefreshDistance() {
		return getPullDistance();
	}

	/**
	 * Sets the fraction for the maximum refresh distance which determines how much can be the refresh
	 * indicator pulled. The specified fraction will be used to compute maximum refresh distance from
	 * the size of the refreshable view.
	 *
	 * @param fraction The desired fraction from the range {@code [0, 1f]}.
	 * @see #setRefreshDistance(float)
	 */
	public void setRefreshDistanceFraction(@FloatRange(from = 0, to = 1) float fraction) {
		setPullDistanceFraction(fraction);
	}

	/**
	 * Returns the fraction used to compute maximum refresh distance.
	 * <p>
	 * Default value: <b>0</b>
	 *
	 * @return Refresh fraction from the range {@code [0, 1f]}.
	 */
	@FloatRange(from = 0, to = 1)
	public float getRefreshDistanceFraction() {
		return getPullDistanceFraction();
	}

	/**
	 * Sets an interpolator that is used when interpolating pull value during refresh gesture.
	 *
	 * @param interpolator The desired interpolator.
	 * @see R.attr#uiRefreshGestureInterpolator ui:uiRefreshGestureInterpolator
	 * @see #setPullTransformerInterpolator(Interpolator)
	 */
	public void setRefreshGestureInterpolator(@NonNull Interpolator interpolator) {
		setPullTransformerInterpolator(interpolator);
	}

	/**
	 * Registers a callback to be invoked whenever a user releases the refresh indicator at or after
	 * the refresh position specified by {@link #setRefreshPosition(float)}.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	public void setOnRefreshListener(@Nullable Refreshable.OnRefreshListener listener) {
		this.mRefreshListener = listener;
	}

	/**
	 * Called from the view which uses this helper to provide refresh feature to dispatch that this
	 * view has been attached to window.
	 * <p>
	 * This should be called whenever {@link View#onAttachedToWindow()} is invoked
	 * within the view.
	 */
	public void dispatchViewAttachedToWindow() {
		if (!hasPrivateFlag(PFLAG_ATTACHED_TO_WINDOW)) {
			this.updatePrivateFlags(PFLAG_ATTACHED_TO_WINDOW, true);
			mIndicator.start();
		}
	}

	/**
	 * Called from the view which uses this helper to provide refresh feature to dispatch that this
	 * view has been detached from window.
	 * <p>
	 * This should be called whenever {@link View#onDetachedFromWindow()} is invoked
	 * within the view.
	 */
	public void dispatchViewDetachedFromWindow() {
		if (hasPrivateFlag(PFLAG_ATTACHED_TO_WINDOW)) {
			this.updatePrivateFlags(PFLAG_ATTACHED_TO_WINDOW, false);
			mIndicator.stop();
			mView.unscheduleDrawable(mIndicator);
			mIndicator.setCallback(null);
		}
	}

	/**
	 * Called from the view which uses this helper to provide refresh feature to dispatch that a size
	 * of this view has been changed to the new one.
	 * <p>
	 * This should be called whenever {@link View#onSizeChanged(int, int, int, int)} is invoked
	 * within the view.
	 *
	 * @param width  New width of the view.
	 * @param height New height of the view.
	 */
	public void dispatchViewSizeChanged(int width, int height) {
		if (mViewWidth != width || mViewHeight != height) {
			this.handleViewSizeChanged(width, height);
		}
	}

	/**
	 * Handles change in the refreshable view's size. This stores the current dimensions of the view,
	 * updates refresh distance if necessary and invalidates the indicator.
	 *
	 * @param width  New width of the view.
	 * @param height New height of the view.
	 */
	private void handleViewSizeChanged(int width, int height) {
		if (width > 0 && height > 0) {
			this.mViewWidth = width;
			this.mViewHeight = height;
			mIndicator.invalidateSelf();
		}
	}

	/**
	 * Returns the drawable which is drawn by this helper within {@link #drawRefreshIndicator(Canvas)}
	 * to indicate running refresh process.
	 * <p>
	 * <b>Use here returned drawable only for customization purposes like to change color or thickness
	 * of the progress arc. Any changes related to visibility or position will be changed in the feature
	 * by this helper so they will not persist.</b>
	 *
	 * @return Refresh indicator's drawable.
	 */
	@NonNull
	public final RefreshDrawable getRefreshIndicator() {
		return mIndicator;
	}

	/**
	 * Called to verify if the given <var>drawable</var> is refresh indicator's drawable of this
	 * helper.
	 * <p>
	 * This should be called whenever {@link View#verifyDrawable(Drawable)}
	 * is invoked within the view.
	 *
	 * @param drawable The drawable to verify.
	 * @return {@code True} if the verified drawable is refresh indicator's drawable, {@code false}
	 * otherwise.
	 */
	public boolean verifyRefreshIndicatorDrawable(@NonNull Drawable drawable) {
		return mIndicator == drawable;
	}

	/**
	 * Draws the refresh indicator's drawable on the specified <var>canvas</var>.
	 * <p>
	 * This should be called from within the view which uses this helper to provide refresh feature
	 * whenever {@link View#dispatchDraw(Canvas)}, so the indicator can be properly
	 * drawn above all content of the view.
	 *
	 * @param canvas The canvas on which to draw refresh indicator.
	 */
	@SuppressLint("NewApi")
	public void drawRefreshIndicator(@NonNull Canvas canvas) {
		if (mIndicator.isVisible() || mView.isInEditMode()) {
			final int indWidth = mIndicator.getIntrinsicWidth();
			final int indHeight = mIndicator.getIntrinsicHeight();
			// Modify (translate, scale, ...) and cut only the part of the canvas exactly for the
			// refresh indicator and draw it.
			final int saveCount = canvas.save();
			canvas.translate(
					mViewWidth / 2 - indWidth / 2,
					INDICATOR_INFO.top + INDICATOR_INFO.translationY
			);
			canvas.clipRect(mIndicator.getBounds());
			canvas.scale(INDICATOR_INFO.scale, INDICATOR_INFO.scale, indWidth / 2f, indHeight / 2f);
			mIndicator.draw(canvas);
			canvas.restoreToCount(saveCount);
			this.postInvalidateViewOnAnimation();
		}
	}

	/**
	 * Causes an invalidate of the pullable view to happen on the next animation time step.
	 */
	private void postInvalidateViewOnAnimation() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mView.postInvalidateOnAnimation();
		} else {
			final long currentTime = SystemClock.uptimeMillis();
			if (currentTime - mLastViewInvalidationTime >= UiConfig.ANIMATION_FRAME_UPDATE_INTERVAL) {
				mView.postDelayed(INVALIDATE_VIEW, UiConfig.ANIMATION_FRAME_UPDATE_INTERVAL);
			}
			this.mLastViewInvalidationTime = currentTime;
		}
	}

	/**
	 */
	@Override
	public boolean shouldInterceptTouchEvent(@NonNull MotionEvent event) {
		return !hasPrivateFlag(PFLAG_REFRESHING) && super.shouldInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean processTouchEvent(@NonNull MotionEvent event) {
		return !hasPrivateFlag(PFLAG_REFRESHING) && super.processTouchEvent(event);
	}

	/**
	 */
	@Override
	void onPullHelperChanged(PullHelper helper) {
		super.onPullHelperChanged(helper);
		helper.setPullMode(PullHelper.START);
		helper.setPullVisibility(PullHelper.START, false);
	}

	/**
	 */
	@Override
	protected void onPullStarted() {
		super.onPullStarted();
		this.updatePrivateFlags(PFLAG_PULLING, true);
		if (!hasPrivateFlag(PFLAG_REFRESHING)) {
			this.resetIndicator(true);
			mIndicator.setProgressArrowVisible(true);
		}
	}

	/**
	 * Resets all properties related to position, alpha and scale of the indicator.
	 *
	 * @param visible {@code True} to set indicator's drawable visible, {@code false} otherwise.
	 */
	private void resetIndicator(boolean visible) {
		mIndicator.setCallback(null);
		switch (mIndicatorTransition) {
			case Refreshable.TRANSITION_BELOW:
				INDICATOR_INFO.scale = 1f;
				mIndicator.setAlpha(255);
				setIndicatorTop(0);
				break;
			case Refreshable.TRANSITION_COPLANAR:
			case Refreshable.TRANSITION_ABOVE:
				INDICATOR_INFO.scale = 0f;
				mIndicator.setAlpha(0);
				setIndicatorTop(mIndicator.getIntrinsicHeight() / 2);
				break;
		}
		INDICATOR_INFO.translationY = 0;
		mIndicator.setVisible(visible, false);
		mIndicator.setInEditMode(mView.isInEditMode());
		if (mView.isInEditMode()) {
			INDICATOR_INFO.scale = 1f;
			mIndicator.setAlpha(255);
			mIndicator.setVisible(true, false);
			setIndicatorTop(Math.round(mRefreshSettlePosition * mPullDistance));
		}
		mIndicator.setCallback(mView);
		mIndicator.invalidateSelf();
	}

	/**
	 */
	@Override
	protected void onApplyPull(int pull, float position) {
		final boolean isRefreshing = hasPrivateFlag(PFLAG_REFRESHING);
		if (position >= 0) {
			if (!isRefreshing && !mAnimations.isPopDismissIndicatorAnimationRunning()) {
				mIndicator.setCallback(null);
				this.setIndicatorPullPosition(position);
				mIndicator.setCallback(mView);
				mIndicator.invalidateSelf();
			}
		} else if (isRefreshing) {
			// User is pulling refreshable view from the bottom, just translate the indicator backward,
			// so it will look like it is still.
			INDICATOR_INFO.translationY = (int) (-position * mPullDistance);
			mIndicator.invalidateSelf();
		}
	}

	/**
	 * Updates the current position of the refresh indicator. Depends on the {@link #mIndicatorTransition}
	 * type, this will update indicator's position, alpha or scale to properly transition indicator
	 * to its final refresh position or back.
	 *
	 * @param position The current pull position.
	 */
	final void setIndicatorPullPosition(float position) {
		if (mIndicatorTransition != Refreshable.TRANSITION_NONE) {
			final float refreshPos = position / mRefreshPosition;

			// Rotate, fade and update progress of indicator.
			if (position <= mRefreshPosition) {
				mIndicator.setProgress(Math.round(refreshPos * MAX_INDICATOR_PULL_PROGRESS));
				mIndicator.setProgressAlpha(Math.round(position / mRefreshPosition * 127));
				this.setIndicatorArrowScale(refreshPos);
			} else {
				mIndicator.setProgress(MAX_INDICATOR_PULL_PROGRESS);
				this.setIndicatorArrowScale(1);
				mIndicator.setProgressAlpha(255);
			}
			mIndicator.setProgressRotation(position * MAX_INDICATOR_PULL_ROTATION);

			// Translate indicator based on the selected transition.
			switch (mIndicatorTransition) {
				case Refreshable.TRANSITION_BELOW:
					this.setIndicatorTopAndInvalidate(Math.round(position * mPullDistance));
					break;
				case Refreshable.TRANSITION_COPLANAR:
				case Refreshable.TRANSITION_ABOVE:
					if (position <= mRefreshPosition) {
						this.setIndicatorScaleFadeRatio(refreshPos);
					}
					final float indScaleSize = mIndicator.getIntrinsicHeight() * (1 - INDICATOR_INFO.scale);
					this.setIndicatorTopAndInvalidate(Math.round(position * mPullDistance + indScaleSize / 2));
					break;
			}
			mIndicator.setVisible(position > 0, false);
		}
	}

	/**
	 * Like {@link #setIndicatorTop(int)} but this will also cause the indicator's invalidation.
	 */
	private void setIndicatorTopAndInvalidate(int top) {
		setIndicatorTop(top);
		mIndicator.invalidateSelf();
	}

	/**
	 * Updates the top position of the indicator and invalidates it.
	 *
	 * @param top The new top position for indicator.
	 */
	final void setIndicatorTop(int top) {
		INDICATOR_INFO.top = top - mIndicator.getIntrinsicHeight() + mIndicatorOffset;
	}

	/**
	 * Updates the scale and fade of the indicator based on the specified <var>ratio</var> and
	 * invalidates it.
	 *
	 * @param ratio The new ratio for the current alpha and scale values for indicator.
	 */
	final void setIndicatorScaleFadeRatio(float ratio) {
		INDICATOR_INFO.scale = ratio;
		mIndicator.setAlpha(Math.round(ratio * 255));
	}

	/**
	 * Updates the scale of the indicator's arrow and invalidates it.
	 *
	 * @param scale The new scale value for indicator's arrow.
	 */
	final void setIndicatorArrowScale(float scale) {
		mIndicator.setProgressArrowScale(scale);
	}

	/**
	 */
	@Override
	protected void onPullReleased(float pull, float position) {
		super.onPullReleased(pull, position);
		this.updatePrivateFlags(PFLAG_PULLING, false);

		if (position < mRefreshPosition) {
			this.hideIndicator(position);
		} else {
			// Change mode to indeterminate and hide progress arrow by scaling it down.
			changeProgressMode(CircularProgressDrawable.MODE_INDETERMINATE, null);

			mAnimations.scaleIndicatorArrowDown();
			if (position != mRefreshSettlePosition) {
				mAnimations.settleIndicator(position);
			}

			// Unfroze view after indicator is settled.
			mView.postDelayed(UNFROZE_VIEW, mAnimations.refreshSettleDuration);

			this.updatePrivateFlags(PFLAG_REFRESHING, true);
			if (mRefreshListener != null) {
				mRefreshListener.onRefresh(mView);
			}
		}
	}

	/**
	 * Translates the indicator the the origin position from where it can be again pulled by a user.
	 *
	 * @param pullPosition The current pull position.
	 */
	private void hideIndicator(float pullPosition) {
		if (pullPosition > 0) mAnimations.hideIndicator(pullPosition);
	}

	/**
	 */
	@Override
	protected void onCollapsePull(float pull, float position) {
		super.onCollapsePull(pull, position);
		// Keep view frozen until the indicator is hided/settled.
		setViewFrozen(true);
	}

	/**
	 * Sets a flag indicating whether the view using this helper is being right now refreshing or not.
	 * <p>
	 * Calling of this method will <b>pop</b> (using scale + fade in animation) the refresh indicator
	 * if calling  with {@code true} or <b>dismisses</b> (using scale + fade out animation) the
	 * indicator if calling with {@code false}.
	 * <p>
	 * <b>Note, that it is not supported to show the refresh indicator while a user is pulling the
	 * refreshable view.</b>
	 *
	 * @param refreshing {@code True} to dispatch that refresh process is running so the indicator
	 *                   should be visible, {@code false} to dispatch that refresh process has been
	 *                   finished so the indicator should be hided.
	 */
	public void setRefreshing(boolean refreshing) {
		if (hasPrivateFlag(PFLAG_REFRESHING) != refreshing) {
			if (refreshing && hasPrivateFlag(PFLAG_PULLING)) {
				return;
			}
			if (refreshing) {
				scheduleAnimation(POP_INDICATOR);
			} else {
				scheduleAnimation(DISMISS_INDICATOR);
			}
		}
	}

	/**
	 * Returns a flag indicating whether the refresh should be running, so the indicator is currently
	 * visible and spinning, or not.
	 * <p>
	 * This method will return {@code true} only in case when a user pulls down a refresh indicator
	 * and releases it after a refresh position specified by {@link #setRefreshPosition(float)} or
	 * {@link #setRefreshing(boolean)} has been last time called with {@code true}.
	 *
	 * @return {@code True} if refresh is running, {@code false} otherwise.
	 */
	public boolean isRefreshing() {
		return hasPrivateFlag(PFLAG_REFRESHING);
	}

	/**
	 * Shows the refresh indicator with scale + fade in animation at the refresh position.
	 */
	private void popIndicator() {
		mIndicator.setCallback(null);
		this.resetIndicator(true);
		mIndicator.setAlpha(0);
		// Position the indicator at the settle position.
		setIndicatorTop(Math.round(mRefreshSettlePosition * mPullDistance));
		mIndicator.setCallback(mView);
		changeProgressMode(CircularProgressDrawable.MODE_INDETERMINATE, false);
		mAnimations.popIndicator();
	}

	/**
	 * Dismisses the refresh indicator with scale + fade out animation from its current position.
	 */
	private void dismissIndicator() {
		mAnimations.dismissIndicator();
	}

	/**
	 * Changes progress mode of the indicator's progress drawable to the specified one.
	 *
	 * @param mode      The desired progress mode. One of {@link CircularProgressDrawable#MODE_DETERMINATE}
	 *                  or {@link CircularProgressDrawable#MODE_INDETERMINATE}.
	 * @param showArrow {@code True} to set progress arrow visible, {@code false} otherwise. Supply
	 *                  {@code null} to not touch arrow's visibility.
	 */
	private void changeProgressMode(@CircularProgressDrawable.ProgressMode int mode, Boolean showArrow) {
		mIndicator.setProgressMode(mode);
		if (showArrow != null) {
			mIndicator.setProgressArrowVisible(showArrow);
		}
	}

	/**
	 * Schedules the specified runnable that contains an animation execution code. If there is already
	 * running some animation that has been scheduled before, this will add the given runnable into
	 * the {@link #mScheduledAnimations} queue so it can be run after the current running animation
	 * ends.
	 *
	 * @param animationRunnable The animation runnable to be run.
	 */
	private void scheduleAnimation(Runnable animationRunnable) {
		if (hasPrivateFlag(PFLAG_RUNNING_SCHEDULED_ANIMATION)) {
			if (mScheduledAnimations == null) {
				this.mScheduledAnimations = new LinkedList<>();
			}
			mScheduledAnimations.add(animationRunnable);
		} else {
			mView.post(animationRunnable);
			updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, true);
		}
	}

	/**
	 * Starts the top (head) animation from the current scheduled animations (if any) if there is
	 * no animation running at this time.
	 *
	 * @see #mScheduledAnimations
	 */
	final void startScheduledAnimation() {
		updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, false);
		final boolean runningAnimation = hasPrivateFlag(PFLAG_RUNNING_SCHEDULED_ANIMATION);
		if (!runningAnimation && mScheduledAnimations != null && !mScheduledAnimations.isEmpty()) {
			mView.post(mScheduledAnimations.poll());
			updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, true);
		}
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Class holding info about the refresh indicator.
	 */
	private static final class IndicatorInfo {

		/**
		 * Current top position of the indicator.
		 */
		int top;

		/**
		 * Current translation Y of the indicator.
		 */
		int translationY;

		/**
		 * Current scale of the indicator.
		 */
		float scale = 1f;

		/**
		 */
		@Override
		public String toString() {
			//noinspection StringBufferReplaceableByString
			final StringBuilder builder = new StringBuilder(IndicatorInfo.class.getSimpleName() + "[");
			builder.append("top(");
			builder.append(top);
			builder.append("), translationY(");
			builder.append(translationY);
			builder.append("), scale(");
			builder.append(scale);
			return builder.append(")]").toString();
		}
	}

	/**
	 * Animations interface for this controller.
	 */
	private static abstract class Animations {

		/**
		 * Refresh controller for which to run animations.
		 */
		final RefreshController controller;

		/**
		 * Duration for the indicator's settle animation. The settle animation is played whenever a user
		 * releases the indicator and its current position is after the {@link #mRefreshSettlePosition}.
		 * <p>
		 * Default value: <b>{@link UiConfig#ANIMATION_DURATION_SHORT} / 2</b>
		 */
		long refreshSettleDuration = UiConfig.ANIMATION_DURATION_SHORT / 2;

		/**
		 * Duration for the indicator's hide animation. The hide animation is played whenever a user
		 * releases the indicator and its current position is before the {@link #mRefreshPosition}.
		 * <p>
		 * Default value: <b>{@link UiConfig#ANIMATION_DURATION_SHORT}</b>
		 */
		long indicatorHideDuration = UiConfig.ANIMATION_DURATION_SHORT;

		/**
		 * Duration for the indicator's pop animation. The pop animation is played whenever {@link #popIndicator()}
		 * is called, that is also whenever {@link #setRefreshing(boolean)} is called with {@code true}.
		 * <p>
		 * Default value: <b>{@link UiConfig#ANIMATION_DURATION_MEDIUM}</b>
		 */
		long indicatorPopDuration = UiConfig.ANIMATION_DURATION_MEDIUM;

		/**
		 * Duration for the indicator's dismiss animation. The dismiss animation is played whenever
		 * {@link #dismissIndicator()} is called, that is also whenever {@link #setRefreshing(boolean)}
		 * is called with {@code false}.
		 * <p>
		 * Default value: <b>250</b>
		 */
		long indicatorDismissDuration = 250L;

		/**
		 * Duration for the indicator's arrow scale down animation. The arrow scale down animation is
		 * played whenever the refresh process is initiated by releasing of the indicator.
		 * <p>
		 * Default value: <b>{@link UiConfig#ANIMATION_DURATION_LONG} * 2</b>
		 */
		long indicatorArrowScaleDuration = UiConfig.ANIMATION_DURATION_LONG * 2;

		/**
		 * Creates a new instance of Animations interface for the specified refresh <var>controller</var>.
		 *
		 * @param controller The refresh controler for which to run animations.
		 */
		Animations(RefreshController controller) {
			this.controller = controller;
		}

		/**
		 * Returns a new instance of Animations implementation specific for the current animations
		 * API capabilities.
		 *
		 * @param controller The refresh controller upon which will the returned Animations object
		 *                   run  all requested animations.
		 * @return New instance of Animations implementation.
		 */
		static Animations get(RefreshController controller) {
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
					new HoneyCombAnimations(controller) :
					new DefaultAnimations(controller);
		}

		/**
		 * Starts animation to pop (show) the refresh indicator.
		 */
		abstract void popIndicator();

		/**
		 * Starts animation to dismiss (hide) the refresh indicator.
		 */
		abstract void dismissIndicator();

		/**
		 * Starts animation to hide the refresh indicator. Animation will translate the indicator
		 * to its starting position pull gesture.
		 *
		 * @param pullPosition Current pull position from which to hide indicator.
		 */
		abstract void hideIndicator(float pullPosition);

		/**
		 * Starts animation to scale the refresh indicator's arrow down.
		 */
		abstract void scaleIndicatorArrowDown();

		/**
		 * Starts animation to settle the refresh indicator on the settle position.
		 *
		 * @param pullPosition Current pull position from which to settle indicator.
		 */
		abstract void settleIndicator(float pullPosition);

		/**
		 * Returns boolean flag indicating whether this wrapper is running some animations or not.
		 *
		 * @return {@code True} if some animations are running, {@code false} otherwise.
		 */
		boolean isRunning() {
			return isPopDismissIndicatorAnimationRunning();
		}

		/**
		 * Returns boolean flag indicating whether the pop/dismiss indicator animation is running
		 * or not.
		 *
		 * @return {@code True} if indicator's pop or dismiss animation is running, {@code false}
		 * otherwise.
		 */
		boolean isPopDismissIndicatorAnimationRunning() {
			return false;
		}

		/**
		 * Cancels all running animations.
		 */
		void cancel() {
		}
	}

	/**
	 * Default implementation of {@link Animations}.
	 */
	private static final class DefaultAnimations extends Animations {

		/**
		 * Creates a new instance of DefaultAnimations for the specified refresh <var>controller</var>.
		 */
		DefaultAnimations(RefreshController controller) {
			super(controller);
		}

		/**
		 */
		@Override
		void popIndicator() {
			controller.setIndicatorScaleFadeRatio(1f);
			controller.mIndicator.invalidateSelf();
			controller.updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, false);
		}

		/**
		 */
		@Override
		void dismissIndicator() {
			controller.setIndicatorScaleFadeRatio(0f);
			controller.mIndicator.setVisible(false, false);
			controller.changeProgressMode(CircularProgressDrawable.MODE_DETERMINATE, true);
			controller.updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, false);
		}

		/**
		 */
		@Override
		void hideIndicator(float pullPosition) {
			controller.setIndicatorPullPosition(0f);
			controller.mIndicator.setVisible(false, false);
			controller.mView.postDelayed(controller.UNFROZE_VIEW, 50);
			controller.updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, false);
		}

		/**
		 */
		@Override
		void scaleIndicatorArrowDown() {
			controller.mIndicator.setProgressArrowScale(0f);
			controller.mIndicator.setProgressArrowVisible(false);
		}

		/**
		 */
		@Override
		void settleIndicator(float pullPosition) {
			controller.setIndicatorTopAndInvalidate((int) (controller.mRefreshSettlePosition * controller.mPullDistance));
			controller.updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, false);
		}
	}

	/**
	 * A {@link Animations} implementation used on Android versions above (including) {@link Build.VERSION_CODES#HONEYCOMB}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static final class HoneyCombAnimations extends Animations {

		/**
		 * Animator used to animate indicator's top position.
		 */
		final ObjectAnimator INDICATOR_TOP_ANIMATOR = ObjectAnimator.ofInt(this, "indicatorTop", 0, 0);

		/**
		 * Animator used to animate indicator's pull position.
		 */
		final ObjectAnimator INDICATOR_PULL_POSITION_ANIMATOR = ObjectAnimator.ofFloat(this, "indicatorPullPosition", 0, 0);

		/**
		 * Animator used to animate indicator's scale and alpha values.
		 */
		final ObjectAnimator INDICATOR_POP_DISMISS_ANIMATOR = ObjectAnimator.ofFloat(this, "indicatorScaleFadeRatio", 0, 0);

		/**
		 * Animator used to animate indicator's arrow scale value.
		 */
		final ObjectAnimator INDICATOR_ARROW_SCALE_ANIMATOR = ObjectAnimator.ofFloat(this, "indicatorArrowScale", 0, 0);

		/**
		 * Creates a new instance of HoneyCombAnimations for the specified refresh <var>controller</var>.
		 */
		HoneyCombAnimations(RefreshController refreshController) {
			super(refreshController);
			INDICATOR_TOP_ANIMATOR.setTarget(controller);
			INDICATOR_PULL_POSITION_ANIMATOR.setTarget(controller);
			INDICATOR_POP_DISMISS_ANIMATOR.setTarget(controller);
			INDICATOR_ARROW_SCALE_ANIMATOR.setTarget(controller);

			final Animator.AnimatorListener scheduledAnimationsListener = new AnimatorListenerAdapter() {

				/**
				 */
				@Override
				public void onAnimationEnd(Animator animation) {
					controller.startScheduledAnimation();
				}
			};

			INDICATOR_TOP_ANIMATOR.addListener(scheduledAnimationsListener);
			INDICATOR_TOP_ANIMATOR.addListener(new AnimatorListenerAdapter() {

				/**
				 */
				@Override
				public void onAnimationEnd(Animator animation) {
					if (controller.INDICATOR_INFO.top < 0) {
						controller.mIndicator.setVisible(false, false);
						controller.setViewFrozen(false);
					}
				}
			});
			INDICATOR_PULL_POSITION_ANIMATOR.addListener(scheduledAnimationsListener);
			INDICATOR_PULL_POSITION_ANIMATOR.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (controller.INDICATOR_INFO.top < 0 || controller.INDICATOR_INFO.scale == 0) {
						controller.mIndicator.setVisible(false, false);
						controller.setViewFrozen(false);
					}
				}
			});
			INDICATOR_POP_DISMISS_ANIMATOR.addListener(scheduledAnimationsListener);
			INDICATOR_POP_DISMISS_ANIMATOR.addListener(new AnimatorListenerAdapter() {

				/**
				 */
				@Override
				public void onAnimationEnd(Animator animation) {
					if (controller.hasPrivateFlag(PFLAG_POPPING_INDICATOR)) {
						controller.updatePrivateFlags(PFLAG_POPPING_INDICATOR, false);
					} else if (controller.hasPrivateFlag(PFLAG_DISMISSING_INDICATOR)) {
						controller.updatePrivateFlags(PFLAG_DISMISSING_INDICATOR, false);
						controller.mIndicator.setVisible(false, false);
						controller.changeProgressMode(CircularProgressDrawable.MODE_DETERMINATE, true);
					}
				}
			});
			INDICATOR_ARROW_SCALE_ANIMATOR.addListener(new AnimatorListenerAdapter() {

				/**
				 */
				@Override
				public void onAnimationEnd(Animator animation) {
					if (controller.mIndicator.getProgressArrowScale() == 0) {
						controller.mIndicator.setProgressArrowVisible(false);
					}
				}
			});
		}

		/**
		 */
		@Override
		void popIndicator() {
			controller.updatePrivateFlags(PFLAG_POPPING_INDICATOR, true);
			INDICATOR_POP_DISMISS_ANIMATOR.setFloatValues(0, 1);
			INDICATOR_POP_DISMISS_ANIMATOR.setDuration(indicatorPopDuration);
			INDICATOR_POP_DISMISS_ANIMATOR.start();
		}

		/**
		 */
		@Override
		void dismissIndicator() {
			controller.updatePrivateFlags(PFLAG_DISMISSING_INDICATOR, true);
			INDICATOR_POP_DISMISS_ANIMATOR.setFloatValues(1, 0);
			INDICATOR_POP_DISMISS_ANIMATOR.setDuration(indicatorDismissDuration);
			INDICATOR_POP_DISMISS_ANIMATOR.start();
		}

		/**
		 */
		@Override
		void hideIndicator(float pullPosition) {
			INDICATOR_PULL_POSITION_ANIMATOR.setFloatValues(pullPosition, 0);
			INDICATOR_PULL_POSITION_ANIMATOR.setDuration(indicatorHideDuration);
			INDICATOR_PULL_POSITION_ANIMATOR.start();
			controller.updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, true);
		}

		/**
		 */
		@Override
		void scaleIndicatorArrowDown() {
			INDICATOR_ARROW_SCALE_ANIMATOR.setFloatValues(1, 0);
			INDICATOR_ARROW_SCALE_ANIMATOR.setDuration(indicatorArrowScaleDuration);
			INDICATOR_ARROW_SCALE_ANIMATOR.start();
		}

		/**
		 */
		@Override
		void settleIndicator(float pullPosition) {
			INDICATOR_TOP_ANIMATOR.setIntValues(
					(int) (pullPosition * controller.mPullDistance),
					(int) (controller.mRefreshSettlePosition * controller.mPullDistance)
			);
			INDICATOR_TOP_ANIMATOR.setDuration(refreshSettleDuration);
			INDICATOR_TOP_ANIMATOR.start();
			controller.updatePrivateFlags(PFLAG_RUNNING_SCHEDULED_ANIMATION, true);
		}

		/**
		 */
		@Override
		boolean isPopDismissIndicatorAnimationRunning() {
			return INDICATOR_POP_DISMISS_ANIMATOR.isRunning();
		}

		/**
		 */
		@Override
		void cancel() {
			super.cancel();
			INDICATOR_TOP_ANIMATOR.cancel();
			INDICATOR_PULL_POSITION_ANIMATOR.cancel();
			INDICATOR_POP_DISMISS_ANIMATOR.cancel();
			INDICATOR_ARROW_SCALE_ANIMATOR.cancel();
		}
	}
}
