package org.example.orderbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.example.orderbook.model.OrderBook;
import org.example.orderbook.util.Type;

public class OrderBookApplication {

    private final Comparator<OrderBook> orderBookComparator = Comparator.comparingInt(OrderBook::getPrice);
    private TreeSet<OrderBook> orderBooks = new TreeSet<>(orderBookComparator);
    private final List<String> lines = new ArrayList<>();
    private final LinkedList<Integer> ordersBuy = new LinkedList<>();
    private final LinkedList<Integer> ordersSell = new LinkedList<>();

    private static final String INPUT_FILE = "input.txt";
    private static final String OUTPUT_FILE = "output.txt";

    private static final String U = "u";
    private static final String Q = "q";
    private static final String O = "o";

    private static final String BEST_BID = "best_bid";
    private static final String BEST_ASK = "best_ask";
    private static final String SIZE = "size";

    private static final String BID = "bid";
    private static final String ASK = "asc";
    private static final String SPREAD = "spread";

    private static final String BUY = "buy";
    private static final String SELL = "sell";

    private static final String NUMBER_REGEX = "^[0-9]+$";

    public static void main(String[] args) throws Exception {
        new OrderBookApplication().go();
    }

    public void go() throws Exception {
        File file = new File(INPUT_FILE);
        BufferedReader br = new BufferedReader(new FileReader(file));
//        PrintWriter writer = new PrintWriter(OUTPUT_FILE, StandardCharsets.UTF_8);
//        writer.println("The first line");
//        writer.println("The second line");
//        writer.close();
        String st;

        while ((st = br.readLine()) != null) {
            String[] splited = st.split(",");
            if (splited.length > 1) {
                switch (splited[0]) {

                    case U:
                        if (splited.length == 4 && splited[1].matches(NUMBER_REGEX) && splited[2].matches(NUMBER_REGEX)) {
                            int price = Integer.parseInt(splited[1]);
                            int size = Integer.parseInt(splited[2]);

                            if (Type.valueOfOrNull(splited[3]) != null) {
                                update(price, size, splited[3]);
                            }
                        }
                        break;
                    case Q:
                        if (splited.length == 2) {
                            query(splited[1], null);
                        } else if (splited.length == 3 && splited[2].matches(NUMBER_REGEX)) {
                            int price = Integer.parseInt(splited[2]);
                            query(splited[1], price);
                        }
                        break;
                    case O:
                        if (splited.length == 3 && splited[2].matches(NUMBER_REGEX)) {
                            int size = Integer.parseInt(splited[2]);
                            if (size > 0) {
                                order(splited[1], size);
                            }
                        }
                }
            }

        }
        Path path = Paths.get(OUTPUT_FILE);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
        Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

    }

    private void update(Integer price, Integer size, String type) {
        Consumer<OrderBook> setSize = orderBook -> {
            if (size >= 0) {
                orderBook.setSize(size);
            }
        };
        Consumer<String> addOrderBook = updateType -> {
            if (price >= 1 && size >= 0) {
                orderBooks.add(new OrderBook(price, size, updateType, null));
            }
        };

        switch (type) {
            case BID:
                orderBooks.stream()
                        .filter(orderBook -> orderBook.getType().equals(BID) && orderBook.getPrice().equals(price))
                        .findFirst()
                        .ifPresentOrElse(orderBook -> {
                            if (size >= 0) {
                                orderBook.setSize(size);
                                int index = 0;
                                int shift = 0;
                                for (Integer orderSize : ordersSell) {
                                    boolean orderCompleted = order(SELL, orderSize);
                                    if (orderCompleted) {
                                        ordersSell.remove(index - shift);
                                        shift++;
                                    }
                                    index++;
                                }
                            }
                        }, () -> {
                            if (price >= 1 && size >= 0) {
                                OrderBook orderBook = new OrderBook(price, size, BID, null);
                                orderBooks.add(orderBook);
                                int index = 0;
                                int shift = 0;
                                for (Integer orderSize : ordersSell) {
                                    boolean orderCompleted = order(SELL, orderSize);
                                    if (orderCompleted) {
                                        ordersSell.remove(index - shift);
                                        shift++;
                                    }
                                    index++;
                                }
                            }
                        });
                break;
            case ASK:
                orderBooks.stream()
                        .filter(orderBook -> orderBook.getType().equals(ASK) && orderBook.getPrice().equals(price))
                        .findFirst()
                        .ifPresentOrElse(orderBook -> {
                            if (size >= 0) {
                                orderBook.setSize(size);
                                int index = 0;
                                int shift = 0;
                                for (Integer orderSize : ordersBuy) {
                                    boolean orderCompleted = order(BUY, orderSize);
                                    if (orderCompleted) {
                                        ordersBuy.remove(index - shift);
                                        shift++;
                                    }
                                    index++;
                                }
                            }
                        }, () -> {
                            if (price >= 1 && size >= 0) {
                                OrderBook orderBook = new OrderBook(price, size, ASK, null);
                                orderBooks.add(orderBook);
                                int index = 0;
                                int shift = 0;
                                for (Integer orderSize : ordersBuy) {
                                    boolean orderCompleted = order(BUY, orderSize);
                                    if (orderCompleted) {
                                        ordersBuy.remove(index - shift);
                                        shift++;
                                    }
                                    index++;
                                }
                            }
                        });
                break;
            case SPREAD:
                orderBooks.stream()
                        .filter(orderBook -> orderBook.getType().equals(SPREAD) && orderBook.getPrice().equals(price))
                        .findFirst()
                        .ifPresentOrElse(setSize, () -> addOrderBook.accept(SPREAD));
                break;
        }
    }

    private void query(String queryType, Integer price) {
        Consumer<OrderBook> addLine = orderBook ->
                lines.add(orderBook.getPrice().toString() + "," + orderBook.getSize().toString());

        switch (queryType) {
            case BEST_BID:
                orderBooks.stream()
                        .filter(orderBook -> orderBook.getType().equals(BID) && orderBook.getSize() > 0)
                        .max(Comparator.comparing(OrderBook::getPrice))
                        .ifPresent(addLine);
                break;
            case BEST_ASK:
                orderBooks.stream()
                        .filter(orderBook -> orderBook.getType().equals(ASK) && orderBook.getSize() > 0)
                        .min(Comparator.comparing(OrderBook::getPrice))
                        .ifPresent(addLine);
                break;
            case SIZE:
                if (price != null) {
                    orderBooks.stream()
                            .filter(orderBook -> orderBook.getPrice().equals(price))
                            .findFirst()
                            .ifPresent(orderBook -> lines.add(orderBook.getSize().toString()));
                }
                break;
        }
    }

    private boolean order(String queryType, Integer size) {
        boolean orderCompleted = false;
        switch (queryType) {
            case BUY:
                List<OrderBook> sortedBooks = orderBooks.stream()
                        .filter(orderBook -> orderBook.getType().equals(ASK) && orderBook.getSize() > 0)
                        .sorted(Comparator.comparingInt(OrderBook::getPrice).reversed())
                        .collect(Collectors.toList());

                if (sortedBooks.stream().mapToInt(OrderBook::getSize).sum() < size) {
                    ordersBuy.add(size);
                    break;
                }

                for (OrderBook orderBook : sortedBooks) {
                    int i = orderBook.getSize();
                    if (size <= i) {
                        orderBook.setSize(i - size);
                        orderCompleted = true;
                    } else {
                        orderBook.setSize(0);
                        size = size - i;
                        orderCompleted = true;
                    }
                }

//                orderBooks.stream()
//                        .filter(orderBook -> orderBook.getType().equals(ASK))
//                        .min(Comparator.comparingInt(OrderBook::getPrice))
//                        .ifPresentOrElse(orderBook -> {
//                                             if (orderBook.getSize() >= size) {
//                                                 orderBook.setSize(orderBook.getSize() - size);
//                                             } else {
//                                                 ordersBuy.add(size);
//                                             }
//                                         },
//                                         () -> ordersBuy.add(size));
                break;
            case SELL:
                List<OrderBook> sortedBooks1 = orderBooks.stream()
                        .filter(orderBook -> orderBook.getType().equals(BID) && orderBook.getSize() > 0)
                        .sorted(Comparator.comparingInt(OrderBook::getPrice))
                        .collect(Collectors.toList());

                if (sortedBooks1.stream().mapToInt(OrderBook::getSize).sum() < size) {
                    ordersSell.add(size);
                    break;
                }

                for (OrderBook orderBook : sortedBooks1) {
                    int i = orderBook.getSize();
                    if (size <= i) {
                        orderBook.setSize(i - size);
                        orderCompleted = true;
                    } else {
                        orderBook.setSize(0);
                        size = size - i;
                        orderCompleted = true;
                    }
                }

//                orderBooks.stream()
//                        .filter(orderBook -> orderBook.getType().equals(BID))
//                        .max(Comparator.comparingInt(OrderBook::getPrice))
//                        .ifPresentOrElse(orderBook -> {
//                                             if (orderBook.getSize() >= size) {
//                                                 orderBook.setSize(orderBook.getSize() - size);
//                                             } else {
//                                                 ordersSell.add(size);
//                                             }
//                                         },
//                                         () -> ordersSell.add(size));
                break;
        }
        return orderCompleted;
    }
}
