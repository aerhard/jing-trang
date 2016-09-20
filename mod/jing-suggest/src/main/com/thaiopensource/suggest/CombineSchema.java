package com.thaiopensource.suggest;

// slightly extended version of com.thaiopensource.validate.CombineSchema

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.CombineValidator;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.Validator;

public class CombineSchema extends AbstractSchema implements SuggesterSchema {
  private final SuggesterSchema schema1;
  private final Schema schema2;

  public CombineSchema(SuggesterSchema schema1, Schema schema2, PropertyMap properties) {
    super(properties);
    this.schema1 = schema1;
    this.schema2 = schema2;
  }

  public Suggester createSuggester(PropertyMap properties) {
    return schema1.createSuggester(properties);
  }

  public Validator createValidator(PropertyMap properties) {
    return new CombineValidator(schema1.createValidator(properties),
        schema2.createValidator(properties));
  }

}
