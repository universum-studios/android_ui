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
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import universum.studios.android.ui.R;

/**
 * Implementation of {@link InputLayout} to provide input container for {@link Spinner} component.
 * All features supported by {@link InputLayout} are also supported by this layout. By default,
 * SpinnerLayout creates full view hierarchy on its initialization with <b>label</b>, <b>note</b>,
 * <b>spinner</b> and <b>constraint</b> views that will be laid out as described in {@link InputLayout}
 * class overview. Whether to show or hide some of these views can be requested by specifying desired
 * input features via {@link #requestInputFeatures(int)}.
 *
 * <h3>Spinner set up</h3>
 * Spinner of SpinnerLayout can be accessed via {@link #getInputView()} method. SpinnerLayout implements
 * only necessary delegate methods to perform base set up of its input view like setting its adapter
 * via {@link #setAdapter(android.widget.SpinnerAdapter)} or obtaining its current selected position
 * via {@link #getSelection()}.
 *
 * <h3>Xml layout</h3>
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
 *      &lt;universum.studios.android.ui.widget.SpinnerLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"/&gt;
 *
 *      --------------------------------------------------------------------------------------------
 *
 *      &lt;!-- Layout with custom Spinner. --&gt;
 *      &lt;universum.studios.android.ui.widget.SpinnerLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"&gt;
 *
 *              &lt;Spinner
 *                      android:layout_width="match_parent"
 *                      android:layout_height="wrap_content"
 *                      ui:uiInputChildType="input"/&gt;
 *
 *      &lt;universum.studios.android.ui.widget.SpinnerLayout&gt;
 *
 *      --------------------------------------------------------------------------------------------
 *
 *      &lt;!-- Layout with all custom views. --&gt;
 *      &lt;universum.studios.android.ui.widget.SpinnerLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"
 *              ui:uiWithEmptyViewHierarchy="true"&gt;
 *              &lt;!--
 *                  uiWithEmptyViewHierarchy="true"
 *                  This flag ensures that the SpinnerLayout will be initialized with the empty view
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
 *              &lt;Spinner
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
 *      &lt;universum.studios.android.ui.widget.SpinnerLayout&gt;
 * &lt;LinearLayout&gt;
 * </pre>
 *
 * <h3>XML attributes</h3>
 * See {@link InputLayout},
 * {@link R.styleable#Ui_SpinnerLayout SpinnerLayout Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiSpinnerLayoutStyle uiSpinnerLayoutStyle}
 *
 * @author Martin Albedinsky
 * @see EditLayout
 */
public class SpinnerLayout extends InputLayout {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Listener used to delegate callbacks from {@link android.widget.AdapterView.OnItemSelectedListener} attached
	 * to a spinner of SpinnerLayout.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnItemSelectedListener {

		/**
		 * Invoked whenever an item at the specified <var>position</var> is selected within a spinner
		 * of the specified <var>spinnerLayout</var>.
		 * <p>
		 * See {@link android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)}
		 * for additional info.
		 *
		 * @param spinnerLayout The spinner layout within which spinner has been item selected.
		 */
		void onItemSelected(@NonNull SpinnerLayout spinnerLayout, AdapterView<?> parent, View view, int position, long id);

		/**
		 * Invoked whenever nothing is selected within a spinner of the specified <var>spinnerLayout</var>.
		 * <p>
		 * See {@link android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)}
		 * for additional info.
		 *
		 * @param spinnerLayout The spinner layout within which spinner has been nothing selected.
		 */
		void onNothingSelected(@NonNull SpinnerLayout spinnerLayout, AdapterView<?> parent);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "SpinnerLayout";

	/**
	 * Flag indicating whether the OnItemSelectedListener has been already attached to the current
	 * Spinner or not.
	 */
	private static final int PFLAG_ITEM_SELECTED_LISTENER_ATTACHED = 0x00000001;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * An {@link android.widget.AdapterView.OnItemSelectedListener} implementation to delegate selection callbacks
	 * to the current {@link #mItemSelectedListener} if any.
	 */
	private final AdapterView.OnItemSelectedListener ITEM_SELECTED_LISTENER = new AdapterView.OnItemSelectedListener() {

		/**
		 */
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (mItemSelectedListener != null) {
				mItemSelectedListener.onItemSelected(SpinnerLayout.this, parent, view, position, id);
			}
		}

		/**
		 */
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			if (mItemSelectedListener != null) {
				mItemSelectedListener.onNothingSelected(SpinnerLayout.this, parent);
			}
		}
	};

	/**
	 * Spinner for user input.
	 */
	private Spinner mSpinner;

	/**
	 * Adapter with data set for the spinner.
	 */
	private SpinnerAdapter mAdapter;

	/**
	 * Selection for the spinner.
	 */
	private int mSelection;

	/**
	 * Prompt text for the spinner's dialog.
	 */
	private CharSequence mPrompt;

	/**
	 * Set of private flags of this dialog view.
	 */
	private int mPrivateFlags;

	/**
	 * Callback to be invoked whenever an item within the current spinner is selected.
	 */
	OnItemSelectedListener mItemSelectedListener;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #SpinnerLayout(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public SpinnerLayout(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #SpinnerLayout(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#uiSpinnerLayoutStyle} as attribute for default style.
	 */
	public SpinnerLayout(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.uiSpinnerLayoutStyle);
	}

	/**
	 * Same as {@link #SpinnerLayout(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public SpinnerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of SpinnerLayout within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SpinnerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		boolean createViewHierarchy = true;
		final TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.Ui_SpinnerLayout, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_SpinnerLayout_uiWithEmptyViewHierarchy) {
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
			addInputView(new SpinnerWidget(context, null, R.attr.uiSpinnerInputStyle));
			addNoteView(new TextViewWidget(context, null, R.attr.uiInputNoteStyle));
			addConstraintView(new TextViewWidget(context, null, R.attr.uiInputConstraintStyle));
		}
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#setAdapter(android.widget.SpinnerAdapter)}.
	 */
	public void setAdapter(@Nullable SpinnerAdapter adapter) {
		this.mAdapter = adapter;
		if (mSpinner != null) {
			mSpinner.setAdapter(mAdapter);
			if ((mPrivateFlags & PFLAG_ITEM_SELECTED_LISTENER_ATTACHED) == 0) {
				mSpinner.setSelection(mSelection, false);
				mSpinner.setOnItemSelectedListener(ITEM_SELECTED_LISTENER);
				this.updatePrivateFlags(PFLAG_ITEM_SELECTED_LISTENER_ATTACHED, true);
			}
		}
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#getAdapter()}.
	 */
	@Nullable
	public SpinnerAdapter getAdapter() {
		return mSpinner != null ? mAdapter = mSpinner.getAdapter() : mAdapter;
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#setSelection(int)}.
	 */
	public void setSelection(int selection) {
		if (mSelection != selection) {
			this.mSelection = selection;
			if (mSpinner != null) mSpinner.setSelection(mSelection);
		}
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#setSelection(int, boolean)}.
	 */
	public void setSelection(int selection, boolean animate) {
		if (mSelection != selection) {
			this.mSelection = selection;
			if (mSpinner != null) mSpinner.setSelection(mSelection, animate);
		}
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#getSelectedItemPosition}.
	 */
	public int getSelection() {
		return mSpinner != null ? mSelection = mSpinner.getSelectedItemPosition() : mSelection;
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#getItemAtPosition(int)}.
	 */
	@Nullable
	public Object getItemAtPosition(int position) {
		return mSpinner != null ? mSpinner.getItemAtPosition(position) : null;
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#getItemIdAtPosition(int)}.
	 */
	public long getItemIdAtPosition(int position) {
		return mSpinner != null ? mSpinner.getItemIdAtPosition(position) : -1;
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#setPrompt(CharSequence)}.
	 */
	public void setPrompt(@Nullable CharSequence prompt) {
		this.mPrompt = prompt;
		if (mSpinner != null) mSpinner.setPrompt(mPrompt);
	}

	/**
	 * Delegate method for {@link android.widget.Spinner#getPrompt()}.
	 */
	@Nullable
	public CharSequence getPrompt() {
		return mSpinner != null ? mPrompt = mSpinner.getPrompt() : mPrompt;
	}

	/**
	 * Registers a callback to be invoked whenever an item within the current spinner is selected.
	 *
	 * @param listener Listener callback.
	 */
	public void setOnItemSelectedListener(@NonNull OnItemSelectedListener listener) {
		this.mItemSelectedListener = listener;
	}

	/**
	 * Removes the current OnItemSelectedListener callback if any.
	 */
	public void removeOnItemSelectedListener() {
		this.mItemSelectedListener = null;
	}

	/**
	 */
	@NonNull
	@Override
	public Spinner getInputView() {
		return mSpinner;
	}

	/**
	 */
	@Override
	protected boolean validateInputView(@NonNull View view) {
		return view instanceof Spinner;
	}

	/**
	 */
	@Override
	protected void onInputViewChanged(@NonNull View inputView) {
		super.onInputViewChanged(inputView);
		if (!(inputView instanceof Spinner)) {
			throw new IllegalArgumentException("Only Spinner is allowed as input view for SpinnerLayout. Found(" + inputView + ") instead.");
		}
		this.mSpinner = (Spinner) inputView;
		if (mPrompt != null) {
			mSpinner.setPrompt(mPrompt);
		}
		if (mAdapter != null) {
			mSpinner.setAdapter(mAdapter);
			mSpinner.setSelection(mSelection, false);
			mSpinner.setOnItemSelectedListener(ITEM_SELECTED_LISTENER);
			this.updatePrivateFlags(PFLAG_ITEM_SELECTED_LISTENER_ATTACHED, true);
		}
	}

	/**
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		final View inputView = super.getInputView();
		if (inputView != mSpinner) {
			onInputViewChanged(inputView);
		}
	}

	/**
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.selection = mSpinner.getSelectedItemPosition();
		savedState.prompt = mPrompt;
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
		setPrompt(savedState.prompt);
		setSelection(savedState.selection, false);
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
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link SpinnerLayout}
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
		int selection;

		/**
		 */
		CharSequence prompt;

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
			this.prompt = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(selection);
			TextUtils.writeToParcel(prompt, dest, flags);
		}
	}
}
