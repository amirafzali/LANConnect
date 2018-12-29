//  LAN ConnectFour
//  Created by Amir Afzali
//  Copyright © 2018 Amir. All rights reserved.

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ConnectFour implements ActionListener {
    public JFrame frame;
    public JPanel panelButtons, panelGame, panelMain;

    public JButton first, second, third, fourth, fifth, sixth, seventh;
    public JButton[] buttons;
    public int[] buttonSize;
    public String player = "r";
    public Client client;


    public Game game;
    public ConnectFour(Client client) {
        this.game = new Game();
        this.client = client;

        //Initiate stuff
        frame = new JFrame(getMessage());
        panelMain = new JPanel();
        panelButtons = new JPanel();
        panelGame = game;

        String message = "";
        //make gui
        makeGui();
        Thread thread = new Thread() {
            public void run() {
                while(true) {
                    panelGame.repaint();
                    frame.setTitle(getMessage());
                    client.send(message);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {}
                }
            }
        }; thread.start();
    }

    public void makeGui() {
        //Buttons, etc
        buttons = new JButton[]{first = new JButton("V"), second = new JButton("V"), third = new JButton("V"), fourth = new JButton("V"), fifth = new JButton("V"), sixth = new JButton("V"), seventh = new JButton("V")};
        int spacing = 14;
        int buttonY=10, buttonWidth = 45, buttonHeight = 25;
        int firstX = 7, secondX = firstX+buttonWidth+ spacing, thirdX = secondX+buttonWidth+spacing, fourthX = thirdX+buttonWidth+spacing, fifthX = fourthX+buttonWidth+spacing, sixthX = fifthX+buttonWidth+spacing, seventhX = sixthX+buttonWidth+spacing;
        buttonSize = new int[]{firstX, secondX, thirdX, fourthX, fifthX, sixthX, seventhX};

        frame.setLayout(new FlowLayout());
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
        panelButtons.setLayout(null);
        panelGame.setLayout(null);

        for(int i =0; i<buttons.length; i++) {
            panelButtons.add(buttons[i]);
            buttons[i].setBounds(buttonSize[i], buttonY, buttonWidth, buttonHeight);
            buttons[i].addActionListener(this);
        }
        //panel stuff
        panelGame.setPreferredSize(new Dimension(420, 420));
        panelButtons.setPreferredSize(new Dimension(100,50));

        panelMain.add(panelButtons);
        panelMain.add(panelGame);

        frame.add(panelMain, BorderLayout.NORTH);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(panelMain);
        frame.setVisible(true);
        frame.setFocusable(true);
        //if second player, move their screen
        if(client.name.equals("Red")) { frame.setLocationRelativeTo(null); }
        frame.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //if not started, stop them
        if(!client.start) {
            JOptionPane.showMessageDialog(game, "Waiting for opponent!");
            return;
        }
        //if not turn, stop
        if((client.name.equals("Blue") && player.equals("b")) || (client.name.equals("Red") && player.equals("r"))) {
            JOptionPane.showMessageDialog(game, "Not your turn!");
            return;
        }
        for(int i=0; i<buttons.length; i++) {
            if(e.getSource()==buttons[i]) {
                placeTile(i);
            }
        }
    }
    public void placeTile(int column) {
        //place tile
        for (int i = 0; i < 6; i++) {
            if ((i==5 || !game.connectGrid[i+1][column].equals("0")) && game.connectGrid[i][column].equals("0")) {
                if (player.equals("r")) {
                    player = "b";
                    frame.setTitle("You: "+client.name+"| Red's Turn");
                } else {
                    player = "r";
                    frame.setTitle("You: "+client.name+"| Blue's Turn");
                }
                //update grid
                game.connectGrid[i][column] = player;
                //update server
                client.send("POS"+i+""+column);
                //check if win
                if(checkWin(column, i, -1, 0)) {
                    reset();
                }
                break;
            }
        }
    }
    //reset game
    private void reset() {
        if (player.equals("r")) {
            JOptionPane.showMessageDialog(game, "Red wins!");
        } else {
            JOptionPane.showMessageDialog(game, "Blue wins!");
        }
        client.send("RESET");
        game.connectGrid = new String[][]{"0 0 0 0 0 0 0 ".split(" "), "0 0 0 0 0 0 0 ".split(" "),
                "0 0 0 0 0 0 0 ".split(" "), "0 0 0 0 0 0 0 ".split(" "),
                "0 0 0 0 0 0 0 ".split(" "), "0 0 0 0 0 0 0 ".split(" ")};
        panelGame.repaint();
    }
    //recursive win chekck
    public boolean checkWin(int column, int row, int type, int count) {
        //if 4 tiles then win
        if(count==4) {
            return true;
        }
        //if out bounds then no
        if(column>6 || row > 5 || column < 0 || row < 0) {
            return false;
        }
        //if not common tile then no
        if(!game.connectGrid[row][column].equals(player)) {
            return false;
        }
        //check directions
        count++;
        switch (type) {
            case(1): return checkWin(column+1, row, 1, count);
            case(2): return checkWin(column-1, row, 2, count);
            case(3): return checkWin(column, row+1, 3, count);
            case(5): return checkWin(column+1, row+1, 5, count);
            case(7): return checkWin(column-1, row+1, 7, count);
            default:
                return(checkWin(column+1, row, 1, count) || checkWin(column-1, row, 2, count) || checkWin(column, row+1, 3, count) || checkWin(column+1, row+1, 5, count) || checkWin(column-1, row+1, 7, count));
        }
    }
    public void checkDrop(String pos) {
        //check drop info from server
        int row = Integer.parseInt(pos.substring(3,4));
        int column = Integer.parseInt(pos.substring(4,5));
        if(game.connectGrid[row][column].equals("0")) {
            placeTile(column);
        }
    }

    public String getMessage() {
        //get game state
        if(client.start) { return "You: "+client.name+"| Blue's Turn"; }
        return "Waiting for opponent";
    }

    public void kill() {
        //kill game
        JOptionPane.showMessageDialog(game, "Opponent disconnected. Bye!");
        System.exit(-1);
    }
}
class Game extends JPanel{

    //game panel drawing stuff

    public int gridX = 0, gridY = 0;
    public String[][] connectGrid = {"0 0 0 0 0 0 0 ".split(" "), "0 0 0 0 0 0 0 ".split(" "),
            "0 0 0 0 0 0 0 ".split(" "), "0 0 0 0 0 0 0 ".split(" "),
            "0 0 0 0 0 0 0 ".split(" "), "0 0 0 0 0 0 0 ".split(" ")};
    Game() {
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);

    }
    private void drawGrid(Graphics g) {
        for (String[] row : connectGrid) {
            for (int j = 0; j < row.length; j++) {
                g.setColor(Color.BLACK);
                g.drawRect(gridX, gridY, 58, 68);
                if (row[j].equals("r")) {
                    g.setColor(Color.RED);
                    g.fillOval(gridX+1, gridY+1, 56, 66);
                }else if (row[j].equals("b")) {
                    g.setColor(Color.BLUE);
                    g.fillOval(gridX+1, gridY+1, 56, 66);
                }
                gridX += 59;
            }
            gridY += 68;
            gridX = 0;
        }
        gridY = 0;
    }
}
