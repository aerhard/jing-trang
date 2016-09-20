package com.thaiopensource.suggest;

import java.util.List;

public class AttributeValueSuggestion implements Suggestion {
  private final String value;
  private final List<String> documentation;
  private final boolean listItem;

  public AttributeValueSuggestion(String value, List<String> documentation, boolean listItem) {
    this.value = value;
    this.documentation = documentation;
    this.listItem = listItem;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public List<String> getDocumentation() {
    return documentation;
  }

  public boolean isListItem() {
    return listItem;
  }
}
