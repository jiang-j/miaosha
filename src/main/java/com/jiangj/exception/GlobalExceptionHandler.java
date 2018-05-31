package com.jiangj.exception;

import com.jiangj.result.CodeMsg;
import com.jiangj.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by jiangjian on 2018/4/26.
 */
@ResponseBody
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        if(e instanceof GlobalException) {
            GlobalException ex = (GlobalException)e;
            return Result.error(ex.getCm());
        }else if(e instanceof BindException){
            BindException bindException = (BindException) e;

            List<ObjectError> objectErrors = bindException.getAllErrors();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(objectErrors.get(0).getDefaultMessage()));
        }else {
            log.error(e.toString());
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }

}
