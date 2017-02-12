package universum.studios.android.samples.ui.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import universum.studios.android.support.fragment.BaseFragment;
import universum.studios.android.samples.ui.content.Extras;

/**
 * @author Martin Albedinsky
 */
public final class LayoutFragment extends BaseFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "ViewFragment";

	public static LayoutFragment newInstance(int layoutResource) {
		final LayoutFragment fragment = new LayoutFragment();
		final Bundle args = new Bundle();
		args.putInt(Extras.EXTRA_LAYOUT_RESOURCE, layoutResource);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final Bundle args = getArguments();
		if (args != null && args.containsKey(Extras.EXTRA_LAYOUT_RESOURCE)) {
			return inflater.inflate(args.getInt(Extras.EXTRA_LAYOUT_RESOURCE), container, false);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}
}
