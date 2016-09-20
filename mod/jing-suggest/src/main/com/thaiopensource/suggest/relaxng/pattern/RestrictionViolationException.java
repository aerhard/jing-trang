package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.xml.util.Name;

public class RestrictionViolationException extends Exception {
  private final String messageId;
  private SourceLocation loc;
  private Name name;
  private String namespaceUri;

  public RestrictionViolationException(String messageId) {
    this.messageId = messageId;
  }

  public RestrictionViolationException(String messageId, Name name) {
    this.messageId = messageId;
    this.name = name;
  }

  public RestrictionViolationException(String messageId, String namespaceUri) {
    this.messageId = messageId;
    this.namespaceUri = namespaceUri;
  }

  public String getMessageId() {
    return messageId;
  }

  public SourceLocation getLocator() {
    return loc;
  }

  void maybeSetLocator(SourceLocation loc) {
    if (this.loc == null)
      this.loc = loc;
  }

  public Name getName() {
    return name;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }
}
  
