package scamell.michael.amulet;

import java.math.BigDecimal;

public class UnitCalculator {

    //source for typical percentages
    //http://www.drinkaware.co.uk/understand-your-drinking/unit-calorie-calculator/
    public static final float beerPintABV = 4;
    public static final float beerBottleABV = 5;
    public static final float ciderABV = 4.5f;
    public static final float wineABV = 13;
    public static final float champagneABV = 12;
    public static final float spiritsABV = 40;
    public static final float alcopopABV = 4f;
    public static final float pintVolume = 568;
    public static final float beerBottleVolume = 330;
    public static final float wineGlassVolume = 175;
    public static final float champagneGlassVolume = 125;
    public static final float spiritGlassVolume = 25;
    public static final float alcopopBottleVolume = 275;
    private static final int bigDecimalScale = 2;

    public static float UnitCalculation(float quantity, float volume, float aBV) {
        BigDecimal result = new BigDecimal((quantity * volume) * aBV / 1000);
        result = result.setScale(bigDecimalScale, BigDecimal.ROUND_HALF_EVEN);
        return result.floatValue();
    }

    public static float UnitCalculation(float quantity, String volumeMeasurement, float volume, float aBV) {
        if (volumeMeasurement.equals("pints")) {
            volume = convertPintsToMl(volume);
        } else if (volumeMeasurement.equals("litres")) {
            volume = volume * 1000;
        } else if (volumeMeasurement.equals("cl")) {
            aBV = aBV * 10;
        }
        BigDecimal result = new BigDecimal((quantity * volume) * aBV / 1000);
        result = result.setScale(bigDecimalScale, BigDecimal.ROUND_HALF_EVEN);
        return result.floatValue();
    }

//    public static float beerPintUnitCalculation(float quantity) {
//        return UnitCalculation(quantity, beerPintABV, pintVolume);
//    }
//
//    public static float beerBottleUnitCalculation(float quantity) {
//        return UnitCalculation(quantity, beerBottleABV, beerBottleVolume);
//    }
//
//    public static float ciderPintUnitCalculation(float quantity) {
//        return UnitCalculation(quantity, ciderABV, pintVolume);
//    }
//
//    public static float wineUnitCalculation(float quantity) {
//        return UnitCalculation(quantity, wineABV, wineGlassVolume);
//    }
//
//    public static float champagneUnitCalculation(float quantity) {
//        return UnitCalculation(quantity, champagneABV, champagneGlassVolume);
//    }
//
//    public static float spiritsUnitCalculation(float quantity) {
//        return UnitCalculation(quantity, spiritsABV, spiritGlassVolume);
//    }
//
//    public static float alcopopUnitCalculation(float quantity) {
//        return UnitCalculation(quantity, alcopopABV, alcopopBottleVolume);
//    }

    private static float convertPintsToMl(float numberToConvert) {
        return numberToConvert * pintVolume;
    }
}
