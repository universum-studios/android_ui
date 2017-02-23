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

import android.animation.ObjectAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import universum.studios.android.samples.ui.ui.fragment.BaseSamplesFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.samples.ui.R;
import universum.studios.android.ui.graphics.drawable.CircularProgressDrawable;
import universum.studios.android.ui.graphics.drawable.LinearProgressDrawable;
import universum.studios.android.ui.graphics.drawable.ProgressDrawable;
import universum.studios.android.ui.widget.BaseProgressBar;
import universum.studios.android.ui.widget.CircularProgressBar;
import universum.studios.android.ui.widget.LinearProgressBar;

/**
 * @author Martin Albedinsky
 */
@ContentView(R.layout.fragment_components_progress_and_activity)
@ActionBarOptions(title = R.string.components_navigation_progress_and_activity)
public class ProgressAndActivityFragment extends BaseSamplesFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "ProgressAndActivityFragment";

	CircularProgressBar mCircularProgressBar;

	private DeterminateHandler mLinearDeterminateHandler;
	private DeterminateHandler mLinearBufferHandler;
	private DeterminateHandler mLinearQueryIndeterminateDeterminateHandler;

	private DeterminateHandler mCircularDeterminateHandler1;
	private DeterminateHandler mCircularDeterminateHandler2;
	private DeterminateHandler mCircularDeterminateHandler3;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.manageLinearProgressBars(view);
		this.manageCircularProgressBars(view);
	}

	private void manageLinearProgressBars(View view) {
		this.mLinearDeterminateHandler = new DeterminateHandler<LinearProgressBar>((LinearProgressBar) view.findViewById(
				R.id.fragment_components_linear_progress_bar_determinate
		)) {

			@Override
			void onStarted() {
				super.onStarted();
				progressBar.setAlpha(1);
			}

			@Override
			void onFinished() {
				super.onFinished();
				ObjectAnimator.ofFloat(progressBar, "alpha", 1, 0).start();
			}
		};
		this.mLinearBufferHandler = new BufferHandler((LinearProgressBar) view.findViewById(
				R.id.fragment_components_linear_progress_bar_buffer
		));
		this.mLinearQueryIndeterminateDeterminateHandler = new QueryIndeterminateDeterminateHandler((LinearProgressBar) view.findViewById(
				R.id.fragment_components_linear_progress_bar_query_indeterminate_determinate
		));

		mLinearDeterminateHandler.start();
		mLinearBufferHandler.start();
		mLinearQueryIndeterminateDeterminateHandler.start();
	}

	@SuppressWarnings("ConstantConditions")
	private void manageCircularProgressBars(View view) {
		final CircularProgressDrawable circularProgressDr = ((CircularProgressBar) view.findViewById(
				R.id.fragment_components_circular_progress_bar_with_arrow
		)).getDrawable();
		circularProgressDr.setArrowEnabled(true);
		circularProgressDr.setArrowVisible(true);
		circularProgressDr.setIndeterminateSpeed(0.5f);
		circularProgressDr.setThickness(15);
		circularProgressDr.setRounded(false);

		// Manage determinate progress bars.
		this.mCircularDeterminateHandler1 = new DeterminateHandler<CircularProgressBar>((CircularProgressBar) view.findViewById(
				R.id.fragment_components_circular_progress_bar_determinate_1
		)) {

			@Override
			void onStarted() {
				super.onStarted();
				progressBar.setAlpha(1);
			}

			@Override
			void onFinished() {
				super.onFinished();
				ObjectAnimator.ofFloat(progressBar, "alpha", 1, 0).start();
			}
		};
		this.mCircularDeterminateHandler2 = new DeterminateHandler<CircularProgressBar>((CircularProgressBar) view.findViewById(
				R.id.fragment_components_circular_progress_bar_determinate_2
		)) {

			@Override
			void onStarted() {
				super.onStarted();
				progressBar.explodeProgress();
			}

			@Override
			void onFinished() {
				super.onFinished();
				progressBar.implodeProgress();
			}
		};
		this.mCircularDeterminateHandler3 = new DeterminateHandler<CircularProgressBar>((CircularProgressBar) view.findViewById(
				R.id.fragment_components_circular_progress_bar_determinate_3
		)) {

			@Override
			void onStarted() {
				super.onStarted();
				progressBar.explodeProgress();
			}

			@Override
			void onFinished() {
				super.onFinished();
				progressBar.implodeProgress();
			}
		};
		mCircularDeterminateHandler1.start();
		mCircularDeterminateHandler2.start();
		//mCircularDeterminateHandler3.start();
		this.mCircularProgressBar = (CircularProgressBar) view.findViewById(
				R.id.fragment_components_circular_progress_bar_determinate_3
		);
		new ProgressAsyncTask().execute();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mLinearDeterminateHandler.stop();
		mLinearBufferHandler.stop();
		mLinearQueryIndeterminateDeterminateHandler.stop();
		mCircularDeterminateHandler1.stop();
		mCircularDeterminateHandler2.stop();
		mCircularDeterminateHandler3.stop();
		mCircularProgressBar.setOnProgressExplodeAnimationListener(null);
	}

	private static class DeterminateHandler<B extends BaseProgressBar> extends Handler {

		final Runnable UPDATE_PROGRESS = new Runnable() {

			@Override
			public void run() {
				if (progress < 100) {
					final float nextProgress = computeProgress(progress);
					if (nextProgress != progress) {
						onProgressChange((int) (progress = nextProgress));
					}
					postDelayed(this, 1000 / 60);
				} else {
					onFinished();
				}
			}
		};

		final Runnable START = new Runnable() {

			@Override
			public void run() {
				start();
			}
		};

		final B progressBar;
		float progress;

		DeterminateHandler(B progressBar) {
			this.progressBar = progressBar;
		}

		void start() {
			this.progress = 0;
			progressBar.setProgress(0);
			post(UPDATE_PROGRESS);
			onStarted();
		}

		void stop() {
			removeCallbacks(UPDATE_PROGRESS);
			onStopped();
		}

		float computeProgress(float currentProgress) {
			if (currentProgress < 30) {
				return currentProgress + 2f;
			} else if (currentProgress < 80) {
				return currentProgress + 1f;
			} else if (currentProgress < 90) {
				return currentProgress + 0.5f;
			} else if (currentProgress < 95) {
				return currentProgress + 0.25f;
			}
			return currentProgress + 0.15f;
		}

		void onProgressChange(int progress) {
			progressBar.setProgress(progress);
		}

		void onStarted() {
		}

		void onFinished() {
			postDelayed(START, 2000);
		}

		void onStopped() {
		}
	}

	private static final class BufferHandler extends DeterminateHandler<LinearProgressBar> {

		boolean buffering;
		float bufferProgress;
		float frozenBuffer;

		BufferHandler(LinearProgressBar progressBar) {
			super(progressBar);
			final LinearProgressDrawable drawable = progressBar.getDrawable();
			//noinspection ConstantConditions
			drawable.setBufferIndeterminateMarksScaleInterval(500);
		}

		@Override
		void onStarted() {
			super.onStarted();
			bufferProgress = frozenBuffer = 0;
			progressBar.restartMode();
			progressBar.explodeProgress();
		}

		@Override
		float computeProgress(float currentProgress) {
			if (bufferProgress == 0) {
				buffering = true;
				bufferProgress += 1;
				progressBar.setSecondaryProgress((int) bufferProgress);
				return currentProgress;
			}

			final int secondaryProgress = progressBar.getSecondaryProgress();
			if (secondaryProgress < 100) {
				// Freeze the buffer for a while.
				if (secondaryProgress > 65 && frozenBuffer < 20) {
					frozenBuffer += 0.15f;
					buffering = false;
					return Math.min(currentProgress + 0.5f, secondaryProgress);
				}

				final int primaryProgress = progressBar.getProgress();
				if ((buffering && secondaryProgress - primaryProgress < 50) || (secondaryProgress - primaryProgress < 20)) {
					buffering = true;
					bufferProgress += 0.5f;
					progressBar.setSecondaryProgress((int) bufferProgress);

					if (secondaryProgress - primaryProgress >= 20) {
						return Math.min(currentProgress + 0.25f, secondaryProgress);
					}
					return currentProgress;
				}
			}
			buffering = false;
			return Math.min(currentProgress + 0.75f, secondaryProgress);
		}

		@Override
		void onProgressChange(int progress) {
			super.onProgressChange(progress);
			if (buffering) {
				bufferProgress += 0.15f;
				progressBar.setSecondaryProgress((int) bufferProgress);
			}
		}

		@Override
		void onFinished() {
			super.onFinished();
			progressBar.implodeProgress();
		}
	}

	private static final class QueryIndeterminateDeterminateHandler extends DeterminateHandler<LinearProgressBar> {

		final Runnable STOP_INDETERMINATE = new Runnable() {

			@Override
			public void run() {
				progressBar.stopIndeterminate();
			}
		};

		QueryIndeterminateDeterminateHandler(LinearProgressBar progressBar) {
			super(progressBar);
			progressBar.setOnProgressAnimationListener(new LinearProgressBar.OnProgressAnimationListener() {

				@Override
				public void onStarted(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable) {
					// Ignored.
				}

				@Override
				public void onStopped(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable) {
					QueryIndeterminateDeterminateHandler.super.start();
				}
			});
		}

		@Override
		void start() {
			progressBar.restartMode();
			progressBar.explodeProgress();
			postDelayed(STOP_INDETERMINATE, 5000);
		}

		@Override
		void onFinished() {
			super.onFinished();
			progressBar.implodeProgress();
		}
	}

	private final class ProgressAsyncTask extends AsyncTask<Void, Void, Void>
		implements
		CircularProgressBar.OnProgressExplodeAnimationListener {

		@Override
		public void onExploded(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable) {
		}

		@Override
		public void onImploded(@NonNull BaseProgressBar progressBar, @NonNull ProgressDrawable drawable) {
			new ProgressAsyncTask().execute();
		}

		@Override
		protected void onPreExecute() {
			if (mCircularProgressBar != null) {
				mCircularProgressBar.setOnProgressExplodeAnimationListener(this);
				mCircularProgressBar.setProgress(0);
				mCircularProgressBar.explodeProgress();
			}
		}

		@Override
		protected Void doInBackground(Void[] params) {
			int progress = 0;
			while (progress < 100) {
				if (mCircularProgressBar != null) {
					mCircularProgressBar.setProgress(++progress);
				}
				try {
					Thread.sleep(1000 / 60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void o) {
			if (mCircularProgressBar != null) {
				mCircularProgressBar.postDelayed(new Runnable() {

					/**
					 */
					@Override
					public void run() {
						mCircularProgressBar.implodeProgress();
					}
				}, 500);
			}
		}
	}
}
