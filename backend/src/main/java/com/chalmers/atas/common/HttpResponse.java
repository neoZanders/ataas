package com.chalmers.atas.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpResponse<T> extends ResponseEntity<T> {

    public HttpResponse(T body, HttpStatus status) {
        super(body, status);
    }

    public static <E extends Error> HttpResponse<E> ofError(E error) {
        return new HttpResponse<>(error, error.getStatus());
    }

    public static <T> HttpResponse<T> fromResult(Result<T> result) {
        if (result.isSuccess()) {
            return new HttpResponse<>(result.getData(), HttpStatus.OK);
        } else {
            @SuppressWarnings("unchecked")
            HttpResponse<T> resp = (HttpResponse<T>) ofError(result.getError());
            return resp;
        }
    }
}
