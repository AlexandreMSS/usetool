/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2010 Mark Richters, University of Bremen
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

// $Id$

package org.tzi.use.uml.sys.soil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tzi.use.uml.mm.MAssociationClass;
import org.tzi.use.uml.ocl.expr.ExpConstString;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MLinkObject;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.util.StringUtil;
import org.tzi.use.util.soil.exceptions.EvaluationFailedException;


/**
 * This statement creates a new link object of an association class.
 * @author Daniel Gent
 *
 */
public class MNewLinkObjectStatement extends MStatement {
	/**
	 * The association class a link object has to be created for.  
	 */
	private MAssociationClass fAssociationClass;
	/**
	 * List of the objects that participate in the link in the same order as association ends. 
	 */
	private List<MRValue> fParticipants;
	/**
	 * List of the qualifier values for the association ends. 
	 */
	private List<List<MRValue>> qualifier;
	/**
	 * An optional expression which is used to construct the object name / identifier.
	 */
	private Expression fObjectName;
	
	/**
	 * Saves the created MLinkObject
	 */
	private MLinkObject fCreatedLinkObject;
		
	/**
	 * Constructs a new MNewLinkCreateStatement.
	 * @param associationClass The <code>MAssociationClass</code> to create a new link object for.
	 * @param participants The objects as <code>MRValue</code> which are linked by the link object
	 * @param qualifiers The qualifier values for the different association ends 
	 * @param objectName An optional <code>Expression</code> for the object name. If not supplied a name is generated by USE.
	 */
	public MNewLinkObjectStatement(
			MAssociationClass associationClass, 
			List<MRValue> participants,
			List<List<MRValue>> qualifiers,
			Expression objectName) {
		
		fAssociationClass = associationClass;
		fParticipants = participants;
		fObjectName = objectName;
		this.qualifier = qualifiers;
	}
	
	
	/**
	 * Constructs a new MNewLinkCreateStatement.
	 * @param associationClass The <code>MAssociationClass</code> to create a new link object for.
	 * @param participants The objects as <code>MRValue</code> which are linked by the link object.
	 * @param qualifiers The qualifier values for the different association ends
	 * @param objectName An optional <code>String</code> for the object name. If not supplied a name is generated by USE.
	 */
	public MNewLinkObjectStatement(
			MAssociationClass associationClass, 
			List<MRValue> participants,
			List<List<MRValue>> qualifiers,
			String objectName) {
		
		fAssociationClass = associationClass;
		fParticipants = participants;
		if (objectName != null) {
			fObjectName = new ExpConstString(objectName);
		}
		this.qualifier = qualifiers;
	}
	
	
	/**
	 * The association class a link object has to be created for.
	 * @return The association class a link object has to be created for.
	 */
	public MAssociationClass getAssociationClass() {
		return fAssociationClass;
	}
	
	
	/**
	 * The objects as <code>MRValue</code> which are linked by the link object 
	 * @return The objects as <code>MRValue</code> which are linked by the link object 
	 */
	public List<MRValue> getParticipants() {
		return fParticipants;
	}
	
	
	/**
	 * Returns the {@link Expression} to generate the object name from, if given.
	 * Otherwise <code>null</code> is returned. 
	 * @return Returns the {@link Expression} to generate the object name from or <code>null</code> if no name was given.
	 */
	public Expression getObjectName() {
		return fObjectName;
	}
	
	
	/**
	 * Returns the created link object when called after the statement has been executed.
	 * @return The created <code>MLinkObject</code>.
	 */
	public MLinkObject getCreatedLinkObject() {
		return fCreatedLinkObject;
	}
	
	
	@Override
	protected void evaluate() throws EvaluationFailedException {
		List<List<Value>> qualifierValues = new ArrayList<List<Value>>();
		List<Value> empty = Collections.emptyList();
		
		if (qualifier != null) {
			for (List<MRValue> values : qualifier) {
				if (values == null) {
					qualifierValues.add(empty);
				} else {
					List<Value> thisQualifierValues = new ArrayList<Value>();
					for (MRValue v : values) {
						thisQualifierValues.add(evaluateRValue(v));
					}
					qualifierValues.add(thisQualifierValues);
				}
			}
		}
		
		// evaluate participants
		List<MObject> participants = 
			evaluateObjectRValues(fParticipants);
		
		String objectName;
		if (fObjectName == null) {
			objectName = fContext.getState().uniqueObjectNameForClass(fAssociationClass);
		} else {
			objectName = evaluateString(fObjectName);
		}
				
		// create link object
		fCreatedLinkObject = 
			createLinkObject(
					fAssociationClass, 
					objectName, 
					participants,
					qualifierValues);
	}


	@Override
	protected String shellCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append("new ");
		sb.append(fAssociationClass.name());
		if (fObjectName != null) {
			sb.append("(");
			sb.append(fObjectName);
			sb.append(")");
		}
		sb.append(" between (");
		
		StringUtil.fmtSeqWithSubSeq(sb, fParticipants, ",", qualifier, ",", "{", "}");
		
		sb.append(")");
		
		return sb.toString();
	}


	@Override
	public boolean hasSideEffects() {
		return true;
	}

	
	@Override
	public String toString() {
		return shellCommand();
	}
}
