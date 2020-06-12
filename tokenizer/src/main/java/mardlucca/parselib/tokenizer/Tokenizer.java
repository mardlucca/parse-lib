/*
 * File: Tokenizer.java
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

import java.io.IOException;

public interface Tokenizer<T> extends Iterable<Token<T, ?>>
{
    Token<T, ?> nextToken(Object aInSyntacticContext)
        throws IOException, UnrecognizedCharacterSequenceException;

    Token<T, ?> peekToken(Object aInSyntacticContext)
            throws IOException, UnrecognizedCharacterSequenceException;

    default Token<T, ?> nextToken()
            throws IOException, UnrecognizedCharacterSequenceException
    {
        return nextToken(null);
    }

    default Token<T, ?> peekToken()
            throws IOException, UnrecognizedCharacterSequenceException
    {
        return peekToken(null);
    }
}
