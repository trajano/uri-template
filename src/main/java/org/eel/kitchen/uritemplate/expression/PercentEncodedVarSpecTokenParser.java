/*
 * Copyright (c) 2012, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eel.kitchen.uritemplate.expression;

import com.google.common.base.CharMatcher;
import org.eel.kitchen.uritemplate.InvalidTemplateException;

import java.nio.CharBuffer;

public final class PercentEncodedVarSpecTokenParser
    implements TokenParser
{
    // When we arrive here, the '%' has already been swallowed
    private static final CharMatcher MATCHER;

    static {
        CharMatcher matcher;

        // ALPHA
        matcher = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('A', 'Z'));

        // DIGIT
        matcher = matcher.or(CharMatcher.inRange('0', '9'));

        // build
        MATCHER = matcher.precomputed();
    }

    private final CharBuffer buf;
    private final int index;
    private final ExpressionBuilder builder;
    private final StringBuilder sb;

    public PercentEncodedVarSpecTokenParser(final CharBuffer buf,
        final int index, final ExpressionBuilder builder,
        final StringBuilder sb)
    {
        this.buf = buf;
        this.index = index;
        this.builder = builder;
        this.sb = sb;
    }

    @Override
    public boolean parse()
        throws InvalidTemplateException
    {
        final int total = buf.length();
        final int remaining = total - index;

        if (remaining < 2)
            throw new InvalidTemplateException("illegal percent-escaped " +
                "sequence: not enough characters remaining");

        /*
         * CharBuffer does not have any absolute get() method into a char array.
         * We therefore read one char at a time and check whether it matches
         * what we want.
         */

        char c;

        c = buf.get(index);
        if (!MATCHER.matches(c))
            throw new IllegalArgumentException("illegal character in " +
                "percent-encoded sequence");
        sb.append(c);

        c = buf.get(index + 1);
        if (!MATCHER.matches(c))
            throw new IllegalArgumentException("illegal character in " +
                "percent-encoded sequence");
        sb.append(c);

        return total >= index + 2;
    }

    @Override
    public TokenParser next()
    {
        return new VarSpecTokenParser(buf, index + 2, builder, sb);
    }
}
