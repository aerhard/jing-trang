package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.*;
import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.Context;

import java.util.List;
import java.util.Vector;

public class AnnotationsImpl implements Annotations<SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl> {
  private final List<AttributeAnnotation> attributes = new Vector<AttributeAnnotation>();
  private final List<AnnotationChild> elements = new Vector<AnnotationChild>();
  private final Context context;

  public AnnotationsImpl(Context context) {
    this.context = context;
  }

  public void addAttribute(String ns, String localName, String prefix, String value, SourceLocation loc)
      throws BuildException {
    AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
    att.setPrefix(prefix);
    att.setSourceLocation(loc);
    attributes.add(att);
  }

  public void addElement(ElementAnnotationBuilderImpl ea) throws BuildException {
    ea.addDocumentationTo(elements);
  }

  public void addComment(CommentListImpl comments) throws BuildException {
  }

  public void addLeadingComment(CommentListImpl comments) throws BuildException {
  }

  public void apply(Annotated subject) {
    subject.setContext(new NamespaceContextImpl(context));
    subject.getAttributeAnnotations().addAll(attributes);

    if (subject.mayContainText())
      subject.getFollowingElementAnnotations().addAll(elements);
    else
      subject.getChildElementAnnotations().addAll(elements);
  }
}

