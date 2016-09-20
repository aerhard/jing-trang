package com.thaiopensource.suggest.relaxng.pattern;


import com.thaiopensource.xml.util.Name;

import java.util.List;

public class NameSuggestionVisitor implements NameClassVisitor {
  List<NameSuggestion> mentionedNames;
  List<NamespaceSuggestion> namespaceSuggestions;
  private Pattern currentPattern;
  private NameClass currentNameClass;

  public NameSuggestionVisitor(List<NameSuggestion> mentionedNames, List<NamespaceSuggestion> namespaceSuggestions) {
    this.mentionedNames = mentionedNames;
    this.namespaceSuggestions = namespaceSuggestions;
  }

  public void start(Pattern p, NameClass nameClass) {
    currentPattern = p;
    currentNameClass = nameClass;
    currentNameClass.accept(this);
  }

  public void visitName(Name name) {
    mentionedNames.add(new NameSuggestion(name, currentNameClass, currentPattern));
  }

  public void visitNsName(String ns) {
    namespaceSuggestions.add(new NamespaceSuggestion(ns, currentNameClass, currentPattern));
  }

  public void visitNsNameExcept(String ns, NameClass nc) {
    namespaceSuggestions.add(new NamespaceSuggestion(ns, currentNameClass, currentPattern));
    NameClass parentNameClass = currentNameClass;
    currentNameClass = nc;
    nc.accept(this);
    currentNameClass = parentNameClass;
  }

  public void visitChoice(NameClass nc1, NameClass nc2) {
    NameClass parentNameClass = currentNameClass;
    currentNameClass = nc1;
    nc1.accept(this);
    currentNameClass = nc2;
    nc2.accept(this);
    currentNameClass = parentNameClass;
  }

  public void visitAnyNameExcept(NameClass nc) {
    NameClass parentNameClass = currentNameClass;
    currentNameClass = nc;
    nc.accept(this);
    currentNameClass = parentNameClass;
  }

  public void visitAnyName() {
  }

  public void visitNull() {
  }

  public void visitError() {
  }
}