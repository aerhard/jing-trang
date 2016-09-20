package com.thaiopensource.suggest.relaxng;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.suggest.relaxng.impl.SchemaReaderImpl;
import com.thaiopensource.suggest.relaxng.pattern.*;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.SchemaReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;

public class SaxSchemaReader extends SchemaReaderImpl {
  private static final SchemaReader theInstance = new SaxSchemaReader();

  private SaxSchemaReader() {
  }

  public static SchemaReader getInstance() {
    return theInstance;
  }

  protected Parseable<Pattern, NameClass, SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl, AnnotationsImpl> createParseable(SAXSource source, SAXResolver resolver, ErrorHandler eh, PropertyMap properties) throws SAXException {
    if (source.getXMLReader() == null)
      source = new SAXSource(resolver.createXMLReader(), source.getInputSource());
    return new SAXParseable<Pattern, NameClass, SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl, AnnotationsImpl>(source, resolver, eh);
  }
}
