/*
 * Copyright (C) 2019 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: https://www.xiaominfo.com.
 * Developer Web Site: https://doc.xiaominfo.com.
 */
package com.come2future.boot.swagger.dubbo.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiOperationExpand {

  /**
   * sort
   *
   * @return
   */
  int order() default 0;


  /**
   * developer
   *
   * @return
   */
  String author() default "";

}
