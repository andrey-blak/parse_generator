package com.example.annotations;

import com.example.annotations.dto.DtoArrays;
import com.example.annotations.dto.DtoArraysJsonParser;
import com.example.annotations.dto.DtoLists;
import com.example.annotations.dto.DtoListsJsonParser;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CollectionsParseTest {
    @Test
    public void testLists() {
        String jsonString = "{\n" +
                "\"booleanList\":[true, false, false, true],\n" +
                "\"integerList\":[1,2,3,4],\n" +
                "\"longList\":[1,2,3,4],\n" +
                "\"shortList\":[1,2,3,4],\n" +
                "\"doubleList\":[1.1,2.2,3.3,4.4],\n" +
                "\"floatList\":[1.1,2.2,3.3,4.4],\n" +
                "\"characterList\":[a,b,c,d,e],\n" +
                "\"stringList\":[java, android, xml, json],\n" +
                "}";
        DtoLists dto = DtoListsJsonParser.parse(jsonString);

        assertArrayEquals(new Boolean[]{true, false, false, true}, dto.booleanList.toArray());
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, dto.integerList.toArray());
        assertArrayEquals(new Long[]{1l, 2l, 3l, 4l}, dto.longList.toArray());
        assertArrayEquals(new Short[]{1, 2, 3, 4}, dto.shortList.toArray());
        assertArrayEquals(new Double[]{1.1, 2.2, 3.3, 4.4}, dto.doubleList.toArray());
        assertArrayEquals(new Float[]{1.1f, 2.2f, 3.3f, 4.4f}, dto.floatList.toArray());
        assertArrayEquals(new Character[]{'a','b','c','d','e'}, dto.characterList.toArray());
        assertArrayEquals(new String[]{"java","android","xml","json"}, dto.stringList.toArray());
    }

    @Test
    public void testArrays() {
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
