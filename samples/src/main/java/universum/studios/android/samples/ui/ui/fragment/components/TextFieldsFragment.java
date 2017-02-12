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
package universum.studios.android.samples.ui.ui.fragment.components;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.fragment.BaseExamplesFragment;
import universum.studios.android.ui.widget.EditLayout;

/**
 * @author Martin Albedinsky
 */
@ContentView(R.layout.fragment_components_text_fields)
@ActionBarOptions(title = R.string.components_navigation_text_fields)
public class TextFieldsFragment extends BaseExamplesFragment
		implements
		EditLayout.OnInputLengthChangeListener {

	@SuppressWarnings("unused")
	private static final String TAG = "TextFieldsFragment";

	private EditLayout mEditLayoutError;
	private EditLayout mEditLayoutLengthConstraint1;
	private EditLayout mEditLayoutLengthConstraint2;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.validate_input).setOnClickListener(this);
		this.mEditLayoutError = (EditLayout) view.findViewById(R.id.fragment_components_text_fields_error_field);
		this.mEditLayoutLengthConstraint1 = (EditLayout) view.findViewById(R.id.fragment_components_text_fields_length_constraint_field_1);
		this.mEditLayoutLengthConstraint2 = (EditLayout) view.findViewById(R.id.fragment_components_text_fields_length_constraint_field_2);
		mEditLayoutError.getInputView().addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Ignored.
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Ignored.
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s == null) {
					return;
				}
				if (s.toString().contentEquals("Error Text")) {
					mEditLayoutError.setError(s);
				} else {
					mEditLayoutError.clearError();
				}
			}
		});
		mEditLayoutLengthConstraint1.setOnInputLengthChangeListener(this);
		mEditLayoutLengthConstraint2.setOnInputLengthChangeListener(this);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onInputLengthChanged(@NonNull EditLayout editLayout, int length) {
		switch (editLayout.getId()) {
			case R.id.fragment_components_text_fields_length_constraint_field_1:
				if (length > editLayout.getLengthConstraint()) {
					editLayout.setError("Maximum allowed count of characters exceeded.");
				} else {
					editLayout.clearError();
				}
				break;
			case R.id.fragment_components_text_fields_length_constraint_field_2:
				if (editLayout.getText().toString().contains("error input")) {
					editLayout.setError("Error input entered");
				} else {
					editLayout.clearError();
				}
				break;
		}
	}

	@Override
	protected boolean onViewClick(@NonNull View view, int id) {
		switch (id) {
			case R.id.validate_input:
				mEditLayoutError.clearError();
				mEditLayoutError.setError("Input is not valid");
				return true;
		}
		return super.onViewClick(view, id);
	}
}
