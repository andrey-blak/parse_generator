package com.example.annotations;

import com.example.annotations.dto.Primitives;
import com.example.annotations.dto.PrimitivesJsonParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PrimitivesParserTest {
    @Test
    public void testPrimitivesDefault() {
        String json = "{}";
        Primitives primitives = PrimitivesJsonParser.parse(json);
        assertEquals(true, primitives._boolean);
        assertEquals(true, primitives._Boolean);
        assertEquals(42, primitives._int);
        assertEquals(42, (int) primitives._Integer);
        assertEquals(1234567890123456789l, primitives._long);
        assertEquals(1234567890123456789l, (long) primitives._Long);
        assertEquals(12345, primitives._short);
        assertEquals(12345, (short) primitives._Short);
        assertEquals(123.456789, primitives._double, 0.0000001);
        assertEquals(123.456789, primitives._Double, 0.0000001);
        assertEquals(123.456789, primitives._float, 0.0001);
        assertEquals(123.456789, primitives._Float, 0.0001);
        assertEquals('S', primitives._char);
        assertEquals('S', primitives._Character.charValue());
        assertEquals("Trinity", primitives._String);
    }
}
