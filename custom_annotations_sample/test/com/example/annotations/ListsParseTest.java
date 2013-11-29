package com.example.annotations;

import com.example.annotations.dto.DtoLists;
import com.example.annotations.dto.DtoListsJsonParser;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class ListsParseTest {
    @Test
    public void testStringList() {
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
}
