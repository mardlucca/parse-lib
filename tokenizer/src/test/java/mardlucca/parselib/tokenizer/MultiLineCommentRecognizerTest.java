/*
 * File: MultiLineCommentRecognizerTest.java
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

import static mardlucca.parselib.tokenizer.MatchResult.*;
import static org.junit.Assert.*;

public class MultiLineCommentRecognizerTest
{
    private static MultiLineCommentRecognizer recognizer =
            new MultiLineCommentRecognizer();

    @Test
    public void testSuccessCases()
    {
        test("/**/a", PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH
                , MATCH, NOT_A_MATCH);
        test("/*s\"\'d */a", PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH
                , PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH
                , PARTIAL_MATCH, MATCH, NOT_A_MATCH);
        test("/*\n\t\n*/a", PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH
                , PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, MATCH,
                NOT_A_MATCH);
        test("/*****/a", PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH
                , PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH, MATCH,
                NOT_A_MATCH);
        test("/*/**/*", PARTIAL_MATCH, PARTIAL_MATCH, PARTIAL_MATCH,
                PARTIAL_MATCH, PARTIAL_MATCH, MATCH, NOT_A_MATCH);
    }

    @Test
    public void testFailureCases()
    {
        test("aa", NOT_A_MATCH, NOT_A_MATCH);
        test("a/**/", NOT_A_MATCH, NOT_A_MATCH, NOT_A_MATCH, NOT_A_MATCH,
                NOT_A_MATCH);
    }

    @Test
    public void testErrorMessages()
    {
        // Cheating. Simulating EOF
        testError("/*aa\naaa* /", "Unclosed comment");
    }

    @Test
    public void testGetValue()
    {
        assertNull("//asjdk", recognizer.getValue("\"\""));
    }

    private void test(String aInString, MatchResult ... aInResults)
    {
        recognizer.reset();
        assertEquals(aInResults.length, aInString.length());
        for (int i = 0; i < aInString.length(); i++)
        {
            assertEquals(aInResults[i], recognizer.test(aInString.charAt(i)));
        }
    }

    private void testError(String aInString, String aInExpectedMessage)
    {
        recognizer.reset();
        Reader in = new StringReader(aInString);

        int lChar;
        do
        {
            try
            {
                lChar = in.read();
                recognizer.test(lChar);
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