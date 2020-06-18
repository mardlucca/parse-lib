/*
 * File: WhitespaceRecognizerTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WhitespaceRecognizerTest
{
    private static WhitespaceRecognizer recognizer =
        new WhitespaceRecognizer();

    @Test
    public void test1()
    {
        test(" \t\n \t\nx", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("x ", MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
    }

    private void test(String aInString, MatchResult ... aInResults)
    {
        recognizer.reset();
        for (int i = 0; i < aInString.length(); i++)
        {
            assertEquals(aInResults[i], recognizer.test(aInString.charAt(i)));
        }
    }
}