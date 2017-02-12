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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * A view that can be used to present a circular picker within a layout that allows to a user to pick
 * one number from the predefined set of numbers that are drawn in arc.
 * <p>
 * Use {@link #setNumbers(int[])} to specify set of numbers from which can a user pick one. If you
 * want to allow to the user to pick from "more discrete" interval than that which is described
 * by the count of specified numbers, you can do so by specifying selection range via {@link #setSelectionRange(int)}
 * that will be used to properly pick the current selected number. This is useful for example when
 * you want to display only 12 numbers representing set of minutes within one hour, but want to allow
 * to the user to pick the desired minute from the range {@code [0, 59]}, than you will specify
 * numbers for minutes {@code CircularNumberPicker.setNumbers({0, 5, ..., 55})} and selection range
 * {@code CircularNumberPicker.setSelectionRange(60)} so the user will be able to pick also minutes
 * between {@code [0, 5]}, {@code [5, 10]} and so on.
 * <p>
 * Initial selected number can be specified via {@link #setSelection(int)} and the current selected
 * can be obtained via {@link #getSelection()}.
 * <p>
 * <b>Note</b>, that this view saves its current state, so it can be later restored (in case of
 * orientation change for example).
 *
 * <h3>Callbacks</h3>
 * Use {@link OnNumberSelectionListener} to listen for callback for a selected number. This listener
 * can be registered via {@link #setOnNumberSelectionListener(OnNumberSelectionListener)}. For callback
 * for changed number use {@link OnNumberChangeListener} that can be registered via
 * {@link #setOnNumberChangeListener(OnNumberChangeListener)} of which callback is fired whenever
 * user changes highlighted number by dragging a selection indicator.
 *
 * <h3>XML attributes</h3>
 * See {@link ViewWidget},
 * {@link R.styleable#Ui_NumberPicker_Circular CircularNumberPicker Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiNumberPickerCircularStyle uiNumberPickerCircularStyle}
 *
 * @author Martin Albedinsky
 */
public class CircularNumberPicker extends ViewWidget implements FontWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Listener that can receive callback about selected number value within {@link CircularNumberPicker}
	 * widget.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnNumberSelectionListener {

		/**
		 * Invoked whenever the specified <var>number</var> has been selected within the given
		 * <var>picker</var>.
		 * <p>
		 * <b>Note</b>, that by selected is meant that the selection indicator has been released after
		 * it was dragged or just pressed within the selectable area (arc with numbers) of the picker.
		 *
		 * @param picker The picker instance within which was the specified number selected.
		 * @param number The selected number. <b>Note</b>, that this doesn't need to be necessary the
		 *               number from the current set of numbers, set to the picker. The specified number
		 *               is calculated as the current selection for the current position of selection
		 *               indicator against the current selection range of the picker.
		 * @see #setSelectionRange(int)
		 */
		void onNumberSelected(@NonNull CircularNumberPicker picker, int number);
	}

	/**
	 * Listener that can receive callback about changed number value within {@link CircularNumberPicker}
	 * widget.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnNumberChangeListener {

		/**
		 * Invoked whenever the specified <var>number</var> has been changed within the given
		 * <var>picker</var>.
		 * <p>
		 * <b>Note</b>, that by changed is meant that the selection indicator has been pressed within
		 * the selectable area (arc with numbers) of the picker and dragged.
		 *
		 * @param picker The picker instance within which was the specified number changed.
		 * @param number The changed number. <b>Note</b>, that this doesn't need to be necessary the
		 *               number from the current set of numbers, set to the picker. The specified number
		 *               is calculated as the current selection for the current position of selection
		 *               indicator against the current selection range of the picker.
		 * @see #setSelectionRange(int)
		 */
		void onNumberChanged(@NonNull CircularNumberPicker picker, int number);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "CircularNumberPicker";

	/**
	 * Ratio for a user touch on the numbers arc. This ratio is used to compute tolerance for touch
	 * distance from the center of this picker. Tolerance distance is computed from the radius of
	 * area needed to draw graphics of one number.
	 */
	private static final float TOUCH_TOLERANCE_RATIO = 0.5f;

	/**
	 * Flag indicating whether a user is dragging selection indicator or not.
	 */
	private static final int PFLAG_DRAGGING = 0x00000001 << 16;

	/**
	 * Flag indicating whether this picker is currently being animated or not.
	 */
	private static final int PFLAG_ANIMATING = 0x00000001 << 17;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Graphics info for numbers text.
	 */
	private final TextInfo TEXT_INFO = new TextInfo();

	/**
	 * Graphics info for indicator's bearing line.
	 */
	private final BearingLineInfo BEARING_LINE_INFO = new BearingLineInfo();

	/**
	 * Graphics info for picker's middle circle.
	 */
	private final MiddleCircleInfo MIDDLE_CIRCLE_INFO = new MiddleCircleInfo();

	/**
	 * Helper used to apply custom font to this view.
	 */
	private final FontApplier FONT_APPLIER = new FontApplier(this);

	/**
	 * Set of private flags of this dialog view.
	 */
	private int mPrivateFlags;

	/**
	 * Radius in which should be drawn numbers of this picker.
	 */
	private float mRadius;

	/**
	 * Padding to be applied to the graphics of this picker. Higher padding will move the numbers
	 * closer to the picker's center.
	 */
	private float mPadding;

	/**
	 * Current center of number picker view.
	 */
	private float mCenter;

	/**
	 * Set of numbers which should be drawn in the arc of this picker view. These numbers are only
	 * for presentation purpose, to be drawn in arc. To determine the current selection of this picker,
	 * see {@link #mSelectionRange} for info.
	 */
	private int[] mNumbers;

	/**
	 * Count of the current {@link #mNumbers} set for better accessibility.
	 */
	private int mNumbersCount;

	/**
	 * Current selection of this picker.
	 */
	private int mSelection;

	/**
	 * The range in which can be numbers of this picker selected. This will be used to compute value
	 * of the current {@link #mSelection} of this picker.
	 */
	private int mSelectionRange;

	/**
	 * Callback to be invoked whenever a specific number is selected within this picker.
	 */
	private OnNumberSelectionListener mSelectionListener;

	/**
	 * Callback to be invoked whenever a specific number is changed within this picker.
	 */
	private OnNumberChangeListener mChangeListener;

	/**
	 * Drawable used to draw selection indicator for the currently selected number.
	 *
	 * @see #mSelection
	 */
	private Drawable mSelectionIndicator;

	/**
	 * Resource id of the {@link #mSelectionIndicator} if indicator has been specified via
	 * {@link #setSelectionIndicator(int)}.
	 */
	private int mSelectionIndicatorRes;

	/**
	 * Data used when tinting components of this view.
	 */
	private TintInfo mTintInfo;

	/**
	 * Current drawable state set. See {@link #drawableStateChanged()}.
	 */
	private int[] mDrawableStateSet;

	/**
	 * Current drawable state set with selected flag. See {@link #drawableStateChanged()}.
	 */
	private int[] mDrawableStateSetSelected;

	/**
	 * Helper rect used when we need to obtain bounds or padding of some component.
	 */
	private Rect mRect;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #CircularNumberPicker(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public CircularNumberPicker(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #CircularNumberPicker(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#uiNumberPickerCircularStyle} as attribute for default style.
	 */
	public CircularNumberPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiNumberPickerCircularStyle);
	}

	/**
	 * Same as {@link #CircularNumberPicker(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public CircularNumberPicker(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of CircularNumberPicker for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CircularNumberPicker(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Called from one of constructors of this view to perform its initialization.
	 * <p>
	 * Initialization is done via parsing of the specified <var>attrs</var> set and obtaining for
	 * this view specific data from it that can be used to configure this new view instance. The
	 * specified <var>defStyleAttr</var> and <var>defStyleRes</var> are used to obtain default data
	 * from the current theme provided by the specified <var>context</var>.
	 */
	@SuppressWarnings({"ConstantConditions", "ResourceType"})
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.ensureRect();
		final Resources.Theme theme = context.getTheme();
		if (!isInEditMode()) {
			// Get font path presented within text appearance style.
			if (theme != null) {
				final TypedArray appearanceArray = theme.obtainStyledAttributes(attrs, new int[]{android.R.attr.textAppearance}, defStyleAttr, defStyleRes);
				if (appearanceArray != null) {
					final int appearance = appearanceArray.getResourceId(0, -1);
					if (appearance != -1) {
						FONT_APPLIER.applyFont(appearance);
					}
					appearanceArray.recycle();
				}
			}
			// Get font path presented within xml attributes.
			FONT_APPLIER.applyFont(attrs, defStyleAttr);
		}

		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_NumberPicker_Circular, defStyleAttr, defStyleRes);
		this.ensureTintInfo();
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerNumbers) {
				final int resId = attributes.getResourceId(index, -1);
				if (resId != -1) {
					setNumbers(context.getResources().getIntArray(resId));
				}
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerNumberFormat) {
				setNumberFormat(attributes.getString(index));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerSelection) {
				setSelection(attributes.getInt(index, mSelection));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerSelectionRange) {
				setSelectionRange(attributes.getInt(index, mSelectionRange));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_android_radius) {
				setRadius(attributes.getDimensionPixelSize(index, 0));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_android_textAppearance) {
				setTextAppearance(attributes.getResourceId(index, 0));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_android_textColor) {
				setTextColor(attributes.getColorStateList(index));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_android_textSize) {
				setTextSize(attributes.getDimensionPixelSize(index, 0));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_android_textStyle) {
				setTypeface(TEXT_INFO.paint.getTypeface(), attributes.getInt(index, Typeface.NORMAL));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiSelectionIndicator) {
				setSelectionIndicator(attributes.getDrawable(index));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiSelectionIndicatorTint) {
				mTintInfo.tintList = attributes.getColorStateList(index);
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiSelectionIndicatorTintMode) {
				mTintInfo.tintMode = TintManager.parseTintMode(attributes.getInt(index, 0), PorterDuff.Mode.SRC_IN);
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerBearingLineColor) {
				setBearingLineColor(attributes.getColorStateList(index));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerBearingLineThickness) {
				setBearingLineThickness(attributes.getDimensionPixelSize(index, 0));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerMiddleCircleColor) {
				setMiddleCircleColor(attributes.getColor(index, 0));
			} else if (index == R.styleable.Ui_NumberPicker_Circular_uiPickerMiddleCircleRadius) {
				setMiddleCircleRadius(attributes.getDimensionPixelSize(index, 0));
			}
		}
		attributes.recycle();
		mTintInfo.hasTintList = mTintInfo.tintList != null;
		mTintInfo.hasTintMode = mTintInfo.tintMode != null;
		this.applySelectionIndicatorTint();
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(CircularNumberPicker.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(CircularNumberPicker.class.getName());
	}

	/**
	 * Ensures that the {@link #mRect} is initialized.
	 */
	private void ensureRect() {
		if (mRect == null) this.mRect = new Rect();
	}

	/**
	 * Ensures that the tint info object is initialized.
	 */
	private void ensureTintInfo() {
		if (mTintInfo == null) this.mTintInfo = new TintInfo();
	}

	/**
	 * Registers a callback to be invoked whenever a specific number is selected within this picker.
	 *
	 * @param listener The desired listener to register.
	 */
	public void setOnNumberSelectionListener(@Nullable OnNumberSelectionListener listener) {
		this.mSelectionListener = listener;
	}

	/**
	 * Registers a callback to be invoked whenever a specific number is changed within this picker.
	 *
	 * @param listener The desired listener to register.
	 */
	public void setOnNumberChangeListener(@Nullable OnNumberChangeListener listener) {
		this.mChangeListener = listener;
	}

	/**
	 * Sets a set of numbers that should be presented within this picker. The given numbers will
	 * be drawn in the circular area within this picker.
	 * <p>
	 * <b>Note</b>, that this will also set a value of the <b>selection range</b> to size of the
	 * specified <var>numbers</var>. If this picker requires different selection range, you should
	 * change it after this call via {@link #setSelectionRange(int)}.
	 *
	 * @param numbers The desired set of numbers.
	 * @see R.attr#uiPickerNumbers ui:uiPickerNumbers
	 * @see #getNumbers()
	 */
	public void setNumbers(@Nullable int[] numbers) {
		this.mNumbers = numbers;
		this.mNumbersCount = mNumbers != null ? mNumbers.length : 0;
		this.mSelectionRange = mNumbersCount;
		invalidate();
	}

	/**
	 * Returns the set of numbers presented within this picker. These are the numbers that are drawn
	 * in the circular area of this picker.
	 *
	 * @return Set of numbers.
	 * @see #setNumbers(int[])
	 */
	@Nullable
	public int[] getNumbers() {
		return mNumbers;
	}

	/**
	 * Returns the count of the numbers presented within this picker.
	 *
	 * @return Count of current numbers.
	 * @see #getNumbers()
	 */
	public int getNumbersCount() {
		return mNumbersCount;
	}

	/**
	 * Same as {@link #setNumberFormat(NumberFormat)} for string value.
	 *
	 * @param format The desired string format.
	 */
	public void setNumberFormat(@NonNull String format) {
		setNumberFormat(new DecimalFormat(format));
	}

	/**
	 * Sets a number format used to format the current number values during drawing of theirs text
	 * representations.
	 *
	 * @param format The desired format.
	 * @see R.attr#uiPickerNumberFormat ui:uiPickerNumberFormat
	 * @see #setNumberFormat(String)
	 * @see #getNumberFormat()
	 */
	public void setNumberFormat(@NonNull NumberFormat format) {
		this.TEXT_INFO.format = format;
		this.invalidateNumbersArea();
	}

	/**
	 * Returns the number format of this picker.
	 *
	 * @return Format used for numbers text formatting.
	 * @see #setNumberFormat(NumberFormat)
	 */
	@NonNull
	public NumberFormat getNumberFormat() {
		return TEXT_INFO.format;
	}

	/**
	 * Sets a selection of this picker. This should be number from the range {@code [0, {@link #getSelectionRange()}]}.
	 *
	 * @param selection The desired selection value to determine which number within this picker
	 *                  should be highlighted by the selection indicator.
	 * @see R.attr#uiPickerSelection ui:uiPickerSelection
	 * @see #getSelection()
	 */
	public void setSelection(int selection) {
		if (mSelection != selection) {
			this.mSelection = selection;
			this.invalidateNumbersArea();
			if (mSelectionListener != null) {
				mSelectionListener.onNumberSelected(this, mSelection);
			}
		}
	}

	/**
	 * Returns the current selection of this picker. This is the number value which is currently
	 * highlighted by the selection indicator.
	 *
	 * @return Current selection value.
	 * @see #setSelection(int)
	 */
	public int getSelection() {
		return mSelection;
	}

	/**
	 * Sets a range in which can be numbers within this picker selected. This range is used to compute
	 * the selection of this picker.
	 *
	 * @param range The desired range.
	 * @see R.attr#uiPickerSelectionRange ui:uiPickerSelectionRange
	 * @see #getSelectionRange()
	 */
	public void setSelectionRange(int range) {
		if (mSelectionRange != range) {
			this.mSelectionRange = range;
			invalidate();
		}
	}

	/**
	 * Returns the selection range of this picker.
	 *
	 * @return Selection range.
	 * @see #setSelectionRange(int)
	 */
	public int getSelectionRange() {
		return mSelectionRange;
	}

	/**
	 * Sets a radius determining size of the numbers circle.
	 *
	 * @param radius The desired radius.
	 * @see android.R.attr#radius android:radius
	 * @see #getRadius()
	 */
	public void setRadius(@FloatRange(from = 0) float radius) {
		if (mRadius != radius) {
			this.mRadius = Math.max(0, radius);
			requestLayout();
		}
	}

	/**
	 * Returns the radius of the numbers circle.
	 *
	 * @return Numbers circle radius.
	 * @see #setRadius(float)
	 */
	@FloatRange(from = 0)
	public float getRadius() {
		return mRadius;
	}

	/**
	 * Sets a text color, size, and style for the numbers text from the specified TextAppearance
	 * resource.
	 *
	 * @param resId Resource id of the desired TextAppearance style.
	 * @see android.R.attr#textAppearance android:textAppearance
	 */
	public void setTextAppearance(@StyleRes int resId) {
		if (TEXT_INFO.fromTextAppearanceStyle(getContext(), resId) && TEXT_INFO.updatePaint(getDrawableState())) {
			this.invalidateNumbersArea();
		}
	}

	/**
	 * Same as {@link #setTextSize(int, float)} in {@link TypedValue#COMPLEX_UNIT_SP} and the specified
	 * <var>size</var>.
	 *
	 * @see android.R.attr#textSize android:textSize
	 * @see #getTextSize()
	 */
	public void setTextSize(float size) {
		setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
	}

	/**
	 * Sets a size for the numbers text to the given <var>unit</var> and <var>size</var>.
	 *
	 * @param unit The desired dimension unit. See {@link TypedValue} for possible units.
	 * @param size The desired size in the specified unit.
	 */
	public void setTextSize(int unit, float size) {
		setRawTextSize(TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics()));
	}

	/**
	 * Sets the raw text size for the Paint used to draw numbers graphics.
	 *
	 * @param size The desired raw size in pixels.
	 */
	private void setRawTextSize(float size) {
		if (TEXT_INFO.updateTextSize(size)) this.invalidateNumbersArea();
	}

	/**
	 * Returns the size of the numbers text.
	 *
	 * @return Size used when drawing numbers text graphics.
	 * @see #setTextSize(int, float)
	 * @see #setTextAppearance(int)
	 */
	public float getTextSize() {
		return TEXT_INFO.paint.getTextSize();
	}

	/**
	 * Sets a single color for the numbers text.
	 *
	 * @param color The desired color.
	 * @see android.R.attr#textColor android:textColor
	 * @see #setTextColor(ColorStateList)
	 * @see #getTextColors()
	 */
	public void setTextColor(@ColorInt int color) {
		setTextColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for the numbers text.
	 *
	 * @param colors The desired colors state list.
	 * @see android.R.attr#textColor android:textColor
	 * @see #setTextColor(int)
	 * @see #getTextColors()
	 */
	public void setTextColor(@NonNull ColorStateList colors) {
		if (TEXT_INFO.updateTextColor(colors, getDrawableState())) this.invalidateNumbersArea();
	}

	/**
	 * Returns the colors for the numbers text.
	 *
	 * @return List of colors used when drawing numbers text graphics.
	 * @see #setTextColor(ColorStateList)
	 * @see #setTextColor(int)
	 * @see #getCurrentTextColor()
	 */
	@NonNull
	@SuppressWarnings("ConstantConditions")
	public ColorStateList getTextColors() {
		return TEXT_INFO.mAppearance.getTextColor();
	}

	/**
	 * Returns the current color used to draw the numbers text.
	 *
	 * @return Current numbers text color.
	 * @see #getTextColors()
	 * @see #setTextColor(ColorStateList)
	 */
	@ColorInt
	public int getCurrentTextColor() {
		return TEXT_INFO.paint.getColor();
	}

	/**
	 */
	@Override
	public void setFont(@NonNull String fontPath) {
		FONT_APPLIER.applyFont(fontPath);
	}

	/**
	 */
	@Override
	public void setFont(@Nullable Font font) {
		FONT_APPLIER.applyFont(font);
	}

	/**
	 * Sets the typeface and style in which the numbers text should be displayed, and turns on the
	 * fake bold and italic bits in the Paint if the Typeface that you provided does not have all the
	 * bits in the style that you specified.
	 *
	 * @param typeface The desired typeface. May be {@code null} to create default one from the specified
	 *                 <var>style</var>.
	 * @param style    One of {@link Typeface#NORMAL}, {@link Typeface#BOLD}, {@link Typeface#ITALIC}
	 *                 or {@link Typeface#BOLD_ITALIC}.
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface, @TextAppearance.TextStyle int style) {
		if (TEXT_INFO.updateTypeface(typeface, style)) this.invalidateNumbersArea();
	}

	/**
	 * Sets the typeface and style in which the numbers text should be displayed.
	 *
	 * @param typeface The desired typeface. May be {@code null} to use a default one.
	 */
	@Override
	public void setTypeface(@Nullable Typeface typeface) {
		if (TEXT_INFO.updateTypeface(typeface)) this.invalidateNumbersArea();
	}

	/**
	 * Returns the typeface used to draw text of the numbers.
	 *
	 * @return Numbers text typeface.
	 * @see #setTypeface(Typeface, int)
	 * @see #setTypeface(Typeface)
	 * @see #getTypefaceStyle()
	 * @see #setTextAppearance(int)
	 */
	@Nullable
	public Typeface getTypeface() {
		return TEXT_INFO.paint.getTypeface();
	}

	/**
	 * Returns the style of the typeface used to draw the numbers text.
	 *
	 * @return Typeface style.
	 * @see #getTypeface()
	 * @see #setTypeface(Typeface, int)
	 */
	@TextAppearance.TextStyle
	@SuppressWarnings("ResourceType")
	public int getTypefaceStyle() {
		final Typeface typeface = TEXT_INFO.paint.getTypeface();
		return typeface != null ? typeface.getStyle() : Typeface.NORMAL;
	}

	/**
	 * Same as {@link #setSelectionIndicator(android.graphics.drawable.Drawable)} for resource id.
	 *
	 * @param resId Resource id of the desired drawable for selection indicator. May be {@code 0} to
	 *              remove the current indicator.
	 */
	public void setSelectionIndicator(@DrawableRes int resId) {
		if (resId == 0) {
			setSelectionIndicator(null);
		} else if (mSelectionIndicatorRes != resId) {
			setSelectionIndicator(ResourceUtils.getDrawable(
					getResources(),
					mSelectionIndicatorRes = resId,
					getContext().getTheme())
			);
		}
	}

	/**
	 * Sets the drawable used to draw the number selection indicator.
	 *
	 * @param indicator The desired drawable for selection indicator. May be {@code null} to clear
	 *                  the current one.
	 * @see R.attr#uiSelectionIndicator ui:uiSelectionIndicator
	 * @see #setSelectionIndicatorTintList(ColorStateList)
	 * @see #setSelectionIndicatorTintMode(PorterDuff.Mode)
	 * @see #getSelectionIndicator()
	 */
	public void setSelectionIndicator(@Nullable Drawable indicator) {
		if (mSelectionIndicator != indicator) {
			final boolean needUpdate;
			if (mSelectionIndicator != null) {
				mSelectionIndicator.setCallback(null);
				unscheduleDrawable(mSelectionIndicator);
				needUpdate = true;
			} else {
				needUpdate = false;
			}

			if (indicator != null) {
				indicator.setCallback(this);
				indicator.setVisible(getVisibility() == VISIBLE, false);
				if (indicator.getPadding(mRect)) {
					TEXT_INFO.padding = Math.max(mRect.left, Math.max(mRect.top, Math.max(mRect.right, mRect.bottom)));
				}
			} else {
				this.mSelectionIndicatorRes = 0;
			}
			this.mSelectionIndicator = indicator;
			this.applySelectionIndicatorTint();
			if (needUpdate) {
				if (mSelectionIndicator.isStateful()) {
					mSelectionIndicator.setState(getDrawableState());
				}
				this.invalidateNumbersArea();
			}
		}
	}

	/**
	 * Returns the current selection indicator's drawable.
	 * <p>
	 * <b>Note, that on pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions
	 * this method will return an instance of {@link TintDrawable TintDrawable} if tint has been applied
	 * via {@link #setSelectionIndicatorTintList(ColorStateList)}.</b>
	 * <p>
	 * The original wrapped indicator drawable can be obtained via {@link TintDrawable#getDrawable()}.
	 *
	 * @return Selection indicator's drawable.
	 * @see #setSelectionIndicator(Drawable)
	 */
	@Nullable
	public Drawable getSelectionIndicator() {
		return mSelectionIndicator;
	}

	/**
	 * Applies a tint to the selection indicator. This call does not modify the current tint mode,
	 * which is {@link PorterDuff.Mode#SRC_IN} by default.
	 *
	 * @param tint The tint to apply, may be {@code null} to clear the current tint.
	 * @see R.attr#uiSelectionIndicatorTint ui:uiSelectionIndicatorTint
	 * @see #getSelectionIndicatorTintList()
	 */
	public void setSelectionIndicatorTintList(@Nullable ColorStateList tint) {
		this.ensureTintInfo();
		mTintInfo.tintList = tint;
		mTintInfo.hasTintList = true;
		this.applySelectionIndicatorTint();
	}

	/**
	 * Returns the tint applied to the selection indicator's graphics.
	 *
	 * @return The discrete indicator's drawable tint.
	 * @see #setSelectionIndicatorTintList(ColorStateList)
	 */
	@Nullable
	public ColorStateList getSelectionIndicatorTintList() {
		return mTintInfo != null ? mTintInfo.tintList : null;
	}

	/**
	 * Specifies the blending mode used to apply the tint specified by {@link #setSelectionIndicatorTintList(ColorStateList)}}
	 * to the selection indicator. The default mode is {@link PorterDuff.Mode#SRC_IN}.
	 *
	 * @param tintMode The blending mode used to apply the tint, may be {@code null} to clear the
	 *                 current tint.
	 * @see R.attr#uiSelectionIndicatorTintMode ui:uiSelectionIndicatorTintMode
	 * @see #getSelectionIndicatorTintMode()
	 */
	public void setSelectionIndicatorTintMode(@Nullable PorterDuff.Mode tintMode) {
		this.ensureTintInfo();
		mTintInfo.tintMode = tintMode;
		mTintInfo.hasTintMode = true;
		this.applySelectionIndicatorTint();
	}

	/**
	 * Returns the blending mode used to apply the tint to the selection indicator's graphics.
	 *
	 * @return The selection indicator's graphics blending mode used to apply the tint.
	 * @see #setSelectionIndicatorTintMode(PorterDuff.Mode)
	 */
	@Nullable
	public PorterDuff.Mode getSelectionIndicatorTintMode() {
		return mTintInfo != null ? mTintInfo.tintMode : null;
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the current selection indicator.
	 */
	@SuppressWarnings("NewApi")
	private void applySelectionIndicatorTint() {
		this.ensureTintInfo();
		if (mSelectionIndicator == null || (!mTintInfo.hasTintList && !mTintInfo.hasTintMode)) {
			return;
		}
		if (UiConfig.MATERIALIZED) {
			this.mSelectionIndicator = mSelectionIndicator.mutate();
			if (mTintInfo.hasTintList) {
				mSelectionIndicator.setTintList(mTintInfo.tintList);
			}
			if (mTintInfo.hasTintMode) {
				mSelectionIndicator.setTintMode(mTintInfo.tintMode);
			}

			if (mSelectionIndicator.isStateful()) {
				mSelectionIndicator.setState(getDrawableState());
			}
			return;
		}
		final boolean isTintDrawable = mSelectionIndicator instanceof TintDrawable;
		final TintDrawable tintDrawable = isTintDrawable ? (TintDrawable) mSelectionIndicator : new TintDrawable(mSelectionIndicator);
		if (mTintInfo.hasTintList) {
			tintDrawable.setTintList(mTintInfo.tintList);
		}
		if (mTintInfo.hasTintMode) {
			tintDrawable.setTintMode(mTintInfo.tintMode);
		}
		if (isTintDrawable) {
			return;
		}
		mSelectionIndicator.setCallback(this);
		if (mSelectionIndicator.isStateful()) {
			mSelectionIndicator.setState(getDrawableState());
		}
	}

	/**
	 * Sets a single color for the bearing line pointing to the selection indicator position.
	 *
	 * @param color The desired color.
	 * @see R.attr#uiPickerBearingLineColor ui:uiPickerBearingLineColor
	 * @see #setBearingLineColor(ColorStateList)
	 * @see #getBearingLineColors()
	 */
	public void setBearingLineColor(@ColorInt int color) {
		setBearingLineColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for the bearing line pointing to the selection indicator position.
	 *
	 * @param colors The desired colors state list.
	 * @see R.attr#uiPickerBearingLineColor ui:uiPickerBearingLineColor
	 * @see #setBearingLineColor(int)
	 * @see #getBearingLineColors()
	 */
	public void setBearingLineColor(@NonNull ColorStateList colors) {
		if (BEARING_LINE_INFO.updateColors(colors, getDrawableState())) invalidate();
	}

	/**
	 * Returns the colors for the bearing line.
	 *
	 * @return List of colors used when drawing the selection indicator's bearing line.
	 * @see #setBearingLineColor(ColorStateList)
	 * @see #setBearingLineColor(int)
	 * @see #getBearingLineCurrentColor()
	 */
	@NonNull
	public ColorStateList getBearingLineColors() {
		return BEARING_LINE_INFO.colors;
	}

	/**
	 * Returns the current color used to draw the selection indicator's bearing line.
	 *
	 * @return Current bearing line color.
	 * @see #getBearingLineColors()
	 * @see #setBearingLineColor(ColorStateList)
	 */
	@ColorInt
	public int getBearingLineCurrentColor() {
		return BEARING_LINE_INFO.paint.getColor();
	}

	/**
	 * Sets a thickness for bearing line of the selection indicator drawn from the center of this
	 * picker to the edge of the circle with numbers at position of the currently selected number.
	 *
	 * @param thickness The desired line thickness. Should be positive value.
	 * @see R.attr#uiPickerBearingLineThickness ui:uiPickerBearingLineThickness
	 * @see #getBearingLineThickness()
	 */
	public void setBearingLineThickness(@FloatRange(from = 0) float thickness) {
		if (BEARING_LINE_INFO.thickness != thickness) {
			BEARING_LINE_INFO.thickness = Math.max(0, thickness);
			invalidate();
		}
	}

	/**
	 * Returns the thickness of the selection indicator's bearing line.
	 *
	 * @return Line thickness.
	 * @see #setBearingLineThickness(float)
	 */
	@FloatRange(from = 0)
	public float getBearingLineThickness() {
		return BEARING_LINE_INFO.thickness;
	}


	/**
	 * Sets a single color for the circle drawn in the middle of this picker as origin point for the
	 * selection indicator's bearing line.
	 *
	 * @param color The desired circle color.
	 * @see R.attr#uiPickerMiddleCircleColor ui:uiPickerMiddleCircleColor
	 * @see #getMiddleCircleColors()
	 * @see #getMiddleCircleCurrentColor()
	 */
	public void setMiddleCircleColor(@ColorInt int color) {
		setMiddleCircleColor(ColorStateList.valueOf(color));
	}

	/**
	 * Sets colors for the circle drawn in the middle of this picker as origin point for the selection
	 * indicator's bearing line.
	 *
	 * @param colors The desired colors state list.
	 * @see R.attr#uiPickerMiddleCircleColor ui:uiPickerMiddleCircleColor
	 * @see #getMiddleCircleColors()
	 * @see #getMiddleCircleCurrentColor() ()
	 */
	public void setMiddleCircleColor(@NonNull ColorStateList colors) {
		if (MIDDLE_CIRCLE_INFO.updateColors(colors, getDrawableState()))
			this.invalidateMiddleCircleArea();
	}

	/**
	 * Returns the middle circle colors.
	 *
	 * @return Colors used to draw the middle circle of this picker.
	 * @see #setMiddleCircleColor(ColorStateList)
	 * @see #setMiddleCircleColor(int)
	 */
	@NonNull
	public ColorStateList getMiddleCircleColors() {
		return MIDDLE_CIRCLE_INFO.colors;
	}

	/**
	 * Returns the current color used to draw the middle circle of this picker.
	 *
	 * @return Current middle circle color.
	 * @see #setMiddleCircleColor(ColorStateList)
	 * @see #setMiddleCircleColor(int)
	 */
	public int getMiddleCircleCurrentColor() {
		return MIDDLE_CIRCLE_INFO.paint.getColor();
	}

	/**
	 * Sets a radius for the circle drawn in the middle of this picker as origin point for the selection
	 * indicator's bearing line.
	 *
	 * @param radius The desired circle radius. Should be positive value.
	 * @see R.attr#uiPickerMiddleCircleRadius ui:uiPickerMiddleCircleRadius
	 * @see #getMiddleCircleRadius()
	 */
	public void setMiddleCircleRadius(@FloatRange(from = 0) float radius) {
		if (MIDDLE_CIRCLE_INFO.radius != radius) {
			MIDDLE_CIRCLE_INFO.radius = Math.max(0, radius);
			invalidate();
		}
	}

	/**
	 * Returns the radius of the middle circle.
	 *
	 * @return Middle circle's radius.
	 * @see #setMiddleCircleRadius(float)
	 */
	@FloatRange(from = 0)
	public float getMiddleCircleRadius() {
		return MIDDLE_CIRCLE_INFO.radius;
	}

	/**
	 */
	@Override
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(background);
		this.resolvePadding();
	}

	/**
	 */
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		super.setPadding(left, top, right, bottom);
		this.resolvePadding();
	}

	/**
	 */
	@Override
	public void setPaddingRelative(int start, int top, int end, int bottom) {
		super.setPaddingRelative(start, top, end, bottom);
		this.resolvePadding();
	}

	/**
	 * Updates value of {@link #mPadding} according to the current specified padding values where
	 * also padding of the background's drawable is taken into count.
	 */
	private void resolvePadding() {
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();
		final Drawable background = getBackground();
		this.ensureRect();
		if (background != null && background.getPadding(mRect)) {
			paddingLeft = Math.max(paddingLeft, mRect.left);
			paddingTop = Math.max(paddingTop, mRect.top);
			paddingRight = Math.max(paddingRight, mRect.right);
			paddingBottom = Math.max(paddingBottom, mRect.bottom);
		}
		this.mPadding = Math.max(paddingLeft, Math.max(paddingTop, Math.max(paddingRight, paddingBottom)));
	}

	/**
	 */
	@Override
	@SuppressWarnings("SuspiciousNameCombination")
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		final int diameter = Math.round(mRadius * 2);
		int width = diameter;
		int height = diameter;

		switch (widthMode) {
			case MeasureSpec.AT_MOST:
				width = Math.min(width, widthSize);
				break;
			case MeasureSpec.EXACTLY:
				width = widthSize;
				break;
			case MeasureSpec.UNSPECIFIED:
			default:
				break;
		}

		switch (heightMode) {
			case MeasureSpec.AT_MOST:
				height = Math.min(height, heightSize);
				break;
			case MeasureSpec.EXACTLY:
				height = heightSize;
				break;
			case MeasureSpec.UNSPECIFIED:
			default:
				break;
		}

		// Check also against minimum size.
		width = Math.max(width, getSuggestedMinimumWidth());
		height = Math.max(height, getSuggestedMinimumHeight());

		// Ensure that the width and height are equals.
		if (width != height) {
			if (width > height) {
				width = height;
			} else {
				height = width;
			}
		}
		this.mRadius = width / 2f;
		setMeasuredDimension(width, height);
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.mCenter = w / 2f;
	}

	/**
	 */
	@Override
	protected void onAnimationStart() {
		super.onAnimationStart();
		this.updatePrivateFlags(PFLAG_ANIMATING, true);
	}

	/**
	 */
	@Override
	protected void onAnimationEnd() {
		super.onAnimationEnd();
		this.updatePrivateFlags(PFLAG_ANIMATING, false);
	}

	/**
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		// Do not process touches if this view is disabled or some animation is running upon it.
		if (!isEnabled() || hasPrivateFlag(PFLAG_ANIMATING)) {
			return super.onTouchEvent(event);
		}

		final float touchX = event.getX();
		final float touchY = event.getY();

		boolean invalidate = false;
		boolean processed = super.onTouchEvent(event);
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				// Check if the touch is within the numbers area.
				if (isTouchWithinNumbersArc(touchX, touchY)) {
					this.updatePrivateFlags(PFLAG_DRAGGING, true);
					setPressed(true);
					this.changeSelectionTo(calculateSelection(touchX, touchY));
					processed = true;
					invalidate = true;
				} else {
					this.updatePrivateFlags(PFLAG_DRAGGING, false);
					processed = false;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (hasPrivateFlag(PFLAG_DRAGGING) && isTouchWithinNumbersArc(touchX, touchY)) {
					this.changeSelectionTo(calculateSelection(touchX, touchY));
					invalidate = true;
				} else {
					this.updatePrivateFlags(PFLAG_DRAGGING, false);
					processed = true;
					setPressed(false);
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				setPressed(false);
				if (hasPrivateFlag(PFLAG_DRAGGING)) {
					if (mSelectionListener != null) {
						mSelectionListener.onNumberSelected(this, mSelection);
					}
					processed = true;
				}
				this.updatePrivateFlags(PFLAG_DRAGGING, false);
				invalidate = true;
				break;
		}
		if (invalidate) {
			invalidate();
		}
		return processed;
	}

	/**
	 * Checks whether a touch with the specified coordinates is within numbers arc of this picker
	 * or not.
	 *
	 * @param touchX The x coordinate of the touch.
	 * @param touchY The y coordinate of the touch.
	 * @return {@code True} if touch with the given coordinates is within the numbers arc,
	 * {@code false} otherwise.
	 */
	private boolean isTouchWithinNumbersArc(float touchX, float touchY) {
		final double dist = Math.sqrt(Math.pow(mCenter - touchX, 2) + Math.pow(mCenter - touchY, 2));
		final float outerRadius = mCenter - mPadding;
		final float numberAreaRadius = TEXT_INFO.padding + TEXT_INFO.paint.getTextSize();
		final float touchDistTolerance = numberAreaRadius * TOUCH_TOLERANCE_RATIO;
		return dist <= (outerRadius + touchDistTolerance) && dist >= (outerRadius - touchDistTolerance - numberAreaRadius);
	}

	/**
	 * Calculates a new selection value depending on the specified <var>touchX</var> and <var>touchY</var>
	 * coordinates.
	 *
	 * @param touchX The x touch coordinate.
	 * @param touchY The y touch coordinate.
	 * @return Selection number for the specified touch coordinates.
	 */
	private int calculateSelection(float touchX, float touchY) {
		final int selection = Math.round((float) (angleAgainstCenter(touchX, touchY) / 360) * mSelectionRange);
		return selection < mSelectionRange ? selection : 0;
	}

	/**
	 * Computes the angle against the current center of this picker for the specified point coordinates.
	 *
	 * @param pointX The x coordinate of point for which to compute angle.
	 * @param pointY The y coordinate of point for which to compute angle.
	 * @return Computed angle in degrees from the range {@code [0, 360]}.
	 */
	private double angleAgainstCenter(float pointX, float pointY) {
		double rads = Math.atan2(mCenter - pointX, mCenter - pointY);
		if (rads < 0) rads = Math.abs(rads);
		else rads = Math.PI * 2 - rads;
		return Math.toDegrees(rads);
	}

	/**
	 * Changes the current value of selection to the specified one and notifies {@link OnNumberChangeListener}
	 * if it is set.
	 *
	 * @param selection The new selection to be changed.
	 * @return {@code True} if selection has changed, {@code false} otherwise.
	 */
	private boolean changeSelectionTo(int selection) {
		if (mSelection != selection) {
			this.mSelection = selection;
			if (mChangeListener != null) {
				mChangeListener.onNumberChanged(this, mSelection);
			}
			return true;
		}
		return false;
	}

	/**
	 */
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		this.mDrawableStateSet = getDrawableState();
		this.mDrawableStateSetSelected = WidgetStateSet.expandStates(mDrawableStateSet, mDrawableStateSet.length + 1);
		this.mDrawableStateSetSelected[mDrawableStateSetSelected.length - 1] = android.R.attr.state_selected;
		if (mSelectionIndicator != null && mSelectionIndicator.isStateful()) {
			mSelectionIndicator.setState(mDrawableStateSet);
		} else {
			this.invalidateNumbersArea();
		}
	}

	/**
	 * Invalidates this view in area where the numbers are drawn.
	 *
	 * @see #invalidate(Rect)
	 */
	private void invalidateNumbersArea() {
		invalidate(
				(int) mPadding,
				(int) mPadding,
				(int) (mRadius * 2 - mPadding),
				(int) (mRadius * 2 - mPadding)
		);
	}

	/**
	 * Invalidates this view in area where the middle circle is drawn.
	 *
	 * @see #MIDDLE_CIRCLE_INFO
	 * @see #invalidate(int, int, int, int)
	 */
	private void invalidateMiddleCircleArea() {
		final float circleRadius = (int) MIDDLE_CIRCLE_INFO.radius;
		invalidate(
				(int) (mCenter - circleRadius),
				(int) (mCenter - circleRadius),
				(int) (mCenter + circleRadius),
				(int) (mCenter + circleRadius)
		);
	}

	/**
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mNumbersCount == 0) {
			return;
		}
		// Draw small circle in the middle as origin point for selection indicator.
		this.drawMiddleCircle(canvas);
		// Draw selection indicator behind numbers.
		this.drawSelectionIndicator(canvas);
		// Draw set of numbers.
		for (int i = 0; i < mNumbersCount; i++) {
			this.drawNumber(canvas, i, mNumbers[i]);
		}
	}

	/**
	 * Draws a simple circle in the middle of this picker.
	 *
	 * @param canvas Canvas on which to draw the middle circle.
	 */
	private void drawMiddleCircle(Canvas canvas) {
		MIDDLE_CIRCLE_INFO.updatePaintColor(mDrawableStateSet);
		canvas.drawCircle(mCenter, mCenter, MIDDLE_CIRCLE_INFO.radius, MIDDLE_CIRCLE_INFO.paint);
	}

	/**
	 * Draws the selection indicator for the current value of {@link #mSelection}. The indicator consists
	 * of the simple thin line drawn from the center of this picker to the start of the numbers circle
	 * where will be drawn an indicator to indicate the currently selected number.
	 * <p>
	 * The position (angle) where to draw selector will be calculated from the current {@link #mSelection}
	 * value against the value of allowed {@link #mSelectionRange}.
	 *
	 * @param canvas Canvas on which to draw the indicator.
	 */
	private void drawSelectionIndicator(Canvas canvas) {
		if (mSelectionIndicator == null) {
			return;
		}
		// Compute radius at which to draw the indicator.
		TEXT_INFO.paint.getTextBounds("0", 0, 1, mRect);
		final float textSize = mRect.height();
		final float radius = mRadius - mPadding - TEXT_INFO.padding - textSize / 2f;
		// Compute indicator's center x + y coordinates.
		final double xCos = Math.cos(mSelection * Math.PI / (mSelectionRange / 2) - Math.PI / 2);
		final double ySin = Math.sin(mSelection * Math.PI / (mSelectionRange / 2) - Math.PI / 2);
		final float centerX = Math.round(xCos * radius + mCenter);
		final float centerY = Math.round(ySin * radius + mCenter);
		// Draw the indicator's bearing line.
		BEARING_LINE_INFO.updatePaint(mDrawableStateSet);
		canvas.drawLine(
				mCenter,
				mCenter,
				centerX,
				centerY,
				BEARING_LINE_INFO.paint
		);
		// Draw the indicator at the position of the current selection.
		final float indicatorRadius = textSize / 2f + TEXT_INFO.padding;
		mSelectionIndicator.setBounds(
				(int) (centerX - indicatorRadius),
				(int) (centerY - indicatorRadius),
				(int) (centerX + indicatorRadius),
				(int) (centerY + indicatorRadius)
		);
		mSelectionIndicator.draw(canvas);
	}

	/**
	 * Draws the text for the specified <var>number</var> at the specified index on the numbers arc
	 * for the current {@link #mRadius}.
	 *
	 * @param canvas Canvas on which to draw the number.
	 * @param index  The index of the specified number within the set of numbers. This index is used
	 *               to compute position (angle) where to draw number's text on arc.
	 * @param number The number of which text to draw.
	 */
	private void drawNumber(Canvas canvas, int index, int number) {
		if ((mSelection / (float) (mSelectionRange / mNumbersCount)) == index) {
			TEXT_INFO.updatePaintColor(mDrawableStateSetSelected);
		} else {
			TEXT_INFO.updatePaintColor(mDrawableStateSet);
		}
		final Paint textPaint = TEXT_INFO.paint;
		TEXT_INFO.paint.getTextBounds("0", 0, 1, mRect);
		final float textSize = mRect.height();
		final float radius = mRadius - mPadding - TEXT_INFO.padding - textSize / 2f;
		final float x = Math.round(Math.cos(index * Math.PI / (mNumbersCount / 2) - Math.PI / 2) * radius + mCenter);
		final float y = Math.round(Math.sin(index * Math.PI / (mNumbersCount / 2) - Math.PI / 2) * radius + mCenter);
		canvas.drawText(TEXT_INFO.format.format(number), x, y + textSize / 2, textPaint);
	}

	/**
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.selection = mSelection;
		savedState.selectionRange = mSelectionRange;
		if (mNumbers != null) {
			savedState.numbers = mNumbers;
		}
		savedState.numberFormat = TEXT_INFO.format;
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
		TEXT_INFO.format = savedState.numberFormat;
		this.mNumbers = savedState.numbers;
		this.mNumbersCount = mNumbers != null ? mNumbers.length : 0;
		this.mSelection = savedState.selection;
		this.mSelectionRange = savedState.selectionRange;
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
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link CircularNumberPicker}
	 * is properly saved.
	 *
	 * @author Martin Albedinsky
	 */
	public static class SavedState extends WidgetSavedState {

		/**
		 * Creator used to create an instance or array of instances of SavedState from {@link Parcel}.
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
		int selection, selectionRange;

		/**
		 */
		int[] numbers = {};

		/**
		 */
		NumberFormat numberFormat;

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
			this.selection = source.readInt();
			this.selectionRange = source.readInt();
			source.readIntArray(numbers = new int[source.readInt()]);
			this.numberFormat = (NumberFormat) source.readSerializable();
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(selection);
			dest.writeInt(selectionRange);
			dest.writeInt(numbers.length);
			dest.writeIntArray(numbers);
			dest.writeSerializable(numberFormat);
		}
	}

	/**
	 * Graphics info that holds all parameters necessary to draw numbers text.
	 */
	private static final class TextInfo extends TextGraphicsInfo {

		/**
		 * Format used to format value of the number text to be drawn.
		 */
		NumberFormat format = new DecimalFormat("0");

		/**
		 * Padding to be applied to the text in selection indicator's area.
		 */
		int padding;

		/**
		 * Creates a new instance of TextInfo with Paint with text aligned to CENTER.
		 */
		TextInfo() {
			super();
			paint.setTextAlign(Paint.Align.CENTER);
		}
	}

	/**
	 * Graphics info that holds all parameters necessary to draw picker's middle circle.
	 */
	private static final class MiddleCircleInfo extends ColorGraphicsInfo {

		/**
		 * Radius of the circle.
		 */
		float radius;

		/**
		 * Creates a new instance of MiddleCircleInfo.
		 */
		MiddleCircleInfo() {
			super();
			paint.setStyle(Paint.Style.FILL);
		}
	}

	/**
	 * Graphics info that holds all parameters necessary to draw indicator's bearing line.
	 */
	private static final class BearingLineInfo extends ColorGraphicsInfo {

		/**
		 * Thickness of the bearing line.
		 */
		float thickness;

		/**
		 * Creates a new instance of BearingLineInfo.
		 */
		BearingLineInfo() {
			super();
			paint.setStyle(Paint.Style.STROKE);
		}

		/**
		 */
		@Override
		public boolean updatePaint(@Nullable int[] stateSet) {
			boolean updated = super.updatePaint(stateSet);
			if (thickness != paint.getStrokeWidth()) {
				paint.setStrokeWidth(thickness);
				updated = true;
			}
			return updated;
		}
	}
}
