/*
 * File: NumberLiteralRecognizerTest.java
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

public class NumberLiteralRecognizerTest
{
    private static final NumberLiteralRecognizer recognizer =
        new NumberLiteralRecognizer<>("N");

    @Test
    public void testIntegralNumbers()
    {
        test("0", MatchResult.MATCH);
        test("0l", MatchResult.MATCH, MatchResult.MATCH);

        test("1", MatchResult.MATCH);
        test("1l", MatchResult.MATCH, MatchResult.MATCH);

        test("12", MatchResult.MATCH, MatchResult.MATCH);
        test("12l", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
    }

    @Test
    public void testFloatingPointNumbers()
    {
        test("0d", MatchResult.MATCH, MatchResult.MATCH);
        test("0f", MatchResult.MATCH, MatchResult.MATCH);

        test("1d", MatchResult.MATCH, MatchResult.MATCH);
        test("1f", MatchResult.MATCH, MatchResult.MATCH);

        test("12d", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("12f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test(".0", MatchResult.PARTIAL_MATCH, MatchResult.MATCH);
        test(".0l", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test(".0f", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test(".0d", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("1.0", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("1.0l", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("1.0f", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("1.0d", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("0.0", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("0.0l", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("0.0f", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("0.0d", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("09.0", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH);
        test("08.0l", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("07.0f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
        test("06.0d", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
    }

    @Test
    public void testOctalNumbers()
    {
        test("01d", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
        test("01f", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
        test("01l", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
        test("076", MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
        test("08f", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH);
        test("091d", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH);
        test("019f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH);
        test("019l", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testHexadecimalNumbers()
    {
        test("x1", MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test("1x1", MatchResult.MATCH, MatchResult.NOT_A_MATCH,
            MatchResult.NOT_A_MATCH);
        test("0x12l", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
        test("0xfdag", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("0x.1", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH);
        test("0xfd.", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("0xeab", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH, MatchResult.MATCH);
        test("0xe-1", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH,
            MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testExponents()
    {
        test("1e1l", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("1e1f", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH);
        test("1e1d", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH);

        test("0e1l", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("0e1f", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH);
        test("0e1d", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH);

        test("1.e1l", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("1.e1f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("1.e1d", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test(".0e1", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH);

        test("0.e1l", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("0.e1f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("0.e1d", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("07e1l", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("07e1f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("07e1d", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("08e1l", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("08e1f", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("08e1d", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("09.e1l", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("09.e1f", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("09.e1d", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("019.e1l", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("019.e1f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);
        test("019.e1d", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.MATCH);

        test("0x1.e1d", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH,
            MatchResult.NOT_A_MATCH, MatchResult.NOT_A_MATCH,
            MatchResult.NOT_A_MATCH);

        test("019.e-1l", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("019.e-1f", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH);
        test("019.e-1d", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.MATCH, MatchResult.MATCH);

        test("019.e+1l", MatchResult.MATCH, MatchResult.MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
                MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("019.e+1f", MatchResult.MATCH, MatchResult.MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
                MatchResult.MATCH, MatchResult.MATCH);
        test("019.e+1d", MatchResult.MATCH, MatchResult.MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
                MatchResult.PARTIAL_MATCH, MatchResult.PARTIAL_MATCH,
                MatchResult.MATCH, MatchResult.MATCH);

    }

    @Test
    public void testNotMatchExamples()
    {
        test("x", MatchResult.NOT_A_MATCH);
        test("1x", MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("1d1", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("1f1", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("1l1", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);

        test("1ea", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.NOT_A_MATCH);
        test("1e-a", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.NOT_A_MATCH);
        test("1e-1a", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);

        test(".a", MatchResult.PARTIAL_MATCH, MatchResult.NOT_A_MATCH);
        test(".1a", MatchResult.PARTIAL_MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);

        test("0a", MatchResult.MATCH, MatchResult.NOT_A_MATCH);
        test("01a", MatchResult.MATCH, MatchResult.MATCH,
            MatchResult.NOT_A_MATCH);
        test("09a", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.NOT_A_MATCH);
        test("0xx", MatchResult.MATCH, MatchResult.PARTIAL_MATCH,
            MatchResult.NOT_A_MATCH);
    }

    @Test
    public void testGetErrorMessages()
    {
        testError(".g", "Floating point number missing decimal value");
        testError("1eg", "Malformed floating point literal");
        testError("1e-g", "Malformed floating point literal");
        testError("09g", "Invalid octal number. Integer number too large.");
        testError("0xg", "Hexadecimal numbers must contain at least one " +
            "hexadecimal digit");
    }

    @Test
    public void testGetValue()
    {
        testValue("10", 10);
        testValue("10d", 10d);
        testValue("10D", 10D);
        testValue("10f", 10f);
        testValue("10F", 10F);
        testValue("10l", 10l);
        testValue("10L", 10L);
        testValue("10.0f", 10.0f);
        testValue("10.0d", 10.0d);
        testValue("10.0e1f", 10.0e1f);
        testValue("10.0e2d", 10.0e2d);
        testValue("10.0e-1f", 10.0e-1f);
        testValue("10.0e-2d", 10.0e-2d);
        testValue("10.0e+1f", 10.0e+1f);
        testValue("10.0e+2d", 10.0e+2d);
        testValue("10e1", 10e1);

        testValue("0x1", 0x1);
        testValue("0xe", 0xe);
        testValue("0xead", 0xead);
        testValue("0xeal", 0xeal);
        testValue("0xeaf", 0xeaf);

        testValue("07", 7);
        testValue("076", 076);
        testValue("076l", 076l);
        testValue("0e20", 0e20);
        testValue("0d", 0d);
        testValue("0e1", 0e1);
        testValue("0f", 0f);
        testValue("07d", 07d);
        testValue("07e1", 07e1);
        testValue("07f", 07f);
        testValue("078d", 078d);
        testValue("078e1", 078e1);
        testValue("078f", 078f);
    }

    private void test(String aInString, MatchResult ... aInResults)
    {
        recognizer.reset();
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

    private void testValue(String aInString, Object aInValue)
    {
        recognizer.reset();
        for (int i = 0; i < aInString.length(); i++)
        {
            recognizer.test(aInString.charAt(i));
        }
        assertEquals(aInValue, recognizer.getValue(aInString));
        assertEquals(aInValue.getClass(),
            recognizer.getValue(aInString).getClass());
    }
}