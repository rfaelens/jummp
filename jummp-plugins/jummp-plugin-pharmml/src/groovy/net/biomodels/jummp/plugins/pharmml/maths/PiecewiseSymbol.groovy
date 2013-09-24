package net.biomodels.jummp.plugins.pharmml.maths

class PiecewiseSymbol extends MathsSymbol {
	int numPieces
	
	public PiecewiseSymbol(int n) {
		super("pcwise","pcwise");
		numPieces=n;
	}
	
	public String getOpening() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mrow><m:mo>{</m:mo><m:mtable>"
		}
		return null
		
	}
	
	public String getClosing() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "</m:mtable></m:mrow>"
		}
		return null
	}
	
	public int getPieceCount() {
		return numPieces
	}

	public String getMapping() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mo>${mapsTo}</mo>"
		}
		return null
	}
}
