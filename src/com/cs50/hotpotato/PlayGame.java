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
					int endDeadline = temp.indexOf("numPasses");

					// if there is no deadline time yet, game hasn't started
					if (startDeadline == endDeadline)
					{
						// letting the user know
				        TextView waitStatus = (TextView) findViewById(R.id.status);
						waitStatus.setText("Game has not begun yet! Check back later.");
						
						// should include a button to go back...
						Button backButton = (Button) findViewById(R.id.goback);
						
						backButton.setOnClickListener(new View.OnClickListener() 
						{
							public void onClick(View arg0) 
							{
								finish();
							}
						});
					}
					
					else
					{
						// the deadline is... (in seconds since Unix Epoch)
						String sdeadline = temp.substring(startDeadline, endDeadline);
						
						int deadline = Integer.parseInt(sdeadline);
						
						
						// check the current user
						int startUser = temp.indexOf("currentUser:") + 13;
						String tempUser = temp.substring(startUser);
						int endUser = tempUser.indexOf(" deadline ");

						// get current user
						String currentUser = tempUser.substring(0, endUser);
						
	/*			        TextView cuser = (TextView) findViewById(R.id.currentuser);
				        String temp2 = cuser.toString();
						cuser.setText("current user: " + temp2);*/


						// get number of passes remaining
						int startPasses = temp.indexOf("numPasses") + 11;
						String tempPasses = temp.substring(startPasses);
						int endPasses = tempPasses.indexOf(" deadline ");

						// get number of passes
						String numPasses = tempPasses.substring(0, endPasses);

						// get current timestamp
						// get current timestamp (in seconds from Unix Epoch)
						long timetemp = System.currentTimeMillis() / 1000;
						int timenow = (int) timetemp;

						

						// check if (logged in) user has potato (aka is currentUser) 
						if (userID.equals(currentUser))
						{
							// check if lost
							if (numPasses.equals("0") || deadline < timenow)
							{
								//you lose
							}
							else
							{
								// else have potato, allow it to be passed - so we have to make button visible
								Button passButton = (Button) findViewById(R.id.pass);
								passButton.setVisibility(View.VISIBLE);
								
								passButton.setOnClickListener(new View.OnClickListener() 
								{
									public void onClick(View arg0) 
									{
										finish();
										
										/*Intent nextScreen = new Intent(getApplicationContext(), PlayGame.class);
										
										//Sending data to another Activity
										nextScreen.putExtra("userID", userID);
										nextScreen.putExtra("gamename", gameName);
										
										// starting new activity
										startActivity(nextScreen);*/

									}
								});

								
								
								TextView haveIt = new TextView(this);
								haveIt.setText("You have the potato! Pass it!");
								setContentView(haveIt);
							}
						}
						// if they don't have the potato
						else 
						{
							// check if won
							if (numPasses.equals("0") || deadline < timenow)  // need to deal with time too
							{
								//you win
							}
							// else you're waiting
							else
							{
								// you're waiting
								setContentView(R.layout.waiting);
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


}