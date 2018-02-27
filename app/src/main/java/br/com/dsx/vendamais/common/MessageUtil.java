package br.com.dsx.vendamais.common;

import java.util.List;

/**
 * Created by salazar on 05/05/17.
 */

public class MessageUtil {

    public static boolean onlySucess(List<Message> messages){
        for (Message msg: messages){
            if (msg.getType()==Message.Type.ERROR || msg.getType()==Message.Type.BUSINESS) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasError(List<Message> messages){
        for (Message msg: messages){
            if (msg.getType()==Message.Type.ERROR) {
                return true;
            }
        }
        return false;
    }
}
