/*
 * File: Token.java
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

public class Token<T, V> {
    private T id;

    private String charSequence;

    private V value;

    Token(T aInId, String aInCharSequence, V aInValue) {
        id = aInId;
        charSequence = aInCharSequence;
        value = aInValue;
    }

    public T getId() {
        return id;
    }

    public String getCharSequence() {
        return charSequence;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (charSequence == null) {
            return "Token{id=" + id + "}";
        }
        return "Token{" +
            "id=" + id +
            ", charSequence='" + charSequence + '\'' +
            ", value='" + value + '\'' +
            ", valueType='" + value.getClass().getName() + "\'}";
    }
}
