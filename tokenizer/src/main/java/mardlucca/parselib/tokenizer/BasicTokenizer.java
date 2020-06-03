/*
 * File: BasicTokenizer.java
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
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BasicTokenizer<T> implements Tokenizer<T>
{
    private List<TokenRecognizer<T>> recognizers;

    private Reader reader;

    private T endOfFile;

    private Token<T> peekedToken;

    Deque<Integer> buffer = new ArrayDeque<>();

    private BasicTokenizer(
            List<TokenRecognizer<T>> aInRecognizers,
            Reader aInReader,
            T aInEndOfFile)
    {
        recognizers = aInRecognizers;
        reader = aInReader;
        endOfFile = aInEndOfFile;
    }

    @Override
    public Token<T> nextToken(Object aInSyntacticContext)
            throws IOException, UnrecognizedCharacterSequenceException
    {
        return nextToken(false, aInSyntacticContext);
    }

    @Override
    public Token<T> peekToken(Object aInSyntacticContext)
            throws IOException, UnrecognizedCharacterSequenceException
    {
        return nextToken(true, aInSyntacticContext);
    }

    private Token<T> nextToken(boolean aInPeek, Object aInSyntacticContext)
        throws IOException, UnrecognizedCharacterSequenceException
    {
        if (peekedToken != null )
        {
            if (aInPeek)
            {
                // still peeking, so return same peeked token
                return peekedToken;
            }
            // not peeking anymore, so we consume previously peeked token
            Token<T> lToken = peekedToken;
            peekedToken = null;
            return lToken;
        }

        // reset stuff
        recognizers.forEach(TokenRecognizer::reset);
        List<TokenRecognizer<T>> lRecognizersLeft =
                new LinkedList<>(recognizers);
        TokenRecognizer<T> lCandidate = null;
        TokenRecognizer<T> lPartialCandidate = null;
        int lCandidateStringLength = 0;
        List<Integer> lCharactersRead = new ArrayList<>();

        do
        {
            int lCurrentCharacter = nextChar();
            if (lCurrentCharacter == -1 && lCharactersRead.isEmpty())
            {
                // end of file/stream.
                return new Token<>(endOfFile, null, null);
            }

            lCharactersRead.add(lCurrentCharacter);

            for (Iterator<TokenRecognizer<T>> lIterator =
                lRecognizersLeft.iterator(); lIterator.hasNext(); )
            {
                TokenRecognizer<T> lRecognizer = lIterator.next();
                MatchResult lMatchResult = lRecognizer.test(
                        lCurrentCharacter, aInSyntacticContext);
                if (lMatchResult == MatchResult.NOT_A_MATCH)
                {
                    // discard recognizer as we know it will not match the final
                    // string
                    lIterator.remove();
                }
                else if (lMatchResult == MatchResult.MATCH)
                {
                    // if not candidates in this pass, we found one
                    if (lCandidateStringLength < lCharactersRead.size())
                    {
                        lCandidateStringLength = lCharactersRead.size();
                        lCandidate = lRecognizer;
                    }
                    // else we already have a candidate, so we prioritize the
                    // first
                }
                else
                {
                    // else we have a partial match. Partial matches are not
                    // considered candidates as we don't know for sure if they
                    // will match
                    lPartialCandidate = lRecognizer;
                }
            }
        } while (lRecognizersLeft.size() > 0);
        // because we left the loop, no recognizers recognized the current
        // character, so it must belong to the next token. We let it be and
        // proceed with what we know so far

        // any characters that were read that are not present in the candidate
        // string must be put back into a buffer so we can start the next pass
        // from them
        for (int i = lCharactersRead.size() - 1;
                i >= lCandidateStringLength;
                i--)
        {
            buffer.push(lCharactersRead.get(i));
        }

        if (lCandidate == null)
        {
            // the previous pass did not produce a match, so we have an error
            String lDetails = null;
            if (lPartialCandidate != null)
            {
                // the last pass produced at least one partial match. We extract
                // from it a description of what when wrong.
                lDetails = lPartialCandidate.getFailureReason();
            }
            // else we can't really tell what went wrong, we just produce a
            // generic error. If the buffer at this point contains only the
            // invalid character we clear the buffer so we may be able to
            // proceed tokenizing
            if (buffer.size() == 1)
            {
                buffer.clear();
            }

            throw new UnrecognizedCharacterSequenceException(
                    toString(lCharactersRead, lCharactersRead.size()),
                    lDetails);
        }

        T lReturn = lCandidate.getToken();

        if (lReturn == null)
        {
            // we found a white space sequence so we discard it and look again
            return nextToken(aInPeek, aInSyntacticContext);
        }

        String lToken = toString(lCharactersRead, lCandidateStringLength);
        Token<T> lNewToken = new Token<>(lReturn, lToken,
                lCandidate.getValue(lToken));
        if (aInPeek)
        {
            peekedToken = lNewToken;
        }
        return lNewToken;
    }

    private int nextChar() throws IOException
    {
        Integer lCharacter = buffer.isEmpty() ? null : buffer.pop();
        return lCharacter == null ? reader.read() : lCharacter;
    }

    public static String toString(
            List<Integer> aInCharactersRead,
            int aInLength)
    {
        StringBuilder lBuilder = new StringBuilder();
        for (int i = 0; i < aInLength; i++)
        {
            lBuilder.append((char) aInCharactersRead.get(i).intValue());
        }
        return lBuilder.toString();
    }

    public static class Builder<T>
    {
        private T characterLiterals;

        private T identifiers;

        private T numberLiterals;

        private Map<Character, T> stringLiterals = new HashMap<>();

        private T endOfFile;

        private Map<String, T> keywords = new HashMap<>();

        private Map<String, T> symbols = new HashMap<>();

        private String singleLineComments;

        private String multiLineCommentsStart;

        private String multiLineCommentsEnd;

        private List<Supplier<TokenRecognizer<T>>> custom = new ArrayList<>();

        public Builder()
        {
        }

        public Builder<T> characterLiterals(T aInToken)
        {
            characterLiterals = aInToken;
            return this;
        }

        public Builder<T> identifiers(T aInToken)
        {
            identifiers = aInToken;
            return this;
        }

        public Builder<T> numberLiterals(T aInToken)
        {
            numberLiterals = aInToken;
            return this;
        }

        public Builder<T> stringLiterals(T aInToken)
        {
            return stringLiterals(aInToken, '"');
        }

        public Builder<T> stringLiterals(T aInToken, char aInDelimiterChar)
        {
            stringLiterals.put(aInDelimiterChar, aInToken);
            return this;
        }

        public Builder<T> keyword(T aInToken)
        {
            return keyword(aInToken.toString(), aInToken);
        }

        public Builder<T> keyword(String aInKeyword, T aInToken)
        {
            keywords.put(aInKeyword, aInToken);
            return this;
        }

        public Builder<T> symbol(T aInToken)
        {
            return symbol(aInToken.toString(), aInToken);
        }

        public Builder<T> symbol(String aInSymbol, T aInToken)
        {
            symbols.put(aInSymbol, aInToken);
            return this;
        }

        public Builder<T> endOfFile(T aInToken)
        {
            endOfFile = aInToken;
            return this;
        }

        public Builder<T> singleLineComments(String aInSingleLineComments)
        {
            singleLineComments = aInSingleLineComments == null
                    ? SingleLineCommentRecognizer.DEFAULT_CHAR_SEQUENCE
                    : aInSingleLineComments;
            return this;
        }

        public Builder<T> multiLineComments(String aInMultiLineCommentsStart)
        {
            multiLineCommentsStart = aInMultiLineCommentsStart == null
                    ? MultiLineCommentRecognizer.DEFAULT_START_CHAR_SEQUENCE
                    : aInMultiLineCommentsStart;
            return this;
        }

        public Builder<T> multiLineComments(String aInMultiLineCommentsStart,
                String aInMultiLineCommentsEnd)
        {
            multiLineCommentsStart = aInMultiLineCommentsStart;
            multiLineCommentsEnd = aInMultiLineCommentsEnd;
            return this;
        }

        public Builder<T> recognizer(Supplier<TokenRecognizer<T>>
                aInRecognizerSupplier)
        {
            custom.add(aInRecognizerSupplier);
            return this;
        }

        public BasicTokenizer<T> build(Reader aInReader)
        {
            if (endOfFile == null)
            {
                throw new RuntimeException(
                    "End of file token must be specified");
            }

            List<TokenRecognizer<T>> lRecognizers = new ArrayList<>();
            lRecognizers.addAll(
                    custom.stream().map(Supplier::get).collect(
                            Collectors.toList()));

            if (singleLineComments != null)
            {
                lRecognizers.add(
                        new SingleLineCommentRecognizer<>(singleLineComments));
            }
            if (multiLineCommentsStart != null)
            {
                lRecognizers.add(
                        new MultiLineCommentRecognizer<>(
                                multiLineCommentsStart, multiLineCommentsEnd));
            }

            symbols.forEach((aInSymbol, aInToken) ->
                lRecognizers.add(
                    new SymbolRecognizer<>(aInSymbol, aInToken)));

            if (characterLiterals != null)
            {
                lRecognizers.add(
                    new CharacterLiteralRecognizer<>(characterLiterals));
            }

            stringLiterals.forEach((aInChar, aInToken) ->
                    lRecognizers.add(new StringLiteralRecognizer<>(
                            aInToken, aInChar)));

            if (numberLiterals != null)
            {
                lRecognizers.add(
                        new NumberLiteralRecognizer<>(numberLiterals));
            }

            keywords.forEach((aInKeyword, aInToken) ->
                    lRecognizers.add(
                            new SymbolRecognizer<>(aInKeyword, aInToken))
            );

            if (identifiers != null)
            {
                lRecognizers.add(
                    new IdentifierRecognizer<>(identifiers));
            }

            lRecognizers.add(new WhitespaceRecognizer<>());

            return new BasicTokenizer<>(lRecognizers, aInReader, endOfFile);
        }
    }

    @Override
    public Iterator<Token<T>> iterator()
    {
        return new Iterator<Token<T>>()
        {
            Token<T> next;

            boolean reachedTheEnd = false;

            @Override
            public boolean hasNext()
            {
                if (reachedTheEnd)
                {
                    return false;
                }

                if (next == null)
                {
                    try
                    {
                        next = nextToken();
                        reachedTheEnd = Objects.equals(next.getId(), endOfFile);
                    }
                    catch (IOException
                        | UnrecognizedCharacterSequenceException e)
                    {
                        throw new RuntimeException(e);
                    }

                    if (next == null)
                    {
                        reachedTheEnd = true;
                    }
                }
                return next != null;
            }

            @Override
            public Token<T> next()
            {
                Token<T> lNext = next;
                next = null;
                return lNext;
            }
        };
    }
}
