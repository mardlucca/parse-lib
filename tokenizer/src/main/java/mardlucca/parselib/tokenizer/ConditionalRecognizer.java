/*
 * File: ConditionalRecognizer.java
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

public class ConditionalRecognizer<T, V>
        implements TokenRecognizer<T, V> {
    private TokenRecognizer<T, V> delegate;
    private Predicate<Object> predicate;

    ConditionalRecognizer(TokenRecognizer<T, V> aInDelegate,
                          Predicate<Object> aInPredicate) {
        delegate = aInDelegate;
        predicate = aInPredicate;
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext) {
        if (!predicate.test(aInSyntacticContext)) {
            return MatchResult.NOT_A_MATCH;
        }
        return delegate.test(aInChar, aInSyntacticContext);
    }

    @Override
    public Token<T, V> getToken(String aInCharSequence) {
        return delegate.getToken(aInCharSequence);
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public String getFailureReason() {
        return delegate.getFailureReason();
    }

    @Override
    public boolean isIgnored() {
        return delegate.isIgnored();
    }
}
