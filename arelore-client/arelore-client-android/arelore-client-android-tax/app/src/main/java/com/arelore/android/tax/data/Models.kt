package com.arelore.android.tax.data

import java.util.UUID

data class IncomeRecord(
    val id: String,
    val year: Int,
    val period: String,
    val incomeType: String,
    val subType: String,
    val agent: String,
    val income: Double,
    val tax: Double
)

data class MonthlyIncomeConfig(
    val id: String = UUID.randomUUID().toString(),
    val year: Int,
    val month: Int,
    val amount: Double,
    val agent: String = DEFAULT_AGENT,
    val subType: String = "正常工资薪金",
    /** Explicit declared tax from seed/screenshot; null = calculate when querying. */
    val tax: Double? = null
) {
    val period: String get() = "%04d-%02d".format(year, month)
}

data class Profile(
    val name: String,
    val taxpayerId: String,
    val employerCount: Int = 1,
    val bankCardCount: Int = 2,
    val specialDeductionCount: Int = 1
)

const val DEFAULT_AGENT = "上海捷晓信息技术有限公司"

val INCOME_TYPES = listOf(
    "工资薪金",
    "劳务报酬",
    "稿酬",
    "特许权使用费"
)
