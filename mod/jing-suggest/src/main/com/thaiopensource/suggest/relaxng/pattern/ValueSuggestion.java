package com.thaiopensource.suggest.relaxng.pattern;

public class ValueSuggestion {
  private final ValuePattern p;
  private final boolean inList;

  public ValueSuggestion(ValuePattern p, boolean inList) {
    this.p = p;
    this.inList = inList;
  }

  public ValuePattern getPattern() {
    return p;
  }

  public boolean isInList() {
    return inList;
  }
}