/*
 * File: StringLiteralRecognizer.java
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

import org.apache.commons.text.StringEscapeUtils;

import java.util.Arrays;

public class StringLiteralRecognizer<T> extends BaseTokenRecognizer<T>
{
    private static final char DEFAULT_ESCAPE_CHARACTER = '\\';

    private static final char DEFAULT_DELIMITER_CHARACTER = '\"';

    private static final char[] DEFAULT_ESCAPE_SEQUENCES =
        {'n', '\"', '\'', 't', '\\', 'r'};

    private char escapeCharacter;

    private char delimiterCharacter;

    private char[] escapeSequences;

    private State state = State.INITIAL;

    public StringLiteralRecognizer(T aInToken)
    {
        this(DEFAULT_ESCAPE_CHARACTER, DEFAULT_DELIMITER_CHARACTER,
            DEFAULT_ESCAPE_SEQUENCES, aInToken);
    }

    public StringLiteralRecognizer(T aInToken, char aInDelimiterCharacter)
    {
        this(DEFAULT_ESCAPE_CHARACTER, aInDelimiterCharacter,
                DEFAULT_ESCAPE_SEQUENCES, aInToken);
    }

    public StringLiteralRecognizer(
        char aInEscapeCharacter,
        char aInDelimiterCharacter,
        char[] aInEscapeSequences,
        T aInToken)
    {
        super(aInToken);
        escapeCharacter = aInEscapeCharacter;
        delimiterCharacter = aInDelimiterCharacter;
        escapeSequences = aInEscapeSequences == null
                ? DEFAULT_ESCAPE_SEQUENCES
                : aInEscapeSequences;
        Arrays.sort(escapeSequences);
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext)
    {
        switch (state)
        {
            case INITIAL:
                return handleInitialState(aInChar);
            case READING_STRING:
                return handleReadingStringState(aInChar);
            case ESCAPE_SEQUENCE:
                return handleEscapeSequenceState(aInChar);
            case SUCCESS:
            case FAILURE:
        }
        return failure(null);
    }

    private MatchResult handleInitialState(int aInChar)
    {
        if (delimiterCharacter == aInChar)
        {
            state = State.READING_STRING;
            return MatchResult.PARTIAL_MATCH;
        }


        return failure(null);
    }


    private MatchResult handleReadingStringState(int aInChar)
    {
        if (delimiterCharacter == aInChar)
        {
            state = State.SUCCESS;
            return MatchResult.MATCH;
        }
        if (escapeCharacter == aInChar)
        {
            state = State.ESCAPE_SEQUENCE;
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar == '\n' || aInChar == -1)
        {
            return failure("Unterminated string literal");
        }

        // accepts the character and stays in the same "reading" state
        return MatchResult.PARTIAL_MATCH;
    }

    private MatchResult handleEscapeSequenceState(int aInChar)
    {
        if (Arrays.binarySearch(escapeSequences, (char) aInChar) >= 0)
        {
            state = State.READING_STRING;
            return MatchResult.PARTIAL_MATCH;
        }
        return failure("Not a valid escape sequence");
    }

    @Override
    public void reset()
    {
        super.reset();
        state = State.INITIAL;
    }

    @Override
    public Object getValue(String aInCharSequence)
    {
        if (aInCharSequence.length() < 3)
        {
            return "";
        }

        return StringEscapeUtils.unescapeJava(
            aInCharSequence.substring(1, aInCharSequence.length() - 1));
    }

    private MatchResult failure(String aInReason)
    {
        setFailureReason(aInReason);
        state = State.FAILURE;
        return MatchResult.NOT_A_MATCH;
    }

    private enum State
    {
        INITIAL,
        READING_STRING,
        ESCAPE_SEQUENCE,
        SUCCESS,
        FAILURE
    }
}
