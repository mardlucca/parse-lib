/*
 * File: BooleanRecognizer.java
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

public class BooleanLiteralRecognizer<T>
        extends BaseTokenRecognizer<T, Boolean> {
    private static final int[] trueString = {'t', 'r', 'u', 'e'};
    private static final int[] falseString =  {'f', 'a', 'l', 's', 'e'};

    private int index = 0;
    private int[] candidate;
    private boolean notAMatch = false;

    BooleanLiteralRecognizer(T aInToken) {
        super(aInToken);
    }

    @Override
    protected Boolean getValue(String aInCharSequence) {
        return aInCharSequence.equals("true");
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext) {
        if (notAMatch) { return MatchResult.NOT_A_MATCH; }

        if (index == 0) {
            if (aInChar == trueString[0]) {
                candidate = trueString;
            } else if (aInChar == falseString[0]) {
                candidate = falseString;
            } else {
                return notAMatch();
            }
        } else {
            if (index >= candidate.length) {
                return notAMatch();
            }
            if (aInChar != candidate[index]) {
                return notAMatch();
            }
        }

        index++;
        return index == candidate.length
                ? MatchResult.MATCH
                : MatchResult.PARTIAL_MATCH;
    }

    @Override
    public void reset() {
        index = 0;
        candidate = null;
        notAMatch = false;
    }

    private MatchResult notAMatch() {
        notAMatch = true;
        return MatchResult.NOT_A_MATCH;
    }
}
