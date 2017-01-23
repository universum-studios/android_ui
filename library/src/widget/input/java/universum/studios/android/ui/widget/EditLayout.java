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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;

/**
 * Implementation of {@link InputLayout} to provide input container for {@link EditText} component.
 * All features supported by {@link InputLayout} are also supported by this layout. By default,
 * EditLayout creates full view hierarchy on its initialization with <b>label</b>, <b>note</b>,
 * <b>edit text</b> and <b>constraint</b> views that will be laid out as described in {@link InputLayout}
 * class overview. Whether to show or hide some of these views can be requested by specifying desired
 * input features via {@link #requestInputFeatures(int)}.
 *
 * <h3>EditText set up</h3>
 * EditText of EditLayout can be accessed via {@link #getInputView()} method. EditLayout implements
 * only necessary delegate methods to perform base set up of its input view like setting its hint text
 * via {@link #setHint(CharSequence)} or its input type via {@link #setInputType(int)}.
 *
 * <h3>XML layout</h3>
 * As described in {@link InputLayout} class overview in the same section, also EditLayout can be
 * used within an Xml layout. The following sample shows Xml layout structure with different usages:
 * <pre>
 * &lt;LinearLayout
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *      xmlns:ui="http://schemas.android.com/apk/res-auto"
 *      android:layout_width="match_parent"
 *      android:layout_height="match_parent"&gt;
 *
 *      &lt;!-- Default usage. --&gt;
 *      &lt;universum.studios.android.ui.widget.EditLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"/&gt;
 *
 *      --------------------------------------------------------------------------------------------
 *
 *      &lt;!-- Layout with custom EditText. --&gt;
 *      &lt;universum.studios.android.ui.widget.EditLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"&gt;
 *
 *              &lt;EditText
 *                      android:layout_width="match_parent"
 *                      android:layout_height="wrap_content"
 *                      ui:uiInputChildType="input"/&gt;
 *
 *      &lt;universum.studios.android.ui.widget.EditLayout&gt;
 *
 *      --------------------------------------------------------------------------------------------
 *
 *      &lt;!-- Layout with all custom views. --&gt;
 *      &lt;universum.studios.android.ui.widget.EditLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"
 *              ui:uiWithEmptyViewHierarchy="true"&gt;
 *              &lt;!--
 *                  uiWithEmptyViewHierarchy="true"
 *                  This flag ensures that the EditLayout will be initialized with the empty view
 *                  hierarchy so we can supply the custom one.
 *              --&gt;
 *
 *              &lt;!-- Label view. --&gt;
 *              &lt;TextView
 *                      android:layout_width="wrap_content"
 *                      android:layout_height="wrap_content"
 *                      ui:uiInputChildType="label"/&gt;
 *
 *              &lt;!-- Input view. --&gt;
 *              &lt;EditText
 *                      android:layout_width="match_parent"
 *                      android:layout_height="wrap_content"
 *                      ui:uiInputChildType="input"/&gt;
 *
 *              &lt;!-- Below views are optional. =========================================== --&gt;
 *
 *              &lt;!-- Note view. --&gt;
 *              &lt;TextView
 *                      android:layout_width="wrap_content"
 *                      android:layout_height="wrap_content"
 *                      ui:uiInputChildType="note"/&gt;
 *
 *              &lt;!-- Constraint view. --&gt;
 *              &lt;TextView
 *                      android:layout_width="wrap_content"
 *                      android:layout_height="wrap_content"
 *                      ui:uiInputChildType="constraint"/&gt;
 *
 *      &lt;universum.studios.android.ui.widget.EditLayout&gt;
 * &lt;LinearLayout&gt;
 * </pre>
 *
 * <h3>XML attributes</h3>
 * See {@link InputLayout},
 * {@link R.styleable#Ui_EditLayout EditLayout Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiEditLayoutStyle uiEditLayoutStyle}
 *
 * @author Martin Albedinsky
 * @see SpinnerLayout
 */
public class EditLayout extends InputLayout {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Listener that can receive a callback about changed focus of input view.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnInputFocusChangeListener {

		/**
		 * Invoked whenever focus of input view of the specified <var>editLayout</var> has changed.
		 *
		 * @param editLayout    The edit layout of which input view's focus changed.
		 * @param inputHasFocus {@code True} if the input view has focus, {@code false} otherwise.
		 */
		void onInputFocusChange(@NonNull EditLayout editLayout, boolean inputHasFocus);
	}

	/**
	 * Listener that can receive callback about a length of the changed input text.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnInputLengthChangeListener {

		/**
		 * Invoked whenever a text has been changed, specifically its length to the given one, within
		 * EditText of the specified <var>editLayout</var>.
		 *
		 * @param editLayout The edit layout to which belongs the EditText with changed editable text.
		 * @param length     The current length of the editable text.
		 */
		void onInputLengthChanged(@NonNull EditLayout editLayout, int length);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "EditLayout";

	/**
	 * Delay before each change in the editable text to be taken into count. This is necessary for
	 * workaround where text change is some times fired with not actual text value presented within
	 * the EditText.
	 */
	private static final int EDITABLE_CHANGE_DELAY = 10;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Animation listener for hint outgoing animation. This listener hides the hint view within {@link #onAnimationEnd()}.
	 */
	private final Animation.AnimationListener HINT_OUT_ANIM_LISTENER = new Animation.AnimationListener() {

		/**
		 */
		@Override
		public void onAnimationStart(Animation animation) {
			// Ignored.
		}

		/**
		 */
		@Override
		public void onAnimationEnd(Animation animation) {
			if (mLabelView != null) mLabelView.setVisibility(INVISIBLE);
		}

		/**
		 */
		@Override
		public void onAnimationRepeat(Animation animation) {
			// Ignored.
		}
	};

	/**
	 * Text watcher to listen for changes within EditText to properly manage showing/hiding of the
	 * hint view.
	 */
	private final TextWatcher TEXT_WATCHER = new TextWatcher() {

		/**
		 */
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Ignored.
		}

		/**
		 */
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// Ignored.
		}

		/**
		 */
		@Override
		public void afterTextChanged(final Editable s) {
			// Workaround to handle empty text changes even when there should be some text presented
			// within EditText widget. There seams to be a problem when deleting text within EditText
			// which contains some whitespace. In such a case EditText fires this callback with an
			// empty text and right after that with text that is really presented within EditText.
			mEditText.removeCallbacks(EDITABLE_CHANGE);
			mEditText.postDelayed(EDITABLE_CHANGE, EDITABLE_CHANGE_DELAY);
		}
	};

	/**
	 * Runnable task which will fire {@link #onEditableChanged(CharSequence)} method.
	 */
	private final Runnable EDITABLE_CHANGE = new Runnable() {

		/**
		 */
		@Override
		public void run() {
			final CharSequence editable = mEditText.getText();
			onEditableChanged(editable != null ? editable : "");
		}
	};

	/**
	 * Edit text for user input.
	 */
	private EditText mEditText;

	/**
	 * Animation for hint text view.
	 */
	private Animation mLabelInAnimation, mLabelOutAnimation;

	/**
	 * Configuration for EditText of this EditLayout. Data from this config will be set to the EditText
	 * when it is first time created.
	 */
	private EditConfig mEditConfig;

	/**
	 * Constraint for the length of the editable text. Note, that this will not constraint max length
	 * of the EditText, but will be only used to indicate current state of editable text's length vs.
	 * this value within constraint text view.
	 */
	private int mLengthConstraint;

	/**
	 * Length of the current editable input.
	 */
	private int mInputLength;

	/**
	 * Callback to be invoked whenever a focus of input view changes.
	 */
	private OnInputFocusChangeListener mInputFocusChangeListener;

	/**
	 * Callback to be invoked whenever a length of the current input text is changed.
	 */
	private OnInputLengthChangeListener mInputLengthListener;

	/**
	 * Focus change listener for input edit text.
	 */
	private OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener() {

		/**
		 */
		@Override
		public void onFocusChange(View view, boolean hasFocus) {
			handleEditFocusChange(hasFocus);
			if (mInputFocusChangeListener != null) mInputFocusChangeListener.onInputFocusChange(EditLayout.this, hasFocus);
		}
	};

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #EditLayout(android.content.Context, android.util.AttributeSet)} without attributes.
	 */
	public EditLayout(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #EditLayout(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#uiEditLayoutStyle} as attribute for default style.
	 */
	public EditLayout(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.uiEditLayoutStyle);
	}

	/**
	 * Same as {@link #EditLayout(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public EditLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of EditLayout within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public EditLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		/**
		 * Process attributes.
		 */
		this.mEditConfig = new EditConfig();
		boolean createViewHierarchy = true;
		final TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.Ui_EditLayout, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_EditLayout_android_text) {
					mEditConfig.text = typedArray.getText(index);
				} else if (index == R.styleable.Ui_EditLayout_android_hint) {
					mEditConfig.hint = typedArray.getText(index);
				} else if (index == R.styleable.Ui_EditLayout_android_inputType) {
					mEditConfig.inputType = typedArray.getInteger(index, mEditConfig.inputType);
				} else if (index == R.styleable.Ui_EditLayout_android_maxLength) {
					mLengthConstraint = mEditConfig.maxLength = typedArray.getInteger(index, mEditConfig.maxLength);
				} else if (index == R.styleable.Ui_EditLayout_android_lines) {
					mEditConfig.lines = typedArray.getInteger(index, mEditConfig.lines);
				} else if (index == R.styleable.Ui_EditLayout_android_minLines) {
					mEditConfig.minLines = typedArray.getInteger(index, mEditConfig.minLines);
				} else if (index == R.styleable.Ui_EditLayout_android_maxLines) {
					mEditConfig.maxLines = typedArray.getInteger(index, mEditConfig.maxLines);
				} else if (index == R.styleable.Ui_EditLayout_uiLengthConstraint) {
					this.mLengthConstraint = typedArray.getInteger(index, mLengthConstraint);
				} else if (index == R.styleable.Ui_EditLayout_uiFloatingLabelAnimationIn) {
					final int inAnimRes = typedArray.getResourceId(index, -1);
					if (inAnimRes != -1) {
						this.mLabelInAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes);
					}
				} else if (index == R.styleable.Ui_EditLayout_uiFloatingLabelAnimationOut) {
					final int outAnimRes = typedArray.getResourceId(index, -1);
					if (outAnimRes != -1) {
						this.mLabelOutAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes);
						this.handleAnimationsUpdate();
					}
				} else if (index == R.styleable.Ui_EditLayout_uiWithEmptyViewHierarchy) {
					createViewHierarchy = !typedArray.getBoolean(index, false);
				}
			}
			typedArray.recycle();
		}
		if (createViewHierarchy) {
			/**
			 * Create default view hierarchy.
			 */
			addLabelView(new TextViewWidget(context, null, R.attr.uiInputLabelStyle));
			addInputView(new EditTextWidget(context, null, R.attr.uiEditTextInputStyle));
			addNoteView(new TextViewWidget(context, null, R.attr.uiInputNoteStyle));
			addConstraintView(new TextViewWidget(context, null, R.attr.uiInputConstraintStyle));
			this.updateEditTextConfiguration();
		}
	}

	/**
	 * Setups the current EditText with configuration options provided by {@link #mEditConfig} if
	 * available.
	 */
	private void updateEditTextConfiguration() {
		if (mEditConfig == null || mEditText == null) {
			return;
		}

		if (mEditConfig.text != null) {
			mEditText.setText(mEditConfig.text);
			super.setLabel(mEditConfig.text);
		}
		if (mEditConfig.hint != null) {
			mEditText.setHint(mEditConfig.hint);
		}
		if (mEditConfig.inputType != 0) {
			// We try to preserve here the type face that is set to the edit text because calling
			// EditText.setInputType(int) method changes it to MONOSPACE.
			final Typeface typeface = mEditText.getTypeface();
			mEditText.setInputType(mEditConfig.inputType);
			mEditText.setTypeface(typeface);
		}
		if (mEditConfig.maxLength != 0) {
			mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mEditConfig.maxLength)});
		}
		if (mEditConfig.lines != 0) {
			mEditText.setLines(mEditConfig.lines);
		}
		if (mEditConfig.minLines != 0) {
			mEditText.setMinLines(mEditConfig.minLines);
		}
		if (mEditConfig.maxLines != 0) {
			mEditText.setMaxLines(mEditConfig.maxLines);
		}
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(EditLayout.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(EditLayout.class.getName());
	}

	/**
	 * Same as {@link #setText(CharSequence)} for resource id.
	 *
	 * @param resId Resource id of the desired text.
	 */
	public void setText(@StringRes int resId) {
		setText(mResources.getText(resId));
	}

	/**
	 * Delegate method for {@link EditText#setText(CharSequence)}.
	 * @see android.R.attr#text android:text
	 * @see #getText()
	 */
	public void setText(@Nullable CharSequence text) {
		mEditConfig.text = text != null ? text : "";
		if (mEditText != null) mEditText.setText(mEditConfig.text);
	}

	/**
	 * Like {@link #getEditableText()} but this method will return the text specified for this edit
	 * layout via {@link android.R.attr#text android:text} attribute if the current input view of
	 * this layout is not valid.
	 */
	@Nullable
	public CharSequence getText() {
		return mEditText != null ? mEditText.getText() : mEditConfig.text;
	}

	/**
	 * Delegate method for {@link EditText#getText()}.
	 */
	@Nullable
	public Editable getEditableText() {
		return mEditText != null ? mEditText.getText() : null;
	}

	/**
	 * Clears the current text within the EditText.
	 *
	 * @deprecated Use {@link #setText(CharSequence)} with {@code null} value instead.
	 */
	@Deprecated
	public void clearText() {
		mEditConfig.text = "";
		if (mEditText != null) mEditText.setText("");
	}

	/**
	 * Same as {@link #setHint(CharSequence)} for resource id.
	 *
	 * @param resId Resource id of the desired hint text.
	 */
	public void setHint(@StringRes int resId) {
		setHint(mResources.getText(resId));
	}

	/**
	 * Delegate method for {@link EditText#setHint(CharSequence)}.
	 * @see android.R.attr#hint android:hint
	 * @see #getHint()
	 */
	public void setHint(@NonNull CharSequence hintText) {
		setLabel(hintText);
		mEditConfig.hint = hintText;
		if (mEditText != null) mEditText.setHint(hintText);
	}

	/**
	 * Delegate method for {@link EditText#getHint()}.
	 */
	@Nullable
	public CharSequence getHint() {
		return mEditText != null ? mEditText.getHint() : mEditConfig.hint;
	}

	/**
	 * Delegate method for {@link EditText#setInputType(int)}.
	 * @see android.R.attr#inputType android:inputType
	 * @see #getInputType()
	 */
	public void setInputType(int inputType) {
		if (mEditConfig.inputType != inputType) {
			mEditConfig.inputType = inputType;
			if (mEditText != null) mEditText.setInputType(inputType);
		}
	}

	/**
	 * Delegate method for {@link EditText#getInputType()}.
	 */
	public int getInputType() {
		return mEditText != null ? mEditText.getInputType() : mEditConfig.inputType;
	}

	/**
	 * Delegate method for {@link EditText#setLines(int)}.
	 * @see android.R.attr#lines android:lines
	 */
	public void setLines(int lines) {
		if (mEditConfig.lines != lines) {
			mEditConfig.lines = Math.max(0, lines);
			if (mEditText != null) mEditText.setLines(mEditConfig.lines);
		}
	}

	/**
	 * Delegate method for {@link EditText#setMinLines(int)}.
	 * @see android.R.attr#minLines android:minLines
	 * @see #getMinLines()
	 */
	public void setMinLines(int minLines) {
		if (mEditConfig.minLines != minLines) {
			mEditConfig.minLines = Math.max(0, minLines);
			if (mEditText != null) mEditText.setMinLines(mEditConfig.minLines);
		}
	}

	/**
	 * Delegate method for {@link EditText#getMinLines()}.
	 * @see #setMinLines(int)
	 */
	@SuppressLint("NewApi")
	public int getMinLines() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || mEditText == null) {
			return mEditConfig.minLines;
		}
		return mEditText.getMinLines();
	}

	/**
	 * Delegate method for {@link EditText#setMaxLines(int)}.
	 * @see android.R.attr#maxLines android:maxLines
	 * @see #getMaxLines()
	 */
	public void setMaxLines(int maxLines) {
		if (mEditConfig.maxLines != maxLines) {
			mEditConfig.maxLines = Math.max(0, maxLines);
			if (mEditText != null) mEditText.setMaxLines(mEditConfig.maxLines);
		}
	}

	/**
	 * Delegate method for {@link EditText#getMaxLines()}.
	 * @see #setMaxLines(int)
	 */
	@SuppressLint("NewApi")
	public int getMaxLines() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || mEditText == null) {
			return mEditConfig.maxLines;
		}
		return mEditText.getMaxLines();
	}

	/**
	 * Sets the maximum length (in characters) to be used as constraint filter for the current EditText.
	 * Setting this property will constraint a user in typing more characters into EditText than the
	 * specified value.
	 * <p>
	 * <b>Note</b>, that if <b>none zero</b>, this value will also change the current value of length
	 * constraint requested by {@link #setLengthConstraint(int)} used to indicate current state of
	 * the editable text's length vs. the constraint one.
	 *
	 * @param maxLength The desired maximum length. Passing {@code 0} will clear the current filter.
	 * @see android.R.attr#maxLength android:maxLength
	 * @see #getMaxLength()
	 */
	public void setMaxLength(int maxLength) {
		if (mEditConfig.maxLength != maxLength) {
			mEditConfig.maxLength = Math.max(0, maxLength);
			if (mEditConfig.maxLength > 0) {
				mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mEditConfig.maxLength)});
			} else {
				mEditText.setFilters(new InputFilter[0]);
			}
			if (!(mEditText instanceof EditTextWidget) || !UiConfig.MATERIALIZED) {
				this.handleLengthConstraintUpdate(mEditConfig.maxLength = maxLength);
			}
		}
	}

	/**
	 * Returns the maximum length (in characters) applied to the current EditText.
	 *
	 * @return Maximum length or {@code 0} by default.
	 * @see #setMaxLength(int)
	 */
	public int getMaxLength() {
		return mEditConfig.maxLength;
	}

	/**
	 * Sets the value of length constraint used when indicating the current editable text's length
	 * vs. the constraint length to show to a user how many more characters can be inputted into
	 * EditText yet. This state will be shown within the constraint view added by {@link #addConstraintView(TextView)}
	 * at the right bottom part of this layout.
	 * <p>
	 * <b>Note</b>, that unlike {@link #setMaxLength(int)}, setting this property will not constraint
	 * the user in typing more characters into EditText than the specified value. This constraint has
	 * only informational character.
	 *
	 * @param lengthConstraint The desired length constraint. Passing {@code 0} will clear the current
	 *                         constraint text within the constraint view.
	 * @see R.attr#uiLengthConstraint ui:uiLengthConstraint
	 * @see #getLengthConstraint()
	 */
	public void setLengthConstraint(int lengthConstraint) {
		if (mLengthConstraint != lengthConstraint) {
			this.handleLengthConstraintUpdate(Math.max(0, lengthConstraint));
		}
	}

	/**
	 * Returns the current length constraint requested for the EditText of this input layout.
	 *
	 * @return Length constraint or {@code 0} by default.
	 * @see #setLengthConstraint(int)
	 */
	public int getLengthConstraint() {
		return mLengthConstraint;
	}

	/**
	 */
	@NonNull
	@Override
	public EditText getInputView() {
		return mEditText;
	}

	/**
	 * Registers a callback to be invoked whenever a focus of input view changes.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 * @see OnFocusChangeListener
	 */
	public void setOnInputFocusChangeListener(@Nullable OnInputFocusChangeListener listener) {
		this.mInputFocusChangeListener = listener;
	}

	/**
	 * Registers a callback to be invoked whenever a length of the current editable text is changed.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 * @see TextWatcher
	 */
	public void setOnInputLengthChangeListener(@Nullable OnInputLengthChangeListener listener) {
		this.mInputLengthListener = listener;
	}

	/**
	 */
	@Override
	protected boolean validateInputView(@NonNull View view) {
		return view instanceof EditText;
	}

	/**
	 */
	@Override
	protected void onInputViewChanged(@NonNull View inputView) {
		super.onInputViewChanged(inputView);
		if (!(inputView instanceof EditText)) {
			throw new IllegalArgumentException("Only EditText is allowed as input view for EditLayout. Found(" + inputView + ") instead.");
		}
		this.mEditText = (EditText) inputView;
		mEditText.addTextChangedListener(TEXT_WATCHER);
		mEditText.setOnFocusChangeListener(mFocusChangeListener);
		this.updateEditTextConfiguration();
		this.updateLabelVisibility(mEditText.getHint());
	}

	/**
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (mInputView != null && mInputView != mEditText) {
			onInputViewChanged(mInputView);
		}
	}

	/**
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.updateLabelVisibility(mEditText.getHint());
		this.handleLengthConstraintUpdate(mLengthConstraint);
	}

	/**
	 * Invoked whenever a change in the text of EditText occurs.
	 * <p>
	 * This implementation will handle this change based on the enabled features ({@link #FEATURE_LABEL},
	 * {@link #FEATURE_CONSTRAINT}, ...) by showing/hiding the label view and updating current text
	 * length vs. constraint length.
	 *
	 * @param editable Current editable value presented within EditText.
	 */
	protected void onEditableChanged(@NonNull CharSequence editable) {
		final int editableLength = editable.length();
		if (mInputLength == editableLength) {
			return;
		}

		if ((mFeatures & FEATURE_CONSTRAINT) != 0) {
			this.updateConstraintState(editable);
		} else if ((mPrivateFlags & PFLAG_ERROR_HIGHLIGHT_VISIBLE) != 0) {
			setErrorHighlightVisible(false);
		}
		this.notifyTextLengthChange(mInputLength = editableLength);

		if (TextUtils.isEmpty(mEditConfig.hint) || (mFeatures & FEATURE_LABEL) == 0) {
			return;
		}

		final boolean empty = TextUtils.isEmpty(editable.toString());
		if (empty && mLabelView.getVisibility() == View.VISIBLE) {
			if (!onAnimateLabel(mLabelView, false)) {
				mLabelView.setVisibility(View.INVISIBLE);
			}
		} else if (!empty && mLabelView.getVisibility() != View.VISIBLE) {
			mLabelView.setText(mEditConfig.hint);
			if (!onAnimateLabel(mLabelView, true)) {
				mLabelView.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Notifies the current OnTextLengthChangeListener that the length of the current text within the
	 * EditText has changed.
	 *
	 * @param textLength The length of the current text.
	 */
	private void notifyTextLengthChange(int textLength) {
		if (mInputLengthListener != null)
			mInputLengthListener.onInputLengthChanged(this, textLength);
	}

	/**
	 */
	@Override
	protected void onInputFeaturesChange(@InputFeature int features) {
		super.onInputFeaturesChange(features);
		this.updateLabelVisibility(mEditText.getHint());
	}

	/**
	 * Invoked whenever a change in the text within EditText occurs and hint view should be showed or
	 * hided by animation.
	 * <p>
	 * This implementation uses animations provided by {@link R.attr#uiFloatingLabelAnimationIn}
	 * and {@link R.attr#uiFloatingLabelAnimationOut} obtained from the style set to this view
	 * or from Xml layout file.
	 *
	 * @param labelView Label text view to be animated. If showing, this text view already contains
	 *                  the current hint from EditText.
	 * @param show      {@code True} if label view should be showed, {@code false} to be hided.
	 * @return {@code True} if label view was showed/hided by animation, {@code false} if it should
	 * be showed/hided just by changing its visibility flag.
	 */
	protected boolean onAnimateLabel(@NonNull TextView labelView, boolean show) {
		if (show && mLabelInAnimation != null) {
			labelView.setVisibility(View.VISIBLE);
			labelView.startAnimation(mLabelInAnimation);
			return true;
		}
		if (!show && mLabelOutAnimation != null) {
			labelView.startAnimation(mLabelOutAnimation);
			return true;
		}
		return false;
	}

	/**
	 * Handles change in the focus of the edit text.
	 *
	 * @param focused {@code True} if the EditText is focused, {@code false} otherwise.
	 */
	void handleEditFocusChange(boolean focused) {
		onEditFocusChange(focused);
	}

	/**
	 * Invoked whenever focus of the edit text contained within this edit layout has changed.
	 * <p>
	 * By default this edit layout activates/deactivates here its label view.
	 */
	protected void onEditFocusChange(boolean focused) {
		if (mLabelView != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mLabelView.setActivated(focused);
			} else {
				mLabelView.setSelected(focused);
			}
		}
	}

	/**
	 * Handles update in the length constraint parameter.
	 *
	 * @param constraint The current length constraint for the EditText of this edit layout.
	 */
	private void handleLengthConstraintUpdate(int constraint) {
		this.mLengthConstraint = constraint;
		if ((mFeatures & FEATURE_CONSTRAINT) == 0 || mConstraintView == null) {
			return;
		}

		if (mLengthConstraint > 0) {
			this.updateConstraintState(mEditText.getText());
			mConstraintView.setVisibility(View.VISIBLE);
		} else {
			mConstraintView.setVisibility(View.INVISIBLE);
			mConstraintView.setText("");
		}
	}

	/**
	 * Updates the current state of the constraint view depends on the given <var>inputText</var>.
	 * This call will update the actual count of characters vs. length constraint text within the
	 * constraint view. If length of the given input text exceeds the current length constraint, the
	 * error will be set to this input layout with empty text otherwise the current error (if any)
	 * will be cleared.
	 * <p>
	 * If there is no length constraint specified, this method does nothing.
	 *
	 * @param inputText The current input text presented within the EditText.
	 */
	private void updateConstraintState(CharSequence inputText) {
		if (mLengthConstraint <= 0) {
			return;
		}
		final int textLength = TextUtils.isEmpty(inputText) ? 0 : inputText.length();
		setConstraint(Integer.toString(textLength) + "/" + Integer.toString(mLengthConstraint));
		if (textLength > mLengthConstraint && (mPrivateFlags & PFLAG_ERROR_HIGHLIGHT_VISIBLE) == 0) {
			setErrorHighlightVisible(true);
		} else if (textLength <= mLengthConstraint && (mPrivateFlags & PFLAG_ERROR_HIGHLIGHT_VISIBLE) != 0) {
			setErrorHighlightVisible(false);
		}
	}

	/**
	 * Updates a visibility of the label text view depends on the given <var>hint</var> value.
	 *
	 * @param hint The current value of hint.
	 */
	private void updateLabelVisibility(CharSequence hint) {
		if (mLabelView == null) {
			return;
		}
		int visibility = View.INVISIBLE;
		if (TextUtils.isEmpty(hint) || (mFeatures & FEATURE_LABEL) == 0) {
			visibility = View.GONE;
		} else if (!TextUtils.isEmpty(mEditText.getText())) {
			visibility = View.VISIBLE;
			setLabel(hint);
		}
		mLabelView.setVisibility(visibility);
	}

	/**
	 * Updates animation listeners for currently changed animations.
	 */
	private void handleAnimationsUpdate() {
		if (mLabelOutAnimation != null) mLabelOutAnimation.setAnimationListener(HINT_OUT_ANIM_LISTENER);
	}

	/**
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.text = mEditText.getText();
		savedState.hint = mEditConfig.hint;
		savedState.inputType = mEditConfig.inputType;
		savedState.lines = mEditConfig.lines;
		savedState.minLines = mEditConfig.minLines;
		savedState.maxLines = mEditConfig.maxLines;
		savedState.maxLength = mEditConfig.maxLength;
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
		mEditConfig.text = savedState.text;
		mEditConfig.hint = savedState.hint;
		mEditConfig.inputType = savedState.inputType;
		mEditConfig.lines = savedState.lines;
		mEditConfig.minLines = savedState.minLines;
		mEditConfig.maxLines = savedState.maxLines;
		mEditConfig.maxLength = savedState.maxLength;
		this.updateEditTextConfiguration();
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link EditLayout}
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
		CharSequence hint, text;

		/**
		 */
		int inputType, maxLength, lines, minLines, maxLines;

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
			this.text = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
			this.hint = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
			this.inputType = source.readInt();
			this.maxLength = source.readInt();
			this.lines = source.readInt();
			this.minLines = source.readInt();
			this.maxLines = source.readInt();
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			TextUtils.writeToParcel(text, dest, flags);
			TextUtils.writeToParcel(hint, dest, flags);
			dest.writeInt(inputType);
			dest.writeInt(maxLength);
			dest.writeInt(lines);
			dest.writeInt(minLines);
			dest.writeInt(maxLines);
		}
	}

	/**
	 * This class holds base options to configure EditText of this input layout.
	 */
	private static final class EditConfig {

		/**
		 * Hint text for EditText.
		 */
		CharSequence hint;

		/**
		 * Text for EditText.
		 */
		CharSequence text;

		/**
		 * Input type flags for EditText.
		 */
		int inputType;

		/**
		 * Max length for EditText determining maximum allowed count of characters.
		 */
		int maxLength;

		/**
		 * Number of lines for EditText.
		 */
		int lines;

		/**
		 * Number of minimum lines for EditText.
		 */
		int minLines;

		/**
		 * Number of maximum lines for EditText.
		 */
		int maxLines;
	}
}
