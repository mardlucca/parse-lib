/*
 * File: Production.java
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

import java.util.Arrays;
import java.util.stream.Collectors;

public class Production<NT>
{
    private NT leftHandSide;
    private Object[] rightHandSide;

    public Production(NT aInLeftHandSide, Object ... aInRightHandSide)
    {
        leftHandSide = aInLeftHandSide;
        rightHandSide = aInRightHandSide;
    }

    public NT getLeftHandSide()
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

        Production<?> lThat = (Production<?>) aInOther;

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
}
