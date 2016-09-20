package com.thaiopensource.suggest.relaxng.pattern;

import java.util.Set;

public class NormalizedSuggestions {
  private final boolean anyNameIncluded;
  private final Set<NameSuggestion> includedNames;
  private final Set<NamespaceSuggestion> includedNamespaces;
  private final Set<NamespaceSuggestion> excludedNamespaces;
  private final Set<NameSuggestion> excludedNames;

  public NormalizedSuggestions(boolean anyNameIncluded, Set<NameSuggestion> includedNames, Set<NamespaceSuggestion> includedNamespaces, Set<NameSuggestion> excludedNames, Set<NamespaceSuggestion> excludedNamespaces) {
    this.anyNameIncluded = anyNameIncluded;
    this.includedNames = includedNames;
    this.includedNamespaces = includedNamespaces;
    this.excludedNamespaces = excludedNamespaces;
    this.excludedNames = excludedNames;
  }

  public NormalizedSuggestions(boolean anyNameIncluded, Set<NameSuggestion> includedNames, Set<NamespaceSuggestion> includedNamespaces) {
    this(anyNameIncluded, includedNames, includedNamespaces, null, null);
  }

  public boolean isAnyNameIncluded() {
    return anyNameIncluded;
  }

  public boolean hasNamedInclusions() {
    return !includedNames.isEmpty() || !includedNamespaces.isEmpty();
  }

  public Set<NameSuggestion> getIncludedNames() {
    return includedNames;
  }

  public Set<NamespaceSuggestion> getIncludedNamespaces() {
    return includedNamespaces;
  }

  public Set<NameSuggestion> getExcludedNames() {
    return excludedNames;
  }

  public Set<NamespaceSuggestion> getExcludedNamespaces() {
    return excludedNamespaces;
  }
}
