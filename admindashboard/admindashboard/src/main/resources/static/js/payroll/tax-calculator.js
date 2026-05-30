/* ==========================================
   Indian Income Tax Calculator – FY 2026-27
   New Regime (default) + Old Regime comparison
   ========================================== */

(function () {

    /* ── Tax Slab Calculators ─────────────────────────────────── */

    function calcNewRegime(grossSalary) {
        var taxable = Math.max(0, grossSalary - 75000); // ₹75,000 standard deduction
        var tax = 0;

        // FY 2026-27 New Regime slabs
        var slabs = [
            { limit: 400000,  rate: 0.00 },
            { limit: 400000,  rate: 0.05 },
            { limit: 400000,  rate: 0.10 },
            { limit: 400000,  rate: 0.15 },
            { limit: 400000,  rate: 0.20 },
            { limit: 400000,  rate: 0.25 },
            { limit: Infinity, rate: 0.30 }
        ];

        var thresholds = [0, 400000, 800000, 1200000, 1600000, 2000000, 2400000];
        var remaining = taxable;

        for (var i = 0; i < thresholds.length; i++) {
            if (remaining <= 0) break;
            var slabWidth = (i < thresholds.length - 1) ? (thresholds[i + 1] - thresholds[i]) : Infinity;
            var taxable_in_slab = Math.min(remaining, slabWidth);
            tax += taxable_in_slab * slabs[i].rate;
            remaining -= taxable_in_slab;
        }

        // Rebate u/s 87A: if taxable income ≤ ₹7,00,000, full rebate
        if (taxable <= 700000) tax = 0;

        var cess = tax * 0.04;
        return { tax: Math.round(tax), cess: Math.round(cess), total: Math.round(tax + cess), taxable: Math.round(taxable) };
    }

    function calcOldRegime(grossSalary, hra, section80C, nps) {
        var stdDed = 50000;
        var taxable = Math.max(0, grossSalary - stdDed - (hra || 0) - Math.min(section80C || 0, 150000) - Math.min(nps || 0, 50000));

        var tax = 0;
        if (taxable <= 250000) {
            tax = 0;
        } else if (taxable <= 500000) {
            tax = (taxable - 250000) * 0.05;
        } else if (taxable <= 1000000) {
            tax = 12500 + (taxable - 500000) * 0.20;
        } else {
            tax = 112500 + (taxable - 1000000) * 0.30;
        }

        // Rebate u/s 87A: if taxable ≤ ₹5,00,000, rebate up to ₹12,500
        if (taxable <= 500000) tax = Math.max(0, tax - 12500);

        var cess = tax * 0.04;
        return { tax: Math.round(tax), cess: Math.round(cess), total: Math.round(tax + cess), taxable: Math.round(taxable) };
    }

    /* ── Format helper ────────────────────────────────────────── */

    function fmt(n) {
        return '₹' + n.toLocaleString('en-IN');
    }

    /* ── Update Result Table ──────────────────────────────────── */

    function updateResults(newR, oldR) {
        // New regime cells
        document.getElementById('res-new-taxable').textContent = fmt(newR.taxable);
        document.getElementById('res-new-tax').textContent     = fmt(newR.tax);
        document.getElementById('res-new-cess').textContent    = fmt(newR.cess);
        document.getElementById('res-new-total').textContent   = fmt(newR.total);

        // Old regime cells
        document.getElementById('res-old-taxable').textContent = fmt(oldR.taxable);
        document.getElementById('res-old-tax').textContent     = fmt(oldR.tax);
        document.getElementById('res-old-cess').textContent    = fmt(oldR.cess);
        document.getElementById('res-old-total').textContent   = fmt(oldR.total);

        // Recommendation banner
        var banner = document.getElementById('tax-recommendation');
        var saving = oldR.total - newR.total;
        if (banner) {
            if (saving > 0) {
                banner.innerHTML = '<i class="fa-solid fa-circle-check"></i> <strong>New Regime saves you ' + fmt(saving) + ' per year.</strong>';
                banner.className = 'tax-banner tax-banner-success';
            } else if (saving < 0) {
                banner.innerHTML = '<i class="fa-solid fa-circle-check"></i> <strong>Old Regime saves you ' + fmt(Math.abs(saving)) + ' per year.</strong>';
                banner.className = 'tax-banner tax-banner-info';
            } else {
                banner.innerHTML = '<i class="fa-solid fa-equals"></i> <strong>Both regimes result in the same tax.</strong>';
                banner.className = 'tax-banner tax-banner-neutral';
            }
            banner.style.display = 'flex';
        }

        // Show result section
        var resultSection = document.getElementById('tax-result-section');
        if (resultSection) resultSection.style.display = 'block';
    }

    /* ── Main Calculate Function ──────────────────────────────── */

    function calculate() {
        var grossSalary = parseFloat(document.getElementById('grossSalary').value) || 0;
        var hra         = parseFloat(document.getElementById('hraExemption').value) || 0;
        var sec80C      = parseFloat(document.getElementById('section80C').value) || 0;
        var nps         = parseFloat(document.getElementById('npsContrib').value) || 0;

        if (grossSalary <= 0) {
            alert('Please enter a valid Annual Gross Salary.');
            return;
        }

        var newR = calcNewRegime(grossSalary);
        var oldR = calcOldRegime(grossSalary, hra, sec80C, nps);
        updateResults(newR, oldR);
    }

    /* ── Event Listeners ──────────────────────────────────────── */

    document.addEventListener('DOMContentLoaded', function () {
        var btn = document.getElementById('calculateTaxBtn');
        if (btn) btn.addEventListener('click', calculate);
    });

})();
