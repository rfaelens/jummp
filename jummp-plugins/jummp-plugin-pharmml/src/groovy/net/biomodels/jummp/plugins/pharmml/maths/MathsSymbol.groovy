package net.biomodels.jummp.plugins.pharmml.maths

class MathsSymbol {
	public enum OutputFormat {MATHML, LATEX}
	OutputFormat format=OutputFormat.MATHML;
	String text;
	String mapsTo;
	
	public MathsSymbol(String t, String m) {
		text=t;
		mapsTo=m;
	}
	
	public String getMapping() {
		if (format==OutputFormat.MATHML) {
			return "<mi>${mapsTo}</mi>"
		}
		return null
	}
	
	public String toString() {
		return mapsTo
	}
}
