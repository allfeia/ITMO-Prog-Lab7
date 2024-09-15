package ConnectionUtils;


import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * Класс отправитель
 * Содержит всю логику соединения с сервером, он отправляет запрос и он же получает на него ответ
 */

public class Sender {
    private String host;
    private int port;
    private int reconnectionTimeout;
    private int maxReconnectionAttempts;

    private SocketChannel socketChannel;
    private ObjectOutputStream serverWriter;
    private ObjectInputStream serverReader;
    private InetSocketAddress serverAddress;
    private SocketChannel socket;

    /**
     * Конструктор устанавливает параметры соединения.
     *
     * @param host                    хост сервера
     * @param port                    порт сервера
     * @param reconnectionTimeout     время ожидания перед повторной попыткой соединения
     * @param maxReconnectionAttempts максимальное количество попыток повторного соединения
     */

    public Sender(String host, int port, int reconnectionTimeout, int maxReconnectionAttempts) {
        this.host = host;
        this.port = port;
        this.reconnectionTimeout = reconnectionTimeout;
        this.maxReconnectionAttempts = maxReconnectionAttempts;

        this.serverAddress = new InetSocketAddress(host, port);
        this.connect();
    }

    /**
     * Отправляет запрос на сервер и получает ответ.
     * В случае ошибки пытается повторно подключиться заданное количество раз.
     *
     * @param request запрос, который нужно отправить
     * @return Response ответ от сервера
     * @throws InterruptedException в случае прерывания потока при ожидании повторного соединения
     */

    public void sendRequest(Request request) throws InterruptedException {
        int reconnectionAttempts = 0;
        try {
            if (!socket.isConnected()){
                connect();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(request);

            objectOutputStream.close();
            ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            socket.write(buffer);

            System.out.println("Запрос отправлен на сервер");
        } catch (IOException ignored) {
            reconnectionAttempts++;
            if (reconnectionAttempts >= maxReconnectionAttempts) {
                ignored.printStackTrace();

            }

            // Вывод сообщения о следующей попытке подключения
            System.err.println("Возникла ошибка подключения, попробуйте позже");
            // Ожидание перед следующей попыткой подключения
            // Thread.sleep(reconnectionTimeout);
            // this.connect();
        }

    }

    public Response getResponse() throws ClassNotFoundException {
        int reconnectionAttempts = 0;
        try {
            // Чтение ответа от сервера
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            socket.read(buffer);

            ByteArrayInputStream bi = new ByteArrayInputStream(buffer.array());
            ObjectInputStream oi = new ObjectInputStream(bi);
            Response response = (Response) oi.readObject();
            if (response.getResult().contains("токен") || response.getResult().contains("token")){
                Client.token = response.getResult().split(" ")[1];
            } else if (response.getResult().contains("отключен")) {
                Client.token = "";
                Client.isConnected = false;
            } else if (response.getResult().contains("вошли") || response.getResult().contains("зарегистрировались")) {
                Client.isConnected = true;
            }
            return response;
        } catch (IOException ignored) {
            reconnectionAttempts++;
            if (reconnectionAttempts >= maxReconnectionAttempts) {
                ignored.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Устанавливает соединение с сервером.
     */

    public void connect() {
        try {
            // socketChannel = SocketChannel.open();
            // socketChannel.connect(new InetSocketAddress(host, port));
            // serverWriter = new ObjectOutputStream(socketChannel.socket().getOutputStream());
            socket = SocketChannel.open();
            socket.connect(serverAddress);
            // serverReader = new ObjectInputStream(socketChannel.socket().getInputStream());
            System.out.println("Connect");
        } catch (IOException e) {
            System.err.println("Error with connection to server: " + e.getMessage());
        }
    }

    /**
     * Разрывает соединение с сервером и закрывает все потоки.
     */

    public void disconnect() {
        try {
            // socket.close();
            serverReader.close();
            serverWriter.close();
        } catch (IOException e) {
            System.err.println("Doesn't connect to server");
            ;
        }
    }
}
