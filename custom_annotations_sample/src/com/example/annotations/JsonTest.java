package com.example.annotations;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class JsonTest {
    public static void main(String[] args) {
        String jsonString = "{\"stringList\":[1,2,3,4]}";
        JSONObject json = new JSONObject(jsonString);

        JSONArray jsonArray = json.getJSONArray("stringList");
        List<String> list = new ArrayList<String>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        for (String string : list) {
            out.println(string);
        }
    }
}
