package net.biomodels.jummp.plugins.pharmml.maths

class OperatorSymbol extends MathsSymbol {
	public enum OperatorType {BINARY,UNARY};
	boolean omitBraces=false;
	boolean needsTermSeparation=false;
	String pre
	String post
	OperatorType type;
	
	public OperatorSymbol(String text, String mapsTo, OperatorType t) {
		super(text,mapsTo);
		type=t;
		if (format==MathsSymbol.OutputFormat.MATHML) {
			pre="<mo>(</mo>"
			post="<mo>)</mo>"
		}
	}
	
	public static OperatorSymbol DivideSymbol() {
		OperatorSymbol op=new OperatorSymbol("divide","",OperatorType.BINARY)
		op.pre="<mfrac>"
		op.post="</mfrac>"
		op.needsTermSeparation=true;
		return op
	}
	
	public String getTermStarter() {
		return "<mrow>"
	}
	
	public String getTermEnder() {
		return "</mrow>"
	}
	
	public String getOpening() {
		if (omitBraces) {
			return "";
		}
		return pre
	}
	
	public String getClosing() {
		if (omitBraces) {
			return "";
		}
		return post		
	}

	public String getMapping() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			if (mapsTo) {
				return "<mo>${mapsTo}</mo>"
			}
			return ""
		}
		return null
	}
}
