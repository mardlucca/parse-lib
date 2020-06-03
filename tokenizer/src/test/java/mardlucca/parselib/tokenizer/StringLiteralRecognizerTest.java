/*
 * File: StringLiteralRecognizerTest.java
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

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class StringLiteralRecognizerTest
{
    private static StringLiteralRecognizer recognizer =
        new StringLiteralRecognizer("S");

    @Test
    public void testSuccessCases()
    {
        test("\"\"x", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("\"a\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("\" a\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("\" \t\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("\"  \"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("\" \\n\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("\" \\\"\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("\" \\\"\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("\" \\t\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("\" \\\\\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("\" \\r\"x", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testFailureCases()
    {
        test("aa", MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test("\"\\x\"", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test("\"\nx", MatchResult.PARTIAL_MATCH, MatchResult.NOT_A_MATCH,
            MatchResult.NOT_A_MATCH);
        test("\"a\nx", MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testErrorMessages()
    {
        testError("aa", null);
        testError("\"\"", null);
        testError("\"a\n", "Unterminated string literal");
        testError("\"ab", "Unterminated string literal");
        testError("\"\\p", "Not a valid escape sequence");
    }

    @Test
    public void testGetValue()
    {
        assertEquals("", recognizer.getValue("\"\""));
        assertEquals("a", recognizer.getValue("\"a\""));
        assertEquals("ab", recognizer.getValue("\"ab\""));
        assertEquals("a\nb", recognizer.getValue("\"a\\nb\""));
    }

    private void test(String aInString, MatchResult ... aInResults)
    {
        recognizer.reset();
        for (int i = 0; i < aInString.length(); i++)
        {
            assertEquals(aInResults[i], recognizer.test(aInString.charAt(i)));
        }
    }

    static void testError(String aInString, String aInExpectedMessage)
    {
        recognizer.reset();
        Reader in = new StringReader(aInString);

        int lChar;
        do
        {
            try
            {
                lChar = in.read();
                if (recognizer.test(lChar) == MatchResult.NOT_A_MATCH)
                {
                    break;
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        while (lChar >= 0);
        assertEquals(aInExpectedMessage, recognizer.getFailureReason());
    }
}
