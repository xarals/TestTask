package com.xaral.testtask.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Provides methods to interact with the Test Assignment API.
 */
public class TestAssignmentApi {
    private final static String URL = "https://frontend-test-assignment-api.abz.agency/api/v1";

    /**
     * Retrieves a list of users from the API.
     *
     * @param page  the page number to retrieve
     * @param count the number of users per page
     * @return a list of User objects
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if a JSON parsing error occurs
     */
    public static List<User> getUsers(int page, int count) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(URL + "/users?page=" + page + "&count=" + count)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200 && response.code() != 404 && response.code() != 422)
            return null;

        JSONObject answer = new JSONObject(response.body().string());

        if (!answer.getBoolean("success"))
            return new ArrayList<>();

        JSONArray usersInfo = answer.getJSONArray("users");
        List<User> users = new ArrayList<>();
        for (int i = 0; i < usersInfo.length(); i++) {
            JSONObject userInfo = usersInfo.getJSONObject(i);
            Long id = userInfo.getLong("id");
            String name = userInfo.getString("name");
            String email = userInfo.getString("email");
            String phone = userInfo.getString("phone");
            String position = userInfo.getString("position");
            Integer positionId = userInfo.getInt("position_id");
            Long registrationTime = userInfo.getLong("registration_timestamp");
            String photo = userInfo.getString("photo");
            users.add(new User(id, name, email, phone, position, positionId, registrationTime, photo));
        }

        return users;
    }

    /**
     * Retrieves a single user by ID from the API.
     *
     * @param id the ID of the user to retrieve
     * @return a User object
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if a JSON parsing error occurs
     */
    public static User getUser(Long id) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(URL + "/users/" + id)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful())
            return null;

        JSONObject answer = new JSONObject(response.body().string());

        if (!answer.getBoolean("success"))
            return null;

        JSONObject userInfo = answer.getJSONObject("user");

        Long user_id = userInfo.getLong("id");
        String name = userInfo.getString("name");
        String email = userInfo.getString("email");
        String phone = userInfo.getString("phone");
        String position = userInfo.getString("position");
        Integer positionId = userInfo.getInt("position_id");
        Long registrationTime = userInfo.getLong("registration_timestamp");
        String photo = userInfo.getString("photo");

        User user = new User(user_id, name, email, phone, position, positionId, registrationTime, photo);

        return user;
    }

    /**
     * Retrieves a map of positions from the API.
     *
     * @return a map of position names to their IDs
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if a JSON parsing error occurs
     */
    public static Map<String, Integer> getPositions() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(URL + "/positions")
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful())
            return null;

        JSONObject answer = new JSONObject(response.body().string());

        if (!answer.getBoolean("success"))
            return null;

        JSONArray positionsInfo = answer.getJSONArray("positions");
        Map<String, Integer> positions = new LinkedHashMap<>();

        for (int i = 0; i < positionsInfo.length(); i++) {
            JSONObject positionInfo = positionsInfo.getJSONObject(i);
            positions.put(positionInfo.getString("name"), positionInfo.getInt("id"));
        }

        return positions;
    }

    /**
     * Creates a new user on the API.
     *
     * @param name        the name of the user
     * @param email       the email address of the user
     * @param phone       the phone number of the user
     * @param positionId  the ID of the user's position
     * @param photo       the photo of the user as a byte array
     * @return a JSONObject containing the API response
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if a JSON parsing error occurs
     */
    public static JSONObject newUser(String name, String email, String phone, int positionId, byte[] photo) throws IOException, JSONException {

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name)
                .addFormDataPart("email", email)
                .addFormDataPart("phone", phone)
                .addFormDataPart("position_id", String.valueOf(positionId))
                .addFormDataPart("photo", "photo",
                        RequestBody.create(photo))
                .build();

        String token = getToken();

        if (token == null || token.isEmpty()) {
            Log.i("Token", "Token is empty");
            return null;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL + "/users")
                .post(requestBody)
                .header("Token", token)
                .build();

        Response response = client.newCall(request).execute();

        Log.i("responce", response.body().toString());

        JSONObject answer = new JSONObject(response.body().string());

        return answer;
    }

    /**
     * Retrieves an authorization token from the API.
     *
     * @return a string containing the token
     * @throws IOException   if an I/O error occurs
     * @throws JSONException if a JSON parsing error occurs
     */
    private static String getToken() throws JSONException, IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(URL + "/token")
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful())
            return null;

        JSONObject answer = new JSONObject(response.body().string());

        if (!answer.getBoolean("success"))
            return null;

        String token = answer.getString("token");

        return token;
    }
}
