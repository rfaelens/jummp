package net.biomodels.jummp.plugins.pharmml.maths

import eu.ddmore.libpharmml.dom.maths.EquationType
import eu.ddmore.libpharmml.dom.maths.Equation
import javax.xml.bind.JAXBElement
import eu.ddmore.libpharmml.dom.maths.BinopType
import eu.ddmore.libpharmml.dom.maths.UniopType
import eu.ddmore.libpharmml.dom.commontypes.SymbolRefType


class MathsUtil {
	private static def pharmMap = [ "plus":"+",
					"minus":"-",
					"times":"x",
					"divide":"/",
					"power":"^",
					"logx":"logx",
					"root":"root",
					]
	
	public static List<MathsSymbol> convertToSymbols(String xml) {
		List<MathsSymbol> symbols = new LinkedList<MathsSymbol>();
		String[] split=xml.split("<");
		split.each {
    	    	    if (it.contains("/") && !it.contains("/>")) {
    	    	    }
    	    	    else if (it.contains("Equation")) {
    	    	    }
    	    	    else {
    	    	    	    if (it.contains("Uniop")) {
    	    	    	    	    symbols.add(new OperatorSymbol(it.split("op=")[1], it.split("op=")[1], OperatorSymbol.OperatorType.UNARY));
    	    	    	    }
    	    	    	    else if (it.contains("Binop")) {
    	    	    	    	 if (it.contains("plus")) {
    	    	    	    	 	 symbols.add(new OperatorSymbol("+", "+", OperatorSymbol.OperatorType.BINARY));
    	    	    	    	 }
    	    	    	    	 else if (it.contains("minus")) {
    	    	    	    	 	 symbols.add(new OperatorSymbol("-", "-", OperatorSymbol.OperatorType.BINARY));
    	    	    	    	 }
    	    	    	    	 else if (it.contains("divide")) {
    	    	    	    	 	 symbols.add(new OperatorSymbol("/", "/", OperatorSymbol.OperatorType.BINARY));
    	    	    	    	 }
    	    	    	    	 else if (it.contains("times")) {
    	    	    	    	 	 symbols.add(new OperatorSymbol("x", "x", OperatorSymbol.OperatorType.BINARY));
    	    	    	    	 }
    	    	    	    }
    	    	    	    else if (it.contains("ct")) {
    	    	    	    	    if (it.contains("SymbRef")) {
    	    	    	    	    	    symbols.add(new MathsSymbol(it.split("symbIdRef=")[1], it.split("symbIdRef=")[1]));
    	    	    	    	    }
    	    	    	    	    else {
    	    	    	    	    	    symbols.add(new MathsSymbol(it.split(">")[1], it.split(">")[1]));
    	    	    	    	    }
    	    	    	    }
   	    	    }
    	    	}
    	    	return symbols;
	}
	
	public static List<MathsSymbol> convertToSymbols(EquationType equation) {
		List<MathsSymbol> symbols = new LinkedList<MathsSymbol>();
		convertJAX(symbols, equation)
		System.out.println("CONVERTED TO ${symbols}")
    	    	return symbols;
	}

	private static void convertJAX(List<MathsSymbol> symbols, def jaxObject) {
		MathsSymbol symbol=getSymbol(jaxObject)
		if (symbol) {
			symbols.add(symbol)
		}
		List<JAXBElement> subtree=getSubTree(jaxObject)
		if (!subtree.isEmpty()) {
			subtree.each {
				convertJAX(symbols, it);
			}
		}
	}
	
	private static List getSubTree(def jaxObject) {
		List subTree=new LinkedList()
		if (jaxObject instanceof EquationType || jaxObject instanceof Equation) {
			jaxObject.getScalarOrSymbRefOrBinop().each {
				subTree.add(it.getValue())
			}
		}
		else if (jaxObject instanceof BinopType) {
			jaxObject.getContent().each {
				subTree.add(it.getValue())
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
		return subTree
	}
	
	private static void addIfExists(Object object, List list) {
		if (object) {
			list.add(object)
		}
	}
	
	private static MathsSymbol getSymbol(def jaxObject) {
		if (jaxObject instanceof BinopType) {
			return new OperatorSymbol(jaxObject.getOp(), 
						  convertTextToSymbol(jaxObject.getOp()), 
						  OperatorSymbol.OperatorType.BINARY)
		}
		if (jaxObject instanceof UniopType) {
			return new OperatorSymbol(jaxObject.getOp(), 
						  convertTextToSymbol(jaxObject.getOp()), 
						  OperatorSymbol.OperatorType.UNARY)
		}
		if (jaxObject instanceof eu.ddmore.libpharmml.dom.commontypes.Boolean) { //NEEDS TO BE LOOKED AT!
			return new MathsSymbol(""+jaxObject.getId(), ""+jaxObject.getId())
		}
		if (jaxObject instanceof SymbolRefType) {
			String varName="";
			if (jaxObject.getBlkIdRef()) {
				varName=jaxObject.getBlkIdRef()+":";
			}
			varName+=jaxObject.getSymbIdRef();
			return new MathsSymbol(varName, varName)
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
