package com.example.annotations;

import org.json.JSONArray;
import org.json.JSONObject;

import static java.lang.System.out;

public class JsonTest {
    public static void main(String[] args) {
        String jsonString = "{\"stringList\":[1,2,3,4]}";
        JSONObject json = new JSONObject(jsonString);

        JSONArray jsonArray = json.getJSONArray("stringList");
        String[] array = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getString(i);
        }
        for (String string : array) {
            out.println(string);
        }
    }
}
