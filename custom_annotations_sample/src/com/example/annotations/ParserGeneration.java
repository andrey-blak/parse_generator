package com.example.annotations;

import com.example.annotations.dto.DtoMenu;
import org.json.JSONObject;

import java.io.PrintStream;

public class ParserGeneration {
    private static PrintStream out = System.out;

    private static final String JSON = "{\"id\":\"152\",\"name\":\"File\",\"popup\":{\"menuitem\":[{\"value\":\"New\",\"onclick\":\"CreateNewDoc()\"},{\"value\":\"Open\",a a\"onclick\":\"OpenDoc()\"},{\"value\":\"Close\",\"onclick\":\"CloseDoc()\"}]}}";

    public static void main(String[] args) {
        DtoMenu menu = parseMenu(JSON);

        out.println(menu.id);
        out.println(menu.name);
    }

    private static DtoMenu parseMenu(String jsonString) {
        DtoMenu menu = new DtoMenu();

        JSONObject json = new JSONObject(jsonString);

        menu.id = json.getInt("id");
        menu.name = json.getString("name");

        return menu;
    }
}
