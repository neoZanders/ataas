package com.chalmers.atas.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpResponse<T> extends ResponseEntity<T> {

    public HttpResponse(T body, HttpStatus status) {
        super(body, status);
    }

    public static <T> HttpResponse<T> fromResult(Result<T> result) {
        if (result.isSuccess()) {
            return new HttpResponse<>(result.getData(), HttpStatus.OK);
        } else {
            Error error = result.getError();
            @SuppressWarnings("unchecked")
            HttpResponse<T> resp = (HttpResponse<T>) new HttpResponse<>(error, error.getStatus());
            return resp;
        }
    }
}
