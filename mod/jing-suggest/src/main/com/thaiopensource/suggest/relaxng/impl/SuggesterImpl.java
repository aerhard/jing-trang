package com.thaiopensource.suggest.relaxng.impl;

// this file is based on com.thaiopensource.PatternValidator

import com.thaiopensource.suggest.*;
import com.thaiopensource.datatype.Datatype2;
import com.thaiopensource.relaxng.sax.Context;
import com.thaiopensource.suggest.relaxng.pattern.*;
import com.thaiopensource.xml.util.Name;

import org.relaxng.datatype.Datatype;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.*;

public class SuggesterImpl extends Context implements Suggester {

  private PatternMatcher matcher;
  private final ErrorHandler eh;
  private boolean bufferingCharacters = false;
  private final StringBuilder charBuf = new StringBuilder();
  private Locator locator = null;


  private Name lastName = null;
  private Attributes lastAtts = null;
  private Stack<String> qNames = new Stack<String>();


  public SuggesterImpl(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    this.matcher = new PatternMatcher(pattern, builder);
    this.eh = eh;
  }

  public void startElement(String namespaceURI,
                           String localName,
                           String qName,
                           Attributes atts) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeStartTag(charBuf.toString(), this));
    }

    Name name = new Name(namespaceURI, localName);
    check(matcher.matchStartTagOpen(name, qName, this));

    lastName = name;
    lastAtts = atts;
    qNames.push(qName);

    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));
      String attQName = atts.getQName(i);
      check(matcher.matchAttributeName(attName, attQName, this));
      check(matcher.matchAttributeValue(atts.getValue(i), attName, attQName, this));
    }
    check(matcher.matchStartTagClose(name, qName, this));
    if (matcher.isTextTyped()) {
      bufferingCharacters = true;
      charBuf.setLength(0);
    }
  }

  public void endElement(String namespaceURI,
                         String localName,
                         String qName) throws SAXException {
    qNames.pop();

    if (bufferingCharacters) {
      bufferingCharacters = false;
      if (charBuf.length() > 0)
        check(matcher.matchTextBeforeEndTag(charBuf.toString(), new Name(namespaceURI, localName),
            qName, this));
    }
    check(matcher.matchEndTag(new Name(namespaceURI, localName), qName, this));
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (bufferingCharacters) {
      charBuf.append(ch, start, length);
      return;
    }
    for (int i = 0; i < length; i++) {
      switch (ch[start + i]) {
        case ' ':
        case '\r':
        case '\t':
        case '\n':
          break;
        default:
          check(matcher.matchUntypedText(this));
          return;
      }
    }
  }

  public void endDocument() throws SAXException {
    check(matcher.matchEndDocument());
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startDocument() throws SAXException {
    check(matcher.matchStartDocument());
  }

  public void processingInstruction(String target, String date) {
  }

  public void skippedEntity(String name) {
  }

  public void ignorableWhitespace(char[] ch, int start, int len) {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeStartTag(charBuf.toString(), this));
    }
    super.startPrefixMapping(prefix, uri);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }

  public void reset() {
    super.reset();
    bufferingCharacters = false;
    locator = null;
    matcher = (PatternMatcher) matcher.start();
  }

  private void check(boolean ok) throws SAXException {
    if (!ok)
      eh.error(new SAXParseException(matcher.getErrorMessage(), locator));
  }

  @Override
  public String suggestClosingTag() {
    return qNames.size() > 0 ? qNames.peek() : null;
  }

  @Override
  public List<ElementSuggestion> suggestElements() {
    List<ElementSuggestion> suggestions = new ArrayList<ElementSuggestion>();

    NormalizedSuggestions nss = matcher.getStartTagSuggestions();

    if (nss.isAnyNameIncluded()) {
      suggestions.add(new ElementSuggestion("*#*", null, null, false));
    }

    if (nss.hasNamedInclusions()) {
      String defaultNsUri = resolveNamespacePrefix("");

      Set<NameSuggestion> names = nss.getIncludedNames();
      suggestions.addAll(formatElementSuggestions(names, defaultNsUri));

      Set<NamespaceSuggestion> namespaces = nss.getIncludedNamespaces();
      for (NamespaceSuggestion namespace : namespaces) {
        suggestions.add(formatElementNamespaceSuggestion(namespace, defaultNsUri));
      }
    }

    return suggestions;
  }

  private List<ElementSuggestion> formatElementSuggestions(Set<NameSuggestion> names, String defaultNsUri) {
    List<ElementSuggestion> results = new ArrayList<ElementSuggestion>();

    for (NameSuggestion name : names) {
      results.add(formatElementSuggestion(name, defaultNsUri));
    }
    return results;
  }

  private ElementSuggestion formatElementSuggestion(NameSuggestion mentionedName, String defaultNsUri) {
    Name name = mentionedName.getName();
    String value = createElementNameValue(name, defaultNsUri);

    matcher.matchStartTagOpen(name, "", this);

    Set<Name> allRequiredAttNames = matcher.requiredAttributeNames();
    List<String> attributes = null;

    if (!allRequiredAttNames.isEmpty()) {
      attributes = new ArrayList<String>();
      for (Name attName : allRequiredAttNames) {
        attributes.add(createAttributeNameValue(attName));
      }
    }

    List<String> annotations = createAnnotations(mentionedName.getPattern(), mentionedName.getNameClass());

    return new ElementSuggestion(value, annotations, attributes, false);
  }

  private ElementSuggestion formatElementNamespaceSuggestion(NamespaceSuggestion namespaceSuggestion, String defaultNsUri) {
    String namespace = namespaceSuggestion.getNamespace();
    String value = createNamespace(namespace, defaultNsUri);
    List<String> annotations = createAnnotations(namespaceSuggestion.getPattern(), namespaceSuggestion.getNameClass());

    return new ElementSuggestion(value, annotations, null, false);
  }

  private AttributeNameSuggestion formatAttributeNamespaceSuggestion(NamespaceSuggestion namespaceSuggestion, String defaultNsUri) {
    String namespace = namespaceSuggestion.getNamespace();
    String value = createNamespace(namespace, defaultNsUri);
    List<String> annotations = createAnnotations(namespaceSuggestion.getPattern(), namespaceSuggestion.getNameClass());

    return new AttributeNameSuggestion(value, annotations);
  }

  private List<String> createAnnotations(Pattern pattern, NameClass nameClass) {
    List<String> patternAnnotations = AnnotationSerializer.getAnnotationStrings(pattern);
    List<String> nameClassAnnotations = AnnotationSerializer.getAnnotationStrings(nameClass);

    boolean hasPatternAnnotations = patternAnnotations != null && !patternAnnotations.isEmpty();
    boolean hasNameClassAnnotations = nameClassAnnotations != null && !nameClassAnnotations.isEmpty();

    if (hasPatternAnnotations || hasNameClassAnnotations) {
      List<String> result = new ArrayList<String>();
      if (hasPatternAnnotations) result.addAll(patternAnnotations);
      if (hasNameClassAnnotations) result.addAll(nameClassAnnotations);
      return result;
    }
    return null;
  }

  private String createElementNameValue(Name name, String defaultNsUri) {
    StringBuilder sb = new StringBuilder();

    String localName = name.getLocalName();
    String nsUri = name.getNamespaceUri();
    if (defaultNsUri != null && defaultNsUri.equals(nsUri)) {
      sb.append(localName);
    } else {
      String prefix = getPrefix(nsUri);
      if (prefix != null) {
        sb.append(prefix);
        sb.append(":");
        sb.append(localName);
      } else {
        sb.append(localName);
        sb.append("#");
        sb.append(nsUri);
      }
    }
    return sb.toString();
  }

  private String createNamespace(String namespace, String defaultNsUri) {
    StringBuilder sb = new StringBuilder();

    if (defaultNsUri != null && defaultNsUri.equals(namespace)) {
      sb.append("*");
    } else {
      String prefix = getPrefix(namespace);
      if (prefix != null && !"".equals(prefix)) {
        sb.append(prefix);
        sb.append(":*");
      } else {
        sb.append("*#");
        sb.append(namespace);
      }
    }

    return sb.toString();
  }

  private String createAttributeNameValue(Name name) {
    StringBuilder sb = new StringBuilder();
    String localName = name.getLocalName();
    String nsUri = name.getNamespaceUri();
    if ("".equals(nsUri)) {
      sb.append(localName);
    } else {
      String prefix = getPrefix(nsUri);
      if (prefix != null && !"".equals(prefix)) {
        sb.append(prefix);
        sb.append(":");
        sb.append(localName);
      } else {
        sb.append(localName);
        sb.append("#");
        sb.append(nsUri);
      }
    }
    return sb.toString();
  }

  @Override
  public List<AttributeNameSuggestion> suggestAttributeNames() {
    List<AttributeNameSuggestion> suggestions = new ArrayList<AttributeNameSuggestion>();

    matcher.matchStartTagOpen(lastName, "", this);

    NormalizedSuggestions nss = matcher.getAttributeNameSuggestions();

    if (nss.isAnyNameIncluded()) {
      suggestions.add(new AttributeNameSuggestion("*#*", null));
    }

    if (nss.hasNamedInclusions()) {
      Set<NameSuggestion> allNames = nss.getIncludedNames();
      Attributes existingAtts = lastAtts;
      Set<NameSuggestion> names = rejectExistingAttributes(allNames, existingAtts);

      suggestions.addAll(formatPossibleAttributeSuggestions(names));

      Set<NamespaceSuggestion> namespaces = nss.getIncludedNamespaces();
      String defaultNsUri = "";

      for (NamespaceSuggestion namespace : namespaces) {
        suggestions.add(formatAttributeNamespaceSuggestion(namespace, defaultNsUri));
      }
    }

    return suggestions;
  }

  private List<AttributeNameSuggestion> formatPossibleAttributeSuggestions(Set<NameSuggestion> attNames) {
    List<AttributeNameSuggestion> suggestions = new ArrayList<AttributeNameSuggestion>();

    for (NameSuggestion mn : attNames) {
      String value = createAttributeNameValue(mn.getName());
      List<String> annotations = createAnnotations(mn.getPattern(), mn.getNameClass());
      suggestions.add(new AttributeNameSuggestion(value, annotations));
    }

    return suggestions;
  }

  @Override
  public List<AttributeValueSuggestion> suggestAttributeValues(String fragment) {
    List<AttributeValueSuggestion> suggestions = new ArrayList<AttributeValueSuggestion>();

    String[] tokens = fragment.split(" ", 2);
    String qName = tokens[0];

    matcher.matchStartTagOpen(lastName, "", this);

    Attributes existingAtts = lastAtts;
    int fragmentIndex = existingAtts.getIndex(qName);

    if (fragmentIndex > -1) {
      String nsUri = existingAtts.getURI(fragmentIndex);
      String localName = existingAtts.getLocalName(fragmentIndex);

      Name name = new Name(nsUri, localName);
      matcher.matchAttributeName(name, qName, this);

      Set<ValueSuggestion> valueSuggestions = matcher.getAttributeValueSuggestions();

      for (ValueSuggestion vs : valueSuggestions) {
        suggestions.add(formatValueSuggestion(vs));
      }
    }

    return suggestions;
  }

  private AttributeValueSuggestion formatValueSuggestion(ValueSuggestion vs) {
    ValuePattern p = vs.getPattern();
    Datatype dt = p.getDatatype();
    String stringValue = p.getStringValue();

    if (stringValue != null) {
      Object value = p.getValue();
      if (value instanceof String && dt instanceof Datatype2) stringValue = (String) value;
    }

    List<String> annotations = createAnnotations(p, null);

    return new AttributeValueSuggestion(stringValue, annotations, vs.isInList());
  }

  private Set<NameSuggestion> rejectExistingAttributes(Set<NameSuggestion> names, Attributes atts) {
    if (atts == null) return names;

    int len = atts.getLength();

    Set<NameSuggestion> result = new HashSet<NameSuggestion>();

    for (NameSuggestion mn : names) {
      Name name = mn.getName();
      boolean exists = false;
      for (int i = 0; i < len; i++) {
        if (atts.getLocalName(i).equals(name.getLocalName()) &&
            atts.getURI(i).equals(name.getNamespaceUri())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        result.add(mn);
      }
    }
    return result;
  }
}
