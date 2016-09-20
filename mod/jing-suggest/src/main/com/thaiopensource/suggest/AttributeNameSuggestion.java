package com.thaiopensource.suggest;

import java.util.List;

public class AttributeNameSuggestion implements Suggestion {
  private final String value;
  private final List<String> documentation;

  public AttributeNameSuggestion(String value, List<String> documentation) {
    this.value = value;
    this.documentation = documentation;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public List<String> getDocumentation() {
    return documentation;
  }

}
