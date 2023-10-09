/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pruebasswing;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author USER
 */
@Getter
@Setter
public class SendEmails extends Thread {

    private Map<String, List<TableEmail>> clientes = null;
    private Service service = null;
    @Override
    public void run() {
        service.sendMassiveEmials(clientes);
        
    }

}
