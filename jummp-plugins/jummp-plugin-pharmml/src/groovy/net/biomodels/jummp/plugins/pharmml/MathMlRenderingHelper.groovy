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
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Apache Tika, Apache Commons, LibPharmml, Perf4j (or a modified version of these
* libraries), containing parts covered by the terms of Apache License v2.0,
* the licensors of this Program grant you additional permission to convey the
* resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Tika, Apache Commons,
* LibPharmml, Perf4j used as well as that of the covered work.}
**/

package net.biomodels.jummp.plugins.pharmml

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariableType
import eu.ddmore.libpharmml.dom.commontypes.FalseBooleanType
import eu.ddmore.libpharmml.dom.commontypes.FuncParameterDefinitionType
import eu.ddmore.libpharmml.dom.commontypes.FunctionDefinitionType
import eu.ddmore.libpharmml.dom.commontypes.IdValueType
import eu.ddmore.libpharmml.dom.commontypes.IntValueType
import eu.ddmore.libpharmml.dom.commontypes.RealValueType
import eu.ddmore.libpharmml.dom.commontypes.Rhs
import eu.ddmore.libpharmml.dom.commontypes.ScalarRhs
import eu.ddmore.libpharmml.dom.commontypes.SequenceType
import eu.ddmore.libpharmml.dom.commontypes.StringValueType
import eu.ddmore.libpharmml.dom.commontypes.SymbolRefType
import eu.ddmore.libpharmml.dom.commontypes.TrueBooleanType
import eu.ddmore.libpharmml.dom.maths.BinopType
import eu.ddmore.libpharmml.dom.maths.ConstantType
import eu.ddmore.libpharmml.dom.maths.Equation
import eu.ddmore.libpharmml.dom.maths.EquationType
import eu.ddmore.libpharmml.dom.maths.FunctionCallType
import eu.ddmore.libpharmml.dom.maths.UniopType
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import net.biomodels.jummp.plugins.pharmml.maths.FunctionSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsSymbol
import net.biomodels.jummp.plugins.pharmml.maths.MathsUtil
import net.biomodels.jummp.plugins.pharmml.maths.OperatorSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PieceSymbol
import net.biomodels.jummp.plugins.pharmml.maths.PiecewiseSymbol

class MathMlRenderingHelper {
    static void prefixToInfix(StringBuilder builder, List<MathsSymbol> stack) {
        if (stack.isEmpty()) {
            return
        }
        MathsSymbol symbol = stack.pop()
        if (symbol instanceof OperatorSymbol) {
            OperatorSymbol operator=symbol as OperatorSymbol
            if (operator.type == OperatorSymbol.OperatorType.BINARY) {
                builder.append(operator.getOpening())
                if (operator.needsTermSeparation) {
                    builder.append("<mrow>")
                }
                prefixToInfix(builder,stack)
                if (operator.needsTermSeparation) {
                    builder.append("</mrow>")
                }
                builder.append(operator.getMapping())
                if (operator.needsTermSeparation) {
                    builder.append("<mrow>")
                }
                prefixToInfix(builder,stack)
                if (operator.needsTermSeparation) {
                    builder.append("</mrow>")
                }
                builder.append(operator.getClosing())
            }
            // Special case of root/square root, handled differently from other
            // operators.
            else if (operator.type == OperatorSymbol.OperatorType.ROOT) {
                StringBuilder operandBuilder = new StringBuilder()
                prefixToInfix(operandBuilder, stack)
                StringBuilder rootBuilder = new StringBuilder()
                prefixToInfix(rootBuilder, stack)
                boolean isSquareRoot = false
                try {
                    String rootValue=rootBuilder.toString().replace("<mi>","").replace("</mi>","")
                    double value = Double.parseDouble(rootValue)
                    if (value==2.0) {
                        isSquareRoot=true
                    }
                }
                catch(Exception notANumber) {
                }
                if (!isSquareRoot) {
                    builder.append("<mroot><mrow>")
                    builder.append(operandBuilder)
                    builder.append("</mrow><mrow>")
                    builder.append(rootBuilder)
                    builder.append("</mrow></mroot>")
                }
                else {
                    builder.append("<msqrt>")
                    builder.append(operandBuilder)
                    builder.append("</msqrt>")
                }
            }
            // Special case of power, handled differently from other
            // operators.
            else if (operator.type==OperatorSymbol.OperatorType.POWER) {
                StringBuilder operandBuilder=new StringBuilder()
                prefixToInfix(operandBuilder, stack)
                StringBuilder powerBuilder=new StringBuilder()
                prefixToInfix(powerBuilder, stack)
                boolean isSquareRoot=false
                builder.append("<msup><mrow>")
                builder.append(operandBuilder)
                builder.append("</mrow><mrow>")
                builder.append(powerBuilder)
                builder.append("</mrow></msup>")
            }
            else {
                builder.append(operator.getMapping())
                builder.append(operator.getOpening())
                prefixToInfix(builder,stack)
                builder.append(operator.getClosing())
            }
        } 
        else if (symbol instanceof FunctionSymbol) {
            FunctionSymbol function=symbol as FunctionSymbol
            builder.append(function.getMapping())
                builder.append(function.getOpening())
                for (int i=0; i<function.getArgCount(); i++) {
                    prefixToInfix(builder, stack)
                    if (i!=function.getArgCount()-1) {
                        builder.append(function.getArgSeparator())
                    }
                }
                builder.append(function.getClosing())
        }
        else if (symbol instanceof PiecewiseSymbol) {
            PiecewiseSymbol piecewise=symbol as PiecewiseSymbol
            builder.append(piecewise.getOpening())
            for (int i=0; i<piecewise.getPieceCount(); i++) {
                prefixToInfix(builder, stack)
            }
            builder.append(piecewise.getClosing())
        }
        else if (symbol instanceof PieceSymbol) {
            PieceSymbol piece=symbol as PieceSymbol
            builder.append(piece.getOpening())
            builder.append(piece.getTermStarter())
            prefixToInfix(builder, stack)
            builder.append(piece.getTermEnder())
            if (piece.type==PieceSymbol.ConditionType.OTHERWISE) {
                builder.append(piece.getOtherwiseText())
            }
            else {
                builder.append(piece.getIfText())
                builder.append(piece.getTermStarter())
                prefixToInfix(builder, stack)
                builder.append(piece.getTermEnder())
            }
            builder.append(piece.getClosing())
        }
        else {
            builder.append(symbol.getMapping())
        }
       // prefixToInfix(builder, stack)
    }

    static JAXBElement expandNestedSymbRefs(JAXBElement<SymbolRefType> symbRef,
            Map<String, Equation> transformations) {
        final EquationType TRANSF_EQ = resolveSymbolReference(symbRef.value, transformations)
        if (TRANSF_EQ) {
            final def FIRST_ELEM = TRANSF_EQ.scalarOrSymbRefOrBinop.first()
            final Class ELEM_CLASS = FIRST_ELEM.value.getClass()
            switch(ELEM_CLASS) {
                case BinopType:
                    break
                case UniopType:
                    break
                case SymbolRefType:
                    break
                case ConstantType:
                    break
                case FunctionCallType:
                    break
                case IdValueType:
                    break
                case StringValueType:
                    break
                case IntValueType:
                    break
                case RealValueType:
                    break
                default:
                    assert false, "Cannot have ${ELEM_CLASS.name} inside a transformation."
                    break
            }
            return FIRST_ELEM
        } else {
            return symbRef
        }
    }

    static JAXBElement expandNestedUniop(JAXBElement<UniopType> jaxbUniop,
            Map<String, Equation> transfMap) {
        UniopType uniop = jaxbUniop.value
        UniopType replacement
        if (uniop.symbRef) {
            final EquationType TRANSF_EQ = resolveSymbolReference(uniop.symbRef, transfMap)
            if (TRANSF_EQ) {
                final def FIRST_ELEM = TRANSF_EQ.scalarOrSymbRefOrBinop.first().value
                final Class ELEM_CLASS = FIRST_ELEM.getClass()
                replacement = new UniopType()
                replacement.op = uniop.op
                switch(ELEM_CLASS) {
                    case BinopType:
                        replacement.binop = FIRST_ELEM
                        break
                    case UniopType:
                        replacement.uniop = FIRST_ELEM
                        break
                    case SymbolRefType:
                        replacement.symbRef = FIRST_ELEM
                        break
                    case ConstantType:
                        replacement.constant = FIRST_ELEM
                        break
                    case FunctionCallType:
                        replacement.functionCall = FIRST_ELEM
                        break
                    case IdValueType:
                        replacement.scalar = FIRST_ELEM
                        break
                    case StringValueType:
                        break
                    case IntValueType:
                        replacement.scalar = FIRST_ELEM
                        break
                    case RealValueType:
                        replacement.scalar = FIRST_ELEM
                        break
                    default:
                        assert false, "Cannot have ${ELEM_CLASS.name} inside a unary operator."
                        replacement = null
                        break
                }
            }
        } else if (uniop.uniop) {
            def expanded = expandNestedUniop(wrapJaxb(uniop.uniop), transfMap)?.value
            if (expanded && !(expanded.equals(uniop.uniop))) {
                uniop.uniop = expanded
            }
        } else if (uniop.binop) {
            def expanded = expandNestedBinop(wrapJaxb(uniop.binop), transfMap)?.value
            if (expanded && !(expanded.equals(uniop.binop))) {
                uniop.binop = expanded
            }
        }
        if (replacement) {
            return wrapJaxb(replacement)
        }
        return jaxbUniop
    }

    static JAXBElement expandNestedBinop(JAXBElement<BinopType> jaxbBinop,
            Map<String, Equation> transfMap) {
        BinopType binop = jaxbBinop.value
        List<JAXBElement> terms = binop.content
        def expandedTerms = terms.collect { c ->
            switch (c.value) {
                case SymbolRefType:
                    return expandNestedSymbRefs(c, transfMap)
                    break
                case BinopType:
                    return expandNestedBinop(c, transfMap)
                    break
                case UniopType:
                    return expandNestedUniop(c, transfMap)
                    break
                default:
                    return c
                    break
            }
        }
        if (expandedTerms.equals(terms)) {
            return jaxbBinop
        }
        BinopType expanded = new BinopType()
        expanded.op = binop.op
        expanded.content = expandedTerms
        return wrapJaxb(expanded)
    }

    static EquationType expandEquation(EquationType equation, Map<String, Equation> transfMap) {
        List<JAXBElement> eqTerms = equation.scalarOrSymbRefOrBinop
        List<JAXBElement> expandedTerms = eqTerms.collect {
            switch(it.value) {
                case BinopType:
                    return expandNestedBinop(it, transfMap)
                    break
                case UniopType:
                    return expandNestedUniop(it, transfMap)
                    break
                case SymbolRefType:
                    return expandNestedSymbRefs(it, transfMap)
                    break
                default:
                    return it
                    break
            }
        }
        if (!eqTerms.equals(expandedTerms)) {
            def newEquation = new EquationType()
            newEquation.scalarOrSymbRefOrBinop = expandedTerms
            return newEquation
        }
        return equation
    }

    static void convertEquation(def equation, StringBuilder builder, Map<String, Equation> transfMap = [:]) {
        def equationToProcess
        if (!transfMap) {
            equationToProcess = equation
        } else if ((equation instanceof EquationType) || (equation instanceof Equation)) {
            equationToProcess = expandEquation(equation, transfMap)
        } else {
            equationToProcess = equation
        }
        List<MathsSymbol> symbols = MathsUtil.convertToSymbols(equationToProcess).reverse()
        List<String> stack = new LinkedList<String>()
        symbols.each {
           stack.push(it)
        }
        prefixToInfix(builder, stack)
    }

}
