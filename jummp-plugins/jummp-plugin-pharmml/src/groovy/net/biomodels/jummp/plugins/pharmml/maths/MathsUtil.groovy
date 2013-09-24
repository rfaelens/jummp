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

class MathsUtil {
	private static def pharmMap = [ "plus":"+",
					"minus":"-",
					"times":" &InvisibleTimes;",
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
	
	public static List<MathsSymbol> convertToSymbols(EquationType equation) {
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
			addIfExists(jaxObject.binop, subTree)
			addIfExists(jaxObject.uniop, subTree)
			addIfExists(jaxObject.symbRef, subTree)
			addIfExists(jaxObject.functionCall, subTree)
			addIfExists(jaxObject.scalar, subTree)
			addIfExists(jaxObject.constant, subTree)
			Condition condition=jaxObject.getCondition()
			if (!condition.getOtherwise()) {
				addIfExists(condition.getLogicBinop(),subTree)
				addIfExists(condition.getLogicUniop(),subTree)
				addIfExists(condition.getBoolean(),subTree)
			}
			
		}
		else if (jaxObject instanceof UniopType) {
			addIfExists(jaxObject.binop, subTree)
			addIfExists(jaxObject.uniop, subTree)
			addIfExists(jaxObject.symbRef, subTree)
			addIfExists(jaxObject.functionCall, subTree)
			addIfExists(jaxObject.scalar, subTree)
			addIfExists(jaxObject.constant, subTree)
		}
		else if (jaxObject instanceof LogicUniOpType) {
			addIfExists(jaxObject.binop, subTree)
			addIfExists(jaxObject.uniop, subTree)
			addIfExists(jaxObject.symbRef, subTree)
			addIfExists(jaxObject.functionCall, subTree)
			addIfExists(jaxObject.scalar, subTree)
			addIfExists(jaxObject.constant, subTree)
			addIfExists(jaxObject.logicBinop, subTree)
			addIfExists(jaxObject.logicUniop, subTree)
		}
		return subTree
	}
	
	private static void addIfExists(Object object, List list) {
		if (object) {
			list.add(object)
		}
	}
	
	
	private static MathsSymbol getSymbol(def jaxObject) {
		if (jaxObject instanceof BinopType || jaxObject instanceof LogicBinOpType) {
			if (jaxObject.getOp() == "divide") {
				return OperatorSymbol.DivideSymbol()
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
		if (jaxObject instanceof eu.ddmore.libpharmml.dom.commontypes.Boolean) { //NEEDS TO BE LOOKED AT!
			return new MathsSymbol(""+jaxObject.getId(), ""+jaxObject.getId())
		}
		if (jaxObject instanceof SymbolRefType) {
			String varName="";
			/*if (jaxObject.getBlkIdRef()) {
				varName=jaxObject.getBlkIdRef()+":";
			}*/
			varName+=jaxObject.getSymbIdRef();
			return new MathsSymbol(varName, varName)
		}
		if (jaxObject instanceof FunctionCallType) {
			MathsSymbol tmp=getSymbol(jaxObject.symbRef)
			return new FunctionSymbol(tmp.mapsTo, tmp.mapsTo, jaxObject.functionArgument.size())
		}
		if (jaxObject instanceof PiecewiseType) {
			return new PiecewiseSymbol(jaxObject.getPiece().size())
		}
		if (jaxObject instanceof PieceType) {
			Condition condition=jaxObject.getCondition()
			if (condition.getOtherwise()) {
				return new PieceSymbol(PieceSymbol.ConditionType.OTHERWISE)
			}
			return new PieceSymbol(PieceSymbol.ConditionType.EXTERNAL)
			
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
