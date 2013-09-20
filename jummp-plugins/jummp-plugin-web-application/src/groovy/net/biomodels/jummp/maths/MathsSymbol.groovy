package net.biomodels.jummp.maths

class MathsSymbol {
	String text;
	String mapsTo;
	
	public MathsSymbol(String t, String m) {
		text=t;
		mapsTo=m;
	}
	
	public String toString() {
		return mapsTo
	}
}
