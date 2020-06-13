/*
 * File: BaseTokenRecognizer.java
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

public abstract class BaseTokenRecognizer<T, V>
        implements TokenRecognizer<T, V> {
    private T token;

    private String failureReason;

    public BaseTokenRecognizer(T aInToken) {
        token = aInToken;
    }

    @Override
    public Token<T, V> getToken(String aInCharSequence) {
        return new Token<>(token, aInCharSequence, getValue(aInCharSequence));
    }

    @Override
    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String aInFailureReason) {
        failureReason = aInFailureReason;
    }

    @Override
    public void reset() {
        failureReason = null;
    }

    protected abstract V getValue(String aInCharSequence);

    @Override
    public boolean isIgnored() {
        return false;
    }
}
