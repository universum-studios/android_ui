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
package universum.studios.android.samples.ui.data.model;

/**
 * @author Martin Albedinsky
 */
public final class CapitalItem {

	@SuppressWarnings("unused")
	private static final String TAG = "CapitalItem";

	public final CharSequence country;
	public final CharSequence name;
	public final int population;

	public CapitalItem(CharSequence country, CharSequence name, int population) {
		this.country = country;
		this.name = name;
		this.population = population;
	}
}
