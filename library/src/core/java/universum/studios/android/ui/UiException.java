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
package universum.studios.android.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;

/**
 * An exception that is used across the Ui library to inform about any misconfiguration or about
 * missing annotations that are required for proper working of parts of an Android application that
 * depend on the Ui library.
 *
 * @author Martin Albedinsky
 */
public class UiException extends AndroidRuntimeException {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "UiException";

	/**
	 * Default type of database exception.
	 */
	public static final int TYPE_DEFAULT = 0x00;

	/**
	 * Type of ui exception determining that such exception has been thrown only due to wrong
	 * configuration.
	 */
	public static final int TYPE_MISCONFIGURATION = 0x01;

	/**
	 * Type of ui exception determining that such exception has been thrown due to instantiation
	 * failure of a specific <b>class</b> of which instance is required.
	 */
	public static final int TYPE_INSTANTIATION = 0x02;

	/**
	 * Message of ui exception that can be created via {@link #annotationsNotEnabled()}.
	 */
	private static final String ANNOTATIONS_NOT_ENABLED_MESSAGE =
			"Trying to access logic that requires annotations processing to be enabled, " +
					"but it seams that the annotations processing is disabled for the Database library.";

	/**
	 * Format for the message of ui exception that can be created via {@link #instantiationException(String, Class, String)}.
	 */
	private static final String INSTANTIATION_FORMAT = "Failed to instantiate instance of %s class of(%s). ";

	/**
	 * Default note for ui exception that can be created via {@link #instantiationException(String, Class)}
	 */
	private static final String INSTANTIATION_NOTE = "Make sure that such class has public access and empty (without arguments) public constructor.";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Type of this ui exception. Can be one of types defined within this class.
	 */
	private final int mType;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of UiException of the specified <var>type</var>.
	 *
	 * @param type    Type of the new exception.
	 * @param message Message for the new exception.
	 */
	private UiException(int type, String message) {
		super(message);
		this.mType = type;
	}

	/**
	 * Creates a new instance of UiException of the specified <var>type</var>.
	 *
	 * @param type    Type of the new exception.
	 * @param message Message for the new exception.
	 * @param cause   Cause of the new exception.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private UiException(int type, String message, Throwable cause) {
		super(message, cause);
		this.mType = type;
	}

	/**
	 * Creates a new instance of UiException of the specified <var>type</var>.
	 *
	 * @param type  Type of the new exception.
	 * @param cause Cause of the new exception.
	 */
	private UiException(int type, Exception cause) {
		super(cause);
		this.mType = type;
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Creates a new instance of UiException type of {@link #TYPE_DEFAULT}.
	 *
	 * @param message A message for the new exception.
	 */
	public static UiException exception(String message) {
		return new UiException(TYPE_DEFAULT, message);
	}

	/**
	 * Creates a new instance of UiException type of {@link #TYPE_DEFAULT}.
	 *
	 * @param message A message for the new exception.
	 * @param cause   A cause for the new exception.
	 */
	public static UiException exception(String message, Throwable cause) {
		return new UiException(TYPE_DEFAULT, message, cause);
	}

	/**
	 * Creates a new instance of UiException type of {@link #TYPE_DEFAULT}.
	 *
	 * @param cause A cause for the new exception.
	 */
	public static UiException exception(Exception cause) {
		return new UiException(TYPE_DEFAULT, cause);
	}

	/**
	 * Creates a new instance of UiException type of {@link #TYPE_MISCONFIGURATION} with message
	 * saying that annotations are not enabled but functionality that requires annotations to be enabled
	 * has been requested.
	 */
	public static UiException annotationsNotEnabled() {
		return misconfiguration(ANNOTATIONS_NOT_ENABLED_MESSAGE);
	}

	/**
	 * Creates a new instance of UiException type of {@link #TYPE_MISCONFIGURATION} with the
	 * specified <var>message</var>.
	 *
	 * @param message The message for the new misconfiguration exception.
	 */
	public static UiException misconfiguration(@NonNull String message) {
		return new UiException(TYPE_MISCONFIGURATION, message);
	}

	/**
	 * Same as {@link #instantiationException(String, Class, String)} with default additional note.
	 */
	public static UiException instantiationException(@NonNull String objectType, @NonNull Class<?> classOf) {
		return instantiationException(objectType, classOf, INSTANTIATION_NOTE);
	}

	/**
	 * Creates a new instance of UiException type of {@link #TYPE_INSTANTIATION} for the specified
	 * <var>objectType</var> and <var>classOf</var>.
	 *
	 * @param objectType Type (name) of the object of which instantiation has failed.
	 * @param classOf    Class of the object of which instantiation has failed.
	 * @param note       Additional note for the new instantiation exception that will be added at
	 *                   the end of the exception's message text.
	 */
	public static UiException instantiationException(@NonNull String objectType, @NonNull Class<?> classOf, @NonNull String note) {
		return new UiException(
				TYPE_INSTANTIATION,
				String.format(INSTANTIATION_FORMAT,
						objectType,
						classOf.getSimpleName()
				) + (TextUtils.isEmpty(note) ? "" : " " + note)
		);
	}

	/**
	 * Returns a type of this exception.
	 *
	 * @return One of {@link #TYPE_MISCONFIGURATION}, {@link #TYPE_INSTANTIATION}.
	 */
	public final int getType() {
		return mType;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}
