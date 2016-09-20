package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

public class ErrorNameClass extends NameClass {
  public boolean contains(Name name) {
    return false;
  }

  public int containsSpecificity(Name name) {
    return SPECIFICITY_NONE;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitError();
  }

  public boolean isOpen() {
    return false;
  }
}
