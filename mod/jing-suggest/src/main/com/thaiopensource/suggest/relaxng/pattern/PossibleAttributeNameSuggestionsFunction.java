package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

/**
 * PatternFunction to compute the name class of possible attributes.
 * Computes a NormalizedNameClass.
 */
class PossibleAttributeNameSuggestionsFunction extends PossibleNameSuggestionsFunction {
  public VoidValue caseAttribute(AttributePattern p) {
    add(p, p.getNameClass());
    return VoidValue.VOID;
  }

  public VoidValue caseGroup(GroupPattern p) {
    return caseBinary(p);
  }
}
