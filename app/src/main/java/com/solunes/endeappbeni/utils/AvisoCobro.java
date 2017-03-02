package com.solunes.endeappbeni.utils;

import com.solunes.endeappbeni.models.DataModel;
import com.solunes.endeappbeni.models.Historico;

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
                                 Historico historico,
                                 double importeTotalFactura,
                                 double importeMesCancelar) {

        String deudasEnergia = "";

        if (dataModel.getTlxDeuEneC() > 0) {
            deudasEnergia = "LEFT\r\n" +
                    "T TF01.CPF 0 40 1120 Señor cliente usted es pasible de corte\r\n" +
                    "T TF01.CPF 0 40 1060 Más deuda(s) pendiente(s) de energía   (" + dataModel.getTlxDeuEneC() + ")  Bs\r\n";
            deudasEnergia += "RIGHT 782\r\n" +
                    "T TF01.CPF 0 720 1060 " + StringUtils.roundTwoDigits(dataModel.getTlxDeuEneI()) + "\r\n";
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
                "T TF01.CPF 0 575 290 Ruta: " + dataModel.getTlxRutO() + "\r\n" +

                "T TF01.CPF 0 40 315 Mes:\r\n" +
                "T HF03.CPF 0 300 305 " + mesString(dataModel.getTlxMes()).toUpperCase() + "-" + dataModel.getTlxAno() + "\r\n" +
                "T TF01.CPF 0 590 315 INDEFINIDO\r\n" +

                "T TF01.CPF 0 40 340 Categoria Tarifacria:\r\n" +
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

                "T TF01.CPF 0 40 450 Enegia Consumida en (" + calcDays(dataModel.getTlxFecAnt(), dataModel.getTlxFecLec()) + ") dias\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 700 450 " + dataModel.getTlxConsumo() + " kWh\r\n" +

                detalleConsumo(dataModel) +

                "CENTER\r\n" +
                "T HF02.CPF 0 0 540 DETALLE AVISO DE COBRANZA\n\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 530 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 560 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 65 575 Importe por Energia\r\n" +
                "T TF01.CPF 0 575 575 Bs\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 575 "+dataModel.getTlxImpEn()+"\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 65 680 Importe Total Suministro \r\n" +
                "T TF01.CPF 0 575 680 Bs\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 680 "+StringUtils.roundTwoDigits(dataModel.getTlxImpFac())+"\r\n" +

                "LEFT\r\n" +
                "LINE 65 760 800 760 1\r\n" +
                "LINE 65 840 800 840 1\r\n" +
                "T TF01.CPF 0 65 765 Tasas para el Gobierno Municipal\r\n" +
                "T TF01.CPF 0 65 790 Tasa de Alumbrado Publico\r\n" +
                "T TF01.CPF 0 65 815 Tasa de Aseo y Recojo de Basura\r\n" +
                "T TF01.CPF 0 65 845 Importe Total Factura\r\n" +
                "T TF01.CPF 0 575 790 Bs\r\n" +
                "T TF01.CPF 0 575 815 Bs\r\n" +
                "T TF01.CPF 0 575 845 Bs\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 790 "+StringUtils.roundTwoDigits(dataModel.getTlxImpTap())+"\r\n" +
                "T TF01.CPF 0 720 815 "+StringUtils.roundTwoDigits(dataModel.getTlxImpAse())+"\r\n" +
                "T TF01.CPF 0 720 845 "+StringUtils.roundTwoDigits(importeTotalFactura)+"\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 870 Son: " + NumberToLetterConverter.convertNumberToLetter(StringUtils.roundTwoDigits(importeTotalFactura)) + " \r\n" +

                "T HF04.CPF 0 45 970 Importe del mes a cancelar Bs.\r\n" +
                "T HF04.CPF 0 575 970 Bs\r\n" +

                "LINE 575 1000 800 1000 3\r\n" +

                "RIGHT 782\r\n" +
                "T HF04.CPF 0 720 970 " + StringUtils.roundTwoDigits(importeMesCancelar) + "\r\n" +

                "CENTER\r\n" +
                "T HF02.CPF 0 0 1015 DEUDA PENDIENTE DE PAGO\n\r\n" +

                deudasEnergia +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 1005 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 1035 -------------------------------------------------------------------------------------------------------------\r\n" +
                "LINE 575 1115 800 1115 3\r\n" +
                "T TF01.CPF 0 40 1090 Importe total a cancelar Bs.\r\n" +
                "T TF01.CPF 0 40 1145 Son: "+ NumberToLetterConverter.convertNumberToLetter(StringUtils.roundTwoDigits(dataModel.getTlxImpTot())) +"\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 1090 " + StringUtils.roundTwoDigits(dataModel.getTlxImpTot()) + "\r\n" +

                "LEFT\r\n" +
                "T TF01.CPF 0 40 1220 Importe para credito fiscal Bs\r\n" +
                "T TF01.CPF 0 40 1195 -------------------------------------------------------------------------------------------------------------\r\n" +
                "T TF01.CPF 0 40 1240 -------------------------------------------------------------------------------------------------------------\r\n" +
                "LINE 575 1245 800 1245 3\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 1220 " + StringUtils.roundTwoDigits(dataModel.getTlxImpFac()) + "\r\n" +

                "LEFT\r\n" +
                "T HF06_1.CPF 0 75 1260 HISTORICO\r\n" +
                "T HF06_1.CPF 0 45 1275 Mes/Año\r\n" +
                "T HF06_1.CPF 0 135 1275 ConsumokWh\r\n" +

                "T TF01.CPF 0 40 1300 " + historico.getConMes01() + "\r\n" +
                "T TF01.CPF 0 40 1320 " + historico.getConMes02() + "\r\n" +
                "T TF01.CPF 0 40 1340 " + historico.getConMes03() + "\r\n" +
                "T TF01.CPF 0 40 1360 " + historico.getConMes04() + "\r\n" +
                "T TF01.CPF 0 40 1380 " + historico.getConMes05() + "\r\n" +
                "T TF01.CPF 0 40 1400 " + historico.getConMes06() + "\r\n" +
                "T TF01.CPF 0 40 1420 " + historico.getConMes07() + "\r\n" +
                "T TF01.CPF 0 40 1440 " + historico.getConMes08() + "\r\n" +
                "T TF01.CPF 0 40 1460 " + historico.getConMes09() + "\r\n" +
                "T TF01.CPF 0 40 1480 " + historico.getConMes10() + "\r\n" +
                "T TF01.CPF 0 40 1500 " + historico.getConMes11() + "\r\n" +
                "T TF01.CPF 0 40 1520 " + historico.getConMes12() + "\r\n" +
                "T TF01.CPF 0 40 1540 " + historico.getConMes12() + "\r\n" +

                "T TF01.CPF 0 160 1300 " + historico.getConKwh01() + "\r\n" +
                "T TF01.CPF 0 160 1320 " + historico.getConKwh02() + "\r\n" +
                "T TF01.CPF 0 160 1340 " + historico.getConKwh03() + "\r\n" +
                "T TF01.CPF 0 160 1360 " + historico.getConKwh04() + "\r\n" +
                "T TF01.CPF 0 160 1380 " + historico.getConKwh05() + "\r\n" +
                "T TF01.CPF 0 160 1400 " + historico.getConKwh06() + "\r\n" +
                "T TF01.CPF 0 160 1420 " + historico.getConKwh07() + "\r\n" +
                "T TF01.CPF 0 160 1440 " + historico.getConKwh08() + "\r\n" +
                "T TF01.CPF 0 160 1460 " + historico.getConKwh09() + "\r\n" +
                "T TF01.CPF 0 160 1480 " + historico.getConKwh10() + "\r\n" +
                "T TF01.CPF 0 160 1500 " + historico.getConKwh11() + "\r\n" +
                "T TF01.CPF 0 160 1520 " + historico.getConKwh12() + "\r\n" +
                "T TF01.CPF 0 160 1540 " + historico.getConKwh12() + "\r\n" +

                "T TF01.CPF 0 260 1280 Fecha de Vencimiento: \r\n" +
                "T TF01.CPF 0 260 1305 Fecha estimada proxima medicion: \r\n" +
                "T TF01.CPF 0 260 1330 Fecha estimada proxima emision: \r\n" +

                "RIGHT 782\r\n" +
                "T TF01.CPF 0 720 1280 " + dataModel.getTlxFecVto() + "\r\n" +
                "T TF01.CPF 0 720 1305 " + dataModel.getTlxFecproMed() + "\r\n" +
                "T TF01.CPF 0 720 1330 " + dataModel.getTlxFecproEmi() + "\r\n" +

                "LEFT\r\n" +
                "T HF05.CPF 0 260 1370 Señor (a) cliente: 'LUZ QUE APAGA LUZ QUE NO PAGA'\r\n" +
                "T TF03.CPF 0 260 1420 LA FALTA DE PAGO EN TERMINO DE DOS\r\n" +
                "T TF03.CPF 0 260 1450 FACTURAS DARA LUGAR AL CORTE DE\r\n" +
                "T TF03.CPF 0 260 1480 SERVICIO EVÍTESE MOLESTIAS PAGUE\r\n" +
                "T TF03.CPF 0 260 1510 SUS FACTURAS A TIEMPO\r\n" +
                "T TF01.CPF 0 40 1560 -------------------------------------------------------------------------------------------------------------\r\n" +
                "";

        cpclConfigLabel += "" +
                "PRINT\r\n";

        return cpclConfigLabel;
    }

    private static String detalleConsumo(DataModel dataModel) {
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
                "T TF01.CPF 0 40 " + offsetX + " Total Energia a Facurar\r\n" +
                "RIGHT 782\r\n" +
                "T TF01.CPF 0 700 " + offsetX + " " + total + " kWh\r\n";
        return res;
    }
}
