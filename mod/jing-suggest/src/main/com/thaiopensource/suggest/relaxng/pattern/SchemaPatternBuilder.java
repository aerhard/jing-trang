package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;

import java.util.List;

public class SchemaPatternBuilder extends PatternBuilder {
  private boolean idTypes;
  private final UnexpandedNotAllowedPattern unexpandedNotAllowed = new UnexpandedNotAllowedPattern();
  private final TextPattern text = new TextPattern();
  private final PatternInterner schemaInterner = new PatternInterner();

  public SchemaPatternBuilder() { }

  public boolean hasIdTypes() {
    return idTypes;
  }

  public Pattern makeElement(NameClass nameClass, Pattern content, SourceLocation loc) {
    Pattern p = new ElementPattern(nameClass, content, loc);
    return schemaInterner.intern(p);
  }

  public Pattern makeAttribute(NameClass nameClass, Pattern value, SourceLocation loc) {
    if (value == notAllowed)
      return value;
    Pattern p = new AttributePattern(nameClass, value, loc);
    return schemaInterner.intern(p);
  }

  public Pattern makeData(Datatype dt, Name dtName, List<String> params) {
    noteDatatype(dt);
    Pattern p = new DataPattern(dt, dtName, params);
    return schemaInterner.intern(p);
  }

  public Pattern makeDataExcept(Datatype dt, Name dtName, List<String> params, Pattern except, SourceLocation loc) {
    noteDatatype(dt);
    Pattern p = new DataExceptPattern(dt, dtName, params, except, loc);
    return schemaInterner.intern(p);
  }

  public Pattern makeValue(Datatype dt, Name dtName, Object value, String stringValue) {
    noteDatatype(dt);
    Pattern p = new ValuePattern(dt, dtName, value, stringValue);
    return schemaInterner.intern(p);
  }

  public Pattern makeText() {
    return text;
  }

  public Pattern makeOneOrMore(Pattern p) {
    if (p == text)
      return p;
    return super.makeOneOrMore(p);
  }

  public Pattern makeUnexpandedNotAllowed() {
    return unexpandedNotAllowed;
  }

  public Pattern makeError() {
    Pattern p = new ErrorPattern();
    return schemaInterner.intern(p);
  }

  public Pattern makeChoice(Pattern p1, Pattern p2) {
    if (p1 == notAllowed || p1 == p2)
      return p2;
    if (p2 == notAllowed)
      return p1;
    return super.makeChoice(p1, p2);
  }

  public Pattern makeList(Pattern p, SourceLocation loc) {
    if (p == notAllowed)
      return p;
    Pattern p1 = new ListPattern(p, loc);
    return schemaInterner.intern(p1);
  }

  public Pattern makeMixed(Pattern p) {
    return makeInterleave(text, p);
  }

  private void noteDatatype(Datatype dt) {
    if (dt.getIdType() != Datatype.ID_TYPE_NULL)
      idTypes = true;
  }
}
