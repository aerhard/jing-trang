package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

public class NameSuggestion {
  private final Name name;
  private final NameClass nc;
  private final Pattern p;

  public NameSuggestion(Name name, NameClass nc, Pattern p) {
    this.name = name;
    this.nc = nc;
    this.p = p;
  }

  public Name getName() {
    return name;
  }

  public NameClass getNameClass() {
    return nc;
  }

  public Pattern getPattern() {
    return p;
  }
}