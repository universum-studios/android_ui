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
import android.view.MenuItem;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.PagesAdapter;
import universum.studios.android.support.samples.ui.SamplesActivity;
import universum.studios.android.ui.widget.ViewPagerWidget;

/**
 * @author Martin Albedinsky
 */
public final class PagerActivity extends SamplesActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "SamplePagerActivity";

	private ViewPagerWidget mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_pager);
		this.mViewPager = (ViewPagerWidget) findViewById(R.id.pager);
		setUpActionBar();
		setUpViewPager();
	}

	private void setUpViewPager() {
		mViewPager.setAdapter(new PagesAdapter(getSupportFragmentManager()));
		mViewPager.setPageFlingSwipingEnabled(true);
	}

	@SuppressWarnings("ConstantConditions")
	private void setUpActionBar() {
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
