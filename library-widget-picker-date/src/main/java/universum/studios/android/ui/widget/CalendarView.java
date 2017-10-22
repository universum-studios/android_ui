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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;

/**
 * A {@link RecyclerView} implementation designed to present data set of months. Calendar view uses
 * by default {@link MonthView} widget as item view for its default adapter. The date (months) range
 * that should be presented by the calendar view can be specified through its adapter via
 * {@link CalendarAdapter#setMinMaxDate(long, long)}, where if you want to be presented "infinite"
 * amount of months, specify {@code 0} as <var>maxDateInMillis</var> attribute. The calendar adapter
 * can be accessed via {@link #getCalendarAdapter()}. If needed custom implementation of {@link CalendarAdapter}
 * can be specified via {@link #setCalendarAdapter(Adapter)} in such case {@link CalendarDataSet}
 * helper class can be used to simplify months data set management.
 * <p>
 * Initial selected date can be specified via {@link #setSelectedDate(java.util.Date)} or
 * {@link #setSelectedDate(long)} and the current selected date can be obtained via {@link #getSelectedDate()}
 * or {@link #getSelectedDateInMillis()}.
 *
 * <h3>Callbacks</h3>
 * Use {@link OnDateSelectionListener} to listen for callback about selected date. This listener can
 * be registered via {@link #setOnDateSelectionListener(OnDateSelectionListener)} and its callback
 * will be fired whenever a user selects its desired date (a specific number within the currently visible month view).
 * <p>
 * If you want to listen for changes in month or year that are caused by scrolling of the calendar
 * view, use {@link OnMonthChangeListener} and {@link OnYearChangeListener}. These listeners can be
 * registered via {@link #setOnMonthChangeListener(OnMonthChangeListener)} and
 * {@link #setOnYearChangeListener(OnYearChangeListener)}.
 *
 * <h3>XML attributes</h3>
 * {@link R.styleable#Ui_CalendarView CalendarView Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiCalendarViewStyle uiCalendarViewStyle}
 *
 * @author Martin Albedinsky
 */
public class CalendarView extends RecyclerView implements Widget {

	/*
	 * Interface ===================================================================================
	 */

	/**
	 * Listener that can receive callback about selected date within {@link CalendarView}.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnDateSelectionListener {

		/**
		 * Invoked whenever the specified <var>dateInMillis</var> has been selected within the given
		 * <var>calendarView</var>.
		 *
		 * @param calendarView The calendar view where the date has been selected.
		 * @param dateInMillis The selected date in milliseconds. Contains <b>{@link Calendar#YEAR},
		 *                     {@link Calendar#MONTH}</b>, and <b>{@link Calendar#DAY_OF_MONTH}</b> data.
		 */
		void onDateSelected(@NonNull CalendarView calendarView, long dateInMillis);

		/**
		 * Invoked whenever the current selected date in the given <var>calendarView</var> has been
		 * cleared.
		 *
		 * @param calendarView The calendar view with no selected date.
		 */
		void onNoDateSelected(@NonNull CalendarView calendarView);
	}

	/**
	 * Extension of {@link MonthView.OnDaySelectionListener} used by {@link CalendarView} to listen
	 * for selected day for a specific month within the calendar view's adapter's data set.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnMonthDaySelectionListener {

		/**
		 * Invoked whenever the specified <var>day</var> has been selected in the given <var>monthView</var>
		 * at the adapter <var>position</var>.
		 *
		 * @param monthView    The month view where the day has been selected.
		 * @param day          The selected day. Will be from the range {@code [1, 31]}.
		 * @param dateInMillis A date in milliseconds that is currently selected within the given month view.
		 * @param position     The adapter position of the month view within the current calendar data set.
		 * @see MonthView.OnDaySelectionListener#onDaySelected(MonthView, int, long)
		 */
		void onMonthDaySelected(@NonNull MonthView monthView, @IntRange(from = 1, to = 31) int day, long dateInMillis, int position);
	}

	/**
	 * Listener that can receive callback about changed month by scroll within {@link CalendarView}.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnMonthChangeListener {

		/**
		 * Invoked whenever the specified <var>month</var> (its view) has been scrolled within the
		 * given <var>calendarView</var>.
		 *
		 * @param calendarView The calendar view where the month has been scrolled.
		 * @param month        The month number of which view has been scrolled.
		 * @param offset       Offset of the scrolled month view from its 'idle' position. Value is
		 *                     from the range {@code [0.0, 1.0)} where {@code 0.0} indicates that
		 *                     the month view is completely scrolled to the left (it is not visible
		 *                     for a user) and {@code 1.0} indicates that the month view is completely
		 *                     scrolled at its 'idle' position (it is fully visible for a user).
		 */
		void onMonthScrolled(@NonNull CalendarView calendarView, @IntRange(from = Calendar.JANUARY, to = Calendar.DECEMBER) int month, @FloatRange(from = 0.0f, to = 1.0f) float offset);

		/**
		 * Invoked whenever the specified <var>month</var> has been changed within the given
		 * <var>calendarView</var> due to change in the scroll.
		 *
		 * @param calendarView The calendar view where the month has been changed.
		 * @param month        The changed month number from the range [{@link Calendar#JANUARY}, {@link Calendar#DECEMBER}].
		 */
		void onMonthChanged(@NonNull CalendarView calendarView, @IntRange(from = Calendar.JANUARY, to = Calendar.DECEMBER) int month);
	}

	/**
	 * Listener that can receive callback about changed year by scroll within {@link CalendarView}.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnYearChangeListener {

		/**
		 * Invoked whenever the specified <var>year</var> has been changed within the given
		 * <var>calendarView</var> due to change in the scroll.
		 *
		 * @param calendarView The calendar view where the year has been changed.
		 * @param year         The changed year number.
		 */
		void onYearChanged(@NonNull CalendarView calendarView, int year);
	}

	/**
	 * Interface for adapter that provides data set of months for {@link CalendarView}.
	 *
	 * @author Martin Albedinsky
	 */
	public interface CalendarAdapter {

		/**
		 * Registers a callback to be invoked whenever a specific day is selected within a {@link MonthView}
		 * at a specific position.
		 *
		 * @param listener Listener callback. May be {@code null} to clear the current one.
		 */
		void setOnMonthDaySelectionListener(@Nullable OnMonthDaySelectionListener listener);

		/**
		 * Changes the minimum + maximum date to determine min boundary for month and the count of
		 * months provided by this adapter.
		 *
		 * @param minDateInMillis Minimum date used to determine the bottom date boundary, so the first month
		 *                        provided by this adapter.
		 * @param maxDateInMillis Maximum date used to determine the count of months provided by this adapter,
		 *                        so the last month.
		 * @return {@code True} if boundaries has been updated, {@code false} otherwise.
		 */

		/**
		 * Sets a date boundaries for months data set of this adapter. These dates are used to resolve
		 * when the months data set should start and how large (in count of months) should be.
		 *
		 * @param minDateInMillis The desired minimum date in milliseconds.
		 * @param maxDateInMillis The desired maximum date in milliseconds.
		 * @see #getDateMinInMillis()
		 * @see #getDateMaxInMillis()
		 */
		void setMinMaxDate(long minDateInMillis, long maxDateInMillis);

		/**
		 * Returns the minimum date specified via {@link #setMinMaxDate(long, long)} used to resolve
		 * start of months data set.
		 *
		 * @return Minimum date in milliseconds.
		 */
		long getDateMinInMillis();

		/**
		 * Returns the maximum date specified via {@link #setMinMaxDate(long, long)} used to limit
		 * count of months available.
		 *
		 * @return Maximum date in milliseconds.
		 */
		long getDateMaxInMillis();

		/**
		 * Sets a date that should be selected in months data set of this adapter.
		 *
		 * @param dateInMillis The desired date in milliseconds that will be used to resolve in which
		 *                     month to select the desired day.
		 */
		void setSelectedDate(@Nullable Long dateInMillis);

		/**
		 * Calculates a position of a month for the given <var>year</var> and <var>month</var>.
		 *
		 * @param year  The desired year number.
		 * @param month The desired month number from the range [{@link Calendar#JANUARY}, {@link Calendar#DECEMBER}].
		 * @return Computed position of the requested month within the current data set.
		 */
		int calculateMonthPosition(int year, @IntRange(from = Calendar.JANUARY, to = Calendar.DECEMBER) int month);

		/**
		 * Calculates the position of a month with the specified <var>dateInMillis</var>.
		 *
		 * @param dateInMillis The date in milliseconds of month of which position to calculate.
		 * @return The calculated month position.
		 */
		int calculateMonthPosition(long dateInMillis);

		/**
		 * Returns the date for a month from this adapter's data set at the specified <var>position</var>.
		 *
		 * @param position Position of the month for which to return its associated date.
		 * @return Month's date in milliseconds.
		 */
		long getMonthDate(int position);

		/**
		 * Sets a locale used to display proper names for calendar fields (month name and names of days in week).
		 *
		 * @param locale The desired locale.
		 * @see #getLocale()
		 */
		void setLocale(@NonNull Locale locale);

		/**
		 * Returns the current locale used by this adapter to display proper calendar field names.
		 *
		 * @return Current locale or {@link Locale#getDefault()} as default.
		 * @see #setLocale(Locale)
		 */
		@NonNull
		Locale getLocale();

		/**
		 * Called to save the current state of this adapter.
		 *
		 * @return Saved state of this adapter or an <b>empty</b> state if this adapter does not need to
		 * save its state.
		 */
		@NonNull
		Parcelable saveInstanceState();

		/**
		 * Called to restore a previous state, saved by {@link #saveInstanceState()}, of this adapter.
		 *
		 * @param savedState Should be the same state as obtained via {@link #saveInstanceState()} before.
		 */
		void restoreInstanceState(@NonNull Parcelable savedState);
	}

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "CalendarView";

	/**
	 * Flag indicating whether this view is pressed or not.
	 */
	private static final int PFLAG_PRESSED = 0x00000001 << 16;

	/**
	 * Number of months within one year.
	 */
	private static final int MONTHS_IN_YEAR = 12;

	/**
	 * Constant used to indicate that an integer field has no value specified.
	 */
	private static final int NO_VALUE = -1;

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Runnable used to change the current position at which is this view scrolled.
	 */
	private final ScrollToPosition SCROLL_TO_POSITION = new ScrollToPosition();

	/**
	 * Runnable used to change the current position at which is this view scrolled smoothly.
	 */
	private final SmoothScrollToPosition SMOOTH_SCROLL_TO_POSITION = new SmoothScrollToPosition();

	/**
	 * Helper that implements listeners of which callbacks calendar view want to receive and process.
	 */
	private final CallbacksHandler CALLBACKS_HANDLER = new CallbacksHandler();

	/**
	 * Orientation of this calendar view used as orientation for its {@link LinearLayoutManager}.
	 */
	private int mOrientation = HORIZONTAL;

	/**
	 * Speed in milliseconds per inch used to calculate duration of smooth scroll.
	 */
	private float mScrollSpeedPerInch;

	/**
	 * Minimum velocity used when overriding fling velocity in {@link #fling(int, int)} method.
	 */
	private float mFlingMinVelocity;

	/**
	 * Minimum velocity used when overriding fling velocity in {@link #fling(int, int)} method.
	 */
	private float mFlingMaxVelocity = Float.MAX_VALUE;

	/**
	 * Linear layout manager set to this calendar view (if any).
	 */
	LinearLayoutManager mLinearLayoutManager;

	/**
	 * Calendar instance used to compute date elements (year, month, ...).
	 */
	Calendar mCalendar;

	/**
	 * Year on that is this CalendarView currently scrolled.
	 */
	int mScrolledYear = NO_VALUE;

	/**
	 * Month on that is this CalendarView currently scrolled.
	 */
	int mScrolledMonth = NO_VALUE;

	/**
	 * Decorator used to extend API of this widget by functionality otherwise not supported or not
	 * available due to current API level.
	 */
	private Decorator mDecorator;

	/**
	 * Adapter which provides data set of months for this CalendarView.
	 */
	private CalendarAdapter mAdapter;

	/**
	 * Mock month view used to compute size of this view.
	 */
	private MonthView mMockMonthView;

	/**
	 * Callback to be invoked whenever a day has been selected.
	 */
	private OnDateSelectionListener mDateSelectionListener;

	/**
	 * Callback to be invoked whenever a month has been changed due to change in the scroll.
	 */
	private OnMonthChangeListener mMonthChangeListener;

	/**
	 * Callback to be invoked whenever a year has been changed due to change in the scroll.
	 */
	private OnYearChangeListener mYearChangeListener;

	/**
	 * Date on that is this CalendarView currently scrolled.
	 */
	private long mDateVisible;

	/**
	 * Date that is currently selected within this CalendarView.
	 */
	private Long mDateSelected;

	/**
	 * Position of month view where {@link #mDateSelected} has been selected either by a user or
	 * via {@link #setSelectedDate(long)}.
	 */
	private int mSelectedDateMonthPosition = NO_POSITION;

	/**
	 * Animator used to animate size of this view.
	 */
	private WidgetSizeAnimator mSizeAnimator;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #CalendarView(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public CalendarView(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #CalendarView(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#uiCalendarViewStyle uiCalendarViewStyle} as attribute for default style.
	 */
	public CalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiCalendarViewStyle);
	}

	/**
	 * Creates a new instance of CalendarView for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 */
	public CalendarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.ensureDecorator();
		this.mDecorator.processAttributes(context, attrs, defStyleAttr, 0);
		this.mCalendar = Calendar.getInstance();
		super.addOnScrollListener(CALLBACKS_HANDLER);

		final Resources resources = context.getResources();
		final float density = resources.getDisplayMetrics().density;
		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_CalendarView, defStyleAttr, 0);
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_CalendarView_android_orientation) {
				this.mOrientation = attributes.getInt(index, mOrientation);
			} else if (index == R.styleable.Ui_CalendarView_uiScrollSpeedPerInch) {
				this.mScrollSpeedPerInch = attributes.getFloat(index, mScrollSpeedPerInch);
			} else if (index == R.styleable.Ui_CalendarView_uiFlingMinVelocity) {
				this.mFlingMinVelocity = attributes.getFloat(index, mFlingMinVelocity) * density;
			} else if (index == R.styleable.Ui_CalendarView_uiFlingMaxVelocity) {
				this.mFlingMaxVelocity = attributes.getFloat(index, mFlingMaxVelocity) * density;
			}
		}
		attributes.recycle();
		setLayoutManager(new LayoutManagerImpl(context, mOrientation, mScrollSpeedPerInch));
		setCalendarAdapter(new SimpleCalendarAdapter(context, new CalendarDataSet()));
	}

	/*
	 * Methods =====================================================================================
	 */

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
		event.setClassName(CalendarView.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(CalendarView.class.getName());
	}

	/**
	 * Sets an orientation in which should be month views layout within this calendar view. This
	 * is the same as calling {@link #setLayoutManager(LayoutManager)} with instance of {@link LinearLayoutManager}
	 * and the specified orientation.
	 *
	 * @param orientation The desired orientation. Should be one of {@link #HORIZONTAL} or {@link #VERTICAL}.
	 * @see android.R.attr#orientation android:orientation
	 * @see LinearLayoutManager#LinearLayoutManager(Context, int, boolean)
	 * @see #getOrientation()
	 */
	public void setOrientation(int orientation) {
		if (mOrientation != orientation) {
			switch (orientation) {
				case HORIZONTAL:
				case VERTICAL:
					setLayoutManager(new LayoutManagerImpl(getContext(), orientation, mScrollSpeedPerInch));
					break;
			}
		}
	}

	/**
	 * Returns the current orientation of this calendar view.
	 *
	 * @return Current orientation as specified for the layout manager (if instance of {@link LinearLayoutManager})
	 * or {@code -1} if this calendar view does not have layout manger type of LinearLayoutManager
	 * specified.
	 * @see #setOrientation(int)
	 */
	public int getOrientation() {
		return mOrientation;
	}

	/**
	 */
	@Override
	public void setLayoutManager(LayoutManager layout) {
		super.setLayoutManager(layout);
		if (layout instanceof LinearLayoutManager) {
			this.mLinearLayoutManager = (LinearLayoutManager) layout;
			this.mOrientation = mLinearLayoutManager.getOrientation();
		} else {
			this.mLinearLayoutManager = null;
			this.mOrientation = NO_VALUE;
		}
	}

	/**
	 * Registers a callback to be invoked whenever a date is selected within this CalendarView.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	public void setOnDateSelectionListener(@Nullable OnDateSelectionListener listener) {
		this.mDateSelectionListener = listener;
	}

	/**
	 * Registers a callback to be invoked whenever a month is changed due to change in scroll of
	 * this CalendarView.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	public void setOnMonthChangeListener(@Nullable OnMonthChangeListener listener) {
		this.mMonthChangeListener = listener;
	}

	/**
	 * Registers a callback to be invoked whenever a year is changed due to change in scroll of
	 * this CalendarView.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	public void setOnYearChangeListener(@Nullable OnYearChangeListener listener) {
		this.mYearChangeListener = listener;
	}

	/**
	 * Returns the calendar instance used by this calendar view for date related calculations.
	 * <p>
	 * <b>Note</b>, that changing locale via {@link #setLocale(Locale)} also changes calendar instance.
	 *
	 * @return Calendar instance of this calendar view.
	 */
	@NonNull
	public final Calendar getCalendar() {
		return mCalendar;
	}

	/**
	 * Like {@link #getCalendar()} but here returned calendar will contain time data for the current
	 * selected date (if any).
	 *
	 * @return Calendar instance with selected date (if selected).
	 */
	@NonNull
	public final Calendar getCalendarWithSelectedDate() {
		if (mDateSelected != null) {
			mCalendar.setTimeInMillis(mDateSelected);
		}
		return mCalendar;
	}

	/**
	 * Sets a locale used to display proper names for calendar fields (month name and names of days in week).
	 *
	 * @param locale The desired locale.
	 * @see #getLocale()
	 */
	public void setLocale(@NonNull Locale locale) {
		this.mCalendar = Calendar.getInstance(locale);
		if (mAdapter != null) {
			mAdapter.setLocale(locale);
		}
	}

	/**
	 * Returns the current locale used to obtain proper names of calendar fields.
	 * <p>
	 * <b>Note</b>, that this also changes the current adapter's locale via {@link CalendarAdapter#setLocale(Locale)}.
	 *
	 * @return This CalendarView's locale or {@link java.util.Locale#getDefault()} by default.
	 * @see #setLocale(Locale)
	 */
	@NonNull
	public Locale getLocale() {
		return mAdapter != null ? mAdapter.getLocale() : Locale.getDefault();
	}

	/**
	 * Same as {@link #setSelectedDate(long)} for {@link Date} object.
	 *
	 * @param date The desired date to be selected. May be {@code null} to clear the current one.
	 * @see #getSelectedDate()
	 */
	public void setSelectedDate(@Nullable Date date) {
		if (date != null) {
			setSelectedDate(date.getTime());
		} else {
			this.handleSelectedDateUpdate(null, -1);
		}
	}

	/**
	 * Sets a date that should be selected in this calendar view (in one of its month views).
	 * <p>
	 * <b>Note</b>, that setting selected date also changes the current visible date (month) to the
	 * selected one. See {@link #setVisibleDate(long)} for more information.
	 *
	 * @param dateInMillis The desired date to be selected in milliseconds.
	 */
	public void setSelectedDate(long dateInMillis) {
		if (mDateSelected == null || mDateSelected != dateInMillis) {
			this.handleSelectedDateUpdate(dateInMillis, NO_VALUE);
			this.updateCurrentPosition(mScrolledYear, mScrolledMonth);
		}
	}

	/**
	 * Handles update in the current selected date (if any) and notifies any registered listeners
	 * about this change.
	 * <p>
	 * Depending on the specified <var>dateInMillis</var> ({@code null} or {@code not null}) this
	 * method will also update visible date to the selected one and also the current visible month
	 * position accordingly.
	 *
	 * @param dateInMillis         The new selected date in milliseconds. May be {@code null} to clear the
	 *                             current one.
	 * @param monthAdapterPosition Position of the MonthView from the current adapter in which has
	 *                             been the date selected. May be {@link #NO_VALUE} to calculate the
	 *                             position using the adapter's {@link CalendarAdapter#calculateMonthPosition(int, int)}
	 *                             if the date has been selected via code and not by a user.
	 */
	final void handleSelectedDateUpdate(Long dateInMillis, int monthAdapterPosition) {
		this.mDateSelected = dateInMillis;
		if (mDateSelected != null) {
			mCalendar.clear();
			mCalendar.setTimeInMillis(mDateSelected);
			final int year = mCalendar.get(Calendar.YEAR);
			final int month = mCalendar.get(Calendar.MONTH);
			this.handleVisibleDateUpdate(year, month);
			if (monthAdapterPosition == NO_POSITION && mAdapter != null) {
				this.mSelectedDateMonthPosition = mAdapter.calculateMonthPosition(mDateSelected);
				this.updateCurrentPosition(year, month);
			}
		} else {
			this.mSelectedDateMonthPosition = NO_POSITION;
		}
		if (mAdapter != null) {
			mAdapter.setSelectedDate(mDateSelected);
		}
		this.notifySelectedDateChange();
	}

	/**
	 * Notifies the current OnDateSelectionListener, that the current selected date ({@link #mDateSelected})
	 * has been changed.
	 */
	private void notifySelectedDateChange() {
		if (mDateSelectionListener != null) {
			if (mDateSelected != null) mDateSelectionListener.onDateSelected(this, mDateSelected);
			else mDateSelectionListener.onNoDateSelected(this);
		}
	}

	/**
	 * Same as {@link #getSelectedDateInMillis()} for {@link Date} object.
	 *
	 * @return The current selected date.
	 */
	@Nullable
	public Date getSelectedDate() {
		return mDateSelected != null ? new Date(mDateSelected) : null;
	}

	/**
	 * Returns the current selected date that has been selected by a user or via {@link #setSelectedDate(long)}.
	 * <p>
	 * The returned date (if any) will contain data for <b>{@link Calendar#YEAR}, {@link Calendar#MONTH}</b>,
	 * and <b>{@link Calendar#DAY_OF_MONTH}</b>.
	 *
	 * @return The current selected date in milliseconds or {@code null} if there has not been selected
	 * any date yet.
	 */
	@Nullable
	public Long getSelectedDateInMillis() {
		return mDateSelected;
	}

	/**
	 * Same as {@link #setVisibleDate(long)} for {@link Date} object.
	 *
	 * @param date The desired visible date.
	 * @see #getVisibleDate()
	 */
	public void setVisibleDate(@NonNull Date date) {
		setVisibleDate(date.getTime());
	}

	/**
	 * Sets a date on which should be this calendar view at this time scrolled. This basically defines
	 * a month in a desired year that should be visible to a user so he/she can pick his/hers desired
	 * date.
	 *
	 * @param dateInMillis The desired visible date in milliseconds.
	 * @see #getVisibleDateInMillis()
	 */
	public void setVisibleDate(long dateInMillis) {
		if (mDateVisible != dateInMillis) {
			mCalendar.clear();
			mCalendar.setTimeInMillis(dateInMillis);
			this.handleVisibleDateUpdate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH));
			this.updateCurrentPosition(mScrolledYear, mScrolledMonth);
		}
	}

	/**
	 * Handles the update in the currently visible date.
	 * <p>
	 * This should be called whenever there has been change in scroll of this calendar view as the
	 * currently visible MonthView represents/shows a different date (month + year).
	 *
	 * @param year  Year number at which is this calendar view at this time scrolled.
	 * @param month Month number at which is this calendar view at this time scrolled
	 */
	private void handleVisibleDateUpdate(int year, int month) {
		this.mCalendar.clear();
		this.mCalendar.set(Calendar.YEAR, mScrolledYear = year);
		this.mCalendar.set(Calendar.MONTH, mScrolledMonth = month);
		this.mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		this.mDateVisible = mCalendar.getTimeInMillis();
	}

	/**
	 * Updates the current visible position of this calendar view according to the specified <var>year</var>
	 * and <var>month</var>.
	 *
	 * @param year  The year number used to calculate position to update to.
	 * @param month The month number used to calculate position to update to.
	 */
	private void updateCurrentPosition(int year, int month) {
		final int currentMonthPosition = getCurrentMonthAdapterPosition();
		final int position = mAdapter.calculateMonthPosition(year, month);
		if (position != currentMonthPosition) {
			SCROLL_TO_POSITION.position = position;
			post(SCROLL_TO_POSITION);
		}
	}

	/**
	 * Returns the position within adapter of the currently visible month view.
	 *
	 * @return Month adapter position or {@link #NO_POSITION} if it is not possible to determine
	 * such position.
	 */
	private int getCurrentMonthAdapterPosition() {
		return mLinearLayoutManager != null ?
				mLinearLayoutManager.findFirstVisibleItemPosition() :
				NO_POSITION;
	}

	/**
	 * Same as {@link #getVisibleDateInMillis()} for {@link Date} object.
	 *
	 * @return Currently visible date.
	 * @see #setVisibleDate(Date)
	 */
	@NonNull
	public Date getVisibleDate() {
		return new Date(mDateVisible);
	}

	/**
	 * Returns the date (year + month) on which is this calendar view at this time scrolled.
	 *
	 * @return Currently visible date in milliseconds.
	 * @see #setVisibleDate(long)
	 */
	public long getVisibleDateInMillis() {
		return mDateVisible;
	}

	/**
	 * Sets an adapter that will provide data set of months for this calendar view.
	 *
	 * @param adapter The desired adapter.
	 * @param <A>     Type of the desired adapter.
	 * @see #getCalendarAdapter()
	 */
	public <A extends RecyclerView.Adapter & CalendarAdapter> void setCalendarAdapter(@NonNull A adapter) {
		if (mAdapter != null) {
			mAdapter.setOnMonthDaySelectionListener(null);
		}
		this.mAdapter = adapter;
		this.mAdapter.setOnMonthDaySelectionListener(CALLBACKS_HANDLER);
		if (mDateSelected != null && mSelectedDateMonthPosition == NO_POSITION) {
			this.mSelectedDateMonthPosition = mAdapter.calculateMonthPosition(mDateSelected);
			this.mAdapter.setSelectedDate(mDateSelected);
		}
		super.setAdapter(adapter);
	}

	/**
	 * Returns the adapter that provides data set of months for this calendar view.
	 *
	 * @param <A> Type of the requested adapter. Should be the same type as specified via
	 *            {@link #setCalendarAdapter(Adapter)}
	 * @return The current adapter. Default is {@link SimpleCalendarAdapter}.
	 * @see #setCalendarAdapter(Adapter)
	 */
	@NonNull
	@SuppressWarnings("unchecked")
	public <A extends RecyclerView.Adapter & CalendarAdapter> A getCalendarAdapter() {
		return (A) mAdapter;
	}

	/**
	 */
	@Override
	public final void setAdapter(Adapter adapter) {
		Log.e(TAG, "Use CalendarView.setCalendarAdapter(A extends RecyclerView.Adapter & CalendarAdapter) instead.");
	}

	/**
	 */
	@Override
	public final Adapter getAdapter() {
		return super.getAdapter();
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
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.ensureMockMonthView();
		// Size of CalendarView will depend on size of a single MonthView with largest (in drawing area)
		// numbers data set.
		mMockMonthView.measure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(
				mMockMonthView.getMeasuredWidth(),
				mMockMonthView.getMeasuredHeight()
		);
	}

	/**
	 * Ensures that the mock MonthView is initialized.
	 */
	private void ensureMockMonthView() {
		if (mMockMonthView == null) {
			this.mMockMonthView = new MonthView(getContext());
			// Set mock date so month view can measure its size.
			mMockMonthView.setDate(new Date(System.currentTimeMillis()));
		}
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
		return (mSizeAnimator != null) ? mSizeAnimator : (mSizeAnimator = new WidgetSizeAnimator(this));
	}

	/**
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		this.ensureDecorator();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mDecorator.updatePrivateFlags(PFLAG_PRESSED, true);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mDecorator.updatePrivateFlags(PFLAG_PRESSED, false);
				break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 */
	@Override
	public boolean fling(int velocityX, int velocityY) {
		return super.fling(overrideFlingVelocity(velocityX), overrideFlingVelocity(velocityY));
	}

	/**
	 * Overrides the given <var>velocity</var> according to the {@link #mFlingMinVelocity} and
	 * {@link #mFlingMaxVelocity} values.
	 *
	 * @param velocity The velocity to correct.
	 * @return Corrected velocity in the range [{@link #mMinFlingVelocity}, {@link #mMaxFlingVelocity}].
	 */
	private int overrideFlingVelocity(int velocity) {
		return velocity != 0 ?
				(int) (velocity < 0 ? Math.max(-mFlingMaxVelocity, velocity) : Math.min(mFlingMaxVelocity, velocity)) :
				0;
	}

	/**
	 * Scrolls smoothly to the previous month if this calendar view is not already scrolled at its
	 * first month available.
	 *
	 * @return {@code True} if the smooth scroll has been performed, {@code false} if last month
	 * available is already visible.
	 * @see #canScrollToPreviousMonth()
	 * @see #smoothScrollToNextMonth()
	 */
	public boolean smoothScrollToPreviousMonth() {
		final int currentMonthPosition = getCurrentMonthAdapterPosition();
		if (currentMonthPosition > 0) {
			smoothScrollToPosition(currentMonthPosition - 1);
			return true;
		}
		return false;
	}

	/**
	 * Checks whether this calendar view can be scrolled to its previous month or not.
	 *
	 * @return {@code True} if the calendar view is not scrolled at its first month from the current
	 * data set and invocation of {@link #smoothScrollToPreviousMonth()} will return {@code true},
	 * {@code false} otherwise.
	 */
	public boolean canScrollToPreviousMonth() {
		return getCurrentMonthAdapterPosition() > 0;
	}

	/**
	 * Scrolls smoothly to the next month if this calendar view is not already scrolled at its
	 * last month available.
	 *
	 * @return {@code True} if the smooth scroll has been performed, {@code false} if first month
	 * available is already visible.
	 * @see #canScrollToNextMonth()
	 * @see #smoothScrollToPreviousMonth()
	 */
	public boolean smoothScrollToNextMonth() {
		final int currentMonthPosition = getCurrentMonthAdapterPosition();
		if (currentMonthPosition < (getAdapter().getItemCount() - 1)) {
			smoothScrollToPosition(currentMonthPosition + 1);
			return true;
		}
		return false;
	}

	/**
	 * Checks whether this calendar view can be scrolled to its next month or not.
	 *
	 * @return {@code True} if the calendar view is not scrolled at its last month from the current
	 * data set and invocation of {@link #smoothScrollToNextMonth()} will return {@code true},
	 * {@code false} otherwise.
	 */
	public boolean canScrollToNextMonth() {
		return getCurrentMonthAdapterPosition() < (getAdapter().getItemCount() - 1);
	}

	/**
	 * Notifies the current OnMonthChangeListener, that scroll of the specified <var>month</var> has
	 * been changed.
	 *
	 * @param month          Number of month with changed scroll.
	 * @param positionOffset Current position offset of the month's view according to this parent
	 *                       calendar view visible bounds.
	 */
	void notifyMonthScrolled(int month, float positionOffset) {
		if (mMonthChangeListener != null)
			mMonthChangeListener.onMonthScrolled(this, month, positionOffset);
	}

	/**
	 * Notifies the current OnMonthChangeListener, that the specified <var>month</var> has been changed.
	 *
	 * @param month Number of changed month.
	 */
	void notifyMonthChanged(int month) {
		if (mMonthChangeListener != null) mMonthChangeListener.onMonthChanged(this, month);
	}

	/**
	 * Notifies the current OnYearChangeListener, that the specified <var>year</var> has been changed.
	 *
	 * @param year The changed year number.
	 */
	void notifyYearChanged(int year) {
		if (mYearChangeListener != null) mYearChangeListener.onYearChanged(this, year);
	}

	/**
	 */
	@Override
	public Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.dateVisible = mDateVisible;
		savedState.dateSelected = mDateSelected;
		savedState.selectedDayMonthPosition = mSelectedDateMonthPosition;
		savedState.locale = getLocale();
		savedState.adapterState = mAdapter.saveInstanceState();
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
		this.mDateSelected = savedState.dateSelected;
		this.mDateVisible = savedState.dateVisible;
		this.mCalendar = Calendar.getInstance(savedState.locale);
		this.mCalendar.clear();
		this.mCalendar.setTimeInMillis(mDateVisible);
		this.mScrolledYear = mCalendar.get(Calendar.YEAR);
		this.mScrolledMonth = mCalendar.get(Calendar.MONTH);
		this.mSelectedDateMonthPosition = savedState.selectedDayMonthPosition;
		if (mAdapter != null) {
			mAdapter.setLocale(savedState.locale);
			if (savedState.adapterState != null) {
				mAdapter.restoreInstanceState(savedState.adapterState);
			}
		}
		updateCurrentPosition(mScrolledYear, mScrolledMonth);
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * A {@link CalendarAdapter} implementation that provides simple data set of months for CalendarView.
	 * This adapter implementation creates instances of {@link MonthView} as its item views and uses
	 * {@link CalendarDataSet} helper to properly provide data set of months.
	 *
	 * @author Martin Albedinsky
	 */
	public static class SimpleCalendarAdapter extends RecyclerView.Adapter implements CalendarAdapter {

		/**
		 * Context within which is this adapter used.
		 */
		protected final Context context;

		/**
		 * Layout inflater used to inflate item views of this adapter.
		 */
		protected final LayoutInflater layoutInflater;

		/**
		 * Helper object used to provide proper set of months by this adapter.
		 */
		final CalendarDataSet dataSet;

		/**
		 * Locale used primary to present text data.
		 */
		Locale locale = Locale.getDefault();

		/**
		 * Listener used to listen for selected day within a {@link MonthView} at a specific position.
		 */
		OnMonthDaySelectionListener monthDaySelectionListener;

		/**
		 * Date that is currently selected. May be {@code null} if no date is selected.
		 */
		Long selectedDate;

		/**
		 * Day number from the range {@code [1, 31]} of the selected date.
		 */
		int selectedDay = NO_VALUE;

		/**
		 * Position of a month from the current data set where the current selected date has been
		 * selected.
		 */
		int selectedDayMonthPosition = NO_POSITION;

		/**
		 * The recycler view to which is this adapter attached (if any).
		 */
		RecyclerView recyclerView;

		/**
		 * Creates a new instance of SimpleCalendarAdapter to provide data set of months.
		 *
		 * @param context Context in which will be the new adapter used. This context will be used
		 *                when creating item views for the adapter.
		 * @param dataSet Calendar data set used to provide set of months for the desired min + max
		 *                date boundaries.
		 */
		public SimpleCalendarAdapter(@NonNull Context context, @NonNull CalendarDataSet dataSet) {
			this.context = context;
			this.layoutInflater = LayoutInflater.from(context);
			this.dataSet = dataSet;
		}

		/**
		 */
		@Override
		public void setOnMonthDaySelectionListener(@Nullable OnMonthDaySelectionListener listener) {
			this.monthDaySelectionListener = listener;
		}

		/**
		 */
		@Override
		public void setMinMaxDate(long minDateInMillis, long maxDateInMillis) {
			if (dataSet.setMinMaxDate(minDateInMillis, maxDateInMillis)) notifyDataSetChanged();
		}

		/**
		 */
		@Override
		public long getDateMinInMillis() {
			return dataSet.dateMin;
		}

		/**
		 */
		@Override
		public long getDateMaxInMillis() {
			return dataSet.dateMax;
		}

		/**
		 */
		@Override
		public int getItemCount() {
			return dataSet.getSize();
		}

		/**
		 */
		@Override
		public void setSelectedDate(@Nullable Long dateInMillis) {
			if ((selectedDate == null && dateInMillis == null) || (selectedDate != null && selectedDate.equals(dateInMillis))) {
				return;
			}
			this.selectedDate = dateInMillis;
			this.selectedDay = NO_VALUE;
			this.selectedDayMonthPosition = NO_POSITION;
			if (dateInMillis != null) {
				dataSet.calendar.clear();
				dataSet.calendar.setTimeInMillis(dateInMillis);
				this.selectedDay = dataSet.calendar.get(Calendar.DAY_OF_MONTH);
				this.selectedDayMonthPosition = dataSet.calculateMonthPosition(dateInMillis);
			}
			if (recyclerView == null || !recyclerView.isComputingLayout()) {
				notifyDataSetChanged();
			}
		}

		/**
		 */
		@Override
		public int calculateMonthPosition(long dateInMillis) {
			return dataSet.calculateMonthPosition(dateInMillis);
		}

		/**
		 */
		@Override
		public int calculateMonthPosition(int year, int month) {
			return dataSet.calculateMonthPosition(year, month);
		}

		/**
		 */
		@Override
		public long getMonthDate(int position) {
			return dataSet.getMonthDate(position);
		}

		/**
		 */
		@Override
		public void setLocale(@NonNull Locale locale) {
			if (!this.locale.equals(locale)) {
				this.locale = locale;
				this.dataSet.setCalendar(Calendar.getInstance(locale));
				if (recyclerView == null || !recyclerView.isComputingLayout()) {
					notifyDataSetChanged();
				}
			}
		}

		/**
		 */
		@NonNull
		@Override
		public Locale getLocale() {
			return locale;
		}

		/**
		 */
		@Override
		public void onAttachedToRecyclerView(RecyclerView recyclerView) {
			super.onAttachedToRecyclerView(recyclerView);
			this.recyclerView = recyclerView;
		}

		/**
		 */
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ItemHolder(new MonthView(context));
		}

		/**
		 */
		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			onBindViewHolder(holder, position, dataSet.getMonthDate(position));
		}

		/**
		 * Invoked from {@link #onBindViewHolder(ViewHolder, int)} with the given <var>dateInMillis</var>
		 * that is associated with the {@link MonthView} at the <var>position</var> according to the
		 * current data set size.
		 *
		 * @param holder       View holder to bind.
		 * @param position     The position for which to perform binding.
		 * @param dateInMillis Date in milliseconds obtained for the month position via
		 *                     {@link CalendarDataSet#getMonthDate(int)}.
		 */
		@SuppressWarnings("ResourceType")
		protected void onBindViewHolder(@NonNull ViewHolder holder, int position, long dateInMillis) {
			final MonthView monthView = (MonthView) holder.itemView;
			monthView.setLocale(locale);
			monthView.setDate(dateInMillis);
			if (selectedDayMonthPosition == position && selectedDay != NO_VALUE) {
				monthView.setSelection(selectedDay);
			} else {
				monthView.setSelection(0);
			}
		}

		/**
		 */
		@NonNull
		@Override
		public Parcelable saveInstanceState() {
			final SimpleCalendarAdapter.SavedState state = new SimpleCalendarAdapter.SavedState(SavedState.EMPTY_STATE);
			state.dataSet = dataSet;
			state.selectedDate = selectedDate;
			state.selectedDay = selectedDay;
			state.selectedDayMonthPosition = selectedDayMonthPosition;
			return state;
		}

		/**
		 */
		@Override
		public void restoreInstanceState(@NonNull Parcelable savedState) {
			if (!(savedState instanceof SimpleCalendarAdapter.SavedState)) {
				return;
			}
			final SimpleCalendarAdapter.SavedState state = (SimpleCalendarAdapter.SavedState) savedState;
			this.dataSet.set(state.dataSet);
			this.selectedDate = state.selectedDate;
			this.selectedDay = state.selectedDay;
			this.selectedDayMonthPosition = state.selectedDayMonthPosition;
			notifyDataSetChanged();
		}

		/**
		 */
		@Override
		public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
			super.onDetachedFromRecyclerView(recyclerView);
			this.recyclerView = null;
		}

		/**
		 * A {@link ViewHolder} implementation for month item view.
		 */
		final class ItemHolder extends ViewHolder implements MonthView.OnDaySelectionListener {

			/**
			 * Creates a new instance of ItemHolder for the specified <var>monthView</var>.
			 */
			ItemHolder(MonthView monthView) {
				super(monthView);
				monthView.setOnDaySelectionListener(this);
			}

			/**
			 */
			@Override
			public void onDaySelected(@NonNull MonthView monthView, @IntRange(from = 1, to = 31) int day, long dateInMillis) {
				if (monthDaySelectionListener != null) monthDaySelectionListener.onMonthDaySelected(
						monthView,
						day,
						dateInMillis, getAdapterPosition()
				);
			}
		}

		/**
		 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link SimpleCalendarAdapter}
		 * is properly saved.
		 */
		static final class SavedState extends WidgetSavedState {

			/**
			 * Creator used to create an instance or array of instances of SavedState from {@link android.os.Parcel}.
			 */
			public static final Creator<SimpleCalendarAdapter.SavedState> CREATOR = new Creator<SimpleCalendarAdapter.SavedState>() {
				/**
				 */
				@Override
				public SimpleCalendarAdapter.SavedState createFromParcel(@NonNull Parcel source) {
					return new SimpleCalendarAdapter.SavedState(source);
				}

				/**
				 */
				@Override
				public SimpleCalendarAdapter.SavedState[] newArray(int size) {
					return new SimpleCalendarAdapter.SavedState[size];
				}
			};

			/**
			 */
			CalendarDataSet dataSet;

			/**
			 */
			Long selectedDate;

			/**
			 */
			int selectedDay, selectedDayMonthPosition;

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
				this.dataSet = source.readParcelable(UiConfig.class.getClassLoader());
				this.selectedDate = (Long) source.readValue(Long.class.getClassLoader());
				this.selectedDay = source.readInt();
				this.selectedDayMonthPosition = source.readInt();
			}

			/**
			 */
			@Override
			public void writeToParcel(@NonNull Parcel dest, int flags) {
				dest.writeParcelable(dataSet, flags);
				dest.writeValue(selectedDate);
				dest.writeInt(selectedDay);
				dest.writeInt(selectedDayMonthPosition);
			}
		}
	}

	/**
	 * Helper class that may be used to simplify management of months data set provided by implementation
	 * of {@link CalendarAdapter}. Data set instance uses instance of {@link Calendar} to properly
	 * calculate as size of the data set that can be obtained via {@link #getSize()} also the proper
	 * positions for a specific <b>year + month</b> pairs that can be requested via {@link #calculateMonthPosition(int, int)}
	 * or for a particular <b>date</b> via {@link #calculateMonthPosition(long)}. Data set boundaries
	 * (starting date and count of months) may be specified via {@link #setMinMaxDate(long, long)}.
	 *
	 * @author Martin Albedinsky
	 */
	public static final class CalendarDataSet implements Parcelable {

		/**
		 * Creator used to create an instance or array of instances of CalendarDataSet from {@link android.os.Parcel}.
		 */
		public static final Creator<CalendarDataSet> CREATOR = new Creator<CalendarDataSet>() {
			/**
			 */
			@Override
			public CalendarDataSet createFromParcel(Parcel source) {
				return new CalendarDataSet(source);
			}

			/**
			 */
			@Override
			public CalendarDataSet[] newArray(int size) {
				return new CalendarDataSet[size];
			}
		};

		/**
		 * Calendar to be used by this data set to compute count of months to be provided depending
		 * on the specified <b>min</b> and <b>max</b> date boundaries via {@link #setMinMaxDate(long, long)}.
		 */
		Calendar calendar;

		/**
		 * Minimum date used to limit size of this data set and also to resolve its beginning.
		 */
		long dateMin;

		/**
		 * Maximum date used to limit size of this data set.
		 */
		long dateMax;

		/**
		 * Number of year at which this data set starts.
		 */
		int startingYear;

		/**
		 * Number of month at which this data set starts. This is basically first month in data set.
		 */
		int startingMonth;

		/**
		 * Count of months (items) currently provided by this data set.
		 */
		int months = Integer.MAX_VALUE;

		/**
		 * Creates a new instance of CalendarDataSet.
		 */
		public CalendarDataSet() {
			this.calendar = Calendar.getInstance();
			this.startingYear = calendar.get(Calendar.YEAR);
			this.startingMonth = calendar.get(Calendar.MONTH);
		}

		/**
		 * Called from {@link #CREATOR} to create an instance of CalendarDataSet form the given parcel
		 * <var>source</var>.
		 *
		 * @param source Parcel with data for the new instance.
		 */
		CalendarDataSet(Parcel source) {
			this.calendar = (Calendar) source.readSerializable();
			this.dateMin = source.readLong();
			this.dateMax = source.readLong();
			this.startingYear = source.readInt();
			this.startingMonth = source.readInt();
			this.months = source.readInt();
		}

		/**
		 */
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeSerializable(calendar);
			dest.writeLong(dateMin);
			dest.writeLong(dateMax);
			dest.writeInt(startingYear);
			dest.writeInt(startingMonth);
			dest.writeInt(months);
		}

		/**
		 */
		@Override
		public int describeContents() {
			return 0;
		}

		/**
		 * Sets all data of this data set from the specified one.
		 *
		 * @param dataSet The data set from which to set up this data set instance.
		 */
		public void set(@NonNull CalendarDataSet dataSet) {
			this.calendar = dataSet.calendar;
			this.dateMin = dataSet.dateMin;
			this.dateMax = dataSet.dateMax;
			this.startingYear = dataSet.startingYear;
			this.startingMonth = dataSet.startingMonth;
			this.months = dataSet.months;
		}

		/**
		 * Sets a calendar used by this data set properly calculate count of months according to
		 * specified data boundaries via {@link #setMinMaxDate(long, long)} and also to calculate
		 * position of a month for a specific <b>year + month</b> pair via {@link #calculateMonthPosition(int, int)}.
		 *
		 * @param calendar The desired calendar instance. May be {@code null} to use default one.
		 * @see #getCalendar()
		 */
		public void setCalendar(@Nullable Calendar calendar) {
			this.calendar = calendar != null ? calendar : Calendar.getInstance();
		}

		/**
		 * Returns the calendar used by this data set for its size calculations and also for month
		 * positions calculations.
		 *
		 * @return This data set's calendar. Default is {@link Calendar#getInstance()}.
		 * @see #setCalendar(Calendar)
		 */
		@NonNull
		public Calendar getCalendar() {
			return calendar;
		}

		/**
		 * Sets a minimum and maximum dates that are used to resolve from which date to start this
		 * data set and how large (in count of months) it should be.
		 *
		 * @param minDateInMillis The desired minimum date in milliseconds that determines the
		 *                        start of data set.
		 * @param maxDateInMillis The desired maximum date in milliseconds. May be {@code 0} to
		 *                        request "infinite" data set of months starting from the minimum date.
		 * @return {@code True} if data of this data set has changed, {@code false} otherwise.
		 * @see #getDateMin()
		 * @see #getDateMax()
		 */
		public boolean setMinMaxDate(long minDateInMillis, long maxDateInMillis) {
			if (minDateInMillis < maxDateInMillis || maxDateInMillis == 0) {
				if (dateMin != minDateInMillis || maxDateInMillis != dateMax) {
					// Update bottom boundary.
					calendar.clear();
					calendar.setTimeInMillis(dateMin = minDateInMillis);
					this.startingYear = calendar.get(Calendar.YEAR);
					this.startingMonth = calendar.get(Calendar.MONTH);
					if ((dateMax = maxDateInMillis) != 0) {
						calendar.clear();
						calendar.setTimeInMillis(dateMax);
						this.months = (calendar.get(Calendar.YEAR) - startingYear) * MONTHS_IN_YEAR + calendar.get(Calendar.MONTH) - startingMonth + 1;
					} else {
						this.months = Integer.MAX_VALUE;
					}
					return true;
				}
			}
			return false;
		}

		/**
		 * Returns the minimum date that limits size (count of months) of this data set.
		 *
		 * @return Minimum date in milliseconds.
		 * @see #getDateMax()
		 * @see #setMinMaxDate(long, long)
		 */
		public long getDateMin() {
			return dateMin;
		}

		/**
		 * Returns the maximum date that limits size (count of months) of this data set.
		 *
		 * @return Maximum date in milliseconds. May be {@code 0} if the current data set is "infinite".
		 * @see #getDateMin()
		 * @see #setMinMaxDate(long, long)
		 */
		public long getDateMax() {
			return dateMax;
		}

		/**
		 * Returns the size of this data set.
		 *
		 * @return Count of months provided by this data set calculated according to the <b>min + max</b>
		 * date boundaries specified via {@link #setMinMaxDate(long, long)}.
		 * @see #getDateMin()
		 * @see #getDateMax()
		 */
		public int getSize() {
			return months;
		}

		/**
		 * Same as {@link #calculateMonthPosition(int, int)} for the specified <var>dateInMillis</var>
		 * from which will be extracted {@link Calendar#YEAR} and {@link Calendar#MONTH} values using
		 * {@link Calendar}.
		 *
		 * @param dateInMillis The desired date in milliseconds. Should contain year and month data.
		 * @return Computed month position.
		 */
		public int calculateMonthPosition(long dateInMillis) {
			calendar.clear();
			calendar.setTimeInMillis(dateInMillis);
			return calculateMonthPosition(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
		}

		/**
		 * Calculates a position of month for the given <var>year</var> and <var>month</var>.
		 *
		 * @param year  The year number of month for which to calculate position.
		 * @param month The month number for which to calculate position.
		 * @return Computed position of the requested month within the current data set that is in
		 * size of {@link #getSize()}.
		 */
		public int calculateMonthPosition(int year, int month) {
			return (year - startingYear) * MONTHS_IN_YEAR + (month - startingMonth);
		}

		/**
		 * Returns the date for a month at the requested <var>position</var> calculated according
		 * to the <b>min + max</b> date boundaries specified via {@link #setMinMaxDate(long, long)}.
		 *
		 * @param position The position of month for which to obtain the date.
		 * @return Calculated date with {@link Calendar#YEAR} and {@link Calendar#MONTH} data in milliseconds.
		 */
		public long getMonthDate(int position) {
			// Each position represents new month from the start date.
			calendar.clear();
			calendar.set(Calendar.YEAR, startingYear + (startingMonth + position) / MONTHS_IN_YEAR);
			calendar.set(Calendar.MONTH, (startingMonth + position) % MONTHS_IN_YEAR);
			return calendar.getTimeInMillis();
		}
	}

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link CalendarView}
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
		int selectedDayMonthPosition;

		/**
		 */
		long dateVisible;

		/**
		 */
		Long dateSelected;

		/**
		 */
		Locale locale;

		/**
		 */
		Parcelable adapterState;

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
			this.dateVisible = source.readLong();
			this.dateSelected = (Long) source.readValue(Long.class.getClassLoader());
			this.selectedDayMonthPosition = source.readInt();
			this.locale = (Locale) source.readSerializable();
			this.adapterState = source.readParcelable(UiConfig.class.getClassLoader());
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeLong(dateVisible);
			dest.writeValue(dateSelected);
			dest.writeInt(selectedDayMonthPosition);
			dest.writeSerializable(locale);
			dest.writeParcelable(adapterState, flags);
		}
	}

	/**
	 * Implementation of all listeners of which callbacks CalendarView want to receive.
	 */
	private final class CallbacksHandler extends OnScrollListener implements OnMonthDaySelectionListener {

		/**
		 * The child view that is at this time being scrolled.
		 */
		View scrolledChild;

		/**
		 * Position that is at this time being scrolled.
		 */
		int scrolledPosition = NO_POSITION;

		/**
		 * Current scroll state.
		 */
		int state;

		/**
		 */
		@Override
		public void onScrollStateChanged(RecyclerView view, int scrollState) {
			this.state = scrollState;
			ensureDecorator();
			if (mDecorator.hasPrivateFlag(PFLAG_PRESSED) || mLinearLayoutManager == null) {
				return;
			}
			switch (state) {
				case SCROLL_STATE_DRAGGING:
				case SCROLL_STATE_SETTLING:
					break;
				case SCROLL_STATE_IDLE:
					if (scrolledChild != null) {
						int scrollToPosition = getChildAdapterPosition(scrolledChild);
						switch (mLinearLayoutManager.getOrientation()) {
							case LinearLayoutManager.VERTICAL:
								final int childTop = scrolledChild.getTop();
								final int childHeight = scrolledChild.getHeight();
								if (Math.abs(childTop) >= childHeight / 2) {
									// Scroll to the next month view.
									scrollToPosition++;
								}
								break;
							default:
								final int childLeft = scrolledChild.getLeft();
								final int childWidth = scrolledChild.getWidth();
								if (Math.abs(childLeft) >= childWidth / 2) {
									// Scroll to the next month view.
									scrollToPosition++;
								}
								break;
						}
						SMOOTH_SCROLL_TO_POSITION.position = scrollToPosition;
						post(SMOOTH_SCROLL_TO_POSITION);
						this.handleMonthScroll(scrollToPosition);
					}
					break;
			}
		}

		/**
		 */
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			if (mLinearLayoutManager != null) {
				switch (state) {
					case SCROLL_STATE_DRAGGING:
					case SCROLL_STATE_SETTLING:
						final int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
						if (firstVisibleItemPosition != scrolledPosition) {
							this.scrolledChild = mLinearLayoutManager.findViewByPosition(scrolledPosition = firstVisibleItemPosition);
						}
						if (scrolledChild != null) {
							notifyMonthScrolled(
									getChildAdapterPosition(scrolledChild),
									Math.abs(scrolledChild.getLeft()) / (float) scrolledChild.getWidth()
							);
						}
						break;
				}
			}
		}

		/**
		 * Handles change in the scroll of calendar view. This will fire {@link #notifyMonthChanged(int)}
		 * or {@link #notifyYearChanged(int)} whether the current scrolled year or month data
		 * has changed.
		 *
		 * @param monthPosition Position of month to which has been calendar view scrolled.
		 */
		void handleMonthScroll(int monthPosition) {
			final long monthDate = mAdapter.getMonthDate(monthPosition);
			mCalendar.clear();
			mCalendar.setTimeInMillis(monthDate);
			final int month = mCalendar.get(Calendar.MONTH);
			if (mScrolledMonth == month) {
				return;
			}
			final int year = mCalendar.get(Calendar.YEAR);
			// Notify changes.
			if (mScrolledYear != year) {
				handleVisibleDateUpdate(year, month);
				notifyMonthChanged(month);
				notifyYearChanged(year);
			} else {
				handleVisibleDateUpdate(year, month);
				notifyMonthChanged(month);
			}
		}

		/**
		 */
		@Override
		public void onMonthDaySelected(@NonNull MonthView monthView, @IntRange(from = 1, to = 31) int day, long dateInMillis, int position) {
			handleSelectedDateUpdate(dateInMillis, position);
		}
	}

	/**
	 * Runnable used to execute {@link #smoothScrollToPosition(int)} method (delayed if needed).
	 */
	private final class SmoothScrollToPosition implements Runnable {

		/**
		 * Position to scroll smoothly to.
		 */
		int position;

		/**
		 */
		@Override
		public void run() {
			smoothScrollToPosition(position);
		}
	}

	/**
	 * Runnable used to execute {@link #scrollToPosition(int)} method (delayed if needed).
	 */
	private final class ScrollToPosition implements Runnable {

		/**
		 * Position to scroll to.
		 */
		int position;

		/**
		 */
		@Override
		public void run() {
			scrollToPosition(position);
		}
	}

	/**
	 * A {@link LinearLayoutManager} implementation that uses custom {@link SmoothScroller} implementation
	 * to customize smooth scrolling feature.
	 */
	private static final class LayoutManagerImpl extends LinearLayoutManager {

		/**
		 * Scrolling speed in milliseconds per inch.
		 */
		float scrollSpeedPerInch;

		/**
		 * Scroller used to perform smooth scroll requests.
		 */
		SmoothScroller scroller;

		/**
		 * Same as {@link #LayoutManagerImpl(Context, int, float)} with default scroll speed of
		 * {@code 50.0} per inch.
		 */
		LayoutManagerImpl(Context context, int orientation) {
			this(context, orientation, 50.0f);
		}

		/**
		 * Creates a new instance of LayoutManagerImpl with the specified <var>orientation</var>.
		 *
		 * @param context            Context in which will be the new layout manager used.
		 * @param orientation        Orientation in which should manager layout views.
		 * @param scrollSpeedPerInch Speed in milliseconds per inch used to calculate scrolling speed
		 *                           per pixel.
		 */
		LayoutManagerImpl(Context context, int orientation, float scrollSpeedPerInch) {
			super(context, orientation, false);
			this.scrollSpeedPerInch = scrollSpeedPerInch;
		}

		/**
		 */
		@Override
		public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
			if (scroller == null) {
				this.scroller = new LinearSmoothScroller(recyclerView.getContext()) {

					/**
					 */
					@Override
					public PointF computeScrollVectorForPosition(int targetPosition) {
						return LayoutManagerImpl.this.computeScrollVectorForPosition(targetPosition);
					}

					/**
					 */
					@Override
					protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
						return scrollSpeedPerInch / displayMetrics.densityDpi;
					}

					/**
					 */
					@Override
					protected int calculateTimeForScrolling(int dx) {
						return dx != 0 ? Math.max(super.calculateTimeForScrolling(dx), 50) : 0;
					}
				};
			}
			scroller.setTargetPosition(position);
			startSmoothScroll(scroller);
		}
	}

	/**
	 * Decorator implementation for this widget.
	 */
	private final class Decorator extends WidgetDecorator<CalendarView> {

		/**
		 * See {@link WidgetDecorator#WidgetDecorator(View, int[])}.
		 */
		Decorator(CalendarView widget) {
			super(widget, R.styleable.Ui_CalendarView);
		}

		/**
		 */
		@Override
		void onProcessTintAttributes(Context context, TypedArray tintAttributes, int tintColor) {
			// Ignored.
		}

		/**
		 */
		@Override
		@SuppressWarnings("deprecation")
		void superSetBackgroundDrawable(Drawable drawable) {
			CalendarView.super.setBackgroundDrawable(drawable);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintList(ColorStateList tint) {
			CalendarView.super.setBackgroundTintList(tint);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		ColorStateList superGetBackgroundTintList() {
			return CalendarView.super.getBackgroundTintList();
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		void superSetBackgroundTintMode(PorterDuff.Mode tintMode) {
			CalendarView.super.setBackgroundTintMode(tintMode);
		}

		/**
		 */
		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		PorterDuff.Mode superGetBackgroundTintMode() {
			return CalendarView.super.getBackgroundTintMode();
		}
	}
}
