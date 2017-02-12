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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.LinkedList;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;

/**
 * A {@link TextViewWidget} implementation that can be used as empty view for collection views like
 * {@link android.widget.ListView ListView} or {@link android.widget.GridView GridView} or {@code RecyclerView}.
 * <p>
 * A desired text for EmptyView can be specified as for ordinary TextView via {@link #setText(int)}
 * or {@link #setText(CharSequence)} where the specified text will be trimmed if such feature is
 * enabled via {@link #setTrimTextEnabled(boolean)}. By default this feature is <b>enabled</b>.
 * The current "empty" text can be also cross-faded with a new one via {@link #crossFadeText(int)}
 * or {@link #crossFadeText(CharSequence)}. Duration for this animation can be specified via
 * {@link #setFadeDuration(long)} as total duration for both <b>fade out</b> and <b>fade in</b>
 * animations together.
 *
 * <h3>XML attributes</h3>
 * See {@link TextViewWidget},
 * {@link R.styleable#Ui_EmptyView EmptyView Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiEmptyViewStyle uiEmptyViewStyle}
 *
 * @author Martin Albedinsky
 * @see TextViewWidget
 */
public class EmptyView extends TextViewWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "EmptyView";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Application resources.
	 */
	private Resources mResources;

	/**
	 * Animations helper used to run animations upon this view regardless current Android version.
	 */
	private Animations mAnimations;

	/**
	 * Flag indicating whether a text set to this view should be trimmed or not.
	 */
	private boolean mTrimTextEnabled = true;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #EmptyView(android.content.Context, android.util.AttributeSet)} without attributes.
	 */
	public EmptyView(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #EmptyView(android.content.Context, android.util.AttributeSet, int)} with
	 * {@link R.attr#uiEmptyViewStyle} as attribute for default style.
	 */
	public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiEmptyViewStyle);
	}

	/**
	 * Same as {@link #EmptyView(android.content.Context, android.util.AttributeSet, int, int)} with
	 * {@code 0} as default style.
	 */
	public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of EmptyView for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
		this.mAnimations = Animations.get(this);
		this.mResources = context.getResources();

		/**
		 * Process attributes.
		 */
		final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_EmptyView, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_EmptyView_android_fadeDuration) {
					setFadeDuration(typedArray.getInt(index, (int) mAnimations.fadeDuration));
				} else if (index == R.styleable.Ui_EmptyView_uiTrimTextEnabled) {
					setTrimTextEnabled(typedArray.getBoolean(index, mTrimTextEnabled));
				}
			}
			typedArray.recycle();
		}

		// If no id has been specified, ensure that we have at least the framework's default one.
		if (getId() == NO_ID) setId(android.R.id.empty);
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(EmptyView.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(EmptyView.class.getName());
	}

	/**
	 * Specifies a duration determining how long should take the cross fade animation whenever one
	 * of {@code crossFade(...)} methods has been invoked, that is, how long it will take to fade
	 * (change) the current text for the new one.
	 * <p>
	 * Default duration is set to <b>600 ms</b>.
	 *
	 * @param duration The desired duration in milliseconds.
	 * @see #getFadeDuration()
	 */
	public void setFadeDuration(long duration) {
		mAnimations.fadeDuration = Math.max(0, duration / 2);
	}

	/**
	 * Returns the duration of the cross fade animation.
	 *
	 * @return Duration for both fade in + out animations together in milliseconds.
	 * @see #setFadeDuration(long)
	 */
	public long getFadeDuration() {
		return mAnimations.fadeDuration * 2;
	}

	/**
	 * Specifies a boolean flag indicating whether the cross-fading text should be also trimmed
	 * before it is set to this view or not.
	 * <p>
	 * This feature is by default <b>enabled</b>.
	 *
	 * @param enabled {@code True} to enable trimming of text, {@code false} otherwise.
	 * @see #isTrimTextEnabled()
	 */
	public void setTrimTextEnabled(boolean enabled) {
		this.mTrimTextEnabled = enabled;
	}

	/**
	 * Returns the boolean flag indicating whether this view will automatically trim the cross-fading
	 * text or not.
	 *
	 * @return {@code True} if trimming of text is enabled, {@code false} otherwise.
	 * @see #setTrimTextEnabled(boolean)
	 */
	public boolean isTrimTextEnabled() {
		return mTrimTextEnabled;
	}

	/**
	 * Clears all texts that are scheduled to be cross faded for this empty view.
	 */
	public void clearTextsToCrossFade() {
		mAnimations.clearTextsToCrossFade();
	}

	/**
	 * Same as {@link #crossFadeText(CharSequence)} for resource id.
	 *
	 * @param resId Resource id of the desired text to be cross-faded with the current one.
	 */
	public void crossFadeText(@StringRes int resId) {
		crossFadeText(mResources.getText(resId));
	}

	/**
	 * Starts <b>cross fade</b> animation of the text of this empty view.
	 *
	 * @param text The desired text to be cross faded with the current one.
	 */
	public void crossFadeText(@Nullable CharSequence text) {
		final boolean shouldTrim = mTrimTextEnabled && !TextUtils.isEmpty(text);
		final CharSequence textToCrossFade = shouldTrim ? trimText(text) : text;
		if (mAnimations.shouldCrossFadeText(textToCrossFade)) mAnimations.crossFadeText(textToCrossFade);
	}

	/**
	 * Starts <b>fade in</b> animation of this view.
	 *
	 * @see #fadeOut()
	 */
	public void fadeIn() {
		mAnimations.fadeIn();
	}

	/**
	 * Starts <b>fade out</b> animation of this view.
	 *
	 * @see #fadeIn()
	 */
	public void fadeOut() {
		mAnimations.fadeOut();
	}

	/**
	 */
	@Override
	public void setText(CharSequence text, BufferType type) {
		if (TextUtils.isEmpty(text) || !mTrimTextEnabled) {
			super.setText(text, type);
			return;
		}
		super.setText(trimText(text), type);
	}

	/**
	 * Trims the specified text by removing white spaces at its start and also at its end.
	 *
	 * @param text The text to be trimmed.
	 * @return Trimmed text.
	 */
	@NonNull
	public static CharSequence trimText(@NonNull CharSequence text) {
		int length = text.length();
		final int trimmedLength = TextUtils.getTrimmedLength(text);
		if (length > trimmedLength) {
			final SpannableStringBuilder builder = new SpannableStringBuilder(text);

			// Remove white spaces from the start.
			int start = 0;
			while (start < length && builder.charAt(start) <= ' ') {
				start++;
			}
			builder.delete(0, start);
			length -= start;

			// Remove white spaces from the end.
			int end = length;
			while (end >= 0 && builder.charAt(end - 1) <= ' ') {
				end--;
			}
			builder.delete(end, length);
			return builder;
		}
		return text;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Animations interface for this view.
	 */
	private static abstract class Animations {

		/**
		 * Flag to indicate that fade in animation is running.
		 */
		static final int FADE_IN = 0x01;

		/**
		 * Flag to indicate that fade out animation is running.
		 */
		static final int FADE_OUT = 0x02;

		/**
		 * Runnable that can be used to post fade in execution as part of cross fade animation.
		 */
		final Runnable FADE_IN_DURING_CROSS_FADE = new Runnable() {

			/**
			 */
			@Override
			public void run() {
				if (crossFading) fadeIn();
			}
		};

		/**
		 * View upon which will be animations performed.
		 */
		final EmptyView view;

		/**
		 * Queue containing texts that should be cross faded.
		 */
		final LinkedList<CharSequence> textsToCrossFade;

		/**
		 * Type of the fade animation that is running at this time (if any).
		 */
		int fadeType;

		/**
		 * Flag indicating whether we are running cross fade animation or not.
		 */
		boolean crossFading = false;

		/**
		 * Duration in milliseconds used for fade animations.
		 */
		long fadeDuration = UiConfig.ANIMATION_DURATION_MEDIUM;

		/**
		 * Creates a new instance of Animations for the specified <var>view</var>.
		 *
		 * @param view The empty view for which to run animations.
		 */
		Animations(EmptyView view) {
			this.view = view;
			this.textsToCrossFade = new LinkedList<>();
		}

		/**
		 * Returns a new instance of Animations implementation specific for the current animations
		 * API capabilities.
		 *
		 * @param view The view upon which will the returned Animations object run all requested
		 *             animations.
		 * @return New instance of Animations implementation.
		 */
		static Animations get(EmptyView view) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				return new IceCreamSandwichAnimations(view);
			}
			return new DefaultAnimations(view);
		}

		/**
		 * Checks whether the specified text should be cross faded into empty view or not.
		 * <p>
		 * This method compares the given text with the current text of the empty view and also with
		 * the last text that is scheduled to be cross faded (if any).
		 *
		 * @param textToCrossFade The desired text that should be cross faded.
		 * @return {@code True} if the specified text should be cross faded via {@link #crossFadeText(CharSequence)}
		 * because it will change current text of the empty view, {@code false} otherwise.
		 */
		boolean shouldCrossFadeText(CharSequence textToCrossFade) {
			CharSequence currentText = textsToCrossFade.isEmpty() ? view.getText() : textsToCrossFade.getLast();
			final boolean boothInvalid = currentText == null && textToCrossFade == null;
			final boolean boothValid = currentText != null && textToCrossFade != null;
			return !boothInvalid && (!boothValid || !currentText.toString().contentEquals(textToCrossFade.toString()));
		}

		/**
		 * Checks whether there is stored some text that should be cross faded.
		 *
		 * @return {@code True} if {@link #poolTextToCrossFade()} will return the next text that should
		 * be cross-faded, {@code false} if all texts has been successfully cross-faded.
		 */
		final boolean hasTextToCrossFade() {
			return !textsToCrossFade.isEmpty();
		}

		/**
		 * Returns and removes the head text from the current queue of texts to cross fade.
		 *
		 * @return The next text to be cross faded or {@code null} if there are no more texts to
		 * cross fade.
		 * @see #hasTextToCrossFade()
		 */
		CharSequence poolTextToCrossFade() {
			return textsToCrossFade.poll();
		}

		/**
		 * Clears all texts that should be cross faded.
		 */
		void clearTextsToCrossFade() {
			textsToCrossFade.clear();
		}

		/**
		 * Starts cross fade animation of the specified <var>text</var>. This will decide whether
		 * to start new cross fade or just fade in animation depends on the current state of running
		 * fade animations and how much texts to cross fade are queued.
		 *
		 * @param text The desired text that should be cross faded for the empty view.
		 */
		void crossFadeText(CharSequence text) {
			if (textsToCrossFade.isEmpty() && !crossFading) {
				if (getCurrentAlpha() == 0) {
					view.setText(text);
					fadeIn();
				} else {
					textsToCrossFade.add(text);
					crossFade();
				}
			} else {
				textsToCrossFade.add(text);
			}
		}

		/**
		 * Starts either fade in or fade out animation of the attached view based on its current alpha
		 * value.
		 *
		 * @return {@code True} if fade in animation has been started, {@code false} if fade out
		 * animation has been started.
		 */
		boolean crossFade() {
			this.crossFading = true;
			if (getCurrentAlpha() == 0) {
				fadeIn();
				return true;
			}
			fadeOut();
			return false;
		}

		/**
		 * Handles animation end that has occurred during the cross fade animation. This will check
		 * the current fade type and based on it will either set the next text to the empty view and
		 * will start the fade in animation to show it or it will start a new cross fade animation
		 * for the next text or does nothing if all texts has been already cross faded.
		 */
		void handleAnimationEndDuringCrossFade() {
			switch (fadeType) {
				case FADE_OUT:
					view.setText(poolTextToCrossFade());
					view.postDelayed(FADE_IN_DURING_CROSS_FADE, 50);
					break;
				case FADE_IN:
					if (hasTextToCrossFade()) {
						crossFade();
					} else {
						crossFading = false;
					}
					break;
			}
		}

		/**
		 * Returns current alpha of the empty view.
		 *
		 * @return Alpha value from the range {@code [0, 255]}.
		 */
		abstract float getCurrentAlpha();

		/**
		 * Starts fade in animation of the attached view.
		 */
		abstract void fadeIn();

		/**
		 * Starts fade out animation of the attached view.
		 */
		abstract void fadeOut();
	}

	/**
	 * Default implementation of {@link Animations}.
	 */
	private static final class DefaultAnimations extends Animations implements Animation.AnimationListener {

		/**
		 * Animation used to fade in the empty view.
		 */
		final AlphaAnimation FADE_IN_ANIMATION = new AlphaAnimation(0.0f, 1.0f);

		/**
		 * Animation used to fade out the empty view.
		 */
		final AlphaAnimation FADE_OUT_ANIMATION = new AlphaAnimation(1.0f, 0.0f);

		/**
		 * Current animated alpha of the empty view.
		 */
		float alpha = 1.0f;

		/**
		 * Creates a new instance of DefaultAnimations for the specified empty <var>view</var>.
		 */
		DefaultAnimations(EmptyView view) {
			super(view);
			FADE_IN_ANIMATION.setAnimationListener(this);
			FADE_OUT_ANIMATION.setAnimationListener(this);
		}

		/**
		 */
		@Override
		float getCurrentAlpha() {
			return alpha;
		}

		/**
		 */
		@Override
		void fadeIn() {
			this.fadeType = FADE_IN;
			FADE_IN_ANIMATION.setDuration(fadeDuration);
			view.setVisibility(VISIBLE);
			view.startAnimation(FADE_IN_ANIMATION);
		}

		/**
		 */
		@Override
		void fadeOut() {
			this.fadeType = FADE_OUT;
			FADE_OUT_ANIMATION.setDuration(fadeDuration);
			view.startAnimation(FADE_OUT_ANIMATION);
		}

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
			// This is not much good solution of determining the current alpha value, but.
			switch (fadeType) {
				case FADE_OUT:
					alpha = 0;
					view.setVisibility(INVISIBLE);
					break;
				case FADE_IN:
					alpha = 1f;
					break;
			}

			if (crossFading) handleAnimationEndDuringCrossFade();
		}

		/**
		 */
		@Override
		public void onAnimationRepeat(Animation animation) {
			// Ignored.
		}
	}

	/**
	 * An {@link Animations} implementation used for post {@link android.os.Build.VERSION_CODES#ICE_CREAM_SANDWICH ICE_CREAM_SANDWICH}
	 * Android versions.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static final class IceCreamSandwichAnimations extends Animations implements Animator.AnimatorListener {

		/**
		 * Creates a new instance of IceCreamSandwichAnimations for the specified empty <var>view</var>.
		 */
		IceCreamSandwichAnimations(EmptyView view) {
			super(view);
		}

		/**
		 */
		@Override
		float getCurrentAlpha() {
			return view.getAlpha();
		}

		/**
		 */
		@Override
		void fadeIn() {
			this.fadeType = FADE_IN;
			view.animate().alpha(1)
					.setListener(this)
					.setDuration(fadeDuration)
					.start();
		}

		/**
		 */
		@Override
		void fadeOut() {
			this.fadeType = FADE_OUT;
			view.animate().alpha(0)
					.setListener(this)
					.setDuration(fadeDuration)
					.start();
		}

		/**
		 */
		@Override
		public void onAnimationStart(Animator animation) {
			// Ignored.
		}

		/**
		 */
		@Override
		public void onAnimationEnd(Animator animation) {
			if (crossFading) handleAnimationEndDuringCrossFade();
		}

		/**
		 */
		@Override
		public void onAnimationCancel(Animator animation) {
			// Ignored.
		}

		/**
		 */
		@Override
		public void onAnimationRepeat(Animator animation) {
			// Ignored.
		}
	}
}
