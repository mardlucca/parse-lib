/*
 * File: GrammarLoader.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.lang.Character.isUpperCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class GrammarLoader {
    private static final Pattern SYMBOL_REGEX =
            Pattern.compile("[ \\t\\n\\r]+");
    private static final String EPSILON_SYMBOL = "''";

    private static Function<String, String> nonTerminalParser =
            aInString -> isUpperCase(aInString.charAt(0))
                    ? aInString : null;
    private static Function<String, Boolean> epsilonParser =
            aInString -> Objects.equals(aInString, EPSILON_SYMBOL);

    public static Grammar load(
            Reader aInReader,
            Function<String, ?> aInTerminalParser) {

        Grammar lGrammar = new Grammar();
        try (BufferedReader lReader = new BufferedReader(aInReader)) {
            for (String aInLine = lReader.readLine();
                    aInLine != null;
                    aInLine = lReader.readLine()) {

                if (isBlank(aInLine) || aInLine.startsWith("#")) {
                    continue;
                }

                String[] lParts = getSymbols(aInLine);
                if (lParts.length < 3) {
                    throw new RuntimeException("Production with less " +
                            "than 3 symbols: " + aInLine);
                }

                String lLeftHandSide = nonTerminalParser.apply(lParts[0]);
                if (lLeftHandSide == null) {
                    throw new RuntimeException("Left hand symbol \"" +
                            lParts[0] + "\" in production \"" + aInLine +
                            "\" was not recognized as a non-terminal");
                }

                List<Object> lRightHandSide = new ArrayList<>();
                for (int i = 2; i < lParts.length; i++) {
                    Object lSymbol = nonTerminalParser.apply(lParts[i]);
                    if (lSymbol == null) {
                        lSymbol = aInTerminalParser.apply(lParts[i]);
                    }
                    if (lSymbol == null) {
                        if (epsilonParser.apply(lParts[i])) {
                            if (lParts.length != 3) {
                                throw new RuntimeException("Epsilon " +
                                        "symbol must be the only one in " +
                                        "the right hand side of a " +
                                        "production: " + aInLine);
                            }

                            // found epsilon, ignore it
                            break;
                        }
                        throw new RuntimeException("Symbol \"" +
                                lParts[i] + "\" in production \"" +
                                aInLine + "\" is not valid");
                    }
                    lRightHandSide.add(lSymbol);
                }

                lGrammar.addProduction(
                        lLeftHandSide,
                        lRightHandSide.toArray(new Object[0]));

            }
            return lGrammar;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getSymbols(String aInLine) {
        return SYMBOL_REGEX.split(aInLine.trim());
    }
}
