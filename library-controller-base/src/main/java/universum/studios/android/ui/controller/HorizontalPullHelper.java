/*
 * =================================================================================================
 *                    Copyright (C) 2017 Universum Studios [Wolf-ITechnologies]
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

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import universum.studios.android.ui.widget.Orientation;
import universum.studios.android.ui.widget.Scrollable;

/**
 * Implementation of {@link PullHelper} to support pulling of
 * scrollable views that has {@link Orientation#HORIZONTAL} orientation like {@link android.support.v4.view.ViewPager}
 * or {@link android.widget.HorizontalScrollView}.
 * <p>
 * The pull is performed on these views by offsetting theirs original left/right positions via
 * {@link View#offsetLeftAndRight(int)} by deltas computed from the {@link android.view.MotionEvent}s
 * passed to the VerticalPullHelper for the changes in {@link android.view.MotionEvent#getX()}.
 *
 * @author Martin Albedinsky
 */
final class HorizontalPullHelper extends PullHelper {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "HorizontalPullHelper";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Last touch X coordinate used to compute delta against the new touch event.
	 */
	private float mTouchLastX = 0;

	/**
	 * Flag indicating whether this helper is expanding pull of the pullable view or collapsing it.
	 */
	private boolean mExpandingPull;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of VerticalPullHelper to manage horizontal pull offsetting of the given
	 * <var>view</var>.
	 *
	 * @param view The view of which pull to manage.
	 */
	<V extends View & Scrollable> HorizontalPullHelper(V view) {
		super(view);
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 */
	@Override
	int getViewSize() {
		return mView.getWidth();
	}

	/**
	 */
	@Override
	void offsetViewBy(int offset) {
		switch (mState) {
			case STATE_PULLING_AT_START:
				if (isPullVisible(START)) {
					mView.offsetLeftAndRight(offset);
				}
				break;
			case STATE_PULLING_AT_END:
				if (isPullVisible(END)) {
					mView.offsetLeftAndRight(offset);
				}
				break;
		}
	}

	/**
	 */
	@Override
	boolean isAllowedVelocity(VelocityTracker tracker, float minVelocity) {
		return Math.abs(tracker.getXVelocity()) >= minVelocity;
	}

	/**
	 */
	@Override
	void dispatchPullInitiated(MotionEvent event) {
		super.dispatchPullInitiated(event);
		switch (mState) {
			case STATE_PULLING_AT_START:
				this.mExpandingPull = mTouchLastX <= event.getX();
			case STATE_PULLING_AT_END:
				this.mExpandingPull = mTouchLastX > event.getX();
		}
		this.mTouchLastX = event.getX();
	}

	/**
	 */
	@Override
	void dispatchPullPerformed(MotionEvent event) {
		super.dispatchPullPerformed(event);
		this.updateTouchLastX(event);
	}

	/**
	 * Updates a value of the last touch X coordinate from the specified motion <var>event</var>.
	 *
	 * @param event The motion event from which to obtain X coordinate.
	 */
	private void updateTouchLastX(MotionEvent event) {
		this.mTouchLastX = event.getX();
	}

	/**
	 */
	@Override
	void dispatchPullCollapsed() {
		super.dispatchPullCollapsed();
		this.mTouchLastX = 0;
	}

	/**
	 */
	@Override
	boolean shouldStartPull(MotionEvent event) {
		switch (mState) {
			case STATE_PULLING_AT_START:
				return (mMode & START) != 0 && mTouchLastX < event.getX();
			case STATE_PULLING_AT_END:
				return (mMode & END) != 0 && mTouchLastX > event.getX();
		}
		return false;
	}

	/**
	 */
	@Override
	float computePullOffset(MotionEvent event) {
		final float delta = event.getX() - mTouchLastX;
		final float absDelta = Math.abs(delta);
		// Remove the shaking effect during slow pull.
		switch (mState) {
			case STATE_PULLING_AT_START:
				if ((mExpandingPull && delta < 0 && absDelta < mMinDeltaToChangePullDirection) ||
						(!mExpandingPull && delta > 0 && absDelta < mMinDeltaToChangePullDirection)) {
					this.mExpandingPull = mTouchLastX < event.getX();
					return 0;
				}
				this.mExpandingPull = mTouchLastX < event.getX();
				break;
			case STATE_PULLING_AT_END:
				if ((mExpandingPull && delta > 0 && absDelta < mMinDeltaToChangePullDirection) ||
						(!mExpandingPull && delta < 0 && absDelta < mMinDeltaToChangePullDirection)) {
					this.mExpandingPull = mTouchLastX > event.getX();
					return 0;
				}
				this.mExpandingPull = mTouchLastX > event.getX();
		}
		return Math.min(absDelta, mMaxPullDelta) * (delta > 0 ? 1 : -1);
	}

	/**
	 */
	@Override
	boolean shouldExpandPull(MotionEvent event) {
		switch (mState) {
			case STATE_PULLING_AT_START:
				return event.getX() > mTouchLastX;
			case STATE_PULLING_AT_END:
				return event.getX() < mTouchLastX;
		}
		return false;
	}

	/**
	 */
	@Override
	boolean shouldCollapsePull(MotionEvent event) {
		switch (mState) {
			case STATE_PULLING_AT_START:
				return event.getX() < mTouchLastX;
			case STATE_PULLING_AT_END:
				return event.getX() > mTouchLastX;
		}
		return false;
	}

	/**
	 */
	@Override
	boolean shouldCancelPull(MotionEvent event) {
		switch (mState) {
			case STATE_PULLING_AT_START:
				return mTouchLastX > event.getX();
			case STATE_PULLING_AT_END:
				return mTouchLastX < event.getX();
		}
		return true;
	}

	/**
	 */
	@Override
	boolean hasTouchChanged(MotionEvent event) {
		return event.getX() != mTouchLastX;
	}

	/**
	 */
	@Override
	float computeOverScroll(int scrollX, int scrollY, boolean clampedX, boolean clampedY, VelocityTracker tracker, int units) {
		if (scrollX != 0 && clampedX) {
			return tracker.getXVelocity() / (units * 0.05f);
		}
		return 0;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
