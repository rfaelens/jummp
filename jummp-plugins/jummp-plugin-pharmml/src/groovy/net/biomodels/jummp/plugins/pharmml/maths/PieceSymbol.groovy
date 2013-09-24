package net.biomodels.jummp.plugins.pharmml.maths

class PieceSymbol extends MathsSymbol {
	enum ConditionType {EXTERNAL, OTHERWISE} 
	ConditionType type
	
	public PieceSymbol(ConditionType t) {
		super("piece","piece");
		type=t;
	}
	
	public String getOpening() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mtr>"
		}
		return null
		
	}
	
	public String getClosing() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "</mtr>"
		}
		return null
	}
	
	public String getTermStarter() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mtd>"
		}
		return null
	}
	
	public String getTermEnder() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "</mtd>"
		}
		return null
	}

	public String getIfText() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mtd columnalign=\"left\"><mtext>&#160; if &#160;</mtext></mtd>"
		}
		return null
	}

	public String getOtherwiseText() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<mtd colspan=\"2\" columnalign=\"left\"><mtext>&#160; otherwise</mtext></mtd>"
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
