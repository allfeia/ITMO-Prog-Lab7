package Utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;

import static java.lang.Thread.sleep;


public class Server {
    private int port;
    private ServerSocketChannel ss;
    private BufferedReader scanner;
    private static CollectionManager collectionManager;
    private InetSocketAddress address;
    private ServerSocketChannel chanel;
    private Selector selector;
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    private HashMap<String, User> connections = new HashMap<>();

    public Server(int port, CollectionManager collectionManager) throws IOException {
        this.port = port;
        this.collectionManager = collectionManager;

        BufferedInputStream bf = new BufferedInputStream(System.in);
        this.scanner = new BufferedReader(new InputStreamReader(bf));

        this.address = new InetSocketAddress(port); // создаем адрес сокета (IP-адрес и порт)
        this.chanel = ServerSocketChannel.open();
        this.chanel.bind(address);
        this.chanel.configureBlocking(false); // неблокирующий режим ввода-вывода
        this.selector = Selector.open();
    }

    public void run() {
        new Thread(() -> {
            while (true) {
                try {
                    // Получаем текущее время
                    LocalDateTime now = LocalDateTime.now();
                    String disconect = "";
                    // Итерируем по ключам
                    Iterator<Map.Entry<String, User>> iterator = connections.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, User> entry = iterator.next();
                        // Проверяем, прошло ли 2 минуты с момента добавления ключа
                        if (now.isAfter(entry.getValue().getTimeOflastRequest().plusMinutes(2).plusSeconds(30))) {
                            Response response = new Response("Вы были отключены из-за не активности.");
                            try {
                                if (entry.getValue().getClient().isOpen()) {
                                    disconect += entry.getKey().toString() + "\n";
                                    sendResponse(entry.getValue().getClient(), response);
                                }
                            } catch (IOException | ClassCastException e) {
                                //
                            }
                            // Выводим уведомление перед удалением
                            ServerLogger.getLogger().info("Отключение пользователя: " + entry.getValue().getClient());
                            ServerLogger.getLogger().info("Инвалидация ключа: " + entry.getKey());
                            iterator.remove(); // Удаляем элемент из итератора
                            entry.getValue().getClient().close();
                        }
                    }
                    if (!disconect.isEmpty()) {
                        for (String key : connections.keySet()) {
                            Response response = new Response("Были отключены пользователи:" + disconect);
                            try {
                                sendResponse(connections.get(key).getClient(), response);
                            } catch (Exception e) {

                            }
                        }
                    }
                    sleep(1000);
                } catch (InterruptedException e) {

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            try {
                chanel.register(selector, SelectionKey.OP_ACCEPT);
                while (true) {
                    selector.select(); // Количество ключей, чьи каналы готовы к операции. БЛОКИРУЕТ, ПОКА НЕ БУДЕТ КЛЮЧЕЙ
                    Set<SelectionKey> selectedKeys = selector.selectedKeys(); // получаем список ключей от каналов, готовых к работеwhile (iter.hasNext()) {
                    Iterator<SelectionKey> iter = selectedKeys.iterator(); // получаем итератор ключей
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        try {
                            if (key.isAcceptable()) {
                                handleAccept(key, selector);
                            } else if (key.isReadable()) {
                                Thread readThread = new Thread(() -> {
                                    try {
                                        handleRead(key, selector);
                                    } catch (IOException | ClassNotFoundException  e) {
                                        // ...
                                    }
                                });
                                readThread.start();

                                Runnable task = () -> {
                                    for (String token : connections.keySet()) {
                                        if (connections.get(token).getClient().equals((SocketChannel) key.channel())) {
                                            connections.get(token).setTimeOflastRequest(LocalDateTime.now());
                                        }
                                    }
                                };
                                executor.execute(task);
                            }
                            iter.remove();

                        } catch (IOException e) {
                            if (e.getMessage().equals("Connection reset")) {
                                SocketChannel client = (SocketChannel) key.channel(); // получаем канал для работы
                                ServerLogger.getLogger().info("The connection was reset by the client: " + client.socket());
                                key.cancel();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                run();
            }
        }).start();
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            // Регистрируем клиентский канал для чтения
            clientChannel.register(selector, SelectionKey.OP_READ);
            ServerLogger.getLogger().info("Новое подключение от " + clientChannel.getRemoteAddress());

            // 1. Генерация токена для пользователя
            String token = generateToken(); // Метод для генерации уникального токена
            // 2. Создание объекта User с токеном
            User user = new User(clientChannel);
            // 3. Добавление пользователя в список подключений
            connections.put(token, user);
            // 4. Отправка токена пользователю
            sendResponse(clientChannel, new Response("Ваш токен: " + token));

            for (String key1 : connections.keySet()) {
                if (!key1.equals(token)) {
                    Response response = new Response("Новое подключение:\n" + key);
                    try {
                        sendResponse(connections.get(key1).getClient(), response);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            connections.put(token, new User(clientChannel).setTimeOflastRequest(LocalDateTime.now()));
        } catch (Exception e) {
            // System.out.println(e.getMessage());
        }

    }

    private String generateToken() {
        // Реализация генерации уникального токена
        return java.util.UUID.randomUUID().toString();
    }

    private class User {
        private SocketChannel client;
        private LocalDateTime timeOflastRequest;
        public String username;
        public String password;

        public User(SocketChannel client) {
            this.client = client;
            this.timeOflastRequest = LocalDateTime.now();
        }

        public SocketChannel getClient() {
            return client;
        }

        public void setClient(SocketChannel client) {
            this.client = client;
        }

        public LocalDateTime getTimeOflastRequest() {
            return timeOflastRequest;
        }

        public User setTimeOflastRequest(LocalDateTime timeOflastRequest) {
            this.timeOflastRequest = timeOflastRequest;
            return this;
        }

    }

    // Обработка события READ (получение данных)
    private void handleRead(SelectionKey key, Selector selector) throws IOException, ClassNotFoundException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        // Читаем данные от клиента
        ByteBuffer buffer = ByteBuffer.allocate(5000);
        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();

                ByteArrayInputStream bi = new ByteArrayInputStream(buffer.array());
                ObjectInputStream oi = new ObjectInputStream(bi);
                Request request = (Request) oi.readObject();

                ServerLogger.getLogger().info("Получено сообщение от клиента: " + request.getCommand().getName());

                bi.close();
                oi.close();

                forkJoinPool.execute(() -> {
                    Response response = new Response("101");
                    for (String key1 : connections.keySet()) {
                        if (connections.get(key1).getClient().equals(clientChannel)) {
                            if (request.getCommand().getName().equals("login") || request.getCommand().getName().equals("reg")) {
                                connections.get(key1).username = request.getArgs().split(" ")[0];
                                connections.get(key1).password = request.getArgs().split(" ")[1];
                            }
                            response = CommandManager.runCommand(request, collectionManager, connections.get(key1).username);
                        }
                    }
                    try {
                        sendResponse(key, response);
                    } catch (Exception e){

                    }
                });
                // connections.put(clientChannel, LocalDateTime.now());
            } else if (bytesRead == -1) {
                // Соединение закрыто клиентом
                clientChannel.close();
                ServerLogger.getLogger().info("Соединение закрыто клиентом.");
            }
        } catch (SocketException e) {
            System.err.println("Соединение сброшено: " + e.getMessage());
            for (String key1 : connections.keySet()) {
                if (!connections.get(key1).getClient().equals(clientChannel)) {
                    Response response = new Response("Пользователь отключился:\n" + key);
                    try {
                        sendResponse(connections.get(key1).getClient(), response);
                    } catch (Exception exception) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            clientChannel.close(); // Закрываем канал, если соединение сброшено
        } catch (StreamCorruptedException e) {
            ServerLogger.getLogger().warning("Возникла ошибка: " + e.getMessage());
        }
    }

    public void sendResponse(SelectionKey key, Response response) throws IOException {
        SocketChannel client = (SocketChannel) key.channel(); // получаем канал для работы
        client.configureBlocking(false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(response);
        objectOutputStream.close();
        ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

        // Отправляем данные
        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
        ServerLogger.getLogger().info("Ответ был отправлен клиенту " + client.socket().toString());
    }

    public void sendResponse(SocketChannel client, Response response) throws IOException {
        client.configureBlocking(false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(response);
        objectOutputStream.close();
        ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

        // Отправляем данные
        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
        ServerLogger.getLogger().info("Ответ был отправлен клиенту " + client.socket().toString());
    }
}