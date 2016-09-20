package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.relaxng.edit.NamespaceContext;
import com.thaiopensource.relaxng.parse.Context;

import java.util.Collections;
import java.util.Set;

public class NamespaceContextImpl implements NamespaceContext {
  private final Context context;
  private Set<String> cachedPrefixes = null;

  public NamespaceContextImpl(Context context) {
    this.context = context.copy();
  }

  public String getNamespace(String prefix) {
    return context.resolveNamespacePrefix(prefix);
  }

  public Set<String> getPrefixes() {
    if (cachedPrefixes == null)
      cachedPrefixes = Collections.unmodifiableSet(context.prefixes());
    return cachedPrefixes;
  }
}
