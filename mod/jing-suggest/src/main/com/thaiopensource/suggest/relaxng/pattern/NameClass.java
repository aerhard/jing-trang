package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.xml.util.Name;

public abstract class NameClass extends Annotated {
  static final int SPECIFICITY_NONE = -1;
  static final int SPECIFICITY_ANY_NAME = 0;
  static final int SPECIFICITY_NS_NAME = 1;
  static final int SPECIFICITY_NAME = 2;
  abstract boolean contains(Name name);
  abstract int containsSpecificity(Name name);
  abstract void accept(NameClassVisitor visitor);
  abstract boolean isOpen();
}
