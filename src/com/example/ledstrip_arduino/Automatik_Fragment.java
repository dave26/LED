package com.example.ledstrip_arduino;


import java.sql.Date;
import java.text.DateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
//import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;


import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Automatik_Fragment extends Fragment implements OnSeekBarChangeListener{
	
	private MainActivity Main_Activity;
//	public void setActivity(Activity a)	{Main_Activity=a;}
//	
	private static final String TAG = "MultiColorLamp";	
	private static final String AUTOMATIK_ACTIVE = "a_a";
	
	// change this to your Bluetooth device address 
	//private static final String DEVICE_ADDRESS =  "00:14:02:26:01:10";

	public static final String EXTRA_MESSAGE = null;
	final int DELAY = 150;
	private SeekBar redSB;
	private SeekBar greenSB;
	private SeekBar blueSB;
	private View colorIndicator;
	private TextView tv_automatik_bluetoothstatus;
	private EditText Andimmuhrzeit_Stunde;
	private EditText Andimmuhrzeit_Minute;
	private EditText Periode_Ausdimmen;
	private EditText Periode_Andimmen;
	private Button btn_automatik_onoff;
	private Button btn_ausdimmen;
	private int red, green, blue;
	private long lastChange;
	private boolean automatik_onoff=false;
	private String andimmuhrzeit="";
	
	private Intent intentactive = new Intent();
	
	public Automatik_Fragment(){
		
	}


	@Override
	public void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	}
	
    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.automatik_fragment,
    			container, false);
    			
    	intentactive.setAction(Main_Activity.BROADCASTACTION);
    	intentactive.putExtra("extra", Main_Activity.BROADCASTEXTRA_ACTIVE);

		// get references to views defined in our main.xml layout file
		redSB = (SeekBar) rootView.findViewById(R.id.SeekBarRed);
		greenSB = (SeekBar) rootView.findViewById(R.id.SeekBarGreen);
		blueSB = (SeekBar) rootView.findViewById(R.id.SeekBarBlue);
		colorIndicator = rootView.findViewById(R.id.ColorIndicator);
		Andimmuhrzeit_Stunde=(EditText)rootView.findViewById(R.id.editText_Andimmuhrzeit_Stunde);
		Andimmuhrzeit_Minute=(EditText)rootView.findViewById(R.id.editText_Andimmuhrzeit_Minute);
		Periode_Ausdimmen=(EditText)rootView.findViewById(R.id.editText_periode_ausdimmen);
		Periode_Ausdimmen.setText("mm");
		Periode_Andimmen=(EditText)rootView.findViewById(R.id.editText_periode_andimmen);
		Periode_Andimmen.setText("mm");
		btn_automatik_onoff=(Button)rootView.findViewById(R.id.button_automatik_onoff);
		tv_automatik_bluetoothstatus=(TextView)rootView.findViewById(R.id.textview_automatik_bluetoothstatus);
		btn_ausdimmen=(Button)rootView.findViewById(R.id.button_ausdimmen);
		
		// register listeners
		redSB.setOnSeekBarChangeListener(this);
		greenSB.setOnSeekBarChangeListener(this);
		blueSB.setOnSeekBarChangeListener(this);
		

		InputFilter filterMax2Digits = new InputFilter() {
		    @Override
		    public CharSequence filter(CharSequence source, int start, int end,
		            Spanned dest, int dstart, int dend) {

		        if (source instanceof SpannableStringBuilder) {
		            SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
		            for (int i = end - 1; i >= start; i--) { 
		                char currentChar = source.charAt(i);
		                 if (!Character.isDigit(currentChar)|| dstart<2 || dend < 2) {    
		                     sourceAsSpannableBuilder.delete(i, i+1);
		                 }     
		            }
		            return source;
		        } else {
		            StringBuilder filteredStringBuilder = new StringBuilder();
		            for (int i = start; i < end; i++) { 
		                char currentChar = source.charAt(i);
		                if (Character.isDigit(currentChar)) { 
		                	if(dstart<2 || dend <2)
		                		filteredStringBuilder.append(currentChar);
		                }     
		            }
		            return filteredStringBuilder.toString();
		        }
		    }
		};
		
		Andimmuhrzeit_Stunde.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(!Andimmuhrzeit_Stunde.getText().toString().contains("h") && !Andimmuhrzeit_Minute.getText().toString().contains("m")){
					andimmuhrzeit = Andimmuhrzeit_Stunde.getText().toString()+":"+Andimmuhrzeit_Minute.getText().toString();
					updateall();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			});
		Andimmuhrzeit_Stunde.setFilters(new InputFilter[] { filterMax2Digits });
		Andimmuhrzeit_Minute.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(!Andimmuhrzeit_Stunde.getText().toString().contains("h") && !Andimmuhrzeit_Minute.getText().toString().contains("m")){
					andimmuhrzeit = Andimmuhrzeit_Stunde.getText().toString()+":"+Andimmuhrzeit_Minute.getText().toString();
					updateall();
				}				
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			});
		Andimmuhrzeit_Minute.setFilters(new InputFilter[] { filterMax2Digits });
		
		Periode_Ausdimmen.addTextChangedListener(new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			updateall();
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		Periode_Ausdimmen.setFilters(new InputFilter[] { filterMax2Digits });
		
		Periode_Andimmen.addTextChangedListener(new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			updateall();
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		Periode_Andimmen.setFilters(new InputFilter[] { filterMax2Digits });
		
		btn_automatik_onoff.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(Main_Activity.connected){					
					if(!automatik_onoff)
						Main_Activity.active="automatik";
					else
						Main_Activity.active="";
					
					Main_Activity.sendBroadcast(intentactive);
				}
				else
					Toast.makeText(Main_Activity,"nicht verbunden -> aktivieren nicht m�glich",
			                 Toast.LENGTH_LONG).show();     
				
				
			}
		});
		btn_ausdimmen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(automatik_onoff){
					if(Main_Activity.connected){					
						String h = "#baus_;";
						Main_Activity.mBluetoothService.write(h.getBytes());
					}
					else
						Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
			                 Toast.LENGTH_LONG).show();  
				}
				else
					Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
			                 Toast.LENGTH_LONG).show(); 
			}
		});
		
		Main_Activity.registerReceiver(bReceiver, new IntentFilter(Main_Activity.BROADCASTACTION)); 
	
        return rootView;
    }
    
    

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		this.Main_Activity = (MainActivity)activity;
	}
	@Override
	public void onResume(){
		//RefreshData();
		super.onResume();
	}
    
	@Override
	public void onStart() {
		super.onStart();

		// load last state
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main_Activity);
        red = prefs.getInt("red_a", 0);
        green = prefs.getInt("green_a", 0);
        blue = prefs.getInt("blue_a", 0);
        
        // set seekbars and feedback color according to last state
        redSB.setProgress(red);
        greenSB.setProgress(green);
        blueSB.setProgress(blue);
        colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));
        
	}

	@Override
	public void onStop() {
		super.onStop();
		// save state
		PreferenceManager.getDefaultSharedPreferences(Main_Activity)
			.edit()
				.putInt("red_a", red)
				.putInt("green_a", green)
				.putInt("blue_a", blue)
			.commit();
		
			}
	
	final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String extra = intent.getStringExtra("extra");
            if(action.startsWith(Main_Activity.BROADCASTACTION)){
		            if(extra.startsWith(Main_Activity.BROADCASTEXTRA_ACTIVE)){
		            	if(Main_Activity.active=="automatik"){
		            		automatik_onoff=true;
		            		btn_automatik_onoff.setText("automatische Steuerung deaktivieren");		            		
	        				if(Main_Activity.connected){					
	        					String h = "#auto_;";
	        					Main_Activity.mBluetoothService.write(h.getBytes());
	        				}
	        				else
	        					Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
	        			                 Toast.LENGTH_LONG).show();     
		        			
		            		updateall(); //alle eingegebenen Werte werden gesendet, sind nicht alle Felder geschrieben wird nichts gesendet
		            	}
		            	else{
		            		automatik_onoff=false;
		            		btn_automatik_onoff.setText("automatische Steuerung aktivieren");
		            	}
		            }
		            else if(extra.startsWith(Main_Activity.BROADCASTEXTRA_BTSTATUS)){
		            	tv_automatik_bluetoothstatus.setText(Main_Activity.bluetoothstatus);
		            }
		        }
        }
};



	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// do not send to many updates, Arduino can't handle so much
		if (System.currentTimeMillis() - lastChange > DELAY ){
			updateState(seekBar);
			lastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		lastChange = System.currentTimeMillis();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {
		switch (seekBar.getId()){
			case R.id.SeekBarRed:
				red = seekBar.getProgress();
				updateRed();
				break;
			case R.id.SeekBarGreen:
				green = seekBar.getProgress();
				updateGreen();
				break;
			case R.id.SeekBarBlue:
				blue = seekBar.getProgress();
				updateBlue();
				break;
		}
		// provide user feedback
		colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));
		}
		private void updateAllColors() {
		// send state to Arduino
		updateRed();
		updateGreen();
		updateBlue();
		}
		private void updateRed(){
			
			if(automatik_onoff){
				if(Main_Activity.connected){					
					String h = "#red_"+red+";";
					Main_Activity.mBluetoothService.write(h.getBytes());
				}
				else
					Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
			                 Toast.LENGTH_LONG).show();     
			}
			else
				Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
		                 Toast.LENGTH_LONG).show(); 
		}
		
		private void updateGreen(){
			if(automatik_onoff){
				if(Main_Activity.connected){					
					String h = "#green_"+green+";";
					Main_Activity.mBluetoothService.write(h.getBytes());
				}
				else
					Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
		                 Toast.LENGTH_LONG).show();  
			}
			else
				Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
		                 Toast.LENGTH_LONG).show(); 
		}
		
		private void updateBlue(){
			if(automatik_onoff){
				if(Main_Activity.connected){					
					String h = "#blue_"+blue+";";
					Main_Activity.mBluetoothService.write(h.getBytes());
				}
				else
					Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
			                 Toast.LENGTH_LONG).show(); 		
			}
			else
				Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
	                 Toast.LENGTH_LONG).show(); 
		}
		
		private void updateAndimmuhrzeit()
		{
			if(andimmuhrzeit!=""){
				if(automatik_onoff){
					if(Main_Activity.connected){						
						String s = "#tan_"+andimmuhrzeit+";";
						Main_Activity.mBluetoothService.write(s.getBytes());
					}
					else
						Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
				                 Toast.LENGTH_LONG).show(); 
				}
				else
					Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
		                 Toast.LENGTH_LONG).show(); 	
			}
		
		
		}
		
		private void updatePeroideAusdimmen()
		{			
			if(Periode_Ausdimmen.getText().toString()!="mm"){
				if(automatik_onoff){
					if(Main_Activity.connected){
						String s = "#paus_"+Periode_Ausdimmen.getText().toString()+";";
						Main_Activity.mBluetoothService.write(s.getBytes());
					}
					else
						Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
				                 Toast.LENGTH_LONG).show(); 
				}
				else
					Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
		                 Toast.LENGTH_LONG).show(); 
			}
		}
		
		private void updatePeroideAndimmen()
		{
			if(Periode_Andimmen.getText().toString()!="mm"){
				if(automatik_onoff){
					if(Main_Activity.connected){
						String s = "#pan_"+Periode_Andimmen.getText().toString()+";";
						Main_Activity.mBluetoothService.write(s.getBytes());
					}
					else
						Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
				                 Toast.LENGTH_LONG).show(); 
				}
				else
					Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
		                 Toast.LENGTH_LONG).show(); 
			}
		
		}
		
		private void updateall(){//alle eingegebenen Werte werden gesendet, sind nicht alle Felder geschrieben wird nichts gesendet			
			if(automatik_onoff){
				if(Main_Activity.connected){
					sendTime();
					updateAllColors();
					updateAndimmuhrzeit();
					updatePeroideAusdimmen();
					updatePeroideAndimmen();
				}
				else
					Toast.makeText(Main_Activity,"nicht verbunden -> senden nicht m�glich",
			                 Toast.LENGTH_LONG).show(); 
			}
			else
				Toast.makeText(Main_Activity,"automatische Steuerung ist deaktiviert",
	                 Toast.LENGTH_LONG).show(); 			
		}
		
		private void sendTime(){
			long currentTime = System.currentTimeMillis();
			DateFormat[] formats = new DateFormat[] {							   
					   DateFormat.getTimeInstance()
					 };
			String date = "#time_"+formats[0].format(new Date(currentTime))+";";
			Main_Activity.mBluetoothService.write(date.getBytes());
		}
	
}
