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

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Interpolator;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.UiException;
import universum.studios.android.ui.interpolator.ResistanceInterpolator;
import universum.studios.android.ui.widget.Orientation;
import universum.studios.android.ui.widget.Scrollable;

/**
 * Base class for controllers that want to provide on pull gesture based features like <b>pull</b>
 * or <b>refresh</b>.
 *
 * @param <V> Type of the pullable view.
 * @author Martin Albedinsky
 */
abstract class BasePullController<V extends View & Scrollable> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Interface used to transform computed pull value for the current pull position. Transformer
	 * can use for example implementation of {@link android.view.animation.Interpolator} to simulate
	 * pull based on a specific curve.
	 *
	 * @author Martin Albedinsky
	 */
	public interface PullTransformer {

		/**
		 * Called to transform pull offset for the specified position.
		 *
		 * @param offset   Computed offset by {@link BasePullController} as delta from the last and
		 *                 current touch event.
		 * @param position Position of the current pull depending on the current value of pull and
		 *                 maximum pull distance.
		 * @return Transformed pull offset to be used by the controller.
		 */
		float transform(float offset, float position);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "BasePullController";

	/**
	 * State indicating that no actions are currently handled by this controller.
	 */
	private static final int STATE_IDLE = 0x00;

	/**
	 * State indicating that the pullable view is being scrolled at this time.
	 */
	private static final int STATE_SCROLLING = 0x01;

	/**
	 * State indicating that this controller is ready to start pulling process.
	 */
	private static final int STATE_READY_TO_PULL = 0x02;

	/**
	 * State indicating that this controller is performing pull upon the pullable view by offsetting
	 * its start/end position.
	 */
	private static final int STATE_PULLING = 0x03;

	/**
	 * State indicating that the current pull has been released by a user.
	 */
	private static final int STATE_RELEASED = 0x04;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * View of which pull will be managed by this controller.
	 */
	final V mView;

	/**
	 * Helper used to apply pull to the attached pullable view.
	 */
	PullHelper mPullHelper;

	/**
	 * Distance determining how much pull can be applied to the attached view.
	 */
	float mPullDistance;

	/**
	 * Fraction determining value of {@link #mPullDistance} from the size of the attached view.
	 */
	float mPullDistanceFraction = 0.25f;

	/**
	 * Value of the current pull.
	 */
	float mPull;

	/**
	 * Value of the current pull overflow.
	 */
	float mPullOverflow;

	/**
	 * Tracker used to track velocity value to determine whether to initiate pull or not.
	 */
	private final VelocityTracker VELOCITY_TRACKER = VelocityTracker.obtain();

	/**
	 * Set of private flags for this controller.
	 */
	private int mPrivateFlags;

	/**
	 * Current state of this controller determining its pull handling.
	 */
	private int mState = STATE_IDLE;

	/**
	 * Minimal velocity (in pixels per second) for pull to start.
	 */
	private float mPullMinVelocity;

	/**
	 * Helper used to transform computed value for the pull. This transformer can use {@link android.view.animation.Interpolator}
	 * to simulate pull based on specific curve.
	 */
	private PullTransformer mPullTransformer = new PullTransformerImpl();

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of BasePullController with the specified <var>view</var> attached.
	 * <p>
	 * <b>Note</b>, that only views with {@link Orientation#VERTICAL}, {@link Orientation#HORIZONTAL}
	 * orientation are supported.
	 *
	 * @param view The view of which pull should be managed by the new controller.
	 * @see Scrollable#getOrientation()
	 */
	protected BasePullController(@NonNull V view) {
		this.mView = view;
		switch (mView.getOrientation()) {
			case Orientation.VERTICAL:
			case Orientation.HORIZONTAL:
				break;
			default:
				throw UiException.misconfiguration("Only views with VERTICAL | HORIZONTAL orientation are supported.");
		}
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Sets the minimum velocity to initiate pull.
	 *
	 * @param velocity The desired velocity value for {@link UiConfig#VELOCITY_UNITS} units.
	 * @see R.attr#uiPullMinVelocity ui:uiPullMinVelocity
	 * @see #getPullMinVelocity()
	 */
	public void setPullMinVelocity(float velocity) {
		this.mPullMinVelocity = Math.max(0, velocity);
	}

	/**
	 * Returns the minimum velocity to initiate pull.
	 * <p>
	 * Default value: <b>0</b>
	 *
	 * @return Minimum velocity.
	 * @see #setPullMinVelocity(float)
	 */
	public float getPullMinVelocity() {
		return mPullMinVelocity;
	}

	/**
	 * Sets the distance which determines how much can be the attached view pulled.
	 *
	 * @param distance The desired value in pixels to determine maximum pull distance.
	 * @see R.attr#uiPullDistance ui:uiPullDistance
	 * @see #getPullDistance()
	 * @see #setPullDistanceFraction(float)
	 */
	public void setPullDistance(float distance) {
		if (mPullDistance != distance) {
			if ((mPullDistance = Math.max(0, distance)) > 0) {
				this.mPullDistanceFraction = 0;
				this.ensurePullDistance();
			}
		}
	}

	/**
	 * Returns the value of the maximum pull distance.
	 * <p>
	 * Default value: see {@link #getPullDistanceFraction()}
	 *
	 * @return Maximum pull distance in pixels or {@code -1} if the pull distance cannot be right now
	 * resolved due to not initialized size of the attached view yet.
	 * @see #setPullDistance(float)
	 */
	public float getPullDistance() {
		return ensurePullDistance() ? mPullDistance : -1;
	}

	/**
	 * Sets the fraction for the maximum pull distance which determines how much can be the attached
	 * view pulled. The specified fraction will be used to compute maximum pull distance from the
	 * size of the view.
	 *
	 * @param fraction The desired fraction from the range {@code [0, 1f]}.
	 * @see R.attr#uiPullDistanceFraction ui:uiPullDistanceFraction
	 * @see #getPullDistanceFraction()
	 * @see #setPullDistance(float)
	 */
	public void setPullDistanceFraction(@FloatRange(from = 0, to = 1) float fraction) {
		if (mPullDistanceFraction != fraction && fraction >= 0 && fraction <= 1) {
			if ((mPullDistanceFraction = fraction) > 0) {
				this.mPullDistance = 0;
				this.ensurePullDistance();
			}
		}
	}

	/**
	 * Returns the fraction used to compute maximum pull distance.
	 * <p>
	 * Default value: <b>0.25f</b>
	 *
	 * @return Pull fraction from the range {@code [0, 1f]}.
	 * @see #setPullDistanceFraction(float)
	 */
	@FloatRange(from = 0, to = 1)
	public float getPullDistanceFraction() {
		return mPullDistanceFraction;
	}

	/**
	 * Ensures that {@link #mPullDistance} is properly initialized.
	 *
	 * @return {@code True} if pull distance has been resolved, {@code false} otherwise.
	 */
	private boolean ensurePullDistance() {
		this.ensurePullHelper();
		final int pullableSize = mPullHelper.getViewSize();
		if (pullableSize > 0) {
			if (mPullDistance <= 0 && mPullDistanceFraction > 0) {
				this.mPullDistance = Math.round(pullableSize * mPullDistanceFraction);
			} else {
				this.mPullDistance = Math.min(mPullDistance, pullableSize);
			}
			return true;
		}
		return false;
	}

	/**
	 * Sets the transformer used to transform computed value for the current pull.
	 *
	 * @param transformer The desired transformer.
	 * @see #getPullTransformer()
	 */
	public void setPullTransformer(@NonNull PullTransformer transformer) {
		this.mPullTransformer = transformer;
	}

	/**
	 * Returns the current pull transformer of this PullController.
	 * <p>
	 * By default, this controller uses transformer implementation with {@link universum.studios.android.ui.interpolator.ResistanceInterpolator}
	 * to transform computed pull offset value based on its current position.
	 *
	 * @return Pull transformer.
	 * @see #setPullTransformer(PullTransformer)
	 */
	@NonNull
	public PullTransformer getPullTransformer() {
		return mPullTransformer;
	}

	/**
	 * Sets the interpolator for the default {@link PullTransformer} of this controller.
	 * <p>
	 * <b>Note</b>, that this can be applied only in case, when there is still default pull transformer
	 * used. If there is custom transformer set, that transformer should handle interpolation logic.
	 *
	 * @param interpolator The desired interpolator for the current pull transformer.
	 */
	public void setPullTransformerInterpolator(@NonNull Interpolator interpolator) {
		if (mPullTransformer instanceof PullTransformerImpl) {
			((PullTransformerImpl) mPullTransformer).interpolator = interpolator;
		}
	}

	/**
	 * Checks whether the attached view that received the specified motion <var>event</var> should
	 * intercept the event or not.
	 *
	 * @param event The motion event used to determine if to intercept or not.
	 * @return {@code True} if the event should be intercepted by the attached view so it should
	 * return {@code true} from within its {@link android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)},
	 * {@code false} otherwise.
	 */
	public boolean shouldInterceptTouchEvent(@NonNull MotionEvent event) {
		this.ensurePullHelper();
		return mState == STATE_PULLING && (mPullHelper.isViewScrolledAtStart() || mPullHelper.isViewScrolledAtEnd());
	}

	/**
	 * Processes the specified motion <var>event</var> received by the attached view. Based on the
	 * data of the event, this controller will perform pull upon the view if appropriate.
	 *
	 * @param event The motion event to be processed.
	 * @return {@code True} if the event was processed so attached view should return {@code true}
	 * from within its {@link View#onTouchEvent(android.view.MotionEvent)}, {@code false} otherwise.
	 */
	public boolean processTouchEvent(@NonNull MotionEvent event) {
		VELOCITY_TRACKER.addMovement(event);
		boolean processed = false;
		this.ensurePullHelper();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if (mState == STATE_IDLE) {
					if (!mPullHelper.hasViewChildren() || (mPullHelper.isViewScrolledAtStart() || mPullHelper.isViewScrolledAtEnd())) {
						this.mState = STATE_READY_TO_PULL;
						mPullHelper.dispatchPullInitiated(event);
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				final boolean touchChanged = mPullHelper.hasTouchChanged(event);
				switch (mState) {
					case STATE_IDLE:
						this.mState = STATE_SCROLLING;
						break;
					case STATE_SCROLLING:
						if (!mPullHelper.hasViewChildren() || (mPullHelper.isViewScrolledAtStart() || mPullHelper.isViewScrolledAtEnd())) {
							this.mState = STATE_READY_TO_PULL;
							mPullHelper.dispatchPullInitiated(event);
						} else {
							break;
						}
					case STATE_READY_TO_PULL:
						if (mPullHelper.shouldStartPull(event)) {
							VELOCITY_TRACKER.computeCurrentVelocity(UiConfig.VELOCITY_UNITS);
							if (mPullHelper.isAllowedVelocity(VELOCITY_TRACKER, mPullMinVelocity)) {
								this.mState = STATE_PULLING;
								onPullStarted();
							}
						} else if (mPullHelper.shouldCancelPull(event)) {
							this.mState = STATE_IDLE;
						}
						break;
					case STATE_PULLING:
						final float pullOffset = mPullHelper.computePullOffset(event);
						if (mPullHelper.shouldExpandPull(event)) {
							processed = onExpandPullBy(pullOffset);
						} else if ((mPullHelper.shouldCollapsePull(event) && onCollapsePullBy(pullOffset)) || !touchChanged) {
							processed = true;
						}
						break;
				}
				if (mState != STATE_SCROLLING) {
					mPullHelper.dispatchPullPerformed(event);
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				VELOCITY_TRACKER.computeCurrentVelocity(UiConfig.VELOCITY_UNITS);
				mPullHelper.dispatchPullReleased(event);
				if (mState == STATE_PULLING) {
					onPullReleased(mPull, mPull / mPullDistance);
				} else {
					this.mState = STATE_IDLE;
				}
				break;
		}
		return processed;
	}

	/**
	 * Ensures that the pull helper is initialized.
	 */
	final void ensurePullHelper() {
		ensurePullHelper(PullHelper.START | PullHelper.END);
	}

	/**
	 * Ensures that the pull helper is initialized with the specified <var>pullMode</var> flags.
	 *
	 * @param pullMode The desired pull mode flags. One of {@link PullHelper#NONE}, {@link PullHelper#START},
	 *                 {@link PullHelper#END} or theirs combination.
	 */
	final void ensurePullHelper(int pullMode) {
		final int viewOrientation = mView.getOrientation();
		if (mPullHelper != null) {
			boolean updatePullHelper = false;
			switch (viewOrientation) {
				case Orientation.VERTICAL:
					updatePullHelper = mPullHelper instanceof HorizontalPullHelper;
					break;
				case Orientation.HORIZONTAL:
					updatePullHelper = mPullHelper instanceof VerticalPullHelper;
					break;
			}

			if (updatePullHelper) {
				final int restorePullMode = mPullHelper.getPullMode();
				this.mPullHelper = null;
				this.ensurePullHelper(restorePullMode);
			}
			return;
		}
		switch (viewOrientation) {
			case Orientation.HORIZONTAL:
				this.mPullHelper = new HorizontalPullHelper(mView);
				break;
			default:
				this.mPullHelper = new VerticalPullHelper(mView);
				break;
		}
		mPullHelper.setPullMode(pullMode);
		onPullHelperChanged(mPullHelper);


	}

	/**
	 * Invoked whenever a new instance of PullHelper has been initialized for this controller.
	 *
	 * @param helper The new helper that will be used by this controller.
	 */
	void onPullHelperChanged(PullHelper helper) {
	}

	/**
	 * Invoked from {@link #processTouchEvent(MotionEvent)} whenever there has been observed touch
	 * motion that initiated the pull gesture. This is basically motion event with <b>DOWN</b> followed
	 * by <b>MOVE</b> action.
	 */
	protected void onPullStarted() {
		this.ensurePullDistance();
	}

	/**
	 * Invoked from {@link #processTouchEvent(MotionEvent)} whenever there has been observed touch
	 * motion that should expand the current pull. This is basically motion event with <b>MOVE</b>
	 * action directed from the pull origin.
	 * <p>
	 * Default implementation updates the current pull via {@link #pullBy(float)} method with the
	 * specified <var>offset</var> and in case of pull overflow invokes {@link #onPullOverflow(float, float, float)}
	 * with the computed overflow and the current pull position.
	 *
	 * @param offset The offset by which to expand the current pull.
	 * @return Always {@code true}.
	 */
	protected boolean onExpandPullBy(float offset) {
		if (Math.abs(mPull) < mPullDistance) {
			pullBy(offset);
		} else {
			this.mPullOverflow += mPullTransformer.transform(offset, mPullOverflow / mPullDistance);
			onPullOverflow(mPull, mPullOverflow, mPullOverflow / mPullDistance);
		}
		return true;
	}

	/**
	 * Invoked whenever {@link #onExpandPullBy(float)} is called and the current pull value is already
	 * equal to {@link #getPullDistance()} so the offset of the attached view is not being updated.
	 *
	 * @param pull     The current pull value. Equals to {@link #getPullDistance()}.
	 * @param overflow The new overflow value. With the value of pull this is the total distance pulled
	 *                 by a user.
	 * @param position The current pull position determined from the current pull + pull overflow and
	 *                 available pull distance.
	 */
	protected void onPullOverflow(float pull, float overflow, float position) {
	}

	/**
	 * Invoked from {@link #processTouchEvent(MotionEvent)} whenever there has been observed touch
	 * motion that should collapse the current pull. This is basically motion event with <b>MOVE</b>
	 * action directed to the pull origin.
	 * <p>
	 * Default implementation updates the current pull via {@link #pullBy(float)} method with the
	 * specified <var>offset</var> and in case of collapsed pull invokes {@link #onPullCollapsed()}.
	 *
	 * @param offset The offset by which to collapse the current pull.
	 * @return {@code True} if there remained some pull to be collapsed yet, {@code false} otherwise.
	 */
	protected boolean onCollapsePullBy(float offset) {
		final boolean positive = mPull > 0;
		if (Math.abs(mPull) <= mPullDistance) {
			this.mPullOverflow = 0;
		}
		pullBy(offset);
		if ((mPull > 0 && !positive) || (mPull < 0 && positive)) {
			this.mState = STATE_IDLE;
			this.ensurePullHelper();
			mPullHelper.dispatchPullCollapsed();
			this.mPull = mPullOverflow = 0;
			onPullCollapsed();
			return false;
		}
		return true;
	}

	/**
	 * Performs pull by the given <var>offset</var> using {@link universum.studios.android.ui.controller.PullHelper}
	 * implementation specific for orientation of the attached view.
	 *
	 * @param offset The offset by which to pull.
	 */
	protected final void pullBy(float offset) {
		if (offset != 0) {
			setViewFrozen(true);
			this.mPull += mPullTransformer.transform(offset, mPull / mPullDistance);
			int nextPull = (int) mPull;
			if (nextPull != mPullHelper.mPullOffset) {
				if (Math.abs(nextPull) > mPullDistance) {
					nextPull = (int) (nextPull > 0 ? mPullDistance : -mPullDistance);
					this.mPull = nextPull;
				}
				onApplyPull(nextPull, mPull / mPullDistance);
			}
		}
	}

	/**
	 * Invoked whenever {@link #pullBy(float)} is called with a <b>none zero</b> <var>offset</var>
	 * and the new computed pull offset is different from the current one.
	 * <p>
	 * Default implementation offsets the attached view at top/bottom or left/right depending on its
	 * orientation.
	 *
	 * @param pull     The pull offset to apply to the attached view.
	 * @param position New pull position determined from the new pull and available pull distance.
	 */
	protected void onApplyPull(int pull, float position) {
		this.ensurePullHelper();
		mPullHelper.setPullOffset(pull);
	}

	/**
	 * Sets the attached view frozen/un-frozen so it cannot to respond to user touches while the pull
	 * is being performed.
	 *
	 * @param frozen {@code True} to froze (disable, set un-pressed) the view, {@code false} otherwise.
	 */
	protected void setViewFrozen(boolean frozen) {
		if (frozen && mView.isPressed()) {
			mView.setPressed(false);
		}
		if (mView.isEnabled() == frozen) {
			mView.setEnabled(!frozen);
		}
	}

	/**
	 * Invoked whenever a user releases the attached view during the pull gesture.
	 * <p>
	 * Default implementation calls {@link #collapsePull()} but inherited controllers may for example
	 * start here animation to collapse the specified pull for better user experience.
	 *
	 * @param pull     Current pull value.
	 * @param position Current pull position determined from the current pull and available pull distance.
	 */
	protected void onPullReleased(float pull, float position) {
		collapsePull();
	}

	/**
	 * Collapses the current pull if there is any via {@link #onCollapsePull(float, float)} or notifies
	 * that the pull is already collapsed via {@link #onPullCollapsed()}.
	 */
	protected final void collapsePull() {
		if (mPull != 0) {
			this.mState = STATE_RELEASED;
			onCollapsePull(mPull, mPull / mPullDistance);
		} else {
			this.handlePullCollapsed();
		}
	}

	/**
	 * Invoked whenever {@link #collapsePull()} is called and there is currently some pull value to
	 * be collapsed.
	 *
	 * @param pull     Current pull value to collapse.
	 * @param position Current pull position determined from the current pull and available pull distance.
	 */
	protected void onCollapsePull(float pull, float position) {
		this.handlePullCollapsed();
	}

	/**
	 * Handles the state when the pull has been fully collapsed. Will also invoke {@link #onPullCollapsed()}.
	 */
	final void handlePullCollapsed() {
		this.mState = STATE_IDLE;
		this.ensurePullHelper();
		mPullHelper.dispatchPullCollapsed();
		this.mPull = mPullOverflow = 0;
		onPullCollapsed();
	}

	/**
	 * Invoked whenever {@link #handlePullCollapsed()} is called. In most cases this will be due to
	 * fact that value of the pull has been decreased to {@code 0}.
	 */
	protected void onPullCollapsed() {
		setViewFrozen(false);
	}

	/**
	 * Delegate method for {@link View#postDelayed(Runnable, long)} of the attached view.
	 */
	final void postDelayed(Runnable action, int delay) {
		mView.postDelayed(action, delay);
	}

	/**
	 * Updates the current private flags.
	 *
	 * @param flag Value of the desired flag to add/remove to/from the current private flags.
	 * @param add  Boolean flag indicating whether to add or remove the specified <var>flag</var>.
	 */
	@SuppressWarnings("unused")
	void updatePrivateFlags(int flag, boolean add) {
		if (add) this.mPrivateFlags |= flag;
		else this.mPrivateFlags &= ~flag;
	}

	/**
	 * Returns a boolean flag indicating whether the specified <var>flag</var> is contained within
	 * the current private flags or not.
	 *
	 * @param flag Value of the flag to check.
	 * @return {@code True} if the requested flag is contained, {@code false} otherwise.
	 */
	@SuppressWarnings("unused")
	boolean hasPrivateFlag(int flag) {
		return (mPrivateFlags & flag) != 0;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Default implementation of PullTransformer. This transformer uses {@link ResistanceInterpolator}
	 * to compute pull offset transformation.
	 */
	private static final class PullTransformerImpl implements PullTransformer {

		/**
		 * Interpolator used when transforming pull offset.
		 */
		Interpolator interpolator = new ResistanceInterpolator();

		/**
		 */
		@Override
		public float transform(float offset, float position) {
			if (interpolator != null) {
				final float absPosition = Math.abs(position);
				return offset * (1 + (interpolator.getInterpolation(absPosition) - absPosition));
			}
			return offset;
		}
	}
}
