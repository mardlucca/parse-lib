/*
 * File: BooleanLiteralRecognizerTest.java
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
import static org.junit.Assert.assertTrue;

public class BooleanLiteralRecognizerTest
{

    @Test
    public void testSuccess()
    {
        BooleanLiteralRecognizer lRecognizer =
                Recognizers.booleans("BOOL").get();
        test(lRecognizer, "trues", MatchResult.PARTIAL_MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
                MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test(lRecognizer, "falses", MatchResult.PARTIAL_MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
                MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testFailure()
    {
        BooleanLiteralRecognizer lRecognizer =
                Recognizers.booleans("BOOL").get();
        test(lRecognizer, "truck", MatchResult.PARTIAL_MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test(lRecognizer, "farses", MatchResult.PARTIAL_MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testUpperCase() {
        BooleanLiteralRecognizer lRecognizer =
                Recognizers.booleans("BOOL").get();
        test(lRecognizer, "TRUES", MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test(lRecognizer, "FALSES", MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH);
        test(lRecognizer, "Trues", MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test(lRecognizer, "Falses", MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testYesNo() {
        BooleanLiteralRecognizer lRecognizer =
                Recognizers.booleans("BOOL").get();
        test(lRecognizer, "yes", MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test(lRecognizer, "no", MatchResult.NOT_A_MATCH,
                MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testGetValue()
    {
        BooleanLiteralRecognizer lRecognizer =
                Recognizers.booleans("BOOL").get();
        assertTrue(lRecognizer.getValue("true"));
        assertTrue(lRecognizer.getValue("true"));
    }

    private void test(BooleanLiteralRecognizer aInRecognizer,
        String aInString, MatchResult ... aInResults)
    {
        aInRecognizer.reset();
        for (int i = 0; i < aInString.length(); i++)
        {
            assertEquals(aInResults[i],
                    aInRecognizer.test(aInString.charAt(i)));
        }
    }
}