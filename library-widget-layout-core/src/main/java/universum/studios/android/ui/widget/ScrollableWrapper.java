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

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * A ScrollableWrapper can be used for purpose of checking whether a specific scrollable view is
 * scrolled at its content's start or end at a time.
 * <p>
 * This wrapper is used for example by all widgets from the UI library that support <b>pull</b> and
 * thus also <b>refresh</b> feature.
 *
 * <h3>Supported scrollable views</h3>
 * <ul>
 * <li>{@link AbsListView}</li>
 * <li>{@link RecyclerView}</li>
 * <li>{@link ScrollView}</li>
 * <li>{@link HorizontalScrollView}</li>
 * <li>{@link ViewPager}</li>
 * </ul>
 * <b>Note that any other view supplied to {@link #wrapScrollableView(View)} will be wrapped into
 * simple wrapper implementation that always returns {@code true} from both {@link #isScrolledAtStart()}
 * and {@link #isScrolledAtEnd()} methods.</b>
 *
 * @author Martin Albedinsky
 */
public abstract class ScrollableWrapper<V extends View> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ScrollableWrapper";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Wrapped scrollable view.
	 */
	final V mScrollableView;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ScrollableWrapper for the given <var>scrollableView</var>.
	 *
	 * @param scrollableView The view that will be wrapped by this wrapper which will provide implementation
	 *                       of {@link #isScrolledAtStart()} and {@link #isScrolledAtEnd()} for it.
	 */
	private ScrollableWrapper(V scrollableView) {
		this.mScrollableView = scrollableView;
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Wraps the given <var>scrollableView</var> into scrollable wrapper implementation that best
	 * suits to the type of the given view.
	 *
	 * @param scrollableView The scrollable view to be wrapped into scrollable wrapper.
	 * @return New scrollable wrapper instance. See description of this class for supported wrappers.
	 */
	@SuppressWarnings("unchecked")
	public static <V extends View> ScrollableWrapper<V> wrapScrollableView(@NonNull V scrollableView) {
		if (scrollableView instanceof AbsListView) {
			return new AbsListViewWrapper((AbsListView) scrollableView);
		} else if (scrollableView instanceof RecyclerView) {
			return new RecyclerViewWrapper((RecyclerView) scrollableView);
		} else if (scrollableView instanceof ScrollView) {
			return new ScrollViewWrapper((ScrollView) scrollableView);
		} else if (scrollableView instanceof HorizontalScrollView) {
			return new HorizontalScrollViewWrapper((HorizontalScrollView) scrollableView);
		} else if (scrollableView instanceof ViewPager) {
			return new ViewPagerWrapper((ViewPager) scrollableView);
		} else if (scrollableView instanceof WebView) {
			return new WebViewWrapper((WebView) scrollableView);
		}
		return new ViewWrapper(scrollableView);
	}

	/**
	 * Checks whether a content of the wrapped scrollable view can be scrolled in any direction.
	 *
	 * @return {@code True} if the content can be scrolled, {@code false} otherwise.
	 */
	public boolean canScroll() {
		return !isScrolledAtStart() || !isScrolledAtEnd();
	}

	/**
	 * Checks whether a content of the wrapped scrollable view is at this time scrolled at its start.
	 *
	 * @return {@code True} if the content is scrolled at its start, {@code false} otherwise.
	 * @see #isScrolledAtEnd()
	 */
	public abstract boolean isScrolledAtStart();

	/**
	 * Checks whether a content of the wrapped scrollable view is at this time scrolled at its end.
	 *
	 * @return {@code True} if the content is scrolled at its end, {@code false} otherwise.
	 * @see #isScrolledAtStart()
	 */
	public abstract boolean isScrolledAtEnd();

	/**
	 * Returns the wrapped scrollable view.
	 *
	 * @return Scrollable view.
	 */
	@NonNull
	public V getScrollableView() {
		return mScrollableView;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Implementation of {@link ScrollableWrapper} to wrap {@link View}.
	 */
	private static final class ViewWrapper<V extends View> extends ScrollableWrapper<V> {

		/**
		 * Creates a new instance of ViewWrapper to wrap the given <var>view</var>.
		 *
		 * @param view The view to wrap.
		 */
		ViewWrapper(V view) {
			super(view);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtStart() {
			return true;
		}

		/**
		 */
		@Override
		public boolean isScrolledAtEnd() {
			return true;
		}
	}


	/**
	 * Implementation of {@link ScrollableWrapper} to wrap {@link AbsListView}.
	 */
	private static final class AbsListViewWrapper<V extends AbsListView> extends ScrollableWrapper<V> {

		/**
		 * Creates a new instance of AbsListViewWrapper to wrap the given <var>absListView</var>.
		 *
		 * @param absListView The abstract list view to wrap.
		 */
		private AbsListViewWrapper(V absListView) {
			super(absListView);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtStart() {
			if (mScrollableView.getChildCount() == 0) {
				return true;
			}

			if (mScrollableView.getFirstVisiblePosition() == 0) {
				final View firstChild = mScrollableView.getChildAt(0);
				return firstChild == null || firstChild.getTop() == mScrollableView.getPaddingTop();
			}
			return false;
		}

		/**
		 */
		@Override
		public boolean isScrolledAtEnd() {
			final int childCount = mScrollableView.getChildCount();
			if (childCount == 0) {
				return true;
			}

			final Adapter adapter = mScrollableView.getAdapter();
			final int adapterCount = adapter != null ? adapter.getCount() : 0;
			if (adapterCount == 0) {
				return true;
			}

			final int lastVisiblePos = mScrollableView.getLastVisiblePosition();
			if (lastVisiblePos == (adapterCount - 1)) {
				final View lastChild = mScrollableView.getChildAt(childCount - 1);
				return lastChild != null && lastChild.getBottom() == mScrollableView.getHeight();
			}
			return false;
		}
	}

	/**
	 * Implementation of {@link ScrollableWrapper} to wrap {@link RecyclerView}.
	 */
	private static final class RecyclerViewWrapper<V extends RecyclerView> extends ScrollableWrapper<V> {

		/**
		 * Creates a new instance of RecyclerViewWrapper to wrap the given <var>recyclerView</var>.
		 *
		 * @param recyclerView The recycler view to wrap.
		 */
		private RecyclerViewWrapper(V recyclerView) {
			super(recyclerView);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtStart() {
			return !ViewCompat.canScrollVertically(mScrollableView, -1);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtEnd() {
			return !ViewCompat.canScrollVertically(mScrollableView, 1);
		}
	}

	/**
	 * Implementation of {@link ScrollableWrapper} to wrap {@link ScrollView}.
	 */
	private static final class ScrollViewWrapper<V extends ScrollView> extends ScrollableWrapper<V> {

		/**
		 * Creates a new instance of ScrollViewWrapper to wrap the given <var>scrollView</var>.
		 *
		 * @param scrollView The scroll view to wrap.
		 */
		private ScrollViewWrapper(V scrollView) {
			super(scrollView);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtStart() {
			return mScrollableView.getScrollY() == 0;
		}

		/**
		 */
		@Override
		public boolean isScrolledAtEnd() {
			final View view = mScrollableView.getChildAt(0);
			return view != null && (view.getHeight() - mScrollableView.getHeight()) == mScrollableView.getScrollY();
		}
	}

	/**
	 * Implementation of {@link ScrollableWrapper} to wrap {@link HorizontalScrollView}.
	 */
	private static final class HorizontalScrollViewWrapper<V extends HorizontalScrollView> extends ScrollableWrapper<V> {

		/**
		 * Creates a new instance of HorizontalScrollViewWrapper to wrap the given <var>scrollView</var>.
		 *
		 * @param scrollView The horizontal scroll view to wrap.
		 */
		private HorizontalScrollViewWrapper(V scrollView) {
			super(scrollView);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtStart() {
			return mScrollableView.getScrollX() == 0;
		}

		/**
		 */
		@Override
		public boolean isScrolledAtEnd() {
			final View view = mScrollableView.getChildAt(0);
			return view != null && (view.getWidth() - mScrollableView.getWidth()) == mScrollableView.getScrollX();
		}
	}

	/**
	 * Implementation of {@link ViewPagerWrapper} to wrap {@link ViewPager}.
	 */
	private static final class ViewPagerWrapper<V extends ViewPager> extends ScrollableWrapper<V> {

		/**
		 * Creates a new instance of ViewPagerWrapper to wrap the given <var>viewPager</var>.
		 *
		 * @param viewPager The view pager to wrap.
		 */
		private ViewPagerWrapper(V viewPager) {
			super(viewPager);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtStart() {
			final int pages = mScrollableView.getChildCount();
			return pages == 0 || mScrollableView.getCurrentItem() == 0;
		}

		/**
		 */
		@Override
		public boolean isScrolledAtEnd() {
			if (mScrollableView.getChildCount() == 0) {
				return true;
			}
			final PagerAdapter adapter = mScrollableView.getAdapter();
			return adapter == null || adapter.getCount() == 0 || mScrollableView.getCurrentItem() == (adapter.getCount() - 1);
		}
	}

	/**
	 * Implementation of {@link ScrollableWrapper} to wrap {@link WebView}.
	 */
	private static final class WebViewWrapper<V extends WebView> extends ScrollableWrapper<V> {

		/**
		 * Creates a new instance of WebViewWrapper to wrap the given <var>webView</var>.
		 *
		 * @param webView The web view to wrap.
		 */
		WebViewWrapper(V webView) {
			super(webView);
		}

		/**
		 */
		@Override
		public boolean isScrolledAtStart() {
			return mScrollableView.getScrollY() == 0;
		}

		/**
		 */
		@Override
		@SuppressWarnings({"deprecation", "NewApi"})
		public boolean isScrolledAtEnd() {
			final float verticalScale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
					mScrollableView.getScaleY() :
					mScrollableView.getScale();
			final int scrollY = mScrollableView.getScrollY();
			final int height = mScrollableView.getHeight();
			final int contentHeight = (int) Math.floor(mScrollableView.getContentHeight() * verticalScale);
			return scrollY >= contentHeight - height;
		}
	}
}
