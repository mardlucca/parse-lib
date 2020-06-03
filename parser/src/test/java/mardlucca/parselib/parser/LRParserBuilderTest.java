/*
 * File: LRParserBuilderTest.java
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

import mardlucca.parselib.parser.LRParser.ParseResult;
import mardlucca.parselib.tokenizer.BasicTokenizer;
import mardlucca.parselib.tokenizer.UnrecognizedCharacterSequenceException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static java.lang.Character.isUpperCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LRParserBuilderTest
{
    private static TestParserListener listener = new TestParserListener();

    private static BasicTokenizer.Builder<TestToken> tokenizerBuilder;

    private static LRParser<TestToken, String> parser;

    @BeforeClass
    public static void beforeClass()
    {
        tokenizerBuilder = new BasicTokenizer.Builder<TestToken>()
            .identifiers(TestToken.IDENTIFIER)
            .numberLiterals(TestToken.NUMBER)
            .symbol("+", TestToken.PLUS)
            .symbol("*", TestToken.STAR)
            .symbol(".", TestToken.PERIOD)
            .symbol("(", TestToken.OPEN_PARENTHESIS)
            .symbol(")", TestToken.CLOSE_PARENTHESIS)
            .endOfFile(TestToken.EOF);

        parser = new LRParserBuilder<>(
            "LRParserBuilderTest",
            TestToken::parse,
            aInString -> isUpperCase(aInString.charAt(0)) ? aInString : null)
            .build(tokenizerBuilder::build);
    }

    @Test
    public void testBuild()
        throws IOException, UnrecognizedCharacterSequenceException
    {
        testString("a", "DOT -> ''", "F -> id DOT", "M -> F", "S -> M");
        testString("( a + 10 ) * b.c",
            "DOT -> ''", "F -> id DOT", "M -> F", "S -> M", "F -> num",
            "M -> F", "S -> S + M", "F -> ( S )", "M -> F", "DOT -> . id",
            "F -> id DOT", "M -> M * F", "S -> M");
        testString("1+2*3", "F -> num", "M -> F", "S -> M", "F -> num",
            "M -> F", "F -> num", "M -> M * F", "S -> S + M");
    }

    @Test
    public void testBuildWithSyntaxError()
        throws IOException, UnrecognizedCharacterSequenceException
    {
        testStringWithError("( a + 10 ", "Unexpected end of file",
            "DOT -> ''", "F -> id DOT", "M -> F", "S -> M", "F -> num",
            "M -> F", "S -> S + M");
    }

    @Test
    public void testInvalidGrammar1()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidGrammar1",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Production with less than 3 symbols:  S'  ->",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidGrammar2()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidGrammar2",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Left hand symbol \"123\" in production \"123 -> " +
                    "num\" was not recognized as a non-terminal",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidGrammar3()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidGrammar3",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Epsilon symbol must be the only one in the right " +
                    "hand side of a production: DOT -> '' id",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidGrammar4()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidGrammar4",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Epsilon symbol must be the only one in the right " +
                    "hand side of a production: DOT -> id ''",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidGrammar5()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidGrammar5",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Symbol \"@\" in production \"F -> @\" is not valid",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidTable1()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidTable1",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Invalid action \"4\" in row \"0\" and column \"num\"",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidTable2()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidTable2",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Invalid action \"r1\" in row \"0\" and column \"S\"",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidTable3()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidTable3",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Expected state \"2\" but found \"1\"",
                e.getMessage());
        }
    }

    @Test
    public void testInvalidTable4()
    {
        try
        {
            new LRParserBuilder<>(
                "invalidTable4",
                TestToken::parse,
                aInString -> isUpperCase(aInString.charAt(0)) ?
                    aInString : null).build(null);
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Could not find error message for error code \"1\"",
                e.getMessage());
        }
    }

    private static void testString(String aInString, String ... aInProductions)
        throws IOException, UnrecognizedCharacterSequenceException
    {
        testStringWithError(aInString, null, aInProductions);
    }

    private static void testStringWithError(
        String aInString,
        String aInErrorMessage,
        String ... aInProductions)
        throws IOException, UnrecognizedCharacterSequenceException
    {
        listener.reset();
        ParseResult lInvocation = parser.parse(
                aInString, listener);

        if (aInErrorMessage == null)
        {
            assertEquals(0, lInvocation.getErrors().size());
        }
        else
        {
            assertEquals(aInErrorMessage, lInvocation.getErrors().get(0));
        }
        assertEquals(aInProductions.length,
            listener.getReducedProductions().size());
        for (int i = 0; i < aInProductions.length; i++)
        {
            assertEquals(aInProductions[i],
                listener.getReducedProductions().get(i));
        }
    }
}