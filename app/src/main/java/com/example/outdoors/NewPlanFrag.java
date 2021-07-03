package com.example.outdoors;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class NewPlanFrag extends Fragment {

    View view;

    UserList userListInst = UserList.getInstance();

    User currUser = userListInst.getCurrentUser();

    private MapView map = null;

    IMapController mapController = null;

    MyLocationNewOverlay myLocationOverlay;

    Button dateButt;
    Button timeButt;

    EditText titleET;

    String planID = null;

    Plan plan = null;

    RecViewAdapter recAdapter;

    PlansActivity act;

    ArrayList<String> people = new ArrayList();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.new_plan_frag, container, false);

        map = (MapView) view.findViewById(R.id.map);
        map.setMultiTouchControls(true);

        mapController = map.getController();

        act = (PlansActivity)getActivity();

        if(getArguments() != null){
            planID = getArguments().getString("planID");
//            plan = act.getPlan(planID);
            plan = userListInst.getPlan(planID);
            Log.w("PLAN FRAG", planID);
            Log.w("PLAN FRAG", String.valueOf(plan));
        }


        if (mapController != null) {
            GeoPoint startPoint;
            mapController.setZoom(16.0);
            startPoint = plan != null ? new GeoPoint(plan.lat, plan.lon) : new GeoPoint(currUser.lat, currUser.lon);
            mapController.setCenter(startPoint);
        }

        setMyLocationOverlay();
        if(plan == null){
            setOnMapClickOverlay();
        }

        TextView newPlanTV = view.findViewById(R.id.newPlanTitleTW);
        titleET = (EditText) view.findViewById(R.id.newPlanTitleInput);

        dateButt = (Button) view.findViewById(R.id.newPlanDateButton);
        timeButt = (Button) view.findViewById(R.id.newPlanTimeButton);

        if(plan!=null) {
            TextView planTitleTV = view.findViewById(R.id.planDetailTitleTV);
            TextView planCreatorTV = view.findViewById(R.id.planDetailCreatorTV);
            TextView planDateTV = view.findViewById(R.id.planDetailDateTV);
            planTitleTV.setText(plan.planTitle);
            planCreatorTV.setText(userListInst.getUser(plan.createdBy).getUsername());
            planDateTV.setText(userListInst.getFormatDate(plan.date) + " " + userListInst.getFormatTime(plan.date));
            planTitleTV.setVisibility(View.VISIBLE);
            planCreatorTV.setVisibility(View.VISIBLE);
            planDateTV.setVisibility(View.VISIBLE);
            titleET.setVisibility(View.GONE);
            newPlanTV.setVisibility(View.GONE);
            dateButt.setVisibility(View.GONE);
            timeButt.setVisibility(View.GONE);
            people = plan.confirmed;
        }

        RecyclerView recView = (RecyclerView) view.findViewById(R.id.newPlanRecView);

        dateButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickerDialog("date");
            }
        });

        timeButt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showPickerDialog("time");
            }
        });


//        RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), people, RecViewAdapter.CARD_VIEW);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recAdapter = new RecViewAdapter(getActivity(), people, RecViewAdapter.CARD_VIEW, null);
        recView.setAdapter(recAdapter);
//        recView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recView.setLayoutManager(layoutManager);


        return view;
    }

    public void getTitle(){
        ((PlansActivity)getActivity()).setPlanTitle(titleET.getText().toString());
    }

    private void setMyLocationOverlay(){
        this.myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity()), map);
        this.myLocationOverlay.enableFollowLocation();
        this.myLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.myLocationOverlay);
    }

    private void setOnMapClickOverlay(){
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                double lat = p.getLatitude();
                double lon = p.getLongitude();
                removeMarker("pin");
                Marker pin = new Marker(map);
                pin.setId("pin");
                pin.setPosition(new GeoPoint(lat, lon));
                pin.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                pin.setIcon(getResources().getDrawable(R.drawable.ic_baseline_place_24));
                map.getOverlays().add(pin);
                map.invalidate();
                ((PlansActivity)getActivity()).setCoords(lat, lon);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay overlayEvents = new MapEventsOverlay(receiver);
        map.getOverlays().add(overlayEvents);
    }

    private void removeMarker(String id){
        for(Overlay overlay : map.getOverlays()){
            if(overlay instanceof Marker && ((Marker) overlay).getId().equals(id)){
                map.getOverlays().remove(overlay);
                map.invalidate();
            }
        }
    }


    public void showPickerDialog(String which) {
        DialogFragment newFragment;
        if(which.equals("date")){
            newFragment = new DatePickerFragment();
        }else{
            newFragment = new TimePickerFragment();
        }

        newFragment.show(getActivity().getSupportFragmentManager(), "dateTimePicker");
    }

    public void setDateLabel(String label){
        dateButt.setText(label);
    }

    public void setTimeLabel(String label){
        timeButt.setText(label);
    }

    public void updateList(){
        people.addAll(people.size()-1, ((PlansActivity)getActivity()).getInvites());
        recAdapter.notifyDataSetChanged();
    }


    public void closeFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }






    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            ((PlansActivity)getActivity()).setDate(year,month, day);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            // Do something with the time chosen by the user
            ((PlansActivity)getActivity()).setTime(hour,minute);
        }
    }




}
