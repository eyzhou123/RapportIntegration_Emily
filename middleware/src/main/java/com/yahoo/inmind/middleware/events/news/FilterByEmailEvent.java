package com.yahoo.inmind.middleware.events.news;

import com.yahoo.inmind.control.events.BaseEvent;
import com.yahoo.inmind.middleware.events.MBRequest;

/**
 * Created by oscarr on 7/1/15.
 */
public class FilterByEmailEvent extends BaseEvent {

    private MBRequest mbRequest;

    public FilterByEmailEvent(MBRequest mbRequest) {
        this.mbRequest = mbRequest;
    }

    public void setMbRequest(MBRequest mbRequest) {
        this.mbRequest = mbRequest;
    }

    public MBRequest getMbRequest() {
        return mbRequest;
    }
}
