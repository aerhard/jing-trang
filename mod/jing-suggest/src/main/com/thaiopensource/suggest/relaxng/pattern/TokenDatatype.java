package com.thaiopensource.suggest.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

public class TokenDatatype extends StringDatatype {
  public Object createValue(String str, ValidationContext vc) {
    return StringNormalizer.normalize(str);
  }
}
