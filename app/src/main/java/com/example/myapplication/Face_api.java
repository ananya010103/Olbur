package com.example.myapplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;

import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;
import android.util.MalformedJsonException;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

class FaceDetect {
    protected HttpsURLConnection urlConn;
    private HttpsURLConnection getAPIConnection(){
        return urlConn;
    }
    private void setAPIConnection(HttpsURLConnection urlConnection){
        this.urlConn = urlConnection;
    }

    //This method is to keep all common activities related to connection in a single place
    public void prepareConnection (String method) throws IOException{
        URL url = null;
        try {
            if (method =="verify")
                url = new URL("https://centralindia.api.cognitive.microsoft.com/face/v1.0/verify");
            else //detect
                url = new URL("https://centralindia.api.cognitive.microsoft.com/face/v1.0/detect");
        } catch (MalformedURLException ex) {
            ex.getMessage();
        }

        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Ocp-Apim-Subscription-Key", "7ca6a1b2734245088c4a27f6b9deb1f3");
        try {
            urlConnection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        try{
            urlConnection.connect();
        }
        catch (MalformedURLException ex){
            ex.printStackTrace();
        }

        setAPIConnection(urlConnection);
    }

    public String verify(String FireBaseUrl, String DriverID) {
        //Input needs in the form of name, value pairs
        //    "faceId": "8c9539d4-0370-4a6a-8d66-4190661fa113",
        //    "personId": "93519625-e05a-4773-a763-ca8f66ee9a78",
        //    "personGroupId": "4"

        String jsonInputString = "";
        String personGrpId, personId="", faceId = "";
        Double confidenceLevel;
        String requestJson;
        String validFace = "false";
        String returnValue = "false";
        boolean validResponse = false;
        HttpsURLConnection conn = null;
        boolean isIdentical;
        try {
            prepareConnection("detect");
            conn = getAPIConnection();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

        //Set the request JSON body for detect API
        if (FireBaseUrl != null) {
            Log.d("checking", "Image URL that contains face:" + FireBaseUrl);

            try (OutputStream os = conn.getOutputStream()) {//set url from uploadeduri here

                //format for JSON is "url": "https://something.com/someplace/"
                requestJson = "{" + "\"url\"" + ":\"" + FireBaseUrl + "\"" + "}";
                Log.d("Face Detect", "JSON request is: " + requestJson);

                byte[] input = requestJson.getBytes("utf-8");
                os.write(input);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Read the response to detect API
        try {
            Log.d("Face Detect", "response code for detect is:" + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {// Success
                try{
                    InputStream in = conn.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(in, "UTF-8");
                    if (responseBodyReader != null) {
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        // Start processing the JSON object
                        //Response to this API contains an array of objects
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) { // Loop through all objects
                            validResponse = true; //to confirm that this was not an empty response
                            jsonReader.beginObject();
                            String key = jsonReader.nextName(); // Fetch the next key
                            Log.d("Face Detect", "Key value is: " + key);
                            if (key.equals("faceId")) { // Check if desired key
                                // Fetch the value as a String
                                faceId = jsonReader.nextString();
                                if (faceId != null) {
                                    Log.d("Face Detect", "Face was detected, id is: " + faceId);
                                    validFace = "true";
                                    break;
                                }
                            }
                        }
                        if (!validResponse) {//when face not detected, success code is 200 but returns an empty array
                            returnValue = "Face not detected";
                            return returnValue;
                        }
                    }
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
                conn.disconnect();
            }

            //Verify API needs to be called only if a face was detected
            try {
                prepareConnection("verify");
                conn = getAPIConnection();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }


            requestJson = "{" +
                    "\"faceId\"" + ":" + "\""+ faceId + "\"" + "," +
                    "\"personId\"" + ":" + "\"" + DriverID + "\"" +"," +
                    //"\"personId\"" + ":" + "\"80c763bb-d096-4d75-ac72-e2482ec15e7b\"" +"," +
                    "\"personGroupId\"" + ":"+ "\"4\"" +
                    "}";

            //Set the request JSON body
            try (OutputStream os = conn.getOutputStream()) {
                Log.d("Face Verify", "JSON request is: " + requestJson);
                byte[] input = requestJson.getBytes("utf-8");
                os.write(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("Face Detect", "Response code from Verify API is: " + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {//read the response for the verify API
                try {
                    // Success
                    InputStream in = conn.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(in, "UTF-8");
                    if (responseBodyReader != null) {
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        try {
                            jsonReader.beginObject(); // Start processing the JSON object
                            while (jsonReader.hasNext()) { // Loop through all keys
                                String key = jsonReader.nextName(); // Fetch the next key

                                if (key.equals("isIdentical")) { // Check if desired key
                                    // Fetch the value as a String
                                    isIdentical = jsonReader.nextBoolean();
                                    if (isIdentical == true)
                                        returnValue = "true";
                                } else if (key.equals("confidence")) { // Check if desired key
                                    // Fetch the value as a String

                                    confidenceLevel = jsonReader.nextDouble();
                                    Log.d("checking", "Confidence level of face match is " + confidenceLevel);
                                    break; // Break out of the loop
                                } else { // Error handling code goes here
                                    return returnValue = "Invalid input";
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return returnValue = "ERROR";
            }
        }
        catch (IOException e){
            e.printStackTrace();
            e.getMessage();
        }
        return returnValue;
    }
};
