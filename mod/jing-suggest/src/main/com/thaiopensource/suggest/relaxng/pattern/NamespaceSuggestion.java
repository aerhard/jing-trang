package com.thaiopensource.suggest.relaxng.pattern;

public class NamespaceSuggestion {
  private final String ns;
  private final NameClass nc;
  private final Pattern p;

  public NamespaceSuggestion(String ns, NameClass nc, Pattern p) {
    this.ns = ns;
    this.nc = nc;
    this.p = p;
  }

  public String getNamespace() {
    return ns;
  }

  public NameClass getNameClass() {
    return nc;
  }

  public Pattern getPattern() {
    return p;
  }
}