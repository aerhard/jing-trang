package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.Annotated;
import org.xml.sax.SAXException;

public abstract class Pattern extends Annotated {
  private final boolean nullable;
  private final int hc;
  private final int contentType;

  static final int TEXT_HASH_CODE = 1;
  static final int ERROR_HASH_CODE = 3;
  static final int EMPTY_HASH_CODE = 5;
  static final int NOT_ALLOWED_HASH_CODE = 7;
  static final int CHOICE_HASH_CODE = 11;
  static final int GROUP_HASH_CODE = 13;
  static final int INTERLEAVE_HASH_CODE = 17;
  static final int ONE_OR_MORE_HASH_CODE = 19;
  static final int ELEMENT_HASH_CODE = 23;
  static final int VALUE_HASH_CODE = 27;
  static final int ATTRIBUTE_HASH_CODE = 29;
  static final int DATA_HASH_CODE = 31;
  static final int LIST_HASH_CODE = 37;
  static final int AFTER_HASH_CODE = 41;

  static int combineHashCode(int hc1, int hc2, int hc3) {
    return hc1 * hc2 * hc3;
  }

  static int combineHashCode(int hc1, int hc2) {
    return hc1 * hc2;
  }

  static final int EMPTY_CONTENT_TYPE = 0;
  static final int ELEMENT_CONTENT_TYPE = 1;
  static final int MIXED_CONTENT_TYPE = 2;
  static final int DATA_CONTENT_TYPE = 3;

  public Pattern(boolean nullable, int contentType, int hc) {
    this.nullable = nullable;
    this.contentType = contentType;
    this.hc = hc;
  }

  public Pattern() {
    this.nullable = false;
    this.hc = hashCode();
    this.contentType = EMPTY_CONTENT_TYPE;
  }

  public void checkRecursion(int depth) throws SAXException { }

  public Pattern expand(SchemaPatternBuilder b) {
    return this;
  }

  final boolean isNullable() {
    return nullable;
  }

  boolean isNotAllowed() {
    return false;
  }

  public static final int START_CONTEXT = 0;
  public static final int ELEMENT_CONTEXT = 1;
  public static final int ELEMENT_REPEAT_CONTEXT = 2;
  public static final int ELEMENT_REPEAT_GROUP_CONTEXT = 3;
  public static final int ELEMENT_REPEAT_INTERLEAVE_CONTEXT = 4;
  public static final int ATTRIBUTE_CONTEXT = 5;
  public static final int LIST_CONTEXT = 6;
  public static final int DATA_EXCEPT_CONTEXT = 7;

  public void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
  }

  // Know that other is not null
  abstract boolean samePattern(Pattern other);

  final int patternHashCode() {
    return hc;
  }

  final int getContentType() {
    return contentType;
  }

  boolean containsChoice(Pattern p) {
    return this == p;
  }

  abstract <T> T apply(PatternFunction<T> f);

  static boolean contentTypeGroupable(int ct1, int ct2) {
    if (ct1 == EMPTY_CONTENT_TYPE || ct2 == EMPTY_CONTENT_TYPE)
      return true;
    if (ct1 == DATA_CONTENT_TYPE || ct2 == DATA_CONTENT_TYPE)
      return false;
    return true;
  }

}