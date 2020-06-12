/*
 * File: SingleLineCommentRecognizerTest.java
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

import static org.junit.Assert.*;

public class SingleLineCommentRecognizerTest
{
    private static SingleLineCommentRecognizer recognizer =
            new SingleLineCommentRecognizer(null);

    @Test
    public void testSuccessCases()
    {
        test("//t", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
                MatchResult.MATCH);
        test("//t\n", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
                MatchResult.MATCH, MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testFailureCases()
    {
        test("aa", MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test("a//a", MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test("/a", MatchResult.PARTIAL_MATCH, MatchResult.NOT_A_MATCH);
        test("/ /", MatchResult.PARTIAL_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testErrorMessages()
    {
        testError("aa", null);
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

    private void testError(CharSequence aInString, String aInExpectedMessage)
    {
        recognizer.reset();
        for (int i = 0; i < aInString.length(); i++)
        {
            recognizer.test(aInString.charAt(i));
        }
        assertEquals(aInExpectedMessage, recognizer.getFailureReason());
    }
}