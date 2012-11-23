package cz.muni.fi.langparser;

import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
import cz.muni.fi.publishsubscribe.countingtree.Constraint;
import cz.muni.fi.publishsubscribe.countingtree.Operator;
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
    private String error = "";
    private int currentPos = 0;
    private char current;
    private String alreadyRead = "";
    
    public LangParser(String attributeName, String input) {
        this.attributeName = attributeName;
        this.input = input;
    }

    public Constraint parse() {
        Constraint constraint;
        try {
            constraint = expr();
        } catch (IndexOutOfBoundsException e) {
            putError("Unexpected end of input");
            return null;
        }
        
        return constraint;
    }
    
    private Constraint expr() {
        if (!accept('#')) {
            putErrorExpected('#');
            return null;
        }
        
        AttributeValue av = null;
        Operator operator = null;
        
        if (accept('l')) {
            if (accept('t')) {
                av = argNum();
                operator = Operator.LESS_THAN;
            } else {
                if (accept('e')) {
                    av = argNum();
                    operator = Operator.LESS_THAN_OR_EQUAL_TO;
                } else {
                    putError("Unsupported Operator");
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
                    putError("Unsupported Operator");
                }
            }
            return createConstraint(av, operator);
        }
        
        if (accept('e')) {
            if (accept('q')) {
                av = argAny();
                operator = Operator.EQUALS;
            } else {
                putError("Unsupported Operator");
            }
            return createConstraint(av, operator);
        }
        
        if (accept('r')) {
            if (accept('n')) {
                if (accept('g')) {
                    av = argNum_argNum();
                    operator = Operator.RANGE;
                } else {
                    putError("Unsupported Operator");
                }
            } else {
                putError("Unsupported Operator");
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
                        putError("Unsupported Operator");
                    }
                } else {
                    putError("Unsupported Operator");
                }
            } else {
                putError("Unsupported Operator");
            }
            return createConstraint(av, operator);
        }
        //TODO add SUFFIX
        
        return createConstraint(av, operator);
    }
    
    private AttributeValue argNum() {
        if (! accept(' ')) {
            putErrorExpected(' ');
            return null;
        }
        
        if ((current = eatChar()) == '-') { //expect negative number
            alreadyRead += current;
            while (currentPos < input.length()) {
                alreadyRead += eatChar();
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
        while ((currentPos < input.length()) && ((current = eatChar()) != ' ')) {
            if (current == '-') { //expect date (y-m-dTh:m:s)
                year = getInteger(alreadyRead);
                if (year == -1) {
                    return null;
                }
                
                month = getIntegerUntil('-');
                if (month == -1) {
                    putError("Integer expected");
                    return null;
                }
                
                day = getIntegerUntil('T');
                if (day == -1) {
                    putError("Integer expected");
                    return null;
                }
                
                dateEndsAt = alreadyRead.length() + 1;
            }
            
            if (current == ':') { //expect time (h:m:s)
                hour = getInteger(alreadyRead.substring(dateEndsAt));
                if (hour == -1) {
                    return null;
                }
                
                min = getIntegerUntil(':');
                if (min == -1) {
                    putError("Integer expected");
                    return null;
                }
                
                alreadyRead += current;
                String seconds = "";
                while ((currentPos < input.length()) && ((current = eatChar()) != ' ')) {
                    seconds += current;
                    alreadyRead += current;
                }
                
                if (currentPos == input.length()) {
                    sec = getInteger(seconds);
                    if (sec == -1) {
                        return null;
                    }
                    if (dateEndsAt == 0) {
                        //TODO AttributeValue<Time>...
                        putError("Time not supported yet");
                        return null;
                    } else {
                        Calendar dateTime = new GregorianCalendar(year, month, day, hour, min, sec);
                        return new AttributeValue<Date>(dateTime.getTime(), Date.class);
                    }
                }
                if (current == ' ') {
                    alreadyRead += current;
                    putError("No space expected at position " + (currentPos - 1));
                    return null;
                }
            }
            alreadyRead += current;
        }
        
        if (currentPos < input.length()) {
            putError("No space expected at position " + (currentPos - 1));
            return null;
        }
        
        if (alreadyRead.equals("")) {
            putError("Exactly one argument expected");
            return null;
        }
        
        //else expect positive integer
        return createNumericAttributeValue();
    }
    
    private AttributeValue argAny() {
        AttributeValue av = argNum();
        
        if (current == ' ') {
            putError("No space expected at position " + (currentPos - 1));
            return null;
        }
        
        if (av == null && (! alreadyRead.equals(""))) {
            while ((currentPos < input.length()) && ((current = eatChar()) != ' ')) {
                alreadyRead += current;
            }
            
            if (currentPos < input.length()) {
                putError("No space expected at position " + (currentPos - 1));
                return null;
            }
            
            if (alreadyRead.trim().length() == 0) {
                putError("No space expected at position " + (currentPos - 1));
                return null;
            }
            
            av = new AttributeValue<String>(alreadyRead, String.class);
        }
        
        return av;
    }
    
    private AttributeValue argNum_argNum() {
        if (! accept(' ')) {
            putErrorExpected(' ');
            return null;
        }
        
        AttributeValue av = null;
        
        //TODO
        putError("Range not supported yet");
        
        return av;
    }
    
    private char eatChar() {
        char next = input.charAt(currentPos);
        currentPos++;
        return next;
    }
    
    private boolean accept(char c) {
        if (input.charAt(currentPos) == c) {
            currentPos++;
            return true;
        } else {
            return false;
        }
    }
    
    private AttributeValue createNumericAttributeValue() {
        AttributeValue av;
        
        try {
            Long num = Long.parseLong(alreadyRead);
            av = new AttributeValue<Long>(num, Long.class);
        } catch (NumberFormatException e) {
            try {
                Double num = Double.parseDouble(alreadyRead);
                av = new AttributeValue<Double>(num, Double.class);
                
                //TODO
                putError("Non-integer values not supported yet");
                return null;
                
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        return av;
    }
    
    private int getInteger(String s) { //expect non-negative integer
        int num;
        try {
            num = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            putError("Integer expected");
            return -1;
        }
        return num;
    }
    
    private int getIntegerUntil(char until) {
        alreadyRead += current;
        String string = "";
        while ((currentPos < input.length()) && ((current = eatChar()) != ' ') && (current != until)) {
            string += current;
            alreadyRead += current;
        }
        
        if (currentPos == input.length()) {
            return -1;
        }
        if (current == ' ') {
            alreadyRead += current;
            return -1;
        }
        
        return getInteger(string);
    }
    
    private Constraint createConstraint(AttributeValue av, Operator operator) {
        if (av == null) {
            putError("Failed to parse \'" + alreadyRead + "\'");
            return null;
        }
        
        if (operator == null) {
            putError("Unsupported Operator");
            return null;
        }
        
        return new Constraint(attributeName, av, operator);
    }
    
    private void putErrorExpected(char c) {
        error += "Expected \'" + c + "\', found \'" + input.charAt(currentPos) + "\'\n";
    }
    
    private void putError(String s) {
        error += s + "\n";
    }
    
    public String getErrorMessage() {
        return error;
    }
}