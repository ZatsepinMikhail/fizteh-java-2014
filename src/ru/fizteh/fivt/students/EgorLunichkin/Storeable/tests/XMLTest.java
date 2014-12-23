package ru.fizteh.fivt.students.EgorLunichkin.Storeable.tests;

import org.junit.Test;
import ru.fizteh.fivt.students.EgorLunichkin.Storeable.XMLManager;

import javax.xml.parsers.ParserConfigurationException;
import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class XMLTest {
    @Test
    public void testSerialize() throws Exception {
        List<Object> objects = new ArrayList<>();
        Integer a = 3;
        objects.add(a);
        Double b = 3.05;
        objects.add(b);
        objects.add(true);
        objects.add(null);
        objects.add(" test string ");
        String xml = XMLManager.serialize(objects);
        assertEquals(xml, "<row><col>3</col><col>3.05</col><col>true</col><null/><col> test string </col></row>");
    }

    @Test
    public void testDeSerialize() throws Exception {
        String xml = "<row><col>3</col><col>3.05</col><col>true</col><null/><col> test string </col></row>";
        List<Class<?>> types = new ArrayList<>();
        types.add(Integer.class);
        types.add(Double.class);
        types.add(Boolean.class);
        types.add(Float.class);
        types.add(String.class);
        List<Object> result = XMLManager.deserialize(xml, types);
        assertEquals(result.get(0), 3);
        assertEquals(result.get(1), 3.05);
        assertEquals(result.get(2), true);
        assertEquals(result.get(3), null);
        assertEquals(result.get(4), " test string ");
    }

    @Test
    public void testDeSerializeIncorrectType() throws ParserConfigurationException {
        String xml = "<row><col>aba</col></row>";
        List<Class<?>> types = new ArrayList<>();
        types.add(Double.class);
        try {
            XMLManager.deserialize(xml, types);
            assertEquals(0, 1);
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Incorrect number format. For input string: \"aba\"");
        }
    }

    @Test
    public void testIncorrectXml() throws ParserConfigurationException {
        String xml = "<row><cool></cool></row>";
        try {
            List<Class<?>> types = new ArrayList<>();
            types.add(String.class);
            XMLManager.deserialize(xml, types);
            assertEquals(0, 1);
        } catch (ParseException e) {
            assertEquals(e.getMessage(), "Incorrect tag inside row statement: cool");
        }
    }
}
