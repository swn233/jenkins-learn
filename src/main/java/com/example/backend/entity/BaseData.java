package com.example.backend.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

//对象转换工具，快速将dto转换成vo
public interface BaseData {

    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer) {
        V v=this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }
    default <V> V asViewObject(Class<V> clazz) {
        try {
            //获取vo中的fields并创建新的vo实例
            Field[] declaredFields = clazz.getDeclaredFields();
            //获取vo构造器
            Constructor<V> constructor = clazz.getConstructor();
            V v = constructor.newInstance();
            //对于vo中的每一个属性，在当前dto如果存在则赋值给vo
            for (Field declaredField : declaredFields) {
                convert(declaredField,v);
            }
            return v;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

    }

    //@field:vo中的一个字段
    //@vo:创建的vo实例
    private void convert(Field field,Object vo){
        try {
            // 获取当前DTO类中与VO中存在的属性同名的字段
            Field source = this.getClass().getDeclaredField(field.getName());

            // 设置字段可访问（即使是私有字段）
            field.setAccessible(true);
            source.setAccessible(true);

            // 从当前DTO对象中获取对应字段的值，并将其设置到VO对象中的相应字段中
            field.set(vo, source.get(this));
        } catch (NoSuchFieldException e) {
            // 如果字段不存在，则抛出运行时异常
            System.out.println(e);
//            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            // 如果访问字段时发生非法访问异常，则抛出运行时异常
//            throw new RuntimeException(e);
            System.out.println(e);
        }
    }
    }

