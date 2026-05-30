package com.example.util

enum class UnitCategory {
    LENGTH, WEIGHT, TEMPERATURE, SPEED, AREA, VOLUME, TIME, PRESSURE, ENERGY, DATA_STORAGE
}

data class UnitInfo(val code: String, val name: String, val factor: Double) // factor relative to base unit

object UnitConverterUtil {

    val categories = mapOf(
        UnitCategory.LENGTH to listOf(
            UnitInfo("m", "Meter (m)", 1.0),
            UnitInfo("mm", "Millimeter (mm)", 0.001),
            UnitInfo("cm", "Centimeter (cm)", 0.01),
            UnitInfo("km", "Kilometer (km)", 1000.0),
            UnitInfo("in", "Inch (in)", 0.0254),
            UnitInfo("ft", "Foot (ft)", 0.3048),
            UnitInfo("yd", "Yard (yd)", 0.9144),
            UnitInfo("mi", "Mile (mi)", 1609.344)
        ),
        UnitCategory.WEIGHT to listOf(
            UnitInfo("kg", "Kilogram (kg)", 1.0),
            UnitInfo("mg", "Milligram (mg)", 1e-6),
            UnitInfo("g", "Gram (g)", 0.001),
            UnitInfo("lb", "Pound (lb)", 0.45359237),
            UnitInfo("oz", "Ounce (oz)", 0.028349523),
            UnitInfo("st", "Stone (st)", 6.35029318)
        ),
        UnitCategory.TEMPERATURE to listOf(
            UnitInfo("C", "Celsius (°C)", 1.0),
            UnitInfo("F", "Fahrenheit (°F)", 1.0), // custom formula applied below
            UnitInfo("K", "Kelvin (K)", 1.0)       // custom formula applied below
        ),
        UnitCategory.SPEED to listOf(
            UnitInfo("m_s", "Meter / Second (m/s)", 1.0),
            UnitInfo("km_h", "Kilometer / Hour (km/h)", 0.27777778),
            UnitInfo("mph", "Miles / Hour (mph)", 0.44704),
            UnitInfo("kn", "Knots (kn)", 0.514444)
        ),
        UnitCategory.AREA to listOf(
            UnitInfo("m2", "Square Meter (m²)", 1.0),
            UnitInfo("mm2", "Square Millimeter (mm²)", 1e-6),
            UnitInfo("cm2", "Square Centimeter (cm²)", 1e-4),
            UnitInfo("km2", "Square Kilometer (km²)", 1e6),
            UnitInfo("in2", "Square Inch (in²)", 0.00064516),
            UnitInfo("ft2", "Square Foot (ft²)", 0.09290304),
            UnitInfo("ac", "Acre (ac)", 4046.85642),
            UnitInfo("ha", "Hectare (ha)", 10000.0)
        ),
        UnitCategory.VOLUME to listOf(
            UnitInfo("L", "Liter (L)", 1.0),
            UnitInfo("mL", "Milliliter (mL)", 0.001),
            UnitInfo("m3", "Cubic Meter (m³)", 1000.0),
            UnitInfo("tsp", "Teaspoon (tsp)", 0.00492892),
            UnitInfo("tbsp", "Tablespoon (tbsp)", 0.01478676),
            UnitInfo("cup", "Cup", 0.23658824),
            UnitInfo("fl_oz", "Fluid Ounce (fl oz)", 0.02957353),
            UnitInfo("pt", "Pint (pt)", 0.47317647),
            UnitInfo("qt", "Quart (qt)", 0.94635295),
            UnitInfo("gal", "Gallon (gal)", 3.78541178)
        ),
        UnitCategory.TIME to listOf(
            UnitInfo("s", "Second (s)", 1.0),
            UnitInfo("ms", "Millisecond (ms)", 0.001),
            UnitInfo("min", "Minute (min)", 60.0),
            UnitInfo("h", "Hour (h)", 3600.0),
            UnitInfo("d", "Day (d)", 86400.0),
            UnitInfo("wk", "Week (wk)", 604800.0),
            UnitInfo("mo", "Month (mo)", 2629746.0), // average month (30.44 days)
            UnitInfo("yr", "Year (yr)", 31556952.0)  // average calendar year (365.2425 days)
        ),
        UnitCategory.PRESSURE to listOf(
            UnitInfo("Pa", "Pascal (Pa)", 1.0),
            UnitInfo("kPa", "Kilopascal (kPa)", 1000.0),
            UnitInfo("bar", "Bar (bar)", 100000.0),
            UnitInfo("psi", "Pounds / Sq. Inch (psi)", 6894.757),
            UnitInfo("atm", "Atmosphere (atm)", 101325.0)
        ),
        UnitCategory.ENERGY to listOf(
            UnitInfo("J", "Joule (J)", 1.0),
            UnitInfo("kJ", "Kilojoule (kJ)", 1000.0),
            UnitInfo("cal", "Calorie (cal)", 4.184),
            UnitInfo("kcal", "Kilocalorie (kcal)", 4184.0),
            UnitInfo("Wh", "Watt-hour (Wh)", 3600.0),
            UnitInfo("kWh", "Kilowatt-hour (kWh)", 3600000.0)
        ),
        UnitCategory.DATA_STORAGE to listOf(
            UnitInfo("B", "Byte (B)", 1.0),
            UnitInfo("bit", "Bit (b)", 0.125),
            valInfo("KB", "Kilobyte (KB)", 1000.0), // standard KB
            valInfo("MB", "Megabyte (MB)", 1000000.0),
            valInfo("GB", "Gigabyte (GB)", 1e9),
            valInfo("TB", "Terabyte (TB)", 1e12),
            valInfo("PB", "Petabyte (PB)", 1e15)
        )
    )

    private fun valInfo(code: String, name: String, factor: Double) = UnitInfo(code, name, factor)

    fun convert(value: Double, from: UnitInfo, to: UnitInfo, category: UnitCategory): Double {
        if (category == UnitCategory.TEMPERATURE) {
            // Temperature conversion needs offsets
            val celsius = when (from.code) {
                "C" -> value
                "F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                else -> value
            }
            return when (to.code) {
                "C" -> celsius
                "F" -> celsius * 9.0 / 5.0 + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        } else {
            val baseValue = value * from.factor
            return baseValue / to.factor
        }
    }
}
