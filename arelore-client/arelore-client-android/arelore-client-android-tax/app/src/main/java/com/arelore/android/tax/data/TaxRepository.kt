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

    fun ensureSeeded() {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        saveMonthlyConfigs(defaultMonthlyConfigs())
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    fun monthlyConfigs(year: Int): List<MonthlyIncomeConfig> {
        ensureSeeded()
        return loadMonthlyConfigs().filter { it.year == year }.sortedByDescending { it.month }
    }

    fun allMonthlyConfigs(): List<MonthlyIncomeConfig> {
        ensureSeeded()
        return loadMonthlyConfigs().sortedWith(compareByDescending<MonthlyIncomeConfig> { it.year }.thenByDescending { it.month })
    }

    fun saveMonthlyConfig(config: MonthlyIncomeConfig) {
        val list = loadMonthlyConfigs().toMutableList()
        val idx = list.indexOfFirst { it.year == config.year && it.month == config.month }
        if (idx >= 0) list[idx] = config else list.add(config)
        saveMonthlyConfigs(list)
    }

    fun deleteMonthlyConfig(year: Int, month: Int) {
        saveMonthlyConfigs(loadMonthlyConfigs().filterNot { it.year == year && it.month == month })
    }

    fun records(year: Int, incomeType: String): List<IncomeRecord> {
        val configs = monthlyConfigs(year)
            .filter { incomeType.isBlank() || incomeType == "工资薪金" }
            .sortedBy { it.month }
        if (configs.isEmpty()) return emptyList()

        val taxes = TaxCalculator.monthlyTax(configs.map { it.amount })
        val generated = configs.zip(taxes).map { (cfg, tax) ->
            IncomeRecord(
                id = "gen-${cfg.year}-${cfg.month}",
                year = cfg.year,
                period = cfg.period,
                incomeType = "工资薪金",
                subType = cfg.subType,
                agent = cfg.agent,
                income = cfg.amount,
                tax = round2(tax)
            )
        }.reversed()

        val extras = seedExtraRecords().filter {
            it.year == year && (incomeType.isBlank() || it.incomeType == incomeType)
        }
        return (generated + extras).sortedByDescending { it.period }
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
                add(
                    MonthlyIncomeConfig(
                        year = o.getInt("year"),
                        month = o.getInt("month"),
                        amount = o.getDouble("amount"),
                        agent = o.optString("agent", DEFAULT_AGENT),
                        subType = o.optString("subType", "正常工资薪金")
                    )
                )
            }
        }
    }

    private fun saveMonthlyConfigs(list: List<MonthlyIncomeConfig>) {
        val arr = JSONArray()
        list.forEach { cfg ->
            arr.put(
                JSONObject()
                    .put("year", cfg.year)
                    .put("month", cfg.month)
                    .put("amount", cfg.amount)
                    .put("agent", cfg.agent)
                    .put("subType", cfg.subType)
            )
        }
        prefs.edit().putString(KEY_MONTHLY, arr.toString()).apply()
    }

    private fun defaultMonthlyConfigs(): List<MonthlyIncomeConfig> {
        val agentA = DEFAULT_AGENT
        val agentB = "睹熠（上海）信息服务有限公司"
        return listOf(
            MonthlyIncomeConfig(2026, 9, 30608.70, agentA),
            MonthlyIncomeConfig(2026, 8, 16051.28, agentB),
            MonthlyIncomeConfig(2026, 7, 28500.00, agentB),
            MonthlyIncomeConfig(2026, 6, 27800.00, agentB),
            MonthlyIncomeConfig(2026, 5, 27200.00, agentB),
            MonthlyIncomeConfig(2026, 4, 26800.00, agentB),
            MonthlyIncomeConfig(2026, 3, 26500.00, agentB),
            MonthlyIncomeConfig(2026, 2, 26000.00, agentB),
            MonthlyIncomeConfig(2026, 1, 25500.00, agentB)
        )
    }

    private fun seedExtraRecords(): List<IncomeRecord> = listOf(
        IncomeRecord(
            id = UUID.nameUUIDFromBytes("comp-2026-08".toByteArray()).toString(),
            year = 2026,
            period = "2026-08",
            incomeType = "工资薪金",
            subType = "解除劳动合同一次性补偿金",
            agent = "睹熠（上海）信息服务有限公司",
            income = 141906.00,
            tax = 0.00
        )
    )

    private fun round2(v: Double): Double = kotlin.math.round(v * 100.0) / 100.0

    companion object {
        private const val PREFS = "tax_sim"
        private const val KEY_SEEDED = "seeded"
        private const val KEY_MONTHLY = "monthly_configs"
        private const val KEY_NAME = "profile_name"
        private const val KEY_TAXPAYER_ID = "taxpayer_id"
    }
}
