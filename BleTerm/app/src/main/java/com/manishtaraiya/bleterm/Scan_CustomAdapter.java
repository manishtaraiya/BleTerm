package com.manishtaraiya.bleterm;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.itangqi.waveloadingview.WaveLoadingView;

public class Scan_CustomAdapter extends ArrayAdapter<String> {

    ArrayList<String> name;
    ArrayList<String> address;
    ArrayList<Integer> rssi;
    private int lastPosition = -1;
    Context context;
    LayoutInflater inflater;
    public Scan_CustomAdapter(Context context, ArrayList<String> name, ArrayList<String> address, ArrayList<Integer> rssi) {
        super(context,R.layout.scan_customadapter, (List<String>) name);
        this.name = name;
        this.address = address;
        this.rssi=rssi;
        this.context =context;
    }

    public void update(ArrayList<String> name, ArrayList<String> address, ArrayList<Integer> rssi){

        this.name = name;
        this.address = address;
        this.rssi=rssi;
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);

        if(convertView==null)
        {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.scan_customadapter,null);
        }

        //convertView.setAnimation(animation);
        lastPosition = position;
        TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
        TextView device_address = (TextView) convertView.findViewById(R.id.device_address);


        WaveLoadingView mWaveLoadingView = (WaveLoadingView) convertView.findViewById(R.id.waveLoadingView);
        mWaveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        //mWaveLoadingView.setTopTitle("Top Title");
        //mWaveLoadingView.setCenterTitleColor(Color.GRAY);
        //mWaveLoadingView.setBottomTitleSize(18);
        //mWaveLoadingView.setProgressValue(80);
        //mWaveLoadingView.setBorderWidth(10);
        //mWaveLoadingView.setAmplitudeRatio(60);
        //mWaveLoadingView.setWaveColor(Color.GRAY);
        //mWaveLoadingView.setBorderColor(Color.GRAY);
        mWaveLoadingView.setProgressValue(-rssi.get(position));
        mWaveLoadingView.setCenterTitle(rssi.get(position)+" db");

        device_name.setText(name.get(position));
        device_address.setText(address.get(position));

        return convertView;
    }
}
