/*
 * File: Recognizers.java
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

import java.util.function.Supplier;

public class Recognizers {
    public static <T> Supplier<CharacterLiteralRecognizer<T>> characters(
            T aInToken) {
        return () -> new CharacterLiteralRecognizer<>(aInToken);
    }

    public static <T> Supplier<CharacterLiteralRecognizer<T>> characters(
            char aInEscapeCharacter,
            char aInDelimiterCharacter,
            char[] aInEscapeSequences,
            T aInToken) {
        return () -> new CharacterLiteralRecognizer<>(
                aInEscapeCharacter,
                aInDelimiterCharacter,
                aInEscapeSequences,
                aInToken);
    }

    public static <T> Supplier<IdentifierRecognizer<T>> identifiers(
            T aInToken) {
        return () -> new IdentifierRecognizer<>(aInToken);
    }

    public static <T>
    Supplier<MultiLineCommentRecognizer<T>> multiLineComments() {
        return () -> new MultiLineCommentRecognizer<>(null, null);
    }

    public static <T> Supplier<MultiLineCommentRecognizer<T>> multiLineComments(
            String aInInitialCharSequence,
            String aInEndCharSequence) {
        return () -> new MultiLineCommentRecognizer<>(
                aInInitialCharSequence, aInEndCharSequence);
    }

    public static <T> Supplier<NumberLiteralRecognizer<T>> numbers(
            T aInToken) {
        return () -> new NumberLiteralRecognizer<>(aInToken);
    }

    public static <T>
    Supplier<SingleLineCommentRecognizer<T>> singleLineComments() {
        return () -> new SingleLineCommentRecognizer<>(null);
    }

    public static <T>
    Supplier<SingleLineCommentRecognizer<T>> singleLineComments(
            String aInCharSequence) {
        return () -> new SingleLineCommentRecognizer<>(aInCharSequence);
    }

    public static <T> Supplier<StringLiteralRecognizer<T>> strings(T aInToken) {
        return () -> new StringLiteralRecognizer<>(aInToken);
    }

    public static <T> Supplier<StringLiteralRecognizer<T>> strings(
            T aInToken, char aInDelimiterCharacter) {
        return () -> new StringLiteralRecognizer<>(
                aInToken, aInDelimiterCharacter);
    }

    public static <T> Supplier<StringLiteralRecognizer<T>> strings(
            char aInEscapeCharacter,
            char aInDelimiterCharacter,
            char[] aInEscapeSequences,
            T aInToken) {
        return () -> new StringLiteralRecognizer<>(
                aInEscapeCharacter,
                aInDelimiterCharacter,
                aInEscapeSequences,
                aInToken);
    }

    public static <T> Supplier<SymbolRecognizer<T>> symbol(T aInToken) {
        return symbol(null, aInToken);
    }

    public static <T> Supplier<SymbolRecognizer<T>> symbol(
            String aInValue, T aInToken) {
        return () -> new SymbolRecognizer<>(aInValue, aInToken);
    }

    public static <T> Supplier<WhitespaceRecognizer<T>> whiteSpaces() {
        return WhitespaceRecognizer::new;
    }
}
