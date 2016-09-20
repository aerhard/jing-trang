package com.thaiopensource.suggest.relaxng.impl;

import com.thaiopensource.suggest.relaxng.sax.PatternValidator;
import com.thaiopensource.suggest.relaxng.pattern.Pattern;
import com.thaiopensource.suggest.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;

public class RngValidator extends PatternValidator implements Validator {
  public RngValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    super(pattern, builder, eh);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }
}
