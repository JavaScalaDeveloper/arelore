package com.arelore.android.prompt.data

/**
 * 与小程序 / user 服务约定对齐的环境预设。
 *
 * 模拟器访问电脑本机服务必须用 10.0.2.2（127.0.0.1 指向模拟器自己）。
 * 真机调试请选「自定义」填电脑局域网 IP，例如 http://192.168.1.8:8081/api
 */
enum class ServerEnv(
    val id: String,
    val label: String,
    val baseUrl: String,
    val hint: String
) {
    LOCAL(
        "local",
        "本机",
        "http://10.0.2.2:8081/api",
        "模拟器→电脑本机（10.0.2.2:8081）；真机请用自定义填局域网 IP"
    ),
    TEST(
        "test",
        "测试",
        "http://test.arelore.com:8281/api",
        "测试机 application-test.yml → 8281"
    ),
    PRD(
        "prd",
        "正式",
        "https://www.arelore.com/api",
        "线上 HTTPS，无端口"
    ),
    CUSTOM(
        "custom",
        "自定义",
        "",
        "真机填电脑局域网 IP，如 http://192.168.x.x:8081/api"
    );

    companion object {
        fun fromId(id: String?): ServerEnv =
            entries.firstOrNull { it.id == id } ?: LOCAL
    }
}
