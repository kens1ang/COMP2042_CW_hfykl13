package brickGame;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Model {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private Ball ballob;
    private Paddle paddle;
    private SoundManager soundManager;
    private int  heart = 100;
    private int level = 0;
    private int score = 0;
    private long time = 0;
    private long hitTime = 0;
    private long goldTime = 0;
    private int ballRadius = 10;
    private int sceneWidth = 500;
    private static int LEFT  = 1;
    private static int RIGHT = 2;
    private double vX = 2.000;
    private double vY = 2.000;
    private boolean isGoldStauts                = false;
    private boolean isExistHeartBlock           = false;
    private boolean colideToBreak               = false;
    private boolean colideToBreakAndMoveToRight = true;
    private boolean colideToRightWall           = false;
    private boolean colideToLeftWall            = false;
    private boolean colideToRightBlock          = false;
    private boolean colideToBottomBlock         = false;
    private boolean colideToLeftBlock           = false;
    private boolean colideToTopBlock            = false;
    private ArrayList<Block> blocks = new ArrayList<Block>();
    private ArrayList<Bonus> chocos = new ArrayList<Bonus>();
    private ArrayList<Bomb> bombs = new ArrayList<>();
    private Color[]          colors = new Color[]{
            Color.rgb(64, 224, 208),
            Color.rgb(255, 105, 180),
            Color.rgb(143, 0, 255),
            Color.rgb(57, 255, 20),
            Color.rgb(255, 69, 0),
            Color.rgb(135, 206, 235),
            Color.rgb(255, 247, 0),
            Color.rgb(0, 163, 232),
            Color.rgb(158, 196, 0),
            Color.rgb(255, 127, 39),
            Color.rgb(111, 45, 168),
            Color.rgb(237, 28, 36),
            Color.rgb(255, 242, 0),
    };
    public Model() {
        this.paddle = new Paddle();
        this.ballob = new Ball();
        this.soundManager = new SoundManager();
    }

    public void initBallPos() {
        randomballspawn();
        setballpos();
    }

    private void randomballspawn() {
        Random rand = new Random();
        ballob.setGoRightBall(rand.nextBoolean());
        ballob.setGoDownBall(rand.nextBoolean());
    }

    private void setballpos() {
        double horizontalCenter = sceneWidth / 2.0;
        double lowestBlockBottom = ((level + 1) * Block.getHeight()) + Block.getPaddingTop();
        double paddleTop = paddle.getyBreak();
        double verticalCenter = (lowestBlockBottom + paddleTop) / 2.0;
        ballob.setxBall(horizontalCenter);
        ballob.setyBall(verticalCenter);
    }

    public void initBoardModel() {
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < (level == 18 ? 13 : level + 1); j++) {
                int r = random.nextInt(500);
                int type;

                if (level == 18) {
                    type = Block.BLOCK_BOMB;
                } else {
                    if (r % 5 == 0) {
                        continue;
                    } else if (r % 10 == 1) {
                        type = Block.BLOCK_CHOCO;
                    } else if (r % 10 == 2) {
                        type = isExistHeartBlock ? Block.BLOCK_NORMAL : Block.BLOCK_HEART;
                        isExistHeartBlock = true;
                    } else if (r % 10 == 3) {
                        type = Block.BLOCK_STAR;
                    } else if (r % 10 == 7) {
                        type = Block.BLOCK_BOMB;
                    } else {
                        type = Block.BLOCK_NORMAL;
                    }
                }

                blocks.add(new Block(j, i, colors[r % (colors.length)], type));
            }
        }
    }


    public void move(final int direction) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int sleepTime = 4;
                for (int i = 0; i < 30; i++) {
                    if (paddle.getxBreak() == (sceneWidth - paddle.getBreakWidth()) && direction == RIGHT) {
                        return;
                    }
                    if (paddle.getxBreak() == 0 && direction == LEFT) {
                        return;
                    }
                    if (direction == RIGHT) {
                        paddle.addxBreak();
                    } else {
                        paddle.minusxBreak();
                    }
                    paddle.setCenterBreakX(paddle.getxBreak() + paddle.getHalfBreakWidth());
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "Interrupted Exception in move method", e);
                        Thread.currentThread().interrupt();
                    }
                    if (i >= 20) {
                        sleepTime = i;
                    }
                }
            }
        }).start();
    }

    public void updateBallPos() {
        if (ballob.isGoDownBall()) {
            ballob.incyBall(vY);
        } else {
            ballob.dreyBall(vY);
        }

        if (ballob.isGoRightBall()) {
            ballob.incxBall(vX);
        } else {
            ballob.drexBall(vX);
        }
    }

    public void handlerightwallcolide() {
        if (ballob.getxBall() >= sceneWidth - ballRadius) {
            resetCollisionFlags();
            colideToRightWall = true;
            ballob.setGoRightBall(false);
        }
    }

    public void handleleftwallcolide() {
        if (ballob.getxBall() <= ballRadius) {
            resetCollisionFlags();
            colideToLeftWall = true;
            ballob.setGoRightBall(true);
        }
    }

    public void handleceilingcolide() {
        if (ballob.getyBall() <= ballRadius) {
            resetCollisionFlags();
            ballob.setGoDownBall(true);
        }
    }

    public void handlefloorcolideModel() {
        resetCollisionFlags();
        if (!isGoldStauts) {
            soundManager.playMinusHeartSound();
        }
    }

    public void handlefloorcolidegameover() {
        soundManager.playGameOverSound();
    }

    public void handlePaddleCollision() {
        if (ballob.getyBall() >= paddle.getyBreak() - ballRadius && ballob.getyBall() - ballRadius <= paddle.getyBreak() + paddle.getBreakHeight() &&
                ballob.getxBall() >= paddle.getxBreak() - ballRadius && ballob.getxBall() - ballRadius <= paddle.getxBreak() + paddle.getBreakWidth()) {
            hitTime = time;
            resetCollisionFlags();
            colideToBreak = true;
            ballob.setGoDownBall(false);
            updateVelocityOnPaddleCollision();
            if (ballob.getxBall() - paddle.getCenterBreakX() > 0) {
                colideToBreakAndMoveToRight = true;
            } else {
                colideToBreakAndMoveToRight = false;
            }
        }
        if (colideToBreak) {
            if (colideToBreakAndMoveToRight) {
                ballob.setGoRightBall(true);
            } else {
                ballob.setGoRightBall(false);
            }
        }
    }

    public void updateVelocityOnPaddleCollision() {
        double relation = (ballob.getxBall() - paddle.getCenterBreakX()) / (paddle.getBreakWidth() / 2);

        if (Math.abs(relation) <= 0.3) {
            vX = Math.abs(relation);
        } else if (Math.abs(relation) > 0.3 && Math.abs(relation) <= 0.7) {
            vX = (Math.abs(relation) * 1.5) + (level / 3.500);
        } else {
            vX = (Math.abs(relation) * 2) + (level / 3.500);
        }
    }

    public void handleBlockCollisions() {
        if (colideToRightBlock) {
            ballob.setGoRightBall(true);
        }
        if (colideToLeftBlock) {
            ballob.setGoRightBall(false);
        }
        if (colideToTopBlock) {
            ballob.setGoDownBall(false);
        }
        if (colideToBottomBlock) {
            ballob.setGoDownBall(true);
        }
    }

    public void resetCollisionFlags() {
        colideToBreak = false;
        colideToBreakAndMoveToRight = false;
        colideToRightWall = false;
        colideToLeftWall = false;

        colideToRightBlock = false;
        colideToBottomBlock = false;
        colideToLeftBlock = false;
        colideToTopBlock = false;
    }

    public void initchocobombModel() {
        for (Bonus choco : chocos) {
            choco.choco.setY(choco.getY());
        }

        for (Bomb bomb : bombs){
            bomb.getBomb().setY(bomb.getY());
        }
    }

    public void handlehitgoldblockModel() {
        goldTime = time;
        soundManager.playHitStarSound();
        System.out.println("gold ball");
        isGoldStauts = true;
    }

    public void handlehitheartblockModel() {
        heart++;
        soundManager.playCollectHeartSound();
    }

    public void checkhitcode(double hitCode) {
        if (hitCode == Block.HIT_RIGHT) {
            colideToRightBlock = true;
            soundManager.playBlockHitSound();
        }
        if (hitCode == Block.HIT_BOTTOM) {
            colideToBottomBlock = true;
            soundManager.playBlockHitSound();
        }
        if (hitCode == Block.HIT_LEFT) {
            colideToLeftBlock = true;
            soundManager.playBlockHitSound();
        }
        if (hitCode == Block.HIT_TOP) {
            colideToTopBlock = true;
            soundManager.playBlockHitSound();
        }
    }

    public void handlegoldModel() {
        isGoldStauts = false;
        goldTime = 0;
    }

    public boolean chocoHitsBreak(Bonus choco) {
        return choco.getY() >= paddle.getyBreak() && choco.getY() <= paddle.getyBreak() + paddle.getBreakHeight() &&
                choco.getX() >= paddle.getxBreak() && choco.getX() <= paddle.getxBreak() + paddle.getBreakWidth();
    }

    public void handleChocoHitModel(Bonus choco) {
        System.out.println("You Got it and +3 score for you");
        soundManager.playCollectBonusSound();
        choco.setTaken(true);
        score += 3;
    }

    public void updateChocoPosition(Bonus choco) {
        choco.addtoY(((time - choco.getTimeCreated()) / 1000.000) + 1.000);
    }

    public boolean bombHitsPaddle(Bomb bomb) {
        return bomb.getY() >= paddle.getyBreak() && bomb.getY() <= paddle.getyBreak() + paddle.getBreakHeight() &&
                bomb.getX() >= paddle.getxBreak() && bomb.getX() <= paddle.getxBreak() + paddle.getBreakWidth();
    }

    public void handleBombPaddleCollisionModel(Bomb bomb) {
        soundManager.playBombHitSound();
        bomb.setTaken(true);
    }

    public Ball getBallob() {
        return ballob;
    }
    public Paddle getPaddle() {
        return paddle;
    }
    public SoundManager getSoundManager() {
        return soundManager;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public void inclevel() {
        this.level ++;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void incScore(int inc) {
        this.score += inc;
    }
    public int getHeart() {
        return heart;
    }
    public void setHeart(int heart) {
        this.heart = heart;
    }
    public void dreHeart() {
        this.heart--;
    }
    public ArrayList<Block> getBlocks() {
        return blocks;
    }
    public ArrayList<Bomb> getBombs() {
        return bombs;
    }
    public ArrayList<Bonus> getChocos() {
        return chocos;
    }
    public Color[] getColors() {
        return colors;
    }
    public void setColideToTopBlock(boolean colideToTopBlock) {
        this.colideToTopBlock = colideToTopBlock;
    }
    public void setColideToRightWall(boolean colideToRightWall) {
        this.colideToRightWall = colideToRightWall;
    }
    public void setColideToRightBlock(boolean colideToRightBlock) {
        this.colideToRightBlock = colideToRightBlock;
    }
    public void setColideToLeftWall(boolean colideToLeftWall) {
        this.colideToLeftWall = colideToLeftWall;
    }
    public void setColideToLeftBlock(boolean colideToLeftBlock) {
        this.colideToLeftBlock = colideToLeftBlock;
    }
    public void setColideToBreakAndMoveToRight(boolean colideToBreakAndMoveToRight) {
        this.colideToBreakAndMoveToRight = colideToBreakAndMoveToRight;
    }
    public void setColideToBreak(boolean colideToBreak) {
        this.colideToBreak = colideToBreak;
    }
    public void setColideToBottomBlock(boolean colideToBottomBlock) {
        this.colideToBottomBlock = colideToBottomBlock;
    }
    public void setvX(double vX) {
        this.vX = vX;
    }
    public void setExistHeartBlock(boolean existHeartBlock) {
        isExistHeartBlock = existHeartBlock;
    }
    public void setGoldStauts(boolean goldStauts) {
        isGoldStauts = goldStauts;
    }
    public void setGoldTime(long goldTime) {
        this.goldTime = goldTime;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public boolean isColideToTopBlock() {
        return colideToTopBlock;
    }
    public boolean isColideToRightWall() {
        return colideToRightWall;
    }
    public boolean isColideToRightBlock() {
        return colideToRightBlock;
    }
    public boolean isColideToLeftWall() {
        return colideToLeftWall;
    }
    public boolean isColideToLeftBlock() {
        return colideToLeftBlock;
    }
    public boolean isColideToBreakAndMoveToRight() {
        return colideToBreakAndMoveToRight;
    }
    public boolean isColideToBreak() {
        return colideToBreak;
    }
    public boolean isColideToBottomBlock() {
        return colideToBottomBlock;
    }
    public boolean isExistHeartBlock() {
        return isExistHeartBlock;
    }
    public boolean isGoldStauts() {
        return isGoldStauts;
    }
    public long getTime() {
        return time;
    }
    public long getGoldTime() {
        return goldTime;
    }
    public double getvX() {
        return vX;
    }
    public static int getLEFT() {
        return LEFT;
    }
    public static int getRIGHT() {
        return RIGHT;
    }
    public void setHitTime(long hitTime) {
        this.hitTime = hitTime;
    }

}