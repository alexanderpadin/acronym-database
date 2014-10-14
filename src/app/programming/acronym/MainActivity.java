package app.programming.acronym;

//Add functionality to report acrnym

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	Adapter adapter;
	
	EditText searchBar;
	ImageButton cancelButton;
	String DETAILS = "";
	Context ctx = this;
	Map<String, String> dataMap;
	ListView list;
	TextView no_sugg;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                Typeface typeface = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
            	title.setTypeface(typeface);
            }
        }
        
        if(!haveData()) {
        	new generateData().execute();
        } else {
        	prepareData();
        	new checkUpdates().execute();
        }
        list = (ListView) findViewById(R.id.listView_sugg);
        searchBar = (EditText) findViewById(R.id.id_search_EditText);
        cancelButton = (ImageButton) findViewById(R.id.cancelButton);
        no_sugg = (TextView) findViewById(R.id.not_sugg);
        
        searchBar.addTextChangedListener(new TextWatcher(){
            @SuppressLint({ "NewApi", "DefaultLocale" }) 
            public void afterTextChanged(Editable s) {
                if(searchBar.getText().toString().length() > 0) {
                	cancelButton.setImageResource(R.drawable.cancel);
                	final Map<String, String> temMap = new HashMap<String, String>();
                	for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                		if(entry.getKey().toUpperCase().contains(searchBar.getText().toString().toUpperCase())) {
                			temMap.put(entry.getKey(), entry.getValue());
                		}
                	}
                	
                	if (!temMap.isEmpty()){
                		list.setVisibility(View.VISIBLE);
                		showSuggestions(temMap);
                		no_sugg.setVisibility(View.GONE);
                		
                	}
                 } else {
                	 cancelButton.setImageResource(R.drawable.trasnparent);
                	 no_sugg.setVisibility(View.VISIBLE);
                	 list.setVisibility(View.GONE);
                 }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        }); 
        cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchBar.setText("");
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.add:
            	openAddDlg();
                return true;
            case R.id.report:
            	openReportDlg();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void prepareData() {
    	dataMap = new HashMap<String, String>();
    	try {
			JSONObject jObject = new JSONObject(readFileData("data.txt"));
			JSONArray jArray = jObject.getJSONArray("data"); 
			for (int i=0; i < jArray.length(); i++) {
			    try {
			        JSONObject oneObject = jArray.getJSONObject(i);
			        dataMap.put(oneObject.getString("acronym"), oneObject.getString("definition"));
			    } catch (JSONException e) {}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    private boolean haveData() {
    	File file = getBaseContext().getFileStreamPath("data.txt");
    	return (file.exists());
    }
    
    private void createFile(String result, String filename) {
	    	FileOutputStream fos;
			try {
				fos = openFileOutput(filename, Context.MODE_PRIVATE);
				fos.write(result.getBytes());
		    	fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    }

    private String readFileData(String filename) {
    	String data;
    	try {
    	    BufferedReader inputReader = new BufferedReader(
    	    		new InputStreamReader(openFileInput(filename)));
    	    String inputString;
    	    StringBuffer stringBuffer = new StringBuffer();                
    	    while ((inputString = inputReader.readLine()) != null) {
    	        stringBuffer.append(inputString + "\n");
    	    }
    	    data = stringBuffer.toString();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	    data = null;
    	}
    	return data;
    	

    }
    
    private boolean sameVersions(String localData, String remoteData){
    	JSONObject localjObject;
    	JSONObject remotejObject;
    	int localVersion = -1;
    	int remoteVersion = -1;
    	
    	try {
			localjObject = new JSONObject(localData);
			remotejObject = new JSONObject(remoteData);
			localVersion = localjObject.getInt("version");
			remoteVersion = remotejObject.getInt("version");
			DETAILS = remotejObject.getString("details");
		} catch (JSONException e) {
			localjObject = null;
	    	remotejObject = null;
			e.printStackTrace();
		}
    	
		return (localVersion == remoteVersion);
		}
    
    @SuppressLint("InflateParams") 
    private void notifyUser() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	builder.setTitle("New Database Update");
    	LayoutInflater inflater = this.getLayoutInflater();

    	View dialogView = inflater.inflate(R.layout.notification, null);
    	builder.setCancelable(false); ///////////////////////////////////////
    	builder.setView(dialogView)
	    	.setPositiveButton("Update", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                new generateData().execute();
	            }
	        })
	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	prepareData();
	                //Dismiss.
	            }
	        });
    	
    	TextView details = (TextView) dialogView.findViewById(R.id.details_text);
    	details.setText(DETAILS);
    	
        builder.show();
        
     	
    }
    
    public void showSuggestions(Map<String, String> map) {
        final Adapter adapter = new Adapter(map);
        list.setAdapter(adapter);
        
        list.setOnItemClickListener(new OnItemClickListener() {
	    	  @Override
	    	  public void onItemClick(AdapterView<?> parent, View view,
	    	    int position, long id) {	
	    		  String query = adapter.getItem(position).getValue();
	    		  query = query.replaceAll(" ", "%20");
	    		  Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.comsearch?q=whats%20"
	    				  + query));
	    		  startActivity(browserIntent); 
	    	  }
	    	}); 
    }
    
    private void openAddDlg() {
		final Dialog dlg = new Dialog(ctx);
    	dlg.setContentView(R.layout.add);
    	dlg.setCanceledOnTouchOutside(true);
    	dlg.setTitle("Add New Acronym");
    	Button btn =  (Button) dlg.findViewById(R.id.button1);
    	final EditText acrnym = (EditText) dlg.findViewById(R.id.editText1);
    	final EditText def = (EditText) dlg.findViewById(R.id.editText2);
    	
    	
    	btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"alexander.padin91@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Acronym++ ADD");
				i.putExtra(Intent.EXTRA_TEXT   , "" + acrnym.getText() + "  -  " + def.getText());
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(ctx, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
				dlg.dismiss();
			}
    	});
    	dlg.show();
	}
    
    private void openReportDlg() {
		final Dialog dlg = new Dialog(ctx);
    	dlg.setContentView(R.layout.report);
    	dlg.setCanceledOnTouchOutside(true);
    	dlg.setTitle("Report Error");
    	Button btn =  (Button) dlg.findViewById(R.id.button1);
    	final EditText acrnym = (EditText) dlg.findViewById(R.id.editText1);
    	final EditText error = (EditText) dlg.findViewById(R.id.editText2);
    	
    	
    	btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"alexander.padin91@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Acronym++ REPORT");
				i.putExtra(Intent.EXTRA_TEXT   , "Error: " + acrnym.getText() + "  -  " + error.getText());
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(ctx, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
				dlg.dismiss();
			}
    	});
    	dlg.show();
	}
    
    private class generateData extends AsyncTask<Void, Void, String>{
    	private ProgressDialog progressDialog;

      @Override
      protected void onPreExecute() {
          super.onPreExecute();
          progressDialog = new ProgressDialog(ctx);
          progressDialog.setMessage("Updating Database...");
          progressDialog.setIndeterminate(true);
          progressDialog.setCanceledOnTouchOutside(false);
          progressDialog.show();
      }
      @Override
      protected String doInBackground(Void... params) {
      	try {
      		getData getDataObj = new getData();
      		return getDataObj.postData();
      	} catch (Exception e) {
      		return null;
      	}
      }
      @SuppressLint("NewApi") @Override
      protected void onPostExecute(String result) {
          super.onPostExecute(result);   
          progressDialog.hide();
          if (result != null && !result.isEmpty()) {
          	createFile(result, "data.txt");
          	prepareData();
          }    
      }
    }
    
    private class checkUpdates extends AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
        	try {
        		getData getDataObj = new getData();
        		return getDataObj.postData();
        	} catch (Exception e) {
        		return null;
        	}
        }
        @SuppressLint("NewApi") @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);   
            if (result != null && !result.isEmpty()) {
            	if(!sameVersions(readFileData("data.txt"), result)) {
            		notifyUser();
            	}
            }    
        }
    }
}

