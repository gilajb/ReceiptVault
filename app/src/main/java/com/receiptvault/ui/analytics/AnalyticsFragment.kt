package com.receiptvault.ui.analytics

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.receiptvault.databinding.FragmentAnalyticsBinding
import com.receiptvault.utils.CsvExporter
import com.receiptvault.utils.monthDisplayName
import com.receiptvault.utils.showToast
import com.receiptvault.utils.toCurrencyString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        setupObservers()
        setupClickListeners()
    }

    private fun setupCharts() {
        // Bar chart style
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            animateY(800)
        }

        // Pie chart style
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            setHoleColor(Color.TRANSPARENT)
            setUsePercentValues(true)
            legend.isEnabled = true
            animateY(800)
        }
    }

    private fun setupObservers() {
        viewModel.currentMonth.observe(viewLifecycleOwner) { prefix ->
            binding.tvMonth.text = monthDisplayName(prefix)
        }

        viewModel.monthlyTotal.observe(viewLifecycleOwner) { total ->
            binding.tvMonthlyTotal.text = (total ?: 0.0).toCurrencyString()
        }

        viewModel.categoryTotals.observe(viewLifecycleOwner) { totals ->
            updateBarChart(totals.map { it.category }.zip(totals.map { it.total }))
            updatePieChart(totals.map { it.category }.zip(totals.map { it.total }))
            updateBudgetBars(totals.associate { it.category to it.total })
        }

        viewModel.receipts.observe(viewLifecycleOwner) { receipts ->
            binding.tvReceiptCount.text = "${receipts.size} receipts this month"
        }
    }

    private fun updateBarChart(data: List<Pair<String, Double>>) {
        if (data.isEmpty()) {
            binding.barChart.clear()
            binding.barChart.invalidate()
            return
        }

        val entries = data.mapIndexed { i, (_, amount) ->
            BarEntry(i.toFloat(), amount.toFloat())
        }
        val labels = data.map { it.first }

        val dataSet = BarDataSet(entries, "Spending by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 10f
        }

        binding.barChart.apply {
            this.data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelRotationAngle = -30f
            invalidate()
        }
    }

    private fun updatePieChart(data: List<Pair<String, Double>>) {
        if (data.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.invalidate()
            return
        }

        val entries = data.map { (label, amount) ->
            PieEntry(amount.toFloat(), label)
        }
        val dataSet = PieDataSet(entries, "Categories").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        binding.pieChart.apply {
            this.data = PieData(dataSet)
            invalidate()
        }
    }

    private fun updateBudgetBars(spending: Map<String, Double>) {
        val budgets = viewModel.budgetLimits
        val container = binding.budgetContainer
        container.removeAllViews()

        budgets.forEach { (category, budget) ->
            val spent = spending[category] ?: 0.0
            val percent = ((spent / budget) * 100).coerceIn(0.0, 100.0).toInt()

            val row = layoutInflater.inflate(
                com.receiptvault.R.layout.item_budget_progress,
                container,
                false
            )

            row.findViewById<android.widget.TextView>(com.receiptvault.R.id.tvBudgetCategory).text = category
            row.findViewById<android.widget.TextView>(com.receiptvault.R.id.tvBudgetAmount).text =
                "${spent.toCurrencyString()} / ${budget.toCurrencyString()}"

            val progressBar = row.findViewById<android.widget.ProgressBar>(com.receiptvault.R.id.progressBudget)
            progressBar.progress = percent
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                when {
                    percent >= 100 -> Color.RED
                    percent >= 80 -> Color.parseColor("#FF9800")
                    else -> Color.parseColor("#4CAF50")
                }
            )

            container.addView(row)
        }
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }

        binding.btnExportCsv.setOnClickListener {
            val receipts = viewModel.receipts.value ?: emptyList()
            if (receipts.isEmpty()) {
                showToast("No receipts to export")
                return@setOnClickListener
            }
            val uri = CsvExporter.exportReceipts(requireContext(), receipts)
            if (uri != null) {
                showToast("Exported to Downloads folder")
                val shareIntent = CsvExporter.createShareIntent(requireContext(), uri)
                startActivity(Intent.createChooser(shareIntent, "Share CSV"))
            } else {
                showToast("Export failed")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
