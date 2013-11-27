package com.example.annotations;

import com.example.annotations.dto.DtoMenu;
import com.example.annotations.dto.DtoMenuJsonParser;
import com.example.annotations.dto.Primitives;
import com.example.annotations.dto.PrimitivesJsonParser;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;

public class JsonTest {
    public static void main(String[] args) {
        String json = "{}";
        Primitives primitives = PrimitivesJsonParser.parse(json);
    }
}
