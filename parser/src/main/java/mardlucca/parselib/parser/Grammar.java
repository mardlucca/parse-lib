/*
 * File: Grammar.java
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

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Grammar
{
    private List<Production> productions = new ArrayList<>();
    private Map<String, Production> productionsByString = new HashMap<>();
    private ReduceListener defaultReduceListener;

    public Grammar()
    {
    }

    public Grammar addProduction(
            String aInLeftHandSide, Object ... aInRightHandSide)
    {
        Production lProduction =
                new Production(aInLeftHandSide, aInRightHandSide);
        productions.add(lProduction);
        productionsByString.put(lProduction.toString(), lProduction);
        return this;
    }

    public Production getProduction(int aInIndex)
    {
        return productions.get(aInIndex);
    }

    public Production getProduction(String aInProductionString)
    {
        return productionsByString.get(aInProductionString);
    }

    public Grammar onDefaultReduce(ReduceListener aInListener) {
        defaultReduceListener = aInListener;
        return this;
    }

    public Grammar onReduce(int aInProduction, ReduceListener aInReduceListener)
    {
        // let it throw NPE so that they get feedback that the production does
        // not exist.
        productions.get(aInProduction).reduceListener = aInReduceListener;
        return this;
    }

    public Grammar onReduce(
            String aInProductionString, ReduceListener aInReduceListener)
    {
        // let it throw NPE so that they get feedback that the production does
        // not exist.
        productionsByString.get(aInProductionString)
                .reduceListener = aInReduceListener;
        return this;
    }

    public class Production
    {
        private String leftHandSide;
        private Object[] rightHandSide;
        private ReduceListener reduceListener;

        private Production(String aInLeftHandSide, Object ... aInRightHandSide)
        {
            leftHandSide = aInLeftHandSide;
            rightHandSide = aInRightHandSide;
        }

        public String getLeftHandSide()
        {
            return leftHandSide;
        }

        public Object[] getRightHandSide()
        {
            return rightHandSide;
        }

        @Override
        public String toString()
        {
            return leftHandSide + " -> " + (ArrayUtils.isEmpty(rightHandSide)
                ? "''"
                : Arrays.stream(rightHandSide)
                    .map(Object::toString)
                    .collect(Collectors.joining(" ")));
        }

        @Override
        public boolean equals(Object aInOther)
        {
            if (this == aInOther)
            {
                return true;
            }
            if (aInOther == null || getClass() != aInOther.getClass())
            {
                return false;
            }

            Production lThat = (Production) aInOther;

            if (!leftHandSide.equals(lThat.leftHandSide))
            {
                return false;
            }

            return Arrays.equals(rightHandSide, lThat.rightHandSide);
        }

        @Override
        public int hashCode()
        {
            int lResult = leftHandSide.hashCode();
            lResult = 31 * lResult + Arrays.hashCode(rightHandSide);
            return lResult;
        }

        Object onReduce(Production aInProduction, Object[] aInValues)
                throws ParsingException {
            return reduceListener == null
                    ? defaultReduceListener == null
                            ? null
                            : defaultReduceListener.onReduce(
                                    aInProduction, aInValues)
                    : reduceListener.onReduce(aInProduction, aInValues);
        }
    }

    public interface ReduceListener
    {
        Object onReduce(Production aInProduction, Object[] aInValues)
                throws ParsingException;
    }
}
