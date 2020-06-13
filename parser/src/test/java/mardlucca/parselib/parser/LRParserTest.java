/*
 * File: LRParserTest.java
 *
 * Copyright 2020 Marcio D. Lucca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mardlucca.parselib.parser;

import mardlucca.parselib.tokenizer.BasicTokenizer;
import mardlucca.parselib.tokenizer.Token;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static mardlucca.parselib.tokenizer.Recognizers.*;
import static org.junit.Assert.*;

public class LRParserTest
{
    private static List<String> reducedProductions = new ArrayList<>();
    private static BasicTokenizer.Builder<TestToken> builder;

    @BeforeClass
    public static void beforeClass()
    {
        builder = new BasicTokenizer.Builder<TestToken>()
            .recognize(identifiers(TestToken.IDENTIFIER))
            .recognize(numbers(TestToken.NUMBER))
            .recognize(symbol("=", TestToken.ASSIGNMENT))
            .endOfFile(TestToken.EOF);
    }

    @Before
    public void setUp() {
        reducedProductions.clear();
    }

    @Test
    public void testSimpleGrammar1() throws Exception
    {
        Map<String, Integer> lMap = new HashMap<>();
        Parser lParser = new SimpleTestTable(lMap).buildParser(builder::build);
        ParseResult lInvocation = lParser.parse("x = 20");
        assertEquals(0, lInvocation.getErrors().size());
        assertEquals(20, lInvocation.getValue());
        assertEquals(20, (int) lMap.get("x"));
    }

    @Test
    public void testSimpleGrammar2() throws Exception
    {
        Map<String, Integer> lMap = new HashMap<>();
        lMap.put("y", 10);
        Parser lParser = new SimpleTestTable(lMap).buildParser(builder::build);
        ParseResult lInvocation = lParser.parse("x = y");
        assertEquals(0, lInvocation.getErrors().size());
        assertEquals(10, lInvocation.getValue());
        assertEquals(10, (int) lMap.get("x"));
    }

    @Test
    public void testSimpleGrammar3() throws Exception
    {
        Map<String, Integer> lMap = new HashMap<>();
        lMap.put("y", 10);
        Parser lParser = new SimpleTestTable(lMap).buildParser(builder::build);
        ParseResult lInvocation = lParser.parse("y");
        assertEquals(0, lInvocation.getErrors().size());
        assertEquals(10, lInvocation.getValue());
        assertNull(lMap.get("x"));
    }

    @Test
    public void testSimpleGrammarErrorSpecificError() throws Exception
    {
        Parser lParser = new SimpleTestTable(new HashMap<>())
                .buildParser(builder::build);
        List<String> lErrors = lParser.parse("x 20").getErrors();

        assertEquals(1, lErrors.size());
        assertEquals("Assignment operator expected", lErrors.get(0));
    }

    @Test
    public void testSimpleGrammarErrorGenericError() throws Exception
    {
        Parser lParser = new SimpleTestTable(new HashMap<>())
                .buildParser(builder::build);

        List<String> lErrors = lParser.parse("x = =").getErrors();

        assertEquals(1, lErrors.size());
        assertEquals("Syntax error", lErrors.get(0));
    }

    @Test
    public void testSimpleTestParser2() throws Exception
    {
        Parser lParser = new SimpleTestTable2().buildParser(builder::build);
        ParseResult lInvocation = lParser.parse("20 = 20");

        assertEquals(0, lInvocation.getErrors().size());
        assertEquals(asList("B -> num", "S -> B = num"), reducedProductions);
    }

    @Test
    public void testSimpleTestParser22() throws Exception
    {
        Parser lParser = new SimpleTestTable2().buildParser(builder::build);
        ParseResult lInvocation = lParser.parse("20 = ");

        assertEquals(0, lInvocation.getErrors().size());
        assertEquals(asList("A -> num", "S -> A ="), reducedProductions);
    }


    /**
     * Test Grammar looks like this:
     *
     * S' -> S
     * S -> id S2
     * S2 -> = VAL
     * S2 -> ''     //epsilon
     * VAL -> id
     * VAL -> num
     */
    private static class SimpleTestTable extends LRParsingTable<TestToken>
    {
        private static Grammar grammar = new Grammar()
            .addProduction("S'", "S")
            .addProduction("S", TestToken.IDENTIFIER, "S2")
            .addProduction("S2", TestToken.ASSIGNMENT, "VAL")
            .addProduction("S2")
            .addProduction("VAL", TestToken.IDENTIFIER)
            .addProduction("VAL", TestToken.NUMBER);

        private SimpleTestTable(Map<String, Integer> aInMap)
        {
            super(grammar);
            newState()      // 0
                .shift(TestToken.IDENTIFIER, 2)
                .goTo("S", 1);

            newState()      // 1
                .accept(TestToken.EOF);

            newState()      // 2
                .shift(TestToken.ASSIGNMENT, 4)
                .reduce(TestToken.EOF, 3)
                .error(TestToken.NUMBER, "Assignment operator expected")
                .goTo("S2", 3);

            newState()      // 3
                .reduce(TestToken.EOF, 1);

            newState()      // 4
                .shift(TestToken.IDENTIFIER, 6)
                .shift(TestToken.NUMBER, 7)
                .goTo("VAL", 5);

            newState()      // 5
                .reduce(TestToken.EOF, 2);

            newState()      // 6
                .reduce(TestToken.EOF, 4);


            newState()      // 7
                .reduce(TestToken.EOF, 5);

            setListeners(aInMap);
        }

        private static void setListeners(
                Map<String, Integer> aInVariables)
        {
            grammar.onDefaultReduce((aInProduction, aInValues) -> null)
                    .onReduce(grammar.getProduction(1).toString(),
                            ((aInProduction, aInValues) ->
                    {
                        String lVariable = (String)
                                ((Token<TestToken, ?>)aInValues[0]).getValue();
                        Object lValue = aInValues[1];
                        if (lValue != null)
                        {
                            aInVariables.put(lVariable, (Integer) lValue);
                        }
                        else
                        {
                            lValue = aInVariables.get(lVariable);
                        }
                        return lValue;
                    }))
                    .onReduce(2, (aInProduction, aInValues) -> aInValues[1])
                    .onReduce(4, (aInProduction, aInValues) ->
                    {
                        String lVariable = (String)
                                ((Token<TestToken, ?>)aInValues[0]).getValue();
                        Object lValue = aInVariables.get(lVariable);
                        if (lValue == null)
                        {
                            throw new RuntimeException("Unknown variable");
                        }
                        return lValue;
                    })
                    .onReduce(5, (aInProduction, aInValues) ->
                            ((Token<TestToken, ?>)aInValues[0]).getValue());
        }
    }

    /**
     * Test Grammar looks like this:
     *
     * S' -> S
     * S -> A =
     * S -> B = num
     * A -> num
     * B -> num
     */
    private static class SimpleTestTable2 extends LRParsingTable<TestToken>
    {
        private static Grammar grammar = new Grammar()
                .addProduction("S'", "S")
                .addProduction("S", "A", TestToken.ASSIGNMENT)
                .addProduction("S", "B", TestToken.ASSIGNMENT,
                        TestToken.NUMBER)
                .addProduction("A", TestToken.NUMBER)
                .addProduction("B", TestToken.NUMBER)
                .onDefaultReduce(new TestParserListener(reducedProductions));


        private SimpleTestTable2()
        {
            super(grammar);

            newState()      // 0
                    .shift(TestToken.NUMBER, 4)
                    .goTo("S", 1)
                    .goTo("A", 2)
                    .goTo("B", 3);

            newState()      // 1
                    .accept(TestToken.EOF);

            newState()      // 2
                    .shift(TestToken.ASSIGNMENT, 5);

            newState()      // 3
                    .shift(TestToken.ASSIGNMENT, 6);

            newState()      // 4
                    .reduce(TestToken.ASSIGNMENT, 3)
                    .reduceIf(TestToken.ASSIGNMENT, TestToken.NUMBER, 4);

            newState()      // 5
                    .reduce(TestToken.EOF, 1);

            newState()      // 6
                    .shift(TestToken.NUMBER, 7);

            newState()      // 7
                    .reduce(TestToken.EOF, 2);

            grammar.onDefaultReduce(new TestParserListener(reducedProductions));
        }
    }
}