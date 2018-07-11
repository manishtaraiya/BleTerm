package com.manishtaraiya.bleterm.Scan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.manishtaraiya.bleterm.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.itangqi.waveloadingview.WaveLoadingView;
import mehdi.sakout.fancybuttons.FancyButton;

public class CustomListAdapter extends BaseAdapter /*extends ArrayAdapter<String>*/ {
    private customButtonListener customListner;


    public interface customButtonListener {
        public void onButtonClickListner(int position, String name, String mac, int rssi);

        public void onRawDataClickListner(int position, String raw_data);
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }

    private Context context;


    private List<SearchElement> searchElements = null;
    private ArrayList<SearchElement> arraylist;

    public CustomListAdapter(Context context, List<SearchElement> searchElements) {

        this.arraylist = new ArrayList<SearchElement>();
        this.searchElements = searchElements;
        this.arraylist.addAll(searchElements);

        this.context = context;
    }

    public void update(List<SearchElement> searchElements) {
        arraylist.clear();
        this.arraylist.addAll(searchElements);

        /*for(SearchElement se : searchElements){
            if(!arraylist.contains(se))
                arraylist.add(se);
        }*/

        this.searchElements = searchElements;
        notifyDataSetChanged();
    }

    // Filter Class
    public void filter(String f_name, String f_mac, int f_rssi) {
        f_name = f_name.toLowerCase(Locale.getDefault());
        f_mac = f_mac.toLowerCase(Locale.getDefault());
        ArrayList<SearchElement> mylist = new ArrayList<SearchElement>();
        ArrayList<SearchElement> mylist1 = new ArrayList<SearchElement>();


        searchElements.clear();
        if (f_name.length() == 0 && f_mac.length() == 0 && f_rssi == 0) {
            searchElements.addAll(arraylist);
        } else {

            for (SearchElement se : arraylist) {
                if(se.getName() !=null) {
                    if (se.getName().toLowerCase(Locale.getDefault()).contains(f_name)) {
                        mylist.add(se);
                    }
                }else if(f_name.equals(""))
                    mylist.add(se);
            }

            for (SearchElement se : mylist) {
                if(se.getMac_address() !=null) {
                    if (se.getMac_address().toLowerCase(Locale.getDefault()).contains(f_mac)) {
                        mylist1.add(se);
                    }
                }else if(f_mac.equals(""))
                    mylist1.add(se);
            }


            for (SearchElement se : mylist1) {
                if (se.getRssi_value() >= f_rssi) {
                    searchElements.add(se);
                }
            }


        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {


        return searchElements.size();
    }

    @Override
    public Object getItem(int i) {
        return searchElements.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        /*int rssi_count = searchElements.get(position).getRssi_value();
        if (rssi_count == 1000) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.native_adview, null);
            NativeExpressAdView adView = (NativeExpressAdView) convertView.findViewById(R.id.adView_native);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);


        } else {*/

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_adapter, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.childTextView);
            viewHolder.raw_data = (TextView) convertView.findViewById(R.id.raw_data_text);
            viewHolder.mac_id_text = (TextView) convertView.findViewById(R.id.mac_id_text);

            viewHolder.mWaveLoadingView = (WaveLoadingView) convertView.findViewById(R.id.waveLoadingView);

            viewHolder.connect_button = new FancyButton(context);

            viewHolder.connect_button = (FancyButton) convertView.findViewById(R.id.connect_button);
            convertView.setTag(viewHolder);


        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.mWaveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
        final int rssi = searchElements.get(position).getRssi_value();   //rssi_value.get(position);
        //final int rssi_int = Integer.parseInt(rssi);
        viewHolder.mWaveLoadingView.setProgressValue(rssi + 100);
        viewHolder.mWaveLoadingView.setCenterTitle((rssi) + " db");
        viewHolder.mWaveLoadingView.setBorderWidth(2);
        viewHolder.mWaveLoadingView.setAmplitudeRatio(20);
        final String device_name = searchElements.get(position).getName();//name.get(position);

        viewHolder.text.setText(device_name);
        final String mac_id = searchElements.get(position).getMac_address();//mac_address.get(position);
        viewHolder.mac_id_text.setText(mac_id);

        viewHolder.connect_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (customListner != null) {
                    customListner.onButtonClickListner(position, device_name, mac_id, rssi);
                }

            }
        });

        viewHolder.raw_data.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (customListner != null) {
                    final String raw_data = searchElements.get(position).getRaw_data();
                    customListner.onRawDataClickListner(position, raw_data);
                }

            }
        });


    return convertView;
}

public class ViewHolder {
    TextView text, raw_data, mac_id_text;
    WaveLoadingView mWaveLoadingView;
    FancyButton connect_button;
}
}