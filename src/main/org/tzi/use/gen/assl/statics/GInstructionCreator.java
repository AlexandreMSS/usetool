/*
 * This is source code of the Snapshot Generator, an extension for USE
 * to generate (valid) system states of UML models.
 * Copyright (C) 2001 Joern Bohling, University of Bremen
 *
 * About USE:
 *   USE - UML based specification environment
 *   Copyright (C) 1999,2000,2001 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.tzi.use.gen.assl.statics;

import org.tzi.use.uml.mm.MModel;
import org.tzi.use.util.HashMultiMap;
import org.tzi.use.util.MultiMap;
import java.util.List;
import java.util.Iterator;

/**
 * @see org.tzi.use.gen.assl.statics
 * @author  Joern Bohling
 */
public class GInstructionCreator { 
    private MultiMap matchermap = null;   // (String -> IGInstructionMatcher)

    /**
     * A handle to the unique Singleton instance.
     */
    private static GInstructionCreator _instance = null;
  
    private GInstructionCreator() {
        IGInstructionMatcher matcherlist[] = {
            new GMatcherCreate_C(),
            new GMatcherAny_Seq(),
            new GMatcherSub_Seq(),
            new GMatcherTry_Seq(),
            new GMatcherCreateN_C_Integer(),
            new GMatcherDelete_Object(),    //disabled, because references
            //    to deleted objects would not be updated.
            new GMatcherInsert_Assoc_Linkends(),
            new GMatcherDelete_Assoc_Linkends(),
            new GMatcherTry_Assoc_LinkendSeqs(),
            new GMatcherSub_Seq_Integer()
        };
        matchermap = new HashMultiMap();
        for (int i = 0; i < matcherlist.length; i++)
            matchermap.put(matcherlist[i].name(), matcherlist[i]);
    }
    
    /**
     * @return The unique instance of this class.
     */
    public static GInstructionCreator instance() {
        if (null == _instance)
            _instance = new GInstructionCreator();
        return _instance;
    }

    public GInstruction create(String name,
                               List params,
                               MModel model) {
        // params is a list over Strings or GOCLExpressions.
        // A containing string is a classname or associationname

        // search by instruction symbol
        List instructions = matchermap.get(name);
        if (instructions.isEmpty() ) // unknown instruction symbol
            return null;
        // search overloaded instructions for a match
        Iterator instrIter = instructions.iterator();
        while (instrIter.hasNext() ) {
            IGInstructionMatcher i = (IGInstructionMatcher) instrIter.next();
            GInstruction result = i.createIfMatches(params, model);
            if (result != null )
                return result;
        }
        return null;
    }
}