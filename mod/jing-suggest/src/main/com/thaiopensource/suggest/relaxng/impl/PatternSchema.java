package com.thaiopensource.suggest.relaxng.impl;

import com.thaiopensource.suggest.Suggester;
import com.thaiopensource.suggest.SuggesterSchema;
import com.thaiopensource.suggest.relaxng.pattern.Pattern;
import com.thaiopensource.suggest.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.suggest.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ErrorHandler;

public class PatternSchema extends AbstractSchema implements SuggesterSchema {
  private final SchemaPatternBuilder spb;
  private final Pattern start;

  public PatternSchema(SchemaPatternBuilder spb, Pattern start, PropertyMap properties) {
    super(properties);
    this.spb = spb;
    this.start = start;
  }

  public Suggester createSuggester(PropertyMap properties) {
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    return new SuggesterImpl(start, new ValidatorPatternBuilder(spb), eh);
  }

  public Validator createValidator(PropertyMap properties) {
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    return new RngValidator(start, new ValidatorPatternBuilder(spb), eh);
  }
}

