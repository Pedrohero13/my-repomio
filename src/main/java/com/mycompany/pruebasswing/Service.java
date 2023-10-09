/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pruebasswing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

/**
 *
 * @author USER
 */
public class Service {

    public Map<String, List<TableEmail>> getData(File fileExcel, File directory) {
        Map<String, List<TableEmail>> respuesta = new HashMap<>();
        try (ReadableWorkbook wb = new ReadableWorkbook(fileExcel)) {

            wb.getSheets().forEach(sheet
                    -> {
                try (Stream<Row> rows = sheet.openStream()) {

                    rows.skip(1).forEach(r -> {
                        TableEmail email = new TableEmail();
                        email.setEquipo(r.getCellAsNumber(0).orElse(null).intValue());
                        email.setAlias(r.getCellAsString(2).orElse(null));
                        email.setFlotaSinPlataforma(r.getCellAsString(6).orElse(null));
                        email.setPlataforma(r.getCellAsString(7).orElse(null));
                        try (ReadableWorkbook wbDir = new ReadableWorkbook(directory)) {

                            wbDir.getSheets().forEach(sheetDir -> {

                                try (Stream<Row> rowsDir = sheetDir.openStream()) {
                                    rowsDir.skip(1).forEach(rDir -> {

                                        if (rDir.getCellAsString(0) != null && rDir.getCellAsString(0).orElse(null).equals(email.getFlotaSinPlataforma())) {
                                            email.setEmailContacto(rDir.getCellAsString(1).orElse(null));

                                        }

                                    });

                                } catch (IOException ex) {
                                    Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            });

                        } catch (IOException ex) {
                            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        respuesta.computeIfAbsent(email.getFlotaSinPlataforma(), k -> new ArrayList<>()).add(email);

                    });

                } catch (IOException ex) {
                    Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
                }

            });

        } catch (IOException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }

        return respuesta;

    }

    public void sendMassiveEmials(Map<String, List<TableEmail>> clientes) {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(Constants.NAME_CONF_PROPERTIES)) {

            Properties props = new Properties();
            props.load(inputStream);
            int i = 0;
            for (Map.Entry<String, List<TableEmail>> entry : clientes.entrySet()) {

                if (i < 10) {
                    try {
                        sendEmail(entry.getValue(), props);
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    break;
                }
                
                i++;
            }
        
        } catch (IOException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendEmail(List<TableEmail> data, Properties props) {

        try {
            String body = createEmail(data);
            Session session = Session.getDefaultInstance(props);//Se instancia la session para poder crear el objeto mensaje
            MimeMessage message = new MimeMessage(session); //se crea el objeto mensaje que contendra todos los datos del correo a enviar

            //apartado de propiedades para la conexion hacia el servidor de smtp, se obtienen del archivo de conf.properties
            String user = (data.get(0).getPlataforma().equalsIgnoreCase(Constants.ENLACE))
                    ? props.getProperty("mail.smtp.user.enlace")
                    : props.getProperty("mail.smtp.user.soporte");
            String password = (data.get(0).getPlataforma().equalsIgnoreCase(Constants.ENLACE))
                    ? props.getProperty("mail.smtp.password.enlace")
                    : props.getProperty("mail.smtp.password.soporte");
            String host = props.getProperty("mail.smtp.host");
            message.setFrom(new InternetAddress(user)); // Se asigna quien es el que envia este esta guardado en el archivo de configuracion
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(data.get(0).getEmailContacto())); // se asigna a quien va dirigido en correo
            message.setSubject(Constants.SUBJECT); //Se asigna el asunto
            message.setContent(body, Constants.TYPE_CONTENT); // se asigna el cuerpo del correo y el tipo de correo que se enviara en este caso un html
            Transport transport = session.getTransport(Constants.TYPE_PROTOCOL); //aqui se crea un Trasport para enviar el mensaje se le asigna el typo de protocolo
            transport.connect(host, user, password);// aqui se conecta el transport a gmail con las propiedades correspondientes
            transport.sendMessage(message, message.getAllRecipients()); // se envia el mensaje cargando el objeto message
            transport.close(); //se cierra el transport
            MyFrame.getConsola().append(Utils.logString(Constants.INFO_FINISH_SEND_EMAIL +" a "+data.get(0).getEmailContacto(), "", "info") + "\n");
            Logger.getLogger(Service.class.getName()).log(Level.INFO, Constants.INFO_FINISH_SEND_EMAIL);
        } catch (AddressException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String createEmail(List<TableEmail> data) { // metodo que con el cual se crea el html (plantilla) que se va enviar.
        String mensaje = "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <title>Mi prueba</title>\n"
                + "    \n"
                + "</head>\n"
                + "<body>\n"
                + "\n"
                + "    <h3>Estimado cliente: " + data.get(0).getFlotaSinPlataforma() + "</h3>\n"
                + "\n"
                + "    <p>Nos ponemos en contacto con usted para solicitar \n"
                + "    información de las siguientes unidades MOTUM, ya que hemos detectado que se encuentran sin reporte desde hace algunos días.</p>\n"
                + "    <br>\n"
                + "    <table style=\"border-collapse: collapse; margin: 25px 0; font-size: 1em; font-family: sans-serif; min-width: 450px; box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);\" class=\"table\">\n"
                + "        <thead style=\"background-color: #094fff; color: #ffffff; text-align: middle;\">\n"
                + "          <tr style=\"background-color: #094fff; color: #ffffff; text-align: middle;\">\n"
                + "            <th style=\"padding: 12px 15px;\" scope=\"col\">Plataforma</th>\n"
                + "            <th scope=\"col\">Equipo</th>\n"
                + "            <th scope=\"col\">Alias</th>\n"
                + "            <th scope=\"col\">Flota sin plataforma</th>\n"
                + "            \n"
                + "          </tr>\n"
                + "        </thead>\n"
                + "        <tbody  style=\"border-bottom: 1px solid #dddddd;\" class=\"table-group-divider\">";

        for (TableEmail table : data) {
            mensaje += "<tr style=\"border-bottom: 1px solid #dddddd;\">\n"
                    + "      \n"
                    + "          <td>" + table.getPlataforma() + "</td>\n"
                    + "      \n"
                    + "          <td>" + table.getEquipo() + "</td>\n"
                    + "      \n"
                    + "          <td>" + table.getAlias() + "</td>\n"
                    + "          <td>" + table.getFlotaSinPlataforma() + "</td>\n"
                    + "      \n"
                    + "        </tr>";
        }

        mensaje += "        </tbody>\n"
                + "      </table>\n"
                + "      <br>\n"
                + "      <p>Agradecemos que nos pueda informar si se encuentran <b>taller, corralón, accidentadas, robadas, fuera de operación y/o ruta.</b> \n"
                + "        Para nosotros es importante nos comparta sus comentarios para poder tomar las medidas correspondientes que permiten que nuestros equipos reporten correctamente.\n"
                + "      </p>"
                + "       <br>\n"
                + "       <p>En espera de sus comentarios.</p>\n"
                + "      <hr>\n"
                + "      <p>Saludos.</p>\n"
                + "      \n"
                + "</body>\n"
                + "\n"
                + "</html>";

        return mensaje;
    }
}
