package com.thaiopensource.suggest.xsd.impl;

import org.apache.xerces.impl.Constants;

class Features {
  private Features() { }
  static final String SCHEMA_AUGMENT_PSVI = Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_AUGMENT_PSVI;
  static final String SCHEMA_FULL_CHECKING = Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;
  static final String VALIDATION = Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
  static final String SCHEMA_VALIDATION = Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
  static final String ID_IDREF_CHECKING = Constants.XERCES_FEATURE_PREFIX + Constants.ID_IDREF_CHECKING_FEATURE;
  static final String IDC_CHECKING = Constants.XERCES_FEATURE_PREFIX + Constants.IDC_CHECKING_FEATURE;
}
