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
package universum.studios.android.samples.ui.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.PagesAdapter;
import universum.studios.android.support.samples.ui.SamplesActivity;
import universum.studios.android.ui.util.ResourceUtils;
import universum.studios.android.ui.widget.ViewPagerWidget;

/**
 * @author Martin Albedinsky
 */
public final class PagerActivity extends SamplesActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "PagerActivity";

	private ViewPagerWidget mViewPager;

	@Override
	@SuppressWarnings("ConstantConditions")
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		requestFeature(FEATURE_TOOLBAR);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_pager);
		this.mViewPager = findViewById(R.id.pager);
		mViewPager.setAdapter(new PagesAdapter(getSupportFragmentManager()));
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeAsUpIndicator(ResourceUtils.getVectorDrawable(getResources(), R.drawable.samples_vc_arrow_back_24dp, getTheme()));
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.pager, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_action_page_swiping_enabled:
				item.setChecked(!item.isChecked());
				mViewPager.setPageSwipingEnabled(item.isChecked());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
