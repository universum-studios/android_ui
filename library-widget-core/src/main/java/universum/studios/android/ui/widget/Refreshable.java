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

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.ui.R;

/**
 * Interface for <b>refreshable</b> views. Specifies API for supporting the refresh feature for the
 * views of which content can be refreshed by a user using the <b>swipe to refresh pattern</b>.
 * Enabling the refresh feature for the Refreshable view can be done via {@link #setRefreshEnabled(boolean)}.
 * To show or hide an indicator showing that the refresh is currently running can be done via
 * {@link #setRefreshing(boolean)} and passing boolean flag to indicate whether to pop or dismiss
 * the indicator.
 *
 * @author Martin Albedinsky
 */
public interface Refreshable extends Scrollable {

	/*
	 * Interface ===================================================================================
	 */

	/**
	 * Listener which can receive callback about the initiated refresh.
	 *
	 * @author Martin Albedinsky
	 */
	interface OnRefreshListener {

		/**
		 * Invoked whenever a user releases the refresh indicator at or after the refresh position.
		 * At this point the refresh indicator stays spinning at its settle point to indicate, that
		 * the refresh is running.
		 * <p>
		 * When the refresh is done, don't forget to call {@link Refreshable#setRefreshing(boolean)}
		 * with {@code false} to dismiss the indicator.
		 *
		 * @param refreshable The refreshable view for which has been refresh initiated.
		 */
		void onRefresh(@NonNull Refreshable refreshable);
	}

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Defines an annotation for determining set of allowed transitions for refresh indicator of
	 * Refreshable widget.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({TRANSITION_NONE, TRANSITION_BELOW, TRANSITION_COPLANAR, TRANSITION_ABOVE})
	@interface IndicatorTransition {
	}

	/**
	 * Transition type for the refresh indicator indicating that no transition should be used when
	 * showing/hiding refresh indicator.
	 * <p>
	 * <b>This transition should not be used, it is here only for convenience and internal purpose.</b>
	 */
	int TRANSITION_NONE = 0x00;

	/**
	 * Transition type for the refresh indicator used for refreshing of a content that is
	 * <b>below another surface in z-space</b>.
	 * <p>
	 * This is basically any refreshable collection view ({@link android.widget.ListView ListView},
	 * {@link android.widget.GridView GridView}, ...) that has above self an {@link android.app.ActionBar ActionBar}
	 * or a {@link android.widget.Toolbar Toolbar} presented.
	 */
	int TRANSITION_BELOW = 0x01;

	/**
	 * <b>This transition type is not supported yet. Using this mode will resolve into same behaviour
	 * as described for {@link #TRANSITION_ABOVE} transition</b>.
	 * <p>
	 * Transition type for the refresh indicator used for refreshing of a content that is
	 * <b>coplanar with another surface</b>.
	 * <p>
	 * This is basically any refreshable collection view ({@link android.widget.ListView ListView},
	 * {@link android.widget.GridView GridView}, ...) that is presented as coplanar with another
	 * surface presented above it.
	 */
	int TRANSITION_COPLANAR = 0x02;

	/**
	 * Transition type for the refresh indicator used for refreshing of a content that is
	 * <b>above every other surface in z-space</b>.
	 * <p>
	 * This is basically any refreshable collection view ({@link android.widget.ListView ListView},
	 * {@link android.widget.GridView GridView}, ...) that is presented above a {@link android.widget.Toolbar Toolbar}
	 * and all surfaces in the current window's view hierarchy.
	 */
	int TRANSITION_ABOVE = 0x03;

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Sets a flag indicating whether the <b>refresh</b> feature is enabled for this view or not.
	 * <p>
	 * Enabling this feature means, that a user can swipe down this view to refresh it if
	 * {@link #isRefreshGestureEnabled()}, otherwise only refresh indicator can be shown/dismissed
	 * programmatically for this view via {@link #setRefreshing(boolean)} method.
	 *
	 * @param enabled {@code True} to enable refresh, {@code false} to disable it.
	 * @see R.attr#uiRefreshEnabled ui:uiRefreshEnabled
	 * @see #isRefreshEnabled()
	 * @see #setRefreshGestureEnabled(boolean)
	 */
	void setRefreshEnabled(boolean enabled);

	/**
	 * Returns a flag indicating whether the <b>refresh</b> feature is enabled for this view or not.
	 *
	 * @return {@code True} if the refresh is enabled, so the refresh indicator can be shown for this
	 * view, {@code false}  otherwise.
	 * @see #setRefreshEnabled(boolean)
	 * @see #isRefreshGestureEnabled()
	 */
	boolean isRefreshEnabled();

	/**
	 * Unlike {@link #setRefreshEnabled(boolean)} this will enable/disable the refresh gesture so a
	 * user can/cannot swipe down this view to refresh it.
	 *
	 * @param enabled {@code True} to enable refresh gesture, {@code false} to disable it.
	 * @see R.attr#uiRefreshGestureEnabled ui:uiRefreshGestureEnabled
	 * @see #isRefreshGestureEnabled()
	 */
	void setRefreshGestureEnabled(boolean enabled);

	/**
	 * Returns a flag indicating whether the <b>refresh</b> gesture is enabled for this view or not.
	 *
	 * @return {@code True} if the refresh gesture is enabled, so a user can swipe down this view to
	 * refresh its content, {@code false} otherwise.
	 * @see #setRefreshGestureEnabled(boolean)
	 * @see #setRefreshEnabled(boolean)
	 */
	boolean isRefreshGestureEnabled();

	/**
	 * Sets a flag indicating whether this view is being refreshing or not. This flag determines
	 * whether to show the refresh indicator for this view or not.
	 * <p>
	 * This should be called whenever
	 * the refresh for this view has been finished, so the indicator can be dismissed by {@code setRefreshing(false)},
	 * or whenever the refresh has been initiated by pressing a button, so the indicator should be
	 * popped by {@code #setRefreshing(true)}.
	 * <p>
	 * <b>Note</b>, that this does not need to be called from within {@link OnRefreshListener#onRefresh(Refreshable) OnRefreshListener.onRefresh(Refreshable)}
	 * callback, as during this callback is the refresh indicator already showing.
	 *
	 * @param refreshing {@code True} if this view is being refreshing, {@code false} otherwise.
	 * @see #isRefreshing()
	 */
	void setRefreshing(boolean refreshing);

	/**
	 * Returns a flag indicating whether this view is being refreshing or not.
	 *
	 * @return {@code True} if this view is being refreshing, so the refresh indicator is visible
	 * and spinning, {@code false} otherwise.
	 * @see #setRefreshing(boolean)
	 */
	boolean isRefreshing();

	/**
	 * Sets the transition type for the refresh indicator. The transition determines the way how
	 * the refresh indicator should be transitioned while it is being pulled up/down by a user.
	 * <p>
	 * <b>Note</b>, that for now {@link #TRANSITION_COPLANAR} and {@link #TRANSITION_ABOVE} behaves
	 * the same way. This is scheduled to be changed in the feature.
	 *
	 * @param transition The desired transition. One of {@link #TRANSITION_BELOW}, {@link #TRANSITION_COPLANAR}
	 *                   or {@link #TRANSITION_ABOVE}.
	 * @see R.attr#uiRefreshIndicatorTransition ui:uiRefreshIndicatorTransition
	 * @see #getRefreshIndicatorTransition()
	 */
	void setRefreshIndicatorTransition(@IndicatorTransition int transition);

	/**
	 * Returns the current transition type used for the refresh indicator hiding/showing.
	 *
	 * @return One of {@link #TRANSITION_BELOW}, {@link #TRANSITION_COPLANAR} or {@link #TRANSITION_ABOVE}.
	 * @see #setRefreshIndicatorTransition(int)
	 */
	@IndicatorTransition
	int getRefreshIndicatorTransition();

	/**
	 * Sets a boolean flag indicating whether this refreshable view should draw its refresh indicator
	 * as part of its own drawing or not.
	 * <p>
	 * This can be useful to draw refresh indicator outside of bounds of this view or at the different
	 * view hierarchy level if needed.
	 * <p>
	 * Default value: <b>{@code true}</b>
	 *
	 * @param draw {@code True} to draw indicator as part of this view's graphics, {@code false}
	 *             otherwise.
	 * @see #drawsRefreshIndicator()
	 */
	void setDrawRefreshIndicator(boolean draw);

	/**
	 * Returns boolean flag indicating whether this refreshable view draws its refresh indicator
	 * or not.
	 *
	 * @return {@code True} if refresh indicator is drawn by this view, {@code false} otherwise.
	 * @see #setDrawRefreshIndicator(boolean)
	 */
	boolean drawsRefreshIndicator();

	/**
	 * Registers a callback to be invoked whenever a user releases the refresh indicator at or after
	 * the refresh position.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	void setOnRefreshListener(@Nullable OnRefreshListener listener);
}
