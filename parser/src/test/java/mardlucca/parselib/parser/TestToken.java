/*
 * File: TestToken.java
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

public enum TestToken
{
    ASSIGNMENT("="),
    CHARACTER("char"),
    CLOSE_PARENTHESIS(")"),
    EOF("$"),
    EQUALS("=="),
    FOR("for"),
    IDENTIFIER("id"),
    IF("if"),
    NUMBER("num"),
    OPEN_PARENTHESIS("("),
    PERIOD("."),
    PLUS("+"),
    STAR("*"),
    STRING("str"),
    SLASH("/");

    TestToken(String aInDisplay)
    {
        display = aInDisplay;
    }

    private String display;

    public static TestToken parse(String aInDisplayValue)
    {
        for (TestToken lToken : TestToken.values())
        {
            if (lToken.display.equals(aInDisplayValue))
            {
                return lToken;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return display;
    }
}
