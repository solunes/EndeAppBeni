package com.solunes.endeappbeni.utils;

import android.util.Log;
import android.util.Pair;

import com.solunes.endeappbeni.models.DataModel;
import com.solunes.endeappbeni.models.Historico;

import java.util.ArrayList;

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
                    "T TF01.CPF 0 40 1120 Señor cliente usted es pasible de corte\r\n" +
                    "T TF01.CPF 0 40 1060 Más deuda(s) pendiente(s) de energía   (" + dataModel.getTlxDeuEneC() + ")  Bs\r\n";
            deudasEnergia += "RIGHT 782\r\n" +
                    "T TF01.CPF 0 720 1060 " + StringUtils.roundTwoDigits(dataModel.getTlxDeuEneI()) + "\r\n";
        }

        String garantiaText = "";
        if (garantia > 0) {
            garantiaText = "LEFT\r\n" +
                    "T HF04.CPF 0 65 870 " + garantiaString + "\r\n" +
                    "T HF04.CPF 0 575 870 Bs\r\n" +
                    "RIGHT 782\r\n" +
                    "T HF04.CPF 0 720 870 " + garantia + "\r\n";
        }

        String detalleFacturacion = "";
//        String[] strings = (String[]) list.keySet().toArray();
        int yValue = 575;
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
                "T HF02.CPF 0 590 0 AVISO DE\r\n" +
                "T HF02.CPF 0 570 25 COBRANZA\r\n" +
                "T HF01_4.CPF 0 70 10 ENDE - DELBENI\r\n" +

                "CENTER\r\n" +
                "T HF02.CPF 0 40 80 DATOS DEL CONSUMIDOR\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 70 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 100 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 120 Fecha Emisión: \r\n" +
                "CENTER\r\n" +
                "T TF01.CPF 0 50 120 " + dataModel.getTlxCiudad() + " " + getFechaEmi(dataModel.getTlxFecEmi()) + " \r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 150 Señor (a):  " + dataModel.getTlxNom() + "\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 205 NIT/CI:\r\n" +
                "T TF01.CPF 0 280 205 N° Cuenta:\r\n" +
                "T TF01.CPF 0 575 205 N° Medidor:\r\n" +
                "RIGHT 100\r\n" +
                "T TF01.CPF 0 130 205 " + dataModel.getTlxCliNit() + "\r\n" +
                "T HF03.CPF 0 405 200 " + dataModel.getTlxCli() + "\r\n" +
                "T TF01.CPF 0 720 205 " + dataModel.getTlxNroMed() + "\r\n" +


                "LEFT\r\n" +
                "T TF01.CPF 0 40 240 Dirección:  " + dataModel.getTlxDir() + "\r\n" +

                "T TF01.CPF 0 30 270 -------------------------------------------------------------------------------------------------------------\r\n" +

                // BLOQUE 2: DATOS DE MEDICION
                "T TF01.CPF 0 40 290 Ciudad o Localidad:  " + dataModel.getTlxCiudad() + "\r\n" +
                "T TF01.CPF 0 575 290 Ruta: " + dataModel.getTlxRem() + "-" + dataModel.getTlxRutO() + "\r\n" +

                "T TF01.CPF 0 40 315 Mes:\r\n" +
                "T HF03.CPF 0 300 305 " + mesString(dataModel.getTlxMes()).toUpperCase() + "-" + dataModel.getTlxAno() + "\r\n" +
                "T TF01.CPF 0 590 315 " + dataModel.getTlxCtaAnt() + "\r\n" +

                "T TF01.CPF 0 40 340 Categoría Tarifaria:\r\n" +
                "T TF01.CPF 0 300 340 " + dataModel.getTlxActivi() + "\r\n" +

                "T TF01.CPF 0 320 360 Anterior\r\n" +
                "T TF01.CPF 0 500 360 Actual\r\n" +
                "T TF01.CPF 0 640 360 Tipo Lectura\r\n" +

                "T TF01.CPF 0 40 390 Fecha Lectura\r\n" +
                "T TF01.CPF 0 300 390 " + formatedDate(dataModel.getTlxFecAnt()) + "\r\n" +
                "T TF01.CPF 0 490 390 " + formatedDate(dataModel.getTlxFecLec()) + "\r\n" +
                "T TF01.CPF 0 620 390 " + DataModel.getTipoLectura(dataModel.getTlxTipLec()) + "\r\n" +

                "T TF01.CPF 0 40 420 Lectura Medidor\r\n" +
                "T TF01.CPF 0 330 420 " + dataModel.getTlxUltInd() + "\r\n" +
                "T TF01.CPF 0 510 420 " + dataModel.getTlxNvaLec() + "\r\n" +

                "T TF01.CPF 0 40 450 Energía Consumida en (" + calcDays(dataModel.getTlxFecAnt(), dataModel.getTlxFecLec()) + ") dias\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 700 450 " + dataModel.getTlxConsumo() + " kWh\r\n" +

                pairDetalleConsumo.first +

                "CENTER\r\n" +
                "T HF02.CPF 0 0 540 DETALLE AVISO DE COBRANZA\n\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 530 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 560 -------------------------------------------------------------------------------------------------------------\r\n" +

                detalleFacturacion +

                "LEFT\r\n" +
                "LINE 65 760 800 760 1\r\n" +
                "LINE 65 840 800 840 1\r\n" +
                "T TF01.CPF 0 65 765 Tasas para el Gobierno Municipal\r\n" +
                "T TF01.CPF 0 65 790 Tasa de Alumbrado Público\r\n" +
                "T TF01.CPF 0 65 815 Tasa de Aseo y Recojo de Basura\r\n" +
                "T TF01.CPF 0 65 845 Importe Total Factura\r\n" +
                "T TF01.CPF 0 575 790 Bs\r\n" +
                "T TF01.CPF 0 575 815 Bs\r\n" +
                "T TF01.CPF 0 575 845 Bs\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 790 " + StringUtils.roundTwoDigits(dataModel.getTlxImpTap()) + "\r\n" +
                "T TF01.CPF 0 720 815 " + StringUtils.roundTwoDigits(dataModel.getTlxImpAse()) + "\r\n" +
                "T TF01.CPF 0 720 845 " + StringUtils.roundTwoDigits(dataModel.getTlxImpFac()) + "\r\n" +

                garantiaText +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 940 Son: " + NumberToLetterConverter.convertNumberToLetter(StringUtils.roundTwoDigits(dataModel.getTlxImpFac())) + " \r\n" +
                "T HF04.CPF 0 45 970 Importe del mes a cancelar Bs.\r\n" +
                "T HF04.CPF 0 575 970 Bs\r\n" +

                "LINE 575 1000 800 1000 3\r\n" +

                "RIGHT 782\r\n" +
                "T HF04.CPF 0 720 970 " + StringUtils.roundTwoDigits(dataModel.getTlxImpMes()) + "\r\n" +

                "CENTER\r\n" +
                "T HF02.CPF 0 0 1015 DEUDA PENDIENTE DE PAGO\n\r\n" +

                deudasEnergia +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 1005 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 1035 -------------------------------------------------------------------------------------------------------------\r\n" +
                "LINE 575 1115 800 1115 3\r\n" +
                "T TF01.CPF 0 40 1090 Importe total a cancelar Bs.\r\n" +
                "T TF01.CPF 0 40 1145 Son: " + NumberToLetterConverter.convertNumberToLetter(StringUtils.roundTwoDigits(dataModel.getTlxImpTot())) + "\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 1090 " + StringUtils.roundTwoDigits(dataModel.getTlxImpTot()) + "\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 1220 Importe para crédito fiscal Bs\r\n" +
                "T TF01.CPF 0 40 1195 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 1240 -------------------------------------------------------------------------------------------------------------\r\n" +
                "LINE 575 1245 800 1245 3\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 1220 " + StringUtils.roundTwoDigits(dataModel.getTlxImpSum()) + "\r\n" +

                "LEFT\r\n" +
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
                "T TF01.CPF 0 720 1280 " + dataModel.getTlxFecVto() + "\r\n" +
                "T TF01.CPF 0 720 1305 " + dataModel.getTlxFecproMed() + "\r\n" +
                "T TF01.CPF 0 720 1330 " + dataModel.getTlxFecproEmi() + "\r\n" +

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

        return cpclConfigLabel;
    }

    private static Pair<String, Integer> detalleConsumo(DataModel dataModel) {
        String res = "";
        int offsetX = 450;
        int total = dataModel.getTlxConsumo();
        if (dataModel.getTlxKwhDev() > 0 && dataModel.getTlxTipLec() != 3 && dataModel.getTlxTipLec() != 6 && dataModel.getTlxTipLec() != 9) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T CONSO2.CPF 0 40 " + offsetX + " kWh a devolver\r\n" +
                    "RIGHT 782\r\n" +
                    "T CONSO2.CPF 0 700 " + offsetX + " " + dataModel.getTlxKwhDev() + " kWh\r\n";
            total += dataModel.getTlxKwhDev();
        }
        if (dataModel.getTlxKwhAdi() > 0) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T CONSO2.CPF 0 40 " + offsetX + " kWh a adicionar\r\n" +
                    "RIGHT 782\r\n" +
                    "T CONSO2.CPF 0 700 " + offsetX + " " + dataModel.getTlxKwhAdi() + " kWh\r\n";
            total += dataModel.getTlxKwhAdi();
        }
        if (dataModel.getTlxTipDem() == 2) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T CONSO2.CPF 0 40 " + offsetX + " Potencia Leida\r\n" +
                    "RIGHT 782\r\n" +
                    "T CONSO2.CPF 0 700 " + offsetX + " " + dataModel.getTlxPotLei() + " kWh\r\n";
            total += dataModel.getTlxPotLei();
        }
        if (dataModel.getTlxPotFac() == 2) {
            offsetX += 30;
            res += "LEFT\r\n" +
                    "T CONSO2.CPF 0 40 " + offsetX + " Potencia Facturada\r\n" +
                    "RIGHT 782\r\n" +
                    "T CONSO2.CPF 0 700 " + offsetX + " " + dataModel.getTlxPotFac() + " kWh\r\n";
            total += dataModel.getTlxPotFac();
        }

        offsetX += 30;
        res += "LEFT\r\n" +
                "T TF01.CPF 0 40 " + offsetX + " Total Energía a Facurar\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 700 " + offsetX + " " + total + " kWh\r\n";
        return Pair.create(res, total);
    }
}
