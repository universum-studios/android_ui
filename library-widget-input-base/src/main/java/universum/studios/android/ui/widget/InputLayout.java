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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.ui.R;

/**
 * A {@link FrameLayoutWidget} implementation that represents a container for a view component of
 * which content/input can be dynamically changed by a user and few secondary view components that
 * show current state of that input component. The input view is the primary part of each InputLayout.
 * It can be for example {@link android.widget.EditText EditText} into which can user type characters
 * from a soft keyboard while the secondary views would show how many characters did user typed and
 * which characters are allowed and which are not.
 *
 * <h3>View hierarchy</h3>
 * View hierarchy of InputLayout is predefined by its current implementation, but can by changed by
 * inheritance instances of the InputLayout class. Default implementation will layout a label view
 * at the beginning of the layout. Below the label view will be laid out an input view. InputLayout
 * can also contain two more views that will be laid out below the input view. A note view for note
 * text or error text, laid out from the start, and/or constraint view for constraint text, laid out
 * from the end of layout.
 * <p>
 * <b>Note, that the input view is essential for this layout and if it is not presented before
 * measurement, InputLayout will throw {@link IllegalStateException}.</b>
 * <p>
 * <b>Simple view hierarchy model:</b>
 * <pre>
 * Label
 * ---------------------------
 * Input .....................
 * ---------------------------
 * Note             Constraint
 * </pre>
 * Whether to show or not some of views mentioned above, can be done by requesting the desired input
 * features upon an instance of InputLayout via {@link #requestInputFeatures(int)}.
 *
 * <h3>XML layout</h3>
 * Implementations or directly InputLayout can be of course inflated from an Xml layout as any view.
 * InputLayout identifies its child views based on the type flag specified for {@link R.attr#uiInputChildType uiInputChildType}
 * attribute. The accepted types are listed below:
 * <ul>
 * <li><b>input</b> - input view</li>
 * <li><b>label</b> - label view</li>
 * <li><b>note</b> - note view</li>
 * <li><b>constraint</b> - input view</li>
 * </ul>
 * InputLayout based on these types handles correct ordering to ensure proper measuring and layout
 * process of its view hierarchy.
 *
 * <h3>Input features</h3>
 * <ul>
 * <li>{@link #FEATURE_LABEL} - specifies whether to show label view or not</li>
 * <li>{@link #FEATURE_NOTE} - specifies whether to show note view or not</li>
 * <li>{@link #FEATURE_CONSTRAINT} - specifies whether to show constraint view or not</li>
 * </ul>
 * All features can be requested via {@link #requestInputFeatures(int)} with feature flags as follows:
 * <b>requestInputFeatures({@link #FEATURE_LABEL} | {@link #FEATURE_NOTE} | {@link #FEATURE_CONSTRAINT})</b>
 *
 * <h3>XML attributes</h3>
 * See {@link FrameLayoutWidget},
 * {@link R.styleable#Ui_InputLayout InputLayout Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@code 0}
 *
 * @author Martin Albedinsky
 * @see EditLayout
 * @see SpinnerLayout
 */
public class InputLayout extends FrameLayoutWidget implements ErrorWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "InputLayout";

	/**
	 * Feature flag to clear all before requested features or just to not request any features at all.
	 */
	public static final int FEATURE_NONE = 0x00000000;

	/**
	 * Feature flag to request a view which can present a label text above the input view.
	 */
	public static final int FEATURE_LABEL = 0x00000001;

	/**
	 * Feature flag to request a view which can present a note text below the input view.
	 */
	public static final int FEATURE_NOTE = 0x00000001 << 1;

	/**
	 * Feature flag to request a view which can present a constraint text below the input view.
	 */
	public static final int FEATURE_CONSTRAINT = 0x00000001 << 2;

	/**
	 * Defines an annotation for determining set of allowed input feature flags.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef(flag = true, value = {FEATURE_NONE, FEATURE_LABEL, FEATURE_NOTE, FEATURE_CONSTRAINT})
	public @interface InputFeature {
	}

	/**
	 * Flag indicating whether this input layout has some error set via {@link #setError(CharSequence)}
	 * or not.
	 */
	static final int PFLAG_HAS_ERROR = 0x00000001 << 16;

	/**
	 * Flag indicating whether this input layout has error highlight visible or not.
	 */
	static final int PFLAG_ERROR_HIGHLIGHT_VISIBLE = 0x00000001 << 17;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Context in which is this view presented.
	 */
	Context mContext;

	/**
	 * Application resources.
	 */
	Resources mResources;

	/**
	 * Input view of this input layout.
	 */
	View mInputView;

	/**
	 * Text view to present the label text above the input view.
	 */
	TextView mLabelView;

	/**
	 * Text view to present the note text below the input view.
	 */
	TextView mNoteView;

	/**
	 * Text view to present the constraint text below the input view.
	 */
	TextView mConstraintView;

	/**
	 * Data used when tinting components of this view.
	 */
	TintInfo mTintInfo;

	/**
	 * Label text.
	 */
	private CharSequence mLabel = "";

	/**
	 * Note text.
	 */
	private CharSequence mNote = "";

	/**
	 * Error text.
	 */
	private CharSequence mError = "";

	/**
	 * Constraint text.
	 */
	private CharSequence mConstraint = "";

	/**
	 * Set of private flags of this dialog view.
	 */
	int mPrivateFlags = PrivateFlags.PFLAG_ALLOWS_DEFAULT_SELECTION;

	/**
	 * Set of currently handled features by this input layout.
	 */
	int mFeatures = FEATURE_LABEL | FEATURE_NOTE;

	/**
	 * All colors used within this view.
	 */
	private ColorsInfo mColorsInfo = new ColorsInfo();

	/**
	 * Listener used for change note text and colors animation.
	 */
	private NoteTextChangeListener mNoteTextChangeListener;

	/**
	 * Duration for change note text and colors animation.
	 */
	long mNoteTextChangeDuration = 100;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #InputLayout(android.content.Context, android.util.AttributeSet)} without attributes.
	 */
	public InputLayout(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #InputLayout(android.content.Context, android.util.AttributeSet, int)} with
	 * {@code 0} as attribute for default style.
	 */
	public InputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Same as {@link #InputLayout(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public InputLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of InputLayout for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public InputLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.mContext = context;
		this.mResources = context.getResources();
		if (ANIMABLE) {
			this.mNoteTextChangeListener = new NoteTextChangeListener();
		}

		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_InputLayout, defStyleAttr, defStyleRes);
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_InputLayout_android_enabled) {
				setEnabled(attributes.getBoolean(index, true));
			} else if (index == R.styleable.Ui_InputLayout_android_minWidth) {
				setMinimumWidth(attributes.getDimensionPixelSize(index, 0));
			} else if (index == R.styleable.Ui_InputLayout_android_minHeight) {
				setMinimumHeight(attributes.getDimensionPixelSize(index, 0));
			} else if (index == R.styleable.Ui_InputLayout_android_label) {
				setLabel(attributes.getText(index));
			} else if (index == R.styleable.Ui_InputLayout_uiNote) {
				setNote(attributes.getText(index));
			} else if (index == R.styleable.Ui_InputLayout_uiNoteTextChangeDuration) {
				this.mNoteTextChangeDuration = attributes.getInt(index, (int) mNoteTextChangeDuration);
			} else if (index == R.styleable.Ui_InputLayout_uiInputFeatures) {
				this.mFeatures = attributes.getInteger(index, mFeatures);
			}
		}
		attributes.recycle();

		this.ensureTintInfo();
		int tintColor = Color.TRANSPARENT;
		final Resources.Theme theme = context.getTheme();
		if (theme != null) {
			final TypedValue typedValue = new TypedValue();
			if (theme.resolveAttribute(R.attr.colorControlActivated, typedValue, true)) {
				tintColor = typedValue.data;
			}
			if (theme.resolveAttribute(R.attr.uiColorErrorHighlight, typedValue, true)) {
				mColorsInfo.errorColors = ColorStateList.valueOf(typedValue.data);
			}
		}
		mTintInfo.labelTint = TintManager.createLabelTintColors(getContext(), tintColor);
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
		event.setClassName(InputLayout.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(InputLayout.class.getName());
	}

	/**
	 */
	@Override
	protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
		return new InputLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}

	/**
	 */
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
		return new InputLayout.LayoutParams(params);
	}

	/**
	 */
	@Override
	public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new InputLayout.LayoutParams(getContext(), attrs);
	}

	/**
	 */
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
		return params instanceof InputLayout.LayoutParams;
	}

	/**
	 * Requests the given <var>features</var> to be enabled for this input layout.
	 *
	 * @param features The desired input features. One of {@link #FEATURE_LABEL}, {@link #FEATURE_NOTE},
	 *                 {@link #FEATURE_CONSTRAINT} or theirs combination.
	 * @return {@code True} if the current features has been changed, {@code false} otherwise.
	 * @see R.attr#uiInputFeatures ui:uiInputFeatures
	 * @see #hasInputFeature(int)
	 * @see #clearInputFeatures()
	 */
	public boolean requestInputFeatures(@InputFeature int features) {
		if (mFeatures != features) {
			this.mFeatures = features;
			this.updateFeaturesVisibility();
			onInputFeaturesChange(mFeatures);
			return true;
		}
		return false;
	}

	/**
	 * Clears the current input features enabled for this input layout.
	 *
	 * @see #requestInputFeatures(int)
	 */
	public void clearInputFeatures() {
		this.mFeatures = FEATURE_NONE;
		this.updateFeaturesVisibility();
		onInputFeaturesChange(mFeatures);
	}

	/**
	 * Returns a flag indicating whether this input layout has the specified <var>feature</var>
	 * enabled or not.
	 *
	 * @param feature The desired input feature to check. One of {@link #FEATURE_LABEL}, {@link #FEATURE_NOTE},
	 *                {@link #FEATURE_CONSTRAINT} or theirs combination.
	 * @return {@code True} if input feature is enabled, {@code false} otherwise.
	 * @see #requestInputFeatures(int)
	 * @see #clearInputFeatures()
	 */
	public boolean hasInputFeature(@InputFeature int feature) {
		return this.hasFeature(feature);
	}

	/**
	 * Adds the label view into this input layout. The label view will be layouted at the beginning
	 * of this layout above the input view.
	 *
	 * @param labelView The desired view to represent label view of this layout.
	 */
	public void addLabelView(@NonNull TextView labelView) {
		this.removeLabelView();
		super.addView(mLabelView = labelView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.updateLabelFeatureVisibility();
		onLabelViewChanged(mLabelView);
	}

	/**
	 * Returns the text view which represents the label for this input layout.
	 * <p>
	 * This view is basically displayed above the input view.
	 *
	 * @return Label text view.
	 */
	@NonNull
	public TextView getLabelView() {
		return mLabelView;
	}

	/**
	 * Removes the current label view from this input layout if any.
	 *
	 * @return Removed label view or {@code null} if there was no label view presented.
	 */
	@Nullable
	public TextView removeLabelView() {
		if (mLabelView != null){
			removeView(mLabelView);
			return mLabelView;
		}
		return null;
	}

	/**
	 * Adds the input view into this input layout. The input view will be layouted after the label
	 * view (if any).
	 *
	 * @param inputView The desired view to represent input view of this layout.
	 */
	public void addInputView(@NonNull View inputView) {
		if (mInputView != null) {
			removeView(mInputView);
		}
		super.addView(mInputView = inputView, Math.min(1, getChildCount()), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		onInputViewChanged(mInputView);
	}

	/**
	 * Returns the input view of this input layout.
	 *
	 * @return Input view.
	 */
	@NonNull
	public View getInputView() {
		return mInputView;
	}

	/**
	 * Adds the note view into this input layout. The note view will be layouted after the input view
	 * from the left. The given view will be also used to show error set to this layout by {@link #setError(CharSequence)}.
	 *
	 * @param noteView The desired view to represent note view of this layout.
	 */
	public void addNoteView(@NonNull TextView noteView) {
		this.removeNoteView();
		super.addView(mNoteView = noteView, Math.min(2, getChildCount()), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.updateNoteFeatureVisibility();
		onNoteViewChanged(mNoteView);
	}

	/**
	 * Returns the text view which presents note/error text set to this input layout.
	 * <p>
	 * This view is basically displayed below the input view.
	 *
	 * @return Error text view.
	 */
	@NonNull
	public TextView getNoteView() {
		return mNoteView;
	}

	/**
	 * Removes the current note view from this input layout if any.
	 *
	 * @return Removed note view or {@code null} if there was no note view presented.
	 */
	@Nullable
	public TextView removeNoteView() {
		if (mNoteView != null) {
			removeView(mNoteView);
			return mNoteView;
		}
		return null;
	}

	/**
	 * Adds the constraint view into this input layout. The constraint view will be layouted after
	 * the input view (at the same base line as the note view) but from the right.
	 *
	 * @param constraintView The desired view to represent constraint view of this layout.
	 */
	public void addConstraintView(@NonNull TextView constraintView) {
		this.removeConstraintView();
		super.addView(mConstraintView = constraintView, Math.min(3, getChildCount()), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.updateConstraintFeatureVisibility();
		onConstraintViewChanged(mConstraintView);
	}

	/**
	 * Returns the text view which presents constraint for input value of this input layout.
	 * <p>
	 * This view is basically displayed below the input view.
	 *
	 * @return Constraint text view.
	 */
	@NonNull
	public TextView getConstraintView() {
		return mConstraintView;
	}

	/**
	 * Removes the current constraint view from this input layout if any.
	 *
	 * @return Removed constraint view or {@code null} if there was no constraint view presented.
	 */
	@Nullable
	public TextView removeConstraintView() {
		if (mConstraintView != null) {
			removeView(mConstraintView);
			return mConstraintView;
		}
		return null;
	}

	/**
	 */
	@Override
	public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
		if (validateInputView(child)) {
			addInputView(child);
			return;
		}
		super.addView(child, index, params);
	}

	/**
	 * Same as {@link #setLabel(CharSequence)} for resource id.
	 *
	 * @param resId Resource id of the desired label text.
	 */
	public void setLabel(@StringRes int resId) {
		setLabel(mResources.getText(resId));
	}

	/**
	 * Sets the text for the label view of this input layout.
	 *
	 * @param label The desired label text.
	 * @see android.R.attr#label android:label
	 * @see #getLabel()
	 */
	public void setLabel(@Nullable CharSequence label) {
		this.mLabel = label != null ? label : "";
		if (mLabelView != null) mLabelView.setText(mLabel);
	}

	/**
	 * Returns the current text presented by label view of this layout.
	 *
	 * @return Label text set by {@link #setLabel(CharSequence)}.
	 */
	@NonNull
	public CharSequence getLabel() {
		return mLabel;
	}

	/**
	 * Same as {@link #setNote(CharSequence)}, but for resource id.
	 *
	 * @param resId Resource id of the desired note text.
	 */
	public void setNote(@StringRes int resId) {
		setNote(mResources.getText(resId));
	}

	/**
	 * Sets the text for the note view of this input layout.
	 *
	 * @param note The desired note text.
	 * @see R.attr#uiNote ui:uiNote
	 * @see #getNote()
	 */
	public void setNote(@Nullable CharSequence note) {
		this.mNote = note != null ? note : "";
		if (mNoteView != null && (mPrivateFlags & PFLAG_HAS_ERROR) == 0) {
			mNoteView.setText(mNote);
		}
	}

	/**
	 * Returns the current text presented by note view of this layout.
	 *
	 * @return Label text set by {@link #setNote(CharSequence)}.
	 */
	@NonNull
	public CharSequence getNote() {
		return mNote;
	}

	/**
	 * Same as {@link #setConstraint(CharSequence)}, but for resource id.
	 *
	 * @param resId Resource id of the desired constraint text.
	 */
	public void setConstraint(@StringRes int resId) {
		setConstraint(mResources.getText(resId));
	}

	/**
	 * Sets the text for the constraint view of this input layout.
	 *
	 * @param constraint The desired constraint text.
	 * @see #getConstraint()
	 */
	public void setConstraint(@Nullable CharSequence constraint) {
		this.mConstraint = constraint != null ? constraint : "";
		if (mConstraintView != null) mConstraintView.setText(mConstraint);
	}

	/**
	 * Returns the current text presented by constraint view of this layout.
	 *
	 * @return Label text set by {@link #setConstraint(CharSequence)}.
	 */
	@NonNull
	public CharSequence getConstraint() {
		return mConstraint;
	}

	/**
	 * Same as {@link #setError(CharSequence)}, but for resource id.
	 *
	 * @param resId Resource id of the desired error text.
	 */
	public void setError(@StringRes int resId) {
		setError(mResources.getText(resId));
	}

	/**
	 * Sets the error text for this input layout. Calling this method will also change the background
	 * of the current input view to the error one (commonly with red highlight) and marks this layout
	 * that it has error, so calling {@link #hasError()} will return {@code true}. Error can be later
	 * cleared by {@link #clearError()}.
	 * <p>
	 * <b>Note</b>, that for displaying of error text is used note view and changing of the current
	 * note text for error text will be done by using of cross-fade animation so the error text will
	 * be displayed below the input view at the left.
	 *
	 * @param error The desired error text.
	 */
	@Override
	public void setError(@NonNull CharSequence error) {
		if (!TextUtils.equals(mError, error)) {
			final boolean hasError = (mPrivateFlags & PFLAG_HAS_ERROR) != 0;
			this.updatePrivateFlags(PFLAG_HAS_ERROR, true);
			if (mInputView instanceof ErrorWidget) {
				((ErrorWidget) mInputView).setError(error);
			}
			this.handleErrorUpdate(error, !hasError);
		}
	}

	/**
	 * Returns a flag indicating whether this input layout has some error set or not.
	 *
	 * @return {@code True} if this layout has error set, {@code false} otherwise.
	 * @see #setError(CharSequence)
	 */
	@Override
	public boolean hasError() {
		return (mPrivateFlags & PFLAG_HAS_ERROR) != 0;
	}

	/**
	 * Returns the current error text set to this input layout.
	 *
	 * @return Error text set via {@link #setError(CharSequence)}.
	 */
	@NonNull
	@Override
	public CharSequence getError() {
		return mError;
	}

	/**
	 * Clears the current error text of this input layout. Calling of this method will also change
	 * the background of the input view back to the original one. Also the current note text will
	 * be set back to the note view, as described in {@link #setError(CharSequence)}, error text for
	 * note text change will be handled by using of cross-fade animation.
	 */
	@Override
	public void clearError() {
		if (mInputView instanceof ErrorWidget) {
			((ErrorWidget) mInputView).clearError();
		}
		clearError(true);
	}

	/**
	 * Returns a flag indicating whether this input layout has error highlight visible or not.
	 * <p>
	 * Error highlight is visible whenever this input layout has some error set ({@link #hasError()})
	 * or if the current input exceeds current constraints.
	 *
	 * @return {@code True} if error highlight is visible, {@code false} otherwise.
	 */
	public boolean isErrorHighlightVisible() {
		return (mPrivateFlags & PFLAG_ERROR_HIGHLIGHT_VISIBLE) != 0;
	}

	/**
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.updateChildrenEnabledState(enabled);
	}

	/**
	 * Sets the duration for the text change within note view used when changing note text for the
	 * error text played whenever {@link #setError(CharSequence)} or {@link #clearError()} are called
	 * and the mentioned texts need to be properly changed.
	 *
	 * @param duration The desired duration in milliseconds.
	 */
	public void setNoteTextChangeDuration(long duration) {
		this.mNoteTextChangeDuration = duration;
	}

	/**
	 * Returns the duration for the text change within note view used when changing note text for the
	 * error text and backwards.
	 * <p>
	 * Default value: <b>{@link 150 ms}</b>
	 *
	 * @return Duration in milliseconds.
	 */
	public long getNoteTextChangeDuration() {
		return mNoteTextChangeDuration;
	}

	/**
	 * Invoked whenever {@link #addLabelView(android.widget.TextView)} is called upon this layout
	 * and the specified <var>labelView</var> has been changed.
	 *
	 * @param labelView The changed label view.
	 */
	protected void onLabelViewChanged(@NonNull TextView labelView) {
		if (!TextUtils.isEmpty(mLabel)) {
			labelView.setText(mLabel);
		}
		this.applyLabelTextTint();
	}

	/**
	 * Called whenever some of {@code addView(...)} methods is called upon this input layout to validate
	 * if the specified <var>view</var> can be input view for this input layout.
	 *
	 * @param view The view to be validated for input view.
	 * @return {@code True} to add the given view into this layout as input view by {@link #addInputView(android.view.View)},
	 * {@code false} to add as regular view.
	 */
	protected boolean validateInputView(@NonNull View view) {
		return false;
	}

	/**
	 * Invoked whenever {@link #addInputView(android.view.View)} is called upon this layout
	 * and the specified <var>inputView</var> has been changed.
	 *
	 * @param inputView The changed input view.
	 */
	protected void onInputViewChanged(@NonNull View inputView) {
	}

	/**
	 * Invoked whenever {@link #addNoteView(android.widget.TextView)} is called upon this layout
	 * and the specified <var>noteView</var> has been changed.
	 *
	 * @param noteView The changed note view.
	 */
	protected void onNoteViewChanged(@NonNull TextView noteView) {
		if (!TextUtils.isEmpty(mNote) && (mPrivateFlags & PFLAG_HAS_ERROR) == 0) {
			noteView.setText(mLabel);
		} else if (!TextUtils.isEmpty(mError) && (mPrivateFlags & PFLAG_HAS_ERROR) != 0) {
			noteView.setText(mError);
		}
	}

	/**
	 * Invoked whenever {@link #addConstraintView(android.widget.TextView)} is called upon this layout
	 * and the specified <var>noteView</var> has been changed.
	 *
	 * @param constraintView The changed constraint view.
	 */
	protected void onConstraintViewChanged(@NonNull TextView constraintView) {
		if (!TextUtils.isEmpty(mConstraint)) {
			constraintView.setText(mConstraint);
		}
	}

	/**
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			final LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
			switch (layoutParams.childType) {
				case LayoutParams.TYPE_INPUT:
					if (child != mInputView) {
						removeView(mInputView);
						this.mInputView = child;
					}
					break;
				case LayoutParams.TYPE_LABEL:
					if (child != mLabelView && child instanceof TextView) {
						removeView(mLabelView);
						this.mLabelView = (TextView) child;
					}
					break;
				case LayoutParams.TYPE_NOTE:
					if (child != mNoteView && child instanceof TextView) {
						removeView(mNoteView);
						this.mNoteView = (TextView) child;
					}
					break;
				case LayoutParams.TYPE_CONSTRAINT:
					if (child != mConstraintView && child instanceof TextView) {
						removeView(mConstraintView);
						this.mConstraintView = (TextView) child;
					}
					break;
			}
		}
		if (mLabelView != null && !TextUtils.isEmpty(mLabel)) {
			mLabelView.setText(mLabel);
		}
		if (mNoteView != null) {
			if (!TextUtils.isEmpty(mNote) && (mPrivateFlags & PFLAG_HAS_ERROR) == 0) {
				mNoteView.setText(mNote);
			} else if (!TextUtils.isEmpty(mError) && (mPrivateFlags & PFLAG_HAS_ERROR) != 0) {
				mNoteView.setText(mError);
			}
		}
		this.updateChildrenEnabledState(isEnabled());
		this.applyLabelTextTint();
		this.updateFeaturesVisibility();
	}

	/**
	 * Updates visibility of the featured views depends on the current input feature flags.
	 */
	private void updateFeaturesVisibility() {
		this.updateLabelFeatureVisibility();
		this.updateNoteFeatureVisibility();
		this.updateConstraintFeatureVisibility();
	}

	/**
	 * Updates visibility of the label view depends on if {@link #FEATURE_LABEL} feature is enabled
	 * for this input layout or not.
	 */
	private void updateLabelFeatureVisibility() {
		this.updateFeatureViewVisibility(mLabelView, FEATURE_LABEL);
	}

	/**
	 * Updates visibility of the note view depends on if {@link #FEATURE_NOTE} feature is enabled
	 * for this input layout or not.
	 */
	private void updateNoteFeatureVisibility() {
		this.updateFeatureViewVisibility(mNoteView, FEATURE_NOTE);
	}

	/**
	 * Updates visibility of the note view depends on if {@link #FEATURE_CONSTRAINT} feature is enabled
	 * for this input layout or not.
	 */
	private void updateConstraintFeatureVisibility() {
		this.updateFeatureViewVisibility(mConstraintView, FEATURE_CONSTRAINT);
	}

	/**
	 * Updates visibility of the specified <var>featureView</var> depending on if a feature with the
	 * specified <var>featureFlag</var> is enabled or not.
	 *
	 * @param featureView The feature view of which visibility to update.
	 * @param featureFlag Flag of the feature to determine the proper visibility.
	 */
	private void updateFeatureViewVisibility(View featureView, int featureFlag) {
		if (featureView != null) featureView.setVisibility(
				(mFeatures & featureFlag) != 0 ? VISIBLE : GONE
		);
	}

	/**
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mInputView == null) {
			throw new IllegalStateException("Input view not found within InputLayout.");
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
		int height = 0;
		if (mInputView.getVisibility() != View.GONE) {
			final LayoutParams params = (LayoutParams) mInputView.getLayoutParams();
			height += mInputView.getMeasuredHeight() + params.topMargin + params.bottomMargin;

			final int contentWidth = width - getPaddingLeft() - getPaddingRight();
			if (params.width == LayoutParams.MATCH_PARENT && mInputView.getMeasuredWidth() < contentWidth) {
				// Measure input view's width to match parent size.
				measureChild(mInputView,
						MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
						heightMeasureSpec
				);
			}
		}
		if (mLabelView != null && mLabelView.getVisibility() != GONE) {
			final LayoutParams params = (LayoutParams) mLabelView.getLayoutParams();
			height += mLabelView.getMeasuredHeight() + params.topMargin + params.bottomMargin;
		}
		int footerHeight, noteWidth, constraintWidth;
		footerHeight = noteWidth = 0;
		if (mNoteView != null && mNoteView.getVisibility() != GONE) {
			final LayoutParams params = (LayoutParams) mNoteView.getLayoutParams();
			footerHeight += mNoteView.getMeasuredHeight() + params.topMargin + params.bottomMargin;
			noteWidth = mNoteView.getMeasuredWidth() + params.leftMargin + params.rightMargin;
		}
		if (mConstraintView != null && mConstraintView.getVisibility() != GONE) {
			final LayoutParams params = (LayoutParams) mConstraintView.getLayoutParams();
			footerHeight = Math.max(footerHeight, mConstraintView.getMeasuredHeight() + params.topMargin + params.bottomMargin);
			constraintWidth = mConstraintView.getMeasuredWidth() + params.leftMargin + params.rightMargin;

			// Check for width of note + constraint text view and if it exceeds edit text's width
			// re-measure note text view to be width only as it is available from edit text's width
			// minus note text view width.
			final int contentWidth = width - getPaddingLeft() - getPaddingRight();
			if ((noteWidth + constraintWidth) > contentWidth) {
				if (noteWidth > 0) {
					final LayoutParams errorParams = (LayoutParams) mNoteView.getLayoutParams();
					measureChild(mNoteView,
							MeasureSpec.makeMeasureSpec(contentWidth - constraintWidth, MeasureSpec.AT_MOST),
							heightMeasureSpec
					);
					footerHeight = Math.max(footerHeight, mNoteView.getMeasuredHeight() + errorParams.topMargin + errorParams.bottomMargin);
				} else {
					measureChild(mConstraintView,
							MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.AT_MOST),
							heightMeasureSpec
					);
					footerHeight = Math.max(footerHeight, mConstraintView.getMeasuredHeight() + params.topMargin + params.bottomMargin);
				}
			}
		}
		height += footerHeight;
		// Take into count also padding.
		height += getPaddingTop() + getPaddingBottom();
		// Check also against minimum size.
		height = Math.max(height, getSuggestedMinimumHeight());
		setMeasuredDimension(width, height);
	}

	/**
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int childTop = getPaddingTop();
		// Layout label text view from the top|left.
		// fixme: we should also provide getPaddingRight() value to this method
		childTop = this.layoutChildWithMargins(mLabelView, getPaddingLeft(), childTop);
		// Layout input view from the next-top|left.
		// fixme: we should also provide getPaddingRight() value to this method
		childTop = this.layoutChildWithMargins(mInputView, getPaddingLeft(), childTop);
		// Layout note/error text view from the next-top|left.
		this.layoutChildWithMargins(mNoteView, getPaddingLeft(), childTop);
		if (mConstraintView != null) {
			// Layout constraint text view from the next-top|right.
			final LayoutParams constraintParams = (LayoutParams) mConstraintView.getLayoutParams();
			final int constraintViewWidth = constraintParams.leftMargin + constraintParams.rightMargin + mConstraintView.getMeasuredWidth();
			this.layoutChildWithMargins(
					mConstraintView,
					getMeasuredWidth() - getPaddingRight() - constraintViewWidth,
					childTop
			);
		}
	}

	/**
	 * Layouts the given <var>child</var> view at the specified <var>left</var> and <var>top</var>
	 * position taking into count also margins of the view.
	 *
	 * @param child The child view to be layouted.
	 * @param left  The desired left position for the child view.
	 * @param top   The desired top position for the child view.
	 * @return New top position for the next child view to be layouted below the specified view.
	 */
	private int layoutChildWithMargins(View child, int left, int top) {
		if (child != null && child.getVisibility() != View.GONE) {
			final LayoutParams params = (LayoutParams) child.getLayoutParams();
			child.layout(
					left += params.leftMargin,
					top += params.topMargin,
					left + child.getMeasuredWidth(),
					top + child.getMeasuredHeight()
			);
			top += child.getMeasuredHeight() + params.bottomMargin;
		}
		return top;
	}

	/**
	 * Invoked whenever {@link #requestInputFeatures(int)} is called upon this layout and the specified
	 * <var>features</var> has been changed.
	 *
	 * @param features The changed input features.
	 */
	protected void onInputFeaturesChange(@InputFeature int features) {
	}

	/**
	 * Invoked whenever {@link #setError(CharSequence)} or {@link #clearError()} is called and text
	 * within note text view need to be updated.
	 * <p>
	 * This implementation basically fades out the <var>noteView</var> changes its text and fades it
	 * back.
	 *
	 * @param noteView Note text view to be animated.
	 * @param text     The text to be changed within not view.
	 * @param colors   ColorStateList which should be also changed for the note view.
	 * @return {@code True} if text change has been handled by animation, {@code false} if text should
	 * be changed just by setting text to the note view directly.
	 */
	@SuppressWarnings("NewApi")
	protected boolean onAnimateNoteTextChange(@NonNull TextView noteView, @NonNull CharSequence text, @NonNull ColorStateList colors) {
		if (mNoteTextChangeDuration > 0 && isAttachedToWindow() && ANIMABLE) {
			mNoteTextChangeListener.text = text;
			mNoteTextChangeListener.colors = colors;
			noteView.animate()
					.setDuration(mNoteTextChangeDuration)
					.setListener(mNoteTextChangeListener)
					.alpha(0)
					.start();
			return true;
		}
		return false;
	}

	/**
	 * Updates the given <var>text</var> and <var>colors</var> of the note view of this layout.
	 *
	 * @param text   The new text for note view.
	 * @param colors The new colors for note view.
	 */
	final void updateNoteViewTextAndColors(CharSequence text, ColorStateList colors) {
		if (mNoteView != null) {
			mNoteView.setText(text);
			mNoteView.setTextColor(colors);
		}
	}

	/**
	 * Clears the current error text of this input layout. Calling of this method will also change
	 * the background of the input view back to the original one. Also the current note text will
	 * be set back to the note view, as described in {@link #setError(CharSequence)}, error text for
	 * note text change will be handled by using of cross-fade animation.
	 *
	 * @param animate {@code True} to animate clearing of the error, {@code false} otherwise.
	 */
	final void clearError(boolean animate) {
		if ((mPrivateFlags & PFLAG_HAS_ERROR) != 0) {
			this.updatePrivateFlags(PFLAG_HAS_ERROR, false);
			handleErrorUpdate("", animate);
		}
	}

	/**
	 * Changes visibility of the error highlight of the edit text and also note and constraint text
	 * views.
	 *
	 * @param visible {@code True} to show error highlight, {@code false} to clear it.
	 */
	final void setErrorHighlightVisible(boolean visible) {
		if (visible != hasPrivateFlag(PFLAG_ERROR_HIGHLIGHT_VISIBLE)) {
			if (visible) {
				this.updatePrivateFlags(PFLAG_ERROR_HIGHLIGHT_VISIBLE, true);
				// Save the original constraint and note text colors.
				final ColorStateList constraintColors = mConstraintView != null ? mConstraintView.getTextColors() : null;
				if (constraintColors != null && !constraintColors.equals(mColorsInfo.errorColors)) {
					mColorsInfo.constraintColors = constraintColors;
				}
				final ColorStateList noteColors = mNoteView != null ? mNoteView.getTextColors() : null;
				if (noteColors != null && !noteColors.equals(mColorsInfo.errorColors)) {
					mColorsInfo.noteColors = noteColors;
				}
				this.updateTextViewColors(mConstraintView, mColorsInfo.errorColors);
			} else if ((mPrivateFlags & PFLAG_HAS_ERROR) == 0) {
				this.updatePrivateFlags(PFLAG_ERROR_HIGHLIGHT_VISIBLE, false);
				this.updateTextViewColors(mConstraintView, mColorsInfo.constraintColors);
			}
		}
	}

	/**
	 * Applies tint to the label view's color from the current {@link #mTintInfo}.
	 */
	private void applyLabelTextTint() {
		if (mLabelView != null &&
				mLabelView.getCurrentTextColor() == Color.TRANSPARENT &&
				mTintInfo != null &&
				mTintInfo.labelTint != null) {
			mLabelView.setTextColor(mTintInfo.labelTint);
		}
	}

	/**
	 * Handles update of the specified <var>error</var>.
	 *
	 * @param error         Error text which should be currently displayed in note view. If empty the note
	 *                      text will be set to the note view.
	 * @param animateChange {@code True} to animate change in note vs. error texts, {@code false} othrewise.
	 */
	private void handleErrorUpdate(CharSequence error, boolean animateChange) {
		this.mError = error != null ? error : "";
		if (mNoteView == null || mInputView == null) {
			refreshDrawableState();
			invalidate();
			return;
		}
		if ((mPrivateFlags & PFLAG_HAS_ERROR) != 0) {
			this.setErrorHighlightVisible(true);
			if (animateChange) {
				if (!onAnimateNoteTextChange(mNoteView, mError, mColorsInfo.errorColors)) {
					updateNoteViewTextAndColors(mError, mColorsInfo.errorColors);
				}
			} else {
				updateNoteViewTextAndColors(mError, mColorsInfo.errorColors);
			}
		} else {
			this.setErrorHighlightVisible(false);
			if (animateChange) {
				if (!onAnimateNoteTextChange(mNoteView, mNote, mColorsInfo.noteColors)) {
					updateNoteViewTextAndColors(mNote, mColorsInfo.noteColors);
				}
			} else {
				updateNoteViewTextAndColors(mNote, mColorsInfo.noteColors);
			}
		}
		refreshDrawableState();
		invalidate();
	}

	/**
	 * Updates enabled state of the current children of this layout.
	 *
	 * @param enabled {@code True} to set children enabled, {@code false} otherwise.
	 */
	private void updateChildrenEnabledState(boolean enabled) {
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);
			if (child != null) child.setEnabled(enabled);
		}
	}

	/**
	 * Updates text color of the specified <var>textView</var> to the given ones.
	 *
	 * @param textView The text view of which colors to update.
	 * @param colors   The desired colors to be set to the text view.
	 */
	private void updateTextViewColors(TextView textView, ColorStateList colors) {
		if (textView != null) textView.setTextColor(colors);
	}

	/**
	 */
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (hasError()) {
			mergeDrawableStates(drawableState, WidgetStateSet.ERROR);
		}
		return drawableState;
	}

	/**
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.label = mLabel;
		savedState.note = mNote;
		savedState.error = mError;
		savedState.constraint = mConstraint;
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
		if (savedState.label != null) {
			setLabel(savedState.label);
		}
		if (savedState.note != null) {
			setNote(savedState.note);
		}
		if (!TextUtils.isEmpty(savedState.error)) {
			setError(savedState.error);
		}
		if (savedState.constraint != null) {
			setConstraint(savedState.constraint);
		}
	}

	/**
	 * Updates the current features flags.
	 *
	 * @param feature Value of the desired feature to add/remove to/from the current features flags.
	 * @param add     Boolean flag indicating whether to add or remove the specified <var>feature</var>.
	 */
	@SuppressWarnings("unused")
	final void updateFeatures(int feature, boolean add) {
		if (add) this.mFeatures |= feature;
		else this.mFeatures &= ~feature;
	}

	/**
	 * Returns a boolean flag indicating whether the specified <var>feature</var> is contained within
	 * the current features flags or not.
	 *
	 * @param feature Value of the feature to check.
	 * @return {@code True} if the requested feature is contained, {@code false} otherwise.
	 */
	@SuppressWarnings("unused")
	final boolean hasFeature(int feature) {
		return (mFeatures & feature) != 0;
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
	 * Per-child layout information for child views of {@link InputLayout}.
	 * <p>
	 * See {@link R.styleable#Ui_InputLayout_LayoutParams Layout Attributes} for a list of all
	 * child view attributes that this class supports.
	 *
	 * @author Martin Albedinsky
	 */
	public static class LayoutParams extends FrameLayoutWidget.LayoutParams {

		/**
		 * Type flag to identify child view that contains a user <b>input</b>.
		 */
		static final int TYPE_INPUT = 0x01;

		/**
		 * Type flag to identify child view that presents a <b>label</b> text.
		 */
		static final int TYPE_LABEL = 0x02;

		/**
		 * Type flag to identify child view that presents a <b>note</b> text.
		 */
		static final int TYPE_NOTE = 0x03;

		/**
		 * Type flag to identify child view that presents a <b>constraint</b> text.
		 */
		static final int TYPE_CONSTRAINT = 0x04;

		/**
		 * Type of a view associated with these parameters. This type is used by InputLayout to
		 * respectively treat and properly position each of its child views.
		 */
		private int childType;

		/**
		 * Creates a new instance of LayoutParams configured from the specified <var>attrs</var>.
		 *
		 * @param context Context used to parse the given attributes set.
		 * @param attrs   The attributes set from which to configure the new layout params.
		 */
		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_InputLayout_LayoutParams, 0, 0);
			if (typedArray != null) {
				final int n = typedArray.getIndexCount();
				for (int i = 0; i < n; i++) {
					final int index = typedArray.getIndex(i);
					if (index == R.styleable.Ui_InputLayout_LayoutParams_uiInputChildType) {
						this.childType = typedArray.getInt(index, childType);
					}
				}
				typedArray.recycle();
			}
		}

		/**
		 * Creates a new instance of LayoutParams with the specified size parameters.
		 *
		 * @param width  The width attribute for the new layout params.
		 * @param height The height attribute for the new layout params.
		 */
		public LayoutParams(int width, int height) {
			super(width, height);
		}

		/**
		 * Creates a new instance of LayoutParams with the specified size and gravity parameters.
		 *
		 * @param width   The width attribute for the new layout params.
		 * @param height  The height attribute for the new layout params.
		 * @param gravity The gravity attribute for the new layout params.
		 */
		public LayoutParams(int width, int height, int gravity) {
			super(width, height, gravity);
		}

		/**
		 * Creates a new instance of LayoutParams from the given <var>source</var> parameters.
		 *
		 * @param source The source layout parameters from which to configure the new layout params.
		 */
		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		/**
		 * Creates a new instance of LayoutParams from the given <var>source</var> parameters.
		 *
		 * @param source The source layout parameters from which to configure the new layout params.
		 */
		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		/**
		 * Creates a new instance of LayoutParams from the given <var>source</var> parameters.
		 *
		 * @param source The source layout parameters from which to configure the new layout params.
		 */
		@TargetApi(Build.VERSION_CODES.KITKAT)
		public LayoutParams(FrameLayout.LayoutParams source) {
			super(source);
		}
	}

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link InputLayout}
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
		CharSequence label, note, error, constraint;

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
			this.label = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
			this.note = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
			this.error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
			this.constraint = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			TextUtils.writeToParcel(label, dest, flags);
			TextUtils.writeToParcel(note, dest, flags);
			TextUtils.writeToParcel(error, dest, flags);
			TextUtils.writeToParcel(constraint, dest, flags);
		}
	}

	/**
	 * This class holds all data necessary to tint all components of this view.
	 */
	private static final class TintInfo extends Widget.TintInfo {

		/**
		 * Colors used to tint label text view.
		 */
		ColorStateList labelTint;
	}

	/**
	 * This class holds all ColorStateLists used within this layout for its TextViews.
	 */
	private static final class ColorsInfo {

		/**
		 * Set of colors set to text views in case of error.
		 */
		private ColorStateList errorColors = ColorStateList.valueOf(Color.parseColor("#F44336"));

		/**
		 * Original set of colors for note view's text.
		 */
		private ColorStateList noteColors;

		/**
		 * Original set of colors for constraint view's text.
		 */
		private ColorStateList constraintColors;
	}

	/**
	 * Animation listener used when calling {@link #onAnimateNoteTextChange(android.widget.TextView, CharSequence, android.content.res.ColorStateList)}
	 * to properly change note text and colors.
	 */
	@SuppressWarnings("NewApi")
	private final class NoteTextChangeListener extends AnimatorListenerAdapter {

		/**
		 * Colors to be changed for note view.
		 */
		ColorStateList colors;

		/**
		 * Text to be changed within note view.
		 */
		CharSequence text;

		/**
		 */
		@Override
		public void onAnimationEnd(Animator animation) {
			if (mNoteView != null) {
				mNoteView.setText(text);
				mNoteView.setTextColor(colors);
				mNoteView.postDelayed(new Runnable() {
					@Override
					public void run() {
						startFadeInAnimation();
					}
				}, 100);
			}
		}

		/**
		 * Starts fade in animation for the note text view.
		 */
		void startFadeInAnimation() {
			mNoteView.animate()
					.setDuration(mNoteTextChangeDuration)
					.setListener(null)
					.alpha(1)
					.start();
		}
	}
}
