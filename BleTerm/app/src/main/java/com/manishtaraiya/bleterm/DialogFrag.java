package com.manishtaraiya.bleterm;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


public class DialogFrag extends DialogFragment {


    ListView mylist;
    TextView textView;
    static String [] serviceuuid, charuuid;
    String title;
    Communicator communicator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communicator = (Communicator) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serviceuuid = getArguments().getStringArray("service");
        charuuid = getArguments().getStringArray("char");
        title = getArguments().getString("title");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.dialog_frag, container, false);
        mylist = (ListView) view.findViewById(R.id.fraglist);
        textView =(TextView) view.findViewById(R.id.fragtitle);
        //getDialog().setTitle(title);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);


        textView.setText(title);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(),serviceuuid,charuuid);
        mylist.setAdapter(customAdapter);

        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                //Toast.makeText(getActivity(), serviceuuid[position], Toast.LENGTH_SHORT).show();
                communicator.onDialogMessage(serviceuuid[position], charuuid[position]);
            }
        });

       return view;

    }

    interface Communicator {
        void onDialogMessage(String service, String chars);
    }

}
