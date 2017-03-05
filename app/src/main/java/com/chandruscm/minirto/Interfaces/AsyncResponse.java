package com.chandruscm.minirto.Interfaces;

import com.chandruscm.minirto.Models.Vehicle;

public interface AsyncResponse
{
    void processFinish(Vehicle vehicle, int statuscode);
}
