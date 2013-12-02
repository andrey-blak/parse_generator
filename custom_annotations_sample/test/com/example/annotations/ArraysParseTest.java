package com.example.annotations;

import com.example.annotations.dto.DtoArrays;
import com.example.annotations.dto.DtoArraysJsonParser;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ArraysParseTest {
    @Test
    public void testStringList() {
        String jsonString = "{\n" +
                "\"booleanArray\":[true, false, false, true],\n" +
                "\"integerArray\":[1,2,3,4],\n" +
                "\"longArray\":[1,2,3,4],\n" +
                "\"shortArray\":[1,2,3,4],\n" +
                "\"doubleArray\":[1.1,2.2,3.3,4.4],\n" +
                "\"floatArray\":[1.1,2.2,3.3,4.4],\n" +
                "\"characterArray\":[a,b,c,d,e],\n" +
                "\"stringArray\":[java, android, xml, json],\n" +
                "}";
        DtoArrays dto = DtoArraysJsonParser.parse(jsonString);

        assertArrayEquals(new Boolean[]{true, false, false, true}, dto.booleanArray);
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, dto.integerArray);
        assertArrayEquals(new long[]{1L, 2L, 3L, 4L}, dto.longArray);
        assertArrayEquals(new Short[]{1, 2, 3, 4}, dto.shortArray);
        assertArrayEquals(new double[]{1.1, 2.2, 3.3, 4.4}, dto.doubleArray, 0.01);
        assertArrayEquals(new Float[]{1.1f, 2.2f, 3.3f, 4.4f}, dto.floatArray);
        assertArrayEquals(new Character[]{'a','b','c','d','e'}, dto.characterArray);
        assertArrayEquals(new String[]{"java","android","xml","json"}, dto.stringArray);
    }
}
