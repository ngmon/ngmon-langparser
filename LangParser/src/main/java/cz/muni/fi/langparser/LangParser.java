package cz.muni.fi.langparser;

import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.LongRange;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/*
 * Inspired by John B. Matthews (https://sites.google.com/site/drjohnbmatthews/enumerated-functions) 
           and Hugh Perkins (https://github.com/hughperkins/jfastparser/blob/master/src/jfastparser/Parser.java)
 */
public class LangParser {
    
    private final String attributeName;
    private final String input;
    private int currentPos = 0;
    private char current;
    private String alreadyRead = "";
    
    public LangParser(String attributeName, String input) {
        this.attributeName = attributeName;
        this.input = input;
    }

    public Constraint parse() {
        Constraint constraint = null;
        try {
            constraint = expr();
        } catch (IndexOutOfBoundsException | UnsupportedOperationException | ParseException e) {
            //TODO
        }
        
        return constraint;
    }
    
    private Constraint expr() throws ParseException {
        if (!accept('#')) {
            throw new ParseException("# expected", currentPos);
        }
        
        AttributeValue av = null;
        Operator operator = null;
        
        if (accept('l')) {
            if (accept('t')) {
                operator = Operator.LESS_THAN;
                av = argNum();
            } else {
                if (accept('e')) {
                    operator = Operator.LESS_THAN_OR_EQUAL_TO;
                    av = argNum();
                } else {
                    throw new ParseException("Unsupported Operator", currentPos);
                }
            }
            return createConstraint(av, operator);
        }
        
        if (accept('g')) {
            if (accept('t')) {
                av = argNum();
                operator = Operator.GREATER_THAN;
            } else {
                if (accept('e')) {
                    av = argNum();
                    operator = Operator.GREATER_THAN_OR_EQUAL_TO;
                } else {
                    throw new ParseException("Unsupported Operator", currentPos);
                }
            }
            return createConstraint(av, operator);
        }
        
        if (accept('e')) {
            if (accept('q')) {
                av = argAny();
                operator = Operator.EQUALS;
            } else {
                throw new ParseException("Unsupported Operator", currentPos);
            }
            return createConstraint(av, operator);
        }
        
        if (accept('r')) {
            if (accept('n')) {
                if (accept('g')) {
                    av = argNum_argNum();
                    operator = Operator.RANGE;
                } else {
                    throw new ParseException("Unsupported Operator", currentPos);
                }
            } else {
                throw new ParseException("Unsupported Operator", currentPos);
            }
            return createConstraint(av, operator);
        }
        
        if (accept('p')) {
            if (accept('r')) {
                if (accept('e')) {
                    if (accept('f')) {
                        av = argAny(); //TODO enforce String?
                        operator = Operator.PREFIX;
                    } else {
                        throw new ParseException("Unsupported Operator", currentPos);
                    }
                } else {
                    throw new ParseException("Unsupported Operator", currentPos);
                }
            } else {
                throw new ParseException("Unsupported Operator", currentPos);
            }
            return createConstraint(av, operator);
        }
        //TODO add SUFFIX
        
        return createConstraint(av, operator);
    }
    
    private AttributeValue argNum() throws ParseException {
        if (! accept(' ')) {
            throw new ParseException("Space expected", currentPos);
        }
        
        alreadyRead = "";
        
        if ((current = eatChar()) == '-') { //expect negative number
            alreadyRead += current;
            while ((currentPos < input.length()) && (eatCharIfNotSpace())) {
                alreadyRead += current;
            }
            
            return createNumericAttributeValue();
        }
        
        int dateEndsAt = 0;
        int year = 0;
        int month = 0;
        int day = 0;
        int hour;
        int min;
        int sec;
                
        alreadyRead += current;
        while ((currentPos < input.length()) && (eatCharIfNotSpace())) {
            if (current == '-') { //expect date (y-m-dTh:m:s)
                year = getInteger(alreadyRead);
                month = getIntegerUntil('-');
                day = getIntegerUntil('T');
                dateEndsAt = alreadyRead.length() + 1;
            }
            
            if (current == ':') { //expect time (h:m:s)
                hour = getInteger(alreadyRead.substring(dateEndsAt));
                min = getIntegerUntil(':');
                alreadyRead += current;
                String seconds = "";
                while ((currentPos < input.length()) && (eatCharIfNotSpace())) {
                    seconds += current;
                    alreadyRead += current;
                }
                
                if (currentPos == input.length()) {
                    sec = getInteger(seconds);
                    if (dateEndsAt == 0) {
                        //TODO AttributeValue<Time>...
                        throw new UnsupportedOperationException("Time not supported yet");
                    } else {
                        Calendar dateTime = new GregorianCalendar(year, month, day, hour, min, sec);
                        return new AttributeValue<>(dateTime.getTime(), Date.class);
                    }
                }
            }
            alreadyRead += current;
        }
        
        //else expect positive integer
        return createNumericAttributeValue();
    }
    
    private AttributeValue argAny() throws ParseException {
        AttributeValue av = null;
        try {
            av = argNum();
        } catch (ParseException | UnsupportedOperationException e) {
            //TODO
        }
        
        if (current == ' ') {
            throw new ParseException("No space expected", currentPos - 1);
        }
        
        if (av == null && (! alreadyRead.equals(""))) {
            while ((currentPos < input.length()) && (eatCharIfNotSpace())) {
                alreadyRead += current;
            }
            
            if (alreadyRead.trim().length() == 0) {
                throw new ParseException("No space expected", currentPos - 1);
            }
            
            av = new AttributeValue<>(alreadyRead, String.class);
        }
        
        return av;
    }
    
    private AttributeValue argNum_argNum() throws ParseException {
        AttributeValue av1 = argNum();
        
        if (av1.getType() == Date.class) {
            AttributeValue av2 = argNum();
            
            if (av2.getType() == Date.class) {
                //TODO return new AttributeValue<DateRange>...
                throw new UnsupportedOperationException("DateRange not supported yet");
            } else {
                throw new ParseException("Type mismatch", currentPos);
            }
        }
        
        //TODO time
        
        if (av1.getType() == Double.class) {
            AttributeValue av2 = argNum();
            
            if ((av2.getType() == Double.class) || (av2.getType() == Long.class)) {
                //TODO return new AttributeValue<DoubleRange>...
                throw new UnsupportedOperationException("DoubleRange not supported yet");
            } else {
                throw new ParseException("Type mismatch", currentPos);
            }
        }
        
        if (av1.getType() == Long.class) {
            AttributeValue av2 = argNum();
            
            if (av2.getType() == Long.class) {
                return new AttributeValue<>(new LongRange((Long)av1.getValue(), (Long)av2.getValue()), LongRange.class);
            } else {
                if (av2.getType() == Double.class) {
                    //TODO return new AttributeValue<DoubleRange>...
                    throw new UnsupportedOperationException("DoubleRange not supported yet");
                } else {
                    throw new ParseException("Type mismatch", currentPos);
                }
            }
        }
        
        throw new ParseException(av1.getType() + " not supported", currentPos);
    }
    
    private char eatChar() {
        char next = input.charAt(currentPos);
        currentPos++;
        return next;
    }
    
    private boolean eatCharIfNotSpace() {
        if (input.charAt(currentPos) == ' ') {
            return false;
        } else {
            current = eatChar();
            return true;
        }
    }
    
    private boolean accept(char c) {
        if (input.charAt(currentPos) == c) {
            currentPos++;
            return true;
        } else {
            return false;
        }
    }
    
    private AttributeValue createNumericAttributeValue() throws ParseException {
        AttributeValue av;
        
        try {
            Long num = Long.parseLong(alreadyRead);
            av = new AttributeValue<>(num, Long.class);
        } catch (NumberFormatException e) {
            try {
//                Double num = Double.parseDouble(alreadyRead);
//                av = new AttributeValue<Double>(num, Double.class);
                
                //TODO
                throw new UnsupportedOperationException("Non-integer values not supported yet");
            } catch (NumberFormatException ex) {
                throw new ParseException("Number expected", currentPos);
            }
        }

        return av;
    }
    
    private int getInteger(String s) throws ParseException { //expect non-negative integer
        int num;
        try {
            num = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new ParseException("Integer expected", currentPos);
        }
        return num;
    }
    
    private int getIntegerUntil(char until) throws ParseException {
        alreadyRead += current;
        String string = "";
        while ((currentPos < input.length()) && (eatCharIfNotSpace()) && (current != until)) {
            string += current;
            alreadyRead += current;
        }
        
        if ((currentPos == input.length()) || (current == ' ')) {
            throw new ParseException("Unexpected end of input", currentPos);
        }
        
        return getInteger(string);
    }
    
    private Constraint createConstraint(AttributeValue av, Operator operator) throws ParseException {
        if (! isLastArg()) {
            throw new ParseException("End of input expected", currentPos);
        }
        
        if (av == null) {
            throw new ParseException("Failed to parse \'" + alreadyRead + "\'", currentPos);
        }
        
        if (operator == null) {
            throw new ParseException("Unsupported Operator", currentPos);
        }
        
        return new Constraint(attributeName, av, operator);
    }
    
    private boolean isLastArg() {
        if (currentPos < input.length()) {
            return false;
        }
        return true;
    }
}