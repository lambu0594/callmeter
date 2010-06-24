/*
 * Copyright (C) 2010 Felix Bechstein, The Android Open Source Project
 * 
 * This file is part of Call Meter 3G.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.callmeter.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import de.ub0r.android.callmeter.R;
import de.ub0r.android.callmeter.data.LogRunner;
import de.ub0r.android.callmeter.ui.prefs.Preferences;
import de.ub0r.android.lib.DonationHelper;

/**
 * Callmeter's Main {@link ListActivity}.
 * 
 * @author flx
 */
public class Plans extends ListActivity {
	/** Tag for output. */
	public static final String TAG = "main";
	/** Dialog: update. */
	private static final int DIALOG_UPDATE = 0;

	/** Prefs: name for last version run. */
	private static final String PREFS_LAST_RUN = "lastrun";

	/** Display ads? */
	private static boolean prefsNoAds;

	/** SharedPreferences. */
	private SharedPreferences preferences;
	/** Plans. */
	private PlanAdapter adapter = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setTheme(Preferences.getTheme(this));
		this.setContentView(R.layout.plans);
		// get prefs.
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String v0 = this.preferences.getString(PREFS_LAST_RUN, "");
		String v1 = this.getString(R.string.app_version);
		if (!v0.equals(v1)) {
			SharedPreferences.Editor editor = this.preferences.edit();
			editor.putString(PREFS_LAST_RUN, v1);
			editor.commit();
			this.showDialog(DIALOG_UPDATE);
		}
		prefsNoAds = DonationHelper.hideAds(this);

		// TextView tv = (TextView) this.findViewById(R.id.calls_);
		// Preferences.textSizeMedium = tv.getTextSize();
		// tv = (TextView) this.findViewById(R.id.calls1_in_);
		// Preferences.textSizeSmall = tv.getTextSize();

		// final ListView list = this.getListView();
		this.adapter = new PlanAdapter(this);
		this.setListAdapter(this.adapter);
		// list.setOnItemClickListener(this);
		// list.setOnItemLongClickListener(this);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onResume() {
		super.onResume();
		if (!prefsNoAds) {
			this.findViewById(R.id.ad).setVisibility(View.VISIBLE);
		}
		LogRunner.registerMain(this);
		// start LogRunner
		LogRunner.update(this.getContentResolver());
		// schedule next update
		LogRunner.schedNext(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onPause() {
		super.onPause();
		LogRunner.unregisterMain(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final Dialog onCreateDialog(final int id) {
		AlertDialog.Builder builder;
		switch (id) {
		case DIALOG_UPDATE:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.changelog_);
			final String[] changes = this.getResources().getStringArray(
					R.array.updates);
			StringBuilder buf = new StringBuilder();

			buf.append(this.getString(R.string.see_about));

			for (int i = 0; i < changes.length; i++) {
				buf.append("\n\n");
				buf.append(changes[i]);
			}
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setMessage(buf.toString());
			buf = null;
			builder.setCancelable(true);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int id) {
							dialog.cancel();
						}
					});
			return builder.create();
		default:
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		if (prefsNoAds) {
			menu.removeItem(R.id.item_donate);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_settings: // start settings activity
			this.startActivity(new Intent(this, Preferences.class));
			return true;
		case R.id.item_donate:
			this.startActivity(new Intent(this, DonationHelper.class));
			return true;
		default:
			return false;
		}
	}
}