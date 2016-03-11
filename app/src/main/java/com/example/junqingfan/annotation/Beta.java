package com.example.junqingfan.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by junqing.fan on 2016/3/11.
 * <p>
 * Java中提供了四种元注解，专门负责注解其他的注解，分别如下
 *
 * @Retention元注解，表示需要在什么级别保存该注释信息（生命周期）。 可选的RetentionPoicy参数包括：
 * RetentionPolicy.SOURCE: 停留在java源文件，编译器被丢掉
 * RetentionPolicy.CLASS：停留在class文件中，但会被VM丢弃（默认）
 * RetentionPolicy.RUNTIME：内存中的字节码，VM将在运行时也保留注解，因此可以通过反射机制读取注解的信息
 * @Target元注解，默认值为任何元素，表示该注解用于什么地方。 可用的ElementType参数包括
 * ElementType.ANNOTATION_TYPE 注解类型声明
 * ElementType.CONSTRUCTOR: 构造器声明
 * ElementType.FIELD: 成员变量、对象、属性（包括enum实例）
 * ElementType.LOCAL_VARIABLE: 局部变量声明
 * ElementType.METHOD: 方法声明
 * ElementType.PACKAGE: 包声明
 * ElementType.PARAMETER: 参数声明
 * ElementType.TYPE: 类、接口（包括注解类型)或enum声明
 * @Documented将注解包含在JavaDoc中
 * @Inheried允许子类继承父类中的注解 指示注释类型被自动继承。如果在注释类型声明中存在 Inherited 元注释，
 * 并且用户在某一类声明中查询该注释类型，同时该类声明中没有此类型的注释，则将在该类的超类中自动查询该注释类型。
 * 此过程会重复进行，直到找到此类型的注释或到达了该类层次结构的顶层 (Object) 为止。如果没有超类具有该类型的注释，
 * 则查询将指示当前类没有这样的注释。
 * 注意，如果使用注释类型注释类以外的任何事物，此元注释类型都是无效的。
 * 还要注意，此元注释仅促成从超类继承注释；对已实现接口的注释无效。
 * @Repeatable （Java8中增加）使用此注解注释的注解，在使用时是可重复使用的。
 * 注意，在Java8之前注解同一个注解在同一个元素上是不可以多次使用的。
 * <p>
 * Java提供了两种方式来处理注解：
 * 第一种是利用运行时反射机制；另一种是使用Java提供的API来处理编译期的注解。
 */

@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE
})
@Documented
@Beta
public @interface Beta {
}
