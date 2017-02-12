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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.ui.R;
import universum.studios.android.ui.graphics.drawable.CircularProgressDrawable;

/**
 * A {@link BaseProgressBar} implementation with {@link universum.studios.android.ui.graphics.drawable.CircularProgressDrawable}
 * used to present a circular progress to a user. Like it is described in {@link BaseProgressBar BaseProgressBar's}
 * class overview, this progress bar only implements necessary logic required for proper working,
 * drawing and animating of its progress drawable.
 * <p>
 * <b>Note</b>, that this progress bar is not a replacement for the Android's {@link android.widget.ProgressBar},
 * but rather an implementation to provide the <b>material</b> based progress graphics for pre
 * {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP} Android versions. This progress bar provides
 * very similar API like the one from the Android SDK, only difference is that, it requires implementation
 * of {@link universum.studios.android.ui.graphics.drawable.CircularProgressDrawable} as its
 * drawable. Appearance of the progress drawable can be changed via {@link #setProgressMode(int)}.
 * <p>
 * If you are building an application only for LOLLIPOP and above, feel free and use ProgressBar from
 * the Android SDK instead.
 *
 * <h3>Progress modes</h3>
 * The mode which can be specified via {@link #setProgressMode(int)} changes appearance/type of the
 * progress graphics (determinate|indeterminate). See {@link universum.studios.android.ui.graphics.drawable.CircularProgressDrawable CircularProgressDrawable's}
 * class overview for supported modes to decide which one of them best fits your needs.
 *
 * <h3>XML attributes</h3>
 * See {@link BaseProgressBar},
 * {@link R.styleable#Ui_ProgressBar_Circular CircularProgressBar Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiProgressBarCircularStyle uiProgressBarCircularStyle}
 *
 * @author Martin Albedinsky
 * @see LinearProgressBar
 */
public class CircularProgressBar extends BaseProgressBar<CircularProgressDrawable> {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "CircularProgressBar";

	/**
	 * Defines an annotation for determining set of allowed modes for CircularProgressBar.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({MODE_INDETERMINATE, MODE_DETERMINATE})
	public @interface ProgressMode {
	}

	/**
	 * Flag copied from {@link universum.studios.android.ui.graphics.drawable.CircularProgressDrawable#MODE_DETERMINATE} for better access.
	 */
	public static final int MODE_DETERMINATE = CircularProgressDrawable.MODE_DETERMINATE;

	/**
	 * Flag copied from {@link universum.studios.android.ui.graphics.drawable.CircularProgressDrawable#MODE_INDETERMINATE} for better access.
	 */
	public static final int MODE_INDETERMINATE = CircularProgressDrawable.MODE_INDETERMINATE;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #CircularProgressBar(android.content.Context, android.util.AttributeSet)}
	 * without attributes.
	 */
	public CircularProgressBar(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #CircularProgressBar(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link R.attr#uiProgressBarCircularStyle} as attribute for default style.
	 */
	public CircularProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.uiProgressBarCircularStyle);
	}

	/**
	 * Same as {@link #CircularProgressBar(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of CircularProgressBar within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
	@SuppressWarnings("ConstantConditions")
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		/**
		 * Process attributes.
		 */
		final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_ProgressBar_Circular, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_ProgressBar_Circular_uiCircularProgressMode) {
					changeMode(typedArray.getInt(index, getProgressMode()));
				} else if (index == R.styleable.Ui_ProgressBar_Circular_uiProgressArrowEnabled) {
					mDrawable.setArrowEnabled(typedArray.getBoolean(index, false));
				}
			}
			typedArray.recycle();
		}
	}

	/**
	 */
	@Override
	void onAttachDrawable() {
		changeMode(MODE_INDETERMINATE);
		setDrawable(new CircularProgressDrawable());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(CircularProgressBar.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(CircularProgressBar.class.getName());
	}

	/**
	 * @see R.attr#uiCircularProgressMode ui:uiCircularProgressMode
	 */
	@Override
	public void setProgressMode(@ProgressMode int mode) {
		super.setProgressMode(mode);
	}

	/**
	 */
	@Override
	@ProgressMode
	@SuppressWarnings("ResourceType")
	public int getProgressMode() {
		return super.getProgressMode();
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#setRotation(float)}.
	 */
	public void setProgressRotation(float rotation) {
		if (mDrawable != null) mDrawable.setRotation(rotation);
	}

	/**
	 * Delegate method for {@link CircularProgressDrawable#getRotation()}.
	 *
	 * @return Progress rotation or {@code 0} if the current progress drawable is {@code null}.
	 */
	public float getProgressRotation() {
		return mDrawable != null ? mDrawable.getRotation() : 0;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
