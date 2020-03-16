package com.zhongwang.cloud.platform.service.code.exception;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaExceptionChecked {

    public static <T, R> Function<T, R> throwingFunctionWrapper(UncheckedFunction<T, R> mapper) {
        Objects.requireNonNull(mapper);
        return t -> {
            try {
                return mapper.apply(t);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static <T> Consumer<T> throwingConsumerWrapper(
            ThrowingConsumer<T, Exception> throwingConsumer) {

        return i -> {
            try {
                throwingConsumer.accept(i);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @FunctionalInterface
    public interface UncheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

}
