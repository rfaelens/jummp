/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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

class MathsSymbol {
	public enum OutputFormat {MATHML, LATEX}
	OutputFormat format=OutputFormat.MATHML;
	String text;
	String mapsTo;
	
	public MathsSymbol(String t, String m) {
		text=t;
		mapsTo=m;
	}
	
	private String simplifyNumber() {
		try
		{
			double number=Double.parseDouble(mapsTo)
			if (number==Math.ceil(number)) {
				return ""+(int)number
			}
		}
		catch(Exception ignore) {
		}
		return mapsTo
	}
	
	public String getMapping() {
		if (format==OutputFormat.MATHML) {
			return "<mi>${simplifyNumber()}</mi>"
		}
		return null
	}
	
	public String toString() {
		return mapsTo
	}
}
