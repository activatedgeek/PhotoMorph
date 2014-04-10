package com.example.myinstagram;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class SpinnerAdapter<T> extends ArrayAdapter<T>{
	public SpinnerAdapter(Context context, T[] objects ){
		super(context,android.R.layout.simple_spinner_item, objects);
	}
	
	@Override
	public View getDropDownView(int position, View convertView,  ViewGroup parent){
		View view = super.getView(position, convertView, parent);
		TextView text = (TextView)view.findViewById(android.R.id.text1);
        text.setTextColor(Color.BLACK);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setTextSize((float)20.0);
        text.setHeight(150);
        return view;
	}
}
