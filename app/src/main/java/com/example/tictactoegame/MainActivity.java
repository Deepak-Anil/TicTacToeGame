package com.example.tictactoegame;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private MaterialButton[][] buttons = new MaterialButton[3][3];
    private boolean playerXTurn = true;
    private int roundCount = 0;
    private TextView playerTurnTextView;
    private TextView gameTitleTextView;
    private boolean vsBot = false;
    private boolean gameEnded = false;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerTurnTextView = findViewById(R.id.playerTurn);
        gameTitleTextView = findViewById(R.id.gameTitle);

        // Initialize buttons
        buttons[0][0] = findViewById(R.id.btn00);
        buttons[0][1] = findViewById(R.id.btn01);
        buttons[0][2] = findViewById(R.id.btn02);
        buttons[1][0] = findViewById(R.id.btn10);
        buttons[1][1] = findViewById(R.id.btn11);
        buttons[1][2] = findViewById(R.id.btn12);
        buttons[2][0] = findViewById(R.id.btn20);
        buttons[2][1] = findViewById(R.id.btn21);
        buttons[2][2] = findViewById(R.id.btn22);

        // Set click listeners
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int col = j;
                buttons[i][j].setOnClickListener(v -> handleButtonClick(row, col));
            }
        }

        MaterialButton resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            resetGame();
        });

        MaterialButtonToggleGroup gameModeToggle = findViewById(R.id.gameModeToggle);
        gameModeToggle.check(R.id.btnTwoPlayers);
        gameModeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                vsBot = checkedId == R.id.btnVsBot;
                resetGame();
            }
        });
    }

    private void handleButtonClick(int row, int col) {
        if (gameEnded || !buttons[row][col].getText().toString().isEmpty()) {
            return;
        }

        makeMove(row, col); // Player move

        if (vsBot && !gameEnded && !playerXTurn) {
            // Delay bot's move slightly for better UX
            new Handler().postDelayed(this::makeBotMove, 500);
        }
    }

    private void makeMove(int row, int col) {
        buttons[row][col].setText(playerXTurn ? "X" : "O");
        buttons[row][col].setTextColor(getColor(playerXTurn ? R.color.x_color : R.color.o_color));
        buttons[row][col].startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        roundCount++;

        if (checkForWin()) {
            gameEnded = true;
            showGameEndDialog(playerXTurn ? "Player X Wins! 🎉" : 
                (vsBot ? "Bot Wins! 🤖" : "Player O Wins! 🎉"));
        } else if (roundCount == 9) {
            gameEnded = true;
            showGameEndDialog("It's a Draw! 🤝");
        } else {
            playerXTurn = !playerXTurn;
            updatePlayerTurn();
        }
    }

    private void makeBotMove() {
        // First try to win
        if (findWinningMove()) return;

        // Then try to block player's winning move
        if (findBlockingMove()) return;

        // Try to take center
        if (buttons[1][1].getText().toString().isEmpty()) {
            makeMove(1, 1);
            return;
        }

        // Try to take corners
        if (makeCornerMove()) return;

        // Take any available edge
        makeRandomMove();
    }

    private boolean findWinningMove() {
        return findStrategicMove("O");
    }

    private boolean findBlockingMove() {
        return findStrategicMove("X");
    }

    private boolean findStrategicMove(String mark) {
        // Check rows, columns and diagonals for potential wins
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().isEmpty()) {
                    buttons[i][j].setText(mark);
                    if (checkForWin()) {
                        buttons[i][j].setText("");
                        makeMove(i, j);
                        return true;
                    }
                    buttons[i][j].setText("");
                }
            }
        }
        return false;
    }

    private boolean makeCornerMove() {
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        List<int[]> availableCorners = new ArrayList<>();
        
        for (int[] corner : corners) {
            if (buttons[corner[0]][corner[1]].getText().toString().isEmpty()) {
                availableCorners.add(corner);
            }
        }
        
        if (!availableCorners.isEmpty()) {
            int[] selectedCorner = availableCorners.get(random.nextInt(availableCorners.size()));
            makeMove(selectedCorner[0], selectedCorner[1]);
            return true;
        }
        return false;
    }

    private void makeRandomMove() {
        List<int[]> availableMoves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().isEmpty()) {
                    availableMoves.add(new int[]{i, j});
                }
            }
        }
        
        if (!availableMoves.isEmpty()) {
            int[] move = availableMoves.get(random.nextInt(availableMoves.size()));
            makeMove(move[0], move[1]);
        }
    }

    private void updatePlayerTurn() {
        String turnText;
        if (vsBot) {
            turnText = playerXTurn ? "Your Turn" : "Bot's Turn 🤖";
        } else {
            turnText = playerXTurn ? "Player X's Turn" : "Player O's Turn";
        }
        playerTurnTextView.setText(turnText);
        
        int colorFrom = getColor(playerXTurn ? R.color.o_color : R.color.x_color);
        int colorTo = getColor(playerXTurn ? R.color.x_color : R.color.o_color);
        
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator -> 
            playerTurnTextView.setTextColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private boolean checkForWin() {
        String[][] field = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        // Check rows, columns and diagonals
        for (int i = 0; i < 3; i++) {
            if (checkLine(field[i][0], field[i][1], field[i][2]) ||
                checkLine(field[0][i], field[1][i], field[2][i])) {
                return true;
            }
        }
        return checkLine(field[0][0], field[1][1], field[2][2]) ||
               checkLine(field[0][2], field[1][1], field[2][0]);
    }

    private boolean checkLine(String a, String b, String c) {
        return !a.isEmpty() && a.equals(b) && a.equals(c);
    }

    private void showGameEndDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setPositiveButton("Play Again", (dialog, which) -> resetGame())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setTextColor(getColor(R.color.primary));
            }
        }
        roundCount = 0;
        playerXTurn = true;
        gameEnded = false;
        updatePlayerTurn();
    }
}