/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cs50.hotpotato;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Displays an Android spinner widget backed by data in an array. The
 * array is loaded from the strings.xml resources file.
 */
public class ShowGameSpinner extends Activity 
{
	// fields that will hold current position and contents of spinner
	protected int mPos;
	protected String mSelection;

	// connects spinner widget to array-based data
	protected ArrayAdapter<CharSequence> mAdapter;

	// initial position of spinner when installed
	public static final int DEFAULT_POSITION = 2;

	// PREFERENCES_FIL = name of properties file that stores position and selection when activity isn't running
	public static final String PREFERENCES_FILE = "SpinnerPrefs";

	// following values are used to read/write properties file

	// PROPERTY_DELIMITER = delimits key and value in Java properties file
	public static final String PROPERTY_DELIMITER = "=";

	// POSITION_KEY = key or label for "position" in preferences file
	public static final String POSITION_KEY = "Position";

	// SELECTION_KEY = key or label for "selection" in the preferences file	 */
	public static final String SELECTION_KEY = "Selection";

	// marker strings used to write the properties into the file
	public static final String POSITION_MARKER = POSITION_KEY + PROPERTY_DELIMITER;
	public static final String SELECTION_MARKER = SELECTION_KEY + PROPERTY_DELIMITER;
	
	// adding an additional thing
	private String gameName;

	/**
	 * Initializes the application and the activity.
	 * 1) Sets the view to spinnergame.xml
	 * 2) Sets the spinner's array data based on info specific php sites
	 * 3) Instantiates a callback listener for handling selection from the
	 *    spinner
	 * Notice that this method includes code that can be uncommented to force
	 * tests to fail.
	 *
	 * This method overrides the default onCreate() method for an Activity.
	 *
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{

		/**
		 * derived classes that use onCreate() overrides must always call the super constructor
		 */
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spinnergame);

		TextView printname = (TextView) findViewById(R.id.username);
		Intent i = getIntent();
		// Receiving the Data from previous activity -- specifically the username of the player
		String name = i.getStringExtra("name");
		// Displaying Received data
		printname.setText(name);

		Spinner spinner = (Spinner) findViewById(R.id.spinner1);


 		this.mAdapter = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item);  
		this.mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);        
		spinner.setAdapter(this.mAdapter);
		
		// this.mAdapter.add("dummy item");  

		
		// have to add all of the possible games to play into the spinner...(a.k.a. this.mAdapter)

		// BUT FIRST, have get the current user's ID
		final String userID = getUserID(name);

		// and then i want to add all of the games...
		// use this doc to get the potatoIDs for games the user is in
		Document docgame = null;

		// connect to webpage containing user IDs and associated games
		try 
		{
			docgame = Jsoup.connect("http://speedspud.com/android/allGames.php/").get();
			
			Elements rows = docgame.select("p");

			// iterate through rows looking for rows containing the user's ID
			for (Element oneRow : rows)
			{
				String temp = oneRow.toString();

				// search this row for current user's ID
				int rowID = temp.indexOf("userID " + userID + " gameID");		

				// if this is the row for the given user ID
				if (rowID != -1)
				{
					// get the potato ID
					int startID = temp.indexOf("gameID:");
					startID = startID + 8;
					String tempID = temp.substring(startID);

					// end of userid
					int endID = tempID.indexOf("</p>");

					String potatoID = tempID.substring(0, endID);

					String gameName = getGameName(potatoID);
					
					this.mAdapter.add(gameName);  
				}
			}
			
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 



		// listener triggered when user has selected an item in the spinner
		OnItemSelectedListener spinnerListener = new myOnItemSelectedListener(this,this.mAdapter);


		spinner.setOnItemSelectedListener(spinnerListener);

		// Binding Click event to Button
		Button closeButton = (Button) findViewById(R.id.playbutton);
		closeButton.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) 
			{
				Intent nextScreen = new Intent(getApplicationContext(), PlayGame.class);
				
				//Sending data to another Activity
				nextScreen.putExtra("userID", userID);
				nextScreen.putExtra("gamename", gameName);
				
				// starting new activity
				startActivity(nextScreen);

			}
		});

	}

	private String getGameName (String potatoID)
	{
		Document doc = null;

		// connect to webpage containing potato game information
		try 
		{
			doc = Jsoup.connect("http://speedspud.com/android/gameInfo.php/").get();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements rows = doc.select("p");

		// iterate through rows looking for rows containing the user's ID
		for (Element oneRow : rows)
		{
			String temp = oneRow.toString();

			// search this row for current potato ID
			int rowID = temp.indexOf(" potatoID: " + potatoID + " currentUser");		

			// if this is a row with the given potato ID
			if (rowID != -1)
			{
				// get the potato game name
				int startName = temp.indexOf("gameName:");
				startName = startName + 10;
				String tempName = temp.substring(startName);

				// end of game name
				int endID = tempName.indexOf(" potatoID:");

				String gameName = tempName.substring(0, endID);	

				return gameName;
			}
		}
		return null;
	}

	private String getUserID (String username)
	{
		Document doc = null;

		// connect to webpage containing usernames and user IDs
		try 
		{
			doc = Jsoup.connect("http://speedspud.com/android/userID.php/").get();		
			Elements rows = doc.select("p");

			for (Element oneRow : rows)
			{
				String temp = oneRow.toString();

				// search this row for current user's username
				int rowID = temp.indexOf(username);		

				// if this is the row for the given username
				if (rowID != -1)
				{
					// get the user ID
					int startID = temp.indexOf("ID:");
					startID = startID + 4;
					String tempID = temp.substring(startID);

					// end of userid
					int endID = tempID.indexOf("</p>");

					String userID = tempID.substring(0, endID);

					return userID;
				}
			}
			return null;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	/**
	 *  A callback listener that implements the
	 *  {@link android.widget.AdapterView.OnItemSelectedListener} interface
	 *  For views based on adapters, this interface defines the methods available
	 *  when the user selects an item from the View.
	 *
	 */
	public class myOnItemSelectedListener implements OnItemSelectedListener 
	{

		/*
		 * provide local instances of the mLocalAdapter and the mLocalContext
		 */

		ArrayAdapter<CharSequence> mLocalAdapter;
		Activity mLocalContext;

		/**
		 *  Constructor
		 *  @param c - The activity that displays the Spinner.
		 *  @param ad - The Adapter view that
		 *    controls the Spinner.
		 *  Instantiate a new listener object.
		 */
		public myOnItemSelectedListener(Activity c, ArrayAdapter<CharSequence> ad) {

			this.mLocalContext = c;
			this.mLocalAdapter = ad;

		}

		/**
		 * When the user selects an item in the spinner, this method is invoked by the callback
		 * chain. Android calls the item selected listener for the spinner, which invokes the
		 * onItemSelected method.
		 *
		 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(
		 *  android.widget.AdapterView, android.view.View, int, long)
		 * @param parent - the AdapterView for this listener
		 * @param v - the View for this listener
		 * @param pos - the 0-based position of the selection in the mLocalAdapter
		 * @param row - the 0-based row number of the selection in the View
		 */
		public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) 
		{

			ShowGameSpinner.this.mPos = pos;
			ShowGameSpinner.this.mSelection = parent.getItemAtPosition(pos).toString();
			/*
			 * Set the value of the text field in the UI
			 */
			TextView resultText = (TextView)findViewById(R.id.SpinnerResult);
			resultText.setText(ShowGameSpinner.this.mSelection);
			
			gameName = ShowGameSpinner.this.mSelection;
		}

		/**
		 * The definition of OnItemSelectedListener requires an override
		 * of onNothingSelected(), even though this implementation does not use it.
		 * @param parent - The View for this Listener
		 */
		public void onNothingSelected(AdapterView<?> parent) 
		{

			// do nothing

		}
	}

	/**
	 * Restores the current state of the spinner (which item is selected, and the value
	 * of that item).
	 * Since onResume() is always called when an Activity is starting, even if it is re-displaying
	 * after being hidden, it is the best place to restore state.
	 *
	 * Attempts to read the state from a preferences file. If this read fails,
	 * assume it was just installed, so do an initialization. Regardless, change the
	 * state of the spinner to be the previous position.
	 *
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() 
	{

		/*
		 * an override to onResume() must call the super constructor first.
		 */

		super.onResume();
/*
		
		 * Try to read the preferences file. If not found, set the state to the desired initial
		 * values.
		 

		if (!readInstanceState(this)) 
			setInitialState();*/

		/*
		 * Set the spinner to the current state.
		 */

		Spinner restoreSpinner = (Spinner)findViewById(R.id.spinner1);
		restoreSpinner.setSelection(getSpinnerPosition());

	}

	/**
	 * Store the current state of the spinner (which item is selected, and the value of that item).
	 * Since onPause() is always called when an Activity is about to be hidden, even if it is about
	 * to be destroyed, it is the best place to save state.
	 *
	 * Attempt to write the state to the preferences file. If this fails, notify the user.
	 *
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {

		/*
		 * an override to onPause() must call the super constructor first.
		 */

		super.onPause();

		/*
		 * Save the state to the preferences file. If it fails, display a Toast, noting the failure.
		 */

		if (!writeInstanceState(this)) 
			Toast.makeText(this,"Failed to write state!", Toast.LENGTH_LONG).show();
	}

	/**
	 * Sets the initial state of the spinner when the application is first run.
	 *//*
	public void setInitialState() 
	{

		this.mPos = DEFAULT_POSITION;

	}*/

	/**
	 * Read the previous state of the spinner from the preferences file
	 * @param c - The Activity's Context
	 */
	public boolean readInstanceState(Context c) {

		/*
		 * The preferences are stored in a SharedPreferences file. The abstract implementation of
		 * SharedPreferences is a "file" containing a hashmap. All instances of an application
		 * share the same instance of this file, which means that all instances of an application
		 * share the same preference settings.
		 */

		/*
		 * Get the SharedPreferences object for this application
		 */

		SharedPreferences p = c.getSharedPreferences(PREFERENCES_FILE, MODE_WORLD_READABLE);
		/*
		 * Get the position and value of the spinner from the file, or a default value if the
		 * key-value pair does not exist.
		 */
		this.mPos = p.getInt(POSITION_KEY, ShowGameSpinner.DEFAULT_POSITION);
		this.mSelection = p.getString(SELECTION_KEY, "");

		/*
		 * SharedPreferences doesn't fail if the code tries to get a non-existent key. The
		 * most straightforward way to indicate success is to return the results of a test that
		 * SharedPreferences contained the position key.
		 */

		return (p.contains(POSITION_KEY));

	}

	/**
	 * Write the application's current state to a properties repository.
	 * @param c - The Activity's Context
	 *
	 */
	public boolean writeInstanceState(Context c) 
	{

		/*
		 * Get the SharedPreferences object for this application
		 */

		SharedPreferences p = c.getSharedPreferences(ShowGameSpinner.PREFERENCES_FILE, MODE_WORLD_READABLE);

		/*
		 * Get the editor for this object. The editor interface abstracts the implementation of
		 * updating the SharedPreferences object.
		 */

		SharedPreferences.Editor e = p.edit();

		/*
		 * Write the keys and values to the Editor
		 */

		e.putInt(POSITION_KEY, this.mPos);
		e.putString(SELECTION_KEY, this.mSelection);

		/*
		 * Commit the changes. Return the result of the commit. The commit fails if Android
		 * failed to commit the changes to persistent storage.
		 */

		return (e.commit());

	}

	public int getSpinnerPosition() 
	{
		return this.mPos;
	}

	public void setSpinnerPosition(int pos) 
	{
		this.mPos = pos;
	}

	public String getSpinnerSelection() 
	{
		return this.mSelection;
	}

	public void setSpinnerSelection(String selection) 
	{
		this.mSelection = selection;
	}
}
