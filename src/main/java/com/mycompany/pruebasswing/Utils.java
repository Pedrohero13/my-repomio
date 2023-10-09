/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pruebasswing;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author USER
 */
public class Utils {

    public static String logString(String mensaje, String contexto, String level) {
        StringBuilder sb = new StringBuilder();
        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss z");

        String date = dateFormat.format(new Date());

        sb.append(date)
                .append(" ")
                .append(contexto)
                .append(" ");
        switch (level) {
            case "info":
                sb.append("INFORMACION: ");
                break;
            case "error":
                sb.append("ERROR: ");
                break;
            case "warn":
                sb.append("ADVERTENCIA: ");
                break;
        }
        sb.append(mensaje);
        return sb.toString();
    }
}
