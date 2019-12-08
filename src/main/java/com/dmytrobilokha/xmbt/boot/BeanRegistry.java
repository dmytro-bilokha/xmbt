package com.dmytrobilokha.xmbt.boot;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@NotThreadSafe
class BeanRegistry {

    @Nonnull
    private final Map<Class, Object> servicesMap;

    BeanRegistry(@Nonnull Object... servicesSeed) {
        this.servicesMap = new HashMap<>();
        for (Object service : servicesSeed) {
            servicesMap.put(service.getClass(), service);
        }
    }

    void initServices(List<Class> serviceClasses) throws InitializationException {
        var classes = new HashSet<>(serviceClasses);
        boolean initialized;
        do {
            initialized = false;
            for (Iterator<Class> serviceClassIterator = classes.iterator(); serviceClassIterator.hasNext();) {
                Class serviceClass = serviceClassIterator.next();
                Object serviceBean = initBean(serviceClass);
                if (serviceBean != null) {
                    initialized = true;
                    servicesMap.put(serviceClass, serviceBean);
                    serviceClassIterator.remove();
                }
            }
        } while (!classes.isEmpty() && initialized);
        if (!classes.isEmpty()) {
            throw new InitializationException("Failed to initialize beans: " + classes
                    + ", because of dependency issues");
        }
        for (Class serviceClass : serviceClasses) {
            if (Initializable.class.isAssignableFrom(serviceClass)) {
                ((Initializable) servicesMap.get(serviceClass)).init();
            }
        }
    }

    @Nonnull
    <T> T getServiceBean(@Nonnull Class<T> serviceClass) throws InitializationException {
        T bean = (T) servicesMap.get(serviceClass);
        if (bean == null) {
            throw new InitializationException("Don't have initialized service bean of class " + serviceClass);
        }
        return bean;
    }

    @CheckForNull
    <T> T initBean(
            @Nonnull Class<T> beanClass) throws InitializationException {
        Constructor<T>[] beanConstructors = (Constructor<T>[]) beanClass.getConstructors();
        if (beanConstructors.length == 0) {
            throw new InitializationException("Couldn't find public constructor for " + beanClass);
        }
        if (beanConstructors.length > 1) {
            throw new InitializationException(
                    "The bean should have only one constructor, but find " + beanConstructors.length
                            + " for " + beanClass);
        }
        Constructor<T> beanConstructor = beanConstructors[0];
        Class[] constructorArgumentClasses = beanConstructor.getParameterTypes();
        Object[] constructorArguments = new Object[constructorArgumentClasses.length];
        for (int i = 0; i < constructorArgumentClasses.length; i++) {
            Object constructorArgument = servicesMap.get(constructorArgumentClasses[i]);
            if (constructorArgument == null) {
                return null;
            }
            constructorArguments[i] = constructorArgument;
        }
        try {
            return beanConstructor.newInstance(constructorArguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new InitializationException(
                    "Got unexpected exception during initializing bean " + beanClass, ex);
        }
    }

}
