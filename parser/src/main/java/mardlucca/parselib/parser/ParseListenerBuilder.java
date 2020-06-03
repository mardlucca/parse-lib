/*
 * File: ParseListenerBuilder.java
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

import java.util.HashMap;
import java.util.Map;

public class ParseListenerBuilder<NT>
{
    private Map<Production<NT>, ReduceListener<NT>> listeners = new HashMap<>();

    private Grammar<NT> grammar;

    private ReduceListener<NT> defaultListener;

    public ParseListenerBuilder(
            Grammar<NT> aInGrammar)
    {
        grammar = aInGrammar;
    }

    public ParseListenerBuilder<NT> onReduce(int aInProduction,
            ReduceListener<NT> aInListener)
    {
        listeners.put(grammar.getProduction(aInProduction), aInListener);
        return this;
    }

    public ParseListenerBuilder<NT> onReduce(String aInProduction,
            ReduceListener<NT> aInListener)
    {
        listeners.put(grammar.getProduction(aInProduction), aInListener);
        return this;
    }

    public ParseListenerBuilder<NT> byDefault(ReduceListener<NT> aInListener)
    {
        defaultListener = aInListener;
        return this;
    }

    public ReduceListener<NT> build()
    {
        return (aInProduction, aInValues) -> {
            ReduceListener<NT> lListener = listeners.get(aInProduction);
            return lListener == null
                    ? defaultListener == null
                            ? null
                            : defaultListener.onReduce(aInProduction, aInValues)
                    : lListener.onReduce(aInProduction, aInValues);
        };
    }
}
