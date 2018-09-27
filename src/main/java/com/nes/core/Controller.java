package com.nes.core;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class Controller {
    public boolean[] buttons;
    public byte index;
    public byte strobe;

    public void SetButtons(boolean[] buttons) {
        this.buttons = buttons;
    }

    public byte Read() {
        byte value = 0;
        if(this.index < 8 && this.buttons[this.index]){
            value = 1;
        }
        this.index++;
        if((this.strobe&1) == 1){
            this.index = 0;
        }
        return value;
    }

    public void Write(byte value) {
        this.strobe = value;
        if((this.strobe&1) == 1){
            this.index = 0;
        }
    }

}
