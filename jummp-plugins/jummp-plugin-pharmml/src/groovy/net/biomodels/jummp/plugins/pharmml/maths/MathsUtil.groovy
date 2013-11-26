/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* LibPharmml (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of LibPharmml used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.pharmml.maths

import eu.ddmore.libpharmml.dom.maths.EquationType
import eu.ddmore.libpharmml.dom.maths.Equation
import javax.xml.bind.JAXBElement
import eu.ddmore.libpharmml.dom.maths.BinopType
import eu.ddmore.libpharmml.dom.maths.UniopType
import eu.ddmore.libpharmml.dom.maths.LogicUniOpType
import eu.ddmore.libpharmml.dom.maths.LogicBinOpType
import eu.ddmore.libpharmml.dom.commontypes.SymbolRefType
import eu.ddmore.libpharmml.dom.maths.FunctionCallType
import eu.ddmore.libpharmml.dom.commontypes.FunctionDefinitionType
import eu.ddmore.libpharmml.dom.maths.PiecewiseType
import eu.ddmore.libpharmml.dom.maths.PieceType
import eu.ddmore.libpharmml.dom.maths.Condition
import eu.ddmore.libpharmml.dom.commontypes.TrueBooleanType
import eu.ddmore.libpharmml.dom.commontypes.FalseBooleanType

class MathsUtil {
	private static def pharmMap = [ "plus":"+",
					"minus":"-",
					"times":" &times;",
					"power":"^",
					"lt": "&lt;",
					"leq": "&le;",
					"gt":"&gt;",
					"geq":"&ge;",
					"eq":"=",
					"neq":"&ne;",
					"and":"&and;",
					"or":"&or;",
					]
	
	public static List<MathsSymbol> convertToSymbols(def equation) {
		List<MathsSymbol> symbols = new LinkedList<MathsSymbol>();
		convertJAX(symbols, equation)
		System.out.println("CONVERTED TO ${symbols}")
    	    	return symbols;
	}

	private static void convertJAX(List<MathsSymbol> symbols, def jaxObject) {
		if (jaxObject instanceof JAXBElement) { //shouldnt really happen, but sanity check.
			convertJAX(symbols, jaxObject.getValue())
			return;
		}
		MathsSymbol symbol=getSymbol(jaxObject)
		if (symbol) {
			symbols.add(symbol)
		}
		List<JAXBElement> subtree=getSubTree(jaxObject, symbols)
		if (!subtree.isEmpty()) {
			subtree.each {
				convertJAX(symbols, it);
			}
		}
	}
	
	private static List getSubTree(def jaxObject, List<MathsSymbol> symbols) {
		List subTree=new LinkedList()
		if (jaxObject instanceof EquationType || jaxObject instanceof Equation) {
			jaxObject.getScalarOrSymbRefOrBinop().each {
				subTree.add(it.getValue())
			}
		}
		else if (jaxObject instanceof BinopType || jaxObject instanceof LogicBinOpType) {
			jaxObject.getContent().each {
				subTree.add(it.getValue())
			}
		}
		else if (jaxObject instanceof FunctionCallType) {
			List args=jaxObject.functionArgument
			args.each {
				if (it.equation) {
					convertJAX(symbols, it.equation)
				}
				addIfExists(it.symbRef, subTree)
				addIfExists(it.scalar, subTree)
				addIfExists(it.constant, subTree)
			}
		}
		else if (jaxObject instanceof PiecewiseType) {
			List pieces=jaxObject.getPiece()
			subTree.addAll(pieces)
		}
		else if (jaxObject instanceof PieceType) {
			addCommonSubElements(jaxObject, subTree)
			Condition condition=jaxObject.getCondition()
			if (!condition.getOtherwise()) {
				addIfExists(condition.getLogicBinop(),subTree)
				addIfExists(condition.getLogicUniop(),subTree)
				addIfExists(condition.getBoolean(),subTree)
			}
			
		}
		else if (jaxObject instanceof UniopType) {
			addCommonSubElements(jaxObject, subTree)
		}
		else if (jaxObject instanceof LogicUniOpType) {
			addCommonSubElements(jaxObject, subTree)
			addIfExists(jaxObject.logicBinop, subTree)
			addIfExists(jaxObject.logicUniop, subTree)
		}
		return subTree
	}

	private static void addCommonSubElements(def jaxObject, List subTree) {
		addIfExists(jaxObject.binop, subTree)
		addIfExists(jaxObject.uniop, subTree)
		addIfExists(jaxObject.symbRef, subTree)
		addIfExists(jaxObject.functionCall, subTree)
		addIfExists(jaxObject.scalar, subTree)
		addIfExists(jaxObject.constant, subTree)
	}
	
	private static void addIfExists(Object object, List list) {
		if (object) {
			list.add(object)
		}
	}
	
	private static MathsSymbol getSymbol(SymbolRefType jaxObject) {
		String varName="";
		/*if (jaxObject.getBlkIdRef()) {
			varName=jaxObject.getBlkIdRef()+":";
		}*/
		varName+=jaxObject.getSymbIdRef();
		return new MathsSymbol(varName, varName)
	}
	
	private static MathsSymbol getSymbol(FunctionCallType jaxObject) {
		MathsSymbol tmp=getSymbol(jaxObject.symbRef)
		return new FunctionSymbol(tmp.mapsTo, tmp.mapsTo, jaxObject.functionArgument.size())
	}
	
	private static MathsSymbol getSymbol(PiecewiseType jaxObject) {
		return new PiecewiseSymbol(jaxObject.getPiece().size())
	}

	private static MathsSymbol getSymbol(PieceType jaxObject) {
		Condition condition=jaxObject.getCondition()
		if (condition.getOtherwise()) {
			return new PieceSymbol(PieceSymbol.ConditionType.OTHERWISE)
		}
		return new PieceSymbol(PieceSymbol.ConditionType.EXTERNAL)
	}
	
	
	private static MathsSymbol getSymbol(TrueBooleanType jaxObject) {
		return new MathsSymbol("true", "true");
	}
	
	private static MathsSymbol getSymbol(FalseBooleanType jaxObject) {
		return new MathsSymbol("false", "false");
	}
	
	private static MathsSymbol getSymbol(def jaxObject) {
		if (jaxObject instanceof BinopType || jaxObject instanceof LogicBinOpType) {
			if (jaxObject.getOp() == "divide") {
				return OperatorSymbol.DivideSymbol()
			}
			if (jaxObject.getOp() == "root") {
				return new OperatorSymbol(jaxObject.getOp(), 
					  convertTextToSymbol(jaxObject.getOp()), 
					  OperatorSymbol.OperatorType.ROOT)
			}
			if (jaxObject.getOp() == "power") {
				return new OperatorSymbol(jaxObject.getOp(), 
					  convertTextToSymbol(jaxObject.getOp()), 
					  OperatorSymbol.OperatorType.POWER)
			}
			return new OperatorSymbol(jaxObject.getOp(), 
						  convertTextToSymbol(jaxObject.getOp()), 
						  OperatorSymbol.OperatorType.BINARY)
		}
		if (jaxObject instanceof UniopType || jaxObject instanceof LogicUniOpType) {
			OperatorSymbol op= new OperatorSymbol(jaxObject.getOp(), 
						  convertTextToSymbol(jaxObject.getOp()), 
						  OperatorSymbol.OperatorType.UNARY)
			if (jaxObject.getOp()=="minus") {
				op.omitBraces=true
			}
			return op
		}
		if (jaxObject.getClass().getCanonicalName().contains("eu.ddmore.libpharmml.dom.commontypes")) {
			return new MathsSymbol(""+jaxObject.getValue(), ""+jaxObject.getValue())
		}
		return null
	}
	
	private static String convertTextToSymbol(String pharmText) {
		if (pharmMap.containsKey(pharmText)) {
			return pharmMap.get(pharmText)
		}
		return pharmText
	}
}
