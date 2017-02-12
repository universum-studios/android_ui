package universum.studios.android.samples.ui.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.data.Extras;
import universum.studios.android.support.samples.ui.SamplesFragment;

/**
 * @author Martin Albedinsky
 */
public final class TextFragment extends SamplesFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "TextFragment";

	@NonNull
	public static TextFragment newInstance(@Nullable CharSequence text) {
		final TextFragment fragment = new TextFragment();
		final Bundle args = new Bundle();
		args.putCharSequence(Extras.EXTRA_TEXT, text);
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	public static TextFragment newInstance(@StringRes int resId) {
		final TextFragment fragment = new TextFragment();
		final Bundle args = new Bundle();
		args.putInt(Extras.EXTRA_TEXT_RES, resId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_text, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.bindText(((TextView) view.findViewById(android.R.id.text1)));
	}

	private void bindText(TextView textView) {
		final Bundle args = getArguments();
		if (args.containsKey(Extras.EXTRA_TEXT)) {
			textView.setText(args.getCharSequence(Extras.EXTRA_TEXT));
		} else if (args.containsKey(Extras.EXTRA_TEXT_RES)) {
			textView.setText(args.getInt(Extras.EXTRA_TEXT_RES));
		}
	}
}
