package com.pradeep.crudgenerator.utils;

public class CrudStringUtils {

    public static String convertCamelToSnake(String input) {
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

    public static String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String lowerCaseFirstLetter(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }
}
