package com.example.taskify.VolleyNetworking;

public interface VolleyCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
