package com.thaiopensource.relaxng.util;

import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.OptionParser;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.Version;
import com.thaiopensource.validate.Flag;
import com.thaiopensource.validate.FlagOption;
import com.thaiopensource.validate.OptionArgumentException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.StringOption;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Driver {
  static private String usageKey = "usage";

  static public void setUsageKey(String key) {
    usageKey = key;
  }

  static public void main(String[] args) {
    System.exit(new Driver().doMain(args));
  }

  private boolean timing = false;
  private String encoding = null;
  private Localizer localizer = new Localizer(Driver.class);
  private boolean systemIn = false;

  public int doMain(String[] args) {
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    OptionParser op = new OptionParser("itcdfe:p:sC:S", args);
    PropertyMapBuilder properties = new PropertyMapBuilder();
    properties.put(ValidateProperty.ERROR_HANDLER, eh);
    RngProperty.CHECK_ID_IDREF.add(properties);
    SchemaReader sr = null;
    boolean compact = false;
    boolean outputSimplifiedSchema = false;
    List<String> catalogUris = new ArrayList<String>();

    try {
      while (op.moveToNextOption()) {
        switch (op.getOptionChar()) {
          case 'i':
            properties.put(RngProperty.CHECK_ID_IDREF, null);
            break;
          case 'C':
            catalogUris.add(UriOrFile.toUri(op.getOptionArg()));
            break;
          case 'c':
            compact = true;
            break;
          case 'd': {
            if (sr == null)
              sr = new AutoSchemaReader();
            FlagOption option = (FlagOption) sr.getOption(SchemaReader.BASE_URI + "diagnose");
            if (option == null) {
              eh.print(localizer.message("no_schematron", op.getOptionCharString()));
              return 2;
            }
            properties.put(option.getPropertyId(), Flag.PRESENT);
          }
          break;
          case 't':
            timing = true;
            break;
          case 'e':
            encoding = op.getOptionArg();
            break;
          case 'f':
            RngProperty.FEASIBLE.add(properties);
            break;
          case 's':
            outputSimplifiedSchema = true;
            break;
          case 'S':
            systemIn = true;
            break;
          case 'p': {
            if (sr == null)
              sr = new AutoSchemaReader();
            StringOption option = (StringOption) sr.getOption(SchemaReader.BASE_URI + "phase");
            if (option == null) {
              eh.print(localizer.message("no_schematron", op.getOptionCharString()));
              return 2;
            }
            try {
              properties.put(option.getPropertyId(), option.valueOf(op.getOptionArg()));
            } catch (OptionArgumentException e) {
              eh.print(localizer.message("invalid_phase", op.getOptionArg()));
              return 2;
            }
          }
          break;
        }
      }
    } catch (OptionParser.InvalidOptionException e) {
      eh.print(localizer.message("invalid_option", op.getOptionCharString()));
      return 2;
    } catch (OptionParser.MissingArgumentException e) {
      eh.print(localizer.message("option_missing_argument", op.getOptionCharString()));
      return 2;
    }
    if (!catalogUris.isEmpty()) {
      try {
        properties.put(ValidateProperty.RESOLVER, new CatalogResolver(catalogUris));
      } catch (LinkageError e) {
        eh.print(localizer.message("resolver_not_found"));
        return 2;
      }
    }
    if (compact)
      sr = CompactSchemaReader.getInstance();
    args = op.getRemainingArgs();
    if (args.length < 1) {
      eh.print(localizer.message(usageKey, Version.getVersion(Driver.class)));
      return 2;
    }
    long startTime = System.currentTimeMillis();
    long loadedPatternTime = -1;
    boolean hadError = false;
    try {
      ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), sr);
      InputSource in = ValidationDriver.uriOrFileInputSource(args[0]);
      if (encoding != null)
        in.setEncoding(encoding);
      if (driver.loadSchema(in)) {
        loadedPatternTime = System.currentTimeMillis();
        if (outputSimplifiedSchema) {
          String simplifiedSchema = driver.getSchemaProperties().get(RngProperty.SIMPLIFIED_SCHEMA);
          if (simplifiedSchema == null) {
            eh.print(localizer.message("no_simplified_schema"));
            hadError = true;
          } else
            System.out.print(simplifiedSchema);
        }
        if (systemIn) {
          InputSource xmlIn = new InputSource(System.in);
          if (args.length == 2) xmlIn.setSystemId(args[1]);
          if (!driver.validate(xmlIn)) hadError = true;
        } else {
          for (int i = 1; i < args.length; i++) {
            if (!driver.validate(ValidationDriver.uriOrFileInputSource(args[i])))
              hadError = true;
          }
        }
      } else
        hadError = true;
    } catch (SAXException e) {
      hadError = true;
      eh.printException(e);
    } catch (IOException e) {
      hadError = true;
      eh.printException(e);
    }
    if (timing) {
      long endTime = System.currentTimeMillis();
      if (loadedPatternTime < 0)
        loadedPatternTime = endTime;
      eh.print(localizer.message("elapsed_time",
          new Object[]{
              loadedPatternTime - startTime,
              endTime - loadedPatternTime,
              endTime - startTime
          }));
    }
    if (hadError)
      return 1;
    return 0;
  }

}
