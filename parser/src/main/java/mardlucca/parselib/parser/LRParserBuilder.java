/*
 * File: LRParserBuilder.java
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

import mardlucca.parselib.tokenizer.TokenizerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class LRParserBuilder<T, NT>
{
    private static final Pattern SYMBOL_REGEX =
        Pattern.compile("[ \\t\\n\\r]+");

    private static final String ERROR_SUFFIX = "error";
    private static final String GRAMMAR_SUFFIX = "grammar";
    private static final String TABLE_SUFFIX = "table";
    private static final String EPSILON_SYMBOL = "''";

    private String languageName;
    private Function<String, T> terminalParser;
    private Function<String, NT> nonTerminalParser;
    private Function<String, Boolean> epsilonParser;
    protected Grammar<NT> grammar;

    public LRParserBuilder(String aInLanguageName,
            Function<String, T> aInTerminalParser,
            Function<String, NT> aInNonTerminalParser)
    {
        this(aInLanguageName, aInTerminalParser, aInNonTerminalParser, null);
    }


    public LRParserBuilder(String aInLanguageName,
            Function<String, T> aInTerminalParser,
            Function<String, NT> aInNonTerminalParser,
            Grammar<NT> aInGrammar)
    {
        languageName = aInLanguageName;
        terminalParser = aInTerminalParser;
        nonTerminalParser = aInNonTerminalParser;
        epsilonParser = aInString -> Objects.equals(aInString, EPSILON_SYMBOL);
        grammar = aInGrammar;
    }

    public LRParser<T, NT> build(TokenizerFactory<T> aInTokenizerFactory)
    {
        Grammar<NT> lGrammar = loadGrammar();

        Properties lErrors = loadErrorFile();
        LRParser<T, NT> lParser = new LRParser<>(aInTokenizerFactory, lGrammar);
        try (BufferedReader lReader = new BufferedReader(
            new InputStreamReader(getClass().getResourceAsStream(
                getFile(TABLE_SUFFIX)))))
        {
            Object[] lSymbols = null;
            boolean[] lIsTerminalFlags = null;
            int lExpectedState = 0;

            for (String lLine = lReader.readLine();
                lLine != null;
                lLine = lReader.readLine())
            {
                if (isBlank(lLine) || lLine.startsWith("#"))
                {
                    continue;
                }

                String[] lParts = lLine.split("\t");
                if (lSymbols == null)
                {
                    // the first thing we need to read is the header row
                    if (isNotBlank(lParts[0])) {
                        throw new RuntimeException(
                            "Could not find header row for table file \"" +
                                getFile(TABLE_SUFFIX) + "\"");
                    }

                    // this is the first line of the file, the one defining
                    // the symbols contained in each column of the table
                    lSymbols = new Object[lParts.length - 1];
                    lIsTerminalFlags = new boolean[lParts.length - 1];
                    for (int i = 1; i < lParts.length; i++)
                    {
                        lSymbols[i - 1] = terminalParser.apply(lParts[i]);
                        if (lSymbols[i - 1] != null)
                        {
                            lIsTerminalFlags[i - 1] = true;
                        }
                        else
                        {
                            lSymbols[i - 1] =
                                nonTerminalParser.apply(lParts[i]);
                        }
                        if (lSymbols[i - 1] == null)
                        {
                            throw new RuntimeException("\"" +
                                lSymbols[i - 1] + "\" in parse table " +
                                "file \"" + languageName +
                                ".table\" is not a valid symbol");
                        }
                    }
                }
                else
                {
                    if (Integer.parseInt(lParts[0]) != lExpectedState)
                    {
                        throw new RuntimeException("Expected state \"" +
                            lExpectedState + "\" but found \"" + lParts[0] +
                            '"');
                    }

                    LRParser<T, NT>.State lState = lParser.newState();
                    lExpectedState++;
                    for (int i = 1; i < lParts.length; i++)
                    {
                        if (isBlank(lParts[i]))
                        {
                            continue;
                        }

                        if (lIsTerminalFlags[i - 1])
                        {
                            T lTerminal = (T) lSymbols[i - 1];
                            if (lParts[i].charAt(0) == 's')
                            {
                                lState = lState.shift(lTerminal,
                                    Integer.parseInt(
                                        lParts[i].substring(1)));
                            }
                            else if  (lParts[i].charAt(0) == 'r')
                            {
                                lState = lState.reduce(
                                        lTerminal,
                                        Integer.parseInt(
                                                lParts[i].substring(1)));
                            }
                            else if  (lParts[i].charAt(0) == 'e')
                            {
                                lState = lState.error(lTerminal,
                                    getError(lErrors, Integer.parseInt(
                                        lParts[i].substring(1))));
                            }
                            else if  (lParts[i].equals("acc"))
                            {
                                lState = lState.accept(lTerminal);
                            }
                            else
                            {
                                throw new RuntimeException(
                                    "Invalid action \"" + lParts[i] +
                                        "\" in row \"" + lParts[0] +
                                        "\" and column \"" +
                                        lSymbols[i - 1] + '"');
                            }
                        }
                        else
                        {
                            // go to expected for non-terminal symbol
                            if (isNumeric(lParts[i]))
                            {
                                lState.goTo((NT) lSymbols[i - 1],
                                    Integer.parseInt(lParts[i]));
                            }
                            else
                            {
                                throw new RuntimeException(
                                    "Invalid action \"" + lParts[i] +
                                        "\" in row \"" + lParts[0] +
                                        "\" and column \"" +
                                        lSymbols[i - 1] + '"');
                            }
                        }
                    }
                    onNewState(lState);
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return lParser;
    }

    protected void onNewState(LRParser<T, NT>.State aInState)
    {
        // do nothing by default
    }

    private static String getError(Properties aInErrors, int aInErrorNumber)
    {
        String lErrorMessage = aInErrors.getProperty(
            String.valueOf(aInErrorNumber));
        if (lErrorMessage == null)
        {
            throw new RuntimeException("Could not find error message for " +
                "error code \"" + aInErrorNumber + '"');
        }
        return lErrorMessage;
    }

    private Grammar<NT> loadGrammar()
    {
        if (grammar != null)
        {
            return grammar;
        }

        try
        {
            Grammar<NT> lGrammar = new Grammar();
            Files.lines(Paths.get(this.getClass().getResource(
                getFile(GRAMMAR_SUFFIX)).toURI())).forEach(aInLine ->
                {
                    if (isBlank(aInLine) || aInLine.startsWith("#"))
                    {
                        return;
                    }

                    String[] lParts = getSymbols(aInLine);
                    if (lParts.length < 3)
                    {
                        throw new RuntimeException("Production with less " +
                            "than 3 symbols: " + aInLine);
                    }

                    NT lLeftHandSide = nonTerminalParser.apply(lParts[0]);
                    if (lLeftHandSide == null)
                    {
                        throw new RuntimeException("Left hand symbol \"" +
                            lParts[0] + "\" in production \"" + aInLine +
                            "\" was not recognized as a non-terminal");
                    }

                    List<Object> lRightHandSide = new ArrayList<>();
                    for (int i = 2; i < lParts.length; i++)
                    {
                        Object lSymbol = nonTerminalParser.apply(lParts[i]);
                        if (lSymbol == null)
                        {
                            lSymbol = terminalParser.apply(lParts[i]);
                        }
                        if (lSymbol == null)
                        {
                            if (epsilonParser.apply(lParts[i]))
                            {
                                if (i != 2 || lParts.length != 3)
                                {
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

                    lGrammar.addProduction(new Production<>(
                            lLeftHandSide,
                            lRightHandSide.toArray(
                                    new Object[lRightHandSide.size()])));

                });
            return lGrammar;
        }
        catch (IOException | URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Properties loadErrorFile()
    {
        Properties lProperties = new Properties();
        try
        {
            InputStream lInputStream = getClass().getResourceAsStream(
                getFile(ERROR_SUFFIX));
            if (lInputStream != null)
            {
                lProperties.load(lInputStream);
            }
        }
        catch (IOException ignore)
        {
        }
        return lProperties;
    }

    private String getFile(String aInExtension)
    {
        return "/META-INF/" + languageName + '.' + aInExtension;
    }

    private String[] getSymbols(String aInLine)
    {
        return SYMBOL_REGEX.split(aInLine.trim());
    }
}
