package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

import java.util.HashSet;
import java.util.Set;

class PossibleAttributeValueSuggestionsFunction extends AbstractPatternFunction<VoidValue> {
  private boolean inList = false;

  private Set<ValueSuggestion> patterns = new HashSet<ValueSuggestion>();

 Set<ValueSuggestion> applyTo(Pattern p) {
    p.apply(this);
    return patterns;
  }

  void add(ValuePattern p) {
    patterns.add(new ValueSuggestion(p, inList));
  }

  public VoidValue caseValue(ValuePattern p) {
    add(p);
    return VoidValue.VOID;
  }

  public VoidValue caseList(ListPattern p) {
    inList = true;
    p.getOperand().apply(this);
    inList = false;
    return VoidValue.VOID;
  }

  public VoidValue caseGroup(GroupPattern p) {
    return caseBinary(p);
  }
  public VoidValue caseAfter(AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public VoidValue caseBinary(BinaryPattern p) {
    p.getOperand1().apply(this);
    p.getOperand2().apply(this);
    return VoidValue.VOID;
  }

  public VoidValue caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }
  public VoidValue caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }
  public VoidValue caseOneOrMore(OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }
  public VoidValue caseOther(Pattern p) {
    return VoidValue.VOID;
  }
}
