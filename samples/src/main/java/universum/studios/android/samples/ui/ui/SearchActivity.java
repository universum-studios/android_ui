package universum.studios.android.samples.ui.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import universum.studios.android.samples.ui.R;
import universum.studios.android.support.samples.ui.SamplesActivity;
import universum.studios.android.ui.widget.SearchView;

/**
 * @author Martin Albedinsky
 */
public final class SearchActivity extends SamplesActivity
		implements
		SearchView.OnIconClickListener,
		SearchView.OnQueryTextListener {

	@SuppressWarnings("unused")
	private static final String TAG = "SearchActivity";

	private SearchView searchView;

	public static void launch(@NonNull Activity caller) {
		caller.startActivity(new Intent(caller, SearchActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestFeature(FEATURE_TOOLBAR);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		this.searchView = (SearchView) findViewById(R.id.search_view);
		this.searchView.setOnIconClickListener(this);
		this.searchView.setOnQueryTextListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_search:
				searchView.reveal();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSearchIconClick(@NonNull SearchView searchView) {
		searchView.clearQuery();
		searchView.conceal();
		return true;
	}

	@Override
	public void onQueryTextChanged(@NonNull SearchView searchView, @NonNull CharSequence queryText) {
		searchView.setSearching(true);
		this.stopSearchingDelayed(1000);
		Log.d(TAG, "Search text changed to: '" + queryText + "'");
	}

	@Override
	public void onQueryTextConfirmed(@NonNull SearchView searchView, @NonNull CharSequence queryText) {
		searchView.setSearching(true);
		this.stopSearchingDelayed(1000);
		Log.d(TAG, "Search text confirmed as: '" + queryText + "'");
	}

	@Override
	public void onQueryTextCleared(@NonNull SearchView searchView) {
		Log.d(TAG, "Search text cleared");
	}

	private void stopSearchingDelayed(long delay) {
		searchView.postDelayed(new Runnable() {
			@Override
			public void run() {
				searchView.setSearching(false);
			}
		}, delay);
	}

	@Override
	public void onBackPressed() {
		if (searchView.isRevealed()) {
			searchView.clearQuery();
			searchView.conceal();
			return;
		}
		super.onBackPressed();
	}
}
