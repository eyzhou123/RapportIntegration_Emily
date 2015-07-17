package com.yahoo.inmind.rapport.view;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.yahoo.inmind.rapport.R;
import com.yahoo.inmind.rapport.control.SingletonApp;

public class RapportMainActivity extends ActionBarActivity{

    private ViewHelper viewHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.rapport_activity_main );
        viewHelper = ViewHelper.getInstance( this );
        SingletonApp.getInstance( getApplicationContext() );
    }



    @Override
    protected void onResume(){
        super.onResume();
    }


    public void startReader(View view){
        viewHelper.showCustomizedReader();
    }

    public void getModels(View view){
        viewHelper.getModelDistributions();
    }


    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        viewHelper.exit();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rapport_menu_activity_main, menu);
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
