/*
 * File: MultiLineCommentRecognizer.java
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

import org.apache.commons.lang3.StringUtils;

public class MultiLineCommentRecognizer<T>
        extends BaseTokenRecognizer<T, String> {
    public static final String DEFAULT_START_CHAR_SEQUENCE = "/*";

    private State state;

    private String initialCharSequence;

    private String endCharSequence;

    private String substring;

    private int index;

    public MultiLineCommentRecognizer(
            String aInInitialCharSequence,
            String aInEndCharSequence) {
        super(null);
        initialCharSequence = StringUtils.isBlank(aInInitialCharSequence)
                ? DEFAULT_START_CHAR_SEQUENCE
                : aInInitialCharSequence;
        endCharSequence = StringUtils.isBlank(aInEndCharSequence)
                ? StringUtils.reverse(initialCharSequence)
                : aInEndCharSequence;
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext) {
        switch (state) {
            case READING_START_SEQUENCE:
                return handleReadingIntialCharSequenceState(aInChar);
            case LOOKING_FOR_END_SEQUENCE:
                return handleLookingForEndSequenceState(aInChar);
            case FINISHED:
        }
        return MatchResult.NOT_A_MATCH;
    }

    private MatchResult handleReadingIntialCharSequenceState(int aInChar) {
        if (aInChar == initialCharSequence.charAt(index)) {
            index++;
            if (index >= initialCharSequence.length()) {
                state = State.LOOKING_FOR_END_SEQUENCE;
                substring = "";
            }
            return MatchResult.PARTIAL_MATCH;
        }

        state = State.FINISHED;
        return MatchResult.NOT_A_MATCH;
    }

    private MatchResult handleLookingForEndSequenceState(int aInChar) {
        if (aInChar == -1) {
            setFailureReason("Unclosed comment");
            state = State.FINISHED;
            return MatchResult.FAILURE;
        }

        if (substring.length() == endCharSequence.length()) {
            // discard first character
            substring = substring.substring(1);
        }
        substring += (char) aInChar;

        if (substring.equals(endCharSequence)) {
            state = State.FINISHED;
            return MatchResult.MATCH;
        }

        return MatchResult.PARTIAL_MATCH;
    }

    @Override
    public void reset() {
        super.reset();
        state = State.READING_START_SEQUENCE;
        index = 0;
    }

    @Override
    public String getValue(String aInCharSequence) {
        return null;
    }

    @Override
    public boolean isIgnored() {
        return true;
    }

    private enum State {
        READING_START_SEQUENCE,
        LOOKING_FOR_END_SEQUENCE,
        FINISHED
    }
}
