/*
 * File: SymbolRecognizer.java
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

import java.util.function.Predicate;

public class SymbolRecognizer<T> extends BaseTokenRecognizer<T>
{
    private String value;

    private int index = 0;

    private boolean notAMatch = false;

    public SymbolRecognizer(T aInToken)
    {
        this(aInToken.toString(), null, aInToken);
    }

    public SymbolRecognizer(String aInValue, T aInToken)
    {
        this(aInValue, null, aInToken);
    }

    public SymbolRecognizer(
        String aInValue,
        Predicate<String> aInValidator,
        T aInToken)
    {
        super(aInToken);
        if (aInValidator != null && !aInValidator.test(aInValue))
        {
            throw new RuntimeException("Invalid symbol \"" + aInValue + '"');
        }
        value = aInValue;
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext)
    {
        if (notAMatch)
        {
            return MatchResult.NOT_A_MATCH;
        }

        if (index >= value.length()
            || value.charAt(index) != aInChar)
        {
            notAMatch = true;
            return MatchResult.NOT_A_MATCH;
        }

        index ++;

        return index < value.length() ?
            MatchResult.PARTIAL_MATCH : MatchResult.MATCH;
    }

    @Override
    public void reset()
    {
        super.reset();
        index = 0;
        notAMatch = false;
    }
}
