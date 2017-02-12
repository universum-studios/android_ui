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
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.widget.Pullable;
import universum.studios.android.ui.widget.Scrollable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * PullController can be used to support <b>pull</b> feature for the scrollable views, especially
 * collections like {@link android.widget.ListView} or {@link android.widget.ScrollView}. A view that
 * wants to provide pull feature need to implement required {@link Scrollable} interface that specifies
 * simple API needed for this controller to properly handle this feature.
 * <p>
 * PullController supports pulling of the attached pullable view at its <b>start</b> and <b>end</b>
 * depending on the view's orientation that is determined via {@link Scrollable#getOrientation()}
 * by changing the view's <b>top and bottom</b> ({@link View#offsetTopAndBottom(int)}) or <b>left and right</b>
 * ({@link View#offsetLeftAndRight(int)}) offset. Whether the pulling at start or end is enabled or
 * not can be requested via {@link #setPullMode(int)} and passing flags for the desired pull type.
 * Visibility of pull can be requested via {@link #setPullVisibility(int, boolean)} and also passing
 * the desired pull flags.
 * <p>
 * To receive callback about the current pull, a watcher needs to register {@link Pullable.OnPullListener}
 * that receives callbacks about the <b>started</b>, <b>performed</b>, <b>released</b> or <b>collapsed</b>
 * pull. {@link Pullable.OnPullOverflowListener} that can be also registered receives one callback
 * about the <b>pull overflow</b>.
 * <p>
 * There are a lot of properties that can be set to customize the pull behaviour, for example how
 * much can be the view pulled ({@link #setPullDistance(float)}), or how much time does the collapse
 * pull animation takes ({@link #setPullCollapseDuration(long)}).
 *
 * <h3>XML attributes</h3>
 * PullController is meant to be used within views so it also supports set up from {@link AttributeSet}
 * via {@link #setUpFromAttrs(android.content.Context, android.util.AttributeSet, int, int)} method
 * that should be called from within a constructor of the pullable view that uses this controller
 * to provide the pull feature. See {@link R.styleable#Ui_PullController PullController Attributes}.
 *
 * @param <V> Type of the pullable view.
 * @author Martin Albedinsky
 */
public class PullController<V extends View & Pullable> extends BasePullController<V> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "PullController";

	/**
	 * Defines an annotation for determining set of allowed flags for {@link #setPullMode(int)} and
	 * {@link #setPullVisibility(int, boolean)} methods.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef(flag = true, value = {NONE, START, END})
	public @interface PullFlags {
	}

	/**
	 * Flag for {@link #setPullMode(int)} to indicate that neither start or end pull are enabled.
	 */
	public static final int NONE = PullHelper.NONE;

	/**
	 * Flag for {@link #setPullMode(int)} to indicate that the pull at the start of a pullable view
	 * is enabled.
	 */
	public static final int START = PullHelper.START;

	/**
	 * Flag for {@link #setPullMode(int)} to indicate that the pull at the end of a pullable view
	 * is enabled.
	 */
	public static final int END = PullHelper.END;

	/**
	 * Default duration for the pull collapse animation.
	 */
	private static final int PULL_COLLAPSE_DURATION = 200;

	/**
	 * Flag indicating whether the fixed time for pull collapse animation is enabled or not.
	 */
	private static final int PFLAG_PULL_COLLAPSE_FIXED_TIME_ENABLED = 0x00000001;

	/**
	 * Flag indicating whether the over-scroll animation is enabled or not.
	 */
	private static final int PFLAG_OVER_SCROLL_ANIMATION_ENABLED = 0x00000001 << 1;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Current pull data.
	 */
	final PullImpl PULL = new PullImpl();

	/**
	 * Tracker used to track velocity value to determine whether to initiate pull or not.
	 */
	private final VelocityTracker VELOCITY_TRACKER = VelocityTracker.obtain();

	/**
	 * Animations helper used to run animations upon this controller regardless current Android version.
	 */
	private final Animations mAnimations;

	/**
	 * Set of pull listeners which can receive callbacks about <b>started</b>, <b>performed</b>,
	 * <b>released</b> and <b>collapsed</b> pull.
	 */
	private List<Pullable.OnPullListener> mListeners;

	/**
	 * Set of pull listeners which can receive callback about <b>pull overflow</b>.
	 */
	private List<Pullable.OnPullOverflowListener> mOverflowListeners;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of PullController to handle pulling of the specified <var>view</var>.
	 *
	 * @param view The view of which pull to handle by the new controller.
	 */
	public PullController(@NonNull V view) {
		super(view);
		this.mAnimations = Animations.get(this);
		updatePrivateFlags(PFLAG_PULL_COLLAPSE_FIXED_TIME_ENABLED, true);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Performs configuration of this controller from the given <var>attrs</var>.
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
		final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_PullController, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			this.ensurePullHelper();
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_PullController_uiPullMode) {
					setPullMode(typedArray.getInteger(index, mPullHelper.getPullMode()));
				} else if (index == R.styleable.Ui_PullController_uiPullMinVelocity) {
					setPullMinVelocity(typedArray.getFloat(index, getPullMinVelocity()));
				} else if (index == R.styleable.Ui_PullController_uiPullDistanceFraction) {
					setPullDistanceFraction(typedArray.getFloat(index, getPullDistanceFraction()));
				} else if (index == R.styleable.Ui_PullController_uiPullDistance) {
					setPullDistance(typedArray.getDimensionPixelSize(index, (int) getPullDistance()));
				} else if (index == R.styleable.Ui_PullController_uiPullCollapseDuration) {
					setPullCollapseDuration(typedArray.getInt(index, (int) mAnimations.pullCollapseDuration));
				} else if (index == R.styleable.Ui_PullController_uiPullCollapseDelay) {
					setPullCollapseDelay(typedArray.getInt(index, (int) mAnimations.pullCollapseDelay));
				}
			}
			typedArray.recycle();
			return n > 0;
		}
		return false;
	}

	/**
	 * Sets the mode for pull to determine which pull to handle by this controller.
	 *
	 * @param mode The desired mode. One of {@link #START}, {@link #END} or theirs combination or
	 *             {@link #NONE} to disable pull.
	 * @see R.attr#uiPullMode ui:uiPullMode
	 * @see #getPullMode()
	 */
	public void setPullMode(@PullFlags int mode) {
		this.ensurePullHelper();
		mPullHelper.setPullMode(mode);
	}

	/**
	 * Returns the current mode for pull.
	 * <p>
	 * Default value: <b>{@link #START} | {@link #END}</b>
	 *
	 * @return Pull mode.
	 * @see #setPullMode(int)
	 */
	@PullFlags
	@SuppressWarnings("ResourceType")
	public int getPullMode() {
		this.ensurePullHelper();
		return mPullHelper.getPullMode();
	}

	/**
	 * Sets a flag indicating whether a pull for the specified <var>pull</var> flags should be visible
	 * or not. This determines whether the attached pullable view should be really offset by this
	 * controller or not. If visibility is set to {@code false} for a specific pull, the pullable view
	 * will not be offset but pull callbacks will be still fired with computed pull value.
	 *
	 * @param pull    The pull flags of which visibility to set.
	 * @param visible {@code True} to set visible, {@code false} otherwise.
	 * @see #isPullVisible(int)
	 */
	public void setPullVisibility(@PullFlags int pull, boolean visible) {
		this.ensurePullHelper();
		mPullHelper.setPullVisibility(pull, visible);
	}

	/**
	 * Returns a flag indicating whether a pull for the specified <var>pull</var> flags is visible,
	 * so the pullable view is really offset by this controller, or not.
	 *
	 * @param pull The pull flags of which visibility to check.
	 * @return {@code True} if pull is visible, {@code false} otherwise.
	 * @see #setPullVisibility(int, boolean)
	 */
	public boolean isPullVisible(@PullFlags int pull) {
		this.ensurePullHelper();
		return mPullHelper.isPullVisible(pull);
	}

	/**
	 * Sets the interpolator for pull collapse animation.
	 *
	 * @param interpolator The desired interpolator used when collapsing pull by animation.
	 */
	public void setPullCollapseInterpolator(@Nullable Interpolator interpolator) {
		mAnimations.interpolator = interpolator;
	}

	/**
	 * Sets the duration for pull collapse animation.
	 *
	 * @param duration The desired duration in milliseconds used when collapsing pull by animation.
	 * @see R.attr#uiPullCollapseDuration ui:uiPullCollapseDuration
	 * @see #getPullCollapseDuration()
	 */
	public void setPullCollapseDuration(long duration) {
		mAnimations.pullCollapseDuration = Math.max(0, duration);
	}

	/**
	 * Returns the duration for pull collapse animation.
	 * <p>
	 * Default value: <b>300 ms</b>
	 *
	 * @return Collapse duration in milliseconds.
	 * @see #setPullCollapseDuration(long)
	 */
	public long getPullCollapseDuration() {
		return mAnimations.pullCollapseDuration;
	}

	/**
	 * Sets the delay for pull collapse animation.
	 *
	 * @param delay The desired delay in milliseconds used when collapsing pull by animation.
	 * @see R.attr#uiPullCollapseDelay ui:uiPullCollapseDelay
	 * @see #getPullCollapseDelay()
	 */
	public void setPullCollapseDelay(long delay) {
		mAnimations.pullCollapseDelay = Math.max(0, delay);
	}

	/**
	 * Returns the delay for pull collapse animation.
	 * <p>
	 * Default value: <b>0</b>
	 *
	 * @return Collapse delay in milliseconds.
	 * @see #setPullCollapseDelay(long)
	 */
	public long getPullCollapseDelay() {
		return mAnimations.pullCollapseDelay;
	}

	/**
	 * Sets a flag indicating whether the duration for pull collapse animation set by {@link #setPullCollapseDuration(long)}
	 * should be used as fixed value or if it should be computed relatively to the distance of the
	 * current pull needed to collapse by animation.
	 *
	 * @param enabled {@code True} to use fixed value, {@code false} to use relative value depends
	 *                on the current pull to collapse.
	 * @see #isPullCollapseFixedTimeEnabled()
	 */
	public void setPullCollapseFixedDurationEnabled(boolean enabled) {
		updatePrivateFlags(PFLAG_PULL_COLLAPSE_FIXED_TIME_ENABLED, enabled);
	}

	/**
	 * Returns a flag indicating whether the duration set by {@link #setPullCollapseDuration(long)}
	 * is used as fixed value or relative value.
	 * <p>
	 * Default value: <b>false</b>
	 *
	 * @return {@code True} if it is used as fixed, {@code false} as relative value.
	 */
	public boolean isPullCollapseFixedTimeEnabled() {
		return hasPrivateFlag(PFLAG_PULL_COLLAPSE_FIXED_TIME_ENABLED);
	}

	/**
	 * Sets a flag indicating whether the over-scroll animation is played whenever the attached
	 * pullable view is over-scrolled, and specific criteria to play the animation are matched, or not.
	 *
	 * @param enabled {@code True} to play over-scroll animation whenever {@link #dispatchOverScroll(int, int, boolean, boolean)}
	 *                is called upon this controller from the attached pullable view, {@code false}
	 *                otherwise.
	 * @see #isOverScrollAnimationEnabled()
	 */
	public void setOverScrollAnimationEnabled(boolean enabled) {
		updatePrivateFlags(PFLAG_OVER_SCROLL_ANIMATION_ENABLED, enabled);
	}

	/**
	 * Returns a flag indicating whether the over-scroll animation is played for the attached pullable
	 * view over-scroll or not.
	 *
	 * @return {@code True} if the animation is played, {@code false} otherwise.
	 * @see #setOverScrollAnimationEnabled(boolean)
	 */
	public boolean isOverScrollAnimationEnabled() {
		return hasPrivateFlag(PFLAG_OVER_SCROLL_ANIMATION_ENABLED);
	}

	/**
	 * Registers a callback to be invoked whenever the pull upon the attached pullable view is <b>started</b>,
	 * <b>performed</b>, <b>released</b> or <b>collapsed</b>.
	 *
	 * @param listener Listener callback to register.
	 * @see #unregisterOnPullListener(Pullable.OnPullListener)
	 */
	public void registerOnPullListener(@NonNull Pullable.OnPullListener listener) {
		if (mListeners == null) this.mListeners = new ArrayList<>();
		if (!mListeners.contains(listener)) mListeners.add(listener);
	}

	/**
	 * Un-registers previously registered OnPullListener listener.
	 *
	 * @param listener Listener callback to un-register.
	 * @see #registerOnPullListener(Pullable.OnPullListener)
	 */
	public void unregisterOnPullListener(@NonNull Pullable.OnPullListener listener) {
		if (mListeners != null) mListeners.remove(listener);
	}

	/**
	 * Registers a callback to be invoked whenever the pull overflow upon the attached pullable view
	 * is <b>performed</b>.
	 *
	 * @param listener Listener callback to register.
	 * @see #unregisterOnPullOverflowListener(Pullable.OnPullOverflowListener)
	 */
	public void registerOnPullOverflowListener(@NonNull Pullable.OnPullOverflowListener listener) {
		if (mOverflowListeners == null) this.mOverflowListeners = new ArrayList<>();
		if (!mOverflowListeners.contains(listener)) mOverflowListeners.add(listener);
	}

	/**
	 * Un-registers previously registered OnPullOverflowListener listener.
	 *
	 * @param listener Listener callback to un-register.
	 * @see #registerOnPullOverflowListener(Pullable.OnPullOverflowListener)
	 */
	public void unregisterOnPullOverflowListener(@NonNull Pullable.OnPullOverflowListener listener) {
		if (mOverflowListeners != null) mOverflowListeners.remove(listener);
	}

	/**
	 */
	@Override
	public boolean shouldInterceptTouchEvent(@NonNull MotionEvent event) {
		return isPullEnabled() && super.shouldInterceptTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean processTouchEvent(@NonNull MotionEvent event) {
		return isPullEnabled() && super.processTouchEvent(event);
	}

	/**
	 */
	@Override
	void onPullHelperChanged(PullHelper helper) {
		super.onPullHelperChanged(helper);
		mAnimations.onTargetsChanged(this);
	}

	/**
	 * Returns boolean flag indicating whether the pull handling is enabled or not.
	 *
	 * @return {@code True} if pull is enabled, {@code false} otherwise.
	 */
	private boolean isPullEnabled() {
		this.ensurePullHelper();
		return mPullHelper.getPullMode() != NONE;
	}

	/**
	 */
	@Override
	protected void onPullStarted() {
		super.onPullStarted();
		if (mListeners != null && !mListeners.isEmpty()) {
			final PullImpl pull = PULL.clone();
			for (Pullable.OnPullListener listener : mListeners) {
				listener.onPullStarted(mView, pull);
			}
		}
	}

	/**
	 */
	@Override
	protected void onApplyPull(int pull, float position) {
		super.onApplyPull(pull, position);
		PULL.position = position;
		this.notifyPull();
	}

	/**
	 * Notifies the current listeners that the pull has been performed.
	 */
	final void notifyPull() {
		if (mListeners != null && !mListeners.isEmpty()) {
			final PullImpl pull = PULL.clone();
			for (Pullable.OnPullListener listener : mListeners) {
				listener.onPull(mView, pull);
			}
		}
	}

	/**
	 */
	@Override
	protected void onPullOverflow(float pull, float overflow, float position) {
		super.onPullOverflow(pull, overflow, position);
		PULL.pullOverflowPosition = position;
		if (mOverflowListeners != null && !mOverflowListeners.isEmpty()) {
			final PullImpl currentPull = PULL.clone();
			for (Pullable.OnPullOverflowListener listener : mOverflowListeners) {
				listener.onPullOverflow(mView, currentPull);
			}
		}
	}

	/**
	 */
	@Override
	protected void onPullReleased(float pull, float position) {
		super.onPullReleased(pull, position);
		if (mListeners != null && !mListeners.isEmpty()) {
			final PullImpl currentPull = PULL.clone();
			for (Pullable.OnPullListener listener : mListeners) {
				listener.onPullReleased(mView, currentPull);
			}
		}
	}

	/**
	 */
	@Override
	protected void onCollapsePull(float pull, float position) {
		mAnimations.cancel();
		mAnimations.collapsePull(pull);
	}

	/**
	 */
	@Override
	protected void onPullCollapsed() {
		super.onPullCollapsed();
		PULL.collapsing = false;
		PULL.position = 0;
		PULL.pullOverflowPosition = 0;
		if (mListeners != null && !mListeners.isEmpty()) {
			for (Pullable.OnPullListener listener : mListeners) {
				listener.onPullCollapsed(mView);
			}
		}
	}

	/**
	 * Called to dispatch over-scroll of the attached pullable view. If the pull is enabled and also
	 * pull over-scroll animation is enabled by {@link #setOverScrollAnimationEnabled(boolean)}, this
	 * will run over-scroll animation upon the pullable view which will look like same as the pull
	 * performed by a user.
	 *
	 * @param scrollX  Current scroll X of the pullable view.
	 * @param scrollY  Current scroll Y of the pullable view.
	 * @param clampedX {@code True} if <var>scrollX</var> was clamped to an over-scroll boundary,
	 *                 {@code false} otherwise.
	 * @param clampedY {@code True} if <var>scrollY</var> was clamped to an over-scroll boundary,
	 *                 {@code false} otherwise.
	 * @return {@code True} if over-scroll animation has been started, {@code false} otherwise.
	 */
	public boolean dispatchOverScroll(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		if (!isPullEnabled() || !hasPrivateFlag(PFLAG_OVER_SCROLL_ANIMATION_ENABLED)) {
			return false;
		}

		final float overScroll = mPullHelper.computeOverScroll(scrollX, scrollY, clampedX, clampedY, VELOCITY_TRACKER, UiConfig.VELOCITY_UNITS);
		final float absOverScroll = Math.abs(overScroll);
		if (absOverScroll > 0 && !mAnimations.isRunning()) {
			mAnimations.mimicOverScroll(absOverScroll);
			return true;
		}
		return false;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Class used to hold data about the current pull handled by {@link PullController}.
	 *
	 * @author Martin Albedinsky
	 */
	private final class PullImpl implements Pullable.Pull, Cloneable {

		/**
		 * Current position of the pull.
		 */
		float position;

		/**
		 * Current position of the pull overflow.
		 */
		float pullOverflowPosition;

		/**
		 * Flag indicating whether pull is being collapsing by animation or not.
		 */
		boolean collapsing;

		/**
		 * Creates a new instance of empty Pull.
		 */
		private PullImpl() {
		}

		/**
		 */
		@Override
		public float getPosition() {
			return position;
		}

		/**
		 */
		@Override
		public float getPullOverflowPosition() {
			return pullOverflowPosition;
		}

		/**
		 */
		@Override
		public boolean collapse() {
			if (!collapsing) {
				this.collapsing = true;
				collapsePull();
				return true;
			}
			return false;
		}

		/**
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected PullImpl clone() {
			try {
				return (PullImpl) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError();
			}
		}
	}

	/**
	 * Animations interface for this controller.
	 */
	private static abstract class Animations {

		/**
		 * Pull controller for which to run animations.
		 */
		final PullController controller;

		/**
		 * Duration for the pull collapse animation. The pull collapse animation is played when the current
		 * pull is released by a user to move pullable view to its original offset.
		 */
		long pullCollapseDuration = PULL_COLLAPSE_DURATION;

		/**
		 * Delay for the pull collapse animation.
		 */
		long pullCollapseDelay;

		/**
		 * Interpolator used to interpolate animated pull value.
		 */
		Interpolator interpolator = new DecelerateInterpolator();

		/**
		 * Creates a new instance of Animations interface for the specified pull <var>controller</var>.
		 *
		 * @param controller The pull controller for which to run animations.
		 */
		Animations(PullController controller) {
			this.controller = controller;
		}

		/**
		 * Returns a new instance of Animations implementation specific for the current animations
		 * API capabilities.
		 *
		 * @param controller The pull controller upon which will the returned Animations object run
		 *                   all requested animations.
		 * @return New instance of Animations implementation.
		 */
		static Animations get(PullController controller) {
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
					new HoneyCombAnimations(controller) :
					new DefaultAnimations(controller);
		}

		/**
		 * Called whenever some of animation targets has changed so they should be reattached to
		 * theirs animators.
		 *
		 * @param controller The pull controller that uses these animations.
		 */
		void onTargetsChanged(PullController controller) {
		}

		/**
		 * Starts animation to collapse current pull.
		 *
		 * @param pull The desired pull value from which to run collapse animation.
		 */
		abstract void collapsePull(float pull);

		/**
		 * Starts animation to mimic over scroll behaviour with the specified <var>overScroll</var>
		 * value.
		 *
		 * @param overScroll The desired over scroll value.
		 */
		abstract void mimicOverScroll(float overScroll);

		/**
		 * Returns boolean flag indicating whether this wrapper is running some animations or not.
		 *
		 * @return {@code True} if some animations are running, {@code false} otherwise.
		 */
		boolean isRunning() {
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
		 * Runnable action used to schedule execution of {@link #handlePullCollapsed()}.
		 */
		private final Runnable HANDLE_PULL_COLLAPSED = new Runnable() {
			@Override
			public void run() {
				controller.handlePullCollapsed();
			}
		};

		/**
		 * Creates a new instance of DefaultAnimations for the specified pull <var>controller</var>.
		 */
		DefaultAnimations(PullController controller) {
			super(controller);
		}

		/**
		 */
		@Override
		void collapsePull(float pull) {
			// We do not run any animation, just handle like if we did and animation did ended.
			controller.postDelayed(HANDLE_PULL_COLLAPSED, 50);
		}

		/**
		 */
		@Override
		void mimicOverScroll(float overScroll) {
			// Ignored.
		}
	}

	/**
	 * A {@link Animations} implementation used on Android versions above (including) {@link Build.VERSION_CODES#HONEYCOMB}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static final class HoneyCombAnimations extends Animations {

		/**
		 * Animator used to animate pull.
		 */
		private final ObjectAnimator PULL_ANIMATOR = ObjectAnimator.ofInt(this, "pullOffset", 0, 0);

		{
			PULL_ANIMATOR.addListener(new AnimatorListenerAdapter() {

				/**
				 */
				@Override
				public void onAnimationEnd(Animator animation) {
					controller.mPullHelper.mPullOffset = (int) PULL_ANIMATOR.getAnimatedValue();
					if (controller.mPullHelper.mPullOffset == 0) {
						controller.handlePullCollapsed();
					}
				}
			});
			PULL_ANIMATOR.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				/**
				 */
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					controller.notifyPull();
				}
			});
		}

		/**
		 * Runnable used to start animation to mimic over-scroll.
		 */
		private final CollapseOverScrollAction mimicOverScroll;

		/**
		 * Creates a new instance of HoneyCombAnimations for the specified pull <var>controller</var>.
		 */
		HoneyCombAnimations(PullController controller) {
			super(controller);
			this.mimicOverScroll = new CollapseOverScrollAction(this);
			onTargetsChanged(controller);
		}

		/**
		 */
		@Override
		void onTargetsChanged(PullController controller) {
			super.onTargetsChanged(controller);
			PULL_ANIMATOR.setTarget(controller.mPullHelper);
		}

		/**
		 */
		@Override
		void collapsePull(float pull) {
			PULL_ANIMATOR.setIntValues((int) pull, 0);
			PULL_ANIMATOR.setDuration(controller.hasPrivateFlag(PFLAG_PULL_COLLAPSE_FIXED_TIME_ENABLED) ?
					pullCollapseDuration :
					Math.round(pullCollapseDuration * (Math.abs(pull) / controller.mPullDistance))
			);
			PULL_ANIMATOR.setStartDelay(pullCollapseDelay);
			PULL_ANIMATOR.setInterpolator(interpolator);
			PULL_ANIMATOR.start();
		}

		/**
		 */
		@Override
		void mimicOverScroll(float overScroll) {
			final int duration = Math.round(overScroll);
			PULL_ANIMATOR.setIntValues(0, (int) overScroll);
			PULL_ANIMATOR.setDuration(duration);
			PULL_ANIMATOR.setInterpolator(interpolator);
			mimicOverScroll.overScroll = overScroll;
			controller.postDelayed(mimicOverScroll, duration);
			PULL_ANIMATOR.start();
		}

		/**
		 */
		@Override
		boolean isRunning() {
			return PULL_ANIMATOR.isRunning();
		}

		/**
		 */
		@Override
		void cancel() {
			PULL_ANIMATOR.cancel();
		}
	}

	/**
	 * Action used to collapse the current over-scroll.
	 */
	private static class CollapseOverScrollAction implements Runnable {

		/**
		 * Animations used to mimic over-scroll.
		 */
		final Animations animations;

		/**
		 * Value of the over-scroll to collapse.
		 */
		float overScroll;

		/**
		 * Creates a new instance of CollapseOverScrollAction with the given <var>animations</var>.
		 *
		 * @param animations Animations used to mimic over-scroll.
		 */
		CollapseOverScrollAction(Animations animations) {
			this.animations = animations;
		}

		/**
		 */
		@Override
		public void run() {
			animations.mimicOverScroll(overScroll);
		}
	}
}
