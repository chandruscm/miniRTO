package com.chandruscm.minirto.Cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chandruscm.minirto.R;
import it.gmariotti.cardslib.library.internal.Card;

public class VehicleCard extends Card
{
    private String number;
    private TextView textView;

    public VehicleCard(Context context, String number)
    {
        this(context, R.layout.vehicle_card_layout);
        setNumber(number);
    }

    public void setNumber(String number)
    {
        if(Character.isLetter(number.charAt(5)))
            this.number = number.substring(0,2) + " " +  number.substring(2,4) + " " + number.substring(4,6) + " " + number.substring(6);
        else
            this.number = number.substring(0,2) + " " +  number.substring(2,4) + " " + number.charAt(4) + " " + number.substring(5);
    }

    public VehicleCard(Context context, int innerLayout)
    {
        super(context, innerLayout);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view)
    {
        textView = (TextView) parent.findViewById(R.id.base_card_text);
        textView.setText(number);
    }
}
