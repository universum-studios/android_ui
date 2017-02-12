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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.graphics.drawable.TintDrawable;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * A {@link ButtonWidget} implementation that can be used as action button within an {@link ActionBar}'s
 * menu to show custom action button or as utility class to configure menu items via
 * {@link #configureMenuItem(ItemConfiguration, Context)} method.
 *
 * <h3>XML attributes</h3>
 * See {@link ButtonWidget}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#actionButtonStyle actionButtonStyle}
 *
 * @author Martin Albedinsky
 */
public class ActionButton extends ButtonWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ActionButton";

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
	 * Same as {@link #ActionButton(Context, AttributeSet)} without attributes.
	 */
	public ActionButton(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #ActionButton(Context, AttributeSet, int)} with {@link R.attr#actionButtonStyle}
	 * as attribute for default style.
	 */
	public ActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.actionButtonStyle);
	}

	/**
	 * Same as {@link #ActionButton(Context, AttributeSet, int, int)} with {@code 0} as default style.
	 */
	public ActionButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of ActionButton for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@SuppressWarnings("unused")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ActionButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
		// No initialization.
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(ActionButton.class.getName());
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(ActionButton.class.getName());
	}

	/**
	 * Performs configuration for a {@link MenuItem} determined by the given <var>configuration</var>.
	 *
	 * @param configuration The configuration object carrying all necessary data to perform menu item
	 *                      configuration.
	 * @param context       Context used to resolve current theme attributes.
	 * @return {@code True} if configuration has been performed, {@code false} if not due to invalid
	 * menu item id or menu specified for the given <var>configuration</var> object.
	 */
	public static boolean configureMenuItem(@NonNull ItemConfiguration configuration, @NonNull Context context) {
		if (configuration.itemId == ItemConfiguration.NO_ID) {
			return false;
		}
		final MenuItem menuItem = configuration.menu.findItem(configuration.itemId);
		if (menuItem == null) {
			return false;
		}
		final Resources resources = context.getResources();
		final Resources.Theme theme = context.getTheme();
		Drawable icon = null;
		if (configuration.icon != null) {
			icon = configuration.icon;
		} else if (configuration.vectorIconResId != ItemConfiguration.NO_ID) {
			icon = ResourceUtils.getVectorDrawable(resources, configuration.vectorIconResId, theme);
		} else if (configuration.iconResId != ItemConfiguration.NO_ID) {
			icon = ResourceUtils.getDrawable(resources, configuration.iconResId, theme);
		}
		if (icon != null) {
			icon = applyIconTint(icon, configuration);
			menuItem.setIcon(icon);
		}
		return true;
	}

	/**
	 * Applies tint specified for the given <var>configuration</var> object to the given <var>icon</var>
	 * drawable.
	 *
	 * @param icon          The icon drawable to which to apply the tint.
	 * @param configuration Configuration object carrying tint list and tint mode used to apply tint
	 *                      to the icon drawable.
	 * @return Icon drawable with applied tint. For pre {@link Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
	 * the icon drawable will be wrapped in {@link TintDrawable} to support tinting feature.
	 */
	@SuppressLint("NewApi")
	private static Drawable applyIconTint(Drawable icon, ItemConfiguration configuration) {
		if (UiConfig.MATERIALIZED) {
			if (configuration.iconTintListSpecified) {
				icon.setTintList(configuration.iconTintList);
			}
			if (configuration.iconTintModeSpecified) {
				icon.setTintMode(configuration.iconTintMode);
			}
		} else {
			final TintDrawable tintIcon = icon instanceof TintDrawable ? (TintDrawable) icon : new TintDrawable(icon);
			if (configuration.iconTintListSpecified) {
				tintIcon.setTintList(configuration.iconTintList);
			}
			if (configuration.iconTintModeSpecified) {
				tintIcon.setTintMode(configuration.iconTintMode);
			}
			icon = tintIcon;
		}
		return icon;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Simple configuration class used to configure items of a specific {@link Menu}. A new instance
	 * of ItemConfiguration can be build via {@link ActionButton.ItemConfigurationBuilder}.
	 * <p>
	 * Current implementation supports only configuration related to menu item's <b>icon</b> like
	 * its resource id, vector resource id and also tint support.
	 *
	 * @author Martin Albedinsky
	 */
	public static final class ItemConfiguration {

		/**
		 * Constant used to identify that no resource id has been specified.
		 */
		static final int NO_ID = -1;

		/**
		 * Menu where to find item with {@link #itemId} that should be configured using this configuration.
		 */
		final Menu menu;

		/**
		 * Id of the icon to be configured using this configuration.
		 */
		final int itemId;

		/**
		 * Resource id of icon drawable.
		 */
		final int iconResId;

		/**
		 * Resource id of icon vector drawable.
		 */
		final int vectorIconResId;

		/**
		 * Icon drawable.
		 */
		final Drawable icon;

		/**
		 * Tint to be applied to the icon drawable.
		 */
		final ColorStateList iconTintList;

		/**
		 * Boolean flag indicating whether {@link #iconTintList} has been specified or not.
		 */
		final boolean iconTintListSpecified;

		/**
		 * Blending mode used to apply {@link #iconTintList} to the icon drawable.
		 */
		final PorterDuff.Mode iconTintMode;

		/**
		 * Boolean flag indicating whether {@link #iconTintMode} has been specified or not.
		 */
		final boolean iconTintModeSpecified;

		/**
		 * Creates a new instance of ItemConfiguration from the current data specified within the
		 * given <var>builder</var>.
		 *
		 * @param builder The builder of which data to use to create new configuration item.
		 */
		ItemConfiguration(ItemConfigurationBuilder builder) {
			this.menu = builder.menu;
			this.itemId = builder.itemId;
			this.iconResId = builder.iconResId;
			this.vectorIconResId = builder.vectorIconResId;
			this.icon = builder.icon;
			this.iconTintList = builder.iconTintList;
			this.iconTintListSpecified = builder.iconTintListSpecified;
			this.iconTintMode = builder.iconTintMode;
			this.iconTintModeSpecified = builder.iconTintModeSpecified;
		}
	}

	/**
	 * Builder that can be used to build a new instances of {@link ItemConfiguration}.
	 * <p>
	 * <b>Note, that none of values that can be supplied to this builder to build a new instance
	 * of ItemConfiguration are required except the {@link Menu} that is supplied during builder's
	 * initialization.</b>
	 *
	 * @author Martin Albedinsky
	 */
	public static final class ItemConfigurationBuilder {

		/**
		 * See {@link ItemConfiguration#menu ItemConfiguration.menu}.
		 */
		final Menu menu;

		/**
		 * See {@link ItemConfiguration#itemId ItemConfiguration.itemId}.
		 */
		int itemId = ItemConfiguration.NO_ID;

		/**
		 * See {@link ItemConfiguration#iconResId ItemConfiguration.iconResId}.
		 */
		int iconResId = ItemConfiguration.NO_ID;

		/**
		 * See {@link ItemConfiguration#vectorIconResId ItemConfiguration.vectorIconResId}.
		 */
		int vectorIconResId = ItemConfiguration.NO_ID;

		/**
		 * See {@link ItemConfiguration#icon ItemConfiguration.icon}.
		 */
		Drawable icon;

		/**
		 * See {@link ItemConfiguration#iconTintList ItemConfiguration.iconTintList}.
		 */
		ColorStateList iconTintList;

		/**
		 * See {@link ItemConfiguration#iconTintListSpecified ItemConfiguration.iconTintListSpecified}.
		 */
		boolean iconTintListSpecified;

		/**
		 * See {@link ItemConfiguration#iconTintMode ItemConfiguration.iconTintMode}.
		 */
		PorterDuff.Mode iconTintMode;

		/**
		 * See {@link ItemConfiguration#iconTintModeSpecified ItemConfiguration.iconTintModeSpecified}.
		 */
		boolean iconTintModeSpecified;

		/**
		 * Creates a new instance of ItemConfigurationBuilder with the given <var>menu</var>.
		 *
		 * @param menu The menu of which items are to be configured.
		 */
		public ItemConfigurationBuilder(@NonNull Menu menu) {
			this.menu = menu;
		}

		/**
		 * Resets this build to its initial state.
		 *
		 * @return This builder to allow methods chaining.
		 */
		public ItemConfigurationBuilder reset() {
			this.itemId = ItemConfiguration.NO_ID;
			this.iconResId = ItemConfiguration.NO_ID;
			this.vectorIconResId = ItemConfiguration.NO_ID;
			this.icon = null;
			this.iconTintList = null;
			this.iconTintListSpecified = false;
			this.iconTintMode = null;
			this.iconTintModeSpecified = false;
			return this;
		}

		/**
		 * Specifies an id of the menu item to by configured by the new ItemConfiguration.
		 *
		 * @param id Id of the desired menu item that is presented in the {@link Menu} specified
		 *           for this builder.
		 * @return This builder to allow methods chaining.
		 */
		public ItemConfigurationBuilder itemId(@IdRes int id) {
			this.itemId = id;
			return this;
		}

		/**
		 * Specifies an icon resource id for the menu item to be configured by the new ItemConfiguration.
		 *
		 * @param resId Resource id of the desired icon.
		 * @return This builder to allow methods chaining.
		 * @see #vectorIcon(int)
		 * @see #icon(Drawable)
		 */
		public ItemConfigurationBuilder icon(@DrawableRes int resId) {
			this.iconResId = resId;
			return this;
		}

		/**
		 * Specifies a vector icon resource id for the menu item to be configured by the new ItemConfiguration.
		 * <p>
		 * <b>Note</b>, that if there are specified both <b>icon</b> and <b>vector icon</b> resources,
		 * the vector resource has higher priority.
		 *
		 * @param resId Resource id of the desired vector icon.
		 * @return This builder to allow methods chaining.
		 * @see #icon(int)
		 * @see #icon(Drawable)
		 */
		public ItemConfigurationBuilder vectorIcon(@DrawableRes int resId) {
			this.vectorIconResId = resId;
			return this;
		}

		/**
		 * Specifies an icon drawable for the menu item to be configured by the new ItemConfiguration.
		 * <p>
		 * <b>Note</b>, that if there are specified icon or vector icon resource ids, they will be
		 * ignored if the <var>icon</var> drawable is valid (non null).
		 *
		 * @param icon The desired icon drawable.
		 * @return This builder to allow methods chaining.
		 * @see #icon(int)
		 * @see #vectorIcon(int)
		 */
		public ItemConfigurationBuilder icon(@Nullable Drawable icon) {
			this.icon = icon;
			return this;
		}

		/**
		 * Same as {@link #iconTintList(ColorStateList)} where the color state list will be created
		 * from the specified <var>tint</var> color.
		 *
		 * @param tint The desired single tint color.
		 * @return This builder to allow methods chaining.
		 */
		public ItemConfigurationBuilder iconTint(@ColorInt int tint) {
			return iconTintList(ColorStateList.valueOf(tint));
		}


		/**
		 * Specifies a tint to be applied to the icon drawable specified via {@link #icon(Drawable)}
		 * or one of icon resource id methods.
		 *
		 * @param tintList he desired tint to be applied.
		 * @return This builder to allow methods chaining.
		 */
		public ItemConfigurationBuilder iconTintList(@Nullable ColorStateList tintList) {
			this.iconTintList = tintList;
			this.iconTintListSpecified = true;
			return this;
		}

		/**
		 * Specifies a blending mode that should be used to apply tint specified via
		 * {@link #iconTintList(ColorStateList)} to the icon drawable.
		 *
		 * @param tintMode The desired Porter duff mode.
		 * @return This builder to allow methods chaining.
		 */
		public ItemConfigurationBuilder iconTintMode(@Nullable PorterDuff.Mode tintMode) {
			this.iconTintMode = tintMode;
			this.iconTintModeSpecified = true;
			return this;
		}

		/**
		 * Builds a new instance of ItemConfiguration from the current data of this builder.
		 *
		 * @return New ItemConfiguration instance.
		 */
		@NonNull
		public ItemConfiguration build() {
			return new ItemConfiguration(this);
		}
	}
}
