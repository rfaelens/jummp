package net.biomodels.jummp.plugins.pharmml.maths

class OperatorSymbol extends MathsSymbol {
	public enum OperatorType {BINARY,UNARY};
	boolean omitBraces=false;
	OperatorType type;
	
	public OperatorSymbol(String text, String mapsTo, OperatorType t) {
		super(text,mapsTo);
		type=t;
	}
	
	public String getOpening() {
		if (omitBraces) {
			return "";
		}
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>(</mo>"
		}
		return null
		
	}
	
	public String getClosing() {
		if (omitBraces) {
			return "";
		}
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>)</mo>"
		}
		return null
		
	}

	public String getMapping() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>${mapsTo}</mo>"
		}
		return null
	}
}
