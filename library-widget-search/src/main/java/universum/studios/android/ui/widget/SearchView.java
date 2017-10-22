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
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * <b>Note, that this implementation of SearchView is for now really 'shallow one', but will be
 * improved in the feature.</b>
 * <p>
 * SearchView is a widget that presents to a user single input field with search icon, clear button
 * and voice search button where user can enter its desired search query.
 *
 * @author Martin Albedinsky
 */
public class SearchView extends LinearLayoutWidget {

	/*
	 * Interface ===================================================================================
	 */

	/**
	 * Listener that can be used to receive callbacks about <b>changed</b>, <b>confirmed</b> or
	 * <b>cleared</b> search query text within associated {@link SearchView}.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnQueryTextListener {

		/**
		 * Invoked whenever the current query text of the specified search view is changed.
		 *
		 * @param searchView The search view where the query text has been changed.
		 * @param queryText  The changed query text.
		 */
		void onQueryTextChanged(@NonNull SearchView searchView, @NonNull CharSequence queryText);

		/**
		 * Invoked whenever the current query text of the specified search view is confirmed by a user.
		 *
		 * @param searchView The search view where the query text has been confirmed.
		 * @param queryText  The confirmed query text.
		 */
		void onQueryTextConfirmed(@NonNull SearchView searchView, @NonNull CharSequence queryText);

		/**
		 * Invoked whenever the current query text of the specified search view is cleared.
		 *
		 * @param searchView The search view where the query text has been cleared.
		 */
		void onQueryTextCleared(@NonNull SearchView searchView);
	}

	/**
	 * Listener that can be used to receive callback about clicked <b>icon</b> within associated
	 * {@link SearchView}.
	 *
	 * @author Martin Albedinsky
	 */
	public interface OnIconClickListener {

		/**
		 * Invoked whenever the icon of the specified search view is clicked.
		 *
		 * @param searchView The search view where the icon has been clicked.
		 * @return {@code True} to indicate to the search view that this action has been processed
		 * by the listener, {@code false} to perform default action by the search view.
		 */
		boolean onSearchIconClick(@NonNull SearchView searchView);
	}

	/**
	 * Handler that is used to run <b>reveal</b> or <b>conceal</b> animations for {@link SearchView}
	 * widget.
	 *
	 * @author Martin Albedinsky
	 * @see #reveal()
	 * @see #conceal()
	 */
	public interface RevealAnimationHandler {

		/**
		 * Called whenever {@link SearchView#reveal()} is called for the given <var>searchView</var>
		 * and the search view is not revealed yet.
		 *
		 * @param searchView The search view widget for which to start <b>reveal</b> animation.
		 */
		void startRevealAnimation(@NonNull SearchView searchView);

		/**
		 * Called whenever {@link SearchView#conceal()} is called for the given <var>searchView</var>
		 * and the search view is currently revealed.
		 *
		 * @param searchView The search view widget for which to start <b>conceal</b> animation.
		 */
		void startConcealAnimation(@NonNull SearchView searchView);
	}

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "SearchView";

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Global listener used to listen for click events for all clickable views of SearchView widget.
	 */
	private final View.OnClickListener CLICK_LISTENER = new View.OnClickListener() {

		/**
		 */
		@Override
		public void onClick(View view) {
			final int id = view.getId();
			if (id == R.id.ui_search_icon) {
				onIconButtonClick();
			} else if (id == R.id.ui_search_mic) {
				onVoiceButtonClick();
			} else if (id == R.id.ui_search_clear) {
				onClearButtonClick();
			}
		}
	};

	/**
	 * Context in which is this view presented.
	 */
	private Context mContext;

	/**
	 * Search input view.
	 */
	private AutoCompleteTextViewWidget mEditText;

	/**
	 * Button presenting the icon of search view.
	 * Whenever clicked, {@link #onIconButtonClick()} is invoked.
	 */
	ImageButtonWidget mButtonIcon;

	/**
	 * Circular progress bar shown when {@link #setSearching(boolean)} is called with {@code true} at
	 * the position of {@link #mButtonIcon}.
	 */
	CircularProgressBar mProgressBar;

	/**
	 * Simple animator that is used to animate showing and hiding of progress bar whenever {@link #setSearching(boolean)}
	 * method is called.
	 */
	private ProgressBarAnimator mProgressBarAnimator;

	/**
	 * Button presenting the clear action of search view.
	 * Whenever clicked, {@link #onClearButtonClick()} is invoked.
	 */
	private ImageButtonWidget mButtonClear;

	/**
	 * Button presenting the voice search action of search view.
	 * Whenever clicked, {@link #onVoiceButtonClick()} is invoked.
	 */
	private ImageButtonWidget mButtonVoiceSearch;

	/**
	 * Callbacks to be invoked whenever query text of this search view is changed, confirmed or cleared.
	 */
	private OnQueryTextListener mQueryTextListener;

	/**
	 * Callback to be invoked whenever icon of this search view is clicked.
	 */
	private OnIconClickListener mIconClickListener;

	/**
	 * Current search query text presented in the search input field.
	 */
	private CharSequence mQueryText = "";

	/**
	 * Implementation of {@link RevealAnimationHandler} used as default animation handler.
	 */
	private BaseRevealAnimationHandler mDefaultRevealAnimationHandler;

	/**
	 * Handler used to handle (start/stop) either reveal or conceal animations for this search view.
	 */
	private RevealAnimationHandler mRevealAnimationHandler;

	/**
	 * Info used when tinting components of this widget.
	 */
	private SearchTintInfo mTintInfo;

	/**
	 * Boolean flag indicating whether this widget
	 */
	private boolean mEatingTouch;

	/**
	 * Boolean flag indicating whether search is running or not. If {@code true}
	 */
	private boolean mSearching;

	/**
	 * Boolean flag indicating whether there is pending clear text event or not. This will be
	 * {@code true} whenever {@link #clearQuery()} is called before it is set to {@code false} in
	 * {@link #onQueryTextChange(CharSequence)}.
	 */
	private boolean mPendingQueryClear;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #SearchView(android.content.Context, android.util.AttributeSet)} without attributes.
	 */
	public SearchView(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #SearchView(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#uiSearchViewStyle} as attribute for default style.
	 */
	public SearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiSearchViewStyle);
	}

	/**
	 * Same as {@link #SearchView(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of SearchView for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context, attrs, defStyleAttr, defStyleRes);
	}

	/*
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
	@SuppressWarnings("ResourceType")
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		this.mContext = context;
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);
		this.inflateHierarchy(context, R.layout.ui_search_view);
		this.mProgressBarAnimator = createProgressBarAnimator();
		this.mDefaultRevealAnimationHandler = createRevealAnimationHandler();
		ColorStateList tintList = null;
		PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;

		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_SearchView, defStyleAttr, defStyleRes);
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_SearchView_android_queryHint) {
				setQueryHint(attributes.getText(index));
			} else if (index == R.styleable.Ui_SearchView_uiTint) {
				tintList = attributes.getColorStateList(index);
			} else if (index == R.styleable.Ui_SearchView_uiTintMode) {
				tintMode = TintManager.parseTintMode(
						attributes.getInt(R.styleable.Ui_SearchView_uiTintMode, 0),
						tintList != null ? PorterDuff.Mode.SRC_IN : null
				);
			} else if (index == R.styleable.Ui_SearchView_uiRevealDuration) {
				mDefaultRevealAnimationHandler.revealDuration = attributes.getInt(index, 0);
			} else if (index == R.styleable.Ui_SearchView_uiConcealDuration) {
				mDefaultRevealAnimationHandler.concealDuration = attributes.getInt(index, 0);
			} else if (index == R.styleable.Ui_SearchView_uiRevealInterpolator) {
				final int resId = attributes.getResourceId(index, 0);
				if (resId != 0) {
					mDefaultRevealAnimationHandler.interpolator = AnimationUtils.loadInterpolator(context, resId);
				}
			}
		}
		attributes.recycle();

		if (tintList != null || tintMode != null) {
			this.mTintInfo = new SearchTintInfo();
			this.mTintInfo.tintList = tintList;
			this.mTintInfo.hasTintList = tintList != null;
			this.mTintInfo.tintMode = tintMode;
			this.mTintInfo.hasTintMode = tintMode != null;
		}
		final Resources.Theme theme = context.getTheme();
		final TypedValue typedValue = new TypedValue();
		if (theme.resolveAttribute(R.attr.uiSearchSelectHandleColor, typedValue, true)) {
			if (mTintInfo == null) {
				this.mTintInfo = new SearchTintInfo();
			}
			if (typedValue.resourceId > 0) {
				mTintInfo.textSelectHandleTintList = ResourceUtils.getColorStateList(
						getResources(),
						typedValue.resourceId,
						theme
				);
			} else {
				mTintInfo.textSelectHandleTintList = ColorStateList.valueOf(typedValue.data);
			}
			mTintInfo.hasTextSelectHandleTintList = mTintInfo.textSelectHandleTintList != null;
		}
		this.applyTint();
		this.mRevealAnimationHandler = mDefaultRevealAnimationHandler;
	}

	/**
	 * Called to inflate a view hierarchy of this view.
	 *
	 * @param context  Context used to obtain an instance of LayoutInflater used to inflate a desired
	 *                 layout resource as view hierarchy for this view.
	 * @param resource Resource id of the layout which should represent a view hierarchy of this view.
	 */
	private void inflateHierarchy(Context context, int resource) {
		LayoutInflater.from(context).inflate(resource, this);
		this.mEditText = (AutoCompleteTextViewWidget) findViewById(R.id.ui_search_input);
		this.mButtonIcon = (ImageButtonWidget) findViewById(R.id.ui_search_icon);
		this.mProgressBar = (CircularProgressBar) findViewById(R.id.ui_search_progress);
		this.mButtonClear = (ImageButtonWidget) findViewById(R.id.ui_search_clear);
		this.mButtonVoiceSearch = (ImageButtonWidget) findViewById(R.id.ui_search_mic);
		// fixme: remove this when voice search is implemented
		mButtonVoiceSearch.setVisibility(View.GONE);
		mEditText.addTextChangedListener(new TextWatcher() {
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
			public void afterTextChanged(Editable s) {
				onQueryTextChange(s != null && s.length() > 0 ? new SpannableStringBuilder(s) : "");
			}
		});
		mEditText.setOnKeyListener(new View.OnKeyListener() {
			/**
			 */
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_ENTER:
						if (event.getAction() == KeyEvent.ACTION_UP) {
							onTextConfirmed();
						}
						return true;
				}
				return false;
			}
		});
		mButtonClear.setOnClickListener(CLICK_LISTENER);
		mButtonVoiceSearch.setOnClickListener(CLICK_LISTENER);
	}

	/**
	 * Creates a new instance of ProgressBarAnimator according to the animation capabilities of the
	 * current Android API level.
	 *
	 * @return New instance of ProgressBarAnimator implementation.
	 */
	private static ProgressBarAnimator createProgressBarAnimator() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new IceCreamSandwichProgressBarAnimator();
		}
		return new DefaultProgressBarAnimator();
	}

	/**
	 * Creates an instance of RevealAnimationHandler according to the animation capabilities of the
	 * current Android API level.
	 *
	 * @return New instance of RevealAnimationHandler implementation.
	 */
	private static BaseRevealAnimationHandler createRevealAnimationHandler() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return new LollipopRevealAnimationHandler();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new IceCreamSandwichRevealAnimationHandler();
		}
		return new DefaultRevealAnimationHandler();
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to all drawable components of this widget.
	 */
	@SuppressWarnings("ConstantConditions")
	private void applyTint() {
		if (mTintInfo == null || (!mTintInfo.hasTintList && !mTintInfo.hasTintMode)) {
			return;
		}
		this.applyTintToButton(mButtonIcon);
		this.applyTintToButton(mButtonVoiceSearch);
		this.applyTintToButton(mButtonClear);
		if (!UiConfig.MATERIALIZED) {
			// Tint also left/right and middle search selection handles. These handles can not be
			// unfortunately set programmatically so we will tint directly drawables within resources.
			final Resources resources = getResources();
			final Resources.Theme theme = mContext.getTheme();
			final int tintColor = mTintInfo.textSelectHandleTintList != null ?
					mTintInfo.textSelectHandleTintList.getDefaultColor() :
					Color.TRANSPARENT;
			TintManager.tintRawDrawable(
					ResourceUtils.getDrawable(
							resources,
							R.drawable.ui_search_text_select_handle_left_alpha,
							theme
					),
					tintColor,
					PorterDuff.Mode.SRC_IN
			);
			TintManager.tintRawDrawable(
					ResourceUtils.getDrawable(
							resources,
							R.drawable.ui_search_text_select_handle_middle_alpha,
							theme
					),
					tintColor,
					PorterDuff.Mode.SRC_IN
			);
			TintManager.tintRawDrawable(
					ResourceUtils.getDrawable(
							resources,
							R.drawable.ui_search_text_select_handle_right_alpha,
							theme
					),
					tintColor,
					PorterDuff.Mode.SRC_IN
			);
		}
	}

	/**
	 * Applies current tint from {@link #mTintInfo} to the specified image <var>button</var> widget.
	 *
	 * @param button The button widget to which to apply the tint (if any).
	 */
	private void applyTintToButton(ImageButtonWidget button) {
		if (mTintInfo.hasTintList) button.setImageTintList(mTintInfo.tintList);
		if (mTintInfo.hasTintMode) button.setImageTintMode(mTintInfo.tintMode);
	}

	/**
	 * Invoked whenever the button with search icon is clicked.
	 */
	void onIconButtonClick() {
		if (mIconClickListener != null && mIconClickListener.onSearchIconClick(this)) return;
		// todo: perform default action
	}

	/**
	 * Invoked whenever the current search query text is changed.
	 *
	 * @param text The changed query text.
	 */
	final void onQueryTextChange(CharSequence text) {
		if (text.length() == 0) {
			this.changeViewVisibility(mButtonClear, false);
			// todo: this.changeViewVisibility(mButtonVoiceSearch, true);
		} else if (mQueryText.length() == 0) {
			this.changeViewVisibility(mButtonClear, true);
			// todo: this.changeViewVisibility(mButtonVoiceSearch, false);
		}
		if (mQueryTextListener != null && !mPendingQueryClear) {
			mQueryTextListener.onQueryTextChanged(this, text);
		}
		this.mQueryText = text;
		this.mPendingQueryClear = false;
	}

	/**
	 * Changes visibility of the given <var>view</var> to {@link #VISIBLE} or {@link #GONE} based on
	 * the specified <var>visible</var> boolean flag.
	 *
	 * @param view    The view of which visibility to change.
	 * @param visible {@code True} to make the view visible, {@code false} to make it gone.
	 */
	private void changeViewVisibility(final View view, boolean visible) {
		view.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	/**
	 * Invoked whenever the search action is clicked on soft-keyboard by a user.
	 */
	void onTextConfirmed() {
		if (mQueryTextListener != null) {
			mQueryTextListener.onQueryTextConfirmed(this, mQueryText);
		}
	}

	/**
	 * Invoked whenever the button with voice search icon is clicked.
	 */
	void onVoiceButtonClick() {
		// todo: launch voice search intent
	}

	/**
	 * Invoked whenever the button with clear icon is clicked.
	 */
	void onClearButtonClick() {
		clearQuery();
	}

	/**
	 * Registers a callback to be invoked whenever an icon of this search view is clicked.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 * @see #setOnQueryTextListener(OnQueryTextListener)
	 */
	public void setOnIconClickListener(@Nullable OnIconClickListener listener) {
		this.mIconClickListener = listener;
		if (mIconClickListener != null) {
			mButtonIcon.setOnClickListener(CLICK_LISTENER);
		}
	}

	/**
	 * Registers a callback to be invoked whenever the query text of this search view is <b>changed</b>,
	 * <b>confirmed</b> or <b>cleared</b>.
	 *
	 * @param listener Listener callback. May be {@code null} to clear the current one.
	 * @see #setOnIconClickListener(OnIconClickListener)
	 */
	public void setOnQueryTextListener(@Nullable OnQueryTextListener listener) {
		this.mQueryTextListener = listener;
	}

	/**
	 * Sets an animation handler used when running <b>reveal</b> or <b>conceal</b> animations for
	 * this search view.
	 * <p>
	 * This handler is used whenever {@link #reveal()} or {@link #conceal()} is called.
	 * <p>
	 * Default animation handler is implemented according to the current animation capabilities of
	 * the current Android API level.
	 *
	 * @param animationHandler The desired handler that will handle starting of reveal and conceal
	 *                         animations for this search view. May be {@code null} to use default
	 *                         handler instead.
	 */
	public void setRevealAnimationHandler(@Nullable RevealAnimationHandler animationHandler) {
		this.mRevealAnimationHandler = animationHandler != null ? animationHandler : mDefaultRevealAnimationHandler;
	}

	/**
	 * Reveals this search view via {@link RevealAnimationHandler#startRevealAnimation(SearchView) RevealAnimationHandler.startRevealAnimation(SearchView)}
	 * using either default handler or handler specified via {@link #setRevealAnimationHandler(RevealAnimationHandler)}.
	 * <p>
	 * This method does nothing if this search view is already revealed.
	 *
	 * @see #isRevealed()
	 * @see #conceal()
	 */
	public void reveal() {
		if (isRevealed()) return;
		mRevealAnimationHandler.startRevealAnimation(this);
		WidgetUtils.showSoftKeyboard(mEditText);
	}

	/**
	 * Checks whether this search view is revealed (visible) or not.
	 *
	 * @return {@code True} if this search view is visible, {@code false} otherwise.
	 * @see #reveal()
	 * @see #conceal()
	 */
	public boolean isRevealed() {
		return getVisibility() == View.VISIBLE;
	}

	/**
	 * Conceals this search view via {@link RevealAnimationHandler#startConcealAnimation(SearchView)}  RevealAnimationHandler.startConcealAnimation(SearchView)}
	 * using either default handler or handler specified via {@link #setRevealAnimationHandler(RevealAnimationHandler)}.
	 * <p>
	 * This method does nothing if this search view is already concealed.
	 *
	 * @see #isRevealed()
	 * @see #reveal()
	 */
	public void conceal() {
		if (!isRevealed()) return;
		this.setSearching(false);
		mRevealAnimationHandler.startConcealAnimation(this);
		WidgetUtils.hideSoftKeyboard(mEditText);
	}

	/**
	 * Same as {@link #setQuery(CharSequence)} for resource id.
	 *
	 * @param resId Resource id of the desired query text.
	 * @see #getQuery()
	 */
	public void setQuery(@StringRes int resId) {
		mEditText.setText(resId);
	}

	/**
	 * Specifies a query text to be set to the search input field.
	 *
	 * @param query The desired query text. May be {@code null} or empty to clear the current search
	 *              text.
	 * @see #setQuery(int)
	 * @see #getQuery()
	 * @see #setQueryHint(CharSequence)
	 */
	public void setQuery(@Nullable CharSequence query) {
		mEditText.setText(query);
	}

	/**
	 * Returns the current text from the search input field.
	 *
	 * @return Search query text or empty text if there was no query text specified or entered by
	 * a user.
	 * @see #setQuery(int)
	 * @see #setQuery(CharSequence)
	 */
	@NonNull
	public CharSequence getQuery() {
		return mQueryText;
	}

	/**
	 * Clears the current search query text if it is presented.
	 * <p>
	 * This will also fire {@link OnQueryTextListener#onQueryTextCleared(SearchView)} callback for
	 * the current listener if it is attached.
	 *
	 * @return {@code True} if the query text has been cleared, {@code false} if there were no query
	 * text to be cleared.
	 */
	public boolean clearQuery() {
		if (TextUtils.isEmpty(mQueryText)) {
			return false;
		}
		setSearching(false);
		this.mPendingQueryClear = true;
		mEditText.setText("");
		if (mQueryTextListener != null) {
			mQueryTextListener.onQueryTextCleared(this);
		}
		return true;
	}

	/**
	 * Same as {@link #setQueryHint(CharSequence)} for resource id.
	 *
	 * @param resId Resource id of the desired hint text.
	 * @see #getQueryHint()
	 */
	public void setQueryHint(@StringRes int resId) {
		mEditText.setHint(resId);
	}

	/**
	 * Specifies a hint text for the search input field.
	 *
	 * @param hint The desired hint text. May be {@code null} or empty to clear the current one.
	 * @see android.R.attr#queryHint
	 * @see #setQueryHint(int)
	 * @see #getQueryHint()
	 * @see #setQuery(CharSequence)
	 */
	public void setQueryHint(@Nullable CharSequence hint) {
		mEditText.setHint(hint);
	}

	/**
	 * Returns the hint text specified for the search input field.
	 *
	 * @return Hint text or {@code null} if no hint has been specified.
	 * @see #setQueryHint(CharSequence)
	 * @see #setQueryHint(int)
	 */
	@Nullable
	public CharSequence getQueryHint() {
		return mEditText.getHint();
	}

	/**
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mButtonIcon.setEnabled(enabled);
		mEditText.setEnabled(enabled);
		mButtonClear.setEnabled(enabled);
		mButtonVoiceSearch.setEnabled(enabled);
	}

	/**
	 * Sets a boolean flag indicating whether there is search running for this search view or not.
	 * If {@code true} is specified a simple loading bar will be displayed at the place of search
	 * icon otherwise it will be hided.
	 * <p>
	 * This method does nothing if this search view is already at the requested state.
	 *
	 * @param searching {@code True} to show loading bar, {@code false} to hide it.
	 * @see #isSearching()
	 */
	public void setSearching(boolean searching) {
		if (mSearching == searching) return;
		if (mSearching = searching) {
			mProgressBarAnimator.showProgressBar(this);
		} else {
			mProgressBarAnimator.hideProgressBar(this);
		}
	}

	/**
	 * Returns a boolean flag indicating whether there is search running at this time for this search
	 * view or not.
	 * <p>
	 * <b>Note</b>, that this is only informative indication as this search view does not handle
	 * searching logic of any kind but only shows a simple loading bar at the place of search icon
	 * if this method returns {@code true}.
	 *
	 * @return {@code True} if search is running and progress bar is visible, {@code false} otherwise.
	 */
	public boolean isSearching() {
		return mSearching;
	}

	/**
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		// SearchView is expected to be used mainly above or at the place of toolbar so it will handle
		// touch events like the Toolbar widget.
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		if (action == MotionEvent.ACTION_DOWN) {
			this.mEatingTouch = false;
		}
		if (!mEatingTouch) {
			final boolean handled = super.onTouchEvent(event);
			if (action == MotionEvent.ACTION_DOWN && !handled) {
				this.mEatingTouch = true;
			}
		}
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			this.mEatingTouch = false;
		}
		return true;
	}

	/**
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.hint = mEditText.getHint();
		savedState.query = mEditText.getText();
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
		if (!TextUtils.isEmpty(savedState.hint)) {
			setQueryHint(savedState.hint);
		}
		if (!TextUtils.isEmpty(savedState.query)) {
			setQuery(savedState.query);
		}
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * A {@link WidgetSavedState} implementation used to ensure that the state of {@link SearchView}
	 * is properly saved.
	 *
	 * @author Martin Albedinsky
	 */
	public static class SavedState extends WidgetSavedState {

		/**
		 * Creator used to create an instance or array of instances of SavedState from {@link android.os.Parcel}.
		 */
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
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
		CharSequence hint, query;

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
			this.hint = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
			this.query = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
		}

		/**
		 */
		@Override
		public void writeToParcel(@NonNull Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			TextUtils.writeToParcel(hint, dest, flags);
			TextUtils.writeToParcel(query, dest, flags);
		}
	}

	/**
	 * This class holds all data necessary to tint all components of this view.
	 */
	private static final class SearchTintInfo extends TintInfo {

		/**
		 * Color state list used to tint a specific states of the <b>text select handle</b> drawables.
		 */
		ColorStateList textSelectHandleTintList;

		/**
		 * Flag indicating whether the {@link #textSelectHandleTintList} has been set or not.
		 */
		boolean hasTextSelectHandleTintList;
	}

	/**
	 * Implementation of {@link RevealAnimationHandler} used as base for reveal animation handlers
	 * specific for a particular API level.
	 */
	private static abstract class BaseRevealAnimationHandler implements RevealAnimationHandler {

		/**
		 * Duration in milliseconds for reveal animation.
		 */
		long revealDuration = UiConfig.ANIMATION_DURATION_MEDIUM;

		/**
		 * Duration in milliseconds for conceal animation.
		 */
		long concealDuration = UiConfig.ANIMATION_DURATION_SHORT;

		/**
		 * Interpolator used for reveal animations.
		 */
		Interpolator interpolator = new FastOutSlowInInterpolator();
	}

	/**
	 * Default implementation of {@link BaseRevealAnimationHandler}.
	 */
	private static class DefaultRevealAnimationHandler extends BaseRevealAnimationHandler {

		/**
		 */
		@Override
		public void startRevealAnimation(@NonNull SearchView searchView) {
			searchView.setVisibility(View.VISIBLE);
		}

		/**
		 */
		@Override
		public void startConcealAnimation(@NonNull SearchView searchView) {
			searchView.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * A {@link BaseRevealAnimationHandler} implementation used on {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH ICE_CREAM_SANDWICH}
	 * and later versions of Android.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class IceCreamSandwichRevealAnimationHandler extends DefaultRevealAnimationHandler {

		/**
		 */
		@Override
		public void startRevealAnimation(@NonNull SearchView searchView) {
			searchView.setAlpha(0f);
			searchView.setVisibility(View.VISIBLE);
			searchView.animate().alpha(1f)
					.setInterpolator(interpolator)
					.setDuration(revealDuration)
					.setListener(null)
					.start();
		}

		/**
		 */
		@Override
		public void startConcealAnimation(@NonNull final SearchView searchView) {
			searchView.animate().alpha(0f)
					.setInterpolator(interpolator)
					.setDuration(concealDuration)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							searchView.setVisibility(View.INVISIBLE);
						}
					}).start();
		}
	}

	/**
	 * A {@link IceCreamSandwichRevealAnimationHandler} extended implementation used on
	 * {@link Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and later versions of Android.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static class LollipopRevealAnimationHandler extends IceCreamSandwichRevealAnimationHandler {

		/**
		 */
		@Override
		public void startRevealAnimation(@NonNull SearchView searchView) {
			searchView.setVisibility(View.VISIBLE);
			final float[] viewCenter = resolveCenter(searchView, 1.0f, 0.5f);
			final Animator animator = createAnimator(
					searchView,
					viewCenter[0] - searchView.getHeight() / 2f, viewCenter[1],
					0,
					calculateRadius(searchView)
			);
			animator.setDuration(revealDuration);
			animator.setInterpolator(interpolator);
			animator.start();
		}

		/**
		 */
		@Override
		public void startConcealAnimation(@NonNull final SearchView searchView) {
			final float[] viewCenter = resolveCenter(searchView, 1.0f, 0.5f);
			final Animator animator = createAnimator(
					searchView,
					viewCenter[0] - searchView.getHeight() / 2f, viewCenter[1],
					calculateRadius(searchView),
					0
			);
			animator.setDuration(concealDuration);
			animator.setInterpolator(interpolator);
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					searchView.setVisibility(View.INVISIBLE);
				}
			});
			animator.start();
		}

		/**
		 * Resolves a center of the specified <var>view</var> based on its current <var>width</var> and
		 * <b>height</b> according to the requested fractions.
		 *
		 * @param view            The view of which center to resolve.
		 * @param centerXFraction Fraction to resolve x coordinate of the center. Should be from the range
		 *                        {@code [0f, 1f]}.
		 * @param centerYFraction Fraction to resolve y coordinate of the center. Should be from the range
		 *                        {@code [0f, 1f]}.
		 * @return An array with center coordinates: centerX[0], centerY[1].
		 */
		static float[] resolveCenter(View view, float centerXFraction, float centerYFraction) {
			return new float[]{
					view.getWidth() * centerXFraction,
					view.getHeight() * centerYFraction
			};
		}

		/**
		 * Calculates a radius of the specified <var>view</var> for the purpose of circular reveal animation
		 * based on its current <b>width</b> and <b>height</b>.
		 *
		 * @param view The view of which radius to calculate.
		 * @return Either width or height of the given view, depends on which is grater.
		 */
		static float calculateRadius(@NonNull View view) {
			return (float) Math.sqrt(Math.pow(view.getWidth(), 2) + Math.pow(view.getHeight(), 2));
		}

		/**
		 * Creates a new instance of circular reveal Animator for the specified <var>view</var>.
		 *
		 * @param view        The view for which to create the requested animator.
		 * @param centerX     X coordinate of a center from where should the reveal animation start.
		 * @param centerY     Y coordinate of a center from where should the reveal animation start.
		 * @param startRadius Radius of the specified view at the start of the reveal animation.
		 * @param endRadius   Radius of the specified view at the end of the reveal animation.
		 * @return Animator that will play circular reveal animation for the specified view according
		 * to the specified parameters when started.
		 * @see ViewAnimationUtils#createCircularReveal(View, int, int, float, float)
		 */
		static Animator createAnimator(View view, float centerX, float centerY, float startRadius, float endRadius) {
			return ViewAnimationUtils.createCircularReveal(view, Math.round(centerX), Math.round(centerY), startRadius, endRadius);
		}
	}

	/**
	 * Base class for simple animator used to animate showing and hiding of SearchView's progress bar.
	 */
	private static abstract class ProgressBarAnimator {

		/**
		 * Called to show progress bar of the given <var>searchView</var>.
		 *
		 * @param searchView The search view of which progress bar to show.
		 */
		abstract void showProgressBar(SearchView searchView);

		/**
		 * Called to hide progress bar of the given <var>searchView</var>.
		 *
		 * @param searchView The search view of which progress bar to hide.
		 */
		abstract void hideProgressBar(SearchView searchView);
	}

	/**
	 * Default implementation of {@link ProgressBarAnimator}.
	 */
	private static class DefaultProgressBarAnimator extends ProgressBarAnimator {

		/**
		 * Interpolator used for icon animations.
		 */
		Interpolator iconInterpolator = new FastOutSlowInInterpolator();

		/**
		 */
		@Override
		void showProgressBar(SearchView searchView) {
			final View icon = searchView.mButtonIcon;
			final CircularProgressBar progressBar = searchView.mProgressBar;
			icon.setVisibility(View.INVISIBLE);
			progressBar.setVisibility(View.VISIBLE);
		}

		/**
		 */
		@Override
		void hideProgressBar(SearchView searchView) {
			final View icon = searchView.mButtonIcon;
			final CircularProgressBar progressBar = searchView.mProgressBar;
			icon.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * A {@link DefaultProgressBarAnimator} extended implementation used on
	 * {@link Build.VERSION_CODES#LOLLIPOP LOLLIPOP} and later versions of Android.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class IceCreamSandwichProgressBarAnimator extends DefaultProgressBarAnimator {

		/**
		 */
		@Override
		void showProgressBar(SearchView searchView) {
			final View icon = searchView.mButtonIcon;
			final ViewPropertyAnimator iconAnimator = icon.animate();
			iconAnimator.setListener(null);
			iconAnimator.cancel();
			iconAnimator
					.scaleX(0.5f)
					.scaleY(0.5f)
					.alpha(0.0f)
					.setDuration(UiConfig.ANIMATION_DURATION_MEDIUM)
					.setInterpolator(iconInterpolator)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							icon.setVisibility(View.INVISIBLE);
						}
					})
					.start();
			final CircularProgressBar progressBar = searchView.mProgressBar;
			final ViewPropertyAnimator progressBarAnimator = progressBar.animate();
			progressBarAnimator.setListener(null);
			progressBarAnimator.cancel();
			progressBar.setAlpha(0.0f);
			progressBar.setVisibility(View.VISIBLE);
			progressBarAnimator
					.alpha(1.0f)
					.setDuration(UiConfig.ANIMATION_DURATION_MEDIUM)
					.setListener(null)
					.start();
		}

		/**
		 */
		@Override
		void hideProgressBar(SearchView searchView) {
			final CircularProgressBar progressBar = searchView.mProgressBar;
			final ViewPropertyAnimator progressBarAnimator = progressBar.animate();
			progressBarAnimator.setListener(null);
			progressBarAnimator.cancel();
			progressBarAnimator
					.alpha(0.0f)
					.setDuration(UiConfig.ANIMATION_DURATION_SHORT)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							progressBar.setVisibility(View.INVISIBLE);
						}
					})
					.start();
			final View icon = searchView.mButtonIcon;
			final ViewPropertyAnimator iconAnimator = icon.animate();
			iconAnimator.setListener(null);
			iconAnimator.cancel();
			icon.setVisibility(View.VISIBLE);
			icon.setScaleX(0.5f);
			icon.setScaleY(0.5f);
			icon.setAlpha(0.0f);
			iconAnimator
					.scaleX(1.0f)
					.scaleY(1.0f)
					.alpha(1.0f)
					.setDuration(UiConfig.ANIMATION_DURATION_SHORT)
					.setInterpolator(iconInterpolator)
					.setListener(null)
					.start();
		}
	}
}
