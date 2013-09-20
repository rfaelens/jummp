package net.biomodels.jummp.maths

class OperatorSymbol extends MathsSymbol {
	public enum OperatorType {BINARY,UNARY};
	OperatorType type;
	
	public OperatorSymbol(String text, String mapsTo, OperatorType t) {
		super(text,mapsTo);
		type=t;
	}
}
