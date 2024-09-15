package ConnectionUtils;

import Data.Movie;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * Response - ответ сервера на запрос клиента, содержит результат выполнения команды
 */

public class Response implements Serializable {
    private static long serialVersionUID = 101l;
    private final String result;

    public Response(String result){
        this.result = result;
    }

    public String getResult(){
        return result;
    }
}
