/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/





package net.biomodels.jummp.plugins.pharmml.maths

class OperatorSymbol extends MathsSymbol {
	public enum OperatorType {BINARY,UNARY, ROOT, POWER};
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
