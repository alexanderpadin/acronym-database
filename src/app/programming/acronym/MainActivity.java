package app.programming.acronym;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

	EditText searchBar;
	boolean EMPTY = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchBar = (EditText) findViewById(R.id.id_search_EditText);
        
        searchBar.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if(searchBar.getText().toString().length() > 0) {
                	searchBar.setCompoundDrawablesWithIntrinsicBounds( R.drawable.search_icon, 0, R.drawable.cancel, 0);
                 } else {
                 	searchBar.setCompoundDrawablesWithIntrinsicBounds( R.drawable.search_icon, 0, 0, 0);	 
                 }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        }); 
        
        searchBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() >= (searchBar.getRight() - searchBar.getCompoundDrawables()[2].getBounds().width())) {
                    	searchBar.setText("");
                 }
                }
                return false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
