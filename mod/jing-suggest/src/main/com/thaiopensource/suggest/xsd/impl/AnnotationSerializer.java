package com.thaiopensource.suggest.xsd.impl;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotationSerializer implements ContentHandler {

  private List<String> annotations = new ArrayList<String>();
  private StringBuilder sb = new StringBuilder();

  private int level = 0;
  private boolean shouldWrite = false;

  public void reset() {
    sb.setLength(0);
    annotations = new ArrayList<String>();
    level = 0;
    shouldWrite = false;
  }

  public List<String> getAnnotationStrings() {
    return annotations;
  }

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
    level++;

    if (level == 2 && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(uri) && "documentation".equals(localName)) {
      shouldWrite = true;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (shouldWrite && level == 2) {
      annotations.add(sb.toString().replaceAll("\\s+", " ").trim());
      sb = new StringBuilder();
    }

    if (level == 2) {
      shouldWrite = false;
    }

    level--;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (shouldWrite) sb.append(Arrays.copyOfRange(ch, start, start + length));
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

  @Override
  public void processingInstruction(String target, String data) throws SAXException {}

  @Override
  public void skippedEntity(String name) throws SAXException {}
}
