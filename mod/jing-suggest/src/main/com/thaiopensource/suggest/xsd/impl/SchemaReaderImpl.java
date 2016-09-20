package com.thaiopensource.suggest.xsd.impl;

import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.*;
import com.thaiopensource.validate.prop.wrap.WrapProperty;
import com.thaiopensource.xml.util.Name;
import org.apache.xerces.parsers.CachingParserPool;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.EntityResolverWrapper;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.SynchronizedSymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import static com.thaiopensource.validate.ValidateProperty.RESOLVER;

public class SchemaReaderImpl extends AbstractSchemaReader {
  private static final PropertyId<?>[] supportedPropertyIds = {
      ValidateProperty.ERROR_HANDLER,
      ValidateProperty.ENTITY_RESOLVER,
  };

  public Schema createSchema(Set<SAXSource> sources, PropertyMap properties)
      throws IOException, SAXException, IncorrectSchemaException {
    SymbolTable symbolTable = new SymbolTable();
    XMLGrammarPreparser preparser = new XMLGrammarPreparser(symbolTable);
    XMLGrammarPool grammarPool = new XMLGrammarPoolImpl();
    preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
    preparser.setGrammarPool(grammarPool);
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    SaxXmlErrorHandler xeh = new SaxXmlErrorHandler(eh);
    preparser.setErrorHandler(xeh);

    EntityResolver er = properties.get(ValidateProperty.ENTITY_RESOLVER);
    if (er == null && properties.contains(RESOLVER)) {
      CatalogResolver cr = (CatalogResolver) properties.get(RESOLVER);
      er = SAX.createEntityResolver(cr);
    }

    if (er != null)
      preparser.setEntityResolver(new EntityResolverWrapper(er));
    try {
      for (SAXSource source : sources) {
        if (source.getInputSource() != null) {
          preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA,
              toXMLInputSource(source.getInputSource()));
        }
      }
      Name attributeOwner = properties.get(WrapProperty.ATTRIBUTE_OWNER);
      if (attributeOwner != null) {
        Reader r = new StringReader(createWrapper(attributeOwner));
        preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA,
            new XMLInputSource(null, null, null, r, null));
      }
    } catch (XNIException e) {
      throw SuggesterImpl.toSAXException(e);
    }
    if (xeh.getHadError())
      throw new IncorrectSchemaException();
    return new SchemaImpl(new SynchronizedSymbolTable(symbolTable),
        new CachingParserPool.SynchronizedGrammarPool(grammarPool),
        properties,
        supportedPropertyIds);
  }

  public Schema createSchema(SAXSource source, PropertyMap properties)
      throws IOException, SAXException, IncorrectSchemaException {
    Set sources = new HashSet<SAXSource>();
    sources.add(source);
    return createSchema(sources, properties);
  }

  public Option getOption(String uri) {
    return null;
  }

  static private String createWrapper(Name attributeOwner) {
    return "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" +
        "    targetNamespace=\"" + attributeOwner.getNamespaceUri() + "\">" +
        "  <xsd:element name=\"" + attributeOwner.getLocalName() + "\">" +
        "    <xsd:complexType><xsd:anyAttribute processContents=\"strict\"/></xsd:complexType>" +
        "  </xsd:element>" +
        "</xsd:schema>";
  }

  private static XMLInputSource toXMLInputSource(InputSource in) {
    XMLInputSource xin = new XMLInputSource(in.getPublicId(), in.getSystemId(), null);
    xin.setByteStream(in.getByteStream());
    xin.setCharacterStream(in.getCharacterStream());
    xin.setEncoding(in.getEncoding());
    return xin;
  }
}
