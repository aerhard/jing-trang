package com.thaiopensource.suggest.relaxng.pattern;

import org.xml.sax.SAXException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;

import java.io.IOException;

public interface PatternFuture {
  Pattern getPattern(boolean isAttributesPattern) throws IllegalSchemaException, SAXException, IOException;
}
