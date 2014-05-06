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
