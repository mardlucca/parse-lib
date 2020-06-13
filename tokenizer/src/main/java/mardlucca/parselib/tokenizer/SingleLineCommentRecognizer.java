/*
 * File: SingleLineCommentRecognizer.java
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

public class SingleLineCommentRecognizer<T>
        extends BaseTokenRecognizer<T, String> {
    public static final String DEFAULT_CHAR_SEQUENCE = "//";

    private State state;

    private String charSequence;

    private int index;

    public SingleLineCommentRecognizer(String aInCharSequence) {
        super(null);
        charSequence = StringUtils.isBlank(aInCharSequence)
                ? DEFAULT_CHAR_SEQUENCE
                : aInCharSequence;
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext) {
        switch (state) {
            case READING_SEQUENCE:
                return handleReadingCharSequenceState(aInChar);
            case READING_COMMENT_LINE:
                return handleReadingCommentLineState(aInChar);
            case FINISHED:
        }
        return MatchResult.NOT_A_MATCH;
    }

    private MatchResult handleReadingCharSequenceState(int aInChar) {
        if (aInChar == charSequence.charAt(index)) {
            index++;
            if (index >= charSequence.length()) {
                state = State.READING_COMMENT_LINE;
                return MatchResult.MATCH;

            }
            return MatchResult.PARTIAL_MATCH;
        }

        state = State.FINISHED;
        return MatchResult.NOT_A_MATCH;
    }

    private MatchResult handleReadingCommentLineState(int aInChar) {
        if (aInChar == '\n' || aInChar == -1) {
            state = State.FINISHED;

            // new line will be removed in next invocation by the whitespace
            // recognizer
            return MatchResult.NOT_A_MATCH;
        }
        return MatchResult.MATCH;
    }

    @Override
    public void reset() {
        super.reset();
        state = State.READING_SEQUENCE;
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
        READING_SEQUENCE,
        READING_COMMENT_LINE,
        FINISHED
    }
}
