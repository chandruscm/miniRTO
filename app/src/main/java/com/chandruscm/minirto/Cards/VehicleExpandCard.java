package com.chandruscm.minirto.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chandruscm.minirto.R;
import com.chandruscm.minirto.Models.Vehicle;

import it.gmariotti.cardslib.library.internal.CardExpand;

public class VehicleExpandCard extends CardExpand
{
    private Vehicle vehicle;

    public VehicleExpandCard(Context context, Vehicle vehicle)
    {
        super(context, R.layout.vehicle_card_expand_layout);
        this.vehicle = vehicle;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view)
    {
        TextView name = (TextView) parent.findViewById(R.id.vehicle_name);
        TextView fuelType = (TextView) parent.findViewById(R.id.vehicle_fuel);
        TextView cc = (TextView) parent.findViewById(R.id.vehicle_cc);
        TextView engine = (TextView) parent.findViewById(R.id.vehicle_engine);
        TextView chasis = (TextView) parent.findViewById(R.id.vehicle_chasis);
        TextView owner = (TextView) parent.findViewById(R.id.vehicle_owner);
        TextView location = (TextView) parent.findViewById(R.id.vehicle_location);
        TextView expiry = (TextView) parent.findViewById(R.id.vehicle_expiry);

        name.setSelected(true);
        name.setText(vehicle.getName());
        fuelType.setText(vehicle.getFuel());
        cc.setText(vehicle.getCc());
        engine.setText(vehicle.getEngine());
        chasis.setText(vehicle.getChassis());
        owner.setText(vehicle.getOwner());
        location.setText(vehicle.getLocation());
        expiry.setText(vehicle.getExpiry());
    }
}
