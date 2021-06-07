package de.rexlmanu.smash.utility.reflection;

import org.bukkit.event.Listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConstructorUtils {

    public static Listener newInstanceOf(Class<?> clazz) {
        Constructor<?> declaredConstructor = clazz.getDeclaredConstructors()[0];
        declaredConstructor.setAccessible(true);
        Object o = null;
        try {
            o = declaredConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return (Listener) o;
    }

}
