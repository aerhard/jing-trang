package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.*;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.ElementAnnotationBuilder;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.List;

public class ElementAnnotationBuilderImpl implements ElementAnnotationBuilder<SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl> {
  private final ElementAnnotation element;

  public ElementAnnotationBuilderImpl(ElementAnnotation element) {
    this.element = element;
  }

  public void addText(String value, SourceLocation loc, CommentListImpl comments) throws BuildException {
    TextAnnotation t = new TextAnnotation(value);
    t.setSourceLocation(loc);
    element.getChildren().add(t);
  }

  public void addAttribute(String ns, String localName, String prefix, String value, SourceLocation loc)
          throws BuildException {
    AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
    att.setPrefix(prefix);
    att.setSourceLocation(loc);
    element.getAttributes().add(att);
  }

  public ElementAnnotationBuilderImpl makeElementAnnotation() throws BuildException {
    return this;
  }

  public void addElement(ElementAnnotationBuilderImpl ea) throws BuildException {
    ea.addTo(element.getChildren());
  }

  public void addComment(CommentListImpl comments) throws BuildException {
  }

  public void addLeadingComment(CommentListImpl comments) throws BuildException {
  }

  public void addTo(List<AnnotationChild> elementList) {
    elementList.add(element);
  }

  public void addDocumentationTo(List<AnnotationChild> elementList) {
    if (WellKnownNamespaces.RELAX_NG_COMPATIBILITY_ANNOTATIONS.equals(element.getNamespaceUri())
        && "documentation".equals(element.getLocalName())) {
      elementList.add(element);
    }

  }
}
