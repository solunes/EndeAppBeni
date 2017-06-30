package com.solunes.endeappbeni.utils;

import android.util.Log;
import android.util.Pair;

import com.solunes.endeappbeni.models.DataModel;
import com.solunes.endeappbeni.models.Historico;

import java.util.ArrayList;
import java.util.Calendar;

import static com.solunes.endeappbeni.utils.PrintGenerator.calcDays;
import static com.solunes.endeappbeni.utils.PrintGenerator.formatedDate;
import static com.solunes.endeappbeni.utils.PrintGenerator.getFechaEmi;
import static com.solunes.endeappbeni.utils.PrintGenerator.mesString;

/**
 * Created by jhonlimaster on 01-03-17.
 */

public class AvisoCobro {
    private static final String TAG = "AvisoCobro";

    public static String creator(DataModel dataModel,
                                 Historico hist,
                                 ArrayList<String> titles,
                                 ArrayList<Double> values,
                                 String garantiaString,
                                 double garantia,
                                 String[] leyenda,
                                 String consejo) {

        String deudasEnergia = "";

        if (dataModel.getTlxDeuEneC() > 0) {
            deudasEnergia = "LEFT\r\n" +
                    "T TF01.CPF 0 40 1170 Deuda(s) pendiente(s) de energía   (" + dataModel.getTlxDeuEneC() + ")  Bs\r\n";
            deudasEnergia += "RIGHT 782\r\n" +
                    "T TF01.CPF 0 720 1170 " + StringUtils.roundTwoDigits(dataModel.getTlxDeuEneI()) + "\r\n";
        }

        if (dataModel.getTlxDeuEneC() > 2) {
            deudasEnergia += "T TF01.CPF 0 40 1200 Señor cliente usted es pasible de corte\r\n";

        }

        String garantiaText = "";
        if (garantia > 0) {
            garantiaText = "LEFT\r\n" +
                    "T HF04.CPF 0 65 930 " + garantiaString + "\r\n" +
                    "T HF04.CPF 0 575 930 Bs\r\n" +
                    "RIGHT 782\r\n" +
                    "T HF04.CPF 0 720 930 " + garantia + "\r\n";
        }

        String detalleFacturacion = "";
//        String[] strings = (String[]) list.keySet().toArray();
        int yValue = 605;
//        String[] values = new String[]{"123.2", "123.4", "123.1", "123.3", "123.5"};
        for (int i = 0; i < titles.size(); i++) {
            if (i == (titles.size() - 1)) {
                detalleFacturacion +=
                        "LEFT\r\n" +
                                "T HF04.CPF 0 65 " + yValue + " " + titles.get(i) + "\r\n" +
                                "T HF04.CPF 0 575 " + yValue + " Bs\r\n" +
                                "RIGHT 782\r\n" +
                                "T HF04.CPF 0 720 " + yValue + " " + StringUtils.roundTwoDigits(values.get(i)) + "\r\n";
                Log.e(TAG, "creator: " + values.get(i));
            } else {
                detalleFacturacion +=
                        "LEFT\r\n" +
                                "T TF01.CPF 0 65 " + yValue + " " + titles.get(i) + "\r\n" +
                                "T TF01.CPF 0 575 " + yValue + " Bs\r\n" +
                                "RIGHT 782\r\n" +
                                "T TF01.CPF 0 720 " + yValue + " " + StringUtils.roundTwoDigits(values.get(i)) + "\r\n";
            }
            yValue += 25;
        }

        Pair<String, Integer> pairDetalleConsumo = detalleConsumo(dataModel);

        try {
            Integer.parseInt(dataModel.getTlxCtaAnt());
        } catch (NumberFormatException nfe) {
            dataModel.setTlxCtaAnt("");
        }

        String cpclConfigLabel = "! 0 200 200 1625 1\r\n" +
                "LEFT\r\n" +
                "T HF02.CPF 0 590 5 PRE AVISO\r\n" +
                "T HF02.CPF 0 570 30 DE COBRANZA\r\n" +
                "PCX 70 5 !<logo1.PCX\r\n" +

                "CENTER\r\n" +
                "T HF02.CPF 0 40 80 DATOS DEL CONSUMIDOR\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 70 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 100 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 120 Fecha Emisión: \r\n" +
                "CENTER\r\n" +
                "T TF01.CPF 0 50 120 " + dataModel.getTlxCiudad() + ", " + getFechaEmi(dataModel.getTlxFecEmi()) + " \r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 150 Señor (a):  " + dataModel.getTlxNom() + "\r\n" +
                "T TF01.CPF 0 565 150 NIT/CI:\r\n" +
                "RIGHT 100\r\n" +
                "T TF01.CPF 0 700 150 " + dataModel.getTlxCliNit() + "\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 195 N° Cliente:\r\n" +
                "T TF01.CPF 0 565 195 N° Medidor:\r\n" +
                "RIGHT 100\r\n" +
                "T HF03.CPF 0 165 190 " + dataModel.getTlxCli() + "\r\n" +
                "T TF01.CPF 0 700 195 " + dataModel.getTlxNroMed() + "\r\n" +


                "LEFT\r\n" +
                "T TF01.CPF 0 40 230 Dirección:  " + dataModel.getTlxDir() + "\r\n" +

                "T TF01.CPF 0 30 260 -------------------------------------------------------------------------------------------------------------\r\n" +

                // BLOQUE 2: DATOS DE MEDICION
                "T TF01.CPF 0 40 280 Ciudad o Localidad:  " + dataModel.getTlxCiudad() + "\r\n" +
                "T TF01.CPF 0 575 280 Ruta: " + dataModel.getTlxRem() + "-" + dataModel.getTlxRutO() + "\r\n" +

                "T TF01.CPF 0 40 305 Mes:\r\n" +
                "T HF03.CPF 0 300 295 " + mesString(dataModel.getTlxMes()).toUpperCase() + "-" + dataModel.getTlxAno() + "\r\n" +
                "T TF01.CPF 0 590 305 " + dataModel.getTlxCtaAnt() + "\r\n" +

                "T TF01.CPF 0 40 330 Categoría Tarifaria:\r\n" +
                "T TF01.CPF 0 300 330 " + dataModel.getTlxSgl() + "\r\n" +

                "T TF01.CPF 0 40 360 Descripción\r\n" +
                "T TF01.CPF 0 295 360 Anterior\r\n" +
                "T TF01.CPF 0 480 360 Actual\r\n" +
                "T TF01.CPF 0 630 360 Tipo Lectura\r\n" +

                "T TF01.CPF 0 40 390 Fecha Lectura\r\n" +
                "T TF01.CPF 0 280 390 " + formatedDate(dataModel.getTlxFecAnt()) + "\r\n" +
                "T TF01.CPF 0 470 390 " + formatedDate(dataModel.getTlxFecLec()) + "\r\n" +
                "T TF01.CPF 0 603 390 " + DataModel.getTipoLectura(dataModel.getTlxTipLec()) + "\r\n" +

                "T TF01.CPF 0 40 420 Lectura Medidor\r\n" +
                "T TF01.CPF 0 310 420 " + dataModel.getTlxUltInd() + "\r\n" +
                "T TF01.CPF 0 500 420 " + dataModel.getTlxNvaLec() + "\r\n" +

                "T TF01.CPF 0 40 450 Energía Consumida en (" + calcDays(dataModel.getTlxFecAnt(), dataModel.getTlxFecLec()) + ") dias\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 700 450 " + dataModel.getTlxConsumo() + " kWh\r\n" +
                subCuadros(450) +

                pairDetalleConsumo.first +
                cuadros() +

                "CENTER\r\n" +
                "T HF02.CPF 0 0 570 DETALLE PRE AVISO DE COBRANZA\n\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 560 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 590 -------------------------------------------------------------------------------------------------------------\r\n" +

                detalleFacturacion +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 745 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 771 -------------------------------------------------------------------------------------------------------------\r\n" +
                "CENTER\r\n" +
                "T TF01.CPF 0 0 758 Tasas para el Gobierno Autónomo Municipal\r\n" +
                "LEFT\r\n" +
                "T TF01.CPF 0 65 790 Tasa de Alumbrado Público\r\n" +
                "T TF01.CPF 0 65 815 Tasa de Aseo y Recojo de Basura\r\n" +
                "T TF01.CPF 0 65 845 Total Tasas para el Gobierno Municipal\r\n" +
                "T TF01.CPF 0 40 864 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 65 880 Importe Total Factura\r\n" +
                "T TF01.CPF 0 575 790 Bs\r\n" +
                "T TF01.CPF 0 575 815 Bs\r\n" +
                "T TF01.CPF 0 575 845 Bs\r\n" +
                "T TF01.CPF 0 575 880 Bs\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 790 " + StringUtils.roundTwoDigits(dataModel.getTlxImpTap()) + "\r\n" +
                "T TF01.CPF 0 720 815 " + StringUtils.roundTwoDigits(dataModel.getTlxImpAse()) + "\r\n" +
                "T TF01.CPF 0 720 845 " + StringUtils.roundTwoDigits(dataModel.getTlxImpAse() + dataModel.getTlxImpTap()) + "\r\n" +
                "T TF01.CPF 0 720 880 " + StringUtils.roundTwoDigits(dataModel.getTlxImpFac()) + "\r\n" +

                garantiaText +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 905 Son: " + NumberToLetterConverter.convertNumberToLetter(StringUtils.roundTwoDigits(dataModel.getTlxImpFac())) + " \r\n" +
                "T TF01.CPF 0 40 958 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T HF03.CPF 0 45 970 Importe del mes a cancelar Bs.\r\n" +
                "T HF04.CPF 0 45 1025 Fecha de disponibilidad en cobranza:  " + formatedDate(mas3dias(dataModel.getTlxFecEmi())) + "\r\n" +
                "T HF03.CPF 0 575 970 Bs\r\n" +

                "LINE 575 1003 800 1003 3\r\n" +

                "RIGHT 782\r\n" +
                "T HF03.CPF 0 720 970 " + StringUtils.roundTwoDigits(dataModel.getTlxImpMes()) + "\r\n" +

                "LEFT\r\n" +
                "T HF03.CPF 0 40 1065 Importe para crédito fiscal \r\n" +
                "T HF03.CPF 0 575 1065 Bs\r\n" +
                "LINE 575 1098 800 1098 3\r\n" +
                "RIGHT 782\r\n" +
                "T HF03.CPF 0 720 1065 " + StringUtils.roundTwoDigits(dataModel.getTlxImpSum()) + "\r\n" +

                "CENTER\r\n" +
                "T HF02.CPF 0 0 1115 DEUDA PENDIENTE DE PAGO\n\r\n" +
                "LEFT\r\n" +
                "T TF01.CPF 0 40 1105 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 1135 -------------------------------------------------------------------------------------------------------------\r\n" +

                deudasEnergia +

                //"LEFT\r\n" +
                //"LINE 575 1115 800 1110 3\r\n" +
                //"T HF04.CPF 0 40 1085 Importe total a cancelar Bs.\r\n" +
                //"T TF01.CPF 0 40 1145 Son: " + NumberToLetterConverter.convertNumberToLetter(StringUtils.roundTwoDigits(dataModel.getTlxImpTot())) + "\r\n" +
                //"RIGHT 782\r\n" +
                //"T TF01.CPF 0 720 1085 " + StringUtils.roundTwoDigits(dataModel.getTlxImpTot()) + "\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 1240 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T HF06_1.CPF 0 75 1260 HISTÓRICO\r\n" +
                "T HF06_1.CPF 0 45 1275 Mes/Año\r\n" +
                "T HF06_1.CPF 0 135 1275 ConsumokWh\r\n" +

                "T TF01.CPF 0 40 1300 " + dataModel.getTlxMes() + "/" + dataModel.getTlxAno() + "\r\n" +
                "T TF01.CPF 0 40 1320 " + (hist.getConMes12() == null ? "" : hist.getConMes12()) + "\r\n" +
                "T TF01.CPF 0 40 1340 " + (hist.getConMes11() == null ? "" : hist.getConMes11()) + "\r\n" +
                "T TF01.CPF 0 40 1360 " + (hist.getConMes10() == null ? "" : hist.getConMes10()) + "\r\n" +
                "T TF01.CPF 0 40 1380 " + (hist.getConMes09() == null ? "" : hist.getConMes09()) + "\r\n" +
                "T TF01.CPF 0 40 1400 " + (hist.getConMes08() == null ? "" : hist.getConMes08()) + "\r\n" +
                "T TF01.CPF 0 40 1420 " + (hist.getConMes07() == null ? "" : hist.getConMes07()) + "\r\n" +
                "T TF01.CPF 0 40 1440 " + (hist.getConMes06() == null ? "" : hist.getConMes06()) + "\r\n" +
                "T TF01.CPF 0 40 1460 " + (hist.getConMes05() == null ? "" : hist.getConMes05()) + "\r\n" +
                "T TF01.CPF 0 40 1480 " + (hist.getConMes04() == null ? "" : hist.getConMes04()) + "\r\n" +
                "T TF01.CPF 0 40 1500 " + (hist.getConMes03() == null ? "" : hist.getConMes03()) + "\r\n" +
                "T TF01.CPF 0 40 1520 " + (hist.getConMes02() == null ? "" : hist.getConMes02()) + "\r\n" +
                "T TF01.CPF 0 40 1540 " + (hist.getConMes01() == null ? "" : hist.getConMes01()) + "\r\n" +

                "T TF01.CPF 0 160 1300 " + pairDetalleConsumo.second + "\r\n" +
                "T TF01.CPF 0 160 1320 " + (hist.getConMes12() == null ? "" : hist.getConKwh12()) + "\r\n" +
                "T TF01.CPF 0 160 1340 " + (hist.getConMes11() == null ? "" : hist.getConKwh11()) + "\r\n" +
                "T TF01.CPF 0 160 1360 " + (hist.getConMes10() == null ? "" : hist.getConKwh10()) + "\r\n" +
                "T TF01.CPF 0 160 1380 " + (hist.getConMes09() == null ? "" : hist.getConKwh09()) + "\r\n" +
                "T TF01.CPF 0 160 1400 " + (hist.getConMes08() == null ? "" : hist.getConKwh08()) + "\r\n" +
                "T TF01.CPF 0 160 1420 " + (hist.getConMes07() == null ? "" : hist.getConKwh07()) + "\r\n" +
                "T TF01.CPF 0 160 1440 " + (hist.getConMes06() == null ? "" : hist.getConKwh06()) + "\r\n" +
                "T TF01.CPF 0 160 1460 " + (hist.getConMes05() == null ? "" : hist.getConKwh05()) + "\r\n" +
                "T TF01.CPF 0 160 1480 " + (hist.getConMes04() == null ? "" : hist.getConKwh04()) + "\r\n" +
                "T TF01.CPF 0 160 1500 " + (hist.getConMes03() == null ? "" : hist.getConKwh03()) + "\r\n" +
                "T TF01.CPF 0 160 1520 " + (hist.getConMes02() == null ? "" : hist.getConKwh02()) + "\r\n" +
                "T TF01.CPF 0 160 1540 " + (hist.getConMes01() == null ? "" : hist.getConKwh01()) + "\r\n" +

                "T TF01.CPF 0 260 1280 Fecha de Vencimiento: \r\n" +
                "T TF01.CPF 0 260 1305 Fecha estimada próxima medición: \r\n" +
                "T TF01.CPF 0 260 1330 Fecha estimada próxima emisión: \r\n" +

                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 1280 " + formatedDate(dataModel.getTlxFecVto()) + "\r\n" +
                "T TF01.CPF 0 720 1305 " + formatedDate(dataModel.getTlxFecproMed()) + "\r\n" +
                "T TF01.CPF 0 720 1330 " + formatedDate(dataModel.getTlxFecproEmi()) + "\r\n" +

                "LEFT\r\n" +
                "T HF05.CPF 0 260 1370 " + consejo + "\r\n" +
                "T TF03.CPF 0 260 1420 " + leyenda[0] + "\r\n" +
                "T TF03.CPF 0 260 1450 " + leyenda[1] + "\r\n" +
                "T TF03.CPF 0 260 1480 " + leyenda[2] + "\r\n" +
                "T TF03.CPF 0 260 1510 " + leyenda[3] + "\r\n" +
                "T TF01.CPF 0 40 1560 -------------------------------------------------------------------------------------------------------------\r\n" +
                "";

        cpclConfigLabel += "" +
                "PRINT\r\n";

        Log.e(TAG, "creator: " + cpclConfigLabel);
        return cpclConfigLabel;
    }

    private static Pair<String, Integer> detalleConsumo(DataModel dataModel) {
        String res = "";
        int offsetX = 450;
        if (dataModel.getTlxKwhDev2() > 0 && dataModel.getTlxTipLec() != 3 && dataModel.getTlxTipLec() != 6 && dataModel.getTlxTipLec() != 9) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T TF01.CPF 0 40 " + offsetX + " kWh a devolver\r\n" +
                    "RIGHT 782\r\n" +
                    "T TF01.CPF 0 700 " + offsetX + " " + dataModel.getTlxKwhDev2() + " kWh\r\n" +
                    subCuadros(offsetX) +
                    "";
        }
        if (dataModel.getTlxKwhAdi2() > 0) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T TF01.CPF 0 40 " + offsetX + " kWh a adicionar\r\n" +
                    "RIGHT 782\r\n" +
                    "T TF01.CPF 0 700 " + offsetX + " " + dataModel.getTlxKwhAdi2() + " kWh\r\n" +
                    subCuadros(offsetX) +
                    "";
        }

        offsetX += 30;
        res += "LEFT\r\n" +
                "T TF01.CPF 0 40 " + offsetX + " Total Energía a Facurar\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 700 " + offsetX + " " + dataModel.getTlxConsFacturado() + " kWh\r\n" +
                subCuadros(offsetX) +
                "";

        if (dataModel.getTlxTipDem() == 2) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T TF01.CPF 0 40 " + offsetX + " Potencia Leida\r\n" +
                    "RIGHT 782\r\n" +
                    "T TF01.CPF 0 700 " + offsetX + " " + dataModel.getTlxPotLei() + " kWh\r\n" +
                    subCuadros(offsetX) +
                    "";
        }
        if (dataModel.getTlxPotFac() == 2) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T TF01.CPF 0 40 " + offsetX + " Potencia Facturada\r\n" +
                    "RIGHT 782\r\n" +
                    "T TF01.CPF 0 700 " + offsetX + " " + dataModel.getTlxPotFac() + " kWh\r\n" +
                    subCuadros(offsetX) +
                    "";
        }
        return Pair.create(res, dataModel.getTlxConsFacturado());
    }

    private static String subCuadros(int offsetX){
        return "LEFT\r\n" +
                "LINE  35 " + (offsetX - 5) + "  35 " + (offsetX + 25) + " 1\r\n" +
                "LINE 787 " + (offsetX - 5) + " 787 " + (offsetX + 25) + " 1\r\n" +
                "LINE 595 " + (offsetX - 5) + " 595 " + (offsetX + 25) + " 1\r\n" +
                "LINE  35 " + (offsetX + 25) + " 787 " + (offsetX + 25) + " 1\r\n" +
                "";
    }

    public static String mas3dias(String fecha) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(StringUtils.formateStringFromDate(StringUtils.DATE_FORMAT, fecha));
        instance.add(Calendar.DATE, 3);
        return StringUtils.formateDateFromstring(StringUtils.DATE_FORMAT, instance.getTime());
    }

    public static String cuadros() {
        return "LEFT\r\n" +
                // lineas horiontales
                "LINE 35 355 787 355 1\r\n" +
                "LINE 35 385 787 385 1\r\n" +
                "LINE 35 415 787 415 1\r\n" +
                "LINE 35 445 787 445 1\r\n" +
                "LINE 35 475 787 475 1\r\n" +

                // lineas vertiales
                "LINE  35 355  35 475 1\r\n" +
                "LINE 260 355 260 445 1\r\n" +
                "LINE 440 355 440 445 1\r\n" +
                "LINE 595 355 595 445 1\r\n" +
                "LINE 787 355 787 475 1\r\n" +
                "";
    }
}
