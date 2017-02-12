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

import android.support.annotation.NonNull;

import universum.studios.android.ui.R;

/**
 * Interface for <b>pullable</b> views. Specifies API for supporting the pull feature for view groups
 * of which content can be pulled down/up or right/left by a user. Enabling the pull feature for the
 * pullable view can be done via {@link #setPullEnabled(boolean)}. Whether to pull the pullable view
 * horizontally or vertically is determined by orientation flag returned by the pullable view from
 * {@link #getOrientation()} method.
 * <p>
 * <b>Note, that only scrollable views like {@link android.widget.ListView ListView} or
 * {@link android.widget.HorizontalScrollView HorizontalScrollView} should support this feature.</b>
 *
 * @author Martin Albedinsky
 */
public interface Pullable extends Scrollable {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * A layer that provides access to current pull data.
	 *
	 * @author Martin Albedinsky
	 */
	interface Pull {

		/**
		 * Returns a position of the current pull. This can be either positive or negative value.
		 * <p>
		 * Positive position indicates pull at the start, negative indicates pull at the end of the
		 * pullable view.
		 *
		 * @return Pull position from the range [0, 1] or [-1, 0].
		 */
		float getPosition();

		/**
		 * Returns a position of the current pull overflow. This can be also either positive or negative
		 * value (see {@link #getPosition()} for info).
		 * <p>
		 * Sum of the <b>position + overflow position</b> results into total pull position.
		 *
		 * @return Pull overflow position from the range
		 */
		float getPullOverflowPosition();

		/**
		 * Collapses the current pull via animation.
		 *
		 * @return {@code True} if collapse animation has been started, {@code false} if it is already
		 * running.
		 */
		boolean collapse();
	}

	/**
	 * Listener which can receive callbacks about the <b>started</b>, <b>performed</b>, <b>released</b>
	 * or <b>collapsed</b> pull.
	 *
	 * @author Martin Albedinsky
	 */
	interface OnPullListener {

		/**
		 * Invoked whenever a user initiates (by touch) the pull upon the specified <var>pullable</var>
		 * view.
		 *
		 * @param pullable The view upon which has been pull started.
		 * @param pull     The initiated pull.
		 */
		void onPullStarted(@NonNull Pullable pullable, @NonNull Pull pull);

		/**
		 * Invoked whenever a user performs (by touch) the pull upon the specified <var>pullable</var>
		 * view.
		 * <p>
		 * <b>Note</b>, that position of the pull can be either positive or negative. Positive indicates
		 * pull at the <b>start</b> of the pullable view, negative indicates pull at the <b>end</b>
		 * of the pullable view. Start/End are determined by the orientation ({@link universum.studios.android.ui.widget.Pullable#getOrientation()})
		 * of the pullable view.
		 *
		 * @param pullable The view upon which has been pull performed.
		 * @param pull     The performed pull.
		 */
		void onPull(@NonNull Pullable pullable, @NonNull Pull pull);

		/**
		 * Invoked whenever a user releases the pull upon the specified <var>pullable</var> view.
		 * <p>
		 * <b>Note</b>, that by default the PullController implementation will start animation to
		 * collapse the current pull before this callback is fired. This behaviour cannot be for now
		 * changed/cancelled, but can be changed in the feature.
		 *
		 * @param pullable The view upon which has been pull released.
		 * @param pull     The released pull.
		 */
		void onPullReleased(@NonNull Pullable pullable, @NonNull Pull pull);

		/**
		 * Invoked whenever the current pull of the specified <var>pullable</var> view has been collapsed.
		 * This can be due to finished collapse animation after the pull has been released or a user
		 * moved the pull back to its origin.
		 */
		void onPullCollapsed(@NonNull Pullable pullable);
	}

	/**
	 * Listener which can receive callback about the <b>pull overflow</b>. The pull overflow value
	 * is generated whenever the current pull has reached its maximum available size and user is still
	 * moving its finger across the pullable view to acquire larger pull.
	 * <p>
	 * I cannot to image situations where this can be really helpful, but that does not mean there
	 * are none.
	 *
	 * @author Martin Albedinsky
	 */
	interface OnPullOverflowListener {

		/**
		 * Invoked whenever a user performs (by touch) overflow of the current pull upon the specified
		 * <var>pullable</var> view.
		 * <p>
		 * For this callback are applied same principles as for {@link OnPullListener#onPull(Pullable, Pull)}.
		 *
		 * @param pullable         The view upon which has been pull overflow performed.
		 * @param pull             The performed pull.
		 */
		void onPullOverflow(@NonNull Pullable pullable, @NonNull Pull pull);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Sets a flag indicating whether the <b>pull</b> feature is enabled within this view or not.
	 * <p>
	 * Enabling this feature means, that a user can pull down/up or right/left this view depending
	 * on its orientation specified by {@link #getOrientation()}.
	 *
	 * @param enabled {@code True} to enable pull, {@code false} to disable it.
	 * @see R.attr#uiPullEnabled ui:uiPullEnabled
	 * @see #isPullEnabled()
	 */
	void setPullEnabled(boolean enabled);

	/**
	 * Returns a flag indicating whether the <b>pull</b> feature is enabled within this view or not.
	 *
	 * @return {@code True} if the pull enabled, so a user can pull content of this view, {@code false}
	 * otherwise.
	 * @see #setPullEnabled(boolean)
	 */
	boolean isPullEnabled();
}
