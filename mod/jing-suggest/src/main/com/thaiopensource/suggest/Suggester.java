package com.thaiopensource.suggest;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;

import java.util.List;

public interface Suggester extends ContentHandler, DTDHandler {
  List<ElementSuggestion> suggestElements();
  String suggestClosingTag();
  List<AttributeNameSuggestion> suggestAttributeNames();
  List<AttributeValueSuggestion> suggestAttributeValues(String fragment);
  void reset();
}
