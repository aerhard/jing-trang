package com.thaiopensource.suggest.relaxng.impl;

import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.TextAnnotation;

import java.util.ArrayList;
import java.util.List;

public class AnnotationSerializer {

  public static List<String> getAnnotationStrings(Annotated p) {
    if (p == null) return null;

    List<AnnotationChild> childAnnotations = p.getChildElementAnnotations();
    List<AnnotationChild> followingElementAnnotations = p.getFollowingElementAnnotations();

    if (!childAnnotations.isEmpty() || !followingElementAnnotations.isEmpty()) {
      List<String> annotations = new ArrayList<String>();

      StringBuilder sb = new StringBuilder();

      for (AnnotationChild a : childAnnotations) {
        if (a instanceof ElementAnnotation) {
          appendAnnotationString(sb, a);
          annotations.add(sb.toString().replaceAll("\\s+", " ").trim());
          sb.setLength(0);
        }
      }

      for (AnnotationChild a : followingElementAnnotations) {
        if (a instanceof ElementAnnotation) {
          appendAnnotationString(sb, a);
          annotations.add(sb.toString().replaceAll("\\s+", " ").trim());
          sb.setLength(0);
        }
      }

      return annotations;

    }

    return null;
  }

  static private void appendAnnotationString(StringBuilder sb, AnnotationChild a) {
    if (a instanceof ElementAnnotation) {
      ElementAnnotation e = (ElementAnnotation) a;

      for (AnnotationChild ch : e.getChildren()) {
        appendAnnotationString(sb, ch);
      }

    } else if (a instanceof TextAnnotation) {
      sb.append(((TextAnnotation) a).getValue());
    }
  }


}
