package com.arelore.android.tax.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TaxRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun profile(): Profile = Profile(
        name = prefs.getString(KEY_NAME, "黄佳豪") ?: "黄佳豪",
        taxpayerId = prefs.getString(KEY_TAXPAYER_ID, "421121199710192412") ?: "421121199710192412"
    )

    /** App font scale relative to Redmi Note 15 baseline (1.0 = 标准). */
    fun fontScale(): Float = prefs.getFloat(KEY_FONT_SCALE, DEFAULT_FONT_SCALE)

    fun setFontScale(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    fun ensureSeeded() {
        val version = prefs.getInt(KEY_SEED_VERSION, 0)
        if (version >= SEED_VERSION) return
        saveMonthlyConfigs(defaultMonthlyConfigs())
        prefs.edit()
            .putInt(KEY_SEED_VERSION, SEED_VERSION)
            .putBoolean(KEY_SEEDED, true)
            .apply()
    }

    fun monthlyConfigs(year: Int): List<MonthlyIncomeConfig> {
        ensureSeeded()
        return loadMonthlyConfigs().filter { it.year == year }
            .sortedWith(compareByDescending<MonthlyIncomeConfig> { it.month }.thenBy { it.subType })
    }

    fun allMonthlyConfigs(): List<MonthlyIncomeConfig> {
        ensureSeeded()
        return loadMonthlyConfigs().sortedWith(
            compareByDescending<MonthlyIncomeConfig> { it.year }
                .thenByDescending { it.month }
                .thenBy { it.subType }
        )
    }

    fun saveMonthlyConfig(config: MonthlyIncomeConfig) {
        val list = loadMonthlyConfigs().toMutableList()
        val idx = list.indexOfFirst { it.id == config.id }
        if (idx >= 0) list[idx] = config else list.add(config)
        saveMonthlyConfigs(list)
    }

    fun deleteMonthlyConfig(id: String) {
        saveMonthlyConfigs(loadMonthlyConfigs().filterNot { it.id == id })
    }

    fun records(year: Int, incomeTypes: Collection<String>): List<IncomeRecord> {
        val types = incomeTypes.filter { it.isNotBlank() }.toSet()
        val includeSalary = types.isEmpty() || "工资薪金" in types
        if (!includeSalary) return emptyList()

        val configs = monthlyConfigs(year)
        if (configs.isEmpty()) return emptyList()

        val withExplicitTax = configs.any { it.tax != null }
        val calculatedTaxes: Map<String, Double> = if (withExplicitTax) {
            emptyMap()
        } else {
            val ordered = configs.sortedWith(compareBy({ it.month }, { it.id }))
            val taxes = TaxCalculator.monthlyTax(ordered.map { it.amount })
            ordered.mapIndexed { i, cfg -> cfg.id to round2(taxes[i]) }.toMap()
        }

        return configs.map { cfg ->
            IncomeRecord(
                id = cfg.id,
                year = cfg.year,
                period = cfg.period,
                incomeType = "工资薪金",
                subType = cfg.subType,
                agent = cfg.agent,
                income = cfg.amount,
                tax = cfg.tax?.let { round2(it) } ?: calculatedTaxes[cfg.id] ?: 0.0
            )
        }.sortedByDescending { it.period }
    }

    fun summary(records: List<IncomeRecord>): Pair<Double, Double> {
        val income = records.sumOf { it.income }
        val tax = records.sumOf { it.tax }
        return round2(income) to round2(tax)
    }

    private fun loadMonthlyConfigs(): List<MonthlyIncomeConfig> {
        val raw = prefs.getString(KEY_MONTHLY, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val tax = if (o.has("tax") && !o.isNull("tax")) o.getDouble("tax") else null
                add(
                    MonthlyIncomeConfig(
                        id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                        year = o.getInt("year"),
                        month = o.getInt("month"),
                        amount = o.getDouble("amount"),
                        agent = o.optString("agent", DEFAULT_AGENT),
                        subType = o.optString("subType", "正常工资薪金"),
                        tax = tax
                    )
                )
            }
        }
    }

    private fun saveMonthlyConfigs(list: List<MonthlyIncomeConfig>) {
        val arr = JSONArray()
        list.forEach { cfg ->
            val o = JSONObject()
                .put("id", cfg.id)
                .put("year", cfg.year)
                .put("month", cfg.month)
                .put("amount", cfg.amount)
                .put("agent", cfg.agent)
                .put("subType", cfg.subType)
            if (cfg.tax != null) o.put("tax", cfg.tax) else o.put("tax", JSONObject.NULL)
            arr.put(o)
        }
        prefs.edit().putString(KEY_MONTHLY, arr.toString()).apply()
    }

    /**
     * 2023 默认明细（来自 23年个税.jpg），已排除：
     * 众安在线、北京城市网邻、解除劳动合同一次性补偿金。
     */
    private fun defaultMonthlyConfigs(): List<MonthlyIncomeConfig> {
        val yunhai = "云海链控股股份有限公司"
        val ant = "蚂蚁智信（杭州）信息技术有限公司"
        fun entry(
            id: String,
            month: Int,
            amount: Double,
            tax: Double,
            agent: String,
            subType: String = "正常工资薪金"
        ) = MonthlyIncomeConfig(
            id = id,
            year = 2023,
            month = month,
            amount = amount,
            agent = agent,
            subType = subType,
            tax = tax
        )
        return listOf(
            entry("2023-12-yunhai", 12, 21770.00, 337.02, yunhai),
            entry("2023-11-yunhai", 11, 22270.00, 332.93, yunhai),
            entry("2023-10-yunhai", 10, 9363.33, 0.00, yunhai),
            entry("2023-06-ant", 6, 7720.00, 0.00, ant),
            entry("2023-05-ant", 5, 24400.00, 1414.04, ant),
            entry("2023-04-ant", 4, 24400.00, 1414.04, ant),
            entry("2023-03-ant-salary", 3, 24460.00, 869.89, ant),
            entry("2023-03-ant-bonus", 3, 22000.00, 660.00, ant, "全年一次性奖金收入"),
            entry("2023-02-ant", 2, 24400.00, 424.21, ant),
            entry("2023-01-ant", 1, 24260.00, 420.01, ant)
        )
    }

    private fun round2(v: Double): Double = kotlin.math.round(v * 100.0) / 100.0

    companion object {
        private const val PREFS = "tax_sim"
        private const val KEY_SEEDED = "seeded"
        private const val KEY_SEED_VERSION = "seed_version"
        /** Bump to force re-apply default seed (2023 data, drop excluded agents). */
        private const val SEED_VERSION = 2
        private const val KEY_MONTHLY = "monthly_configs"
        private const val KEY_NAME = "profile_name"
        private const val KEY_TAXPAYER_ID = "taxpayer_id"
        private const val KEY_FONT_SCALE = "font_scale"
        const val DEFAULT_FONT_SCALE = 1.0f
    }
}
