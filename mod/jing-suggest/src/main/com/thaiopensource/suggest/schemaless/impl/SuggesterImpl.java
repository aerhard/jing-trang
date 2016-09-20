package com.thaiopensource.suggest.schemaless.impl;

import com.thaiopensource.suggest.AttributeNameSuggestion;
import com.thaiopensource.suggest.AttributeValueSuggestion;
import com.thaiopensource.suggest.ElementSuggestion;
import com.thaiopensource.suggest.Suggester;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.Stack;

public class SuggesterImpl implements Suggester {
  private Stack<String> qNames = new Stack<String>();

  @Override
  public List<ElementSuggestion> suggestElements() {
    return null;
  }
  @Override
  public String suggestClosingTag() {
    return qNames.size() > 0 ? qNames.peek() : null;
  }
  @Override
  public List<AttributeNameSuggestion> suggestAttributeNames() {
    return null;
  }
  @Override
  public List<AttributeValueSuggestion> suggestAttributeValues(String fragment) {
    return null;
  }
  @Override
  public void reset() {}
  @Override
  public void setDocumentLocator(Locator locator) {}
  @Override
  public void startDocument() throws SAXException {}
  @Override
  public void endDocument() throws SAXException {}
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {}
  @Override
  public void endPrefixMapping(String prefix) throws SAXException {}

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    qNames.push(qName);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    qNames.pop();
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {}
  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
  @Override
  public void processingInstruction(String target, String data) throws SAXException {}
  @Override
  public void skippedEntity(String name) throws SAXException {}
  @Override
  public void notationDecl(String name, String publicId, String systemId) throws SAXException {}
  @Override
  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {}
}
