package org.openmarkov.java.enumUtils;

public class EnumUtils {
    
    public static String toCamelCase(Enum<?> enumConstant) {
        var name = enumConstant.name();
        String camelCased = "";
        var shouldCapitalize = false;
        for (var character : name.toCharArray()) {
            if (character == '_') {
                shouldCapitalize = true;
                continue;
            }
            if (shouldCapitalize) {
                camelCased += Character.toUpperCase(character);
                shouldCapitalize = false;
            } else {
                camelCased += (Character.toLowerCase(character));
            }
        }
        return camelCased;
    }
    
    public static String toPascalCase(Enum<?> enumConstant) {
        var name = enumConstant.name();
        String pascalCased = "";
        var shouldCapitalize = true;
        for (var character : name.toCharArray()) {
            if (character == '_') {
                shouldCapitalize = true;
                continue;
            }
            if (shouldCapitalize) {
                pascalCased += Character.toUpperCase(character);
                shouldCapitalize = false;
            } else {
                pascalCased += Character.toLowerCase(character);
            }
        }
        return pascalCased;
    }
    
    public static String toTitleCase(Enum<?> enumConstant) {
        var name = enumConstant.name();
        String titleCased = "";
        var shouldCapitalize = true;
        for (var character : name.toCharArray()) {
            if (character == '_') {
                titleCased += " ";
                shouldCapitalize = true;
                continue;
            }
            if (shouldCapitalize) {
                titleCased += Character.toUpperCase(character);
                shouldCapitalize = false;
            } else {
                titleCased += Character.toLowerCase(character);
            }
        }
        return titleCased.toString();
    }
    
}
