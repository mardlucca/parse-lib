/*
 * File: SymbolRecognizerTest.java
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

public class SymbolRecognizerTest
{

    @Test
    public void testSuccess() throws Exception
    {
        SymbolRecognizer lRecognizer = new SymbolRecognizer("<");
        test(lRecognizer, "<x", MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        lRecognizer.reset();
        test(lRecognizer, ">x", MatchResult.NOT_A_MATCH,
            MatchResult.NOT_A_MATCH);

        lRecognizer = new SymbolRecognizer("if");
        test(lRecognizer, "ifx", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        lRecognizer.reset();
        test(lRecognizer, "ixx", MatchResult.PARTIAL_MATCH,
            MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testInvalidPredicate()
    {
        try
        {
            new SymbolRecognizer("<", aInSymbol -> false, "<");
            fail("Should have thrown exception");
        }
        catch (RuntimeException e)
        {
            assertEquals("Invalid symbol \"<\"", e.getMessage());
        }
    }

    @Test
    public void testGetValue()
    {
        SymbolRecognizer lRecognizer = new SymbolRecognizer("<");
        assertEquals("<", lRecognizer.getValue("<"));
    }

    private void test(SymbolRecognizer aInSymbolRecognizer,
        String aInString, MatchResult ... aInResults)
    {
        aInSymbolRecognizer.reset();
        for (int i = 0; i < aInString.length(); i++)
        {
            assertEquals(aInResults[i],
                aInSymbolRecognizer.test(aInString.charAt(i)));
        }
    }
}