package com.cs50.hotpotato;

import com.cs50.hotpotato.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShowGame extends Activity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showgame);
		
        TextView printname = (TextView) findViewById(R.id.username);
		
        Intent i = getIntent();

        // Receiving the Data
        String name = i.getStringExtra("name");
        
        // Displaying Received data
        printname.setText(name);

        // Binding Click event to Button
        Button closeButton = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() 
        {
			public void onClick(View arg0) 
			{
				finish();
			}
		});
    }
}
