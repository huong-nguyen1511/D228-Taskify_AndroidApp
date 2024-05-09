package com.example.taskify.VolleyNetworking;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.taskify.Model.TaskModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager {
    private static final String BASE_URL = "https://daviddurand.info/D228/reminder";
    private static final String KEY = "emiage2023-2";
    private static NetworkManager instance;
    private RequestQueue requestQueue;
    private String sessionCookie = "";

    private NetworkManager(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }
        return instance;
    }

    public void setSessionCookie(String cookie) {
        this.sessionCookie = cookie;
    }

    //===============================================================================================================================================================
    //REGISTER USER METHOD
    //===============================================================================================================================================================
    public void registerUser(String username, String password, String registrationKey, final VolleyCallback<String> callback) {
        String url = BASE_URL + "/register/" + username + "/" + password + "/" + KEY;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            String message = jsonResponse.getString("message");
                            if (code == 201) {
                                callback.onSuccess(message);
                            } else {
                                callback.onError(message);
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error occurred");
                    }
                });
        requestQueue.add(request);
    }

    //===============================================================================================================================================================
    //LOGIN USER METHOD
    //===============================================================================================================================================================
    public void loginUser(String username, String password, final VolleyCallback<String> callback) {
        String url = BASE_URL + "/connect/" + username + "/" + password;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            String message = jsonResponse.getString("message");
                            if (code == 200) {
                                callback.onSuccess(message);
                            } else {
                                callback.onError(message);
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error occurred");
                    }
                }){
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // Store the session cookie
                String cookie = response.headers.get("Set-Cookie");
                if (cookie != null && cookie.contains("reminder")) {
                    setSessionCookie(cookie.split(";")[0]);
                }
                return super.parseNetworkResponse(response);
            }
        };
        requestQueue.add(request);
    }

    //===============================================================================================================================================================
    //LOGOUT USER METHOD
    //===============================================================================================================================================================
    public void logoutUser(final VolleyCallback<String> callback) {
        String url = BASE_URL + "/deconnect";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            String message = jsonResponse.getString("message");
                            if (code == 200) {
                                callback.onSuccess(message);
                            } else {
                                callback.onError(message);
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error on logout: " + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (sessionCookie != null && !sessionCookie.isEmpty()) {
                    headers.put("Cookie", sessionCookie);
                }
                return headers;
            }
        };
        requestQueue.add(request);
    }


    //===============================================================================================================================================================
    //ADD TASK METHOD
    //===============================================================================================================================================================
    public void addTask(TaskModel task, final VolleyCallback<String> callback) {
        String url = BASE_URL + "/add";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            if (code == 200) {
                                String taskId = jsonResponse.getJSONObject("data").getString("id");
                                callback.onSuccess(taskId);
                            } else {
                                callback.onError(jsonResponse.getString("message"));
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error occurred");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("texte", task.getTaskName());
                params.put("e", task.getTaskDeadline());
                params.put("o", String.valueOf(task.getTaskOrder()));
                params.put("c", task.getTaskColor());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", sessionCookie);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    //===============================================================================================================================================================
    //UPDATE TASK METHOD
    //===============================================================================================================================================================
    public void updateTask(String taskId, TaskModel updatedTask, final VolleyCallback<String> callback) {
        String url = BASE_URL + "/change/" + taskId;
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String updatedTaskId = jsonResponse.getString("data");
                            callback.onSuccess(updatedTaskId);
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error occurred");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("texte", updatedTask.getTaskName());
                params.put("e", updatedTask.getTaskDeadline());
                params.put("o", String.valueOf(updatedTask.getTaskOrder()));
                params.put("c", updatedTask.getTaskColor());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", sessionCookie);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    //===============================================================================================================================================================
    //GET TASK LIST METHOD
    //===============================================================================================================================================================
    public void getTaskList(final VolleyCallback<ArrayList<TaskModel>> callback) {
        String url = BASE_URL + "/list";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            if (code == 200) {
                                JSONArray jsonArray = jsonResponse.getJSONArray("data");
                                ArrayList<TaskModel> taskList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonTask = jsonArray.getJSONObject(i);
                                    String taskId = jsonTask.getString("id");
                                    String taskName = jsonTask.getString("tache");
                                    String taskDeadline = jsonTask.getString("echeance");
                                    int taskOrder = jsonTask.getInt("ordre");
                                    String taskColor = jsonTask.getString("couleur");
                                    TaskModel task = new TaskModel(taskId, taskName, taskDeadline, taskColor, "");
                                    task.setTaskOrder(taskOrder);
                                    taskList.add(task);
                                }
                                callback.onSuccess(taskList);
                            } else {
                                callback.onError(jsonResponse.getString("message"));
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error occurred");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", sessionCookie);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    //===============================================================================================================================================================
    //GET TASK BY ID METHOD
    //===============================================================================================================================================================

    public void getTaskById(String taskId, final VolleyCallback<TaskModel> callback) {
        String url = BASE_URL + "/get/" + taskId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response from /get: " + response);  // Log the full response
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            if (code == 200) {
                                JSONObject taskObject = jsonResponse.getJSONObject("data");
                                String id = taskObject.getString("id");
                                String taskName = taskObject.getString("tache");
                                String taskDeadline = taskObject.getString("echeance");
                                String taskColor = taskObject.getString("couleur");
                                int taskOrder = taskObject.optInt("ordre", 0); // Default order to 0 if not present or parsing fails
//
                                TaskModel task = new TaskModel(id, taskName, taskDeadline, taskColor, "");
                                task.setTaskOrder(taskOrder);
                                callback.onSuccess(task);
                            } else {
                                callback.onError(jsonResponse.getString("message"));
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Network error on /get: " + error.toString());
                        callback.onError("Network error: " + error.getMessage());
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", sessionCookie);
                return headers;
            }
        };
        requestQueue.add(request);
    }


    //===============================================================================================================================================================
    //DELETE TASK METHOD
    //===============================================================================================================================================================
    public void deleteTask(String taskId, final VolleyCallback<String> callback) {
        String url = BASE_URL + "/remove/" + taskId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            String message = jsonResponse.getString("message");
                            if (code == 200) {
                                callback.onSuccess(message);
                            } else {
                                callback.onError(message);
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error occurred");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", sessionCookie);
                return headers;
            }
        };
        requestQueue.add(request);
    }
}
