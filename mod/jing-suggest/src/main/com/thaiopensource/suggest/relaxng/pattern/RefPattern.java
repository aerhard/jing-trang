package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class RefPattern extends Pattern {
  private Pattern p;
  private SourceLocation refLoc;
  private final String name;
  private int checkRecursionDepth = -1;
  private boolean combineImplicit = false;
  private byte combineType = COMBINE_NONE;
  private byte replacementStatus = REPLACEMENT_KEEP;
  private boolean expanded = false;

  public static final byte REPLACEMENT_KEEP = 0;
  public static final byte REPLACEMENT_REQUIRE = 1;
  public static final byte REPLACEMENT_IGNORE = 2;

  public static final byte COMBINE_NONE = 0;
  public static final byte COMBINE_CHOICE = 1;
  public static final byte COMBINE_INTERLEAVE = 2;

  public RefPattern(String name) {
    this.name = name;
  }

  public Pattern getPattern() {
    return p;
  }
  
  public void setPattern(Pattern p) {
    this.p = p;
  }

  public SourceLocation getRefLocator() {
    return refLoc;
  }
  
  public void setRefLocator(SourceLocation loc) {
    this.refLoc = loc;
  }

  public void checkRecursion(int depth) throws SAXException {
    if (checkRecursionDepth == -1) {
      checkRecursionDepth = depth;
      p.checkRecursion(depth);
      checkRecursionDepth = -2;
    }
    else if (depth == checkRecursionDepth)
      // XXX try to recover from this?
      throw new SAXParseException(SchemaBuilderImpl.localizer.message("recursive_reference", name),
				  SchemaBuilderImpl.makeLocation(refLoc));
  }

  public Pattern expand(SchemaPatternBuilder b) {
    if (!expanded) {
      p = p.expand(b);
      expanded = true;
    }
    return p;
  }

  public boolean samePattern(Pattern other) {
    return false;
  }

  public <T> T apply(PatternFunction<T> f) {
    return f.caseRef(this);
  }

  public byte getReplacementStatus() {
    return replacementStatus;
  }

  public void setReplacementStatus(byte replacementStatus) {
    this.replacementStatus = replacementStatus;
  }

  public boolean isCombineImplicit() {
    return combineImplicit;
  }

  public void setCombineImplicit() {
    combineImplicit = true;
  }

  public byte getCombineType() {
    return combineType;
  }

  public void setCombineType(byte combineType) {
    this.combineType = combineType;
  }

  public String getName() {
    return name;
  }
}

