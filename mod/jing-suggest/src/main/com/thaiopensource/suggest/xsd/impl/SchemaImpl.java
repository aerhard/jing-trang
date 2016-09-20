package com.thaiopensource.suggest.xsd.impl;

import com.thaiopensource.suggest.Suggester;
import com.thaiopensource.suggest.SuggesterSchema;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.Validator;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;

class SchemaImpl extends AbstractSchema implements SuggesterSchema {
  private final SymbolTable symbolTable;
  private final XMLGrammarPool grammarPool;

  SchemaImpl(SymbolTable symbolTable,
             XMLGrammarPool grammarPool,
             PropertyMap properties,
             PropertyId<?>[] supportedPropertyIds) {
    super(properties, supportedPropertyIds);
    this.symbolTable = symbolTable;
    this.grammarPool = grammarPool;
  }

  public Suggester createSuggester(PropertyMap properties) {
    return new SuggesterImpl(symbolTable, grammarPool, properties);
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(symbolTable, grammarPool, properties);
  }
}
