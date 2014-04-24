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
