/*
 * File: BasicTokenizerTest.java
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

package mardlucca.parselib.tokenizer;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static mardlucca.parselib.tokenizer.Recognizers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BasicTokenizerTest
{
    private BasicTokenizer.Builder<TestToken> builder;

    @Before
    public void setUp()
    {
        builder = new BasicTokenizer.Builder<TestToken>()
                .recognize(singleLineComments())
                .recognize(multiLineComments())
                .recognize(characters(TestToken.CHARACTER))
                .recognize(symbol("if", TestToken.IF))
                .recognize(symbol("for", TestToken.FOR))
                .recognize(numbers(TestToken.NUMBER))
                .recognize(strings(TestToken.STRING))
                .recognize(symbol("==", TestToken.EQUALS))
                .recognize(symbol("=", TestToken.ASSIGNMENT))
                .recognize(symbol("(", TestToken.OPEN_PARENTHESIS))
                .recognize(symbol(")", TestToken.CLOSE_PARENTHESIS))
                .recognize(symbol("/", TestToken.SLASH))
                .recognize(identifiers(TestToken.IDENTIFIER))
                .endOfFile(TestToken.EOF);
    }

    @Test
    public void testNextToken() throws Exception
    {
        Reader lReader =
            new StringReader("if(test==\"bla\\'h\")\n\ta=20l\n    b = 'c' ");

        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);
        test(lTokenizer.nextToken(), TestToken.IF,
            "if", "if", String.class);
        test(lTokenizer.nextToken(), TestToken.OPEN_PARENTHESIS,
            "(", "(", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
            "test", "test", String.class);
        test(lTokenizer.nextToken(), TestToken.EQUALS,
            "==", "==", String.class);
        test(lTokenizer.nextToken(), TestToken.STRING,
            "\"bla\\'h\"", "bla'h", String.class);
        test(lTokenizer.nextToken(), TestToken.CLOSE_PARENTHESIS,
            ")", ")", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
            "a", "a", String.class);
        test(lTokenizer.nextToken(), TestToken.ASSIGNMENT,
            "=", "=", String.class);
        test(lTokenizer.nextToken(), TestToken.NUMBER,
            "20l", 20L, Long.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
            "b", "b", String.class);
        test(lTokenizer.nextToken(), TestToken.ASSIGNMENT,
            "=", "=", String.class);
        test(lTokenizer.nextToken(), TestToken.CHARACTER,
            "'c'", 'c', Character.class);
        assertEquals(TestToken.EOF, lTokenizer.nextToken().getId());
    }

    private void test(Token<TestToken,?> aInToken, TestToken aInTokenId,
        String aInCharSequence, Object aInValue, Class<?> aInType)
    {
        assertEquals(aInTokenId, aInToken.getId());
        assertEquals(aInCharSequence, aInToken.getCharSequence());
        assertEquals(aInValue, aInToken.getValue());
        assertEquals(aInType, aInToken.getValue().getClass());
    }

    @Test
    public void testNextToken2() throws Exception
    {
        Reader lReader =
            new StringReader("ifa i");
        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);

        test(lTokenizer.peekToken(), TestToken.IDENTIFIER,
                "ifa", "ifa", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
            "ifa", "ifa", String.class);
        test(lTokenizer.peekToken(), TestToken.IDENTIFIER,
                "i", "i", String.class);
        test(lTokenizer.peekToken(), TestToken.IDENTIFIER,
                "i", "i", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
            "i", "i", String.class);
        Token<TestToken,?> lEOF = lTokenizer.nextToken();
        assertNotNull(lEOF);
        assertEquals(TestToken.EOF, lEOF.getId());
    }

    @Test
    public void testNextToken3() throws Exception
    {
        Reader lReader =
            new StringReader(".a .3 13.fb");
        builder.recognize(symbol(".", TestToken.PERIOD));
        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);

        test(lTokenizer.nextToken(), TestToken.PERIOD,
            ".", ".", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
            "a", "a", String.class);
        test(lTokenizer.nextToken(), TestToken.NUMBER,
            ".3", .3, Double.class);
        test(lTokenizer.nextToken(), TestToken.NUMBER,
            "13.f", 13.f, Float.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
            "b", "b", String.class);
        assertEquals(TestToken.EOF, lTokenizer.nextToken().getId());
    }

    @Test
    public void testUnrecognizedSequence()
        throws IOException, UnrecognizedCharacterSequenceException
    {
        Reader lReader =
            new StringReader("[12.3f");
        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);

        try
        {
            lTokenizer.nextToken();
        }
        catch (UnrecognizedCharacterSequenceException e)
        {
            assertEquals("Unrecognized character sequence: [",
                e.getMessage());
        }
        test(lTokenizer.nextToken(), TestToken.NUMBER,
            "12.3f", 12.3f, Float.class);
        assertEquals(TestToken.EOF, lTokenizer.nextToken().getId());
        // making sure calling again returns null again
        assertEquals(TestToken.EOF, lTokenizer.nextToken().getId());
    }

    @Test
    public void testUnrecognizedSequence2()
        throws IOException
    {
        Reader lReader =
            new StringReader("\"unterminated string\n12.3f");
        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);

        try
        {
            lTokenizer.nextToken();
        }
        catch (UnrecognizedCharacterSequenceException e)
        {
            assertEquals("Unterminated string literal: \"unterminated string\n",
                e.getMessage());
        }
    }

    @Test
    public void testToStringAndIterator()
    {
        Reader lReader =
            new StringReader("if");
        List<String> lList = new ArrayList<>();
        builder.build(lReader).forEach(
            aInToken -> lList.add(aInToken.toString()));
        assertEquals(2, lList.size());
        assertEquals("Token{id=if, charSequence='if', value='if', " +
            "valueType='java.lang.String'}", lList.get(0));
        assertEquals("Token{id=$}", lList.get(1));
    }

    @Test
    public void testCommentsVsSlash() throws Exception
    {
        Reader lReader =
                new StringReader("a / b // this is a comment");

        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
                "a", "a", String.class);
        test(lTokenizer.nextToken(), TestToken.SLASH,
                "/", "/", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
                "b", "b", String.class);
        assertEquals(TestToken.EOF, lTokenizer.nextToken().getId());

        lReader = new StringReader("/ /*this is a comment");

        lTokenizer = builder.build(lReader);
        test(lTokenizer.nextToken(), TestToken.SLASH,
                "/", "/", String.class);

        try
        {
            lTokenizer.nextToken();
        }
        catch (UnrecognizedCharacterSequenceException e)
        {
            assertEquals("Unclosed comment: /*this is a comment",
                    e.getMessage());
        }
    }

    @Test
    public void testSlashVsRegExpLiteral()
            throws IOException, UnrecognizedCharacterSequenceException
    {
        builder = new BasicTokenizer.Builder<TestToken>()
                .recognize(strings(TestToken.STRING, '/'))
                .recognize(symbol(TestToken.SLASH))
                .recognize(identifiers(TestToken.IDENTIFIER))
                .endOfFile(TestToken.EOF);

        Reader lReader = new StringReader("/regexp/ a/ba ");

        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);
        test(lTokenizer.nextToken(), TestToken.STRING,
                "/regexp/", "regexp", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
                "a", "a", String.class);
        test(lTokenizer.nextToken(), TestToken.SLASH,
                "/", "/", String.class);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
                "ba", "ba", String.class);
        assertEquals(TestToken.EOF, lTokenizer.nextToken().getId());
    }

    @Test
    public void testCustomRecognizer()
            throws IOException, UnrecognizedCharacterSequenceException
    {
        builder = new BasicTokenizer.Builder<TestToken>()
                .recognize(() ->
                        new IdentifierRecognizer<>(TestToken.IDENTIFIER))
                .endOfFile(TestToken.EOF);
        Reader lReader = new StringReader("ba ");
        BasicTokenizer<TestToken> lTokenizer = builder.build(lReader);
        test(lTokenizer.nextToken(), TestToken.IDENTIFIER,
                "ba", "ba", String.class);
        assertEquals(TestToken.EOF, lTokenizer.nextToken().getId());
    }
}