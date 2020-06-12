/*
 * File: IdentifierRecognizer.java
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

public class IdentifierRecognizer<T> extends BaseTokenRecognizer<T, String>
{
    private int index = -1;

    private boolean matchFailure = false;

    public IdentifierRecognizer(T aInToken)
    {
        super(aInToken);
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext)
    {
        if (matchFailure) {
            return MatchResult.NOT_A_MATCH;
        }

        index ++;
        MatchResult lResult;

        if (index == 0)
        {
            lResult = Character.isJavaIdentifierStart(aInChar) ?
                MatchResult.MATCH : MatchResult.NOT_A_MATCH;
        }
        else
        {
            lResult = Character.isJavaIdentifierPart(aInChar) ?
                MatchResult.MATCH : MatchResult.NOT_A_MATCH;
        }

        if (lResult == MatchResult.NOT_A_MATCH)
        {
            matchFailure = true;
        }

        return lResult;
    }

    @Override
    public void reset()
    {
        super.reset();
        index = -1;
        matchFailure = false;
    }

    @Override
    public String getValue(String aInCharSequence) {
        return aInCharSequence;
    }
}
