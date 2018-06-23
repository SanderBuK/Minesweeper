package minesweeper;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class Minesweeper extends JApplet {

    File highscoresBin; //Binary textfiles
    FileOutputStream fileWriterBin;
    ObjectOutputStream objectWriter;
    FileInputStream fileReaderBin;
    ObjectInputStream objectReader;
    boolean readOnly; //Used when program is first starting, and reading all the previous times

    URL fieldIconURL;
    ImageIcon fieldIcon;
    URL flagIconURL;
    ImageIcon flagIcon;
    URL questionIconURL;
    ImageIcon questionIcon;
    URL mineIconURL;
    ImageIcon mineIcon;
    URL mineWrongIconURL;
    ImageIcon mineWrongIcon;
    URL mineWrongClickedIconURL;
    ImageIcon mineWrongClickedIcon;
    URL mineRightIconURL;
    ImageIcon mineRightIcon;
    URL[] fieldsURL;
    ImageIcon[] fieldsIcon;

    Field[][] fields;

    JPanel window; //JPanel that holds all the other JPanels
    JPanel boardChooserAndHighScores;
    JPanel statusAndBoardPanel;

    boolean changingBoard = false;
    JPanel board;
    JScrollPane scrollableBoard;
    JPanel boardPanel;

    JPanel statusAndHintPanel;
    JPanel statusPanel;

    String difficulty;
    int difficultyIndex;
    String[] difficulties = new String[]{"Beginner", "Intermediate", "Expert", "Insane", "Custom"};
    JComboBox boardChooser;

    JPanel customWindow;
    JPanel customPanel;
    JLabel customRows;
    JTextField customRowsText;
    JLabel customColumns;
    JTextField customColumnsText;
    JLabel customMines;
    JTextField customMinesText;

    JLabel mineCounter;

    JButton resetButton; //TODO: Potentially make it a JLabel with a smiley picture
    boolean isReset = false;

    JLabel timerText;
    Timer timer;
    int timeSeconds = 0;
    int timeMinutes = 0;

    JPanel highscorePanel;
    JLabel highscoreDifficulty;
    JLabel highscoreTime;
    JLabel highscoreReset;
    JButton highscoresButton;
    int[][] highscoreTimes;

    JButton hintButton;
    boolean usedHint = false;
    JPanel hintWarningPanel;
    JCheckBox showHintAgainBox;
    boolean showHintWarningAgain = true;
    int boardRow; //Used in determining the fields numbers
    int boardColumn;

    int gameRows = 16;
    int gameColumns = 16;
    int gameMines = 40;

    int flagCount;

    int gameStarter = 0;
    boolean gameLost = false;

    JLabel victoryText;

    @Override
    public void init() {

        try {
            fieldIconURL = Field.class.getResource("/resources/MineField.png");
            fieldIcon = new ImageIcon(fieldIconURL);
            flagIconURL = Field.class.getResource("/resources/MineFlag.png");
            flagIcon = new ImageIcon(flagIconURL);
            questionIconURL = Field.class.getResource("/resources/MineQuestion.png");
            questionIcon = new ImageIcon(questionIconURL);
            mineIconURL = Field.class.getResource("/resources/Mine.png");
            mineIcon = new ImageIcon(mineIconURL);
            mineWrongIconURL = Field.class.getResource("/resources/MineWrong.png");
            mineWrongIcon = new ImageIcon(mineWrongIconURL);
            mineWrongClickedIconURL = Field.class.getResource("/resources/MineWrongClicked.png");
            mineWrongClickedIcon = new ImageIcon(mineWrongClickedIconURL);
            mineRightIconURL = Field.class.getResource("/resources/MineRight.png");
            mineRightIcon = new ImageIcon(mineRightIconURL);

            fieldsURL = new URL[9];
            fieldsIcon = new ImageIcon[9];
            for (int i = 0; i < 9; i++) {
                fieldsURL[i] = Field.class.getResource("/resources/MineField" + i + ".png");
                fieldsIcon[i] = new ImageIcon(fieldsURL[i]);
            }
        } catch (Exception e) {
            System.err.println("Error in initializing the images!");
        }

        victoryText = new JLabel();

        highscorePanel = new JPanel(new BorderLayout());
        highscoreDifficulty = new JLabel();
        highscoreTime = new JLabel();
        highscoreReset = new JLabel("<html><u>Reset scores</u></html>");

        highscoreReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int resetScoreYesNo = JOptionPane.showConfirmDialog(highscorePanel, "<html><h4>Are you sure?</h4></html>", "Reset highscores", JOptionPane.YES_NO_OPTION);
                if (resetScoreYesNo == 0) {
                    try {
                        fileWriterBin = new FileOutputStream(highscoresBin);
                        objectWriter = new ObjectOutputStream(fileWriterBin);

                        //TODO: Make resetting score possible!
                        for (String difficulty : difficulties) {
                            if (!difficulty.equals("Custom")) {
                                objectWriter.writeUTF(difficulty);
                                objectWriter.writeInt(-1);
                                objectWriter.writeInt(-1);
                            }
                        }
                        objectWriter.close();
                    } catch (IOException ex) {
                        System.err.println("Error in reseting scores manually!");
                    }
                    readOnly = true;
                    updateHighcores();
                    readOnly = false;
                }
            }
        });

        highscoreTimes = new int[4][2];

        try {
            highscoresBin = new File("highscores.bin");

            if (!highscoresBin.exists()) { //If the file hasn't been created yet, create the template for it
                fileWriterBin = new FileOutputStream(highscoresBin);
                objectWriter = new ObjectOutputStream(fileWriterBin);

                //TODO: Make resetting score possible!
                for (String difficulty : difficulties) {
                    if (!difficulty.equals("Custom")) {
                        objectWriter.writeUTF(difficulty);
                        objectWriter.writeInt(-1);
                        objectWriter.writeInt(-1);
                    }
                }
                objectWriter.close();
            }
            readOnly = true;
            updateHighcores();
            readOnly = false;

        } catch (IOException ex) {
            System.err.println("Error in creating fileclass!");
        }

        customWindow = new JPanel(new BorderLayout());
        customPanel = new JPanel(new GridLayout(3, 2));

        customRows = new JLabel("Enter number of rows (5-100): ");
        customRowsText = new JTextField();
        customColumns = new JLabel("Enter number of columns (5-100): ");
        customColumnsText = new JTextField();
        customMines = new JLabel("Enter number of mines (max. 1/3 of board): ");
        customMinesText = new JTextField();

        customPanel.add(customRows);
        customPanel.add(customRowsText);
        customPanel.add(customColumns);
        customPanel.add(customColumnsText);
        customPanel.add(customMines);
        customPanel.add(customMinesText);

        customWindow.add(new JLabel("<html><h2><center>Make your custom board!</center></h2></html>"), BorderLayout.NORTH);
        customWindow.add(customPanel, BorderLayout.SOUTH);

        boardChooser = new JComboBox(difficulties);
        boardChooser.setSelectedIndex(1);
        difficulty = "Intermediate";
        difficultyIndex = 1;

        boardChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (boardChooser.getSelectedIndex()) {
                    case 0:
                        if (!difficulty.equals("Beginner")) {
                            difficulty = "Beginner";
                            difficultyIndex = 0;
                            System.out.println("Beginner chosen");
                            changeBoard(8, 8, 10);
                        }
                        break;
                    case 1:
                        if (!difficulty.equals("Intermediate")) {
                            difficulty = "Intermediate";
                            difficultyIndex = 1;
                            System.out.println("Intermediate chosen");
                            changeBoard(16, 16, 40);
                        }
                        break;
                    case 2:
                        if (!difficulty.equals("Expert")) {
                            difficulty = "Expert";
                            difficultyIndex = 2;
                            System.out.println("Expert chosen");
                            changeBoard(16, 31, 99);
                        }
                        break;
                    case 3:
                        if (!difficulty.equals("Insane")) {
                            difficulty = "Insane";
                            difficultyIndex = 3;
                            System.out.println("Insane chosen");
                            changeBoard(100, 100, 1500);
                        }
                        break;
                    case 4:
                        int state = JOptionPane.showConfirmDialog(rootPane, customWindow, "Custom Board", JOptionPane.OK_CANCEL_OPTION);
                        if (state == 2) { //Set the chosen board back to previous state if 'Cancel' is pressed
                            boardChooser.setSelectedIndex(difficultyIndex);
                        } else {
                            try {
                                int newRows = Integer.parseInt(customRowsText.getText());
                                int newColumns = Integer.parseInt(customColumnsText.getText());
                                int newMines = Integer.parseInt(customMinesText.getText());

                                if (!(newRows < 5 || newRows > 100 || newColumns < 5 || newColumns > 100 || newMines > (newColumns * newRows) / 3)) {
                                    difficulty = "Custom";
                                    difficultyIndex = 4;
                                    System.out.println("Creating board!");
                                    changeBoard(newRows, newColumns, newMines);
                                    System.out.println("Board created!");
                                } else {
                                    boardChooser.setSelectedIndex(difficultyIndex);
                                }
                            } catch (NumberFormatException ex) {
                                System.err.println("Error in making a custom board!");
                                boardChooser.setSelectedIndex(difficultyIndex);
                            }
                        }
                        System.out.println("Custom chosen");
                        break;
                }
            }
        });

        statusAndBoardPanel = new JPanel(new BorderLayout());

        statusAndHintPanel = new JPanel(new BorderLayout());
        statusPanel = new JPanel(new GridLayout(1, 5));
        mineCounter = new JLabel("Mines left: " + gameMines, SwingConstants.CENTER);
        updateMineCounter();

        resetButton = new JButton("Reset game");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });

        timerText = new JLabel("Time: " + timeMinutes + ":" + ((timeSeconds < 10) ? "0" : "") + timeSeconds, SwingConstants.CENTER);
        timerText.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                giveAHint();
            }
        });
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeSeconds++;
                if (timeSeconds == 60) {
                    timeMinutes++;
                    timeSeconds = 0;
                }
                timerText.setText("Time: " + timeMinutes + ":" + ((timeSeconds < 10) ? "0" : "") + timeSeconds);
            }
        });

        highscoresButton = new JButton("Highscores");
        highscoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(rootPane, highscorePanel, "Highscores", JOptionPane.DEFAULT_OPTION);
            }
        });

        hintButton = new JButton("Hint");
        hintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                giveAHint();
            }
        });
        statusPanel.add(boardChooser);
        statusPanel.add(mineCounter);
        statusPanel.add(resetButton);
        statusPanel.add(timerText);
        statusPanel.add(highscoresButton);

        initializeBoard(gameRows, gameColumns);

        boardPanel = new JPanel();
        boardPanel.add(board);
        scrollableBoard = new JScrollPane(boardPanel);
        scrollableBoard.getVerticalScrollBar().setUnitIncrement(7);

        statusAndBoardPanel.add(statusPanel, BorderLayout.NORTH);
        statusAndBoardPanel.add(scrollableBoard);
        //TODO: if the hint button should be activated
//        statusAndBoardPanel.add(hintButton, BorderLayout.SOUTH);

        super.add(statusAndBoardPanel);
    }

    public void updateMineCounter() {
        mineCounter.setText("Mines left: " + (gameMines - flagCount));
    }

    public void updateHighcores() {
        try {
            if (!readOnly) {
                fileWriterBin = new FileOutputStream(highscoresBin);
                objectWriter = new ObjectOutputStream(fileWriterBin);

                for (int i = 0; i < difficulties.length - 1; i++) {
                    objectWriter.writeUTF(difficulties[i]);
                    objectWriter.writeInt(highscoreTimes[i][0]);
                    objectWriter.writeInt(highscoreTimes[i][1]);
                }
                objectWriter.close();
            }
            fileReaderBin = new FileInputStream(highscoresBin);
            objectReader = new ObjectInputStream(fileReaderBin);

        } catch (FileNotFoundException ex) {
            System.err.println("Error in initializing file reader!");
        } catch (IOException ex) {
            System.err.println("Error in initializing file writer!");
        }
        try {
            while (true) {
                String nowDifficulty = objectReader.readUTF();
                System.out.println("NOW DIFFICULTY: " + nowDifficulty);
                switch (nowDifficulty) { //Store all the previous times.
                    case "Beginner":
                        highscoreTimes[0][0] = objectReader.readInt();
                        highscoreTimes[0][1] = objectReader.readInt();
                        break;
                    case "Intermediate":
                        highscoreTimes[1][0] = objectReader.readInt();
                        highscoreTimes[1][1] = objectReader.readInt();
                        break;
                    case "Expert":
                        highscoreTimes[2][0] = objectReader.readInt();
                        highscoreTimes[2][1] = objectReader.readInt();
                        break;
                    case "Insane":
                        highscoreTimes[3][0] = objectReader.readInt();
                        highscoreTimes[3][1] = objectReader.readInt();
                        break;
                }
            }
        } catch (EOFException e) {
            System.err.println("Reached end of binaryfile!");
        } catch (IOException ex) {
            System.err.println("Error in reading the previous scores!");
        }
        try {
            objectReader.close();
        } catch (IOException ex) {
            System.err.println("Error in closing objectReader!");
        }
        for (int i = 0; i < highscoreTimes.length; i++) {
            System.out.println(highscoreTimes[i][0]);
            System.out.println(highscoreTimes[i][1]);
        }
        StringBuilder highscoreTimeText = new StringBuilder();
        for (int i = 0; i < highscoreTimes.length; i++) {
            if (highscoreTimes[i][0] == -1) {
                highscoreTimeText.append("N/A<br>");
            } else {
                highscoreTimeText.append(highscoreTimes[i][0]).append(":").append((highscoreTimes[i][1] < 10) ? "0" + highscoreTimes[i][1] : highscoreTimes[i][1]).append("<br>");
            }
        }
        highscoreDifficulty.setText("<html><h2>Beginner: <br>Intermediate: <br>Expert: <br>Insane: </h2></html>");
        highscoreTime.setText("<html><h2>" + highscoreTimeText.toString() + "</h2></html>");
        highscorePanel.add(new JLabel("<html><h1>Highscores</h1></html>", SwingConstants.CENTER), BorderLayout.NORTH);
        highscorePanel.add(highscoreDifficulty, BorderLayout.WEST);
        highscorePanel.add(highscoreTime, BorderLayout.EAST);
        highscorePanel.add(highscoreReset, BorderLayout.SOUTH);
    }

    public void checkForHighscore(int difficultyIndex) {
        if (!(highscoreTimes[difficultyIndex][0] == -1)) { //Check if the time has been initialized
            int prevMinutes = highscoreTimes[difficultyIndex][0];
            int prevSeconds = highscoreTimes[difficultyIndex][1];
            if (prevMinutes > timeMinutes || (prevMinutes == timeMinutes && prevSeconds > timeSeconds)) {
                highscoreTimes[difficultyIndex][0] = timeMinutes;
                highscoreTimes[difficultyIndex][1] = timeSeconds;
                updateHighcores();
            }
        } else {
            highscoreTimes[difficultyIndex][0] = timeMinutes;
            highscoreTimes[difficultyIndex][1] = timeSeconds;
            updateHighcores();
        }
    }

    public void changeBoard(int rows, int columns, int mines) {
        gameRows = rows;
        gameColumns = columns;
        gameMines = mines;

        board.removeAll();
        board.repaint();

        changingBoard = true;

        initializeBoard(gameRows, gameColumns);

        resetGame();
    }

    public void initializeBoard(int row, int column) {
        boardRow = row - 1;
        boardColumn = column - 1;
        if (!isReset) {
            if (!changingBoard) {
                board = new JPanel(new GridLayout(row, column));
            } else {
                board.setLayout(new GridLayout(row, column));
            }
            fields = new Field[row][column];
            System.out.println(row + " " + column);

            int id = 0;
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    fields[i][j] = new Field(i, j, id);
                    board.add(fields[i][j]);
                    id++;
                }
            }
        }
    }

    public void startGame(int row, int column, int mines) {
        //Loop through the grid until all mines are placed randomly

        System.out.println("Activating mines!");

        for (int i = 0; i < mines; i++) {
            while (true) {
                int randRow = (int) (Math.random() * gameRows);
                int randColumn = (int) (Math.random() * gameColumns);
                if (!fields[randRow][randColumn].isMine() && !fields[randRow][randColumn].isOpen()) {
                    fields[randRow][randColumn].setMine(true);
                    break;
                }
            }
        }

        System.out.println("Determining numbers!");

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                fields[i][j].setMineNum(determineNum(i, j));
                if (fields[i][j].isOpen()) {
                    fields[i][j].updateIcon();
                }
//                System.out.println(determineNum(i, j) + " " + i + " " + j);
            }
        }
    }

    public int determineNum(int row, int column) {
        int fieldNum = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if ((row + i >= 0 && row + i <= boardRow)
                        && (column + j >= 0 && column + j <= boardColumn)
                        && fields[row + i][column + j].isMine()) {
                    fieldNum++;
                }
            }
        }
        return fieldNum;
    }

    public void resetGame() {
        for (Field[] fieldsArray : fields) {
            for (Field field : fieldsArray) {
                if (field.isFlag() || field.isQuestion()) {
                    field.setNormalField();
                }
                field.setClickable(true);
                field.setMineNum(0);
                field.setOpen(false);
                field.setMine(false);
                field.setIcon(fieldIcon);
                gameStarter = 0;
            }
        }
        if (timer.isRunning()) {
            timer.stop();
        }
        timeSeconds = 0;
        timeMinutes = 0;
        timerText.setText("Time: " + timeMinutes + ":" + ((timeSeconds < 10) ? "0" : "") + timeSeconds);

        mineCounter.setText("Mines left: 0"); //Updating the minetext updates the board for some reason
        flagCount = 0;
        updateMineCounter();

        usedHint = false;

        isReset = true;
        initializeBoard(gameRows, gameColumns);
        isReset = false;
    }

    public void gameLost(int clickedID) {
        for (Field[] fieldsArray : fields) {
            for (Field field : fieldsArray) {
                if (field.id != clickedID) {
                    if (field.isMine() && field.isFlag()) {
                        field.setIcon(mineRightIcon);
                    } else if (field.isMine()) {
                        field.setIcon(mineIcon);
                    } else if (field.isFlag()) {
                        field.setIcon(mineWrongIcon);
                    }
                }
                field.setClickable(false);
            }
        }
        timer.stop();
    }

    public void checkForWin() {
        int openFields = 0;
        for (Field[] fieldArray : fields) {
            for (Field field : fieldArray) {
                if (field.isOpen() && !field.isMine()) {
                    openFields++;
                }
            }
        }
        if (openFields == (gameRows * gameColumns) - gameMines) {
            timer.stop();
            for (Field[] fieldArray : fields) {
                for (Field field : fieldArray) {
                    if (field.isMine() && !field.isFlag()) {
                        field.setFlagField();
                        field.setIcon(flagIcon);
                    }
                    field.setClickable(false);
                }
            }
            victoryText.setText("<html><h1><center>WINNER!</center></h1><br><h3>Congratulations on completing Minesweeper on '" + difficulty + "' difficulty in " + timeMinutes + ":" + ((timeSeconds < 10) ? "0" : "") + timeSeconds + "</h3></html>");
            JOptionPane.showMessageDialog(rootPane, victoryText,
                    "Congratulations!", JOptionPane.PLAIN_MESSAGE);
            if (difficultyIndex != 4) {
                checkForHighscore(difficultyIndex);
            }
        }
    }

    public void giveAHint() {
        ArrayList<Field> possibleHints = new ArrayList<>();
        boolean searching = true;

        usedHint = true;

        for (int row = 0; row < gameRows; row++) {
            for (int column = 0; column < gameColumns; column++) {
                if (fields[row][column].isMine() && !fields[row][column].isFlag() && !fields[row][column].isQuestion()) {
                    for (int i = -1; i < 2; i++) {
                        for (int j = -1; j < 2; j++) {
                            if (row + i >= 0 && row + i < gameRows && column + j >= 0 && column < gameColumns) {
                                try {
                                    if (fields[row + i][column + j].isOpen()) {
                                        possibleHints.add(fields[row][column]);
                                    }
                                } catch (ArrayIndexOutOfBoundsException ex) {
                                    System.err.println("OutOfBounds");
                                    System.out.println("Field: " + row + " " + column);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!possibleHints.isEmpty()) {
            possibleHints.get((int) (Math.random() * possibleHints.size())).setQuestionField();
        }
    }

    //**********************************************//
    //**************Field Class*********************//
    //**********************************************//
    public class Field extends JLabel {

        int id; //Used for calculating the position of the field and its adjacent fields

        //If the field is a mine or not
        private boolean mineState = false;

        //If the field is a flag or not
        private boolean flagState = false;

        private boolean questionState = false;

        //If the field is open or not
        private boolean openState = false;

        boolean clickable = true; //Used when game losses, and the mines show.

        //Number of mines in a 3x3 grid around the field
        private int mineNum;

        Field(int row, int column, int id) {

            this.id = id;

            super.setIcon(fieldIcon);
            super.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {

                    if (e.getButton() == MouseEvent.BUTTON3 && !isOpen() && clickable) { //Place a flag if rightclick
                        switchFieldState();
                    } else if (e.getButton() == MouseEvent.BUTTON1 && clickable) {
                        if (!isOpen() && !isFlag()) { //Only change the field if it isn't open.
                            if (!isMine()) { //If the field isn't a mine, show the number. Else show the mine.
                                if (gameStarter == 0) {
                                    fields[row][column].setOpen(true);
                                    System.out.println("Opening starting area!");
                                    openStartingArea(row, column);
                                    System.out.println("Starting game!");
                                    startGame(gameRows, gameColumns, gameMines);

                                    for (int i = -1; i < 2; i++) { //Use the clearzero method on the starting 3x3 grid
                                        for (int j = -1; j < 2; j++) {
                                            if ((row + i >= 0 && row + i <= boardRow) //Check wether the 3x3 grid is out of bounds
                                                    && (column + j >= 0 && column + j <= boardColumn)) {
                                                if (fields[row + i][column + j].mineNum == 0) {
                                                    clearZeroFields(row + i, column + j);
                                                }
                                            }
                                        }
                                    }

                                    gameStarter++;
                                    timer.start();
                                }
                                if (mineNum == 0) { //Clear the fields if the mineNum is 0
                                    clearZeroFields(row, column);
                                }
                                setOpen(true);
                                openState = true;
                                System.out.println("Height, width: " + row + " " + column);
                                checkForWin();
                            } else {
                                setOpen(true);
                                setIcon(mineWrongClickedIcon);
                                gameLost(id);
                            }
                        }
                    }
                }
            });
        }

        public void setMineNum(int mineNum) {
            this.mineNum = mineNum;
        }

        public void updateIcon() {
            setIcon(fieldsIcon[mineNum]);
        }

        public boolean isOpen() {
            return openState;
        }

        public void setOpen(boolean state) {
            updateIcon();
            openState = state;
        }

        public void setClickable(boolean state) {
            clickable = state;
        }

        public boolean isFlag() {
            return flagState;
        }

        public boolean isQuestion() {
            return questionState;
        }

        public void setNormalField() {
            if (isFlag()) {
                flagState = false;
                flagCount--;
                updateMineCounter();
            }
            questionState = false;
            setIcon(fieldIcon);
        }

        public void setFlagField() {
            flagCount++;
            flagState = true;
            questionState = false;
            setIcon(flagIcon);
            updateMineCounter();
        }

        public void setQuestionField() {
            if (isFlag()) {
                flagState = false;
                flagCount--;
                updateMineCounter();
            }
            questionState = true;
            setIcon(questionIcon);
        }

        public void switchFieldState() {
            if (!isFlag() && !isQuestion()) {
                setFlagField();
            } else if (isFlag()) {
                setQuestionField();
            } else {
                setNormalField();
            }
            updateMineCounter();
        }

        public boolean isMine() {
            return mineState;
        }

        public void setMine(boolean state) {
            mineState = state;
        }

        public void openStartingArea(int row, int column) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if ((row + i >= 0 && row + i <= boardRow) //Loop through all the fields in the 3x3 grid
                            && (column + j >= 0 && column + j <= boardColumn)) {
                        System.out.println("Setting: " + (row + i) + ":" + (column + j) + " open!");
                        if (fields[row + i][column + j].isFlag() || fields[row + i][column + j].isQuestion()) {
                            fields[row + i][column + j].setNormalField();
                        }
                        fields[row + i][column + j].setOpen(true); //And open them is they aren't out of bounds
                    }
                }
            }
        }

        public void clearZeroFields(int row, int column) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if ((row + i >= 0 && row + i <= boardRow) //Loop through all the fields in the 3x3 grid
                            && (column + j >= 0 && column + j <= boardColumn)
                            && !(fields[row + i][column + j].isOpen())) { //And open them if they are closed
                        if (fields[row + i][column + j].isFlag() || fields[row + i][column + j].isQuestion()) {
                            fields[row + i][column + j].setNormalField();
                        }
                        fields[row + i][column + j].setOpen(true);
                        if (fields[row + i][column + j].mineNum == 0) {
                            clearZeroFields(row + i, column + j); //Call the clear Zero method again if any of the newly cleared fields have a mineNum of 0
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame main = new JFrame("MineSweeper");
        main.setSize(800, 558);
        Minesweeper app = new Minesweeper();

        app.init();
        app.start();
        main.add(app);

        main.setLocationRelativeTo(null);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }
}
