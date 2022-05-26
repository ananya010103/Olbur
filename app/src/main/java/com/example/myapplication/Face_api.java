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

import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;
import android.util.MalformedJsonException;

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

    public String verify(String FireBaseUrl) {
        //Input needs in the form of name, value pairs
        //    "faceId": "8c9539d4-0370-4a6a-8d66-4190661fa113",
        //    "personId": "93519625-e05a-4773-a763-ca8f66ee9a78",
        //    "personGroupId": "4"

        String jsonInputString = "";
        String personGrpId, personId, faceId = "";
        Double confidenceLevel;
        String requestJson;
        String validFace = "false";
        String returnValue = "false";
        HttpsURLConnection conn = null;
        boolean isIdentical;
        try {
            prepareConnection("detect");
            conn = getAPIConnection();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

        //Set the request JSON body
        if (FireBaseUrl != null) {
            Log.d("checking", "Image URL that contains face:" + FireBaseUrl);
        }
        try (OutputStream os = conn.getOutputStream()) {
            //set url from uploadeduri here
            requestJson = "{" +
                    "\"url\"" + ":\"" + FireBaseUrl + "\"" +
                    "}";
            Log.d("Face Detect", "JSON request is: " + requestJson);
            byte[] input = requestJson.getBytes("utf-8");
            os.write(input);
            Log.d("Face Detect", input.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Read the response to detect API
        try {
            Log.d("Face Detect", "response code for detect is:" + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                // Success
                try{
                    InputStream in = conn.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(in, "UTF-8");
                    if (responseBodyReader != null) {
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        // Start processing the JSON object
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) { // Loop through all keys

                            //JSONObject jsonLine = new JSONObject(jsonReader.)
                            jsonReader.beginObject();
                            String key = jsonReader.nextName(); // Fetch the next key
                            Log.d("Key: ", key);
                            if (key.equals("faceId")) { // Check if desired key
                                // Fetch the value as a String
                                faceId = jsonReader.nextString();
                                if (faceId != null) {
                                    Log.d("Face Detect", "Face was recognised, id is: " + faceId);
                                    validFace = "true";
                                    break;
                                }
                            } else { // Error handling code goes here

                            }
                        }
                    }
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
                conn.disconnect();
            }

            if (validFace.equals("true")) {
                personId = "96352b4a-70f1-4b52-8522-edf9bac1b2ad";
                personGrpId = "2";

                try {
                    prepareConnection("verify");
                    conn = getAPIConnection();
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }

                requestJson = "{" +
                        "\"faceId\"" + ":" + "\""+ faceId + "\"" + "," +
                        "\"personId\"" + ":" + "\"93519625-e05a-4773-a763-ca8f66ee9a78\"" +"," +
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
                                        if (isIdentical==true)
                                            returnValue = "true";
                                    } else if (key.equals("confidence")) { // Check if desired key
                                        // Fetch the value as a String
                                        confidenceLevel = jsonReader.nextDouble();
                                        break; // Break out of the loop
                                    } else { // Error handling code goes here

                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            returnValue = "ERROR";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnValue;
        }
        catch (IOException e){
            e.printStackTrace();
            e.getMessage();
        }
        return returnValue;
    }
};
