package com.thaiopensource.suggest.xsd.impl;

import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.SchemaReaderFactory;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.apache.xerces.parsers.XMLGrammarPreparser;

public class XsdSchemaReaderFactory implements SchemaReaderFactory {
  public XsdSchemaReaderFactory() {
    // Force a linkage error if Xerces is not available
    new XMLGrammarPreparser();
  }

  public SchemaReaderImpl createSchemaReader(String namespaceUri) {
    if (WellKnownNamespaces.XML_SCHEMA.equals(namespaceUri))
      return new SchemaReaderImpl();
    return null;
  }

  public Option getOption(String uri) {
    return null;
  }
}
