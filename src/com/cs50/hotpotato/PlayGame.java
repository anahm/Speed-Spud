package com.cs50.hotpotato;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlayGame extends Activity 
{
	/** Called when the activity is first created. */
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playgame);

		// need to get potatoID or potatoName of game selected

		Intent i = getIntent();
		// Receiving the Data from previous activity -- specifically userID and gameName
		String userID = i.getStringExtra("userID");
		String gameName = i.getStringExtra("gamename");

		// Displaying Received data
		TextView printname = (TextView) findViewById(R.id.gamename);
		printname.setText("You are playing in " + gameName);

		// Check if the user has the potato
		Document doc = null;

		// connect to webpage containing potato game information
		try 
		{
			doc = Jsoup.connect("http://speedspud.com/android/gameInfo.php/").get();

			Elements rows = doc.select("p");

			// iterate through rows looking for row containing the potatoID
			for (Element oneRow : rows)
			{
				String temp = oneRow.toString();

				// search this row row for current game name
				String lookingFor = "gameName: " + gameName;

				// if this is a row with the given potato Name
				if (temp.indexOf(lookingFor) != -1)
				{
					// get deadline
					String searchItem = "deadline: ";
					int startDeadline = temp.indexOf(searchItem) + searchItem.length();
					int endDeadline = temp.indexOf(" numPasses");

					// if there is no deadline time yet, game hasn't started
					if (startDeadline >= endDeadline)
					{
						updatePage("Game has not begun yet! Check back later.");
					}
					else
					{
						// the deadline is... (in seconds since Unix Epoch)
						String sdeadline = temp.substring(startDeadline, endDeadline);
						int deadline = Integer.parseInt(sdeadline);

						// check the current user
						searchItem = "currentUser: ";
						int startUser = temp.indexOf(searchItem) + searchItem.length();
						String tempUser = temp.substring(startUser);
						int endUser = tempUser.indexOf(" deadline");

						// get current user
						String currentUser = tempUser.substring(0, endUser);

						TextView cuser = (TextView) findViewById(R.id.currentuser);
						String temp2 = currentUser.toString();
						cuser.setText("current user: " + temp2);

						// get number of passes remaining
						searchItem = "numPasses: ";
						int startPasses = temp.indexOf(searchItem) + searchItem.length();
						String numPasses = temp.substring(startPasses);

						// get current timestamp (in seconds from Unix Epoch)
						int timenow = (int) System.currentTimeMillis() / 1000;

						// check if (logged in) user has potato (aka is currentUser) 
						if (userID.equals(currentUser))
						{
							// check if lost
							if (numPasses.equals("0") || deadline < timenow)
							{
								// you lost!
								changePage(R.layout.explosion);
							}
							else
							{
								// letting the user know
								TextView haveStatus = (TextView) findViewById(R.id.status);
								haveStatus.setText("You have the potato! Pass it!");

								// else have potato, allow it to be passed - so we have to make button visible
								Button passButton = (Button) findViewById(R.id.pass);
								passButton.setVisibility(View.VISIBLE);

								passButton.setOnClickListener(new View.OnClickListener() 
								{
									public void onClick(View arg0) 
									{
										// NEED TO CALL CASEY'S UPDATE METHOD...

										finish();
									}
								});
							}
						}
						// if they don't have the potato
						else 
						{
							// check if won
							if (numPasses.equals("0") || deadline < timenow)  // need to deal with time too
							{
								updatePage("you won the game! Kudos.");
							}
							// else you're waiting
							else
							{
								// you're waiting
								changePage(R.layout.waiting);
							}
						}

					}
				}

			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * updatePage
	 * 
	 * method that will add a game status notification and a back button
	 * on the playgame.xml
	 * 
	 * @param: String message (pretty self-explanatory, takes in the notification message)
	 * @return: void
	 */
	private void updatePage(String message)
	{
		//you win - letting the user know
		TextView userStatus = (TextView) findViewById(R.id.status);
		userStatus.setText(message);

		// should include a button to go back...
		Button backButton = (Button) findViewById(R.id.goback);
		backButton.setVisibility(View.VISIBLE);

		backButton.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) 
			{
				finish();
			}
		});
	}
	
	/*
	 * changePage
	 * 
	 * method that will change the ContentView to a new xml page
	 * 
	 * @param
	 * @return: void
	 */
	private void changePage(int r_id)
	{
		setContentView(r_id);
		
		// should include a button to go back...
		Button backButton = (Button) findViewById(R.id.goback);
		backButton.setVisibility(View.VISIBLE);

		backButton.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0) 
			{
				finish();
			}
		});
	}

}