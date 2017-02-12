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
package universum.studios.android.samples.ui.ui.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import universum.studios.android.samples.ui.R;
import universum.studios.android.widget.adapter.SimpleAdapter;
import universum.studios.android.widget.adapter.ViewHolder;

/**
 * @author Martin Albedinsky
 */
public final class AppsGridAdapter extends SimpleAdapter<ApplicationInfo, AppsGridAdapter.Holder> {

	@SuppressWarnings("unused")
	private static final String TAG = "AppsGridAdapter";

	final PackageManager mPackageManager;

	public AppsGridAdapter(Context context) {
		super(context);
		this.mPackageManager = context.getPackageManager();
	}

	@NonNull
	public Context getContext() {
		return mContext;
	}

	@NonNull
	public PackageManager getPackageManager() {
		return mPackageManager;
	}

	@NonNull
	@Override
	protected View onCreateView(@NonNull ViewGroup parent, int position) {
		return inflate(R.layout.item_grid_app, parent);
	}

	@Nullable
	@Override
	protected Holder onCreateViewHolder(@NonNull View itemView, int position) {
		return new Holder(itemView);
	}

	@Override
	protected void onBindViewHolder(@NonNull Holder viewHolder, int position) {
		final ApplicationInfo info = getItem(position);
		viewHolder.icon.setImageDrawable(info.loadIcon(mPackageManager));
		viewHolder.name.setText(info.loadLabel(mPackageManager));
	}

	static final class Holder extends ViewHolder {

		ImageView icon;
		TextView name;

		Holder(@NonNull View view) {
			super(view);
			this.icon = (ImageView) view.findViewById(R.id.item_grid_app_image_view_icon);
			this.name = (TextView) view.findViewById(R.id.item_grid_app_text_view_name);
		}
	}
}
