package cz.muni.fi.langparser;

import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
import static org.junit.Assert.*;
import org.junit.Test;

public class LangParserTestCase { //TODO test the rest
    
    private static final String ATTRIBUTE_NAME = "attribute";
    
    @Test
    public void testParseLTLE() {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#lt 42");
        Constraint constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.LESS_THAN, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#lt -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.LESS_THAN, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#le -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.LESS_THAN_OR_EQUAL_TO, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#lt -42a");
        constraint = parser.parse();
        assertNull(constraint);
    }
    
    @Test
    public void testParseGTGE() {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#gt 42");
        Constraint constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.GREATER_THAN, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#gt -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.GREATER_THAN, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#ge -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.GREATER_THAN_OR_EQUAL_TO, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#gt -42a");
        constraint = parser.parse();
        assertNull(constraint);
    }
    
    @Test
    public void testParseEQ() {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#eq 42");
        Constraint constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#eq -42");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(Long.class, constraint.getAttributeValue().getType());
        assertEquals(-42L, constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#eq bazinga");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("bazinga", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#eq 42a");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("42a", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#eq 10.2");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("10.2", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
        
        parser = new LangParser(ATTRIBUTE_NAME, "#eq 00:00:00:00");
        constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("00:00:00:00", constraint.getAttributeValue().getValue());
        assertEquals(Operator.EQUALS, constraint.getOperator());
    }
    
    @Test
    public void testParsePREF() {
        LangParser parser = new LangParser(ATTRIBUTE_NAME, "#pref abc");
        Constraint constraint = parser.parse();
        assertNotNull(constraint);
        assertEquals(ATTRIBUTE_NAME, constraint.getAttributeName());
        assertEquals(String.class, constraint.getAttributeValue().getType());
        assertEquals("abc", constraint.getAttributeValue().getValue());
        assertEquals(Operator.PREFIX, constraint.getOperator());
    }
}
