/*
 * File: NumberLiteralRecognizer.java
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

public class NumberLiteralRecognizer<T> extends BaseTokenRecognizer<T>
{
    private State state = State.INITIAL;

    private int radix;

    private Class<?> type = int.class;

    public NumberLiteralRecognizer(T aInToken)
    {
        super(aInToken);
    }

    @Override
    public void reset()
    {
        super.reset();
        state = State.INITIAL;
        type = int.class;
        radix = 10;
    }

    @Override
    public Object getValue(String aInCharSequence)
    {
        if (type == int.class)
        {
            return Integer.parseInt(getCharSequenceForParsing(
                    aInCharSequence , radix), radix);
        }
        if (type == float.class)
        {
            return Float.parseFloat(aInCharSequence);
        }
        if (type == long.class)
        {
            return Long.parseLong(
                    aInCharSequence.substring(
                            radix == 16 ? 2 : radix == 8 ? 1 : 0,
                        aInCharSequence.length() - 1),
                    radix);
        }
        return Double.parseDouble(aInCharSequence);
    }

    @Override
    public MatchResult test(int aInChar, Object aInSyntacticContext)
    {
        switch (state)
        {
            case INITIAL:
                return handleInitialState(aInChar);
            case INTEGRAL_NUMBER_WITHOUT_EXPONENT:
                return handleIntegralNumberWithoutExponentState(aInChar);
            case FLOATING_POINT_NEEDS_DECIMAL_PART:
                return handleFloatingPointNeedsDecimalPart(aInChar);
            case FIRST_CHAR_WAS_A_ZERO:
                return handleFirstCharWasAZeroState(aInChar);
            case FLOATING_POINT_NUMBER_WITHOUT_EXPONENT:
                return handleFloatingPointNumberWithouthExponentState(aInChar);
            case NEED_EXPONENT_VALUE:
                return handleNeedExponentValueState(aInChar);
            case FIRST_EXPONENT_CHARACTER_WAS_A_NEGATIVE:
                return handleFirstExponentCharacterWasANegative(aInChar);
            case FLOATING_POINT_NUMBER_WITH_EXPONENT:
                return handleFloatingPointNumberWithExponent(aInChar);
            case OCTAL_NUMBER:
                return handleOctalNumberState(aInChar);
            case INVALID_OCTAL_MAY_STILL_BE_FLOATING_POINT:
                return handleInvalidOctalMayStillBeFloatingPointState(aInChar);
            case NEED_HEXADECIMAL_VALUE:
                return handleNeedHexadecimalValueAfterXState(aInChar);
            case HEXADECIMAL_NUMBER:
                return handleHexadecimalNumberState(aInChar);
            case LONG_SUFFIX:
                return handleLongSuffixState(aInChar);
            case FLOAT_SUFFIX:
                return handleFloatSuffixState(aInChar);
            case DOUBLE_SUFFIX:
                return handleDoubleSuffixState(aInChar);
         }

        return MatchResult.NOT_A_MATCH;
    }

    private MatchResult handleInitialState(int aInChar)
    {
        if (aInChar >= '1' && aInChar <= '9')
        {
            state = State.INTEGRAL_NUMBER_WITHOUT_EXPONENT;
            return MatchResult.MATCH;
        }
        if (aInChar == '0')
        {
            state = State.FIRST_CHAR_WAS_A_ZERO;
            return MatchResult.MATCH;
        }
        if (aInChar == '.')
        {
            type = double.class;
            state = State.FLOATING_POINT_NEEDS_DECIMAL_PART;
            return MatchResult.PARTIAL_MATCH;
        }
        return setNotAMatch(null);
    }

    private MatchResult handleIntegralNumberWithoutExponentState(
        int aInChar)
    {
        if (isDigit(aInChar))
        {
            return MatchResult.MATCH;
        }
        if (aInChar == 'd')
        {
            state = State.DOUBLE_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'f')
        {
            type = float.class;
            state = State.FLOAT_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'l')
        {
            type = long.class;
            state = State.LONG_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == '.')
        {
            type = double.class;
            state = State.FLOATING_POINT_NUMBER_WITHOUT_EXPONENT;
            return MatchResult.MATCH;
        }
        if (aInChar == 'e')
        {
            state = State.NEED_EXPONENT_VALUE;
            return MatchResult.PARTIAL_MATCH;
        }

        return setNotAMatch(null);
    }

    private MatchResult handleFloatingPointNeedsDecimalPart(
        int aInChar)
    {
        if (isDigit(aInChar))
        {
            state = State.FLOATING_POINT_NUMBER_WITHOUT_EXPONENT;
            return MatchResult.MATCH;
        }
        return setNotAMatch("Floating point number missing decimal value");
    }

    private MatchResult handleFloatingPointNumberWithouthExponentState(
        int aInChar)
    {
        if (isDigit(aInChar))
        {
            return MatchResult.MATCH;
        }
        if (aInChar == 'e')
        {
            state = State.NEED_EXPONENT_VALUE;
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar == 'f')
        {
            type = float.class;
            state = State.FLOAT_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'd')
        {
            state = State.DOUBLE_SUFFIX;
            return MatchResult.MATCH;
        }
        return setNotAMatch(null);
    }

    private MatchResult handleNeedExponentValueState(
        int aInChar)
    {
        if (isDigit(aInChar))
        {
            state = State.FLOATING_POINT_NUMBER_WITH_EXPONENT;
            return MatchResult.MATCH;
        }
        if (aInChar == '-')
        {
            state = State.FIRST_EXPONENT_CHARACTER_WAS_A_NEGATIVE;
            return MatchResult.PARTIAL_MATCH;
        }
        return setNotAMatch("Malformed floating point literal");
    }


    private MatchResult handleFirstExponentCharacterWasANegative(
        int aInChar)
    {
        if (isDigit(aInChar))
        {
            state = State.FLOATING_POINT_NUMBER_WITH_EXPONENT;
            return MatchResult.MATCH;
        }
        return setNotAMatch("Malformed floating point literal");
    }

    private MatchResult handleFloatingPointNumberWithExponent(
        int aInChar)
    {
        if (aInChar == 'f')
        {
            type = float.class;
            state = State.FLOAT_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'd')
        {
            state = State.DOUBLE_SUFFIX;
            return MatchResult.MATCH;
        }
        return setNotAMatch(null);
    }

    private MatchResult handleFirstCharWasAZeroState(
        int aInChar)
    {
        if (aInChar == '.')
        {
            type = double.class;
            state = State.FLOATING_POINT_NUMBER_WITHOUT_EXPONENT;
            return MatchResult.MATCH;
        }
        if (aInChar == 'x')
        {
            state = State.NEED_HEXADECIMAL_VALUE;
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar == 'e')
        {
            state = State.NEED_EXPONENT_VALUE;
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar >= '0' && aInChar <= '7')
        {
            state = State.OCTAL_NUMBER;
            radix = 8;
            return MatchResult.MATCH;
        }
        if (aInChar >= '8' && aInChar <= '9')
        {
            state = State.INVALID_OCTAL_MAY_STILL_BE_FLOATING_POINT;
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar == 'd')
        {
            state = State.DOUBLE_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'f')
        {
            type = float.class;
            state = State.FLOAT_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'l')
        {
            type = long.class;
            state = State.LONG_SUFFIX;
            return MatchResult.MATCH;
        }
        return setNotAMatch(null);
    }

    private MatchResult handleOctalNumberState(
        int aInChar)
    {
        if (aInChar == '.')
        {
            type = double.class;
            state = State.FLOATING_POINT_NUMBER_WITHOUT_EXPONENT;
            return MatchResult.MATCH;
        }
        if (aInChar >= '0' && aInChar <= '7')
        {
            return MatchResult.MATCH;
        }
        if (aInChar >= '8' && aInChar <= '9')
        {
            state = State.INVALID_OCTAL_MAY_STILL_BE_FLOATING_POINT;
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar == 'd')
        {
            state = State.DOUBLE_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'f')
        {
            type = float.class;
            state = State.FLOAT_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'l')
        {
            type = long.class;
            state = State.LONG_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'e')
        {
            state = State.NEED_EXPONENT_VALUE;
            return MatchResult.PARTIAL_MATCH;
        }
        return setNotAMatch(null);
    }

    private MatchResult handleInvalidOctalMayStillBeFloatingPointState(
        int aInChar)
    {
        if (isDigit(aInChar))
        {
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar == '.')
        {
            type = double.class;
            state = State.FLOATING_POINT_NUMBER_WITHOUT_EXPONENT;
            return MatchResult.MATCH;
        }
        if (aInChar == 'e')
        {
            state = State.NEED_EXPONENT_VALUE;
            return MatchResult.PARTIAL_MATCH;
        }
        if (aInChar == 'd')
        {
            state = State.DOUBLE_SUFFIX;
            return MatchResult.MATCH;
        }
        if (aInChar == 'f')
        {
            type = float.class;
            state = State.FLOAT_SUFFIX;
            return MatchResult.MATCH;
        }
        return setNotAMatch("Invalid octal number. Integer number too large.");
    }

    private MatchResult handleNeedHexadecimalValueAfterXState(
        int aInChar)
    {
        if (isHexDigit(aInChar))
        {
            state = State.HEXADECIMAL_NUMBER;
            radix = 16;
            return MatchResult.MATCH;
        }
        return setNotAMatch("Hexadecimal numbers must contain at least one " +
            "hexadecimal digit");
    }

    private MatchResult handleHexadecimalNumberState(
        int aInChar)
    {
        if (isHexDigit(aInChar))
        {
            return MatchResult.MATCH;
        }
        if (aInChar == 'l')
        {
            type = long.class;
            state = State.LONG_SUFFIX;
            return MatchResult.MATCH;
        }
        // note that e, f and d are valid hex digits and not special letters
        return setNotAMatch(null);
    }

    private boolean isDigit(int aInChar)
    {
        return aInChar >= '0' && aInChar <= '9';
    }

    private boolean isHexDigit(int aInChar)
    {
        return (aInChar >= '0' && aInChar <= '9')
            || (aInChar >= 'a' && aInChar <= 'f')
            || (aInChar >= 'A' && aInChar <= 'F');
    }

    private MatchResult handleLongSuffixState(int aInChar)
    {
        return setNotAMatch(null);
    }

    private MatchResult handleFloatSuffixState(int aInChar)
    {
        return setNotAMatch(null);
    }

    private MatchResult handleDoubleSuffixState(int aInChar)
    {
        return setNotAMatch(null);
    }

    private MatchResult setNotAMatch(String aInReason)
    {
        setFailureReason(aInReason);
        state = State.NOT_A_MATCH;
        return MatchResult.NOT_A_MATCH;
    }

    private String getCharSequenceForParsing(
            String aInCharSequence, int aInRadix)
    {
        switch (aInRadix)
        {
            case 8 : return aInCharSequence.substring(1);
            case 16 : return aInCharSequence.substring(2);
        }
        return aInCharSequence;
    }

    private enum State {
        INITIAL,
        INTEGRAL_NUMBER_WITHOUT_EXPONENT,
        FLOATING_POINT_NEEDS_DECIMAL_PART,
        FIRST_CHAR_WAS_A_ZERO,
        FLOATING_POINT_NUMBER_WITHOUT_EXPONENT,
        NEED_EXPONENT_VALUE,
        FIRST_EXPONENT_CHARACTER_WAS_A_NEGATIVE,
        FLOATING_POINT_NUMBER_WITH_EXPONENT,
        OCTAL_NUMBER,
        INVALID_OCTAL_MAY_STILL_BE_FLOATING_POINT,
        NEED_HEXADECIMAL_VALUE,
        HEXADECIMAL_NUMBER,
        LONG_SUFFIX,
        FLOAT_SUFFIX,
        DOUBLE_SUFFIX,
        NOT_A_MATCH
    }
}
