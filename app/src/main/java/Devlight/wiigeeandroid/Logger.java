package Devlight.wiigeeandroid;

import android.util.Log;
import android.widget.TextView;

import java.util.Vector;

public class Logger {
	
	TextView textView;
	int maxLines;
	
	Vector<String> logs;
	
	public Logger(TextView textView, int maxLines) {
		this.textView = textView;
		this.maxLines = maxLines;
		
		logs = new Vector<String>();
	}
	
	public void addLog(String msg) {
		
		logs.add(msg);
		
		while(logs.size() > maxLines) {
			logs.remove(0);
		}
		
		Log.i("LOGGER", msg);
		
		textView.setText(toText());
	}
	
	public String toText() {
		StringBuilder sb = new StringBuilder();
		for(String msg : logs) {
			sb.append(msg);
			sb.append('\n');
		}
		return new String(sb);
	}
	public void clear(){
		textView.setText(null);
		logs.clear();
	}

}
