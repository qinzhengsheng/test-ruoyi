package com.ruoyi.common.utils.pdf;

public class ReceiptPosition {
    private float posX = 0.0F;
    private float posY = 0.0F;
    private float posLastX = 0.0F;
    private float posEndX = 0.0F;
    private float posLastY = 0.0F;
    private float posEndY = 0.0F;
    private String text;

    public ReceiptPosition() {
    }

    public float getPosX() {
        return this.posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return this.posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getPosLastX() {
        return this.posLastX;
    }

    public void setPosLastX(float posLastX) {
        this.posLastX = posLastX;
    }

    public float getPosLastY() {
        return this.posLastY;
    }

    public void setPosLastY(float posLastY) {
        this.posLastY = posLastY;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getPosEndX() {
        return this.posEndX;
    }

    public void setPosEndX(float posEndX) {
        this.posEndX = posEndX;
    }

    public float getPosEndY() {
        return this.posEndY;
    }

    public void setPosEndY(float posEndY) {
        this.posEndY = posEndY;
    }

    public String toString() {
        return "ReceiptPosition [posEndX=" + this.posEndX + ", posEndY=" + this.posEndY + ", posLastX=" + this.posLastX + ", posLastY=" + this.posLastY + ", posX=" + this.posX + ", posY=" + this.posY + ", text=" + this.text + "]";
    }
}
