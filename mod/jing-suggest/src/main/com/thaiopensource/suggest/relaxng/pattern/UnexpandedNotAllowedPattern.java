package com.thaiopensource.suggest.relaxng.pattern;

class UnexpandedNotAllowedPattern extends NotAllowedPattern {
  UnexpandedNotAllowedPattern() {
  }
  boolean isNotAllowed() {
    return false;
  }
  public Pattern expand(SchemaPatternBuilder b) {
    return b.makeNotAllowed();
  }
}
