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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.ui.R;
import universum.studios.android.ui.UiConfig;
import universum.studios.android.ui.util.ResourceUtils;

/**
 * An {@link universum.studios.android.ui.widget.ImageButtonWidget ImageButtonWidget} implementation
 * that can be used as button providing access to an Android application specific action like creating
 * a new calendar event or composing a new email.
 * <p>
 * This type of button uses <b>oval</b> shape as its background (that should not be changed) and an
 * elevation (for pre LOLLIPOP Android versions is used drawable imitating drop shadow) so the button
 * looks like raised above the content below it.
 * <p>
 * Icon for your desired FAB button can be specified via standard Xml attribute ({@link android.R.attr#src android:src})
 * or via one of setter methods like ({@link #setImageDrawable(Drawable)}).
 *
 * <h3>Button size</h3>
 * A FloatingActionButton can be presented in two sizes:
 * <ul>
 * <li>{@link #SIZE_NORMAL}</li>
 * <li>{@link #SIZE_MINI}</li>
 * </ul>
 * that can be specified via {@link #setSize(int)}.
 *
 *  <h3>XML attributes</h3>
 * See {@link ImageButtonWidget},
 * {@link R.styleable#Ui_FloatingActionButton FloatingActionButton Attributes}
 *
 * <h3>Default style attribute</h3>
 * {@link R.attr#uiFloatingActionButtonStyle uiFloatingActionButtonStyle}
 *
 * @author Martin Albedinsky
 */
public class FloatingActionButton extends ImageButtonWidget {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "FloatingActionButton";

	/**
	 * Size type of FAB button in content size of <b>24 dp</b> with the <b>56 dp</b> touchable area.
	 */
	public static final int SIZE_NORMAL = 0x01;

	/**
	 * Size type of FAB button in content size of <b>24 dp</b> with the <b>40 dp</b> touchable area.
	 */
	public static final int SIZE_MINI = 0x02;

	/**
	 * Defines an annotation for determining set of allowed size types for FAB button.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({SIZE_NORMAL, SIZE_MINI})
	public @interface ButtonSize {
	}

	/**
	 * Ratio used for correction of the drop shadow's offset for the pre LOLLIPOP Android versions.
	 */
	private static final float MINI_SIZE_SHADOW_OFFSET_CORRECTION_RATIO = 3.5f;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Size type flag to determine size of this FAB button.
	 */
	private int mSizeType = SIZE_NORMAL;

	/**
	 * Size of this button in pixels in case of {@link #SIZE_NORMAL} type.
	 */
	private int mSizeNormal;

	/**
	 * Size of this button in pixels in case of {@link #SIZE_MINI} type.
	 */
	private int mSizeMini;

	/**
	 * Inset of the background used so the elevation (or drop shadow) graphics could be properly
	 * visible.
	 */
	private int mInset;

	/**
	 * Drawable representing a drop shadow of this button used on pre LOLLIPOP Android versions.
	 */
	private Drawable mDropShadow;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Same as {@link #FloatingActionButton(android.content.Context, android.util.AttributeSet)}
	 * without attributes.
	 */
	public FloatingActionButton(@NonNull Context context) {
		this(context, null);
	}

	/**
	 * Same as {@link #FloatingActionButton(android.content.Context, android.util.AttributeSet, int)}
	 * with {@link R.attr#uiFloatingActionButtonStyle} as attribute for default style.
	 */
	public FloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, R.attr.uiFloatingActionButtonStyle);
	}

	/**
	 * Same as {@link #FloatingActionButton(android.content.Context, android.util.AttributeSet, int, int)}
	 * with {@code 0} as default style.
	 */
	public FloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new instance of FloatingActionButton for the given <var>context</var>.
	 *
	 * @param context      Context in which will be the new view presented.
	 * @param attrs        Set of Xml attributes used to configure the new instance of this view.
	 * @param defStyleAttr An attribute which contains a reference to a default style resource for
	 *                     this view within a theme of the given context.
	 * @param defStyleRes  Resource id of the default style for the new view.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public FloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
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
	@SuppressWarnings("WrongConstant")
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Ui_FloatingActionButton, defStyleAttr, defStyleRes);
		for (int i = 0; i < attributes.getIndexCount(); i++) {
			final int index = attributes.getIndex(i);
			if (index == R.styleable.Ui_FloatingActionButton_uiFabSize) {
				setSize(attributes.getInteger(index, mSizeType));
			}
		}
		attributes.recycle();
		final Resources resources = context.getResources();
		this.mSizeNormal = resources.getDimensionPixelSize(R.dimen.ui_fab_size_normal);
		this.mSizeMini = resources.getDimensionPixelSize(R.dimen.ui_fab_size_mini);
		this.mInset = resources.getDimensionPixelSize(R.dimen.ui_fab_inset);
        if (!UiConfig.MATERIALIZED) {
	        this.mDropShadow = ResourceUtils.getDrawable(resources, R.drawable.ui_sdw_drop_fab, context.getTheme());
        }
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(FloatingActionButton.class.getName());
	}

	/**
	 */
	@Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(FloatingActionButton.class.getName());
	}

	/**
	 * Sets a flag determining the size of this Floating action button.
	 *
	 * @param size One of {@link #SIZE_NORMAL} or {@link #SIZE_MINI}.
	 * @see R.attr#uiFabSize ui:uiFabSize
	 */
	public void setSize(@ButtonSize int size) {
		if (mSizeType != size) {
			this.mSizeType = size;
			requestLayout();
		}
	}

	/**
	 * Returns a flag determining the size of this Floating action button.
	 *
	 * @return The current button size type. One of {@link #SIZE_NORMAL} or {@link #SIZE_MINI}.
	 */
	@ButtonSize
	public int getSize() {
		return mSizeType;
	}

	/**
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = mSizeType == SIZE_NORMAL ? mSizeNormal : mSizeMini;
		size += (mInset * 2);
		setMeasuredDimension(size, size);
	}

	/**
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.updateDropShadowBounds(w, h);
	}

	/**
	 * Updates bounds of the drop shadow drawable (if used).
	 *
	 * @param width  Current width of this view.
	 * @param height Current height of this view.
	 */
	private void updateDropShadowBounds(int width, int height) {
		if (mDropShadow != null) {
            switch (mSizeType) {
                case SIZE_NORMAL:
                    mDropShadow.setBounds(0, 0, width, height);
                    break;
                case SIZE_MINI:
                    final int offset = Math.round(mInset / MINI_SIZE_SHADOW_OFFSET_CORRECTION_RATIO);
                    mDropShadow.setBounds(offset, offset, width - offset, height - offset);
                    break;
            }
        }
	}

    /**
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!UiConfig.MATERIALIZED) drawDropShadow(canvas);
        super.draw(canvas);
    }

    /**
     * Invoked to draw a drop shadow of this floating action button.
     * <p>
     * <b>Note</b>, that this is invoked only on the pre {@link android.os.Build.VERSION_CODES#LOLLIPOP LOLLIPOP}
     * Android versions.
     *
     * @param canvas Canvas that can be used to draw the shadow.
     */
    protected void drawDropShadow(@NonNull Canvas canvas) {
        if (mDropShadow != null) mDropShadow.draw(canvas);
    }

	/**
	 */
	@Nullable
	@Override
	ColorStateList createBackgroundTintColors(int tintColor) {
		return TintManager.createButtonBackgroundTintColors(getContext(), tintColor);
	}

    /**
	 * Inner classes ===============================================================================
	 */
}
