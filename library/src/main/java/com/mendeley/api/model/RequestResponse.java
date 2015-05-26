package com.mendeley.api.model;

import com.mendeley.api.request.params.Page;

import java.util.Date;

public class RequestResponse<T> {
    public final T resource;
    public final Page next;
    public final Date serverDate;

    public RequestResponse(T resource, Date serverDate, Page next) {
        this.resource = resource;
        this.next = next;
        this.serverDate = serverDate;
    }

    public RequestResponse(T resource, Date serverDate) {
        this(resource, serverDate, null);
    }
}
