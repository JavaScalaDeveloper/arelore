package com.arelore.server.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 通用返回结果封装类
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, System.currentTimeMillis());
    }

    /**
     * 成功返回（带数据）
     *
     * @param data 数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }

    /**
     * 成功返回（自定义消息和数据）
     *
     * @param message 消息
     * @param data    数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }

    /**
     * 失败返回（默认错误）
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null, System.currentTimeMillis());
    }

    /**
     * 失败返回（自定义消息）
     *
     * @param message 消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null, System.currentTimeMillis());
    }

    /**
     * 失败返回（自定义状态码和消息）
     *
     * @param code    状态码
     * @param message 消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    /**
     * 失败返回（使用枚举）
     *
     * @param resultCode 结果码枚举
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null, System.currentTimeMillis());
    }

    /**
     * 失败返回（使用枚举Code + 自定义消息）
     *
     * @param resultCode 结果码枚举
     * @param message    自定义消息
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null, System.currentTimeMillis());
    }

    /**
     * 判断是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }

    /**
     * 构建器模式创建 Result
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder 模式
     */
    public static class Builder<T> {
        private Integer code = 200;
        private String message = "操作成功";
        private T data;
        private Long timestamp = System.currentTimeMillis();

        public Builder<T> code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Result<T> build() {
            return new Result<>(code, message, data, timestamp);
        }
    }
}
