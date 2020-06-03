/*
 * File: LRParser.java
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

package mardlucca.parselib.parser;

import mardlucca.parselib.tokenizer.Token;
import mardlucca.parselib.tokenizer.Tokenizer;
import mardlucca.parselib.tokenizer.TokenizerFactory;
import mardlucca.parselib.tokenizer.UnrecognizedCharacterSequenceException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class LRParser<T, NT>
{
    private int stateSequence = 0;

    private List<State> states = new ArrayList<>();

    private ErrorAction defaultErrorAction =
        new ErrorAction("Syntax error");

    private TokenizerFactory<T> tokenizerFactory;

    private Grammar<NT> grammar;

    protected LRParser(
            TokenizerFactory<T> aInTokenizerFactory,
            Grammar<NT> aInGrammar)
    {
        tokenizerFactory = aInTokenizerFactory;
        grammar = aInGrammar;
    }

    public ParseResult parse(String aInString, ReduceListener<NT> aInListener)
            throws IOException, UnrecognizedCharacterSequenceException
    {
        return parse(new StringReader(aInString), aInListener);
    }

    public ParseResult parse(
        Reader aInReader, ReduceListener<NT> aInListener)
        throws IOException, UnrecognizedCharacterSequenceException
    {
        Tokenizer<T> lTokenizer = tokenizerFactory.newTokenizer(aInReader);
        ParseInvocation lInvocation = new ParseInvocation(
            states.get(0), lTokenizer, aInListener);

        lInvocation.currentToken = lTokenizer.nextToken(
                lInvocation.currentState);
        Action lNextAction;
        do
        {
            lNextAction = lInvocation.nextAction();
            if (lNextAction == null)
            {
                // no action found, so we're in error
                lNextAction = defaultErrorAction;
            }
        }
        while (lNextAction.execute(lInvocation));

        return lInvocation;
    }

    public Grammar<NT> getGrammar()
    {
        return grammar;
    }

    protected State newState()
    {
        State lNewState = new State(stateSequence++);
        states.add(lNewState);
        return lNewState;
    }

    protected State getState(int aInIndex)
    {
        return states.get(aInIndex);
    }

    public class State
    {
        private int number;
        private Map<NT, GotoAction> goTos = new HashMap<>();
        private Map<T, Action> actions = new HashMap<>();

        private State(int aInNumber)
        {
            number = aInNumber;
        }

        public int getNumber()
        {
            return number;
        }

        public State accept(T aInTerminal)
        {
            actions.put(aInTerminal, new AcceptAction());
            return this;
        }

        public State error(T aInTerminal, String aInMessage)
        {
            actions.put(aInTerminal, new ErrorAction(aInMessage));
            return this;
        }

        public State goTo(NT aInSymbol, int aInState)
        {
            goTos.put(aInSymbol, new GotoAction(aInState));
            return this;
        }

        public State shift(T aInTerminal, int aInState)
        {
            actions.put(aInTerminal, new ShiftAction(aInState));
            return this;
        }

        public State shiftIf(T aInTerminal, T aInLookAhead, int aInState)
        {
            Action lPreviousAction = actions.get(aInTerminal);

            actions.put(aInTerminal, new ConditionalAction(
                    aInLookAhead,
                    new ShiftAction(aInState),
                    lPreviousAction == null
                            ? defaultErrorAction
                            : lPreviousAction));

            return this;
        }

        public State reduce(T aInTerminal, int aInProduction)
        {
            actions.put(aInTerminal,
                    new ReduceAction(grammar.getProduction(aInProduction)));
            return this;
        }

        public State reduceIf(T aInTerminal, T aInLookAhead, int aInProduction)
        {
            Action lPreviousAction = actions.get(aInTerminal);

            actions.put(aInTerminal, new ConditionalAction(
                    aInLookAhead,
                    new ReduceAction(grammar.getProduction(aInProduction)),
                    lPreviousAction == null
                            ? defaultErrorAction
                            : lPreviousAction));

            return this;
        }

        public boolean hasAction(T aInTerminal)
        {
            return actions.containsKey(aInTerminal);
        }
    }

    private abstract class Action
    {
        abstract boolean execute(ParseInvocation aInInvocation)
            throws IOException, UnrecognizedCharacterSequenceException;
    }

    private class AcceptAction extends Action
    {
        private AcceptAction()
        {
        }

        @Override
        public boolean execute(ParseInvocation aInInvocation)
        {
            aInInvocation.value = aInInvocation.symbolStack.pop().getValue();
            return false;
        }
    }

    private class ErrorAction extends Action
    {
        private String message;

        private ErrorAction(String aInMessage)
        {
            message = aInMessage;
        }

        @Override
        public boolean execute(ParseInvocation aInInvocation)
        {
            aInInvocation.errors.add(message);
            return false;
        }
    }

    private class GotoAction extends Action
    {
        private int state;

        private GotoAction(int aInState)
        {
            state = aInState;
        }

        @Override
        public boolean execute(ParseInvocation aInInvocation)
        {
            State lNextState = states.get(state);
            aInInvocation.stateStack.push(aInInvocation.currentState);
            aInInvocation.symbolStack.push(aInInvocation.goTo);
            aInInvocation.currentState = lNextState;
            aInInvocation.goTo = null;
            return true;
        }

        @Override
        public String toString()
        {
            return "GO" + state;
        }
    }

    private class ShiftAction extends Action
    {
        private int state;

        private ShiftAction(int aInState)
        {
            state = aInState;
        }

        @Override
        public boolean execute(ParseInvocation aInInvocation)
            throws IOException, UnrecognizedCharacterSequenceException
        {
            State lNextState = states.get(state);
            aInInvocation.stateStack.push(aInInvocation.currentState);
            aInInvocation.symbolStack.push(
                new Terminal<>(aInInvocation.currentToken));

            aInInvocation.currentState = lNextState;
            aInInvocation.currentToken =
                    aInInvocation.tokenizer.nextToken(
                            aInInvocation.currentState);
            return true;
        }

        @Override
        public String toString()
        {
            return "SFT " + state ;
        }
    }

    private class ReduceAction extends Action
    {
        private Production<NT> production;

        private int numberOfSymbols;

        public ReduceAction(
            Production<NT> aInProduction)
        {
            production = aInProduction;
            numberOfSymbols = production.getRightHandSide().length;
        }

        @Override
        public boolean execute(ParseInvocation aInInvocation)
        {
            Object[] lValues = new Object[numberOfSymbols];
            for (int i = numberOfSymbols -1; i >= 0; i--)
            {
                aInInvocation.currentState = aInInvocation.stateStack.pop();
                lValues[i] = aInInvocation.symbolStack.pop().getValue();
            }

            Object lNewValue = null;
            try
            {
                lNewValue = aInInvocation.listener == null ?
                    null : aInInvocation.listener.onReduce(production, lValues);
                aInInvocation.goTo = new NonTerminal<>
                        (production.getLeftHandSide(), lNewValue);

                return true;
            }
            catch (ParsingException pe)
            {
                aInInvocation.errors.add(pe.getMessage());
            }
            return false;
        }

        @Override
        public String toString()
        {
            return production.toString();
        }
    }

    private class ConditionalAction extends Action
    {
        private T nextToken;
        private Action equalsAction;
        private Action notEqualsAction;

        public ConditionalAction(T aInNextToken,
                Action aInEqualsAction,
                Action aInNotEqualsAction)
        {
            nextToken = aInNextToken;
            equalsAction = aInEqualsAction;
            notEqualsAction = aInNotEqualsAction;
        }

        @Override
        public boolean execute(ParseInvocation aInInvocation)
                throws IOException, UnrecognizedCharacterSequenceException
        {
            Token<T> lNextToken =
                    aInInvocation.tokenizer.peekToken(
                            aInInvocation.currentState);
            if (Objects.equals(nextToken, lNextToken.getId()))
            {
                return equalsAction.execute(aInInvocation);
            }
            return notEqualsAction.execute(aInInvocation);
        }
    }

    public interface ParseResult
    {
        Object getValue();

        List<String> getErrors();
    }

    public class ParseInvocation implements ParseResult
    {
        private Stack<Symbol<?>> symbolStack = new Stack<>();

        private Stack<State> stateStack = new Stack<>();

        private State currentState;

        private Token<T> currentToken;
        
        private Tokenizer<T> tokenizer;
        
        private NonTerminal<NT> goTo;

        // TODO: Should this be a generic type?
        private Object value;

        private List<String> errors = new ArrayList<>();

        private ReduceListener<NT> listener;

        private ParseInvocation(
            State aInCurrentState,
            Tokenizer<T> aInTokenizer,
            ReduceListener<NT> aInListener)
        {
            currentState = aInCurrentState;
            tokenizer = aInTokenizer;
            listener = aInListener;
        }

        private Action nextAction()
        {
            // if there is a go to pending, we do that first otherwise we
            // process the next action
            return goTo != null
                ? currentState.goTos.get(goTo.getId())
                : currentState.actions.get(currentToken.getId());
        }

        public Object getValue()
        {
            return value;
        }

        public List<String> getErrors()
        {
            return errors;
        }
    }

    private interface Symbol<T>
    {
        T getId();

        Object getValue();
    }

    private static class Terminal<T> implements Symbol<T>
    {
        private Token<T> value;

        public Terminal(Token<T> aInValue)
        {
            value = aInValue;
        }

        public T getId()
        {
            return value.getId();
        }

        @Override
        public Token<T> getValue()
        {
            return value;
        }
    }

    private  static class NonTerminal<T> implements Symbol<T>
    {
        private T id;

        private Object value;

        public NonTerminal(T aInId, Object aInValue)
        {
            id = aInId;
            value = aInValue;
        }

        @Override
        public T getId()
        {
            return id;
        }

        @Override
        public Object getValue()
        {
            return value;
        }
    }

}
