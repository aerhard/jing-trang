package com.thaiopensource.suggest.xsd.impl;

import com.thaiopensource.suggest.*;
import com.thaiopensource.suggest.xsd.xerces.XmlSchemaValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.xs.*;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xpath.regex.ParseException;
import org.apache.xerces.impl.xpath.regex.RegularExpression;
import org.apache.xerces.impl.xs.*;
import org.apache.xerces.util.*;
import org.apache.xerces.xni.*;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.*;
import org.apache.xerces.xs.*;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.*;

import static org.apache.xerces.impl.dv.XSSimpleType.PRIMITIVE_NOTATION;
import static org.apache.xerces.impl.dv.XSSimpleType.PRIMITIVE_QNAME;
import static org.apache.xerces.xs.XSAnnotation.SAX_CONTENTHANDLER;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.*;

public class SuggesterImpl extends ParserConfigurationSettings implements Suggester, XMLLocator, XMLEntityResolver, EntityState {

  private final XmlSchemaValidator schemaValidator = new XmlSchemaValidator();
  private final XMLErrorReporter errorReporter = new XMLErrorReporter();
  private final XMLEntityManager entityManager = new XMLEntityManager();
  private final ValidationManager validationManager = new ValidationManager();
  private final NamespaceContext namespaceContext = new NamespaceSupport();

  private Attributes originalAttributes = new AttributesImpl();

  private final XMLAttributes attributes = new XMLAttributesImpl();
  private final SymbolTable symbolTable;
  private final XMLComponent[] components;
  private Locator locator;
  private final Set<String> entities = new HashSet<String>();
  private boolean pushedContext = false;

  // XXX deal with baseURI

  static private final String[] recognizedFeatures = {
      Features.SCHEMA_AUGMENT_PSVI,
      Features.SCHEMA_FULL_CHECKING,
      Features.VALIDATION,
      Features.SCHEMA_VALIDATION,
  };

  static private final String[] recognizedProperties = {
      Properties.XMLGRAMMAR_POOL,
      Properties.SYMBOL_TABLE,
      Properties.ERROR_REPORTER,
      Properties.ERROR_HANDLER,
      Properties.VALIDATION_MANAGER,
      Properties.ENTITY_MANAGER,
      Properties.ENTITY_RESOLVER,
  };
  private Stack<String> qNames = new Stack<String>();

  SuggesterImpl(SymbolTable symbolTable, XMLGrammarPool grammarPool, PropertyMap properties) {
    this.symbolTable = symbolTable;
    XMLErrorHandler errorHandlerWrapper = new ErrorHandlerWrapper(properties.get(ValidateProperty.ERROR_HANDLER));
    components = new XMLComponent[]{errorReporter, schemaValidator, entityManager};
    for (XMLComponent component : components) {
      addRecognizedFeatures(component.getRecognizedFeatures());
      addRecognizedProperties(component.getRecognizedProperties());
    }

    addRecognizedFeatures(recognizedFeatures);
    addRecognizedProperties(recognizedProperties);
    setFeature(Features.SCHEMA_AUGMENT_PSVI, false);
    setFeature(Features.SCHEMA_FULL_CHECKING, true);
    setFeature(Features.VALIDATION, true);
    setFeature(Features.SCHEMA_VALIDATION, true);
    setFeature(Features.ID_IDREF_CHECKING, true);
    setFeature(Features.IDC_CHECKING, true);
    setProperty(Properties.XMLGRAMMAR_POOL, grammarPool);
    setProperty(Properties.SYMBOL_TABLE, symbolTable);
    errorReporter.setDocumentLocator(this);
    setProperty(Properties.ERROR_REPORTER, errorReporter);
    setProperty(Properties.ERROR_HANDLER, errorHandlerWrapper);
    setProperty(Properties.VALIDATION_MANAGER, validationManager);
    setProperty(Properties.ENTITY_MANAGER, entityManager);
    setProperty(Properties.ENTITY_RESOLVER, this);
    reset();
  }

  public void reset() {
    validationManager.reset();
    namespaceContext.reset();
    for (XMLComponent component : components) component.reset(this);
    validationManager.setEntityState(this);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId) {
    // nothing needed
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName) {
    entities.add(name);
  }

  public boolean isEntityDeclared(String name) {
    return entities.contains(name);
  }

  public boolean isEntityUnparsed(String name) {
    return entities.contains(name);
  }

  public void startDocument()
      throws SAXException {
    try {
      schemaValidator.startDocument(locator == null ? null : this, null, namespaceContext, null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endDocument()
      throws SAXException {
    try {
      schemaValidator.endDocument(null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts)
      throws SAXException {
    originalAttributes = atts;
    qNames.push(qName);

    try {
      attributes.removeAllAttributes();

      if (!pushedContext)
        namespaceContext.pushContext();
      else
        pushedContext = false;
      for (int i = 0, len = atts.getLength(); i < len; i++) {
        attributes.addAttribute(makeQName(atts.getURI(i), atts.getLocalName(i), atts.getQName(i)),
            symbolTable.addSymbol(atts.getType(i)),
            atts.getValue(i));
      }
      schemaValidator.startElement(makeQName(namespaceURI, localName, qName), attributes, null);

    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endElement(String namespaceURI, String localName,
                         String qName)
      throws SAXException {
    qNames.pop();
    try {
      schemaValidator.endElement(makeQName(namespaceURI, localName, qName), null);
      namespaceContext.popContext();
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void startPrefixMapping(String prefix, String uri)
      throws SAXException {
    try {
      if (!pushedContext) {
        namespaceContext.pushContext();
        pushedContext = true;
      }
      if (prefix == null)
        prefix = XMLSymbols.EMPTY_STRING;
      else
        prefix = symbolTable.addSymbol(prefix);
      if (uri != null) {
        if (uri.equals(""))
          uri = null;
        else
          uri = symbolTable.addSymbol(uri);
      }
      namespaceContext.declarePrefix(prefix, uri);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endPrefixMapping(String prefix)
      throws SAXException {
    // do nothing
  }

  public void characters(char ch[], int start, int length)
      throws SAXException {
    try {
      schemaValidator.characters(new XMLString(ch, start, length), null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void ignorableWhitespace(char ch[], int start, int length)
      throws SAXException {
    try {
      schemaValidator.ignorableWhitespace(new XMLString(ch, start, length), null);
    } catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void processingInstruction(String target, String data)
      throws SAXException {
    // do nothing
  }

  public void skippedEntity(String name)
      throws SAXException {
    // do nothing
  }

  private QName makeQName(String namespaceURI, String localName, String qName) {
    localName = symbolTable.addSymbol(localName);
    String prefix;
    if (namespaceURI.equals("")) {
      namespaceURI = null;
      prefix = XMLSymbols.EMPTY_STRING;
      qName = localName;
    } else {
      namespaceURI = symbolTable.addSymbol(namespaceURI);
      if (qName.equals("")) {
        prefix = namespaceContext.getPrefix(namespaceURI);
        if (prefix == XMLSymbols.EMPTY_STRING)
          qName = localName;
        else if (prefix == null)
          qName = localName; // XXX what to do?
        else
          qName = symbolTable.addSymbol(prefix + ":" + localName);
      } else {
        qName = symbolTable.addSymbol(qName);
        int colon = qName.indexOf(':');
        if (colon > 0)
          prefix = symbolTable.addSymbol(qName.substring(0, colon));
        else
          prefix = XMLSymbols.EMPTY_STRING;
      }
    }
    return new QName(prefix, localName, qName, namespaceURI);
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
      throws XNIException, IOException {
    return null;
  }

  public String getPublicId() {
    return locator.getPublicId();
  }

  public String getEncoding() {
    return null;
  }

  public String getBaseSystemId() {
    return null;
  }

  public String getLiteralSystemId() {
    return null;
  }

  public String getExpandedSystemId() {
    return locator.getSystemId();
  }

  public int getLineNumber() {
    return locator.getLineNumber();
  }

  public int getColumnNumber() {
    return locator.getColumnNumber();
  }

  public int getCharacterOffset() {
    return -1;
  }

  public String getXMLVersion() {
    return "1.0";
  }

  static SAXException toSAXException(XNIException e) {
    if (e instanceof XMLParseException) {
      XMLParseException pe = (XMLParseException) e;
      return new SAXParseException(pe.getMessage(),
          pe.getPublicId(),
          pe.getExpandedSystemId(),
          pe.getLineNumber(),
          pe.getColumnNumber(),
          pe.getException());
    }
    Exception nested = e.getException();
    if (nested == null)
      return new SAXException(e.getMessage());
    if (nested instanceof SAXException)
      return (SAXException) nested;
    if (nested instanceof RuntimeException)
      throw (RuntimeException) nested;
    return new SAXException(nested);
  }

  @Override
  public String suggestClosingTag() {
    return qNames.size() > 0 ? qNames.peek() : null;
  }

  @Override
  public List<ElementSuggestion> suggestElements() {

    List<ElementSuggestion> suggestions = new ArrayList<ElementSuggestion>();

    if (schemaValidator.getSkipValidationDepth() >= 0) {
      return suggestions;
    }

    AnnotationSerializer annotationSerializer = new AnnotationSerializer();

    Set<Object> expectedEls = new HashSet<Object>();

    if (schemaValidator.getElementDepth() == -1 && schemaValidator.getCurrentCM() == null) {
      Grammar[] grammars = schemaValidator.getGrammarPool().retrieveInitialGrammarSet(XMLGrammarDescription.XML_SCHEMA);

      for (Grammar grammar : grammars) {
        SchemaGrammar schemaGrammar = (SchemaGrammar) grammar;
        XSModel model = schemaGrammar.toXSModel();
        XSNamedMap elementDeclarations = model.getComponents(XSConstants.ELEMENT_DECLARATION);
        expectedEls.addAll(elementDeclarations.values());
      }

    } else if (schemaValidator.getElementDepth() != -1) {

      XSComplexTypeDecl ctype = (XSComplexTypeDecl) schemaValidator.getCurrentType();
      Set<Object> next;
      if (ctype.getParticle() != null
          && (next = new HashSet<Object>(schemaValidator.getCurrentCM().whatCanGoHere(schemaValidator.getCurrCMState()))).size() > 0) {
        expectedEls = next;
      }
    }

    if (expectedEls.size() > 0) {
      Map<String, String> prefixMap = getPrefixMap(namespaceContext);

      Set<String> nsUris = new HashSet<String>();

      for (Object obj : expectedEls) {
        if (obj instanceof XSWildcardDecl) {
          XSWildcardDecl wildcardDecl = (XSWildcardDecl) obj;

          addWildcardComponents(wildcardDecl, XSConstants.ELEMENT_DECLARATION, expectedEls, nsUris, prefixMap);
        }
      }

      for (Object obj : expectedEls) {
        if (obj instanceof XSElementDecl) {
          XSElementDecl elDecl = (XSElementDecl) obj;

          String value = getElementNameValue(elDecl, prefixMap);

          List<String> attributes = null;
          XSAttributeGroupDecl attrGrp = getAttributeGroup(elDecl);
          if (attrGrp != null) {
            Set<String> attributesSet = getRequiredAttributes(attrGrp, prefixMap);
            if (!attributesSet.isEmpty()) {
              attributes = new ArrayList<String>();
              attributes.addAll(attributesSet);
            }
          }

          List<String> annotations = getAnnotations(annotationSerializer, elDecl.getAnnotation());

          suggestions.add(new ElementSuggestion(value, annotations, attributes, false));
        }
      }

      for (String nsUri : nsUris) {
        suggestions.add(getElementNamespaceSuggestion(nsUri, prefixMap));
      }
    }

    return suggestions;
  }

  private String getElementNameValue(XSElementDecl elDecl, Map<String, String> prefixMap) {
    String nsUri = elDecl.getNamespace();
    String localName = elDecl.getName();
    String prefix = prefixMap.get(nsUri);

    if (prefix != null) {
      return "".equals(prefix) ? localName : prefix + ":" + localName;
    }

    return localName + '#' + nsUri;
  }

  private ElementSuggestion getElementNamespaceSuggestion(String nsUri, Map<String, String> prefixMap) {
    String value;
    String prefix = prefixMap.get(nsUri);

    if (prefix == null) {
      value = "*#" + nsUri;
    } else if ("".equals(prefix)) {
      value = "*";
    } else {
      value = prefix + ":*";
    }
    return new ElementSuggestion(value, null, null, false);
  }

  private String getAttributeNameValue(XSAttributeDecl attrDecl, Map<String, String> prefixMap) {
    String nsUri = attrDecl.getNamespace();
    String localName = attrDecl.getName();
    String prefix;

    if (nsUri == null || "".equals(prefix = prefixMap.get(nsUri))) return localName;

    return prefix == null
        ? localName + "#" + nsUri
        : prefix + ":" + localName;
  }

  private AttributeNameSuggestion getAttributeNamespaceSuggestion(String nsUri, Map<String, String> prefixMap) {
    String value;
    String prefix;
    if (nsUri == null || "".equals(prefix = prefixMap.get(nsUri))) {
      value = "*";
    } else if (prefix == null) {
      value = "*#" + nsUri;
    } else {
      value = prefix + ":*";
    }

    return new AttributeNameSuggestion(value, null);
  }

  private List<String> getAnnotations(AnnotationSerializer annotationSerializer, XSAnnotation annot) {
    List<String> annotations = null;
    if (annot != null) {
      annot.writeAnnotation(annotationSerializer, SAX_CONTENTHANDLER);
      annotations = annotationSerializer.getAnnotationStrings();
      annotationSerializer.reset();
    }
    return annotations;
  }

  @Override
  public List<AttributeNameSuggestion> suggestAttributeNames() {
    List<AttributeNameSuggestion> suggestions = new ArrayList<AttributeNameSuggestion>();

    XSElementDeclaration elDecl = schemaValidator.getCurrentPSVIElementDecl();
    if (elDecl != null) {

      Map<String, String> prefixMap = getPrefixMap(namespaceContext);

      XSAttributeGroupDecl attrGrp = getAttributeGroup(elDecl);
      if (attrGrp != null) {
        Set<Object> attrDecls = new HashSet<Object>();
        Set<String> nsUris = new HashSet<String>();

        attrDecls.addAll(getUseAttrDecls(attrGrp, true));

        XSWildcardDecl wildcardDecl = (XSWildcardDecl) attrGrp.getAttributeWildcard();
        if (wildcardDecl != null) {
          addWildcardComponents(wildcardDecl, XSConstants.ATTRIBUTE_DECLARATION, attrDecls, nsUris, prefixMap);
        }

        AnnotationSerializer annotationSerializer = new AnnotationSerializer();

        for (Object obj : attrDecls) {
          if (obj instanceof XSAttributeDecl) {
            XSAttributeDecl attrDecl = (XSAttributeDecl) obj;
            String nsUri = attrDecl.getNamespace() == null ? "" : attrDecl.getNamespace();

            if (originalAttributes.getIndex(nsUri, attrDecl.getName()) == -1) {
              String value = getAttributeNameValue(attrDecl, prefixMap);
              List<String> annotations = getAnnotations(annotationSerializer, attrDecl.getAnnotation());
              suggestions.add(new AttributeNameSuggestion(value, annotations));
            }
          }
        }

        for (String nsUri : nsUris) {
          suggestions.add(getAttributeNamespaceSuggestion(nsUri, prefixMap));
        }
      }
    }

    return suggestions;
  }

  @Override
  public List<AttributeValueSuggestion> suggestAttributeValues(String fragment) {
    String[] tokens = fragment.split(" ", 2);
    String attrQName = tokens[0];
//    String currentValue = tokens.length > 1 ? tokens[1] : null;

    List<AttributeValueSuggestion> suggestions = new ArrayList<AttributeValueSuggestion>();

    XSElementDeclaration elDecl = schemaValidator.getCurrentPSVIElementDecl();
    if (elDecl != null) {
      XSAttributeGroupDecl attrGrp = getAttributeGroup(elDecl);
      if (attrGrp != null) {
        Map<String, String> prefixMap = getPrefixMap(namespaceContext);

        XSObjectList attrUses = attrGrp.getAttributeUses();
        XSAttributeUseImpl currUse = null;
        XSAttributeDecl currDecl = null;
        boolean foundMatch = false;

        int len = attrUses.getLength();
        for (int i = 0; i < len; i++) {
          currUse = (XSAttributeUseImpl) attrUses.item(i);
          currDecl = currUse.fAttrDecl;

          String value = getAttributeNameValue(currDecl, prefixMap);
          if (value.equals(attrQName)) {
            foundMatch = true;
            break;
          }
        }

        if (foundMatch && currDecl != null) {
          if (currUse.getConstraintType() == XSConstants.VC_FIXED) {
            String value = currUse.getConstraintValue();
            suggestions.add(new AttributeValueSuggestion(value, null, false));
          } else if (currDecl.getTypeDefinition() instanceof XSSimpleTypeDecl) {

            AnnotationSerializer annotationSerializer = new AnnotationSerializer();

            XSSimpleTypeDecl type = (XSSimpleTypeDecl) currDecl.getTypeDefinition();

            for (Object[] objs : getValueSuggestions(type)) {
              String value = objs[0].toString();
              List<String> annotations = null;

              if (objs[1] != null) {
                annotations = getAnnotations(annotationSerializer, (XSAnnotation) objs[1]);
              }
              suggestions.add(new AttributeValueSuggestion(value, annotations, (Boolean) objs[2]));
            }
          }
        }
      }
    }

    return suggestions;
  }

  private Set<Object[]> getValueSuggestions(XSSimpleTypeDecl type) {
    Set<Object[]> suggestions = new HashSet<Object[]>();

    boolean isList = type.getVariety() == VARIETY_LIST;

    if (isList && type.getItemType() instanceof XSSimpleTypeDecl) {
      type = (XSSimpleTypeDecl) type.getItemType();
    }

    XSObjectList memberTypes = type.getMemberTypes();
    int memberLen = memberTypes.getLength();
    for (int i = 0; i<memberLen;i++) {
      Object obj = memberTypes.get(i);
      if (obj instanceof XSSimpleTypeDecl) {
        XSSimpleTypeDecl memberType = (XSSimpleTypeDecl) obj;
        suggestions.addAll(getValueSuggestions(memberType));
      }
    }

    XSObjectList tl = type.enumerationAnnotations;

    ObjectList enumeration = type.getActualEnumeration();
    int valueLen = enumeration.getLength();
    for (int j = 0; j < valueLen; j++) {
      Object suggestion = enumeration.item(j);
      Object annot;
      if (tl != null && (annot = tl.item(j)) != null) {
        suggestions.add(new Object[] { suggestion, annot, false });
      } else {
        suggestions.add(new Object[] { suggestion, null, false });
      }
    }

    XSObjectList facets = type.getFacets();
    if (facets.getLength() > 0) {
      applyFacetsFilter(suggestions, facets, type);
    }

    StringList patternStrings = type.getLexicalPattern();
    if (patternStrings.getLength() > 0) {
      applyPatternFilter(suggestions, patternStrings);
    }

    if (isList) {
      for (Object[] suggestion : suggestions) {
        suggestion[2] = true;
      }
    }

    return suggestions;
  }

  private static final TypeValidator[] gDVs = {
      new AnySimpleDV(),
      new StringDV(),
      new BooleanDV(),
      new DecimalDV(),
      new FloatDV(),
      new DoubleDV(),
      new DurationDV(),
      new DateTimeDV(),
      new TimeDV(),
      new DateDV(),
      new YearMonthDV(),
      new YearDV(),
      new MonthDayDV(),
      new DayDV(),
      new MonthDV(),
      new HexBinaryDV(),
      new Base64BinaryDV(),
      new AnyURIDV(),
      new QNameDV(),
      null, // XML Schema 1.1 type
      new QNameDV(),   // notation use the same one as qname
      new IDDV(),
      new IDREFDV(),
      new EntityDV(),
      new IntegerDV(),
      new ListDV(),
      new UnionDV(),
      null, // XML Schema 1.1 type
      null, // XML Schema 1.1 type
      null // XML Schema 1.1 type
  };

  private void applyFacetsFilter(Set<Object[]> suggestions, XSObjectList facets, XSSimpleTypeDecl type) {
    int fl = facets.getLength();

    if (fl == 0) return;

    short primitiveKind = type.getPrimitiveKind();
    TypeValidator dv = gDVs[primitiveKind];

    Iterator<Object[]> it = suggestions.iterator();
    while (it.hasNext()) {
      Object suggestion = it.next()[0];

      for (int i = 0; i < fl;i++) {
        int compare;

        XSFacet facet = (XSFacet) facets.get(i);
        short kind = facet.getFacetKind();

        boolean canHaveLengthFacet = primitiveKind != PRIMITIVE_QNAME && primitiveKind != PRIMITIVE_NOTATION;

        if (kind == FACET_LENGTH && canHaveLengthFacet && dv.getDataLength(suggestion) != facet.getIntFacetValue()) {
          it.remove();
          break;
        } else if (kind == FACET_MINLENGTH && canHaveLengthFacet && dv.getDataLength(suggestion) < facet.getIntFacetValue()) {
          it.remove();
          break;
        } else if (kind == FACET_MAXLENGTH && canHaveLengthFacet && dv.getDataLength(suggestion) > facet.getIntFacetValue()) {
          it.remove();
          break;
        } else if (kind == FACET_FRACTIONDIGITS && dv.getFractionDigits(suggestion) > facet.getIntFacetValue()) {
          it.remove();
          break;
        } else if (kind == FACET_TOTALDIGITS && dv.getTotalDigits(suggestion) > facet.getIntFacetValue()) {
          it.remove();
          break;
        } else if (kind == FACET_MAXINCLUSIVE) {
          compare = dv.compare(suggestion, facet.getActualFacetValue());
          if (compare != -1 && compare != 0) {
            it.remove();
            break;
          }
        } else if (kind == FACET_MAXEXCLUSIVE) {
          compare = dv.compare(suggestion, facet.getActualFacetValue());
          if (compare != -1) {
            it.remove();
            break;
          }
        } else if (kind == FACET_MININCLUSIVE) {
          compare = dv.compare(suggestion, facet.getActualFacetValue());
          if (compare != 1 && compare != 0) {
            it.remove();
            break;
          }
        } else if (kind == FACET_MINEXCLUSIVE) {
          compare = dv.compare(suggestion, facet.getActualFacetValue());
          if (compare != 1) {
            it.remove();
            break;
          }
        }
      }
    }
  }

  private void applyPatternFilter(Set<Object[]> suggestions, StringList patternStrings) {
    List<RegularExpression> regexes = new ArrayList<RegularExpression>();
    for (int i = 0, j = patternStrings.getLength();i<j;i++) {
      String patternString = (String) patternStrings.get(i);
      try {
        RegularExpression regex = new RegularExpression(patternString, "X");
        regexes.add(regex);
      } catch (ParseException e) {}
    }

    Iterator<Object[]> it = suggestions.iterator();
    while (it.hasNext()) {
      Object suggestion = it.next()[0];
      if (!matchesPatterns(suggestion.toString(), regexes)) {
        it.remove();
      }
    }
  }

  private boolean matchesPatterns(String str, List<RegularExpression> regexes) {
    for (RegularExpression regex : regexes) if (!regex.matches(str)) return false;
    return true;
  }

  private Map<String, String> getPrefixMap(NamespaceContext namespaceContext) {
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
    Enumeration prefixes = namespaceContext.getAllPrefixes();
    while (prefixes.hasMoreElements()) {
      String prefix = (String) prefixes.nextElement();
      String nsUri = namespaceContext.getURI(prefix);
      prefixMap.put(nsUri, prefix);
    }
    return prefixMap;
  }

  private XSAttributeGroupDecl getAttributeGroup(XSElementDeclaration elDecl) {
    XSTypeDefinition type = elDecl.getTypeDefinition();
    if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
      XSComplexTypeDecl ctype = (XSComplexTypeDecl) type;
      return ctype.getAttrGrp();
    }
    return null;
  }

  private Set<String> getRequiredAttributes(XSAttributeGroupDecl attrGrp,
                                            Map<String, String> prefixMap) {

    Set<XSAttributeDecl> attrDecls = getUseAttrDecls(attrGrp, false);

    Set<String> attrStrings = new HashSet<String>();

    for (XSAttributeDecl attrDecl : attrDecls) {
      String str = getAttributeNameValue(attrDecl, prefixMap);
      attrStrings.add(str);
    }

    return attrStrings;
  }

  private void addWildcardComponents(XSWildcardDecl wildcardDecl, short componentType, Set<Object> expectedComponents,
                                     Set<String> nsUris, Map<String, String> prefixMap) {
    SchemaGrammar[] grammars = schemaValidator.getGrammarBucket().getGrammars();

    boolean isStrict = wildcardDecl.getProcessContents() == XSWildcard.PC_STRICT;


    if (wildcardDecl.getConstraintType() == XSWildcard.NSCONSTRAINT_LIST) {
      // only the listed nsUris...

      for (SchemaGrammar grammar : grammars) {
        XSModel model = grammar.toXSModel();
        StringList nsConstraintList = wildcardDecl.getNsConstraintList();
        int len = nsConstraintList.getLength();

        for (int i = 0; i < len; i++) {
          String nsUri = (String) nsConstraintList.get(i);
          XSNamedMap components = model.getComponentsByNamespace(componentType, nsUri);
          expectedComponents.addAll(components.values());
          nsUris.add(nsUri);
        }
      }

    } else if (wildcardDecl.getConstraintType() == XSWildcard.NSCONSTRAINT_NOT) {
      // all namespaces but...

      StringList excludedNamespaces = wildcardDecl.getNsConstraintList();

      for (SchemaGrammar grammar : grammars) {
        XSModel model = grammar.toXSModel();

        String grammarNamespace = grammar.getTargetNamespace();
        if (!excludedNamespaces.contains(grammarNamespace)) {
          XSNamedMap components = model.getComponentsByNamespace(componentType, grammarNamespace);
          expectedComponents.addAll(components.values());

          nsUris.add(grammarNamespace);
        }
      }


      if (!isStrict) {
        nsUris.add("*");
        Set<String> prefixMapNsUris = new HashSet<String>(prefixMap.keySet());
        if (componentType == XSConstants.ELEMENT_DECLARATION) {
          prefixMapNsUris.remove(XMLConstants.XML_NS_URI);
        }
        for (String nsUri : prefixMapNsUris) {
          if (!excludedNamespaces.contains(nsUri)) {
            nsUris.add(nsUri);
          }
        }
      }
    } else {
      // no namespace constraints...

      for (SchemaGrammar grammar : grammars) {
        XSModel model = grammar.toXSModel();

        String grammarNamespace = grammar.getTargetNamespace();

        XSNamedMap components = model.getComponents(componentType);
        Collection elDecl = components.values();
        expectedComponents.addAll(elDecl);

        nsUris.add(grammarNamespace);
      }

      if (componentType == XSConstants.ATTRIBUTE_DECLARATION) {
        nsUris.add(null);
      }

      if (!isStrict) {
        nsUris.add("*");
        Set<String> prefixMapNsUris = new HashSet<String>(prefixMap.keySet());
        if (componentType == XSConstants.ELEMENT_DECLARATION) {
          prefixMapNsUris.remove(XMLConstants.XML_NS_URI);
        }
        for (String nsUri : prefixMapNsUris) {
          nsUris.add(nsUri);
        }
      }
    }
  }

  private Set<XSAttributeDecl> getUseAttrDecls(XSAttributeGroupDecl attrGrp, boolean allAttributes) {
    XSObjectList attrUses = attrGrp.getAttributeUses();
    XSAttributeUseImpl currUse;
    XSAttributeDecl currDecl;

    Set<XSAttributeDecl> attrDecls = new HashSet<XSAttributeDecl>();

    int len = attrUses.getLength();
    for (int i = 0; i < len; i++) {
      currUse = (XSAttributeUseImpl) attrUses.item(i);
      currDecl = currUse.fAttrDecl;

      if (allAttributes || currUse.getRequired()) {
        attrDecls.add(currDecl);
      }
    }

    return attrDecls;
  }
}