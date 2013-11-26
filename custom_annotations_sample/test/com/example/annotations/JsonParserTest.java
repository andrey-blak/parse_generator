package com.example.annotations;

import com.example.annotations.dto.DtoMenu;
import com.example.annotations.dto.DtoMenuJsonParser;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JsonParserTest {
    @Test
    public void testStringNull() {
        DtoMenu menu = DtoMenuJsonParser.parse((String) null);
        assertNull(menu);
    }

    @Test
    public void testJsonNull() {
        DtoMenu menu = DtoMenuJsonParser.parse((JSONObject) null);
        assertNull(menu);
    }

    @Test
    public void testEmpty() {
        DtoMenu menu = DtoMenuJsonParser.parse("");
        assertNull(menu);
    }

    @Test
    public void testEmptyObject() {
        DtoMenu menu = DtoMenuJsonParser.parse("{}");
        assertEquals(0, menu.id);
        assertEquals(Integer.valueOf(0), menu.amount);
        assertEquals(0, menu.symbol);
        assertEquals("", menu.name);
        assertNull(menu.color);
        assertNull(menu.menuItem);
        assertNull(menu.popup);
    }

    @Test
    public void testPrimitives() {
        String jsonString = "{\"Id\":12,\"amount\":13,\"symbol\":\"s\", \"name\": \"Smith\", \"color\":\"RED\"}";
        DtoMenu menu = DtoMenuJsonParser.parse(jsonString);
        assertEquals(12, menu.id);
        assertEquals(Integer.valueOf(13), menu.amount);
        assertEquals('s', menu.symbol);
        assertEquals("Smith", menu.name);
        assertEquals(DtoMenu.Color.RED, menu.color);
        assertNull(menu.menuItem);
        assertNull(menu.popup);
    }
}
