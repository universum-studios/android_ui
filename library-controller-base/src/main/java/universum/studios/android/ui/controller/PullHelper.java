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

import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import universum.studios.android.ui.widget.Scrollable;

/**
 * Helper class used to handle core pull logic for {@link BasePullController}
 * that is used within scrollable views, mainly collections like {@link android.widget.ListView},
 * to support pulling at start and at end of these views.
 * <p>
 * This class represents only base implementation with API required by BasePullController to properly
 * pull a view to which is attached to. See {@link universum.studios.android.ui.controller.VerticalPullHelper}
 * and {@link universum.studios.android.ui.controller.HorizontalPullHelper} for orientation based
 * implementation details.
 *
 * @author Martin Albedinsky
 */
abstract class PullHelper {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "PullHelper";

	/**
	 * Flag for {@link #setPullMode(int)} to indicate that neither start or end pull are enabled.
	 */
	static final int NONE = 0x00000000;

	/**
	 * Flag for {@link #setPullMode(int)} to indicate that the pull at the start of a pullable view
	 * is enabled.
	 */
	static final int START = 0x00000001;

	/**
	 * Flag for {@link #setPullMode(int)} to indicate that the pull at the end of a pullable view
	 * is enabled.
	 */
	static final int END = 0x00000002;

	/**
	 * State determining that this helper is in idle state, so it is not offsetting the attached view.
	 */
	static final int STATE_IDLE = 0x00;

	/**
	 * State determining that this helper is offsetting the attached view at its start. Start is determined
	 * by the attached view's orientation ({@link universum.studios.android.ui.widget.Pullable#getOrientation()}).
	 */
	static final int STATE_PULLING_AT_START = 0x01;

	/**
	 * State determining that this helper is offsetting the attached view at its end. End is determined
	 * by the attached view's orientation ({@link universum.studios.android.ui.widget.Pullable#getOrientation()}).
	 */
	static final int STATE_PULLING_AT_END = 0x02;

	/**
	 * Flag indicating whether the pull at the start is visible or not. This determines whether this
	 * helper should really offset the attached view at its start to show current pull.
	 */
	private static final int PFLAG_START_PULL_VISIBLE = 0x01;

	/**
	 * Flag indicating whether the pull at the end is visible or not. This determines whether this
	 * helper should really offset the attached view at its end to show current pull.
	 */
	private static final int PFLAG_END_PULL_VISIBLE = 0x02;

	/**
	 * Minimum delta of pull required to be direction of pull changed ind DP units.
	 */
	private static final int MIN_DELTA_TO_CHANGE_PULL_DIRECTION = 3;

	/**
	 * Maximum delta of pull which will be handled by this helper in DP units.
	 */
	private static final int MAX_PULL_DELTA = 4;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * View with which has been this helper initialized.
	 */
	final View mView;

	/**
	 * Minimum delta of pull required to be direction of pull changed in pixels.
	 */
	final float mMinDeltaToChangePullDirection;

	/**
	 * Maximum delta of pull which will be handled by this helper in pixels. This means that this
	 * helper will offset the attached view maximum by this value at the time.
	 */
	final float mMaxPullDelta;

	/**
	 * Current value of pull determined by offset of the attached view.
	 */
	int mPullOffset;

	/**
	 * Current state of this helper.
	 */
	int mState = STATE_IDLE;

	/**
	 * Mode determining which pull (offset) should be really handled by this helper by offsetting
	 * the attached view.
	 */
	int mMode = START | END;

	/**
	 * Set of private flags specific for this helper.
	 */
	private int mPrivateFlags = PFLAG_START_PULL_VISIBLE | PFLAG_END_PULL_VISIBLE;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of PullHelper for the given <var>view</var>.
	 *
	 * @param view The view of which offset will be managed by this helper.
	 */
	<V extends View & Scrollable> PullHelper(V view) {
		this.mView = view;
		final Resources resources = view.getResources();
		final float density = resources.getDisplayMetrics().density;
		this.mMinDeltaToChangePullDirection = MIN_DELTA_TO_CHANGE_PULL_DIRECTION * density;
		this.mMaxPullDelta = MAX_PULL_DELTA * density;
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Returns the view with which has been this helper initialized.
	 *
	 * @return The attached view.
	 */
	final View accessView() {
		return mView;
	}

	/**
	 * Sets the pull mode flags used to determine which pull to handle and which to not.
	 *
	 * @param mode The desired mode flags.
	 */
	final void setPullMode(int mode) {
		switch (mode) {
			case NONE:
			case START:
			case END:
			case START | END:
				this.mMode = mode;
		}
	}

	/**
	 * Returns the current pull mode flags.
	 *
	 * @return Mode flags set by {@link #setPullMode(int)} or ({@link #START} | {@link #END})
	 * by default.
	 */
	final int getPullMode() {
		return mMode;
	}

	/**
	 * Sets the visibility flags used to determine which pull to show and which to not.
	 *
	 * @param pull    The desired pull flags.
	 * @param visible {@code True} to show pull for the given flags, {@code false} otherwise.
	 */
	final void setPullVisibility(int pull, boolean visible) {
		switch (pull) {
			case START:
				this.updatePrivateFlags(PFLAG_START_PULL_VISIBLE, visible);
				break;
			case END:
				this.updatePrivateFlags(PFLAG_END_PULL_VISIBLE, visible);
				break;
			case START | END:
				this.updatePrivateFlags(PFLAG_START_PULL_VISIBLE, visible);
				this.updatePrivateFlags(PFLAG_END_PULL_VISIBLE, visible);
				break;
		}
	}

	/**
	 * Returns a flag indicating whether the pull for the specified <var>pull</var> flags is visible
	 * (this helper is really offsetting the attached view) or not.
	 *
	 * @param pull The desired pull flags of which visibility to resolve.
	 * @return {@code True} if pull for the specified flags is visible, {@code false} otherwise.
	 */
	final boolean isPullVisible(int pull) {
		switch (pull) {
			case START:
				return hasPrivateFlag(PFLAG_START_PULL_VISIBLE);
			case END:
				return hasPrivateFlag(PFLAG_END_PULL_VISIBLE);
			case START | END:
				return hasPrivateFlag(PFLAG_START_PULL_VISIBLE) && hasPrivateFlag(PFLAG_END_PULL_VISIBLE);
		}
		return false;
	}

	/**
	 * Offsets the attached view by the specified <var>offset</var>.
	 * <p>
	 * Note, that orientation (vertical/horizontal) of offset will depends on this pull helper implementation.
	 *
	 * @param offset The desired offset for the attached view.
	 */
	final void setPullOffset(int offset) {
		if (mPullOffset != offset) {
			final int offDelta = offset - mPullOffset;
			if (offDelta != 0) {
				offsetViewBy(offDelta);
				this.mPullOffset = offset;
			}
		}
	}

	/**
	 * Clears the current offset of the attached view.
	 */
	final void clearPullOffset() {
		offsetViewBy(-mPullOffset);
		this.mPullOffset = 0;
	}

	/**
	 * Called to offset the attached view by the specified offset.
	 *
	 * @param offset The offset by which to offset the attached view.
	 */
	abstract void offsetViewBy(int offset);

	/**
	 * Checks whether the attached view has some children or not.
	 *
	 * @return {@code True} if the view is a {@link android.view.ViewGroup} and has some children,
	 * {@code false} otherwise.
	 */
	final boolean hasViewChildren() {
		return mView instanceof ViewGroup && ((ViewGroup) mView).getChildCount() > 0;
	}

	/**
	 * Checks whether the attached view is at its start (of scrollable content) or not.
	 *
	 * @return {@code True} if the view is at the start of scroll, {@code false} otherwise.
	 */
	final boolean isViewScrolledAtStart() {
		return ((Scrollable) mView).isScrolledAtStart();
	}

	/**
	 * Checks whether the attached view is at its end (of scrollable content) or not.
	 *
	 * @return {@code True} if the view is at the end of scroll, {@code false} otherwise.
	 */
	final boolean isViewScrolledAtEnd() {
		return ((Scrollable) mView).isScrolledAtEnd();
	}

	/**
	 * Changes state of this helper.
	 *
	 * @param state The state to be changed. One of {@link #STATE_IDLE}, {@link #STATE_PULLING_AT_START}
	 *              or {@link #STATE_PULLING_AT_END}.
	 */
	final void changeState(int state) {
		this.mState = state;
	}

	/**
	 * Called to dispatch that a motion event upon the attached view has been initiated.
	 *
	 * @param event The initiated motion event.
	 */
	void dispatchPullInitiated(MotionEvent event) {
		changeState(isViewScrolledAtStart() ? STATE_PULLING_AT_START : STATE_PULLING_AT_END);
	}

	/**
	 * Called to dispatch tat a motion event upon the attached view has been performed.
	 *
	 * @param event The motion event for performed pull.
	 */
	void dispatchPullPerformed(MotionEvent event) {
	}

	/**
	 * Called to dispatch release (UP/CANCEL) motion even upon the attached view.
	 *
	 * @param event The motion event action of {@link MotionEvent#ACTION_UP} or {@link MotionEvent#ACTION_CANCEL}.
	 */
	void dispatchPullReleased(MotionEvent event) {
	}

	/**
	 * Called after the pull of the attached view has been collapsed by running animation or just by a user.
	 */
	void dispatchPullCollapsed() {
		clearPullOffset();
		changeState(STATE_IDLE);
	}

	/**
	 * Returns the size of the attached view. This will be view's width or height depends on this
	 * helper's implementation.
	 *
	 * @return View's size.
	 */
	abstract int getViewSize();

	/**
	 * Checks whether the specified <var>tracker</var> contains velocity which is allowed depends
	 * on the given <var>minVelocity</var>.
	 *
	 * @param tracker     The tracker with velocity to check.
	 * @param minVelocity The min velocity to which to check against.
	 * @return {@code True} if velocity of the tracker can be processed, {@code false} otherwise.
	 */
	abstract boolean isAllowedVelocity(VelocityTracker tracker, float minVelocity);

	/**
	 * Called to check if pull should be started for the specified motion <var>event</var>.
	 *
	 * @param event The motion event to check.
	 * @return {@code True} if pull should be started, {@code false} otherwise.
	 */
	abstract boolean shouldStartPull(MotionEvent event);

	/**
	 * Called to check if pull should be canceled for the specified motion <var>event</var>.
	 *
	 * @param event The motion event to check.
	 * @return {@code True} if pull should be cancelled, {@code false} otherwise.
	 */
	abstract boolean shouldCancelPull(MotionEvent event);

	/**
	 * Called to compute pull offset for the specified motion <var>event</var>.
	 *
	 * @param event The motion event to be used for pull computation.
	 * @return Computed pull offset for the given motion event.
	 */
	abstract float computePullOffset(MotionEvent event);

	/**
	 * Called to check if pull should be expanded or not. This determines whether to offset the attached
	 * view from its start/end or not.
	 *
	 * @param event The motion event to check.
	 * @return {@code True} if pull should be still expanded, {@code false} otherwise.
	 */
	abstract boolean shouldExpandPull(MotionEvent event);

	/**
	 * Called to check if pull should be collapsed or not. This determines whether to offset the attached
	 * view to its start/end or not.
	 *
	 * @param event The motion event to check.
	 * @return {@code True} if pull should be still collapsed, {@code false} otherwise.
	 */
	abstract boolean shouldCollapsePull(MotionEvent event);

	/**
	 * Checks whether the specified motion <var>event</var> has changed since the last one passed
	 * to be processed by this helper or not.
	 *
	 * @param event The motion event to check.
	 * @return {@code True} if the event contains data which are different from the last one specific
	 * for this helper implementation,  {@code false} otherwise.
	 */
	abstract boolean hasTouchChanged(MotionEvent event);

	/**
	 * Called to compute over-scroll distance for the given scroll data.
	 *
	 * @param scrollX  Current scroll X of the attached view.
	 * @param scrollY  Current scroll Y of the attached view.
	 * @param clampedX {@code True} if <var>scrollX</var> was clamped to an over-scroll boundary,
	 *                 {@code false} otherwise.
	 * @param clampedY {@code True} if <var>scrollY</var> was clamped to an over-scroll boundary,
	 *                 {@code false} otherwise.
	 * @param tracker  Velocity tracker with data of the current touch's velocity.
	 * @param units    Velocity units for the specified tracker.
	 * @return Computed over-scroll distance or {@code 0} if no over-scroll has been captured.
	 */
	abstract float computeOverScroll(int scrollX, int scrollY, boolean clampedX, boolean clampedY, VelocityTracker tracker, int units);

	/**
	 * Updates the current private flags.
	 *
	 * @param flag Value of the desired flag to add/remove to/from the current private flags.
	 * @param add  Boolean flag indicating whether to add or remove the specified <var>flag</var>.
	 */
	@SuppressWarnings("unused")
	private void updatePrivateFlags(int flag, boolean add) {
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
	private boolean hasPrivateFlag(int flag) {
		return (mPrivateFlags & flag) != 0;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
