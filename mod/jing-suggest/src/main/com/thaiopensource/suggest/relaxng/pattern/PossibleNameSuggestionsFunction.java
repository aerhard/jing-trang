package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

abstract class PossibleNameSuggestionsFunction extends AbstractPatternFunction<VoidValue> {
  private final NameSuggestionNormalizer normalizer = new NameSuggestionNormalizer();

  NormalizedSuggestions applyTo(Pattern p) {
    p.apply(this);
    return normalizer.normalize();
  }

  void add(Pattern p, NameClass nc) {
    normalizer.add(p, nc);
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
