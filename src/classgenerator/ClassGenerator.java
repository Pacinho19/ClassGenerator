/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classgenerator;

import classgenerator.exceptions.DestincationPathNotExistExeption;
import classgenerator.exceptions.FileExistExeption;
import classgenerator.exceptions.UndefinedColumnTypeExeption;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

/**
 *
 * @author pojdana
 */
public class ClassGenerator {

    private static HashMap<Integer, String> columnTypes = new HashMap<Integer, String>() {
        {
            put(2004, "String");
            put(16, "Boolean");
            put(91, "Date");
            put(8, "Double");
            put(4, "Integer");
            put(92, "Time");
            put(93, "Timestamp");
            put(5, "int");
            put(-9, "String");
            put(12, "String");
        }
    };

    public static String generateCode(String packageName,
            String className,
            ResultSetMetaData resultSetMetaData,
            boolean getters,
            boolean setters,
            String srcPath) throws Exception {
        StringBuilder code = new StringBuilder(classStart(className));
        StringBuilder imports = new StringBuilder("");
        StringBuilder gettersSB = new StringBuilder("");
        StringBuilder settersSB = new StringBuilder("");
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            String columnType = columnTypes.get(resultSetMetaData.getColumnType(i));
            if (columnType == null) {
                throw new UndefinedColumnTypeExeption("Undefined column type");
            }
            String columnName = resultSetMetaData.getColumnLabel(i);
            code.append("   private ").append(columnType).append(" ").append(columnName).append(";").append(System.lineSeparator());
            if (columnType.equals("Date") || columnType.equals("Time") || columnType.equals("Timestamp")) {
                imports.append("import java.sql.").append(columnType).append(";").append(System.lineSeparator());
            }
            if (getters) {
                gettersSB.append(generateGetter(columnType, columnName));
            }
            if (setters) {
                settersSB.append(generateSetter(columnType, columnName));
            }
        }
        if (!imports.toString().isEmpty()) {
            imports.append("\n");
        }
        if (getters) {
            code.append("\n");
            code.append(gettersSB.toString());
        }
        if (setters) {
            if (!getters) {
                code.append("\n");
            }
            code.append(settersSB.toString());
        }
        code.append("}");
        String outCode = (packageName != null ? "package " + packageName + ";" + System.lineSeparator() + System.lineSeparator() : "") + imports.toString() + code.toString();
        if (srcPath != null) {
            saveFile(outCode, srcPath, packageName, className);
        }
        return outCode;
    }

    private static String classStart(String name) {
        return "public class " + name + "{" + System.lineSeparator() + System.lineSeparator();
    }

    private static String generateGetter(String columnType, String columnName) {
        StringBuilder getter = new StringBuilder("   public " + columnType + " get" + transformColumnName(columnName) + "(){" + System.lineSeparator());
        getter.append("      return ").append(columnName).append(";").append(System.lineSeparator());
        getter.append("   }").append(System.lineSeparator()).append(System.lineSeparator());
        return getter.toString();
    }

    private static String generateSetter(String columnType, String columnName) {
        StringBuilder getter = new StringBuilder("   public void set" + transformColumnName(columnName) + "(" + columnType + " " + columnName + "){" + System.lineSeparator());
        getter.append("      this.").append(columnName).append(" = ").append(columnName).append(";").append(System.lineSeparator());
        getter.append("   }").append(System.lineSeparator()).append(System.lineSeparator());
        return getter.toString();
    }

    private static String transformColumnName(String columnName) {
        return columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
    }

    private static void saveFile(String outCode, String srcPath, String packageName, String fileName) throws FileExistExeption, DestincationPathNotExistExeption {
        String path = checksrcPath(srcPath) + packageName.replaceAll("\\.", "\\\\") + "\\";
        String fullPath = path + fileName + ".java";;
        if (new File(fullPath).exists()) {
            throw new FileExistExeption("In destination folder " + path + " just exist " + fileName + " class!");
        }

        if (!new File(path).exists()) {
            throw new DestincationPathNotExistExeption("Destication location " + path + " not exist !");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fullPath),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            writer.write(outCode);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private static String checksrcPath(String srcPath) {
        if (!srcPath.endsWith("\\")) {
            srcPath += "\\";
        }
        return srcPath;
    }

}
