/*
 * File: TransformingRecognizer.java
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

import java.util.function.Function;

public class TransformingRecognizer<T, VF, VT>
        implements TokenRecognizer<T, VT> {
    private TokenRecognizer<T, VF> delegate;
    private Function<? super VF, ? extends VT> transform;

    public TransformingRecognizer(
            TokenRecognizer<T, VF> aInDelegate,
            Function<? super VF, ? extends VT> aInTransform) {
        delegate = aInDelegate;
        transform = aInTransform;
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext) {
        return delegate.test(aInChar, aInSyntacticContext);
    }

    @Override
    public MatchResult test(int aInChar) {
        return delegate.test(aInChar);
    }

    @Override
    public Token<T, VT> getToken(String aInCharSequence) {
        Token<T, VF> lToken = delegate.getToken(aInCharSequence);
        return new Token<>(
                lToken.getId(),
                aInCharSequence,
                transform.apply(lToken.getValue()));
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
