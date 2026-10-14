package com.arelore.android.tax.data

/**
 * Simplified cumulative withholding for demo (综合所得累计预扣预缴).
 * Basic deduction 5000/month; progressive rates matching IIT brackets.
 */
object TaxCalculator {

    private val brackets = listOf(
        36_000.0 to 0.03,
        144_000.0 to 0.10,
        300_000.0 to 0.20,
        420_000.0 to 0.25,
        660_000.0 to 0.30,
        960_000.0 to 0.35,
        Double.MAX_VALUE to 0.45
    )

    fun monthlyTax(
        monthlyIncomes: List<Double>,
        specialDeductionMonthly: Double = 1500.0
    ): List<Double> {
        var cumulativeIncome = 0.0
        var cumulativeDeduction = 0.0
        var cumulativeTax = 0.0
        return monthlyIncomes.map { income ->
            cumulativeIncome += income
            cumulativeDeduction += 5000.0 + specialDeductionMonthly
            val taxable = (cumulativeIncome - cumulativeDeduction).coerceAtLeast(0.0)
            val totalTax = progressiveTax(taxable)
            val monthTax = (totalTax - cumulativeTax).coerceAtLeast(0.0)
            cumulativeTax = totalTax
            monthTax
        }
    }

    private fun progressiveTax(taxable: Double): Double {
        if (taxable <= 0) return 0.0
        var remaining = taxable
        var previousCap = 0.0
        var tax = 0.0
        for ((cap, rate) in brackets) {
            val band = (cap - previousCap).coerceAtLeast(0.0)
            val take = remaining.coerceAtMost(band)
            tax += take * rate
            remaining -= take
            previousCap = cap
            if (remaining <= 0) break
        }
        return tax
    }
}
