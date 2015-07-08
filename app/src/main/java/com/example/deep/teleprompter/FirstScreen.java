package com.example.deep.teleprompter;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class FirstScreen extends ActionBarActivity {

    public static final String TELEPROMPT_TEXT = "TelePromptText";
    EditText editText;

    private static final String LOGTAG = "FirstScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);

        editText = (EditText) findViewById(R.id.telepromptText);
    }

    public void startTeleprompt(View view){

        if(editText.getText() == null || editText.getText().equals("")) {

            Toast.makeText(this, "No text to teleprompt", Toast.LENGTH_SHORT).show();
        } else {

            Intent intent = new Intent(this, MainActivity.class);
            Log.v(LOGTAG, editText.getText().toString());
            intent.putExtra(TELEPROMPT_TEXT, editText.getText().toString());
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
