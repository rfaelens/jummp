package net.biomodels.jummp.maths

class MathsUtil {
	
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
}
