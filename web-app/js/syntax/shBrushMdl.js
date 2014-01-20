/**
 * SyntaxHighlighter
 * http://alexgorbatchev.com/SyntaxHighlighter
 *
 * SyntaxHighlighter is donationware. If you are using it, please donate.
 * http://alexgorbatchev.com/SyntaxHighlighter/donate.html
 *
 * @version
 * 3.0.83 (July 02 2010)
 * 
 * @copyright
 * Copyright (C) 2004-2010 Alex Gorbatchev.
 *
 * @license
 * Dual licensed under the MIT and GPL licenses.
 */
;(function()
{
	// CommonJS
	typeof(require) != 'undefined' ? SyntaxHighlighter = require('shCore').SyntaxHighlighter : null;

	function Brush()
	{
		var keywords =	'mdlobj parobj dataobj taskobj telobj INDIVIDUAL_VARIABLES MODEL_PREDICTION RANDOM_VARIABLE_DEFINITION INPUT_VARIABLES STRUCTURAL_PARAMETERS ' +
						'VARIABILITY_PARAMETERS OUTPUT_VARIABLES GROUP_VARIABLES OBSERVATION ESTIMATION SIMULATION STRUCTURAL ' +
						'false VARIABILITY PRIOR_PARAMETERS HEADER FILE PARAMETER DATA IGNORE ACCEPT ' +
						'DROP ADD REMOVE MODEL list LIBRARY null continuous covariate  Normal Binomial Poisson Student_T MVNormal' +
						'ODE MIXTURE matrix diag same INLINE DESIGN RSCRIPT ESTIMATE SIMULATE EXECUTE LIKELIHOOD NMTRAN_CODE ' +
						'MLXTRAN_CODE PML_CODE BUGS_CODE R_CODE MATLAB_CODE MISSING mdv id dv idv dvid amt categorical ' +
						'~ true';

		this.regexList = [
			{ regex: SyntaxHighlighter.regexLib.singleLinePerlComments,	css: 'comments' },		// one line comments
			{ regex: SyntaxHighlighter.regexLib.doubleQuotedString,		css: 'string' },		// strings
			{ regex: SyntaxHighlighter.regexLib.singleQuotedString,		css: 'string' },		// strings
			{ regex: /\b([\d]+(\.[\d]+)?|0x[a-f0-9]+)\b/gi,				css: 'value' },			// numbers
			{ regex: new RegExp(this.getKeywords(keywords), 'gm'),		css: 'keyword' }		// MDL keywords
			];

		this.forHtmlScript({
			left	: /(&lt;|<)%[@!=]?/g, 
			right	: /%(&gt;|>)/g 
		});
	};

	Brush.prototype	= new SyntaxHighlighter.Highlighter();
	Brush.aliases	= ['mdl'];
	
	SyntaxHighlighter.brushes.mdl = Brush;

	// CommonJS
	typeof(exports) != 'undefined' ? exports.Brush = Brush : null;
})();
