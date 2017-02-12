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
import android.app.ActionBar;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.MenuItemCompat;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import universum.studios.android.ui.R;

/**
 * A {@link ActionButton} implementation that can be used as action button within an {@link ActionBar}'s
 * menu to show only text without icon.
 *
 * <h3>XML attributes</h3>
 * See {@link ActionButton},
 * {@link R.styleable#Ui_ActionTextButton ActionTextButton Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiActionTextButtonStyle uiActionTextButtonStyle}
 *
 * @author Martin Albedinsky
 */
public class ActionTextButton extends ActionButton {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ActionTextButton";

	/**
	 * Boolean flag indicating whether to use compatibility approach when working with menu items or
	 * the native one.
	 */
	private static final boolean COMPAT_MODE = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;

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
	 * Same as {@link #ActionTextButton(android.content.Context, android.util.AttributeSet)} without
	 * attributes.
	 */
	public ActionTextButton(Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #ActionTextButton(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link R.attr#uiActionTextButtonStyle} as attribute for default style.
	 */
	public ActionTextButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.uiActionTextButtonStyle);
	}

	/**
	 * Same as {@link #ActionTextButton(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public ActionTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of ActionTextButton within the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ActionTextButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Ui_ActionTextButton, defStyleAttr, defStyleRes);
		if (typedArray != null) {
			final int n = typedArray.getIndexCount();
			for (int i = 0; i < n; i++) {
				final int index = typedArray.getIndex(i);
				if (index == R.styleable.Ui_ActionTextButton_android_text) {
					setText(typedArray.getText(index));
				} else if (index == R.styleable.Ui_ActionTextButton_android_paddingLeft) {
					setPadding(
							typedArray.getDimensionPixelSize(index, 0),
							getPaddingTop(),
							getPaddingRight(),
							getPaddingBottom()
					);
				} else if (index == R.styleable.Ui_ActionTextButton_android_paddingRight) {
					setPadding(
							getPaddingLeft(),
							getPaddingTop(),
							typedArray.getDimensionPixelSize(index, 0),
							getPaddingBottom()
					);
				}
			}
			typedArray.recycle();
		}
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(ActionTextButton.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(ActionTextButton.class.getName());
	}

	/**
	 * Same as {@link #setText(Menu, int, CharSequence)} for resource id.
	 *
	 * @param resId Resource id of the desired text to be set.
	 * @see #configureMenuItem(ItemConfiguration, Context)
	 */
	public static void setText(@NonNull Menu menu, int itemId, @StringRes int resId) {
		final View actionView = obtainActionView(menu, itemId);
		if (actionView instanceof TextView) ((TextView) actionView).setText(resId);
	}

	/**
	 * Sets the specified <var>text</var> for a view of an item with the specified <var>itemId</var>.
	 * <p>
	 * <b>Note</b>, that the specified text will be set to the item's view only if it is instance of
	 * {@link TextView}.
	 *
	 * @param menu   Menu within which is the menu item presented (to be found).
	 * @param itemId Id of the desired item on which to set the text.
	 * @param text   The desired text to be set.
	 * @see #setText(Menu, int, int)
	 * @see #configureMenuItem(ItemConfiguration, Context)
	 */
	public static void setText(@NonNull Menu menu, int itemId, @Nullable CharSequence text) {
		final View actionView = obtainActionView(menu, itemId);
		if (actionView instanceof TextView) ((TextView) actionView).setText(text);
	}

	/**
	 * Sets up the specified <var>clickListener</var> on a view of an item with the specified <var>itemId</var>.
	 *
	 * @param menu          Menu within which is the menu item presented (to be found).
	 * @param itemId        Id of the desired item on which view to set the click listener.
	 * @param clickListener The desired click listener to be set.
	 * @see #configureMenuItem(ItemConfiguration, Context)
	 */
	public static void setOnClickListener(@NonNull Menu menu, int itemId, @NonNull OnClickListener clickListener) {
		final View actionView = obtainActionView(menu, itemId);
		if (actionView != null) actionView.setOnClickListener(clickListener);
	}

	/**
	 * Obtains action view of an item with the specified <var>itemId</var> from the given <var>menu</var>.
	 *
	 * @param menu   The menu where is the item presented.
	 * @param itemId Id of the menu item of which action view to obtain.
	 * @return Obtained view or {@code null} if the item does not have action view or it is not
	 * presented within the given menu.
	 */
	@SuppressLint("NewApi")
	private static View obtainActionView(Menu menu, int itemId) {
		final MenuItem menuItem = menu.findItem(itemId);
		if (menuItem == null) return null;
		return COMPAT_MODE ? MenuItemCompat.getActionView(menuItem) : menuItem.getActionView();
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
