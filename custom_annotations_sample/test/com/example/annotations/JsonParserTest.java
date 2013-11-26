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
    }

    @Test
    public void testClass() {
        String jsonString = "{\"menuItem\":{\"onClick\": \"onclick()\", \"fraction\":15.7, \"byte\":61 ,\"value\":\"open new\"}}";
        DtoMenu menu = DtoMenuJsonParser.parse(jsonString);
        DtoMenu.InnerOuterClass.DtoMenuItem menuItem = menu.menuItem;
        assertNotNull(menu.menuItem);
        assertEquals("onclick()", menu.menuItem.onclick);
        assertEquals(15.7, (double) menu.menuItem.fraction, 0.0001);
        assertEquals(61, menu.menuItem._byte);
        assertEquals("open new", menu.menuItem.getValue());
    }

    @Test
    public void testFunctional() {
        String jsonString = "{\"Id\":\"32\", \"amount\":\"1222\", \"symbol\":\"a\", \"name\":\"Trinity\", \"color\":\"GREEN\", \"Popup\": {\"menuMenu\":{\"onClick\":\"some method\", \"fraction\":\"18.1\", \"byte\":\"124\", \"value\":\"my value\"}}, \"menuItem\":{\"onClick\":\"other method\", \"fraction\":\"15.789\", \"byte\":\"14\", \"value\":\"other value\"}}";
        DtoMenu menu = DtoMenuJsonParser.parse(jsonString);
        assertEquals(32, menu.id);
        assertEquals(1222, (int) menu.amount);
        assertEquals('a', menu.symbol);
        assertEquals("Trinity", menu.name);
        assertEquals(DtoMenu.Color.GREEN, menu.color);

        DtoMenu.InnerOuterClass.DtoMenuItem popupItem = menu.popup.menuItem;
        assertEquals("some method", popupItem.onclick);
        assertEquals(18.1, popupItem.fraction, 0.0001);
        assertEquals(124, popupItem._byte);
        assertEquals("my value", popupItem.getValue());

        DtoMenu.InnerOuterClass.DtoMenuItem menuItem = menu.menuItem;
        assertEquals("other method", menuItem.onclick);
        assertEquals(15.789, menuItem.fraction, 0.000001);
        assertEquals(14, menuItem._byte);
        assertEquals("other value", menuItem.getValue());
    }
}
