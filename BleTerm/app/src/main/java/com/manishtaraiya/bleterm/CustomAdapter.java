package com.manishtaraiya.bleterm;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<String> {

    String[] servuuid;
    String[] chruuid;
    Context context;
    LayoutInflater inflater;
    public CustomAdapter(Context context, String[] servuuid,String[] chruuid) {
        super(context,R.layout.custom_adapter, servuuid);
        this.servuuid = servuuid;
        this.chruuid = chruuid;
        this.context =context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_adapter,null);

        }

        TextView serviceuuid = (TextView) convertView.findViewById(R.id.serviceuuid);
        TextView charuuid = (TextView) convertView.findViewById(R.id.charuuid);

        serviceuuid.setText(servuuid[position]);
        charuuid.setText(chruuid[position]);

        return convertView;
    }
}
