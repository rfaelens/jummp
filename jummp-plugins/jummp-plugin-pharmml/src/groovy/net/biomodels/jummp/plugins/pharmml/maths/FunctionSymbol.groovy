package net.biomodels.jummp.plugins.pharmml.maths

class FunctionSymbol extends MathsSymbol {
	int args
	
	public FunctionSymbol(String text, String mapsTo, int a) {
		super(text,mapsTo);
		args=a;
	}
	
	public String getOpening() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>(</mo>"
		}
		return null
		
	}
	
	public String getClosing() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>)</mo>"
		}
		return null
	}
	
	public int getArgCount() {
		return args
	}

	public String getMapping() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>${mapsTo}</mo>"
		}
		return null
	}
	
	public String getArgSeparator() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>,</mo>"
		}
		return null
	}
}
