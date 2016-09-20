package com.thaiopensource.suggest;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Schema;

public interface SuggesterSchema extends Schema {
  Suggester createSuggester(PropertyMap properties);
}
