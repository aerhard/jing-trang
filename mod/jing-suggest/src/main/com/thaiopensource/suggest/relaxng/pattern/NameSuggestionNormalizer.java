package com.thaiopensource.suggest.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.*;

public class NameSuggestionNormalizer {
  private static final String IMPOSSIBLE = "\u0000";

  public List<Pattern> patterns = new ArrayList<Pattern>();
  public List<NameClass> nameClasses = new ArrayList<NameClass>();

  private boolean nameClassesContain(Name name) {
    for (NameClass nc : nameClasses) {
      if (nc.contains(name)) return true;
    }
    return false;
  }

  public NormalizedSuggestions normalize() {
    List<NameSuggestion> mentionedNames = new ArrayList<NameSuggestion>();
    List<NamespaceSuggestion> namespaceSuggestions = new ArrayList<NamespaceSuggestion>();

    NameSuggestionVisitor nameSuggestionVisitor = new NameSuggestionVisitor(mentionedNames, namespaceSuggestions);

    for (Pattern p : patterns) {
      if (p instanceof ElementPattern) {
        nameSuggestionVisitor.start(p, ((ElementPattern) p).getNameClass());
      } else if (p instanceof AttributePattern) {
        nameSuggestionVisitor.start(p, ((AttributePattern) p).getNameClass());
      }
    }

    if (nameClassesContain(new Name(IMPOSSIBLE, IMPOSSIBLE))) {
      Set<NameSuggestion> includedNames = new HashSet<NameSuggestion>();
      Set<NamespaceSuggestion> excludedNamespaces = new HashSet<NamespaceSuggestion>();
      Set<NameSuggestion> excludedNames = new HashSet<NameSuggestion>();
      Set<NamespaceSuggestion> includedNamespaces = new HashSet<NamespaceSuggestion>();

      for (NamespaceSuggestion mns : namespaceSuggestions) {
        if (!nameClassesContain(new Name(mns.getNamespace(), IMPOSSIBLE))) {
          excludedNamespaces.add(mns);
        }

        String ns = mns.getNamespace();
        if (nameClassesContain(new Name(ns, IMPOSSIBLE))) {
          includedNamespaces.add(mns);
        }
      }

      for (NameSuggestion mns : mentionedNames) {
        Name name = mns.getName();
        boolean in = nameClassesContain(name);
        if (in) {
          includedNames.add(mns);
        } else {
          excludedNames.add(mns);
        }
      }
      return new NormalizedSuggestions(true, includedNames, includedNamespaces, excludedNames, excludedNamespaces);
    }

    Set<NamespaceSuggestion> includedNamespaces = new HashSet<NamespaceSuggestion>();
    Map<String, HashSet<String>> nsMap = new HashMap<String, HashSet<String>>();
    for (NamespaceSuggestion mns : namespaceSuggestions) {
      String ns = mns.getNamespace();
      if (nameClassesContain(new Name(ns, IMPOSSIBLE)) && nsMap.get(ns) == null) {
        nsMap.put(ns, new HashSet<String>());
        includedNamespaces.add(mns);
      }
    }

    Set<NameSuggestion> includedNames = new HashSet<NameSuggestion>();
    for (NameSuggestion mn : mentionedNames) {
      Name name = mn.getName();
      boolean in = nameClassesContain(name);
      Set<String> excluded = nsMap.get(name.getNamespaceUri());
      if (excluded == null) {
        if (in) {
          includedNames.add(mn);
        }
      } else if (!in) {
        excluded.add(name.getLocalName());
      }
    }
    return new NormalizedSuggestions(false, includedNames, includedNamespaces);
  }

  public void add(Pattern p, NameClass nameClass) {
    patterns.add(p);
    nameClasses.add(nameClass);
  }
}
