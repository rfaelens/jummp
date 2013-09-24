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
			return "<m:mtr>"
		}
		return null
		
	}
	
	public String getClosing() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "</m:mtr>"
		}
		return null
	}
	
	public String getTermStarter() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<m:mtd>"
		}
		return null
	}
	
	public String getTermEnder() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "</m:mtd>"
		}
		return null
	}

	public String getIfText() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<m:mtd columnalign=\"left\"><m:mtext>&#160; if &#160;</m:mtext></m:mtd>"
		}
		return null
	}

	public String getOtherwiseText() {
		if (format==MathsSymbol.OutputFormat.MATHML) {
			return "<m:mtd colspan=\"2\" columnalign=\"left\"><m:mtext>&#160; otherwise</m:mtext></m:mtd>"
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
