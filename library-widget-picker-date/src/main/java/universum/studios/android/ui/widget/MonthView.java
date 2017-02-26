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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import universum.studios.android.font.Font;
import universum.studios.android.font.FontWidget;
import universum.studios.android.font.util.FontApplier;
import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * This view is used by {@link CalendarView} to display days data set for a specific month to
 * allow to a user to pick its desired day for a specific month and year.
 * <p>
 * This view draws name of month and year number at the top as title text and set of day numbers below
 * it in {@code 7} columns and {@code 6} rows so also the months with the largest days data set
 * will fit into drawing area. The numbers "table" has also one header row containing first letters
 * of days in week.
 * <p>
 * Month of which days should the MonthView present can be specified via {@link #setDate(java.util.Date)},
 * where such a date should have number and year calendar fields specified. Initial selected
 * day can be specified via {@link #setSelection(int)} and the current selected day can be obtained
 * via {@link #getSelection()} or {@link #getSelectionDate()}. The later will return date that is
 * represented by the current selected day for the specified month and year.
 * <p>
 * <b>Note</b>, that this view saves its current state, so it can be later restored (in case of
 * orientation change for example).
 *
 * <h3>Callbacks</h3>
 * Use {@link OnDaySelectionListener} to listen for callback about selected day. This listener can
 * be registered via {@link #setOnDaySelectionListener(OnDaySelectionListener)} and its callback
 * will be fired whenever a user touches and releases its desired day.
 *
 * <h3>XML attributes</h3>
 * See {@link ViewWidget},
 * {@link R.styleable#Ui_MonthView MonthView Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiMonthViewStyle uiMonthViewStyle}
 *
 * @author Martin Albedinsky
 */
public class MonthView extends ViewWidget implements FontWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Listener that can receive a callback about selected day within {@link MonthView}.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnDaySelectionListener {

		/**
		 * Invoked whenever the specified <var>day</var> has been selected within the given <var>monthView</var>.
		 *
		 * @param monthView    The month view within which has been the day selected.
		 * @param day          The selected day. Will be from the range {@code [1, 31]}.
		 * @param dateInMillis A date in milliseconds that is currently selected within the given month view.
		 */
		void onDaySelected(@NonNull MonthView monthView, @IntRange(from = 1, to = 31) int day, long dateInMillis);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "MonthView";

	/**
	 * Flag indicating whether some number of day has been touched or not.
	 */
	private static final int PFLAG_DAY_TOUCHED = 0x00000001 << 16;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Flag indicating whether the current Android version is pre {@link android.os.Build.VERSION_CODES#GINGERBREAD}
	 * or not.
	 */
	private static final boolean PRE_GINGERBREAD = Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD;

	/**
	 * Copy of {@link java.util.Calendar#LONG} flag to provide support also for pre {@link android.os.Build.VERSION_CODES#GINGERBREAD GINGERBREAD}
	 * Android version.
	 */
	@SuppressLint("InlinedApi")
	public static final int CALENDAR_STYLE_LONG = PRE_GINGERBREAD ? 2 : Calendar.LONG;

	/**
	 * Copy of {@link java.util.Calendar#SHORT} flag to provide support also for pre {@link android.os.Build.VERSION_CODES#GINGERBREAD GINGERBREAD}
	 * Android version.
	 */
	@SuppressLint("InlinedApi")
	public static final int CALENDAR_STYLE_SHORT = PRE_GINGERBREAD ? 2 : Calendar.SHORT;

	/**
	 * Array of month names for pre {@link android.os.Build.VERSION_CODES#GINGERBREAD GINGERBREAD} Android version.
	 */
	private static final String[] PRE_GINGERBREAD_MONTH_NAMES;

	static {
		if (PRE_GINGERBREAD) {
			PRE_GINGERBREAD_MONTH_NAMES = new String[]{
					"January", "February", "March", "April", "May", "June",
					"July", "August", "September", "October", "November", "December"
			};
		} else {
			PRE_GINGERBREAD_MONTH_NAMES = null;
		}
	}

	/**
	 * Array of day names for pre {@link android.os.Build.VERSION_CODES#GINGERBREAD GINGERBREAD} Android version.
	 */
	private static final String[] PRE_GINGERBREAD_DAY_NAMES;

	static {
		if (PRE_GINGERBREAD) {
			PRE_GINGERBREAD_DAY_NAMES = new String[]{
					"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
			};
		} else {
			PRE_GINGERBREAD_DAY_NAMES = null;
		}
	}

	/**
	 * Count of days in one week.
	 */
	private static final int DAYS_IN_WEEK = 7;

	/**
	 * Maximum rows in which can be drawn the day numbers.
	 */
	private static final int MAX_DAYS_ROWS = 6;

	/**
	 * Maximum day number for month.
	 */
	private static final int MAX_DAY_IN_MONTH = 31;

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Array with first letters of day names for the current locale specified for this month view
	 * via {@link #setLocale(java.util.Locale)}.
	 */
	private final String[] DAY_LETTERS = {"", "", "", "", "", "", ""};

	/**
	 * Graphics info for title text.
	 */
	private final TextGraphicsInfo TITLE_TEXT_INFO = new TextGraphicsInfo();

	/**
	 * Graphics info for day letters text.
	 */
	private final TextGraphicsInfo DAY_LETTER_TEXT_INFO = new TextGraphicsInfo();

	/**
	 * Graphics info for day numbers text.
	 */
	private final TextGraphicsInfo DAY_NUMBER_TEXT_INFO = new TextGraphicsInfo();

	// Set up paints.
	{
		TITLE_TEXT_INFO.paint.setTextAlign(Paint.Align.CENTER);
		DAY_LETTER_TEXT_INFO.paint.setTextAlign(Paint.Align.CENTER);
		DAY_NUMBER_TEXT_INFO.paint.setTextAlign(Paint.Align.CENTER);
	}

	/**
	 * Rect used to capture text bounds when drawing text within this view.
	 */
	private final Rect TEXT_BOUNDS = new Rect();

	/**
	 * Rect representing the touchable area with day numbers.
	 */
	private final RectF TOUCHABLE_AREA = new RectF();

	/**
	 * Date instance used when updating date values of this month view.
	 */
	private final Date DATE = new Date(0);

	/**
	 * Holder for colors used to draw day numbers graphics of this view.
	 */
	private final DayNumberColorsState DAY_NUMBERS_COLORS_STATE = new DayNumberColorsState();

	/**
	 * Colors used to highlight current day among all month days.
	 */
	private ColorStateList mCurrentDayTextColors = ColorStateList.valueOf(Color.BLACK);

	/**
	 * Current color used to highlight current day when drawing its number on canvas.
	 */
	private int mCurrentDayCurrentTextColor = mCurrentDayTextColors.getDefaultColor();

	/**
	 * Set of private flags of this dialog view.
	 */
	private int mPrivateFlags = PrivateFlags.PFLAG_ALLOWS_DEFAULT_SELECTION;

	/**
	 * Display name of the month of the date specified for this month view via {@link #setDate(java.util.Date)}.
	 */
	private String mMonthName = "";

	/**
	 * Value of the year of the date specified for this month view via {@link #setDate(java.util.Date)}.
	 */
	private int mYear;

	/**
	 * First day of week of the date specified via {@link #setDate(java.util.Date)} for the current
	 * locale specified for this month view via {@link #setLocale(java.util.Locale)}.
	 */
	private int mFirstDayOfWeek = Calendar.SUNDAY;

	/**
	 * Number of the current calendar day of month. Number should be from the range {@code [1, 31]}.
	 * If {@code 0}, this month view does not present data for the current month.
	 */
	private int mCurrentDay;

	/**
	 * Number of a day that is at this time selected. Number should be from the range {@code [1, 31]}.
	 * If {@code 0}, there is no selected day at this time.
	 */
	private int mSelectedDay;

	/**
	 * Number of a day that is at this time pressed. Number should be from the range {@code [1, 31]}.
	 * If {@code 0}, there is no pressed day at this time.
	 */
	private int mPressedDay;

	/**
	 * Count of days for the month of date specified for this month view via {@link #setDate(java.util.Date)}.
	 */
	private int mDaysCount;

	/**
	 * Day ({@link java.util.Calendar#DAY_OF_WEEK}) used to resolve where to start drawing of day numbers.
	 */
	private int mStartDay;

	/**
	 * Size dimension of this view.
	 */
	private int mWidth;

	/**
	 * Vertical offset for the week day letters area.
	 */
	private int mDayLettersOffsetVertical;

	/**
	 * Vertical offset for the day numbers area.
	 */
	private int mDayNumbersOffsetVertical;

	/**
	 * Vertical spacing for the day numbers rows.
	 */
	private int mSpacingVertical;

	/**
	 * Horizontal spacing for the day numbers columns.
	 */
	private int mSpacingHorizontal;

	/**
	 * Locale instance used to obtain proper names for calendar fields.
	 */
	private Locale mLocale;

	/**
	 * Calendar instance used to access all necessary data for the date set to this month view.
	 */
	private Calendar mCalendar;

	/**
	 * Drawable used to draw the day selector indicating the current selected/pressed day.
	 */
	private Drawable mDaySelector;

	/**
	 * Resource id of the day selector's drawable.
	 */
	private int mDaySelectorRes;

	/**
	 * Radius for the day selector's drawable.
	 */
	private int mDaySelectorRadius;

	/**
	 * Listener callback fired whenever a specific day is selected.
	 */
	private OnDaySelectionListener mSelectionListener;

	/**
	 * Touch coordinate.
	 */
	private float mDragTouchX, mDragTouchY;

	/**
	 * Data used when tinting components of this view.
	 */
	private TintInfo mTintInfo;

	/**
	 * Day representation used in {@link #onDrawDayNumber(Canvas, Day, Paint)} to properly draw text
	 * for a particular day number.
	 */
	private Day mDay = new Day();

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #MonthView(android.content.Context, android.util.AttributeSet)} without attributes.
	 */
	public MonthView(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #MonthView(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#uiMonthViewStyle uiMonthViewStyle} as attribute for default style.
	 */
	public MonthView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiMonthViewStyle);
	}

	/**
	 * Same as {@link #MonthView(android.content.Context, android.util.AttributeSet, int, int)} with
	 * {@code 0} as default style.
	 */
	public MonthView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of MonthView for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public MonthView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Obtains a name of a calendar field with the specified <var>field</var> identifier from the
	 * given <var>calendar</var> for the specified <var>locale</var>.
	 * <p>
	 * <b>Note</b>, that for pre {@link android.os.Build.VERSION_CODES#GINGERBREAD GINGERBREAD} Android
	 * version and field names listed below, this method will return only English name regardless
	 * the specified <var>locale</var>:
	 * <ul>
	 * <li>{@link Calendar#MONTH}</li>
	 * <li>{@link Calendar#DAY_OF_WEEK}</li>
	 * </ul>
	 *
	 * @param calendar The calendar from which to obtain the requested field's name.
	 * @param field    The desired field identifier. See {@link Calendar} for field identifiers.
	 * @param style    Style flag for the requested name. One of {@link #CALENDAR_STYLE_SHORT} or
	 *                 {@link #CALENDAR_STYLE_LONG}.
	 * @param locale   The locale for which to obtain the requested name. If there is no name available
	 *                 for the requested locale, {@link UiConfig#DEFAULT_LOCALE} will be used instead.
	 * @return Name of the requested calendar field.
	 */
	@NonNull
	@SuppressLint("NewApi")
	public static String resolveCalendarFieldName(@NonNull Calendar calendar, int field, int style, @NonNull Locale locale) {
		if (!PRE_GINGERBREAD) {
			final String name = calendar.getDisplayName(field, style, locale);
			return name != null ? name : calendar.getDisplayName(field, style, UiConfig.DEFAULT_LOCALE);
		}
		switch (field) {
			case Calendar.MONTH:
				return PRE_GINGERBREAD_MONTH_NAMES[calendar.get(field)];
			case Calendar.DAY_OF_WEEK:
				return PRE_GINGERBREAD_DAY_NAMES[calendar.get(field) - 1];
		}
		return "";
	}

	/**
	 * Called from one of constructors of this view to perform its initialization.
	 * <p>
	 * Initialization is done via parsing of the specified <var>attrs</var> set and obtaining for
	 * this view specific data from it that can be used to configure this new view instance. The
	 * specified <var>defStyleAttr</var> and <var>defStyleRes</var> are used to obtain default data
	 * from the current theme provided by the specified <var>context</var>.
	 */
	@SuppressWarnings("ConstantConditions")
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		if (!isInEditMode()) {
			final Resources.Theme theme = context.getTheme();
			// Try to apply font presented within text appearance style.
			final TypedArray appearanceAttributes = theme.obtainStyledAttributes(attrs, new int[]{android.R.attr.textAppearance}, defStyleAttr, defStyleRes);
			final int appearance = appearanceAttributes.getResourceId(0, -1);
			if (appearance != -1) {
				FontApplier.applyFont(this, appearance);
			}
			appearanceAttributes.recycle();
			// Try to apply font presented within xml attributes.
			FontApplier.applyFont(this, attrs, defStyleAttr, defStyleRes);
		}
		// Default set up.
		this.mLocale = Locale.getDefault();
		this.handleLocaleChange();

		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_MonthView, defStyleAttr, defStyleRes);
		this.processTintValues(context, attributes);
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_MonthView_android_horizontalSpacing) {
				this.mSpacingHorizontal = attributes.getDimensionPixelSize(index, mSpacingHorizontal);
			} else if (index == R.styleable.Ui_MonthView_android_verticalSpacing) {
				this.mSpacingVertical = attributes.getDimensionPixelSize(index, mSpacingVertical);
			} else if (index == R.styleable.Ui_MonthView_uiMonthDayLettersOffsetVertical) {
				this.mDayLettersOffsetVertical = attributes.getDimensionPixelSize(index, mDayLettersOffsetVertical);
			} else if (index == R.styleable.Ui_MonthView_uiMonthDayNumbersOffsetVertical) {
				this.mDayNumbersOffsetVertical = attributes.getDimensionPixelOffset(index, mDayNumbersOffsetVertical);
			} else if (index == R.styleable.Ui_MonthView_uiMonthTitleTextAppearance) {
				setTitleTextAppearance(attributes.getResourceId(index, 0));
			} else if (index == R.styleable.Ui_MonthView_uiMonthDayLetterTextAppearance) {
				setDayLetterTextAppearance(attributes.getResourceId(index, 0));
			} else if (index == R.styleable.Ui_MonthView_uiMonthDayNumberTextAppearance) {
				setDayNumberTextAppearance(attributes.getResourceId(index, 0));
			} else if (index == R.styleable.Ui_MonthView_uiMonthDaySelector) {
				setDaySelector(attributes.getResourceId(index, 0));
			} else if (index == R.styleable.Ui_MonthView_uiMonthDaySelectorRadius) {
				setDaySelectorRadius(attributes.getDimensionPixelSize(index, mDaySelectorRadius));
			} else if (index == R.styleable.Ui_MonthView_uiMonthCurrentDayTextColor) {
				setCurrentDayTextColor(attributes.getColorStateList(index));
			}
		}
		attributes.recycle();
		this.applyDaySelectorTint();
	}

	/**
	 * Called from the constructor to process tint values for this view.
	 *
	 * @param context    The context passed to constructor.
	 * @param typedArray TypedArray obtained for styleable attributes specific for this view.
	 */
	@SuppressWarnings("All")
	private void processTintValues(Context context, TypedArray typedArray) {
		this.ensureTintInfo();
		if (typedArray.hasValue(R.styleable.Ui_MonthView_uiIndicatorTint)) {
			mTintInfo.tintList = typedArray.getColorStateList(R.styleable.Ui_MonthView_uiIndicatorTint);
		}
		mTintInfo.tintMode = TintManager.parseTintMode(
				typedArray.getInt(R.styleable.Ui_MonthView_uiIndicatorTintMode, 0),
				PorterDuff.Mode.SRC_IN
		);
		// If there is no tint mode specified within style/xml do not tint at all.
		if (mTintInfo.tintMode == null) mTintInfo.tintList = null;
		mTintInfo.hasTintList = mTintInfo.tintList != null;
		mTintInfo.hasTintMode = mTintInfo.tintMode != null;
	}

	/**
	 * Ensures that the tint info object is initialized.
	 */
	private void ensureTintInfo() {
		if (mTintInfo == null) this.mTintInfo = new TintInfo();
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(MonthView.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(MonthView.class.getName());
	}

	/**
	 * Registers a callback to be invoked whenever a specific day is selected within this month view.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 */
	public void setOnDaySelectionListener(@Nullable OnDaySelectionListener listener) {
		this.mSelectionListener = listener;
	}

	/**
	 * Sets a locale used to obtain proper names of calendar fields to be presented within this view.
	 *
	 * @param locale The desired locale.
	 * @see #getLocale()
	 */
	public void setLocale(@NonNull Locale locale) {
		if (!mLocale.equals(locale)) {
			this.mLocale = locale;
			this.handleLocaleChange();
		}
	}

	/**
	 * Handles change in the current locale. This will update day letters and will invoke
	 * {@link #handleDateUpdate()} so all necessary date related data will be updated and properly
	 * drawn in {@link #onDraw(Canvas)}.
	 */
	private void handleLocaleChange() {
		this.ensureCalendar(mLocale, true);
		mCalendar.clear();
		// Update day name letters.
		mCalendar.setFirstDayOfWeek(mFirstDayOfWeek);
		for (int i = 0; i < DAYS_IN_WEEK; i++) {
			mCalendar.set(Calendar.DAY_OF_WEEK, i + 1);
			final String letter = obtainCalendarFieldName(mCalendar, Calendar.DAY_OF_WEEK, CALENDAR_STYLE_SHORT, mLocale);
			DAY_LETTERS[i] = letter.substring(0, 1).toUpperCase();
		}
		this.handleDateUpdate();
	}

	/**
	 * Invoked to obtain a name of a calendar field with the specified <var>field</var> identifier
	 * from the given <var>calendar</var> for the specified <var>locale</var>.
	 * <p>
	 * This method is here primarily to support displaying of calendar field names in this view also
	 * for pre {@link android.os.Build.VERSION_CODES#GINGERBREAD GINGERBREAD}.
	 *
	 * @param calendar The calendar from which to obtain the requested field's name.
	 * @param field    The desired field identifier.
	 * @param style    Style flag for the requested name. One of {@link #CALENDAR_STYLE_SHORT} or
	 *                 {@link #CALENDAR_STYLE_LONG}.
	 * @param locale   The locale for which to obtain the requested name.
	 * @return Name of the requested calendar field.
	 * @see #resolveCalendarFieldName(Calendar, int, int, Locale)
	 */
	@NonNull
	protected String obtainCalendarFieldName(@NonNull Calendar calendar, int field, int style, @NonNull Locale locale) {
		return resolveCalendarFieldName(calendar, field, style, locale);
	}

	/**
	 * Ensures that the calendar with the specified <var>locale</var> is initialized.
	 *
	 * @param locale        The locale with which should be the calendar initialized.
	 * @param localeChanged {@code True} if locale has been changed so the current calendar need to
	 *                      be re-initialized {@code false} otherwise.
	 */
	private void ensureCalendar(Locale locale, boolean localeChanged) {
		if (mCalendar == null || localeChanged) this.mCalendar = Calendar.getInstance(locale);
	}

	/**
	 * Returns the current locale used to obtain proper names of calendar fields.
	 *
	 * @return Calendar's locale.
	 * @see #setLocale(Locale)
	 */
	@NonNull
	public Locale getLocale() {
		return mLocale;
	}

	/**
	 * Same as {@link #setDate(long)} for {@link Date} object.
	 *
	 * @param date The desired date to present by this month view.
	 * @see #getDate()
	 */
	public void setDate(@NonNull Date date) {
		if (DATE.getTime() != date.getTime()) {
			DATE.setTime(date.getTime());
			this.handleDateUpdate();
		}
	}

	/**
	 * Specifies a date in milliseconds of which data should be presented by this month view.
	 * <p>
	 * This view will present name of month and year from the specified <var>date</var> and full set
	 * of days for the obtained month that will be displayed in 7 columns starting in the column of
	 * the <b>first day of week</b>.
	 * <p>
	 * A selected day that should be highlighted can be specified via {@link #setSelection(int)}.
	 *
	 * @param dateInMillis The desired date in milliseconds to present by this month view.
	 * @see #getDateInMillis()
	 */
	public void setDate(long dateInMillis) {
		if (dateInMillis != DATE.getTime()) {
			DATE.setTime(dateInMillis);
			this.handleDateUpdate();
		}
	}

	/**
	 * Handles update in the date. This will update the current calendar and also month name, year
	 * number, set of days and starting day and invokes {@link #invalidate()} so the changes performed
	 * here will be immediately presented in the UI.
	 */
	private void handleDateUpdate() {
		this.ensureCalendar(mLocale, false);
		// Obtain data for the date specified.
		mCalendar.clear();
		mCalendar.setTime(DATE);
		this.mFirstDayOfWeek = mCalendar.getFirstDayOfWeek();
		this.mMonthName = obtainCalendarFieldName(mCalendar, Calendar.MONTH, CALENDAR_STYLE_LONG, mLocale);
		this.mMonthName = mMonthName.substring(0, 1).toUpperCase() + mMonthName.substring(1, mMonthName.length());
		this.mYear = mCalendar.get(Calendar.YEAR);
		this.mDaysCount = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		this.mStartDay = mCalendar.get(Calendar.DAY_OF_WEEK);
		final int month = mCalendar.get(Calendar.MONTH);
		// Obtain data for the current day.
		mCalendar.clear();
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		if (mYear == mCalendar.get(Calendar.YEAR) && month == mCalendar.get(Calendar.MONTH)) {
			this.mCurrentDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		} else {
			this.mCurrentDay = 0;
		}
		invalidate();
	}

	/**
	 * Same as {@link #getDateInMillis()} for {@link Date} object.
	 *
	 * @return This month's view date.
	 * @see #setDate(java.util.Date)
	 */
	@NonNull
	public Date getDate() {
		return new Date(getDateInMillis());
	}

	/**
	 * Returns the date of which month data are currently presented by this view.
	 *
	 * @return This month's view date in milliseconds.
	 * @see #setDate(long)
	 * @see #getDate()
	 */
	public long getDateInMillis() {
		return DATE.getTime();
	}

	/**
	 * Specifies a number of day that should be selected (highlighted by selector specified via
	 * {@link #setDaySelector(android.graphics.drawable.Drawable)}) for this month view.
	 *
	 * @param selection The desired day to be selected. May be {@code 0} to clear the current selection.
	 * @see #getSelection()
	 * @see #getSelectionDate()
	 */
	public void setSelection(@IntRange(from = 0, to = 31) int selection) {
		this.updateSelection(selection);
	}

	/**
	 * Updates the current selected day to the specified one.
	 *
	 * @param selection The current selected day within this month view.
	 */
	private void updateSelection(int selection) {
		if (mSelectedDay != selection) {
			this.mSelectedDay = Math.max(0, Math.min(selection, mDaysCount));
			if (mSelectedDay != 0) {
				this.notifyDaySelected();
			}
			invalidateDayNumbersArea();
		}
	}

	/**
	 * Notifies the current OnDaySelectionListener (if any) that a day has been selected.
	 */
	private void notifyDaySelected() {
		if (mSelectionListener != null) mSelectionListener.onDaySelected(this, mSelectedDay, getSelectionDateInMillis());
	}

	/**
	 * Returns the current selected day within this month view.
	 *
	 * @return The selected day or {@code 0} if no day is currently selected.
	 */
	@IntRange(from = 0, to = 31)
	public int getSelection() {
		return mSelectedDay;
	}

	/**
	 * Returns a boolean flag indicating whether there is selected some day within this month view
	 * or not.
	 *
	 * @return {@code True} if this month view has selected day, {@code false} otherwise.
	 */
	public boolean hasSelection() {
		return mSelectedDay != 0;
	}

	/**
	 * Same as {@link #getSelectionDateInMillis()} for {@link Date} object.
	 *
	 * @return Current selection date.
	 */
	@NonNull
	public Date getSelectionDate() {
		return new Date(getSelectionDateInMillis());
	}

	/**
	 * Returns the date containing data of date specified via {@link #setDate(java.util.Date)} and
	 * the current selected day as {@link Calendar#DAY_OF_MONTH}.
	 *
	 * @return Current selection date in milliseconds.
	 * @see #hasSelection()
	 * @see #getSelection()
	 * @see #getSelectionDate()
	 */
	public long getSelectionDateInMillis() {
		this.ensureCalendar(mLocale, false);
		mCalendar.clear();
		mCalendar.setTime(DATE);
		mCalendar.set(Calendar.DAY_OF_MONTH, mSelectedDay);
		return mCalendar.getTimeInMillis();
	}

	/**
	 * Sets a vertical offset for week day letters. This offset can be used to control space between
	 * title (month + year) and week day letters components.
	 *
	 * @param offset The desired offset in pixels.
	 * @see R.attr#uiMonthDayLettersOffsetVertical ui:uiMonthDayLettersOffsetVertical
	 * @see #getDayLettersOffsetVertical()
	 * @see #setDayNumbersOffsetVertical(int)
	 */
	public void setDayLettersOffsetVertical(@Px int offset) {
		if (mDayLettersOffsetVertical != offset) {
			this.mDayLettersOffsetVertical = offset;
			requestLayout();
		}
	}

	/**
	 * Returns the current vertical offset for week day letters.
	 *
	 * @return Day letters vertical offset.
	 * @see #setDayLettersOffsetVertical(int)
	 */
	@Px
	public int getDayLettersOffsetVertical() {
		return mDayLettersOffsetVertical;
	}

	/**
	 * Sets a vertical offset for day numbers table. This offset can be used to control space between
	 * week day letters and day numbers components.
	 *
	 * @param offset The desired offset in pixels.
	 * @see R.attr#uiMonthDayNumbersOffsetVertical ui:uiMonthDayNumbersOffsetVertical
	 * @see #getDayNumbersOffsetVertical()
	 * @see #setSpacing(int, int)
	 * @see #setDayLettersOffsetVertical(int)
	 */
	public void setDayNumbersOffsetVertical(@Px int offset) {
		if (mDayNumbersOffsetVertical != offset) {
			this.mDayNumbersOffsetVertical = offset;
			requestLayout();
		}
	}

	/**
	 * Returns the current vertical offset for day numbers table.
	 *
	 * @return Day numbers vertical offset.
	 * @see #setDayNumbersOffsetVertical(int)
	 */
	@Px
	public int getDayNumbersOffsetVertical() {
		return mDayNumbersOffsetVertical;
	}

	/**
	 * Sets a vertical and horizontal spacing used for day numbers table.
	 *
	 * @param horizontal The spacing in pixels applied to rows of day numbers table.
	 * @param vertical   The spacing in pixels applied to columns of day numbers table.
	 * @see android.R.attr#horizontalSpacing android:horizontalSpacing
	 * @see android.R.attr#verticalSpacing android:verticalSpacing
	 * @see #getSpacingHorizontal()
	 * @see #getSpacingVertical()
	 */
	public void setSpacing(@Px int horizontal, @Px int vertical) {
		if (mSpacingHorizontal != horizontal || mSpacingVertical != vertical) {
			this.mSpacingHorizontal = horizontal;
			this.mSpacingVertical = vertical;
			requestLayout();
		}
	}

	/**
	 * Returns the horizontal spacing for day numbers table.
	 *
	 * @return Horizontal spacing in pixels.
	 * @see #setSpacing(int, int)
	 * @see #getSpacingVertical()
	 */
	@Px
	public int getSpacingHorizontal() {
		return mSpacingHorizontal;
	}

	/**
	 * Returns the vertical spacing for day numbers table.
	 *
	 * @return Vertical spacing in pixels.
	 * @see #setSpacing(int, int)
	 * @see #getSpacingHorizontal()
	 */
	@Px
	public int getSpacingVertical() {
		return mSpacingVertical;
	}

	/**
	 * Sets a text color, size, and style for the title text (month + year) from the specified
	 * TextAppearance resource.
	 *
	 * @param resId Resource id of the desired TextAppearance style.
	 * @see R.attr#uiMonthTitleTextAppearance ui:uiMonthTitleTextAppearance
	 * @see #setTitleTextSize(int, float)
	 * @see #setTitleTextColor(ColorStateList)
	 * @see #setTitleTypeface(Typeface)
	 */
	public void setTitleTextAppearance(@StyleRes int resId) {
		if (TITLE_TEXT_INFO.fromTextAppearanceStyle(getContext(), resId) && TITLE_TEXT_INFO.updatePaint(getDrawableState())) {
			this.invalidateTitleArea();
		}
	}

	/**
	 * Same as {@link #setTitleTextSize(int, float)} in {@link TypedValue#COMPLEX_UNIT_SP} and the
	 * specified <var>size</var>.
	 *
	 * @see #getTitleTextSize()
	 */
	public void setTitleTextSize(float size) {
		setTitleTextSize(TypedValue.COMPLEX_UNIT_SP, size);
	}

	/**
	 * Sets a size for the title text (month + year) to the given <var>unit</var> and <var>size</var>.
	 *
	 * @param unit The desired dimension unit. See {@link TypedValue} for possible units.
	 * @param size The desired size in the specified unit.
	 * @see #setTitleTextSize(float)
	 * @see #getTitleTextSize()
	 */
	public void setTitleTextSize(int unit, float size) {
		setTitleRawTextSize(TypedValue.applyDimension(
				unit,
				size,
				getResources().getDisplayMetrics()
		));
	}

	/**
	 * Sets the raw text size for the Paint used to draw title graphics.
	 *
	 * @param size The desired raw size in pixels.
	 */
	private void setTitleRawTextSize(float size) {
		if (TITLE_TEXT_INFO.updateTextSize(size)) {
			this.invalidateTitleArea();
		}
	}

	/**
	 * Returns the size of the title text.
	 *
	 * @return Size used when drawing title text graphics.
	 * @see #setTitleTextSize(int, float)
	 * @see #setTitleTextAppearance(int)
	 */
	public float getTitleTextSize() {
		return TITLE_TEXT_INFO.paint.getTextSize();
	}

	/**
	 * Sets a single color for the title text (month + year).
	 *
	 * @param color The desired color.
	 * @see #setTitleTextColor(ColorStateList)
	 */
	public void setTitleTextColor(@ColorInt int color) {
		setTitleTextColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for the title text (month + year).
	 *
	 * @param colors The desired colors state list.
	 * @see #setTitleTextColor(int)
	 * @see #getTitleTextColors()
	 * @see #getTitleCurrentTextColor()
	 */
	public void setTitleTextColor(@NonNull ColorStateList colors) {
		if (TITLE_TEXT_INFO.updateTextColor(colors, getDrawableState())) {
			this.invalidateTitleArea();
		}
	}

	/**
	 * Returns the colors for the title text.
	 *
	 * @return List of colors used when drawing title text graphics.
	 * @see #setTitleTextColor(android.content.res.ColorStateList)
	 * @see #setTitleTextColor(int)
	 * @see #getTitleCurrentTextColor()
	 */
	@Nullable
	public ColorStateList getTitleTextColors() {
		return TITLE_TEXT_INFO.mAppearance.getTextColor();
	}

	/**
	 * Returns the current color used to draw the title text.
	 *
	 * @return Current title text color.
	 * @see #getTitleTextColors()
	 */
	@ColorInt
	public int getTitleCurrentTextColor() {
		return TITLE_TEXT_INFO.paint.getColor();
	}

	/**
	 * Sets a typeface and style in which the title text (month + year) should be displayed, and
	 * turns on the fake bold and italic bits in the Paint if the Typeface that you provided does
	 * not have all the bits in the style that you specified.
	 *
	 * @param typeface The desired typeface. May be {@code null} to create default one from the specified
	 *                 <var>style</var>.
	 * @param style    One of {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link Typeface#ITALIC}
	 *                 or {@link Typeface#BOLD_ITALIC}.
	 * @see #setTitleTypeface(Typeface)
	 * @see #getTitleTypeface()
	 * @see #getTitleTypefaceStyle()
	 */
	public void setTitleTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		if (TITLE_TEXT_INFO.updateTypeface(typeface, style)) {
			this.invalidateTitleArea();
		}
	}

	/**
	 * Sets a typeface in which the title text (month + year) should be displayed.
	 * <p>
	 * <b>Note</b>, that not all Typeface families actually have bold and italic variants, so you
	 * may need to use {@link #setTitleTypeface(Typeface, int)} to get the appearance that you actually
	 * want.
	 *
	 * @param typeface The desired typeface. May be {@code null} to use a default one.
	 * @see #getTitleTypeface()
	 * @see #getTitleTypefaceStyle()
	 */
	public void setTitleTypeface(@Nullable Typeface typeface) {
		if (TITLE_TEXT_INFO.updateTypeface(typeface)) {
			this.invalidateTitleArea();
		}
	}

	/**
	 * Returns the typeface used to draw title text.
	 *
	 * @return Title text typeface.
	 * @see #setTitleTypeface(Typeface, int)
	 * @see #setTitleTypeface(Typeface)
	 * @see #getTitleTypefaceStyle()
	 * @see #setTitleTextAppearance(int)
	 */
	@Nullable
	public Typeface getTitleTypeface() {
		return TITLE_TEXT_INFO.paint.getTypeface();
	}

	/**
	 * Returns the style of the typeface used to draw the title text.
	 *
	 * @return Typeface style.
	 * @see #getTitleTypeface()
	 * @see #setTitleTypeface(Typeface, int)
	 */
	@TextAppearance.TextStyle
	@SuppressWarnings("ResourceType")
	public int getTitleTypefaceStyle() {
		final Typeface typeface = TITLE_TEXT_INFO.paint.getTypeface();
		return typeface != null ? typeface.getStyle() : Typeface.NORMAL;
	}

	/**
	 * Sets a text color, size, and style for the first letters of day names from the specified
	 * TextAppearance resource.
	 *
	 * @param resId Resource id of the desired TextAppearance style.
	 * @see R.attr#uiMonthDayLetterTextAppearance ui:uiMonthDayLetterTextAppearance
	 * @see #setDayLetterTextSize(int, float)
	 * @see #setDayLetterTextColor(ColorStateList)
	 * @see #setDayLetterTypeface(Typeface)
	 */
	public void setDayLetterTextAppearance(@StyleRes int resId) {
		if (DAY_LETTER_TEXT_INFO.fromTextAppearanceStyle(getContext(), resId) && DAY_LETTER_TEXT_INFO.updatePaint(getDrawableState())) {
			this.invalidateDayLettersArea();
		}
	}

	/**
	 * Same as {@link #setDayLetterTextSize(int, float)} in {@link TypedValue#COMPLEX_UNIT_SP} and
	 * the specified <var>size</var>.
	 *
	 * @see #getDayLetterTextSize()
	 */
	public void setDayLetterTextSize(float size) {
		setDayLetterTextSize(TypedValue.COMPLEX_UNIT_SP, size);
	}

	/**
	 * Sets a size for the first letters of day names to the given <var>unit</var> and <var>size</var>.
	 *
	 * @param unit The desired dimension unit. See {@link TypedValue} for possible units.
	 * @param size The desired size in the specified unit.
	 * @see #setDayLetterTextSize(float)
	 * @see #getDayLetterTextSize()
	 */
	public void setDayLetterTextSize(int unit, float size) {
		setDayLetterRawTextSize(TypedValue.applyDimension(
				unit,
				size,
				getResources().getDisplayMetrics()
		));
	}

	/**
	 * Sets the raw text size for the Paint used to draw first letters of day names graphics.
	 *
	 * @param size The desired raw size in pixels.
	 */
	private void setDayLetterRawTextSize(float size) {
		if (DAY_LETTER_TEXT_INFO.updateTextSize(size)) {
			this.invalidateDayLettersArea();
		}
	}

	/**
	 * Returns the size of the first letters of day names.
	 *
	 * @return Size used when drawing day letters graphics.
	 * @see #setDayLetterTextSize(int, float)
	 * @see #setDayLetterTextAppearance(int)
	 */
	public float getDayLetterTextSize() {
		return DAY_LETTER_TEXT_INFO.paint.getTextSize();
	}

	/**
	 * Sets a single color for the first letters of day names.
	 *
	 * @param color The desired color.
	 * @see #setDayLetterTextColor(ColorStateList)
	 */
	public void setDayLetterTextColor(@ColorInt int color) {
		setDayLetterTextColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for the first letters of day names.
	 *
	 * @param colors The desired colors state list.
	 * @see #setDayLetterTextColor(int)
	 * @see #getDayLetterTextColors()
	 * @see #getDayLetterCurrentTextColor()
	 */
	public void setDayLetterTextColor(@NonNull ColorStateList colors) {
		if (DAY_LETTER_TEXT_INFO.updateTextColor(colors, getDrawableState())) {
			this.invalidateDayLettersArea();
		}
	}

	/**
	 * Returns the colors for the first letters of day names.
	 *
	 * @return List of colors used when drawing day letters graphics.
	 * @see #setDayLetterTextColor(android.content.res.ColorStateList)
	 * @see #setDayLetterTextColor(int)
	 * @see #getDayLetterCurrentTextColor()
	 */
	@NonNull
	public ColorStateList getDayLetterTextColors() {
		return DAY_LETTER_TEXT_INFO.mAppearance.getTextColor();
	}

	/**
	 * Returns the current color used to draw the first letters of day names.
	 *
	 * @return Current day letters color.
	 * @see #getDayLetterTextColors()
	 */
	@ColorInt
	public int getDayLetterCurrentTextColor() {
		return DAY_LETTER_TEXT_INFO.paint.getColor();
	}

	/**
	 * Sets a typeface and style in which the first letters of day names should be displayed, and
	 * turns on the fake bold and italic bits in the Paint if the Typeface that you provided does
	 * not have all the bits in the style that you specified.
	 *
	 * @param typeface The desired typeface. May be {@code null} to create default one from the specified
	 *                 <var>style</var>.
	 * @param style    One of {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link Typeface#ITALIC}
	 *                 or {@link Typeface#BOLD_ITALIC}.
	 * @see #setDayLetterTypeface(Typeface)
	 * @see #getDayLetterTypeface()
	 * @see #getDayLetterTypefaceStyle()
	 */
	public void setDayLetterTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		if (DAY_LETTER_TEXT_INFO.updateTypeface(typeface, style)) {
			this.invalidateDayLettersArea();
		}
	}

	/**
	 * Sets a typeface in which the first letters of day names should be displayed.
	 * <p>
	 * <b>Note</b>, that not all Typeface families actually have bold and italic variants, so you
	 * may need to use {@link #setDayLetterTypeface(Typeface, int)} to get the appearance that you
	 * actually want.
	 *
	 * @param typeface The desired typeface. May be {@code null} to use a default one.
	 * @see #getDayLetterTypeface()
	 * @see #getDayLetterTypefaceStyle()
	 */
	public void setDayLetterTypeface(@Nullable Typeface typeface) {
		if (DAY_LETTER_TEXT_INFO.updateTypeface(typeface)) {
			this.invalidateDayLettersArea();
		}
	}

	/**
	 * Returns the typeface used to draw first letters of day names.
	 *
	 * @return Day letters typeface.
	 * @see #setDayLetterTypeface(Typeface, int)
	 * @see #setDayLetterTypeface(Typeface)
	 * @see #getDayLetterTypefaceStyle()
	 * @see #setDayLetterTextAppearance(int)
	 */
	@Nullable
	public Typeface getDayLetterTypeface() {
		return DAY_LETTER_TEXT_INFO.paint.getTypeface();
	}

	/**
	 * Returns the style of the typeface used to draw the first letters of day names.
	 *
	 * @return Typeface style.
	 * @see #getDayLetterTypeface()
	 * @see #setDayLetterTypeface(Typeface, int)
	 */
	@TextAppearance.TextStyle
	@SuppressWarnings("ResourceType")
	public int getDayLetterTypefaceStyle() {
		final Typeface typeface = DAY_LETTER_TEXT_INFO.paint.getTypeface();
		return typeface != null ? typeface.getStyle() : Typeface.NORMAL;
	}

	/**
	 * Sets a text color, size, and style for day numbers from the specified TextAppearance resource.
	 *
	 * @param resId Resource id of the desired TextAppearance style.
	 * @see R.attr#uiMonthDayNumberTextAppearance ui:uiMonthDayNumberTextAppearance
	 * @see #setDayNumberTextSize(int, float)
	 * @see #setDayNumberTextColor(ColorStateList)
	 * @see #setDayNumberTypeface(Typeface)
	 */
	public void setDayNumberTextAppearance(@StyleRes int resId) {
		if (DAY_NUMBER_TEXT_INFO.fromTextAppearanceStyle(getContext(), resId) && DAY_NUMBER_TEXT_INFO.updatePaint(getDrawableState())) {
			this.updateDayNumberTextColors();
			this.invalidateDayNumbersArea();
		}
	}

	/**
	 * Same as {@link #setDayNumberTextSize(int, float)} in {@link TypedValue#COMPLEX_UNIT_SP} and
	 * the specified <var>size</var>.
	 *
	 * @see #getDayNumberTextSize()
	 */
	public void setDayNumberTextSize(float size) {
		setDayNumberTextSize(TypedValue.COMPLEX_UNIT_SP, size);
	}

	/**
	 * Sets a size for day numbers to the given <var>unit</var> and <var>size</var>.
	 *
	 * @param unit The desired dimension unit. See {@link TypedValue} for possible units.
	 * @param size The desired size in the specified unit.
	 * @see #setDayNumberTextSize(float)
	 * @see #getDayNumberTextSize()
	 */
	public void setDayNumberTextSize(int unit, float size) {
		setDayNumberRawTextSize(TypedValue.applyDimension(
				unit,
				size,
				getResources().getDisplayMetrics()
		));
	}

	/**
	 * Sets the raw text size for the Paint used to draw day numbers graphics.
	 *
	 * @param size The desired raw size in pixels.
	 */
	private void setDayNumberRawTextSize(float size) {
		if (DAY_LETTER_TEXT_INFO.updateTextSize(size)) {
			this.invalidateDayLettersArea();
		}
	}

	/**
	 * Returns the size of day numbers.
	 *
	 * @return Size used when drawing day numbers graphics.
	 * @see #setDayNumberTextSize(int, float)
	 * @see #setDayNumberTextAppearance(int)
	 */
	public float getDayNumberTextSize() {
		return DAY_LETTER_TEXT_INFO.paint.getTextSize();
	}

	/**
	 * Sets a single color for day numbers.
	 *
	 * @param color The desired color.
	 * @see #setDayNumberTextColor(ColorStateList)
	 */
	public void setDayNumberTextColor(@ColorInt int color) {
		setDayNumberTextColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for day numbers.
	 *
	 * @param colors The desired colors state list.
	 * @see #setDayNumberTextColor(int)
	 * @see #getDayNumberTextColors()
	 * @see #getDayNumberCurrentTextColor()
	 */
	public void setDayNumberTextColor(@NonNull ColorStateList colors) {
		DAY_NUMBER_TEXT_INFO.updateTextColor(colors, null);
		if (updateDayNumberTextColors()) {
			this.invalidateDayNumbersArea();
		}
	}

	/**
	 * Updates colors for different states hold by {@link #DAY_NUMBERS_COLORS_STATE} from the current
	 * {@link ColorStateList} specified via {@link #setDayNumberTextColor(ColorStateList)}.
	 *
	 * @return {@code True} if at least one color has been changed so invalidation of day numbers
	 * area is needed, {@code false} otherwise.
	 */
	private boolean updateDayNumberTextColors() {
		final ColorStateList colors = DAY_NUMBER_TEXT_INFO.mAppearance.getTextColor();
		int defColor = DayNumberColorsState.DEF_COLOR;
		if (colors.isStateful()) {
			defColor = colors.getDefaultColor();
			DAY_NUMBERS_COLORS_STATE.dayNumberColorNormal = colors.getColorForState(WidgetStateSet.ENABLED, defColor);
			DAY_NUMBERS_COLORS_STATE.dayNumberColorPressed = colors.getColorForState(WidgetStateSet.ENABLED_PRESSED, defColor);
			DAY_NUMBERS_COLORS_STATE.dayNumberColorSelected = colors.getColorForState(WidgetStateSet.ENABLED_SELECTED, defColor);
			DAY_NUMBERS_COLORS_STATE.dayNumberColorDisabled = colors.getColorForState(WidgetStateSet.DISABLED, defColor);
		} else {
			DAY_NUMBERS_COLORS_STATE.dayNumberColorNormal = defColor;
			DAY_NUMBERS_COLORS_STATE.dayNumberColorPressed = defColor;
			DAY_NUMBERS_COLORS_STATE.dayNumberColorSelected = defColor;
			DAY_NUMBERS_COLORS_STATE.dayNumberColorDisabled = defColor;
		}
		return true;
	}

	/**
	 * Returns the colors for day numbers.
	 *
	 * @return List of colors used when drawing day numbers graphics.
	 * @see #setDayNumberTextColor(android.content.res.ColorStateList)
	 * @see #setDayNumberTextColor(int)
	 * @see #getDayNumberCurrentTextColor()
	 */
	@NonNull
	public ColorStateList getDayNumberTextColors() {
		return DAY_NUMBER_TEXT_INFO.mAppearance.getTextColor();
	}

	/**
	 * Returns the current color used to draw day numbers.
	 *
	 * @return Current day numbers color.
	 * @see #getDayNumberTextColors()
	 */
	@ColorInt
	public int getDayNumberCurrentTextColor() {
		return DAY_NUMBER_TEXT_INFO.paint.getColor();
	}

	/**
	 * Sets a typeface and style in which day numbers should be displayed, and turns on the fake bold
	 * and italic bits in the Paint if the Typeface that you provided does not have all the bits in
	 * the style that you specified.
	 *
	 * @param typeface The desired typeface. May be {@code null} to create default one from the specified
	 *                 <var>style</var>.
	 * @param style    One of {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link Typeface#ITALIC}
	 *                 or {@link Typeface#BOLD_ITALIC}.
	 * @see #setDayNumberTypeface(Typeface)
	 * @see #getDayNumberTypeface()
	 * @see #getDayNumberTypefaceStyle()
	 */
	public void setDayNumberTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		if (DAY_NUMBER_TEXT_INFO.updateTypeface(typeface, style)) {
			this.invalidateDayNumbersArea();
		}
	}

	/**
	 * Sets a typeface in which day numbers should be displayed.
	 * <p>
	 * <b>Note</b>, that not all Typeface families actually have bold and italic variants, so you
	 * may need to use {@link #setDayLetterTypeface(Typeface, int)} to get the appearance that you
	 * actually want.
	 *
	 * @param typeface The desired typeface. May be {@code null} to use a default one.
	 * @see #getDayNumberTypeface()
	 * @see #getDayNumberTypefaceStyle()
	 */
	public void setDayNumberTypeface(@Nullable Typeface typeface) {
		if (DAY_NUMBER_TEXT_INFO.updateTypeface(typeface)) {
			this.invalidateDayNumbersArea();
		}
	}

	/**
	 * Returns the typeface used to draw day numbers.
	 *
	 * @return Day numbers typeface.
	 * @see #setDayNumberTypeface(Typeface, int)
	 * @see #setDayNumberTypeface(Typeface)
	 * @see #getDayNumberTypefaceStyle()
	 * @see #setDayNumberTextAppearance(int)
	 */
	@Nullable
	public Typeface getDayNumberTypeface() {
		return DAY_NUMBER_TEXT_INFO.paint.getTypeface();
	}

	/**
	 * Returns the style of the typeface used to draw day numbers.
	 *
	 * @return Typeface style.
	 * @see #getDayNumberTypeface()
	 * @see #setDayNumberTypeface(Typeface, int)
	 */
	@TextAppearance.TextStyle
	@SuppressWarnings("ResourceType")
	public int getDayNumberTypefaceStyle() {
		final Typeface typeface = DAY_NUMBER_TEXT_INFO.paint.getTypeface();
		return typeface != null ? typeface.getStyle() : Typeface.NORMAL;
	}


	/**
	 * Same as {@link #setDaySelector(Drawable)} for resource id.
	 *
	 * @param resId Resource id of the desired indicator's drawable. May be {@code 0} to clear the
	 *              current selector.
	 * @see #getDaySelector()
	 */
	@SuppressLint("NewApi")
	public void setDaySelector(@DrawableRes int resId) {
		if (resId == 0) {
			setDaySelector(null);
		} else if (mDaySelectorRes != resId) {
			setDaySelector(ResourceUtils.getDrawable(
					getResources(),
					mDaySelectorRes = resId,
					getContext().getTheme())
			);
		}
	}

	/**
	 * Sets a drawable used to draw the selector highlighting the current selected day.
	 *
	 * @param selector The desired drawable for day selector. May be {@code null} to clear the current
	 *                 one.
	 * @see R.attr#uiMonthDaySelector ui:uiMonthDaySelector
	 * @see #setDaySelectorTintList(ColorStateList)
	 * @see #setDaySelectorTintMode(PorterDuff.Mode)
	 * @see #getDaySelector()
	 */
	public void setDaySelector(@Nullable Drawable selector) {
		if (mDaySelector != selector) {
			final boolean needUpdate;
			if (mDaySelector != null) {
				mDaySelector.setCallback(null);
				unscheduleDrawable(mDaySelector);
				needUpdate = true;
			} else {
				needUpdate = false;
			}
			if (selector != null) {
				selector.setCallback(this);
				selector.setVisible(getVisibility() == VISIBLE, false);
			} else {
				this.mDaySelectorRes = 0;
			}
			this.mDaySelector = selector;
			this.applyDaySelectorTint();
			if (needUpdate) {
				if (mDaySelector.isStateful()) {
					mDaySelector.setState(getDrawableState());
				}
				invalidate();
			}
		}
	}

	/**
	 * Returns the current day selector's drawable.
	 * <p>
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setDaySelectorTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped indicator drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 *
	 * @return Day selector's drawable.
	 * @see #setDaySelector(Drawable)
	 */
	@Nullable
	public Drawable getDaySelector() {
		return mDaySelector;
	}

	/**
	 * Applies a tint to the day selector, if specified. This call does not modify the current
	 * tint mode, which is {@link android.graphics.PorterDuff.Mode#SRC_IN} by default.
	 * <p>
	 * Subsequent calls to {@link #setDaySelector(android.graphics.drawable.Drawable)} will
	 * automatically mutate the drawable and apply the specified tint and tint mode using
	 * {@link android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)}.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see R.attr#uiIndicatorTint ui:uiIndicatorTint
	 * @see #getDaySelectorTintList()
	 * @see android.graphics.drawable.Drawable#setTintList(android.content.res.ColorStateList)
	 */
	public void setDaySelectorTintList(@Nullable ColorStateList tint) {
		this.ensureTintInfo();
		mTintInfo.tintList = tint;
		mTintInfo.hasTintList = true;
		this.applyDaySelectorTint();
	}

	/**
	 * Returns the tint applied to the day selector's drawable, if specified.
	 *
	 * @return The day selector's drawable tint.
	 * @see #setDaySelectorTintList(android.content.res.ColorStateList)
	 */
	@Nullable
	public ColorStateList getDaySelectorTintList() {
		return mTintInfo != null ? mTintInfo.tintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setDaySelectorTintList(android.content.res.ColorStateList)}
	 * to the discrete indicator. The default mode is {@link android.graphics.PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see R.attr#uiIndicatorTintMode ui:uiIndicatorTintMode
	 * @see #getDaySelectorTintMode()
	 * @see android.graphics.drawable.Drawable#setTintMode(android.graphics.PorterDuff.Mode)
	 */
	public void setDaySelectorTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureTintInfo();
		mTintInfo.tintMode = tintMode;
		mTintInfo.hasTintMode = true;
		this.applyDaySelectorTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the day selector's drawable, if specified.
	 *
	 * @return The day selector's drawable blending mode used to apply the tint.
	 * @see #setDaySelectorTintMode(android.graphics.PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getDaySelectorTintMode() {
		return mTintInfo != null ? mTintInfo.tintMode : null;
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the current day selector's drawable.
	 */
	@SuppressWarnings("NewApi")
	private void applyDaySelectorTint() {
		if (mTintInfo == null ||
				(!mTintInfo.hasTintList && !mTintInfo.hasTintMode) ||
				mDaySelector == null) {
			return;
		}
		if (UiConfig.MATERIALIZED) {
			mDaySelector.mutate();
			if (mTintInfo.hasTintList) {
				mDaySelector.setTintList(mTintInfo.tintList);
			}
			if (mTintInfo.hasTintMode) {
				mDaySelector.setTintMode(mTintInfo.tintMode);
			}
			return;
		}
		final boolean isTintDrawable = mDaySelector instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) mDaySelector : new TintDrawable(mDaySelector);
		if (mTintInfo.hasTintList) {
			tintDrawable.setTintList(mTintInfo.tintList);
		}
		if (mTintInfo.hasTintMode) {
			tintDrawable.setTintMode(mTintInfo.tintMode);
		}
		if (tintDrawable.isStateful()) {
			tintDrawable.setState(getDrawableState());
		}
		if (isTintDrawable) {
			return;
		}
		this.mDaySelector = tintDrawable;
		mDaySelector.setCallback(this);
	}

	/**
	 * Sets a radius in which should be drawn the day selector's drawable specified via
	 * {@link #setDaySelector(android.graphics.drawable.Drawable)}.
	 *
	 * @param radius The desired radius in pixels. May be {@code 0} to not draw the day selector.
	 * @see R.attr#uiMonthDaySelectorRadius ui:uiMonthDaySelectorRadius
	 * @see #getDaySelectorRadius()
	 */
	public void setDaySelectorRadius(@Px int radius) {
		if (mDaySelectorRadius != radius && radius >= 0) {
			this.mDaySelectorRadius = radius;
			invalidate();
		}
	}

	/**
	 * Returns the current radius of the day selector's drawable.
	 *
	 * @return Day selector's radius in pixels.
	 * @see #setDaySelectorRadius(int)
	 */
	@Px
	public int getDaySelectorRadius() {
		return mDaySelectorRadius;
	}

	/**
	 * Sets a single color for the current day's number text.
	 *
	 * @param color The desired color.
	 * @see R.attr#uiMonthCurrentDayTextColor ui:uiMonthCurrentDayTextColor
	 * @see #setCurrentDayTextColor(ColorStateList)
	 */
	public void setCurrentDayTextColor(@ColorInt int color) {
		setCurrentDayTextColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for the current day's number text.
	 *
	 * @param colors The desired colors state list.
	 * @see R.attr#uiMonthCurrentDayTextColor ui:uiMonthCurrentDayTextColor
	 * @see #setCurrentDayTextColor(int)
	 * @see #getCurrentDayTextColors()
	 * @see #getCurrentDayCurrentTextColor()
	 */
	public void setCurrentDayTextColor(@NonNull ColorStateList colors) {
		this.mCurrentDayTextColors = colors;
		int currentColor = mCurrentDayTextColors.getDefaultColor();
		if (mCurrentDayTextColors.isStateful()) {
			currentColor = mCurrentDayTextColors.getColorForState(getDrawableState(), currentColor);
		}
		if (currentColor != mCurrentDayCurrentTextColor) {
			this.mCurrentDayCurrentTextColor = currentColor;
			invalidateDayNumbersArea();
		}
	}

	/**
	 * Returns the colors for the current day's number text.
	 *
	 * @return List of colors used when drawing current day's number graphics.
	 * @see #setCurrentDayTextColor(ColorStateList)
	 * @see #setCurrentDayTextColor(int)
	 * @see #getCurrentDayCurrentTextColor() ()
	 */
	@NonNull
	public ColorStateList getCurrentDayTextColors() {
		return mCurrentDayTextColors;
	}

	/**
	 * Returns the current color used to draw the current day's number.
	 *
	 * @return Current day's number text color.
	 * @see #getTitleTextColors()
	 */
	@ColorInt
	public int getCurrentDayCurrentTextColor() {
		return mCurrentDayCurrentTextColor;
	}

	/**
	 */
	@Override
	public void setFont(@NonNull String fontPath) {
		FontApplier.applyFont(this, fontPath);
	}

	/**
	 */
	@Override
	public void setFont(@Nullable Font font) {
		FontApplier.applyFont(this, font);
	}

	/**
	 * Sets a <b>global</b> typeface and style in which all text of this view should be displayed,
	 * and turns on the fake bold and italic bits in the Paint if the Typeface that you provided does
	 * not have all the bits in the style that you specified.
	 *
	 * @param typeface The desired typeface. May be {@code null} to create default one from the specified
	 *                 <var>style</var>.
	 * @param style    One of {@link android.graphics.Typeface#BOLD}, {@link android.graphics.Typeface#ITALIC}
	 *                 or {@link android.graphics.Typeface#BOLD_ITALIC}.
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface, int style) {
		if (TITLE_TEXT_INFO.updateTypeface(typeface, style)) {
			this.invalidateTitleArea();
		}
		if (DAY_LETTER_TEXT_INFO.updateTypeface(typeface, style)) {
			this.invalidateDayLettersArea();
		}
		if (DAY_NUMBER_TEXT_INFO.updateTypeface(typeface, style)) {
			this.invalidateDayNumbersArea();
		}
	}

	/**
	 * Sets a <b>global</b> typeface in which all text of this view should be displayed.
	 *
	 * @param typeface The desired typeface. May be {@code null} to use a default one.
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface) {
		if (TITLE_TEXT_INFO.updateTypeface(typeface)) {
			this.invalidateTitleArea();
		}
		if (DAY_LETTER_TEXT_INFO.updateTypeface(typeface)) {
			this.invalidateDayLettersArea();
		}
		if (DAY_NUMBER_TEXT_INFO.updateTypeface(typeface)) {
			this.invalidateDayNumbersArea();
		}
	}

	/**
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int paddingTop = getPaddingTop();
		final int paddingBottom = getPaddingBottom();
		final int paddingLeft = getPaddingLeft();
		final int paddingRight = getPaddingRight();
		int width, height;
		width = height = 0;
		// Count for title text with its half height + half vertical spacing.
		height += (int) TITLE_TEXT_INFO.paint.getTextSize() + mDayLettersOffsetVertical;
		// Count for day name first letter text with single vertical spacing.
		DAY_LETTER_TEXT_INFO.paint.getTextBounds(DAY_LETTERS[0], 0, 1, TEXT_BOUNDS);
		height += TEXT_BOUNDS.height() + mDayNumbersOffsetVertical;
		TOUCHABLE_AREA.top = paddingTop + height - mDaySelectorRadius;
		// Compute width depending on the horizontal spacing + append height with space needed to
		// draw max 6 rows of month days.
		DAY_NUMBER_TEXT_INFO.paint.getTextBounds(Integer.toString(MAX_DAY_IN_MONTH), 0, 1, TEXT_BOUNDS);
		width += mSpacingHorizontal * (DAYS_IN_WEEK - 1) + TEXT_BOUNDS.width() * 2;
		height += mSpacingVertical * (MAX_DAYS_ROWS - 1) + TEXT_BOUNDS.height();
		TOUCHABLE_AREA.bottom = paddingTop + height + mDaySelectorRadius;
		// Take into count also padding.
		width += paddingLeft + paddingRight;
		height += paddingTop + paddingBottom;
		// Update touchable area left, right values.
		TOUCHABLE_AREA.left = paddingLeft - mDaySelectorRadius;
		TOUCHABLE_AREA.right = width - paddingRight + mDaySelectorRadius;
		// Take into count also suggested minimum width and height.
		setMeasuredDimension(
				Math.max(width, getSuggestedMinimumWidth()),
				Math.max(height, getSuggestedMinimumHeight())
		);
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.mWidth = w;
	}

	/**
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (!isEnabled()) {
			return super.onTouchEvent(event);
		}
		boolean processed = false;
		final float touchX = event.getX();
		final float touchY = event.getY();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if (isDaysAreaTouched(touchX, touchY)) {
					final int touchedDay = findTouchedDay(touchX, touchY);
					if (touchedDay > 0 && touchedDay <= mDaysCount) {
						this.updatePrivateFlags(PFLAG_DAY_TOUCHED, true);
						this.mPressedDay = touchedDay;
						invalidate();
						processed = true;
					}
					this.mDragTouchX = touchX;
					this.mDragTouchY = touchY;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mPressedDay > 0 && (Math.abs(touchX - mDragTouchX) > mDaySelectorRadius || (Math.abs(touchY - mDragTouchY) > mDaySelectorRadius))) {
					this.mDragTouchX = mDragTouchY = 0;
					// Cancel pressed state.
					this.updatePrivateFlags(PFLAG_DAY_TOUCHED, false);
					this.mPressedDay = 0;
					invalidate();
					// todo: allow selection via horizontal drag
				}
				break;
			case MotionEvent.ACTION_UP:
				if ((mPrivateFlags & PFLAG_DAY_TOUCHED) != 0) {
					this.updatePrivateFlags(PFLAG_DAY_TOUCHED, false);
					this.updateSelection(mPressedDay);
					this.mPressedDay = 0;
					processed = true;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				this.updatePrivateFlags(PFLAG_DAY_TOUCHED, false);
				this.mPressedDay = 0;
				invalidate();
				processed = true;
				break;
		}
		return super.onTouchEvent(event) || processed;
	}

	/**
	 * Checks whether the area with the current drawn days is touched by touch with the specified
	 * coordinates or not.
	 *
	 * @param touchX X coordinate of the touch.
	 * @param touchY Y coordinate of the touch.
	 * @return {@code True} if days area has been touched, {@code false} otherwise.
	 */
	private boolean isDaysAreaTouched(float touchX, float touchY) {
		return touchX >= TOUCHABLE_AREA.left && touchX <= TOUCHABLE_AREA.right &&
				touchY >= TOUCHABLE_AREA.top && touchY <= TOUCHABLE_AREA.bottom;
	}

	/**
	 * Founds a day among the current set of drawn days that is in area of touch with the specified
	 * coordinates.
	 *
	 * @param touchX X coordinate of the touch.
	 * @param touchY Y coordinate of the touch.
	 * @return Day number from the range {@code [1, 31]} or negative number if no day from the current
	 * ones has been touched.
	 */
	private int findTouchedDay(float touchX, float touchY) {
		DAY_NUMBER_TEXT_INFO.paint.getTextBounds(Integer.toString(MAX_DAY_IN_MONTH), 0, 2, TEXT_BOUNDS);
		// Remove padding data from touch coordinates to obtain touch coordinates for area only with
		// day numbers.
		touchX -= TOUCHABLE_AREA.left + mDaySelectorRadius;
		touchY -= TOUCHABLE_AREA.top + mDaySelectorRadius;
		final int column = Math.min(DAYS_IN_WEEK, Math.round(touchX / mSpacingHorizontal) + 1);
		final int row = Math.min(MAX_DAYS_ROWS, Math.round(touchY / mSpacingVertical) + 1);
		return (row * DAYS_IN_WEEK - (DAYS_IN_WEEK - column)) - (mStartDay - 1);
	}

	/**
	 */
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		// We do not update state of day selector here but it is updated within onDraw(...) due to
		// proper states handling.
		if (updateTextColors(getDrawableState()) || (mDaySelector != null && mDaySelector.isStateful())) {
			invalidate();
		}
	}

	/**
	 * Updates the current text colors according to the current state of this view.
	 *
	 * @param stateSet Current drawable state for this view.
	 * @return {@code True} whenever {@link #invalidate()} should be called due to changed colors,
	 * {@code false} otherwise.
	 */
	private boolean updateTextColors(int[] stateSet) {
		boolean invalidate = TITLE_TEXT_INFO.updatePaintColor(stateSet);
		invalidate |= DAY_LETTER_TEXT_INFO.updatePaintColor(stateSet);
		// We do not update day number paint color here, because it is updated in onDraw(...) method,
		// due to handling of selected/pressed states properly.
		invalidate |= DAY_NUMBER_TEXT_INFO.mAppearance.getTextColor().isStateful();
		final int currentDayColor = mCurrentDayTextColors.getColorForState(stateSet, mCurrentDayTextColors.getDefaultColor());
		if (currentDayColor != mCurrentDayCurrentTextColor) {
			this.mCurrentDayCurrentTextColor = currentDayColor;
			invalidate = true;
		}
		return invalidate;
	}

	/**
	 */
	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mDaySelector || super.verifyDrawable(who);
	}

	/**
	 * Invalidates this view in area where the title text is presented using its current bounds.
	 *
	 * @see #invalidate(Rect)
	 */
	private void invalidateTitleArea() {
		if (!TextUtils.isEmpty(mMonthName)) {
			final int top = getPaddingTop();
			final String title = mMonthName + " " + Integer.toString(mYear);
			TITLE_TEXT_INFO.paint.getTextBounds(title, 0, title.length(), TEXT_BOUNDS);
			invalidate(
					getPaddingLeft(),
					top,
					getWidth() - getPaddingRight(),
					top + TEXT_BOUNDS.height()
			);
		}
	}

	/**
	 * Invalidates this view in area where the first day letters are presented using theirs current
	 * bounds.
	 *
	 * @see #invalidate(Rect)
	 */
	private void invalidateDayLettersArea() {
		if (!TextUtils.isEmpty(mMonthName) && !TextUtils.isEmpty(DAY_LETTERS[0])) {
			TITLE_TEXT_INFO.paint.getTextBounds(mMonthName, 0, mMonthName.length(), TEXT_BOUNDS);
			final int top = getPaddingTop() + TEXT_BOUNDS.height() + mDayLettersOffsetVertical;
			DAY_LETTER_TEXT_INFO.paint.getTextBounds(DAY_LETTERS[0], 0, 1, TEXT_BOUNDS);
			invalidate(
					getPaddingLeft(),
					top,
					getWidth() - getPaddingRight(),
					top + TEXT_BOUNDS.height()
			);
		}
	}

	/**
	 * Invalidates this view in area where the day numbers are presented using theirs current bounds.
	 *
	 * @see #invalidate(Rect)
	 */
	private void invalidateDayNumbersArea() {
		invalidate(
				(int) TOUCHABLE_AREA.left,
				(int) TOUCHABLE_AREA.top,
				(int) TOUCHABLE_AREA.right,
				(int) TOUCHABLE_AREA.bottom
		);
	}

	/**
	 * @see #onDrawTitle(Canvas, float, Paint)
	 * @see #onDrawWeekDayLetters(Canvas, float, Paint)
	 * @see #onDrawDayNumbers(Canvas, float, Paint)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float top = getPaddingTop();
		top = onDrawTitle(canvas, top, TITLE_TEXT_INFO.paint);
		top = onDrawWeekDayLetters(canvas, top + mDayLettersOffsetVertical, DAY_LETTER_TEXT_INFO.paint);
		onDrawDayNumbers(canvas, top + mDayNumbersOffsetVertical, DAY_NUMBER_TEXT_INFO.paint);
	}

	/**
	 * Invoked to draw title of this month view from the specified <var>top</var> position.
	 * <p>
	 * Default implementation draws the name of month and current year number for the date specified
	 * for this month view side by side (horizontally) on the specified <var>canvas</var>.
	 *
	 * @param canvas The canvas on which to draw the title (month name + year number).
	 * @param top    Top position from which to start the drawing. By default a value from
	 *               {@link #getPaddingTop()} is used.
	 * @param paint  Paint for title text ready to be used for drawing.
	 * @return Updated top position from which following graphic components (week day letters, day numbers)
	 * of this view will be drawn.
	 * @see #onDrawWeekDayLetters(Canvas, float, Paint)
	 * @see #onDrawDayNumbers(Canvas, float, Paint)
	 */
	protected float onDrawTitle(@NonNull Canvas canvas, float top, @NonNull Paint paint) {
		final CharSequence titleText = mMonthName + " " + Integer.toString(mYear);
		top += (int) paint.getTextSize();
		canvas.drawText(
				titleText,
				0,
				titleText.length(),
				mWidth / 2,
				top,
				paint
		);
		return top;
	}

	/**
	 * Invoked to draw first letters of days in week from the specified <var>top</var> position.
	 * <p>
	 * Default implementation draws day letters in one row on the specified <var>canvas</var>.
	 *
	 * @param canvas The canvas on which to draw day letters.
	 * @param top    Top position from which to start the drawing. This is the position returned from
	 *               {@link #onDrawTitle(Canvas, float, Paint)} method {@code +} {@link #getDayLettersOffsetVertical()}.
	 * @param paint  Paint for day letters ready to be used for drawing.
	 * @return Updated top position from which following graphic components (day numbers) of this view
	 * will be drawn.
	 * @see #onDrawDayNumbers(Canvas, float, Paint)
	 */
	protected float onDrawWeekDayLetters(@NonNull Canvas canvas, float top, @NonNull Paint paint) {
		/**
		 * We will draw 7 columns for each day in week.
		 */
		int dayIndex = mFirstDayOfWeek - 1;
		paint.getTextBounds(Integer.toString(MAX_DAY_IN_MONTH), 0, 2, TEXT_BOUNDS);
		float left = getPaddingLeft() + TEXT_BOUNDS.width() / 2f;
		top += (int) paint.getTextSize();
		for (int i = 0; i < DAYS_IN_WEEK; i++) {
			final String dayLetter = DAY_LETTERS[dayIndex];
			canvas.drawText(dayLetter, 0, 1, left, top, paint);
			left += mSpacingHorizontal;
			if (++dayIndex >= DAYS_IN_WEEK) {
				dayIndex = 0;
			}
		}
		return top;
	}

	/**
	 * Invoked to draw day numbers.
	 * <p>
	 * Default implementation draws all days of the month for the date specified for this month view
	 * on the specified <var>canvas</var> in {@link #DAYS_IN_WEEK} columns and max {@link #MAX_DAYS_ROWS}
	 * rows.
	 *
	 * @param canvas The canvas on which to draw day numbers.
	 * @param top    Top position from which to start the drawing. This is the position returned from
	 *               {@link #onDrawWeekDayLetters(Canvas, float, Paint)} method {@code +} {@link #getDayNumbersOffsetVertical()}.
	 * @param paint  Paint for day numbers ready to be used for drawing.
	 */
	protected void onDrawDayNumbers(@NonNull Canvas canvas, float top, @NonNull Paint paint) {
		/**
		 * Draw days of month within grid 7x6 depending on the start day.
		 */
		int day = 1;
		int startCol = mStartDay;
		paint.getTextBounds(Integer.toString(MAX_DAY_IN_MONTH), 0, 2, TEXT_BOUNDS);
		final float textHeight = TEXT_BOUNDS.height();
		final float textWidth = TEXT_BOUNDS.width();
		final float leftOrigin = getPaddingLeft() + textWidth / 2f;
		float left = leftOrigin + (startCol - 1) * mSpacingHorizontal;
		for (int row = 1; row <= MAX_DAYS_ROWS; row++) {
			if (day > mDaysCount) {
				break;
			}
			for (int col = startCol; col <= DAYS_IN_WEEK; col++) {
				if (day > mDaysCount) {
					break;
				}
				final boolean pressedDay = day == mPressedDay;
				final boolean selectedDay = day == mSelectedDay;
				// Draw selector for selected/pressed day.
				if (selectedDay || pressedDay) {
					drawDaySelector(canvas, left, top, day);
				}
				// Draw day number text.
				mDay.number = day;
				mDay.current = day == mCurrentDay;
				mDay.pressed = pressedDay;
				mDay.selected = selectedDay;
				mDay.bounds.set(
						left - textWidth / 2f,
						top,
						left + textWidth / 2f,
						top + textHeight
				);
				onDrawDayNumber(canvas, mDay, paint);
				left += mSpacingHorizontal;
				day++;
			}
			startCol = 1;
			left = leftOrigin;
			top += mSpacingVertical;
		}
	}

	/**
	 * Draws the current day selector (if specified) on the specified <var>canvas</var>.
	 *
	 * @param canvas The canvas on which to draw the selector.
	 * @param left   Left position from which to start the drawing.
	 * @param top    Top position from which to start the drawing.
	 * @param day    Day fow which to draw the selector. Day is used to resolve whether to draw the
	 *               selector in selected or pressed state.
	 */
	private void drawDaySelector(Canvas canvas, float left, float top, int day) {
		if (mDaySelector != null) {
			final int selectorCenterX = Math.round(left);
			final int selectorCenterY = Math.round(top + TEXT_BOUNDS.height() / 2f);
			mDaySelector.setBounds(
					selectorCenterX - mDaySelectorRadius,
					selectorCenterY - mDaySelectorRadius,
					selectorCenterX + mDaySelectorRadius,
					selectorCenterY + mDaySelectorRadius
			);
			if (isEnabled()) {
				if (mSelectedDay == day) {
					mDaySelector.setState(WidgetStateSet.ENABLED_SELECTED);
				} else if (mPressedDay == day) {
					mDaySelector.setState(WidgetStateSet.ENABLED_PRESSED);
				}
			} else {
				mDaySelector.setState(mSelectedDay == day ?
						WidgetStateSet.DISABLED_SELECTED :
						WidgetStateSet.DISABLED
				);
			}
			mDaySelector.draw(canvas);
		}
	}

	/**
	 * Invoked from {@link #onDrawDayNumbers(Canvas, float, Paint)} for each day number of the current
	 * month do draw a number text for the specified <var>day</var>.
	 *
	 * @param canvas The canvas on which to draw day number text.
	 * @param day    The day for which to draw the number text.
	 * @param paint  Paint for day number ready (text size and typeface are already specified, but
	 *               text color will be resolved depending on the given <var>day</var> state) to be
	 *               used for drawing.
	 * @see Day#isPressed()
	 * @see Day#isSelected()
	 */
	protected void onDrawDayNumber(@NonNull Canvas canvas, @NonNull Day day, @NonNull Paint paint) {
		int textColor = DAY_NUMBERS_COLORS_STATE.dayNumberColorNormal;
		if (isEnabled()) {
			if (day.selected) {
				textColor = DAY_NUMBERS_COLORS_STATE.dayNumberColorSelected;
			} else if (day.pressed) {
				textColor = DAY_NUMBERS_COLORS_STATE.dayNumberColorPressed;
			} else if (day.current) {
				textColor = mCurrentDayCurrentTextColor;
			}
		} else {
			if (day.current) {
				textColor = mCurrentDayCurrentTextColor;
			} else {
				textColor = DAY_NUMBERS_COLORS_STATE.dayNumberColorDisabled;
			}
		}
		paint.setColor(textColor);
		paint.setFakeBoldText(day.isCurrent());
		canvas.drawText(Integer.toString(day.number), day.bounds.centerX(), day.bounds.bottom, paint);
	}

	/**
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.date = DATE.getTime();
		savedState.selectedDay = mSelectedDay;
		savedState.locale = mLocale;
		return savedState;
	}

	/**
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		DATE.setTime(savedState.date);
		this.mSelectedDay = savedState.selectedDay;
		this.mLocale = savedState.locale;
		// Locale change handling will update day name letters + will fire date change handling + invalidation.
		this.handleLocaleChange();
	}

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

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link MonthView}
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
		long date;

		/**
		 */
		int selectedDay;

		/**
		 */
		Locale locale;

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
			this.date = source.readLong();
			this.selectedDay = source.readInt();
			this.locale = (Locale) source.readSerializable();
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeLong(date);
			dest.writeInt(selectedDay);
			dest.writeSerializable(locale);
		}
	}

	/**
	 * Represents a single day to be drawn for via {@link #onDrawDayNumber(Canvas, Day, Paint)}.
	 */
	protected static final class Day {

		/**
		 * This day's number.
		 */
		int number;

		/**
		 * Boolean flag indicating whether this day is current or not.
		 */
		boolean current;

		/**
		 * Boolean flag indicating whether this day is pressed or not.
		 */
		boolean pressed;

		/**
		 * Boolean flag indicating whether this day is selected or not.
		 */
		boolean selected;

		/**
		 * Bounds of this day determining where to draw its number on a Canvas.
		 */
		final RectF bounds = new RectF();

		/**
		 * Returns the number of this day.
		 *
		 * @return This day's number (as month day from the range {@code [1, 31]}).
		 */
		@IntRange(from = 1, to = 31)
		protected int getNumber() {
			return number;
		}

		/**
		 * Returns a boolean flag indicating whether this day is current or not.
		 *
		 * @return {@code True} if number of this day is same as current calendar day of month,
		 * {@code false} otherwise.
		 */
		protected boolean isCurrent() {
			return current;
		}

		/**
		 * Returns a boolean flag indicating whether this day is pressed or not.
		 *
		 * @return {@code True} if area within {@link MonthView} for this day is at this time pressed
		 * by a user, {@code false} otherwise.
		 */
		protected boolean isPressed() {
			return pressed;
		}

		/**
		 * Returns a boolean flag indicating whether this day is selected or not.
		 *
		 * @return {@code True} if number of this day has been selected (either programmatically via
		 * {@link #setSelection(int)} or by a user via touch), {@code false} otherwise.
		 */
		protected boolean isSelected() {
			return selected;
		}

		/**
		 * Returns the bounds of this day's number that will be used to properly position its text
		 * representation on a {@link Canvas} when drawing this day via {@link #onDrawDayNumber(Canvas, Day, Paint)}
		 * method. These bounds can be used to draw graphics related to this day like event indicators
		 * or a custom day selector.
		 * <p>
		 * <b>Note, that the {@link Rect#top} and {@link Rect#bottom} will be the exact positions
		 * where the text will be drawn, but {@link Rect#left} and {@link Rect#right} positions will
		 * be 'relative'/general for the text representation of number '31'.</b>
		 *
		 * @return Number text bounds.
		 */
		@NonNull
		protected RectF getBounds() {
			return bounds;
		}
	}

	/**
	 * This class holds all colors used to draw day numbers graphics.
	 */
	private static final class DayNumberColorsState {

		/**
		 * Default color used for drawing graphics of this widget.
		 */
		static final int DEF_COLOR = Color.BLACK;

		/**
		 * Color used to draw day number text in normal (enabled) state.
		 */
		int dayNumberColorNormal = DEF_COLOR;

		/**
		 * Color used to draw day number text in pressed state.
		 */
		int dayNumberColorPressed = DEF_COLOR;

		/**
		 * Color used to draw day number text in selected state.
		 */
		int dayNumberColorSelected = DEF_COLOR;

		/**
		 * Color used to draw day number text in disabled state.
		 */
		int dayNumberColorDisabled = DEF_COLOR;
	}
}
