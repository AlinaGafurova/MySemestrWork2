package edu.lmu.cs.networking;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.PrintWriter;
        import java.net.Socket;

/**
 * A two-player game.
 */

public class Game {

    /**
     * A board has nine squares.  Each square is either unowned or
     * it is owned by a player.  So we use a simple array of player
     * references.  If null, the corresponding square is unowned,
     * otherwise the array cell stores a reference to the player that
     * owns it.
     */

    /**
     * The server board.
     */

    private Object[] board;
    private int columnLength = (int) Math.sqrt(25);



    /**
     * The current player.
     */

    Player currentPlayer;

    void setDesk(Object[] board, int locX, int locO) {
        this.board = board;

        currentPlayer.previousLocation = locX;
        currentPlayer.opponent.previousLocation = locO;

        currentPlayer.setStartPosition(currentPlayer.previousLocation);
        System.out.println(currentPlayer + " - " + currentPlayer.previousLocation);
        currentPlayer = currentPlayer.opponent;

        currentPlayer.setStartPosition(currentPlayer.previousLocation);
        System.out.println(currentPlayer + " - " + currentPlayer.previousLocation);
        currentPlayer.setOtherStartPosition(currentPlayer.opponent.previousLocation);

        currentPlayer = currentPlayer.opponent;
        currentPlayer.setOtherStartPosition(currentPlayer.opponent.previousLocation);
    }

    private void changePlayer() {
        currentPlayer.wasMove = false;
        currentPlayer = currentPlayer.opponent;
        currentPlayer.output.println("YOUR_MOVE");
    }

    /**
     * Returns whether the current state of the board is such that one
     * of the players is a winner.
     */

    private boolean hasWinner(int location) throws NullPointerException, ArrayIndexOutOfBoundsException {
        return board[location].equals(currentPlayer.opponent);
    }

    /**
     * Called by the player threads when a player tries to make a
     * move.  This method checks to see if the move is legal: that
     * is, the player requesting the move must be the current player
     * and the square in which she is trying to move must not already
     * be occupied.  If the move is legal the game state is updated
     * (the square is set and the next player becomes current) and
     * the other player is notified of the move so it can update its
     * client.
     */

    private synchronized int direction(String subCommand) { //направление хода
        if (subCommand.startsWith("LEFT"))
            return currentPlayer.previousLocation - 1;
        else if (subCommand.startsWith("RIGHT"))
            return currentPlayer.previousLocation + 1;
        else if (subCommand.startsWith("UP"))
            return currentPlayer.previousLocation - columnLength;
        else if (subCommand.startsWith("DOWN"))
            return currentPlayer.previousLocation + columnLength;
        else
            return -1;
    }
    /*
        private synchronized boolean validShow(Player player, String command, PrintWriter output) {
            if (player == currentPlayer) { //если сейчас ходит правильный игрок
                String turn = command.substring(5);
                int location = direction(turn); //определяем координату по направлению
                int code = -1; //код клетки (0 - пусто, 1 - препятствие)
                try {
                    if (currentPlayer.previousLocation % columnLength == 0 && turn.equals("LEFT")) { //проверка для одномерного массива
                        throw new ArrayIndexOutOfBoundsException();
                    } else if ((currentPlayer.previousLocation + 1) % columnLength == 0 && turn.equals("RIGHT")) { //проверка для одномерного массива
                        throw new ArrayIndexOutOfBoundsException();
                    }
                    if (board[location].equals(currentPlayer.opponent)) {
                        code = 0;
                    } else if (board[location].equals(brick) || board[location].equals(granite)) { //если препятствие
                        code = 1; //если препятствие то это код 1
                    }
                } catch (NullPointerException e) { //если клетка пустая
                    code = 0;
                } catch (ArrayIndexOutOfBoundsException e) { //если вышли за пределы поля
                    code = 1;
                }
                output.println("OPEN " + code + " " + turn); //команда отдает состояние нужной клетки пользователю
                currentPlayer.opponent.otherPlayerOpened(code, turn); //уведомляем оппонента о результате хода
                currentPlayer.wasShow = true;
                return true;
            }
            return false;
        }
    */
    private synchronized boolean validMove(Player player, String command, PrintWriter output) {
        if (player == currentPlayer) {
            String turn = command.substring(5);
            int location = direction(turn);

            board[currentPlayer.previousLocation] = null;

            try {
                if (currentPlayer.previousLocation % columnLength == 0 && turn.equals("left")) {
                    throw new ArrayIndexOutOfBoundsException();
                } else if ((currentPlayer.previousLocation + 1) % columnLength == 0 && turn.equals("right")) {
                    throw new ArrayIndexOutOfBoundsException();
                }

                if (board[location].equals(brick) || board[location].equals(granite)) {
                    int code = 1;
                    output.println("open " + code + " " + turn);
                    currentPlayer.opponent.otherPlayerOpened(code, turn);

                } else if (board[location].equals(currentPlayer.opponent) && !currentPlayer.opponent.onRuins) { //если на клетке противник и под ним нет руин, то идем
                    board[location] = currentPlayer;
                    if (currentPlayer.onRuins) {
                        board[currentPlayer.previousLocation] = ruin;
                        currentPlayer.onRuins = false;
                    }
                    output.println("VALID_MOVE " + turn);
                    currentPlayer.opponent.otherPlayerMoved(turn, 0);
                    currentPlayer.previousLocation = location;

                } else if (board[location].equals(ruin) || (board[location].equals(currentPlayer.opponent) && currentPlayer.opponent.onRuins)) { //если на клетке осколки или противник под которым руины, то идем
                    board[location] = currentPlayer;
                    currentPlayer.onRuins = true;
                    output.println("VALID_MOVE_" + turn);
                    currentPlayer.opponent.otherPlayerMoved(turn, 1);
                    currentPlayer.previousLocation = location;

            } catch (ArrayIndexOutOfBoundsException e) {
                int code = 1;
                output.println("OPEN " + code + " " + turn);
                currentPlayer.opponent.otherPlayerOpened(code, turn);
                //currentPlayer.wasShow = true;

            } catch (NullPointerException e) {
                board[location] = currentPlayer;
                if (currentPlayer.onRuins) {
                    board[currentPlayer.previousLocation] = ruin;
                    currentPlayer.onRuins = false;
                }
                output.println("VALID_MOVE " + turn);
                currentPlayer.opponent.otherPlayerMoved(turn, 0);
                    currentPlayer.previousLocation = location;

            }
            currentPlayer.wasMove = true;
            currentPlayer.output.println("Message: you can throw a bomb or a complete turn");
            return true;
        }
        return false;
    }

    private synchronized boolean validThrow(Player player, String command, PrintWriter output) {
        if (player == currentPlayer) {
            String turn = command.substring(5);
            int location = direction(turn);
            String message = null;

            try {
                if (currentPlayer.previousLocation % columnLength == 0 && turn.equals("left")) {
                    throw new ArrayIndexOutOfBoundsException();
                } else if ((currentPlayer.previousLocation + 1) % columnLength == 0 && turn.equals("right")) {
                    throw new ArrayIndexOutOfBoundsException();
                }

                if (hasWinner(location)) {
                    board[location] = null;
                    output.println("victory");

                    message = "defeat";

                } else if (board[location].equals(brick)) {
                    board[location] = ruin;
                    output.println("DESTROYED " + turn);

                    message = "OPPONENT_DESTROYED " + turn;

                } else if (board[location].equals(granite)) {
                    output.println("NOT_DESTROYED " + turn);

                    message = "OPPONENT_NOT_DESTROYED " + turn;

                } else if (board[location].equals(ruin)) {
                    output.println("THROW_INTO_THE_VOID_" + turn);

                    message = "OPPONENT_THROW_INTO_THE_VOID_" + turn;
                }

            } catch (NullPointerException e) {
                if (player == currentPlayer) {
                    output.println("THROW_INTO_THE_VOID " + turn);

                    message = "OPPONENT_THROW_INTO_THE_VOID " + turn;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                output.println("NOT_DESTROYED " + turn);

                message = "OPPONENT_NOT_DESTROYED " + turn;
            }
            currentPlayer.opponent.otherPlayerThrowed(message);
            changePlayer();
            return true;
        }
        return false;
    }

    /**
     * The class for the helper threads in this multithreaded server
     * application.  A Player is identified by a character mark
     * which is either 'X' or 'O'.  For communication with the
     * client the player has a socket with its input and output
     * streams.  Since only text is being communicated we use a
     * reader and a writer.
     */

    private class Brick {
    }

    private class Granite {
    }

    private class Ruin {

    }

    Brick brick = new Brick();
    Granite granite = new Granite();
    Ruin ruin = new Ruin();

    class Player extends Thread {
        char mark;
        Game game = new Game();
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

        int currentLocation;
        int previousLocation;

        boolean firstMove = true;

        boolean wasMove = false;

        boolean onRuins = false;

        /**
         * Constructs a handler thread for a given socket and mark
         * initializes the stream fields, displays the first two
         * welcoming messages.
         */

        Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("Welcome " + mark);
                output.println("Message: Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        /**
         * Accepts notification of who the opponent is.
         */

        void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * Handles the otherPlayerMoved message.
         */

        void setOtherStartPosition(int location) {
            if (firstMove) {
                board[location] = currentPlayer.opponent;
                output.println("OPPONENT_START");
                currentPlayer.firstMove = false;
            }
        }

        void otherPlayerOpened(int code, String turn) {
            output.println("OPPONENT_OPEN " + code + " " + turn);
        }

        void otherPlayerMoved(String turn, int code) {
            if (code == 0) {
                System.out.println("OPPONENT_MOVED ");
                output.println("OPPONENT_MOVED " + turn);
            }
            else {
                System.out.println("OPPONENT_MOVED_");
                output.println("OPPONENT_MOVED_" + turn);
            }
        }

        void otherPlayerThrowed(String message) {
            output.println(message);
        }

        void setStartPosition(int location) {
            if (firstMove) {
                board[location] = currentPlayer; //ставим игрока на стартовую позицию
                output.println("Start");
            }
        }

        /**
         * The run method of this thread.
         */

        public void run() {
            try {

                output.println("Message: All players connected");


                if (mark == 'X') {
                    output.println("Message: Your move");
                }


                while (true) {
                    String command = input.readLine();
                    if (command != null) {
                        /*
                        if (command.startsWith("SHOW") && !wasShow) {
                            if (!validShow(this, command, output)) {
                                output.println("MESSAGE It's not your turn");
                            } else {
                            }
                        } else */
                        if (command.startsWith("MOVE") && !wasMove) {
                            if (!validMove(this, command, output)) {
                                output.println("Message: It isn`t your turn");
                            }

                        } else if (command.startsWith("BOMB")) {
                            if (!validThrow(this, command, output)) {
                                output.println("Message: It isn`t your turn");
                            }

                        } else if (command.startsWith("CHANGE")) {
                            changePlayer();

                        } else if (command.startsWith("QUIT")) {
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Player died...: " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public String toString() {
        if (currentPlayer != null) {
            return currentPlayer.mark + " has WOOON!!!";
        } else {
            return "game in progress";
        }
    }
}