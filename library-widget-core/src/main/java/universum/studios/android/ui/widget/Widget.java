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

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * Interface for the widgets presented within the UI library.
 *
 * @author Martin Albedinsky
 */
public interface Widget {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Flag indicating whether the widgets can use new animation API introduced in
	 * {@link android.os.Build.VERSION_CODES#ICE_CREAM_SANDWICH ICE_CREAM_SANDWICH} to animate theirs
	 * properties.
	 */
	boolean ANIMABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * <b>Not supported yet.</b>
	 */
	@NonNull
	WidgetSizeAnimator animateSize();

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * This class holds base data necessary to tint components of a particular widget.
	 */
	class TintInfo {

		/**
		 * Color state list used to tint a specific states of a particular drawable of the
		 * associated widget.
		 */
		public ColorStateList tintList;

		/**
		 * Flag indicating whether the {@link #tintList} has been set or not.
		 */
		public boolean hasTintList;

		/**
		 * Blending mode used to apply tint to a particular drawable of the associated widget.
		 */
		public PorterDuff.Mode tintMode;

		/**
		 * Flag indicating whether the {@link #tintMode} has been set or not.
		 */
		public boolean hasTintMode;
	}

	/**
	 * This class holds base data necessary to tint components and background of a particular widget.
	 */
	class BackgroundTintInfo extends TintInfo {

		/**
		 * Color state list used to tint a specific states of a background drawable of the associated
		 * widget.
		 */
		public ColorStateList backgroundTintList;

		/**
		 * Flag indicating whether the {@link #backgroundTintList} has been set or not.
		 */
		public boolean hasBackgroundTintList;

		/**
		 * Blending mode used to apply tint to a background drawable of the associated widget.
		 */
		public PorterDuff.Mode backgroundTintMode;

		/**
		 * Flag indicating whether the {@link #backgroundTintMode} has been set or not.
		 */
		public boolean hasBackgroundTinMode;
	}
}
