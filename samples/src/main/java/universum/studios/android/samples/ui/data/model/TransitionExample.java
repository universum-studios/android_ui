/*
 * =================================================================================================
 *                             Copyright (C) 2015 Martin Albedinsky
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
package universum.studios.android.samples.ui.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Martin Albedinsky
 */
public class TransitionExample implements Parcelable {

	@SuppressWarnings("unused")
	private static final String TAG = "TransitionExample";

	public static final Creator<TransitionExample> CREATOR = new Creator<TransitionExample>() {
		@Override
		public TransitionExample createFromParcel(Parcel source) {
			return new TransitionExample(source);
		}

		@Override
		public TransitionExample[] newArray(int size) {
			return new TransitionExample[size];
		}
	};

	public final int id;
	public final int titleRes;
	public final int themeA, themeB;
	public final int layoutResourceA, layoutResourceB;

	private TransitionExample(Builder builder) {
		this.id = builder.id;
		this.titleRes = builder.titleRes;
		this.themeA = builder.themeA;
		this.themeB = builder.themeB;
		this.layoutResourceA = builder.layoutResourceA;
		this.layoutResourceB = builder.layoutResourceB;
	}

	private TransitionExample(Parcel source) {
		this.id = source.readInt();
		this.titleRes = source.readInt();
		this.themeA = source.readInt();
		this.themeB = source.readInt();
		this.layoutResourceA = source.readInt();
		this.layoutResourceB = source.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(titleRes);
		dest.writeInt(themeA);
		dest.writeInt(themeB);
		dest.writeInt(layoutResourceA);
		dest.writeInt(layoutResourceB);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final class Subheader extends TransitionExample {

		public Subheader(int titleRes) {
			super(new Builder().title(titleRes));
		}
	}

	public static final class Builder {

		private int id;
		private int titleRes;
		private int themeA, themeB;
		private int layoutResourceA, layoutResourceB;

		public TransitionExample build() {
			return new TransitionExample(this);
		}

		public Builder reset() {
			this.id = 0;
			this.titleRes = 0;
			this.themeA = 0;
			this.themeB = 0;
			this.layoutResourceA = 0;
			this.layoutResourceB = 0;
			return this;
		}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder title(int resId) {
			this.titleRes = resId;
			return this;
		}

		public Builder themeA(int resId) {
			this.themeA = resId;
			return this;
		}

		public Builder themeB(int resId) {
			this.themeB = resId;
			return this;
		}

		public Builder layoutResourceA(int layoutResource) {
			this.layoutResourceA = layoutResource;
			return this;
		}

		public Builder layoutResourceB(int layoutResource) {
			this.layoutResourceB = layoutResource;
			return this;
		}
	}
}
