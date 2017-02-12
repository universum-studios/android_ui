/*
 * =================================================================================================
 *                             Copyright (C) 2014 Martin Albedinsky
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
package universum.studios.android.samples.ui.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Martin Albedinsky
 */
public abstract class BaseSamplesAdapterFragment<AV extends ViewGroup, A> extends BaseSamplesFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "BaseExamplesFragment";

	private A mAdapter;

	public void setAdapter(@Nullable A adapter) {
		if (mAdapter != null) {
			if (isViewCreated()) {
				onDetachAdapterFromView(mAdapter);
			}
			onDetachAdapter(mAdapter);
		}
		this.mAdapter = adapter;
		if (mAdapter != null) {
			onAttachAdapter(mAdapter);
		}
		if (isViewCreated() && mAdapter != null) {
			onAttachAdapterToView(mAdapter);
		}
	}

	public A getAdapter() {
		return mAdapter;
	}

	protected void onAttachAdapter(@NonNull A adapter) {
	}

	protected void onDetachAdapter(@NonNull A adapter) {
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (mAdapter != null) {
			onAttachAdapterToView(mAdapter);
		}
	}

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	protected AV findAdapterView() {
		return isViewCreated() ? (AV) getView().findViewById(android.R.id.list) : null;
	}

	protected abstract void onAttachAdapterToView(@NonNull A adapter);

	protected abstract void onDetachAdapterFromView(@NonNull A adapter);

	public void setEmptyText(@StringRes int resId) {
		setEmptyText(getResources().getText(resId));
	}

	public void setEmptyText(@Nullable CharSequence text) {
		// todo:
	}
}
