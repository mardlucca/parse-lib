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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grammar<NT>
{
    private List<Production<NT>> productions = new ArrayList<>();
    private Map<String, Production<NT>> productionsByString = new HashMap<>();

    Grammar()
    {
    }

    public Grammar(List<Production<NT>> aInProductions)
    {
        for (Production<NT> lProduction : aInProductions)
        {
            addProduction(lProduction);
        }
    }

    void addProduction(Production<NT> aInProduction)
    {
        productions.add(aInProduction);
        productionsByString.put(aInProduction.toString(), aInProduction);
    }

    public Production<NT> getProduction(int aInIndex)
    {
        return productions.get(aInIndex);
    }

    public Production<NT> getProduction(String aInProductionString)
    {
        return productionsByString.get(aInProductionString);
    }
}
